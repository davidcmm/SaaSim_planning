package planning.heuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;

import planning.util.MachineUsageData;
import planning.util.PlanIOHandler;
import provisioning.DPS;
import provisioning.Monitor;
import provisioning.OptimalProvisioningSystemForHeterogeneousMachines;

import commons.cloud.Contract;
import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;
import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.sim.SimpleSimulator;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;
import commons.sim.util.SimulatorProperties;

/**
 * This class represents the heuristic that plans reservation evaluating machine utilization
 * over a planning period (e.g., one year)
 * @author David Candeia
 *
 */
public class HistoryBasedHeuristic implements PlanningHeuristic{

	private static final long YEAR_IN_HOURS = 8640;
	
	private Map<MachineType, Integer> plan;
	private MachineUsageData machineData;
	private final Monitor monitor;

	private Map<MachineType, Map<Long, Double>> hoursUsed;//map used to save CPU consumption information during the planning period

	/**
	 * Default constructor. 
	 * @param scheduler Event scheduler
	 * @param monitor Application monitor (DPS)
	 * @param loadBalancers Load balancers used in simulation
	 */
	public HistoryBasedHeuristic(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers){
		this.monitor = monitor;
		
		this.plan = new HashMap<MachineType, Integer>();
		
		try {
			machineData = PlanIOHandler.getMachineData();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		if(machineData == null){
			machineData = new MachineUsageData();
		}
		
		this.hoursUsed = new HashMap<MachineType, Map<Long,Double>>();
	}
	
	@Override
	public void findPlan(Provider[] cloudProviders, User[] cloudUsers) {
		
		//Simulating ...
		DPS dps = (DPS) this.monitor;
		
		SimpleSimulator simulator = (SimpleSimulator) Checkpointer.loadApplication();
		
		dps.registerConfigurable(simulator);
		
		simulator.start();
		
		//Calculating machines use data
		LoadBalancer[] loadBalancers = simulator.getTiers(); 
		calculateMachinesUsage();
		Configuration config = Configuration.getInstance();
		
		if(Checkpointer.loadSimulationInfo().isFinishDay()){//Simulation finished!
			
			//Creates reservation plan
			calculateMachinesToReserve(config);
			
			//Evaluates if other number of machines reserved can improve the SaaS provider utility
			try {
				hillClimbingWithReservedMachines(config, dps);
			} catch (ConfigurationException e) {
				throw new RuntimeException(e);
			}
			
			Checkpointer.clear();
			PlanIOHandler.clear();
			try {
				PlanIOHandler.createPlanFile(this.plan, Checkpointer.loadProviders());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			
		}else{//Persist data to other round
			
			persisDataToNextRound(loadBalancers, config);
		}
	}
	
	/**
	 * This method estimates the SaaS provider receipt considering the active users and its periodical and setup fees
	 * @param users Current active users
	 * @return
	 */
	private double estimateReceipt(User[] users) {

		double oneTimeFees = 0d;
		double periodicalIncome = 0d;
		long planningPeriod = Configuration.getInstance().getLong(SimulatorProperties.PLANNING_PERIOD);
		
		for(User user : users){	
			
			Contract contract = user.getContract();
			oneTimeFees += contract.calculateOneTimeFees();
			periodicalIncome += Math.round((planningPeriod / 30.0)) * contract.getPrice();
			
		}
		
		return periodicalIncome + oneTimeFees;
	}
	
	/**
	 * This method searches, using hill climbing, for other reservation configurations that can improve estimated utility. The first plan to be tested is the 
	 * reservation plan calculated from target utilization! The plan with best estimated utility will be established with IaaS provider!
	 * @param config 
	 * @param dps
	 * @throws ConfigurationException
	 */
	//TODO: Make it really work for multiple machines type!
	private void hillClimbingWithReservedMachines(Configuration config, DPS dps) throws ConfigurationException {
		
		Map<MachineType, Integer> bestPlan = new HashMap<MachineType, Integer>();
		double bestUtility = Double.NEGATIVE_INFINITY;
		boolean stop = false;
		Map<MachineType, Integer> currentPlan = this.plan;
		
		Provider provider = config.getProviders()[0];
		
		//Receipt
		double estimatedReceipt = estimateReceipt(config.getUsers());
		
		do{
			//Cost
			double reservedCost = 0d;
			double onDemandCost = 0d;
			
			//Evaluating current plan cost
			for(MachineType type : currentPlan.keySet()){
				int reservedMachines = currentPlan.get(type);
				
				reservedCost += reservedMachines * provider.getReservationOneYearFee(type);//Reservation fee
				
				Map<Long, Double> totalUsed = this.hoursUsed.get(type);
				
				for(Entry<Long, Double> entry : totalUsed.entrySet()){
					long currentNumberOfMachines = entry.getKey();
					double currentHoursConsumed = entry.getValue();
					double totalCurrentConsumed = currentNumberOfMachines * currentHoursConsumed;
					
					if(currentNumberOfMachines <= reservedMachines){//only reserved usage
						reservedCost += totalCurrentConsumed * provider.getReservedCpuCost(type); 
					}else{//on-demand + reserved usage
						double reservedHours = (reservedMachines * 1.0 / currentNumberOfMachines) * totalCurrentConsumed; 
						reservedCost +=  reservedHours * provider.getReservedCpuCost(type);
						onDemandCost += (totalCurrentConsumed - reservedHours) * provider.getOnDemandCpuCost(type);
					}
				}
			}
			
			//Evaluating utility
			double currentUtility = estimatedReceipt - reservedCost - onDemandCost;
			if(currentUtility > bestUtility){
				bestUtility = currentUtility;
				bestPlan = new HashMap<MachineType, Integer>(currentPlan);
				for(MachineType type : currentPlan.keySet()){
					Integer currentNumberOfMachines = currentPlan.get(type);
					currentNumberOfMachines--;
					if(currentNumberOfMachines > 0){
						currentPlan.put(type, currentNumberOfMachines);
					}else{
						stop = true;
					}
				}
			}else{
				stop = true;
			}
		}while(!stop);	
		
		//Updating plan to be effectively used!
		this.plan = bestPlan;
	}

	/**
	 * This method calculates the utilization target at which each machine type reservation is profitable
	 * @param cloudProvider IaaS provider
	 * @param planningPeriod Period being planned 
	 * @return Percentage of minimum utilization for each machine type
	 */
	private Map<MachineType, Double> findReservationTargets(Provider cloudProvider, long planningPeriod) {
		Map<MachineType, Double> typesLimits = new HashMap<MachineType, Double>();
		
		MachineType[] machineTypes = cloudProvider.getAvailableTypes();
		
		for(MachineType type : machineTypes){
			double yearFee = cloudProvider.getReservationOneYearFee(type);
			double reservedCpuCost = cloudProvider.getReservedCpuCost(type);
			double onDemandCpuCost = cloudProvider.getOnDemandCpuCost(type);
			
			long minimumHoursToBeUsed = Math.round(yearFee / (onDemandCpuCost - reservedCpuCost));
			double usageProportion = 1.0 * minimumHoursToBeUsed / (planningPeriod * 24);
			typesLimits.put(type, usageProportion);
		}
		
		return typesLimits;
	}

	/**
	 * Considers that machines which utilization is higher than the target will be aggregated and then a new amount
	 * of machines to be reserved is calculated!
	 * @param config Simulation configurations
	 */
	private void calculateMachinesToReserve(Configuration config) {
		long planningPeriod = config.getLong(SimulatorProperties.PLANNING_PERIOD);
		Map<MachineType, Long> highestNumberOfMachinesUsedPerType = new HashMap<MachineType, Long>();
		
		Map<MachineType, Map<Long, Double>> hoursUsed = this.machineData.getMachineUsagePerType();
		Map<MachineType, Double> utilizationTargets = findReservationTargets(Checkpointer.loadProviders()[0], planningPeriod);
	
		this.hoursUsed = new HashMap<MachineType, Map<Long, Double>>(hoursUsed);
		
		//Updating hours used
		for(MachineType machineType : hoursUsed.keySet()){
			Map<Long, Double> usageData = hoursUsed.get(machineType);
			Map<Long, Double> newUsageData = new HashMap<Long, Double>();
			
			ArrayList<Long> numberOfMachinesList = new ArrayList<Long>(usageData.keySet());
			Collections.sort(numberOfMachinesList);
			Collections.reverse(numberOfMachinesList);
			
			highestNumberOfMachinesUsedPerType.put(machineType, numberOfMachinesList.get(0));
			
			newUsageData.put(numberOfMachinesList.get(0), usageData.get(numberOfMachinesList.get(0)));//First value
			
			for(Long numberOfMachinesUsed : numberOfMachinesList){//Iterating from the highest number of machines to the lowest one
				double higherUsed = usageData.get(numberOfMachinesUsed);
				
				for(long i = numberOfMachinesUsed - 1; i > 0; i--){//If 15 machines were used for 10 hours, 10 machines were also used for 10 hours ...
					Double lowerUsed = (newUsageData.get(i) != null) ? newUsageData.get(i) : usageData.get(i);
					
					if(lowerUsed != null){
						newUsageData.put(i, lowerUsed+higherUsed);
					}
				}
			}
			
			hoursUsed.put(machineType, newUsageData);
		}
		
		//Checking machines which had utilization higher than the target
		for(MachineType type : hoursUsed.keySet()){
			Map<Long, Double> machines = hoursUsed.get(type);
			double totalUsed = 0d;
			
			//Searching for the largest number of machines which is used for an interval greater than the target
			for(long numberOfMachines = highestNumberOfMachinesUsedPerType.get(type); numberOfMachines > 0; numberOfMachines--){
				Double currentUsed = machines.get(numberOfMachines);
			
				if(currentUsed != null && currentUsed > 0){
					
					double currentUtilization = currentUsed / (type.getNumberOfCores() * planningPeriod * 24);
					
					if(currentUtilization >= utilizationTargets.get(type)){
						totalUsed += numberOfMachines * currentUtilization;
						break;
					}
				}
			}
			
			this.plan.put(type, (int)Math.ceil(totalUsed / utilizationTargets.get(type)));
		}
	}
	
	/**
	 * Saving current simulation day information
	 * @param loadBalancers
	 * @param config
	 */
	private void persisDataToNextRound(LoadBalancer[] loadBalancers,
			Configuration config) {
		try {
			Checkpointer.save();
			Checkpointer.dumpMachineData(this.machineData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieving amount of machines used by DPS.
	 */
	private void calculateMachinesUsage() {
		
		//TODO: Change this to collect usage for all machine types
		LinkedList<Integer> usage = ((OptimalProvisioningSystemForHeterogeneousMachines) this.monitor).machinesPerHour;
		for(Integer numberOfMachines : usage){
			machineData.addUsage(MachineType.M1_SMALL, numberOfMachines);
		}
	}

	@Override
	public double getEstimatedProfit(int period) {
		return 0;
	}

	@Override
	public Map<MachineType, Integer> getPlan(User[] cloudUsers) {
		return this.plan;
	}
}

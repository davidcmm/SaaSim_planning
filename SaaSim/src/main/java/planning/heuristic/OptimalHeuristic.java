package planning.heuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

import planning.io.PlanningWorkloadParser;
import planning.util.PlanIOHandler;
import planning.util.Summary;
import provisioning.Monitor;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;

/**
 * Heuristic based on queue networks. This heuristic builds all possible reservation plans
 * and then evaluates each plan considering a queue network in order to find the most
 * profitable plan.
 * 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class OptimalHeuristic implements PlanningHeuristic{
	
	private static final long YEAR_IN_HOURS = 8640;
	
	private Map<User, List<Summary>> summaries;
	private List<MachineType> types;
	
	private IChromosome bestChromosome;
	private double bestFitnessValue;
	
	/**
	 * Default constructor
	 * @param scheduler Event scheduler
	 * @param monitor Application monitor (DPS)
	 * @param loadBalancers Infrastructure load balancers
	 */
	public OptimalHeuristic(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers){
		this.types = new ArrayList<MachineType>();
		this.summaries = new HashMap<User, List<Summary>>();
		this.bestChromosome = null;
		this.bestFitnessValue = Double.MIN_VALUE;
	}
	
	@Override
	public void findPlan(Provider[] cloudProviders, User[] cloudUsers){

		//Reading workload data
		try {
			cloudUsers = commons.config.Configuration.getInstance().getUsers();
			readWorkloadData(cloudUsers);
		} catch (ConfigurationException e1) {
			throw new RuntimeException(e1);
		}
	
		//Configuring genetic algorithm
		Configuration config = new DefaultConfiguration();
		try {

			PlanningFitnessFunction myFunc = createFitnessFunction(cloudUsers, cloudProviders);
			
			//Searching for maximum number of resources that can be reserved for each type
			Map<MachineType, Integer> limits = findReservationLimits(cloudProviders[0]);
			for(MachineType type : limits.keySet()){
				types.add(type);
			}
			int [] currentValues = new int[limits.size()];
			
			evaluateGenes(config, myFunc, cloudProviders[0], limits, 0, currentValues);//Searching best configuration
			myFunc.close();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		//Retrieving reservation plan and saving it
		Map<MachineType, Integer> plan = this.getPlan(cloudUsers);
		try {
			PlanIOHandler.createPlanFile(plan, cloudProviders);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Evaluating all reservation plans
	 * @param config Simulation configuration
	 * @param function Fitness function that evaluates each plan according to a queue network
	 * @param provider IaaS provider
	 * @param limits Machine utilization targets 
	 * @param typesIndex
	 * @param currentValues
	 * @throws InvalidConfigurationException
	 */
	private void evaluateGenes(Configuration config, PlanningFitnessFunction function, Provider provider, Map<MachineType, Integer> limits, int typesIndex, int[] currentValues) throws InvalidConfigurationException {
		if(typesIndex < this.types.size()){//for each machine type
			MachineType machineType = this.types.get(typesIndex);
			Integer limit = limits.get(machineType);
			for(int i = 0; i <= limit; i++){//checking different amount of machines being reserved
				currentValues[typesIndex] = i;
				evaluateGenes(config, function, provider, limits, typesIndex+1, currentValues);
			}
		}else{
			Gene[] genes = new IntegerGene[provider.getAvailableTypes().length];
			for(int i = 0; i < provider.getAvailableTypes().length; i++){//Creating a reservation plan
				genes[i] = new IntegerGene(config);
				genes[i].setAllele(currentValues[i]);
			}
			Chromosome chrom = new Chromosome(config, genes);
			double fitness = function.evaluate(chrom);//Evaluating reservation plan

			if(this.bestChromosome == null || fitness > this.bestFitnessValue){//Saving reservation plan with greatest utility
				this.bestChromosome = chrom;
				this.bestFitnessValue = fitness;
			}
		}
	}
	
	/**
	 * Reading workload statistics
	 * @param cloudUsers
	 */
	private void readWorkloadData(User[] cloudUsers) {
		commons.config.Configuration simConfig = commons.config.Configuration.getInstance();
		String[] workloads = simConfig.getWorkloads();
		
		this.summaries = new HashMap<User, List<Summary>>();
		
		int index = 0;
		for(String workload : workloads){
			PlanningWorkloadParser parser;
			try {
				parser = new PlanningWorkloadParser(workload);
				parser.readData();
				this.summaries.put(cloudUsers[index++], parser.getSummaries());
				
			} catch (ConfigurationException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	/**
	 * Evaluating the maximum number of resources that can be reserved for each machine type.
	 * This evaluation considers the estimated CPU demand and the prices at the IaaS provider.
	 * @param cloudProvider IaaS provider.
	 * @return
	 */
	private Map<MachineType, Integer> findReservationLimits(Provider cloudProvider) {
		Map<MachineType, Integer> typesLimits = new HashMap<MachineType, Integer>();
		
		MachineType[] machineTypes = cloudProvider.getAvailableTypes();
		long totalDemand = calcTotalDemand();
		
		for(MachineType type : machineTypes){
			double yearFee = cloudProvider.getReservationOneYearFee(type);
			double reservedCpuCost = cloudProvider.getReservedCpuCost(type);
			double onDemandCpuCost = cloudProvider.getOnDemandCpuCost(type);
			
			long minimumHoursToBeUsed = Math.round(yearFee / (onDemandCpuCost - reservedCpuCost));
			double usageProportion = 1.0 * minimumHoursToBeUsed / YEAR_IN_HOURS;
			
			int maximumNumberOfMachines = (int)Math.ceil(totalDemand / (usageProportion * YEAR_IN_HOURS));
			typesLimits.put(type, maximumNumberOfMachines);
		}
		
		return typesLimits;
	}
	
	/**
	 * Estimating total workload demand in CPU-hrs
	 * @return
	 */
	private long calcTotalDemand() {
		double totalDemand = 0;
		for(List<Summary> summaries : this.summaries.values()){
			for(Summary summary : summaries){
				totalDemand += summary.getTotalCpuHrs();
			}
		}
		
		return (long)Math.ceil(totalDemand);
	}

	private PlanningFitnessFunction createFitnessFunction(User[] cloudUsers, Provider[] cloudProviders) {
		return new PlanningFitnessFunction(this.summaries, cloudUsers, cloudProviders, this.types);
	}

	@Override
	public double getEstimatedProfit(int period) {
		return 0;
	}

	@Override
	public Map<MachineType, Integer> getPlan(User[] cloudUsers) {
		Map<MachineType, Integer> plan = new HashMap<MachineType, Integer>();
		Gene[] genes = this.bestChromosome.getGenes();
		int index = 0;
		
		for(MachineType type : this.types){
			plan.put(type, (Integer) genes[index++].getAllele());
		}

		return plan;
	}
}

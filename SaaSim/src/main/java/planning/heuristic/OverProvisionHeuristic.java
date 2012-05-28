package planning.heuristic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import planning.util.PlanIOHandler;
import provisioning.DPS;
import provisioning.Monitor;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.Request;
import commons.cloud.User;
import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.io.WorkloadParser;
import commons.sim.SimpleSimulator;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEvent;
import commons.sim.jeevent.JEEventScheduler;
import commons.sim.jeevent.JEEventType;
import commons.sim.util.SimulatorProperties;

/**
 * This class represents the heuristic that elaborates overprovisioned reservation plans.
 *  
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class OverProvisionHeuristic extends SimpleSimulator implements PlanningHeuristic {

	public static final double FACTOR = 0.2;//Overprovisioned infrastructure utilization factor according to Above the Clouds: ...
	private final long COUNTING_PAGE_SIZE = 100;

	private int maximumNumberOfServers;
	private double requestsMeanDemand;
	
	private int[] currentRequestsCounter;
	private int[] nextRequestsCounter;
	private double totalProcessingTime;
	private long numberOfRequests;
	private int size;
	
	/**
	 * Default constructor.
	 * @param scheduler Event scheduler
	 * @param monitor Application monitor (DPS)
	 * @param loadBalancers Load balancers being used
	 */
	public OverProvisionHeuristic(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers){
		super(scheduler, loadBalancers);
		try{
			this.maximumNumberOfServers = PlanIOHandler.getNumberOfMachinesFromFile();
		}catch(Exception e){
			this.maximumNumberOfServers = 0;
		}

		size = (int)(Configuration.getInstance().getParserPageSize().getMillis() / COUNTING_PAGE_SIZE);
		try{
			this.currentRequestsCounter = PlanIOHandler.getNumberOfMachinesArray();
		}catch(Exception e){
			this.currentRequestsCounter = new int[size];
		}
		
		try{
			this.requestsMeanDemand = PlanIOHandler.getRequestsMeanDemandFromFile();
		}catch(Exception e){
			this.requestsMeanDemand = 0d;
		}
		
		this.totalProcessingTime = 0d;
		this.numberOfRequests = 0l;
		this.setMonitor(monitor);
	}

	@Override
	public void findPlan(Provider[] cloudProviders, User[] cloudUsers) {
		
		//Simulating ...
		DPS dps = (DPS) this.monitor;
		
		dps.registerConfigurable(this);
		
		this.start();
		
		//Calculating requests mean demand
		if(this.requestsMeanDemand == 0d){//first day
			this.requestsMeanDemand = this.totalProcessingTime / this.numberOfRequests;
		}else{//other days
			this.requestsMeanDemand = (this.requestsMeanDemand + (this.totalProcessingTime / this.numberOfRequests)) / 2;
		}

		if(Checkpointer.loadSimulationInfo().isFinishDay()){//Simulation finished!
			
			Checkpointer.clear();
			PlanIOHandler.clear();
			Map<MachineType, Integer> plan = this.getPlan(null);
			try {
				PlanIOHandler.createPlanFile(plan, Checkpointer.loadProviders());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}else{
			try {
				Checkpointer.save();
				PlanIOHandler.createNumberOfMachinesFile(this.maximumNumberOfServers, this.nextRequestsCounter, this.requestsMeanDemand);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void handleEvent(JEEvent event) {
		
		switch (event.getType()) {
			case READWORKLOAD:
				if(workloadParser.hasNext()) {
					List<Request> requests = workloadParser.next();
					numberOfRequests += requests.size();
					
					totalProcessingTime += calcNumberOfMachines(requests, event.getScheduledTime());
					evaluateMaximumNumber();
					
					if(workloadParser.hasNext()){
						long newEventTime = getScheduler().now() + Configuration.getInstance().getParserPageSize().getMillis();
						send(new JEEvent(JEEventType.READWORKLOAD, this, newEventTime, true));
					}else{
						workloadParser.close();
					}
				}
				break;
			default:
				break;
		}	
		
	}
	
	/**
	 * Searching maximum number of servers that could be used in parallel
	 */
	private void evaluateMaximumNumber() {
		for(int value : this.currentRequestsCounter){
			if(value > this.maximumNumberOfServers){
				this.maximumNumberOfServers = value;
			}
		}
		
		for(int value : this.nextRequestsCounter){
			if(value > this.maximumNumberOfServers){
				this.maximumNumberOfServers = value;
			}
		}
	}
	
	/**
	 * Evaluates requests demands in order to calculate the number of servers that can
	 * be used in parallel
	 * @param requests Requests to be processed
	 * @param currentTime
	 * @return
	 */
	private double calcNumberOfMachines(List<Request> requests, long currentTime) {
		double totalProcessingTime = 0d;
		
		if(this.nextRequestsCounter != null){
			this.currentRequestsCounter = this.nextRequestsCounter;
		}
		this.nextRequestsCounter = new int[size];
		
		for(Request request : requests){
			int index = (int) ((request.getArrivalTimeInMillis() - currentTime) / this.COUNTING_PAGE_SIZE);
			this.currentRequestsCounter[index]++;//Adding demand in arrival interval
			
			long totalMeanToProcess = request.getMaximumToProcess();
			totalProcessingTime+= totalMeanToProcess;
			
			long intervalsToProcess = totalMeanToProcess / this.COUNTING_PAGE_SIZE;
			if(totalMeanToProcess == this.COUNTING_PAGE_SIZE){
				intervalsToProcess = 0;
			}
			
			for(int i = index+1; i < index + intervalsToProcess; i++){//Adding demand to subsequent intervals
				if(i >= this.currentRequestsCounter.length){
					this.nextRequestsCounter[i - this.currentRequestsCounter.length]++;
				}else{
					this.currentRequestsCounter[i]++;
				}
			}
		}
		
		return totalProcessingTime;
	}

	@Override
	public double getEstimatedProfit(int period) {
		return 0;
	}

	@Override
	public Map<MachineType, Integer> getPlan(User[] cloudUsers) {
		Configuration config = Configuration.getInstance();

		int machinesToReserve = (int)Math.ceil( ( maximumNumberOfServers ) * FACTOR );
		
		Map<MachineType, Integer> plan = new HashMap<MachineType, Integer>();
		MachineType machineType = MachineType.valueOf(config.getString(SimulatorProperties.PLANNING_TYPE).toUpperCase().replace('.', '_'));
		plan.put(machineType, machinesToReserve);
		return plan;
	}
}

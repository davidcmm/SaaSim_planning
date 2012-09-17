package provisioning;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.rank.Percentile;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.Request;
import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.io.GEISTWorkloadParser;
import commons.io.WorkloadParser;
import commons.sim.provisioningheuristics.MachineStatistics;
import commons.sim.util.SaaSAppProperties;
import edu.uah.math.distributions.BinomialDistribution;

/**
 * This class represents the DPS business logic modified from original RANJAN. Here some statistics of current
 * available machines (i.e, utilisation) is used to verify if new machines need to be added to 
 * an application tier, or if some machines can be removed from any application tier. After the number of servers needed
 * is calculated, the DPS verifies if any powerful reserved machine is available to be added and, if not, accelerator nodes
 * are purchased from the cloud provider.
 * 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 *
 */
public class OptimalProvisioningSystemForHeterogeneousMachines extends DynamicProvisioningSystem {

	private static final int QUANTUM_SIZE_IN_MILLIS = 100;

	protected MachineType[] acceleratorTypes = {MachineType.M1_SMALL};
	
	private int oneHourTick;
	private long currentTick;
	private Request[] leftOver;
	private WorkloadParser[] parsers;
	private double[] currentRequestsCounter;
	private double[] nextRequestsCounter;
	private long SLA;
	
	private long totalMeanToProcess;
	private int numberOfRequests;
	public LinkedList<Integer> machinesPerHour;
	
	//Debug
//	private FileWriter writer; 
	
	/**
	 * Default constructor.
	 */
	public OptimalProvisioningSystemForHeterogeneousMachines() {
		super();
		
		String[] workloadFiles = Configuration.getInstance().getWorkloads();
		this.oneHourTick = 1000 * 60 * 60;
		this.currentTick = Checkpointer.loadSimulationInfo().getCurrentDayInMillis() + oneHourTick;
		this.leftOver = new Request[workloadFiles.length];

		this.SLA = Configuration.getInstance().getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME);
		this.nextRequestsCounter = new double[36000];
		this.machinesPerHour =  new LinkedList<Integer>();
		
//		try {
//			this.writer = new FileWriter(new File("machineConsumption.dat"), true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public boolean isOptimal() {
		return true;
	}
	
	/**
	 * Reading future workload and requesting machines!
	 */
	@Override
	public void sendStatistics(long now, MachineStatistics statistics, int tier) {
		
		if(parsers == null){
			String[] workloadFiles = Configuration.getInstance().getWorkloads();
			this.parsers = new WorkloadParser[workloadFiles.length];
			for(int i = 0; i < workloadFiles.length; i++){
				this.parsers[i] = new GEISTWorkloadParser(workloadFiles[i]);
			}
		}
		
		if(this.nextRequestsCounter != null){
			this.currentRequestsCounter = this.nextRequestsCounter;
		}
		this.nextRequestsCounter = new double[36000];
		this.totalMeanToProcess = 0;
		this.numberOfRequests = 0;
		
		for (int i = 0; i < leftOver.length; i++) {//Requests left from previous workload reading
			Request left = leftOver[i];
			if(left != null){
				if(left.getArrivalTimeInMillis() < currentTick){
					countData(left, currentTick - oneHourTick);
					leftOver[i] = null;
				}
			}
		}
		
		for (int i = 0; i < parsers.length; i++) {
			if(leftOver[i] == null){
				WorkloadParser<Request> parser = parsers[i];
				while(parser.hasNext()){
					Request next = parser.next();
					if(next.getArrivalTimeInMillis() < currentTick){
						countData(next, currentTick - oneHourTick);
					}else{
						leftOver[i] = next;
						break;
					}
				}
			}
		}
		
		this.currentTick += oneHourTick;
		
		//Calculating number of machines!
		evaluateNumberOfServersToAdd(tier, statistics.totalNumberOfServers, now);
	}
	
	/**
	 * Evaluating request demand and updating number of parallel servers that can be used
	 * @param request Request to be processed
	 * @param currentTime
	 */
	private void countData(Request request, long currentTime) {
		this.numberOfRequests++;
		
		int index = (int) ((request.getArrivalTimeInMillis() - currentTime) / QUANTUM_SIZE_IN_MILLIS);
		this.currentRequestsCounter[index]++;//Adding demand in arrival interval
		
		long intervalsToProcess = request.getMaximumToProcess() / QUANTUM_SIZE_IN_MILLIS;
		
		for(int i = index+1; i < index + intervalsToProcess; i++){//Adding demand to subsequent intervals
			if(i >= this.currentRequestsCounter.length){
				this.nextRequestsCounter[i - this.currentRequestsCounter.length]++;
			}else{
				this.currentRequestsCounter[i]++;
			}
		}
	}
	
	/**
	 * Calculates the number of servers to be added or removed at next execution interval
	 * @param tier
	 * @param totalNumberOfServers Current number of machines
	 * @param now
	 */
	private void evaluateNumberOfServersToAdd(int tier, long totalNumberOfServers, long now) {
		
		Percentile percentile = new Percentile(Configuration.getInstance().getOptimalDPSPercentile());
		double maximumDemand = percentile.evaluate(currentRequestsCounter);

		//FIXME: Remove this >>> Testing
//		System.err.print(">>>>Counter\t");
//		for(double machine : currentRequestsCounter){
//			System.err.print(machine+"\t");
//		}
//		System.err.println();
		//>>> Testing
		
		int numberOfServers = (int)Math.ceil(maximumDemand);
		this.machinesPerHour.add(numberOfServers);
		
		if(Configuration.getInstance().isDebugMode()){
			System.err.println("REQ:\t"+numberOfServers);
		}
		
		long numberOfServersToAdd = numberOfServers - totalNumberOfServers;
		if(numberOfServersToAdd > 0){//Adding servers
			evaluateMachinesToBeAdded(tier, numberOfServersToAdd);
		}else if(numberOfServersToAdd < 0){//Removing servers
			for (int i = 0; i < -numberOfServersToAdd; i++) {
				configurable.removeServer(tier, false);
			}
		}
		
		//Debug resource consumption information
		if(Configuration.getInstance().isDebugMode()){
			for (Provider provider : providers) {
				int amountOfReservedResources = provider.getAmountOfReservedResources();
				int amountOfOnDemandResources = provider.getAmountOfOnDemandResources();
				int total = amountOfReservedResources + amountOfOnDemandResources;
	
				System.err.println("ACC:\t"+amountOfReservedResources+"\t"+amountOfOnDemandResources+"\t"+total);
			}
		}
		
	}
	
	/**
	 * Buying machines at IaaS providers and adding them to IT infrastructure
	 * @param tier
	 * @param numberOfServersToAdd
	 */
	private void evaluateMachinesToBeAdded(int tier, long numberOfServersToAdd) {
		int serversAdded = 0;
		
		List<MachineType> typeList = Arrays.asList(MachineType.values());
		Collections.reverse(typeList);
		for(MachineType machineType: typeList){//TODO test which order is the best
			for (Provider provider : providers) {
				while(provider.canBuyMachine(true, machineType) && 
						serversAdded + machineType.getNumberOfCores() <= numberOfServersToAdd){
					configurable.addServer(tier, provider.buyMachine(true, machineType), true);
					serversAdded += machineType.getNumberOfCores();
				}
				if(serversAdded == numberOfServersToAdd){
					break;
				}
			}
			if(serversAdded == numberOfServersToAdd){
				break;
			}
		}
		
		//If servers are still needed ...
		if(serversAdded < numberOfServersToAdd){
			
			//Applying on-demand market risk ...
			numberOfServersToAdd = (numberOfServersToAdd - serversAdded);
			serversAdded = 0;

			numberOfServersToAdd = calculateNumberOfServersFromOnDemand(numberOfServersToAdd);
			
			for(MachineType machineType : this.acceleratorTypes){
				for (Provider provider : providers) {
					while(provider.canBuyMachine(false, machineType) && 
							serversAdded + machineType.getNumberOfCores() <= numberOfServersToAdd){
						configurable.addServer(tier, provider.buyMachine(false, machineType), true);
						serversAdded += machineType.getNumberOfCores();
					}
					if(serversAdded == numberOfServersToAdd){
						break;
					}
				}
				if(serversAdded == numberOfServersToAdd){
					break;
				}
			}
		}
	}
	
	/**
	 * This method calculates the number of resources that are successfully acquired from the IaaS on-demand market
	 * considering that there is a DoS risk
	 * @param numberOfServersToAdd The amount of resources required by the Optimal DPS system
	 * @return The number of resources that were successfully acquired from the IaaS on-demand market
	 */
	private long calculateNumberOfServersFromOnDemand(long numberOfServersToAdd) {
		int trials = (int) numberOfServersToAdd;
		double p = 1.0 - Configuration.getInstance().getIaaSOnDemandRisk();
		BinomialDistribution dist = new BinomialDistribution(trials, p);
		
		return (long)dist.simulate();
	}

	@Override
	public void requestQueued(long timeMilliSeconds, Request request, int tier) {
		reportLostRequest(request);
	}
}

package planning.heuristic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

import planning.util.Summary;

import commons.cloud.Contract;
import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;
import commons.cloud.UtilityResultEntry;
import commons.config.Configuration;
import commons.sim.util.SaaSAppProperties;
import commons.sim.util.SimulatorProperties;
import commons.util.SimulationInfo;

/**
 * Fitness function that evaluates a reservation plan utility according to a queue network.
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 *
 */
public class PlanningFitnessFunction extends FitnessFunction{

	private static double LOAD_FACTOR = 5.5;//DPS resource consumption / workload demand

	public static final int HOUR_IN_MILLIS = 3600000;

	private long SUMMARY_LENGTH_IN_SECONDS;
	private double OPTIMAL_UTILIZATION = 0.75;//Resource target utilization
	
	private final Provider[] cloudProviders;
	private final User[] cloudUsers;
	private final Map<User, List<Summary>> summaries;
	private final List<MachineType> types;
	
	private double totalOnDemandConsumedHrs;
	
	//Logging
	private FileWriter writer;
	StringBuffer log = new StringBuffer();
	
	/**
	 * Default constructor.
	 * @param summaries Workload statistics.
	 * @param cloudUsers SaaS clients.
	 * @param cloudProviders IaaS providers.
	 * @param types Machine types. 
	 */
	public PlanningFitnessFunction(Map<User, List<Summary>> summaries, User[] cloudUsers, Provider[] cloudProviders, List<MachineType> types) {
		this.summaries = summaries;
		this.cloudProviders = cloudProviders;
		this.cloudUsers = cloudUsers;
		this.types = types;
		this.totalOnDemandConsumedHrs = 0d;
		
		try{
			this.SUMMARY_LENGTH_IN_SECONDS = Configuration.getInstance().getLong(SimulatorProperties.PLANNING_INTERVAL_SIZE);
		}catch(Exception e){
			this.SUMMARY_LENGTH_IN_SECONDS = 60 * 60;
		}
		
		//Logging
		try {
			writer = new FileWriter(new File("profits.dat"));
		} catch (IOException e) {
		}
	}
	
	/**
	 * Evaluates a reservation plan.
	 */
	@Override
	protected double evaluate(IChromosome arg0) {
		log = new StringBuffer();
		
		this.totalOnDemandConsumedHrs = 0d;
		
		//Since round-robin is used, the total arrival rate is splitted among reserved servers according to each server number of cores
		Map<MachineType, Integer> currentCoresPerMachineType = new HashMap<MachineType, Integer>();
		int index = 0;
		double totalCores = 0;
		for(Gene gene : arg0.getGenes()){
			Integer numberOfMachinesReserved = (Integer)gene.getAllele();
			MachineType type = this.types.get(index);
			
			currentCoresPerMachineType.put(type, (int)Math.round(1.0 * numberOfMachinesReserved * type.getNumberOfCores()));
			totalCores += Math.round(1.0 * numberOfMachinesReserved * type.getNumberOfCores());
			index++;
		}
		
		Map<MachineType, Double> totalRequestsFinished = new HashMap<MachineType, Double>();
		double requestsLostDueToResponseTime = 0;
		double requestsLostDueToThroughput = 0;
		double requestsRemovedFromQueue = 0;
		double ratesDifference = 0d;//Arrival rate not being processed at reserved resources
		double accumulatedRequestsMeanServiceTime = 0d;
		
		int currentSummaryInterval = 0;
		int totalNumberOfIntervals = calcNumberOfIntervals();
		long maxResponseTimeInMillis = Configuration.getInstance().getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME);
		
		while(currentSummaryInterval < totalNumberOfIntervals){//for each hour in the simulation period
			
			double arrivalRate = aggregateArrivals(currentSummaryInterval);
			double meanServiceTimeInMillis = aggregateServiceDemand(currentSummaryInterval);
			accumulatedRequestsMeanServiceTime += meanServiceTimeInMillis;
			
			//Calculating arrival rates per machine type
			Map<MachineType, Double> arrivalRatesPerMachineType = extractArrivalsPerMachineType(currentCoresPerMachineType, totalCores, arrivalRate);
			
			//Assuming a base computing power (e.g., EC2 unit for each core)
			Map<MachineType, Double> throughputPerMachineType = new HashMap<MachineType, Double>();
			double reservedThroughput = 0d;
			double missingThroughput = 0d;
			//TODO: Used to round queues: double totalFinished = 0d;
			
			for(MachineType type : arrivalRatesPerMachineType.keySet()){
				
				Double currentArrivalRate = arrivalRatesPerMachineType.get(type);
				
				double maximumThroughput = 0d;
				if(meanServiceTimeInMillis != 0){
					maximumThroughput = (1 / (meanServiceTimeInMillis/1000)) * currentCoresPerMachineType.get(type);//Using all cores
				}else{
					maximumThroughput = Double.MAX_VALUE;
				}
				
				if(currentArrivalRate > maximumThroughput){//All requests cannot be processed in reserved resources
					throughputPerMachineType.put(type, maximumThroughput);
					missingThroughput += (currentArrivalRate - maximumThroughput);
					reservedThroughput += maximumThroughput;
					
					Double currentFinished = totalRequestsFinished.get(type);
					if(currentFinished == null){
						currentFinished = 0d;
					}
					currentFinished += maximumThroughput * SUMMARY_LENGTH_IN_SECONDS;
					totalRequestsFinished.put(type, currentFinished);
					//totalFinished += currentFinished;
					
				}else{//All requests can be processed in reserved resources!
					throughputPerMachineType.put(type, currentArrivalRate);
					reservedThroughput += currentArrivalRate;
					
					Double currentFinished = totalRequestsFinished.get(type);
					if(currentFinished == null){
						currentFinished = 0d;
					}
					currentFinished += currentArrivalRate * SUMMARY_LENGTH_IN_SECONDS;
					totalRequestsFinished.put(type, currentFinished);
					//totalFinished += currentFinished;
					
				}
			}
			
			if(reservedThroughput == 0){//No arrival at reserved machines!
				missingThroughput = arrivalRate;
			}
			
			//Calculating missing requests. This value is amortized by reserved queue size!
			double requestsMissed = missingThroughput * SUMMARY_LENGTH_IN_SECONDS;
			if(ratesDifference == 0 && reservedThroughput != 0){//Queue starts at this interval, so some requests are not really missed!
				//FIXME: Used to round queues:
//				double queuedRequests = Math.max(( (maxResponseTimeInMillis / meanServiceTimeInMillis) * (HOUR_IN_MILLIS/maxResponseTimeInMillis) * totalCores - totalFinished) * OPTIMAL_UTILIZATION, 0); 
//				requestsMissed = Math.max(requestsMissed - queuedRequests, 0);
//				requestsRemovedFromQueue += Math.min(requestsMissed, queuedRequests);
				
				requestsMissed -= Math.max(requestsMissed -(maxResponseTimeInMillis / meanServiceTimeInMillis) * totalCores * OPTIMAL_UTILIZATION, 0);
				requestsRemovedFromQueue += (maxResponseTimeInMillis / meanServiceTimeInMillis) * totalCores * OPTIMAL_UTILIZATION;
			}
			
			//Evaluating on-demand resources consumption because of requests that could not be processed in reserved resources
			if(requestsMissed > 0){
				int onDemandQueueSize = (int) Math.max(Math.ceil( ((maxResponseTimeInMillis / meanServiceTimeInMillis) * OPTIMAL_UTILIZATION) ), 0);
				int numberOfOnDemandMachines = (int) Math.ceil(requestsMissed / (onDemandQueueSize * HOUR_IN_MILLIS/maxResponseTimeInMillis));
				totalOnDemandConsumedHrs += numberOfOnDemandMachines;
			}
			
			ratesDifference = missingThroughput;
			requestsLostDueToThroughput += requestsMissed;
			
			//Estimated response time
			double totalNumberOfUsers = 0.0;
			if(arrivalRate != 0){
				totalNumberOfUsers = aggregateNumberOfUsers(currentSummaryInterval) * (reservedThroughput / arrivalRate);
			}
			double averageThinkTimeInSeconds = aggregateThinkTime(currentSummaryInterval);
			if(reservedThroughput != 0){
				double responseTimeInSeconds = totalNumberOfUsers / reservedThroughput - averageThinkTimeInSeconds;
				double responseTimeLoss = Math.max( (responseTimeInSeconds * 1000 - maxResponseTimeInMillis)/maxResponseTimeInMillis, 0 );
				requestsLostDueToResponseTime += calcResponseTimeLoss(responseTimeLoss, totalRequestsFinished);
			}
			
			currentSummaryInterval++;
		}
		
		requestsLostDueToThroughput = Math.max(0, requestsLostDueToThroughput);
		
		//Estimating utility
		double receipt = calcReceipt();
		double cost = calcCost(totalRequestsFinished, accumulatedRequestsMeanServiceTime / totalNumberOfIntervals, currentCoresPerMachineType, requestsLostDueToResponseTime, requestsLostDueToThroughput, requestsRemovedFromQueue);
		
		double fitness = receipt - cost;
		
		try {
			writer.write(currentCoresPerMachineType.keySet()+"\t"+currentCoresPerMachineType.values().toString()+"\t"+fitness+"\t"+receipt+"\t"+cost+"\n");
		} catch (IOException e) {
		}
		
		if(fitness < 1){
			return (1/Math.abs(fitness))+1;
		}
		
		return fitness;
		
	}
	
	/**
	 * This method distributes a certain arrival rate to existing reserved machine according to their processing power
	 * @param currentPowerPerMachineType
	 * @param totalPower
	 * @param arrivalRate
	 * @return
	 */
	protected Map<MachineType, Double> extractArrivalsPerMachineType(Map<MachineType, Integer> currentPowerPerMachineType,
			double totalPower, double arrivalRate) {
		Map<MachineType, Double> arrivalRatesPerMachineType = new HashMap<MachineType, Double>();
		for(MachineType type : currentPowerPerMachineType.keySet()){
			if(totalPower == 0){
				arrivalRatesPerMachineType.put(type, 0d);
			}else{
				arrivalRatesPerMachineType.put(type, (currentPowerPerMachineType.get(type) / totalPower) * arrivalRate);
			}
		}
		return arrivalRatesPerMachineType;
	}
	
	/**
	 * This method gets the total amount of requests finished at a certain interval and applies a certain loss according to a percent
	 * of requests that finish after the SLA.
	 * @param responseTimeLoss
	 * @param totalRequestsFinished
	 * @return
	 */
	protected double calcResponseTimeLoss(double responseTimeLoss, Map<MachineType, Double> totalRequestsFinished) {
		double totalFinished = 0d;
		for(Double value : totalRequestsFinished.values()){
			totalFinished += value;
		}
		
		return totalFinished * responseTimeLoss;
	}
	
	/**
	 * This method calculates the number of intervals to be evaluated
	 * @return
	 */
	protected int calcNumberOfIntervals() {
		for(List<Summary> data : this.summaries.values()){
			return data.size();
		}
		
		return 0;
	}
	
	/**
	 * This method calculates penalties to be paid by the SaaS provider since some requests were not processed or some requests were
	 * processed outside the SLA.
	 * @param responseTimeRequestsLost Amount of requests that were processed outside the SLA
	 * @param requestsThatCouldNotBeAttended Amount of requests that were not processed
	 * @param totalRequestsFinished Amount of requests that were succesfully processed
	 * @return The penalty to be paid.
	 */
	protected double calcPenalties(double responseTimeRequestsLost, double requestsThatCouldNotBeAttended, double totalRequestsFinished){
		
		double lossPerUser = (responseTimeRequestsLost + requestsThatCouldNotBeAttended) / (requestsThatCouldNotBeAttended + responseTimeRequestsLost +
				totalRequestsFinished) / this.cloudUsers.length;
		if(totalRequestsFinished == 0){
			lossPerUser = 1;
		}
		
		double penalty = 0d;
		for(User user : this.cloudUsers){
			penalty += user.calculatePenalty(lossPerUser);
		}
		return penalty;
	}

	/**
	 * This method calculates the cost of reserved and on-demand consumption 
	 * @param requestsFinishedPerMachineType
	 * @param meanServiceTimeInMillis
	 * @param currentPowerPerMachineType
	 * @param requestsLostDueToResponseTime
	 * @param requestsLostDueToThroughput
	 * @param requestsRemovedFromQueue
	 * @return
	 */
	protected double calcCost(Map<MachineType, Double> requestsFinishedPerMachineType, double meanServiceTimeInMillis, 
			Map<MachineType, Integer> currentPowerPerMachineType, double requestsLostDueToResponseTime, double requestsLostDueToThroughput, double requestsRemovedFromQueue) {
		
		//Verifying on-demand resources that can be used
//		double onDemandRisk = Configuration.getInstance().getDouble(SimulatorProperties.PLANNING_RISK);
		double onDemandRisk = (Configuration.getInstance().getDouble(SimulatorProperties.PLANNING_NORMAL_RISK) + 
				Configuration.getInstance().getDouble(SimulatorProperties.PLANNING_TRANS_RISK) + 
				Configuration.getInstance().getDouble(SimulatorProperties.PLANNING_PEAK_RISK) ) / 3.0;
		long onDemandResources = Math.round(cloudProviders[0].getOnDemandLimit());
		
		Provider provider = cloudProviders[0];
		double cost = 0;
		
		double totalFinished = 0d;
		for(Double finishedPerType : requestsFinishedPerMachineType.values()){
			totalFinished += finishedPerType;
		}
		
		//Reserved Costs
		long totalRequestsFinished = 0;
		for(MachineType type : requestsFinishedPerMachineType.keySet()){
			totalRequestsFinished += requestsFinishedPerMachineType.get(type);
			double CPUHoursPerType = (requestsFinishedPerMachineType.get(type) * meanServiceTimeInMillis / type.getNumberOfCores() ) / HOUR_IN_MILLIS;
			
			//Distributing requests that were removed from queue at each machine type!
			if(totalFinished != 0){
				double requestsFromQueue = (requestsFinishedPerMachineType.get(type) / totalFinished) * requestsRemovedFromQueue;
				CPUHoursPerType += (requestsFromQueue * meanServiceTimeInMillis/type.getNumberOfCores() ) / HOUR_IN_MILLIS;
			}
			
			CPUHoursPerType *= LOAD_FACTOR;
			
			cost += provider.getReservationOneYearFee(type) * ( currentPowerPerMachineType.get(type)/type.getNumberOfCores() ) 
						+ provider.getReservedCpuCost(type) * CPUHoursPerType;
		}

		
		//On-demand costs
		double onDemandCPUHours = ( LOAD_FACTOR * (requestsLostDueToThroughput * meanServiceTimeInMillis) / HOUR_IN_MILLIS ) * (1-onDemandRisk);
		double adjustedOnDemandCPUHours = totalOnDemandConsumedHrs * LOAD_FACTOR * (1-onDemandRisk);
		
		long requestsThatCouldNotBeAttended = 0;
		long planningPeriod = Configuration.getInstance().getLong(SimulatorProperties.PLANNING_PERIOD);
		if(adjustedOnDemandCPUHours > onDemandResources * planningPeriod * 24){//Demand is greater than what could be retrieved!
			double extraHoursNeeded = onDemandCPUHours - (onDemandResources * planningPeriod * 24);
			
			if(extraHoursNeeded > 0){
				requestsThatCouldNotBeAttended = Math.round( (extraHoursNeeded * HOUR_IN_MILLIS / meanServiceTimeInMillis) );
				totalRequestsFinished -= requestsThatCouldNotBeAttended; 
			}
			
			adjustedOnDemandCPUHours = onDemandResources * planningPeriod * 24;
		}

		totalRequestsFinished += requestsLostDueToThroughput * (1-onDemandRisk);
		requestsThatCouldNotBeAttended += requestsLostDueToThroughput * onDemandRisk;

		cost += provider.getOnDemandCpuCost(MachineType.M1_SMALL) * adjustedOnDemandCPUHours;
		
		//Penalties
		double penalties = calcPenalties(requestsLostDueToResponseTime, requestsThatCouldNotBeAttended, totalRequestsFinished);
		return cost + penalties;
	}
	
	/**
	 * Estimating receipt obtained by SaaS provider. The receipt comes from periodical payments of each SaaS client and from
	 * setup fees.
	 * @return
	 */
	protected double calcReceipt() {
		UtilityResultEntry resultEntry = new UtilityResultEntry(0, this.cloudUsers, this.cloudProviders);

		double oneTimeFees = 0d;
		for(Entry<User, List<Summary>> entry : this.summaries.entrySet()){
			Contract contract = entry.getKey().getContract();
			int index = 0;
			int counter = 0;
			double totalCPUHrs = 0;
			
			for(Summary summary : entry.getValue()){
				counter++;
				totalCPUHrs += summary.getTotalCpuHrs();
				if(counter == (SimulationInfo.daysInMonths[index]+1) * 24){//Calculate receipt for a complete month!
					contract.calculateReceipt(resultEntry, entry.getKey().getId(), (long)Math.ceil(totalCPUHrs * 60 * 60 * 1000), 0l, 0l, 0l);
					index++;
					totalCPUHrs = 0;
				}
			}
			
			oneTimeFees += contract.calculateOneTimeFees();//Setup fees
		}
		
		return resultEntry.getReceipt() + oneTimeFees;
	}

	protected double aggregateThinkTime(int currentSummaryInterval) {
		double thinkTime = 0d;
		int totalNumberOfValues = 0;
		
		for(Entry<User, List<Summary>> entry : this.summaries.entrySet()){
			thinkTime += entry.getValue().get(currentSummaryInterval).getUserThinkTimeInSeconds();
			totalNumberOfValues++;
		}
		
		if(totalNumberOfValues == 0){
			return thinkTime;
		}
		return thinkTime / totalNumberOfValues;
	}

	protected double aggregateNumberOfUsers(int currentSummaryInterval) {
		int totalNumberOfUsers = 0;
		
		for(Entry<User, List<Summary>> entry : this.summaries.entrySet()){
			totalNumberOfUsers += entry.getValue().get(currentSummaryInterval).getNumberOfUsers();
		}
		
		return totalNumberOfUsers;
	}

	protected double aggregateServiceDemand(int currentSummaryInterval) {
		double serviceTime = 0d;
		int totalNumberOfValues = 0;
		
		for(Entry<User, List<Summary>> entry : this.summaries.entrySet()){
			serviceTime += entry.getValue().get(currentSummaryInterval).getRequestServiceDemandInMillis();
			totalNumberOfValues++;
		}
		
		if(totalNumberOfValues == 0){
			return serviceTime;
		}
		
		return serviceTime / totalNumberOfValues;
	}

	protected double aggregateArrivals(int currentSummaryInterval) {
		double totalArrivalRate = 0;
		for(Entry<User, List<Summary>> entry : this.summaries.entrySet()){
			totalArrivalRate += entry.getValue().get(currentSummaryInterval).getArrivalRate();
		}
		
		return totalArrivalRate;
	}

	public void close() {
		try {
			this.writer.close();
		} catch (IOException e) {
		}
	}
}

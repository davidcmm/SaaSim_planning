package planning.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import commons.cloud.MachineType;

/**
 * This class aims to evaluate resource consumption during a planning interval and define an optimal 
 * reservation plan.
 * @author David Candeia
 *
 */
public class OptimumPlanning {
	
	protected static HashMap<MachineType, Double> reservationFees;
	protected static HashMap<MachineType, Double> onDemandFees;
	protected static HashMap<MachineType, Double> reservedFees;
	protected static HashMap<MachineType, Double> minimumConsumption;
	protected static double monitoringFee;
	
	protected static double totalDemand;
	protected static double cost;
	
	private static long minimumConsumed;
	private static long maximumConsumed;
	
	
	public static void main(String[] args) {
		if(args.length != 2){
			System.err.println("Usage: <resource consumption file> <receipts file>");
			System.exit(1);
		}
		
		initValues();
		Map<Long, Double> machinesUsage = readMachineUsage(args[0]);
		double totalReceipt = readReceipt(args[1]);
		
		//Finding maximum number of machines that could be reserved
		Map<MachineType, Long> reservationLimit = findReservationLimit(totalDemand, minimumConsumption);
		
		//Searching for the best reservation plan
		Map<MachineType, Long> reservationPlan = findOptimumReservationPlan(totalReceipt, reservationLimit, machinesUsage);
		
		//Persisting data
		saveData(reservationPlan, totalReceipt, cost, args[0]);
	}
	
	/**
	 * This method saves the optimum plan found as well as business information
	 * @param reservationPlan
	 * @param totalReceipt
	 * @param cost
	 */
	protected static void saveData(Map<MachineType, Long> reservationPlan,
			double totalReceipt, double cost, String usageFile) {
		String scenario = usageFile.split("_")[1];
		try {
			FileWriter writer = new FileWriter(new File("data_"+scenario));
			
			//Utility, receipt and cost
			writer.write((totalReceipt - cost)+"\t"+totalReceipt+"\t"+cost+"\n");
			
			//Reservation plan
			for(MachineType type : reservationPlan.keySet()){
				writer.write("\t"+type.toString()+":\t"+reservationPlan.get(type));
			}
			writer.write("\n");
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method searches for an optimum reservation plan
	 * @param totalReceipt
	 * @param reservationLimit
	 * @param machinesUsage
	 * @return
	 */
	protected static Map<MachineType, Long> findOptimumReservationPlan(double totalReceipt,
			Map<MachineType, Long> reservationLimit,
			Map<Long, Double> machinesUsage) {
		
		double bestCost = Double.MAX_VALUE;
		long bestSmall = 0;
		long bestLarge = 0;
		long bestXLarge = 0;
		
		//Building all financially viable reservation plans
		for(int small = 0; small < reservationLimit.get(MachineType.M1_SMALL); small++){
			
			for(int large = 0; large < reservationLimit.get(MachineType.M1_LARGE); large++){
				
				for(int xLarge = 0; xLarge < reservationLimit.get(MachineType.M1_XLARGE); xLarge++){
					
					//Simulating costs
					double reservationFeesPaid = small * reservationFees.get(MachineType.M1_SMALL) +
						large * reservationFees.get(MachineType.M1_LARGE) + 
						xLarge * reservationFees.get(MachineType.M1_XLARGE);
					double usageFeesPaid = 0d;
					
					if(small == 32 && large == 0 && xLarge == 0){
						System.out.println("32!");
					}else if(small == 6 && large == 1 && xLarge == 6){
						System.out.println("6	1	6");
					}
					
					for(long machinesRequested : machinesUsage.keySet()){//Checking consumption
						
						long totalMachines = machinesRequested;
						double totalHours = machinesUsage.get(machinesRequested);
						
						if(xLarge > 0){//Consuming xLarge resources
							long machinesConsumed = Math.min(totalMachines, xLarge);
							usageFeesPaid += (machinesConsumed * totalHours)* (reservedFees.get(MachineType.M1_XLARGE) + monitoringFee);
							long toConsume = Math.min(totalMachines, xLarge * 4);
							totalMachines -= toConsume;
						}
						if(large > 0 && totalMachines > 0){//Consuming large resources
							long machinesConsumed = Math.min(totalMachines, large);
							usageFeesPaid += (machinesConsumed * totalHours)* (reservedFees.get(MachineType.M1_LARGE) + monitoringFee);
							long toConsume = Math.min(totalMachines, large * 2);
							totalMachines -= toConsume;
						}
						if(small > 0 && totalMachines > 0){//Consuming small resources
							long toConsume = Math.min(totalMachines, small);
							usageFeesPaid += (toConsume * totalHours)* (reservedFees.get(MachineType.M1_SMALL) + monitoringFee);
							totalMachines -= toConsume;
						}
						if(totalMachines > 0){//on-demand resources
							usageFeesPaid += (totalMachines * totalHours)* (onDemandFees.get(MachineType.M1_SMALL) + monitoringFee);
						}
					}
					
					//Checking if a better cost was found!
					if(usageFeesPaid + reservationFeesPaid < bestCost){
						bestCost = usageFeesPaid + reservationFeesPaid;
						bestSmall = small;
						bestLarge = large;
						bestXLarge = xLarge;
					}
				}
				
			}
			
		}
		
		//Saving best reservation plan found
		Map<MachineType, Long> bestPlan = new HashMap<MachineType, Long>();
		bestPlan.put(MachineType.M1_SMALL, bestSmall);
		bestPlan.put(MachineType.M1_LARGE, bestLarge);
		bestPlan.put(MachineType.M1_XLARGE, bestXLarge);
		
		cost = bestCost; 
		
		return bestPlan;
	}

	/**
	 * This method evaluates application demand in order to estimate the maximum 
	 * number of machines that could be reserved
	 * @param totalDemand
	 * @param minimumConsumption
	 * @return
	 */
	protected static Map<MachineType, Long> findReservationLimit(double totalDemand,
			Map<MachineType, Double> minimumConsumption) {
		
		Map<MachineType, Long> reservationLimit = new HashMap<MachineType, Long>();
		
		for(MachineType type : minimumConsumption.keySet()){
//			long limit = (long) Math.ceil(totalDemand / minimumConsumption.get(type));
//			reservationLimit.put(type, limit);
			reservationLimit.put(type, maximumConsumed);
		}
		
		return reservationLimit;
	}

	/**
	 * Machine types and prices considered in simulation
	 */
	protected static void initValues() {
		reservationFees = new HashMap<MachineType, Double>();
		reservationFees.put(MachineType.M1_SMALL, 227.50);
		reservationFees.put(MachineType.M1_LARGE, 910.0);
		reservationFees.put(MachineType.M1_XLARGE, 1820.0);
		
		onDemandFees = new HashMap<MachineType, Double>();
		onDemandFees.put(MachineType.M1_SMALL, 0.085);
		onDemandFees.put(MachineType.M1_LARGE, 0.34);
		onDemandFees.put(MachineType.M1_XLARGE, 0.68);
		
		reservedFees = new HashMap<MachineType, Double>();
		reservedFees.put(MachineType.M1_SMALL, 0.03);
		reservedFees.put(MachineType.M1_LARGE, 0.12);
		reservedFees.put(MachineType.M1_XLARGE, 0.24);
		
		monitoringFee = 0.15;

		minimumConsumption = new HashMap<MachineType, Double>();
		
		double smallConsumption = reservationFees.get(MachineType.M1_SMALL) / (onDemandFees.get(MachineType.M1_SMALL) - reservedFees.get(MachineType.M1_SMALL));
		minimumConsumption.put( MachineType.M1_SMALL, smallConsumption);
		
		double largeConsumption = reservationFees.get(MachineType.M1_LARGE) / (onDemandFees.get(MachineType.M1_LARGE) - reservedFees.get(MachineType.M1_LARGE));
		minimumConsumption.put(MachineType.M1_LARGE, largeConsumption);
		
		double xLargeConsumption = reservationFees.get(MachineType.M1_XLARGE) / (onDemandFees.get(MachineType.M1_XLARGE) - reservedFees.get(MachineType.M1_XLARGE));
		minimumConsumption.put(MachineType.M1_XLARGE, xLargeConsumption);
		
		totalDemand = 0;
		cost = 0;
		minimumConsumed = 0;
		maximumConsumed = 0;
		
	}

	/**
	 * This method returns a map containing for each number of machines used the amount of hours consumed
	 * @param file
	 * @return
	 */
	protected static Map<Long, Double> readMachineUsage(String file) {
		//Input format: ACC:    0       5       5
		
		//Reading machine consumption from file
		Map<Long, Double> usageData = new HashMap<Long, Double>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			while(reader.ready()){
				String[] split = reader.readLine().split("\\s+");
				long numberOfMachinesConsumed = Long.valueOf(split[3]);
				
				Double hoursConsumed = usageData.get(numberOfMachinesConsumed);
				if(hoursConsumed == null){
					hoursConsumed = 0d;
				}
				
				hoursConsumed++;
				usageData.put(numberOfMachinesConsumed, hoursConsumed);
				
				if(numberOfMachinesConsumed < minimumConsumed){
					minimumConsumed = numberOfMachinesConsumed;
				}else if(numberOfMachinesConsumed > maximumConsumed){
					maximumConsumed = numberOfMachinesConsumed;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//Calculating total demand
		for(Entry<Long, Double> entry : usageData.entrySet()){
			totalDemand += entry.getKey() * entry.getValue(); 
		}
		
//		//Updating hours used
//		Map<Long, Double> newUsageData = new HashMap<Long, Double>();
//		
//		ArrayList<Long> numberOfMachinesList = new ArrayList<Long>(usageData.keySet());
//		Collections.sort(numberOfMachinesList);
//		Collections.reverse(numberOfMachinesList);
//		
//		newUsageData.put(numberOfMachinesList.get(0), usageData.get(numberOfMachinesList.get(0)));//First value
//		
//		for(Long numberOfMachinesUsed : numberOfMachinesList){//Iterating from the highest number of machines to the lowest one
//			double higherAmountUsed = usageData.get(numberOfMachinesUsed);
//			
//			for(long i = numberOfMachinesUsed - 1; i > 0; i--){//If 15 machines were used for 10 hours, 10 machines were also used for 10 hours ...
//				Double lowerAmountUsed = (newUsageData.get(i) != null) ? newUsageData.get(i) : usageData.get(i);
//				
//				if(lowerAmountUsed != null){
//					newUsageData.put(i, lowerAmountUsed + higherAmountUsed);
//				}
//			}
//		}
		
		return usageData;
	}


	/**
	 * Reading receipt for each month from a previous generated file
	 * @param file
	 * @return
	 */
	protected static double readReceipt(String file) {
		double totalReceipt = 0d;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			while(reader.ready()){
				String[] data = reader.readLine().split("\\s+");
				double currentReceipt = Double.valueOf(data[0]);
				double transferenceCosts = Double.valueOf(data[1]) + Double.valueOf(data[2]);
				totalReceipt += (currentReceipt - transferenceCosts);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return totalReceipt;
	}

}

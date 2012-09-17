package commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConsumptionAnalyser {
	
	private static int fileIndex = 1;
	
	public static void main(String[] args) {
		
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
			Map<Integer, Integer> reservedUsage = new HashMap<Integer, Integer>();
			Map<Integer, Integer> onDemandUsage = new HashMap<Integer, Integer>();
			
			int higherReservedMachines = 0;
			int higherOnDemandMachines = 0;
			
//			while(reader.ready()){
				
				for (int i = 0; i < 365 * 24 * 3; i++){
					
					String[] split = reader.readLine().split("\t");
					int reservedMachines = Integer.parseInt(split[1]);
					int onDemandMachines = Integer.parseInt(split[2]);

					Integer reservedTotalUsage = reservedUsage.get(reservedMachines);
					Integer onDemandTotalUsage = onDemandUsage.get(onDemandMachines);
					if(reservedTotalUsage == null){
						reservedTotalUsage = 0;
					}
					if(onDemandTotalUsage == null){
						onDemandTotalUsage = 0;
					}
					
					reservedTotalUsage++;
					reservedUsage.put(reservedMachines, reservedTotalUsage);
					if(reservedMachines > higherReservedMachines){
						higherReservedMachines = reservedMachines;
					}
					
					onDemandTotalUsage++;
					onDemandUsage.put(onDemandMachines, onDemandTotalUsage);
					if(onDemandMachines > higherOnDemandMachines){
						higherOnDemandMachines = onDemandMachines;
					}
					
					System.out.println(i);
					
					if(i == 8759 || i == 17519 || i == 26279){
						saveData(reservedUsage, onDemandUsage, higherReservedMachines, higherOnDemandMachines);
						
						reservedUsage = new HashMap<Integer, Integer>();
						onDemandUsage = new HashMap<Integer, Integer>();
						higherReservedMachines = 0;
						higherOnDemandMachines = 0;
					}
					
				}
				
//			}
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
	}

	private static void saveData(Map<Integer, Integer> reservedUsage,
			Map<Integer, Integer> onDemandUsage, int higherReservedMachines, int higherOnDemandMachines) throws IOException {
		
		//Pre-processing data
		Map<Integer, Integer> newReservedUsage = new HashMap<Integer, Integer>();
		for(int i = higherReservedMachines; i >= 0; i--){
			Integer usage = reservedUsage.get(i);
			
			if(usage != null){
				for(int j = 0; j <= i; j++){
					Integer currentUsage = newReservedUsage.get(j);
					if(currentUsage == null){
						currentUsage = 0;
					}
					currentUsage += usage;
					newReservedUsage.put(j, currentUsage);
				}
			}
			
		}
		
		//Pre-processing data
		Map<Integer, Integer> newOnDemandUsage = new HashMap<Integer, Integer>();
		for(int i = higherOnDemandMachines; i >= 0; i--){
			Integer usage = onDemandUsage.get(i);
			
			if(usage != null){
				for(int j = 0; j <= i; j++){
					Integer currentUsage = newOnDemandUsage.get(j);
					if(currentUsage == null){
						currentUsage = 0;
					}
					currentUsage += usage;
					newOnDemandUsage.put(j, currentUsage);
				}
			}
		}
		
		FileWriter writer = new FileWriter(new File("reserved_"+fileIndex+".dat"));
		for(Entry<Integer, Integer> entry : newReservedUsage.entrySet()){
			writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		writer.close();
		
		writer = new FileWriter(new File("onDemand_"+fileIndex+".dat"));
		for(Entry<Integer, Integer> entry : newOnDemandUsage.entrySet()){
			writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		writer.close();
		
		fileIndex++;
	}

}

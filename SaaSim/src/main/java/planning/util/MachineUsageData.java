package planning.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import commons.cloud.MachineType;

/**
 * This class is used to save intervals of time at which different number of machines were used.
 *  
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class MachineUsageData implements Serializable{

	private Map<MachineType, Map<Long, Double>> machineUsagePerType;
	
	public MachineUsageData() {
		this.machineUsagePerType = new HashMap<MachineType, Map<Long,Double>>();
	}

	public MachineUsageData(Map<MachineType, Map<Long, Double>> machineUsagePerType) {
		this.machineUsagePerType = machineUsagePerType;
	}

	public Map<MachineType, Map<Long, Double>> getMachineUsagePerType() {
		return machineUsagePerType;
	}

	public void setMachineUsagePerType(Map<MachineType, Map<Long, Double>> machineUsagePerType) {
		this.machineUsagePerType = machineUsagePerType;
	}
	
	@Deprecated
	public void addUsage(MachineType type, long machineID, double timeInMillis){
		Map<Long, Double> machineTypeData = this.machineUsagePerType.get(type);
		if(machineTypeData == null){
			machineTypeData = new HashMap<Long, Double>();
			this.machineUsagePerType.put(type, machineTypeData);
		}
		
		Double timeUsed = machineTypeData.get(machineID);
		if(timeUsed == null){
			timeUsed = 0d;
		}
		timeUsed = timeInMillis;
		machineTypeData.put(machineID, timeUsed);
	}
	
	/**
	 * Saving usage of a certain number of machines
	 * @param type Machine type
	 * @param numberOfMachines Number of machines used
	 */
	public void addUsage(MachineType type, long numberOfMachines){
		Map<Long, Double> machineTypeData = this.machineUsagePerType.get(type);
		if(machineTypeData == null){
			machineTypeData = new HashMap<Long, Double>();
			this.machineUsagePerType.put(type, machineTypeData);
		}
		
		Double hoursUsed = machineTypeData.get(numberOfMachines);
		if(hoursUsed == null){
			hoursUsed = 0d;
		}
		hoursUsed++;
		machineTypeData.put(numberOfMachines, hoursUsed);		
	}
}

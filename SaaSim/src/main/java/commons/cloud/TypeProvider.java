package commons.cloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import commons.sim.components.MachineDescriptor;
import commons.util.TimeUnit;


/**
 * Abstraction for encapsulate a specific {@link MachineType} market of a {@link Provider}.
 * An IaaS provider renting machines of three different types works like three providers renting
 * each one a single type.
 * 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @version 1.0
 */
public class TypeProvider implements Serializable{
	
	/**
	 * Version 1.0
	 */
	private static final long serialVersionUID = -2384651815453738053L;

	private final int providerID;
	private final MachineType type;

	private final double onDemandCpuCost;
	private final double reservedCpuCost;
	private final double reservationOneYearFee;
	private final double reservationThreeYearsFee;
	private final long alreadyReserved;

	private final List<MachineDescriptor> reservedRunningMachines;
	private final List<MachineDescriptor> reservedFinishedMachines;
	
	private final List<MachineDescriptor> onDemandRunningMachines;
	private final List<MachineDescriptor> onDemandFinishedMachines;
	
	/**
	 * Default constructor.
	 * @param providerID Provider ID
	 * @param type Machine type represented in current TypeProvider
	 * @param onDemandCpuCost Cost of a CPU-hr in on-demand market
	 * @param reservedCpuCost Cost of a CPU-hr in reservation market
	 * @param reservationOneYearFee Cost of reserving a machine during one year
	 * @param reservationThreeYearsFee Cost of reserving a machine during three years
	 * @param alreadyReserved Amount of resources previously reserved
	 */
	public TypeProvider(int providerID, MachineType type,
			double onDemandCpuCost, double reservedCpuCost,
			double reservationOneYearFee, double reservationThreeYearsFee, long reservation) {
		this.providerID = providerID;
		this.type = type;
		this.onDemandCpuCost = onDemandCpuCost;
		this.reservedCpuCost = reservedCpuCost;
		this.reservationOneYearFee = reservationOneYearFee;
		this.reservationThreeYearsFee = reservationThreeYearsFee;
		this.alreadyReserved = reservation;
		
		this.reservedRunningMachines = new ArrayList<MachineDescriptor>();
		this.reservedFinishedMachines = new ArrayList<MachineDescriptor>();

		this.onDemandRunningMachines = new ArrayList<MachineDescriptor>();
		this.onDemandFinishedMachines = new ArrayList<MachineDescriptor>();
	}

	public List<MachineDescriptor> getReservedRunningMachines() {
		return reservedRunningMachines;
	}

	public List<MachineDescriptor> getReservedFinishedMachines() {
		return reservedFinishedMachines;
	}

	public List<MachineDescriptor> getOnDemandRunningMachines() {
		return onDemandRunningMachines;
	}

	public List<MachineDescriptor> getOnDemandFinishedMachines() {
		return onDemandFinishedMachines;
	}

	public MachineType getType() {
		return type;
	}

	public double getOnDemandCpuCost() {
		return onDemandCpuCost;
	}

	public double getReservedCpuCost() {
		return reservedCpuCost;
	}

	public double getReservationOneYearFee() {
		return reservationOneYearFee;
	}

	public double getReservationThreeYearsFee() {
		return reservationThreeYearsFee;
	}

	public long getReservation() {
		return alreadyReserved;
	}

	/**
	 * Reports that the machine represented by such {@link MachineDescriptor} has been turned off.
	 * @param machineDescriptor The machine that has been turned off.
	 * @return <code>true</code> if this provider is responsible for this machine and the report 
	 * has been successfully done, and <code>false</code> otherwise.
	 */
	public boolean shutdownMachine(MachineDescriptor machineDescriptor) {
		if(machineDescriptor.isReserved()){
			if( reservedRunningMachines.remove(machineDescriptor) ){
				return reservedFinishedMachines.add(machineDescriptor);
			}
		}else{
			if( onDemandRunningMachines.remove(machineDescriptor) ){
				return onDemandFinishedMachines.add(machineDescriptor);
			}
		}
		return false;
	}

	/**
	 * Starts a new machine of current {@link MachineType}
	 * @param isReserved A boolean indicating a reserved machine should be started
	 * @return A descriptor of started machine. Returns null if a machine could not be
	 * started
	 */
	public MachineDescriptor buyMachine(boolean isReserved) {
		if(isReserved){
			if(canBuy()){
				MachineDescriptor descriptor = new MachineDescriptor(IDGenerator.GENERATOR.next(), isReserved, getType(), providerID);
				reservedRunningMachines.add(descriptor);
				return descriptor;
			}
		}else{
			MachineDescriptor descriptor = new MachineDescriptor(IDGenerator.GENERATOR.next(), isReserved, getType(), providerID);
			onDemandRunningMachines.add(descriptor);
			return descriptor;
		}
		return null;
	}

	/**
	 * @return <code>true</code> if there are RESERVED machines available to be bought, 
	 * and <code>false</code> otherwise..
	 */
	public boolean canBuy() {
		return reservedRunningMachines.size() < alreadyReserved;
	}

	/**
	 * Calculates the usage cost of all machines used in the last evaluation interval
	 * @param entry The entry at which the costs should be stored
	 * @param currentTimeInMillis Current simulation time
	 * @param monitoringCostPerHour
	 */
	public void calculateMachinesCost(UtilityResultEntry entry, long currentTimeInMillis, double monitoringCostPerHour) {
		long onDemandUpTimeInFullHours = 0;
		long reservedUpTimeInFullHours = 0;
		
		for (MachineDescriptor descriptor : onDemandFinishedMachines) {
			onDemandUpTimeInFullHours += (long) Math.ceil(Math.max(1.0,descriptor.getUpTimeInMillis())/TimeUnit.HOUR.getMillis());
		}
		for (MachineDescriptor descriptor : reservedFinishedMachines) {
			reservedUpTimeInFullHours += (long) Math.ceil(Math.max(1.0,descriptor.getUpTimeInMillis())/TimeUnit.HOUR.getMillis());
		}
		for (MachineDescriptor descriptor : onDemandRunningMachines) {
			onDemandUpTimeInFullHours += (long) Math.ceil(1.0*(currentTimeInMillis - descriptor.getStartTimeInMillis())/TimeUnit.HOUR.getMillis());
			descriptor.reset(currentTimeInMillis);
		}
		for (MachineDescriptor descriptor : reservedRunningMachines) {
			reservedUpTimeInFullHours += (long) Math.ceil(1.0*(currentTimeInMillis - descriptor.getStartTimeInMillis())/TimeUnit.HOUR.getMillis());
			descriptor.reset(currentTimeInMillis);
		}
		
		double onDemandCost = onDemandUpTimeInFullHours * onDemandCpuCost;
		double reservedCost = reservedUpTimeInFullHours * reservedCpuCost;
		double monitoringCost = (onDemandUpTimeInFullHours + reservedUpTimeInFullHours) * monitoringCostPerHour;
		
		entry.addUsageToCost(providerID, type, onDemandUpTimeInFullHours, onDemandCost, reservedUpTimeInFullHours, reservedCost, monitoringCost);
		
		onDemandFinishedMachines.clear();
		reservedFinishedMachines.clear();
	}

	public long[] getTotalTransferences() {
		long [] transferences = new long[2];
		Arrays.fill(transferences, 0);
		for (MachineDescriptor descriptor : onDemandFinishedMachines) {
			transferences[0] += descriptor.getInTransference();
			transferences[1] += descriptor.getOutTransference();
		}
		for (MachineDescriptor descriptor : reservedFinishedMachines) {
			transferences[0] += descriptor.getInTransference();
			transferences[1] += descriptor.getOutTransference();
		}
		for (MachineDescriptor descriptor : onDemandRunningMachines) {
			transferences[0] += descriptor.getInTransference();
			transferences[1] += descriptor.getOutTransference();
		}
		for (MachineDescriptor descriptor : reservedRunningMachines) {
			transferences[0] += descriptor.getInTransference();
			transferences[1] += descriptor.getOutTransference();
		}
		return transferences;
	}

	/**
	 * Calculates the cost incurred because of a previous reservation
	 */
	public double calculateUniqueCost() {
		return alreadyReserved * reservationOneYearFee;
	}
	
}

package commons.cloud;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import commons.sim.components.Machine;
import commons.sim.components.MachineDescriptor;
import commons.util.CostCalculus;

/**
 * IaaS {@link Machine} provider. Based on Amazon EC2 market model.
 * 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 * @version 1.1
 */
public class Provider implements Serializable{
	
	/**
	 * Version 1.1
	 */
	private static final long serialVersionUID = -746266289404954541L;
	private final int id;
	private final String name;
	private final int onDemandLimit;//maximum number of purchased resources that can be active from the on-demand market
	private final int reservationLimit;//maximum number of resources to be acquired in the reservation market
	
	private final Map<MachineType, TypeProvider> types;
	private final double monitoringCost;
	
	private final long[] transferInLimitsInBytes;
	private final double[] transferInCostsPerByte;
	private final long[] transferOutLimitsInBytes;
	private final double[] transferOutCostsPerByte;
	
	private int onDemandRunningMachines;//number of on-demand resources currently in use
	
	/**
	 * Default constructor.
	 * @param id Provider id in simulation
	 * @param name Provider name
	 * @param onDemandLimit Maximum number of purchased resources that can be active from the on-demand market
	 * @param reservationLimit Maximum number of resources that can be acquired in the reservation market
	 * @param monitoringCost The cost to monitor the status of each resource
	 * @param transferInLimitsInBytes Data transference (to IaaS provider) limits at pricing model
	 * @param transferInCostsPerByte Data transference (to IaaS provider) costs at pricing model
	 * @param transferOutLimitsInBytes Data transference (from IaaS provider) limits at pricing model
	 * @param transferOutCostsPerByte Data transference (from IaaS provider) limits at pricing model
	 * @param types Machine types available at IaaS provider
	 */
	public Provider(int id, String name,
			int onDemandLimit, int reservationLimit,
			double monitoringCost,
			long[] transferInLimitsInBytes,
			double[] transferInCostsPerByte, long[] transferOutLimitsInBytes,
			double[] transferOutCostsPerByte, List<TypeProvider> types) {
		this.id = id;
		this.name = name;
		this.onDemandLimit = onDemandLimit;
		this.reservationLimit = reservationLimit;
		this.monitoringCost = monitoringCost;
		this.transferInLimitsInBytes = transferInLimitsInBytes;
		this.transferInCostsPerByte = transferInCostsPerByte;
		this.transferOutLimitsInBytes = transferOutLimitsInBytes;
		this.transferOutCostsPerByte = transferOutCostsPerByte;
		
		this.types = new HashMap<MachineType, TypeProvider>();
		for (TypeProvider machineType : types) {
			this.types.put(machineType.getType(), machineType);
		}
		this.onDemandRunningMachines = 0;
	}

	public int getOnDemandRunningMachines() {
		return onDemandRunningMachines;
	}

	public int getId() {
		return id;
	}

	/**
	 * Avoid using it. It breaks type encapsulation.
	 * 
	 * @return
	 */
	@Deprecated()
	public Map<MachineType, TypeProvider> getTypes() {
		return types;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the onDemandCpuCost
	 */
	public double getOnDemandCpuCost(MachineType type) {
		return types.get(type).getOnDemandCpuCost();
	}

	/**
	 * @return the onDemandLimit
	 */
	public int getOnDemandLimit() {
		return onDemandLimit;
	}

	/**
	 * @return the reservationLimit
	 */
	public int getReservationLimit() {
		return reservationLimit;
	}

	/**
	 * @return the reservedCpuCost
	 */
	public double getReservedCpuCost(MachineType type) {
		return types.get(type).getReservedCpuCost();
	}

	/**
	 * @return the reservationOneYearFee
	 */
	public double getReservationOneYearFee(MachineType type) {
		return types.get(type).getReservationOneYearFee();
	}

	/**
	 * @return the reservationThreeYearsFee
	 */
	public double getReservationThreeYearsFee(MachineType type) {
		return types.get(type).getReservationThreeYearsFee();
	}

	/**
	 * @return the monitoringCost
	 */
	public double getMonitoringCost() {
		return monitoringCost;
	}

	/**
	 * @return the transferInLimits
	 */
	public long[] getTransferInLimits() {
		return transferInLimitsInBytes;
	}

	/**
	 * @return the transferInCosts
	 */
	public double[] getTransferInCosts() {
		return transferInCostsPerByte;
	}

	/**
	 * @return the transferOutLimits
	 */
	public long[] getTransferOutLimits() {
		return transferOutLimitsInBytes;
	}

	/**
	 * @return the transferOutCosts
	 */
	public double[] getTransferOutCosts() {
		return transferOutCostsPerByte;
	}


	public MachineType[] getAvailableTypes() {
		Set<MachineType> set = types.keySet();
		return set.toArray(new MachineType[set.size()]);
	}

	/**
	 * This method evaluates if a machine can be acquired either in the reservation
	 * market or in the on-demand market.
	 * @param reserved A boolean that indicates if a reservation machine is being requested
	 * @param type The machine type being requested
	 * @return
	 */
	public boolean canBuyMachine(boolean reserved, MachineType type) {
		if(!reserved){
			return types.containsKey(type) && onDemandRunningMachines < getOnDemandLimit();
		}
		return types.containsKey(type) && types.get(type).canBuy();
	}
	
	/**
	 * Buy a new machine in this {@link Provider}.
	 * @param isReserved <code>true</code> if such machine is a previously reserved one. 
	 * @param instanceType See {@link MachineType}
	 * @return A new {@link MachineDescriptor} if succeeded in creation, or <code>null</code> otherwise.
	 */
	public MachineDescriptor buyMachine(boolean isReserved, MachineType instanceType) {
		if(!types.containsKey(instanceType)){
			throw new RuntimeException("Attempt to buy a machine of type " + instanceType + " at provider: " + getName());
		}
		MachineDescriptor descriptor = types.get(instanceType).buyMachine(isReserved);
		if(!isReserved && descriptor != null){
			this.onDemandRunningMachines++;
		}
		return descriptor;
	}

	/**
	 * This method is responsible for shutting down a machine at the IaaS provider.
	 * @param machineDescriptor The descriptor of the machine to be turned off.
	 * @return
	 */
	public boolean shutdownMachine(MachineDescriptor machineDescriptor) {
		if(!types.containsKey(machineDescriptor.getType())){
			return false;
		}
		boolean removed = types.get(machineDescriptor.getType()).shutdownMachine(machineDescriptor);
		if(removed && !machineDescriptor.isReserved()){
			onDemandRunningMachines--;
		}
		return removed;
	}
	
	/**
	 * This method calculates the usage cost at current IaaS provider for an interval of time.
	 * @param entry The entry at which the costs should be saved
	 * @param currentTimeInMillis Current simulation time
	 */
	public void calculateCost(UtilityResultEntry entry, long currentTimeInMillis) {
		
		long [] transferences = new long[2];
				
		for (TypeProvider typeProvider : types.values()) {
			long [] typeTransferences = typeProvider.getTotalTransferences();
			transferences[0] += typeTransferences[0];
			transferences[1] += typeTransferences[1];
			typeProvider.calculateMachinesCost(entry, currentTimeInMillis, monitoringCost);
		}
		
		transferences[0] = transferences[0];
		transferences[1] = transferences[1];
		
		double inCost = CostCalculus.calcTransferenceCost(transferences[0], transferInLimitsInBytes, transferInCostsPerByte);
		double outCost = CostCalculus.calcTransferenceCost(transferences[1], transferOutLimitsInBytes, transferOutCostsPerByte);
		
		entry.addTransferenceToCost(id, transferences[0], inCost, transferences[1], outCost);
	}
	
	/**
	 * This method calculates the reservation cost at current IaaS provider
	 * @param result
	 */
	public void calculateUniqueCost(UtilityResult result) {

		for (TypeProvider typeProvider : types.values()) {
			double cost = typeProvider.calculateUniqueCost();
			result.addProviderUniqueCost(id, typeProvider.getType(), cost);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		assert (obj != null) && (getClass() == obj.getClass()): "Can't compare with another class object.";
		
		if (this == obj)
			return true;
		Provider other = (Provider) obj;
		return (id == other.id);
	}

	public int getAmountOfReservedResources() {
		int total = 0;
		for(Entry<MachineType, TypeProvider> entry : types.entrySet()){
			total += entry.getValue().getReservedRunningMachines().size();
		}
		return total;
	}

	public int getAmountOfOnDemandResources() {
		int total = 0;
		for(Entry<MachineType, TypeProvider> entry : types.entrySet()){
			total += entry.getValue().getOnDemandRunningMachines().size();
		}
		return total;
	}
	
}

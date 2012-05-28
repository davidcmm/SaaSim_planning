package commons.cloud;

import java.io.Serializable;
import java.util.Arrays;


/**
 * This class represents a request to be executed in the SaaS provider IT infrastructure. 
 * 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 * @version 1.0
 */
public class Request implements Serializable{
	
	/**
	 * Versino 1.0
	 */
	private static final long serialVersionUID = -6648523605288936095L;
	private int saasClient;
	private int userID;
	private long reqID;
	private long arrivalTimeInMillis;
	private long[] cpuDemandInMillis;
	private long requestSizeInBytes;
	private long responseSizeInBytes;
	
	private long totalProcessed;
	private MachineType machineType;//Type of machine that is processing the request
	
	/**
	 * Default constructor.
	 * @param reqID A request id
	 * @param saasClient SaaS client that submitted the request 
	 * @param userID End user that submitted the request
	 * @param arrivalTimeInMillis Time at which the request arrived to be processed
	 * @param requestSizeInBytes Request size
	 * @param responseSizeInBytes Size of response page
	 * @param cpuDemandInMillis Amount of time needed to process the request
	 */
	public Request(long reqID, int saasClient, int userID, long arrivalTimeInMillis,
			long requestSizeInBytes, long responseSizeInBytes, long[] cpuDemandInMillis) {
		this.saasClient = saasClient;
		this.reqID = reqID;
		this.userID = userID;
		this.arrivalTimeInMillis = arrivalTimeInMillis;
		this.requestSizeInBytes = requestSizeInBytes;
		this.responseSizeInBytes = responseSizeInBytes;
		this.cpuDemandInMillis = cpuDemandInMillis;
		this.totalProcessed = 0;
	}

	public long getReqID() {
		return reqID;
	}

	/**
	 * @return the saasClient
	 */
	public int getSaasClient() {
		return saasClient;
	}
	
	/**
	 * @return the userID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * @return the arrivalTimeInMillis
	 */
	public long getArrivalTimeInMillis() {
		return arrivalTimeInMillis;
	}

	/**
	 * @return the cpuDemandInMillis
	 */
	public long[] getCpuDemandInMillis() {
		return cpuDemandInMillis;
	}

	/**
	 * @return the requestSizeInBytes
	 */
	public long getRequestSizeInBytes() {
		return requestSizeInBytes;
	}

	/**
	 * @return the responseSizeInBytes
	 */
	public long getResponseSizeInBytes() {
		return responseSizeInBytes;
	}

	/**
	 * @return the value
	 */
	public MachineType getValue() {
		return machineType;
	}

	/**
	 * @param machineType
	 */
	public void assignTo(MachineType machineType){
		this.machineType = machineType;
	}

	/**
	 * Updates processed demand value.
	 *  
	 * @param processedDemand
	 */
	public void update(long processedDemand){
		assert processedDemand >= 0 : "Invalid process amount: "+processedDemand+" in request "+this.reqID+" of "+this.saasClient;
		
		this.totalProcessed += Math.min(processedDemand, getTotalToProcess());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (reqID ^ (reqID >>> 32));
		result = prime * result + saasClient;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		assert obj != null: "Comparing with a null object, check code.";
		assert obj.getClass() == getClass(): "Comparing with an object of another class, check code."; 
		
		Request other = (Request) obj;
		if (saasClient != other.saasClient)
			return false;
		return reqID == other.reqID;
	}

	/**
	 * @return
	 */
	public boolean isFinished(){
		return getTotalToProcess() == 0;
	}

	/**
	 * @return
	 */
	public long getTotalToProcess() {
		return getDemand() - this.totalProcessed;
	}
	
	public long getMaximumToProcess() {
		long total = 0;
		for(long demand : this.cpuDemandInMillis){
			if(demand > total){
				total = demand;
			}
		}
		
		return total;
	}
	
	private long getDemand(){
		return cpuDemandInMillis[machineType.ordinal()];
	}

	/**
	 * 
	 */
	public void reset() {
		this.totalProcessed = 0;
		this.machineType = null;
	}

	/**
	 * @return the totalProcessed
	 */
	public long getTotalProcessed() {
		return totalProcessed;
	}

	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public String toString() {
		return "Request [saasClient=" + saasClient + ", reqID=" + reqID
				+ ", userID=" + userID + ", arrivalTime=" + arrivalTimeInMillis
				+ ", cpuDemandInMillis=" + Arrays.toString(cpuDemandInMillis)
				+ ", requestSizeInBytes=" + requestSizeInBytes
				+ ", responseSizeInBytes=" + responseSizeInBytes
				+ ", totalProcessed=" + totalProcessed + ", assignedTo=" + (machineType==null?"Nobody":machineType.toString())
				+ "]";
	}
}

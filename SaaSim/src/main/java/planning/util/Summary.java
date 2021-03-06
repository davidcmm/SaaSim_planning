package planning.util;

import planning.heuristic.OptimalHeuristic;

/**
 * Statistics used by {@link OptimalHeuristic} to represent an hour of a workload.
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class Summary{
	
	double arrivalRate;
	double totalCpuHrs;
	double requestServiceDemandInMillis;
	double userThinkTimeInSeconds;
	long numberOfUsers;
	
	/**
	 * Default constructor.
	 * @param arrivalRate
	 * @param totalCpuHrs
	 * @param serviceDemandInMillis
	 * @param userThinkTimeInSeconds
	 * @param numberOfUsers
	 */
	public Summary(double arrivalRate, double totalCpuHrs,
			double serviceDemandInMillis, double userThinkTimeInSeconds, long numberOfUsers) {
		this.arrivalRate = arrivalRate;
		this.totalCpuHrs = totalCpuHrs;
		this.requestServiceDemandInMillis = serviceDemandInMillis;
		this.userThinkTimeInSeconds = userThinkTimeInSeconds;
		this.numberOfUsers = numberOfUsers;
	}

	public double getArrivalRate() {
		return arrivalRate;
	}

	public void setArrivalRate(double arrivalRate) {
		this.arrivalRate = arrivalRate;
	}

	public double getTotalCpuHrs() {
		return totalCpuHrs;
	}

	public void setTotalCpuHrs(double totalCpuHrs) {
		this.totalCpuHrs = totalCpuHrs;
	}

	public double getRequestServiceDemandInMillis() {
		return requestServiceDemandInMillis;
	}

	public void setRequestServiceDemand(double requestServiceDemand) {
		this.requestServiceDemandInMillis = requestServiceDemand;
	}

	public double getUserThinkTimeInSeconds() {
		return userThinkTimeInSeconds;
	}

	public void setUserThinkTime(double userThinkTime) {
		this.userThinkTimeInSeconds = userThinkTime;
	}

	public long getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(long numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}
}
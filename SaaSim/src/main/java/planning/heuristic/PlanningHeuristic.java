package planning.heuristic;

import java.util.Map;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;

/**
 * Common actions for a planning heuristic
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public interface PlanningHeuristic {
	
	/**
	 * Build a reservation plan
	 * @param cloudProviders IaaS providers
	 * @param cloudUsers SaaS clients.
	 */
	public void findPlan(Provider[] cloudProviders, User[] cloudUsers);
	
	public double getEstimatedProfit(int period);
	
	public Map<MachineType, Integer> getPlan(User[] cloudUsers);
}

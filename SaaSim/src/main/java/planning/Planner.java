package planning;

import java.util.Map;

import planning.heuristic.PlanningHeuristic;
import planning.util.PlanningHeuristicFactory;
import provisioning.Monitor;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;

/**
 * Agent responsible to realize a capacity planning.
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class Planner {

	private Provider[] cloudProviders;
	private PlanningHeuristic planningHeuristic;
	private User[] cloudUsers;
	
	/**
	 * Default constructor
	 * @param scheduler Event scheduler.
	 * @param monitor Application monitor (DPS).
	 * @param loadBalancers Load balancers in use
	 * @param providers IaaS providers
	 * @param cloudUsers SaaS clients
	 */
	public Planner(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers, 
			Provider[] providers, User[] cloudUsers) {
		this.cloudProviders = providers;
		this.planningHeuristic = PlanningHeuristicFactory.createHeuristic(scheduler, monitor, loadBalancers);
		
		this.cloudUsers = cloudUsers;
		this.verifyProperties();
	}
	
	private void verifyProperties() {
		if(this.cloudUsers == null || this.cloudUsers.length == 0){
			throw new RuntimeException("Invalid users in Planner!");
		}
		if(this.cloudProviders == null || this.cloudProviders.length == 0){
			throw new RuntimeException("Invalid cloud providers in Planner!");
		}
	}

	/**
	 * Given the heuristic and the scenario data, this method is responsible for requesting the planning
	 * of the infrastructure
	 */
	public Map<MachineType, Integer> plan() {
		
		//Asking for a plan
		this.planningHeuristic.findPlan(cloudProviders, cloudUsers);
		
		//Persisting planning
		Map<MachineType, Integer> plan = this.planningHeuristic.getPlan(cloudUsers);
		return plan;
	}
}

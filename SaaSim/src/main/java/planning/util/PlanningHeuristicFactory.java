package planning.util;

import planning.heuristic.PlanningHeuristic;
import provisioning.Monitor;

import commons.config.Configuration;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;

/**
 * Factory to create a planning heuristic
 * 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class PlanningHeuristicFactory {
	
	/**
	 * Creating a planning heuristic
	 * @param initargs 
	 * @return
	 */
	public static PlanningHeuristic createHeuristic(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers){
		Class<?> clazz = Configuration.getInstance().getPlanningHeuristicClass();
		
		try {
			return (PlanningHeuristic) clazz.getDeclaredConstructors()[0].newInstance(scheduler, monitor, loadBalancers);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong when loading "+ clazz.getCanonicalName(), e);
		}
	}

}

package commons.config;

import planning.heuristic.HistoryBasedHeuristic;
import planning.heuristic.OptimalHeuristic;
import planning.heuristic.OverProvisionHeuristic;
import planning.heuristic.PlanningHeuristic;

/**
 * Planning heuristics that can be used.
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public enum PlanningHeuristicValues {
	
//	EVOLUTIONARY(AGHeuristic.class), 
	OVERPROVISIONING(OverProvisionHeuristic.class), 
	OPTIMAL(OptimalHeuristic.class), 
	HISTORY(HistoryBasedHeuristic.class);
	
	private final Class<? extends PlanningHeuristic> clazz;

	/**
	 * Default private constructor.
	 * @param className
	 */
	private PlanningHeuristicValues(Class<? extends PlanningHeuristic> clazz){
		this.clazz = clazz;
	}

	public Class<?> getClazz() {
		return clazz;
	}
}

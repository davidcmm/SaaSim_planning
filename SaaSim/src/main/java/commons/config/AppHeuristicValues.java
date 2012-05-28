package commons.config;

import commons.sim.schedulingheuristics.RanjanHeuristic;
import commons.sim.schedulingheuristics.RoundRobinHeuristic;
import commons.sim.schedulingheuristics.RoundRobinHeuristicForHeterogenousMachines;

/**
 * Contains available heuristics that can be used by the load balancer to allocate requests
 * to resources
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public enum AppHeuristicValues {
	
	ROUNDROBIN(RoundRobinHeuristic.class.getCanonicalName()),
	ROUNDROBIN_HET(RoundRobinHeuristicForHeterogenousMachines.class.getCanonicalName()),
	RANJAN(RanjanHeuristic.class.getCanonicalName()), 
	CUSTOM("");
	
	private final String className;

	private AppHeuristicValues(String className){
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
}

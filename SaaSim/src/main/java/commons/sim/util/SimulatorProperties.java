package commons.sim.util;

/**
 * Properties that characterize simulation properties 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class SimulatorProperties {
	
	public static final String DPS_HEURISTIC = "dps.heuristic";
	public static final String DPS_CUSTOM_HEURISTIC = "dps.heuristicclass";
	public static final String DPS_MONITOR_INTERVAL = "dps.monitor.interval";
	public static final String PARSER_IDIOM = "dps.workload.parser";
	public static final String PARSER_PAGE_SIZE = "dps.workload.pagesize";
	public static final String DPS_OPTIMAL_PERCENTILE = "dps.optimal.percentile";

	public static final String MACHINE_NUMBER_OF_TOKENS = "machine.numberoftokens";
	public static final String MACHINE_BACKLOG_SIZE = "machine.backlogsize";
	public static final String MACHINE_SESSION_AFFINITY = "machine.session";
	public static final String MACHINE_SESSION_TIMEOUT = "machine.session.timeout";
	
	public static final String PLANNING_HEURISTIC = "planning.heuristic";
	public static final String PLANNING_PERIOD = "planning.period";
	public static final String PLANNING_TYPE = "planning.type";
	
	public static final String PLANNING_NORMAL_RISK = "planning.normal.risk";
	public static final String PLANNING_TRANS_RISK = "planning.trans.risk";
	public static final String PLANNING_PEAK_RISK = "planning.peak.risk";
	
	public static final String PLANNING_ERROR = "planning.error";
	public static final String PLANNING_INTERVAL_SIZE = "planning.interval.size";

	public static final String PLANNING_USE_ERROR = "planning.enableerror";
	public static final String DEBUG_MODE = "debug.mode";
	
	public static final String WORKLOAD_NORM_TAG = "norm";
	public static final String WORKLOAD_TRANSITION_TAG = "trans"; 
	public static final String WORKLOAD_PEAK_TAG = "peak";
}

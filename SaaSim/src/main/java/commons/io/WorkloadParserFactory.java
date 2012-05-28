/**
 * 
 */
package commons.io;

import java.util.List;

import commons.cloud.Request;
import commons.config.Configuration;

/**
 * Factory responsible to build workload parsers
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 *
 */
public class WorkloadParserFactory {
	
	public static WorkloadParser<List<Request>> getWorkloadParser(){
		return getWorkloadParser(Configuration.getInstance().getParserPageSize().getMillis());
	}

	/**
	 * Building workload parsers
	 * @param pageSize Interval of time to read workload
	 * @return
	 */
	public static WorkloadParser<List<Request>> getWorkloadParser(long pageSize){
		
		assert pageSize > 0: "Invalid page size";
		
		Configuration config = Configuration.getInstance();
		String[] workloads = config.getWorkloads();
		ParserIdiom parserIdiom = config.getParserIdiom();
		
		@SuppressWarnings("unchecked")
		WorkloadParser<Request>[] parsers = new WorkloadParser[workloads.length];
		
		for (int i = 0; i < workloads.length; i++) {
			parsers[i] = parserIdiom.getInstance(workloads[i]);
		}
		
		return new TimeBasedWorkloadParser(pageSize, parsers);
	}
}

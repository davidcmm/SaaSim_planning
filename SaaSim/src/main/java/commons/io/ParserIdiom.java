package commons.io;

import commons.cloud.Request;

/**
 * Workload formats available
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public enum ParserIdiom {

	GEIST(GEISTWorkloadParser.class);
	
	private final Class<?> idiomParserClass;

	private ParserIdiom(Class<?> idiomParserClass){
		this.idiomParserClass = idiomParserClass;
	}

	/**
	 * @return the idiomParserClass
	 */
	public Class<?> getIdiomParserClass() {
		return idiomParserClass;
	}

	@SuppressWarnings("unchecked")
	public WorkloadParser<Request> getInstance(String string) {
		try {
			return (WorkloadParser<Request>) idiomParserClass.getConstructor(String.class).newInstance(string);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

package commons.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import commons.cloud.Request;
import commons.config.Configuration;

/**
 * General methods used for reading workload
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public abstract class AbstractWorkloadParser implements WorkloadParser<Request>{
	
	protected static int saasClientIDSeed = 0;
	
	private BufferedReader reader;
	private int currentDay = 0;
	
	protected int periodsAlreadyRead = 0;
	private Request next;
	
	protected final int saasClientID;
	protected final long shift;
	protected final String workload;

	/**
	 * Default constructor.
	 * 
	 * @param workload Workload file name.
	 * @param saasclientID SaaS client ID.
	 * @param shift Indicates amount of time already simulated. It is used to correct requests
	 * arrival times.
	 */
	public AbstractWorkloadParser(String workload, long shift) {
		this.workload = workload;
		assert workload != null: "Null workload. Please check your configuration and trace files.";
		
		this.shift = shift;
		
		String workloadFile = readFileToUse(workload);
		try {
			this.saasClientID = saasClientIDSeed++;
			this.reader = new BufferedReader(new FileReader(workloadFile));//Using normal load file
			this.next = readNext();
		} catch (FileNotFoundException e) {
			if(workloadFile.isEmpty()){
				throw new RuntimeException("Blank line in " + workload + " file." , e);
			}
			throw new RuntimeException("Problem reading workload file. ", e);
		}
	}
	
	/**
	 * Reads the workload file to be used according to current simulation day
	 * @param workload A file containing the name of each workload related to each
	 * simulation day
	 */
	private String readFileToUse(String workload) {
		this.currentDay = Checkpointer.loadSimulationInfo().getCurrentDay();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(workload));
			String file = reader.readLine();
			int currentLine = 0;
			while(currentLine < this.currentDay){
				currentLine++;
				file = reader.readLine();
			}
			reader.close();
			
			//Configuring which IaaS provider risk to use
			configRiskToUse(file);
			
			return file == null? "": file;
		} catch (Exception e) {
			throw new RuntimeException("Problem reading workload file.", e);
		}
	}
	
	/**
	 * This method configures the IaaS provider risk to be used according to the workload file being
	 * simulated
	 * @param file The workload file
	 */
	private void configRiskToUse(String file) {
		Configuration config = Configuration.getInstance();
		if(!config.isRiskConfigured()){
			config.setRisk(file);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDaysAlreadyRead(int simulatedDays){
		throw new RuntimeException("not yet implemented");
	}
	
	@Override
	public void clear() {
		throw new RuntimeException("not yet implemented");
	}
	
	@Override
	public Request next() {
		Request toReturn = this.next;
		this.next = readNext();
		return toReturn;
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	/**
	 * @return
	 */
	private Request readNext() {
		String line;
		try {
			line = reader.readLine();
			return line == null? null: parseRequest(line);
		} catch (Exception e) {
			throw new RuntimeException("Problem reading workload file.", e);
		}
	}

	/**
	 * @param line
	 * @return
	 */
	protected abstract Request parseRequest(String line);

	@Override
	public void close(){
		try {
			this.reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Problem closing workload file.", e);
		}
	}
}

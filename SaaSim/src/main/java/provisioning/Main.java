package provisioning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import provisioning.util.DPSFactory;

import commons.cloud.UtilityResult;
import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.sim.Simulator;

/**
 * Provisioning simulator execution entry point.
 * 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class Main {
	
	/**
	 * Entry point.
	 * 
	 * @param args Path to the configuration file.
	 * @throws ConfigurationException Related to problems during configuration loading.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException {
		
		if(args.length != 1){
			System.out.println("Usage: java -cp saasim.jar provisioning.Main <property file>");
			System.exit(1);
		}
		
		Configuration.buildInstance(args[0]);
		
		DPS dps = DPSFactory.createDPS();
		
		Simulator simulator = Checkpointer.loadApplication();
		
		dps.registerConfigurable(simulator);
		
		simulator.start();
		
		if(Checkpointer.loadSimulationInfo().isFinishDay()){
			
			UtilityResult utilityResult = dps.calculateUtility();
			System.out.println(utilityResult.getUtility());
			System.out.println(utilityResult);
			
			String events = Checkpointer.loadScheduler().dumpPostMortemEvents();
			if(!events.isEmpty()){
				FileWriter writer = new FileWriter(new File("events.dat"));
				writer.write(events);
				writer.close();
			}
			
			Checkpointer.clear();
		}else{//Persisting dump
			Checkpointer.save();
		}
	}
}

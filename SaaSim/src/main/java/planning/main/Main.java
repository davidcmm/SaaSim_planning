package planning.main;

import org.apache.commons.configuration.ConfigurationException;

import planning.Planner;
import provisioning.DPS;
import provisioning.util.DPSFactory;

import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;

/**
 * This class is responsible for obtaining input parameters from a configuration file: workload, cloud provider
 * data (price, limits, etc.) and planning heuristics to be used. After reading such parameters the capacity planning
 * phase is started
 * 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class Main {
	
	public static void main(String[] args) {
		if(args.length != 1){
			System.err.println("Configuration file is missing!");
			System.exit(1);
		}
		
		try {
			//Loading simulator configuration data
			Configuration.enableParserError();
			Configuration.buildInstance(args[0]);
			
			JEEventScheduler scheduler = Checkpointer.loadScheduler();
			DPS dps = DPSFactory.createDPS();
			LoadBalancer[] loadBalancers = Checkpointer.loadApplication().getTiers();
			
			//Creating planner
			Planner planner = new Planner(scheduler, dps, loadBalancers, Checkpointer.loadProviders(), Checkpointer.loadUsers());
			planner.plan();
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

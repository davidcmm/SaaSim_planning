package commons.sim.util;


import commons.sim.SimpleSimulator;
import commons.sim.Simulator;
import commons.sim.jeevent.JEEventScheduler;

/**
 * Factory that creates the simulator used to simulate the application execution. 
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 */
public class SimulatorFactory {
	
	/**
	 * @param scheduler 
	 */
	public static Simulator buildSimulator(JEEventScheduler scheduler){
		return new SimpleSimulator(scheduler, ApplicationFactory.getInstance().buildApplication(scheduler));
	}
}

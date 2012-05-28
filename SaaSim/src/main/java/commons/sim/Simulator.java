package commons.sim;

import commons.sim.components.LoadBalancer;



/**
 * Defines simulator operations. All simulators are dynamically 
 * configurable entities.
 * 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public interface Simulator extends DynamicConfigurable{
	
	/**
	 * Start simulation.
	 */
	void start();

	LoadBalancer[] getTiers();
}

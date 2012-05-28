package provisioning.util;

import provisioning.DPS;

import commons.config.Configuration;


/**
 * Factory that creates a Dynamic Provisioning System to be used in the simulation.
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class DPSFactory {
	
	/**
	 * 
	 * @param initargs 
	 * @return
	 */
	public static DPS createDPS(Object... initargs){
		Class<?> clazz = Configuration.getInstance().getDPSHeuristicClass();
		
		try {
			return (DPS) clazz.getDeclaredConstructors()[0].newInstance(initargs);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong when loading "+ clazz.getCanonicalName(), e);
		}
	}

	
}

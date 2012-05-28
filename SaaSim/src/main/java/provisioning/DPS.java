package provisioning;


import commons.cloud.UtilityResult;
import commons.sim.DynamicConfigurable;

/**
 * Dynamic Provisioning System interface. A DPS is a {@link Monitor} object too.
 * 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public interface DPS extends Monitor{
	
	/**
	 * @param configurable The new application to provide infrastructure.
	 */
	void registerConfigurable(DynamicConfigurable configurable);
	
	/**
	 * @return Compute application total utility.
	 */
	UtilityResult calculateUtility();

}

package commons.sim.jeevent;

import java.io.Serializable;

/**
 * Interface for an entity responsible for processing simulation events. 
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public interface JEEventHandler extends Serializable{

	/**
	 * @param <T>
	 * @param event
	 */
	void handleEvent(JEEvent event);

	/**
	 * @param event
	 */
	void send(JEEvent event);

	/**
	 * @return
	 */
	int getHandlerId();

	/**
	 * @param event
	 * @param handler
	 */
	void forward(JEEvent event, JEEventHandler handler);

}
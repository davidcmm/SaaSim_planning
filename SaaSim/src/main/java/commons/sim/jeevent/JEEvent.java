/* JEEvent - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package commons.sim.jeevent;

import java.io.Serializable;
import java.util.Arrays;


/**
 * A simulation event.
 *
 * @author thiago - thiago@lsd.ufcg.edu.br
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 *
 */
public class JEEvent implements Comparable<JEEvent>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1977642650443631889L;

	private static int eventIdSeed = 0;
	
	private final int eventId;
    private final int targetHandlerId;
    private long scheduledTime;
    private JEEventType type;
	private Object[] value;
    
    
    /**
     * Default constructor
     * @param type Event type
     * @param targetHandler Event handler
     * @param scheduledTime Time at which the event should be processed
     * @param value Additional objects that are important for event processing
     */
    public JEEvent(JEEventType type, JEEventHandler targetHandler, long scheduledTime, Object... value) {
    	
		this.eventId = eventIdSeed++;
    	
		this.targetHandlerId = targetHandler.getHandlerId();
		this.scheduledTime = scheduledTime;
		this.type = type;
		this.value = value;
		
    }
    
    /**
     * @param event
     * @param targetHandler
     */
    public JEEvent(JEEvent event, JEEventHandler targetHandler) {
		this(event.getType(), targetHandler, event.getScheduledTime(), event.getValue());
	}

	/**
     * @return
     */
    public int getTargetHandlerId() {
    	return targetHandlerId;
    }
    
    /**
     * @return
     */
    public long getScheduledTime() {
    	return scheduledTime;
    }
    
    /**
     * @return
     */
    public int getEventId() {
		return eventId;
	}
    
	/**
	 * @return the type
	 */
	public JEEventType getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(JEEvent o) {
		int result = (scheduledTime < o.scheduledTime ? -1 : (scheduledTime == o.scheduledTime ? 0 : 1));

		if(result != 0){
			return result;
		}
		
		result = type.ordinal() - o.type.ordinal();
		
		return result != 0? result: eventId - o.eventId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return eventId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		assert (obj != null): "Can't compare with null object.";
		assert (getClass() != obj.getClass()): "Can't compare with a different class object.";
		
		if (this == obj)
			return true;
		return (eventId == ((JEEvent) obj).eventId);
	}

	/**
	 * @return
	 */
	public Object[] getValue() {
		return value;
	}
	

	@Override
	public String toString() {
		return "[TIME=" + scheduledTime + ", ID="
				+ eventId + ", Handler=" + targetHandlerId
				+ ", TYPE=" + type + ", V=" + Arrays.toString(value) + "]";
	}
}
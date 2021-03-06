package commons.sim.components;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import provisioning.Monitor;

import commons.cloud.Request;
import commons.config.Configuration;
import commons.sim.jeevent.JEAbstractEventHandler;
import commons.sim.jeevent.JEEvent;
import commons.sim.jeevent.JEEventScheduler;
import commons.sim.jeevent.JEEventType;
import commons.sim.provisioningheuristics.MachineStatistics;
import commons.sim.schedulingheuristics.SchedulingHeuristic;
import commons.sim.util.SaaSAppProperties;

/**
 * Tier load balancer.
 * @author Ricardo Araújo Santos - ricardo@lsd.ufcg.edu.br
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 * @version 1.0
 */
public class LoadBalancer extends JEAbstractEventHandler{
	
	/**
	 * Version 1.0
	 */
	private static final long serialVersionUID = -8572489707494357108L;

	private long MINIMUM_NUMBER_OF_MACHINES = 1;
	
	private final int tier;
	private final List<Machine> servers;
	private final SchedulingHeuristic heuristic;
	private final Queue<Request> requestsToBeProcessed;
	private transient Monitor monitor;

	/**
	 * Default constructor.
	 * @param scheduler Event scheduler.
	 * @param heuristic {@link SchedulingHeuristic}
	 * @param maxServersAllowed Max number of servers to manage in this layer.
	 * @param machines An initial collection of {@link Machine}s.
	 */
	public LoadBalancer(JEEventScheduler scheduler, SchedulingHeuristic heuristic, int maxServersAllowed, int tier) {
		super(scheduler);
		this.heuristic = heuristic;
		this.tier = tier;
		this.servers = new ArrayList<Machine>();
		this.requestsToBeProcessed = new LinkedList<Request>();
		
	}

	/**
	 * Adds a new server acquired from a IaaS provider to load balancer. 
	 * @param useStartUpDelay Boolean indicating if server should be immediately available
	 * or if a startup delay should be used
	 */
	public void addServer(MachineDescriptor descriptor, boolean useStartUpDelay){
		Machine server = buildMachine(descriptor);
		long serverUpTime = getScheduler().now();
		if(useStartUpDelay){
			serverUpTime = serverUpTime + (Configuration.getInstance().getLong(SaaSAppProperties.APPLICATION_SETUP_TIME));
		}
		send(new JEEvent(JEEventType.ADD_SERVER, this, serverUpTime, server));
	}
	
	/**
	 * @param machineDescriptor
	 * @return
	 */
	private Machine buildMachine(MachineDescriptor machineDescriptor) {
		return new TimeSharedMachine(getScheduler(), machineDescriptor, this);
	}
	
	/**
	 * Removes a machine from the list of servers available to the load balancer.
	 */
	public void removeServer(MachineDescriptor descriptor, boolean force){
		for (int i = 0; i < servers.size(); i++) {
			Machine server = servers.get(i);
			if(server.getDescriptor().equals(descriptor)){
				if(force){
					migrateRequests(server);
					send(new JEEvent(JEEventType.MACHINE_TURNED_OFF, this, getScheduler().now(), server));
				}
				servers.remove(server);
				server.shutdownOnFinish();
				heuristic.finishServer(server, i, servers);
				break;// not a concurrent modification because of "break" statement.
			}
		}
	}

	/**
	 * Requests that were being executed when a machine is turned off should be submitted
	 * again to other resource
	 * @param server Machine that was turned off
	 */
	private void migrateRequests(Machine server) {
		long now = getScheduler().now();
		for (Request request : server.getProcessorQueue()) {
			request.reset();
			send(new JEEvent(JEEventType.NEWREQUEST, this, now, request));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(JEEvent event) {
		switch (event.getType()) {
			case NEWREQUEST:
				Request request = (Request) event.getValue()[0];
				Machine nextServer = heuristic.getNextServer(request, getServers());
				if(nextServer != null){//Reusing an existent machine
					nextServer.sendRequest(request);
				}else{
					monitor.requestQueued(getScheduler().now(), request, tier);
				}
				break;
			case ADD_SERVER:
				Machine machine = (Machine) event.getValue()[0];
				machine.getDescriptor().setStartTimeInMillis(getScheduler().now());
				servers.add(machine);
				
				this.heuristic.updateServers(servers);
				
				for (Request queuedRequest : requestsToBeProcessed) {
					send(new JEEvent(JEEventType.NEWREQUEST, this, getScheduler().now(), queuedRequest));
				}
				break;
			case MACHINE_TURNED_OFF:
				monitor.machineTurnedOff((MachineDescriptor)event.getValue()[0]);
				break;
			case REQUESTQUEUED:
				monitor.requestQueued(getScheduler().now(), (Request)event.getValue()[0], tier);
				break;
			default:
				break;
		}
	}
	
	/**
	 * This method is called when the optimal provisioning system is used. It is used to collect current amount of servers being used
	 * by current load balancer.
	 * @param eventTime Current simulation time
	 */
	public void estimateServers(long eventTime) {
		MachineStatistics statistics = new MachineStatistics(0, 0, 0, servers.size());
		monitor.sendStatistics(eventTime, statistics, tier);
	}

	/**
	 * This method is used to collect statistics of current running servers. Such statistics include: machine utilisation, number of
	 * requests that arrived, number of finished requests and current number of servers. 
	 * @param eventTime Current simulation time
	 */
	public void collectStatistics(long eventTime) {
		double averageUtilisation = 0d;
		for(Machine machine : servers){
			averageUtilisation += machine.computeUtilisation(eventTime);
		}
		
		if(!servers.isEmpty()){
			averageUtilisation /= servers.size();
		}
		
		long requestsArrivalCounter = this.heuristic.getRequestsArrivalCounter();
		long finishedRequestsCounter = this.heuristic.getFinishedRequestsCounter();
		this.heuristic.resetCounters();
		
		MachineStatistics statistics = new MachineStatistics(averageUtilisation, requestsArrivalCounter, finishedRequestsCounter, servers.size());
		monitor.sendStatistics(eventTime, statistics, tier);
	}

	/**
	 * Copy of the servers list.
	 * @return the servers
	 */
	public List<Machine> getServers() {
		return new ArrayList<Machine>(servers);
	}
	
	/**
	 * Informs that a request could not be processed
	 * @param requestQueued Request that could not be processed
	 */
	public void reportRequestQueued(Request requestQueued){
		monitor.requestQueued(getScheduler().now(), requestQueued, tier);
	}
	
	/**
	 * Informs that a request was processed
	 * @param requestFinished Request that was processed
	 */
	public void reportRequestFinished(Request requestFinished) {
		
		heuristic.reportRequestFinished();
		monitor.requestFinished(requestFinished);
	}

	/**
	 * Removes any server from the list of available servers to the load balancer.
	 * @param force
	 */
	public void removeServer(boolean force) {
		if(servers.size() <= MINIMUM_NUMBER_OF_MACHINES){
			return;
		}

		for (int i = servers.size()-1; i >= 0; i--) {
			MachineDescriptor descriptor = servers.get(i).getDescriptor();
			if(!descriptor.isReserved()){
				removeServer(descriptor, force);
				return;
			}
		}
		removeServer(servers.get(servers.size()-1).getDescriptor(), force);
	}

	public int getTier() {
		return tier;
	}

	/**
	 * @param monitor the monitor to set
	 */
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}

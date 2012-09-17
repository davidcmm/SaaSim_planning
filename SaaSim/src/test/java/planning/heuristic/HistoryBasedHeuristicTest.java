package planning.heuristic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import planning.util.MachineUsageData;
import planning.util.PlanIOHandler;
import provisioning.OptimalProvisioningSystemForHeterogeneousMachines;
import provisioning.util.DPSFactory;
import util.ValidConfigurationTest;

import commons.cloud.Contract;
import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.TypeProvider;
import commons.cloud.User;
import commons.config.Configuration;
import commons.io.Checkpointer;
import commons.sim.SimpleSimulator;
import commons.sim.components.LoadBalancer;
import commons.sim.jeevent.JEEventScheduler;
import commons.sim.schedulingheuristics.RoundRobinHeuristic;
import commons.sim.util.SaaSAppProperties;
import commons.sim.util.SimulatorFactory;
import commons.sim.util.SimulatorProperties;
import commons.util.SimulationInfo;
import commons.util.TimeUnit;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("org.apache.log4j.*")
@PrepareForTest({SimulatorFactory.class, DPSFactory.class, Configuration.class, PlanIOHandler.class, Checkpointer.class})
public class HistoryBasedHeuristicTest extends ValidConfigurationTest{
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		buildFullConfiguration();
		Checkpointer.clear();
	}
	
	@After
	public void tearDown(){
		Checkpointer.clear();
	}
	
	@Test
	public void testFindPlanWithoutServersAndOneDayFinished() throws ConfigurationException, ClassNotFoundException{
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		types.add(new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 227.50, 350, 10));
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		dps.machinesPerHour = usageData;

		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(0, plan.size());
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithoutServersAndMoreSimulationDaysNeeded() throws ClassNotFoundException, ConfigurationException{

		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 2);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		types.add(new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 227.50, 350, 10));
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(4);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{});
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(2l);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);

		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(0, plan.size());
		
		assertTrue(new File(Checkpointer.MACHINE_DATA_DUMP).exists());
		assertTrue(new File(Checkpointer.CHECKPOINT_FILE).exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithWellUsedServersAndOneTierAndCheapOnDemand() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 0.623287671, 350, 10);
		
		types.add(typeProvider);
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 12; i++){//12 hours 
			usageData.add(1);
		}
		for(int i = 0; i < 12; i++){//12 hours 
			usageData.add(3);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(3, (int)plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithWellUsedServersAndOneTierAndExpensiveOnDemand() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 100.0, 0.03, 227.50, 350, 10);
		
		types.add(typeProvider);
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 12; i++){//12 hours 
			usageData.add(1);
		}
		for(int i = 0; i < 12; i++){//12 hours 
			usageData.add(3);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		/*
		 * Three machines used for 50% of time ends up initially in 18 reservations because of expensive on-demand hour (100). 
		 * Since three machines is the maximum number of machines really used in parallel, it is better to reserve 3 machines!
		 */
		assertEquals(3, (int)plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithOneServersUsedForThreeHoursAndOneTier() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 227.50, 350, 10);
		types.add(typeProvider);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		usageData.add(1);
		usageData.add(1);
		usageData.add(1);
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(0, (int)plan.get(MachineType.M1_SMALL));//Three machines used just one hour a day ends up in no machines being reserved
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithThreeServersUsedForOneHourAndOneTier() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 227.50, 350, 10);
		types.add(typeProvider);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		usageData.add(3);
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(0, (int)plan.get(MachineType.M1_SMALL));//Three machines used just one hour a day ends up in no machines being reserved
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithUnderUsedServersAndOneTier() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 227.50, 350, 10);
		types.add(typeProvider);//0.4787037037037037 minimum utilisation
		
		types.add(new TypeProvider(1, MachineType.C1_MEDIUM, 0.17, 0.06, 455, 700, 10));
		types.add(new TypeProvider(1, MachineType.M1_XLARGE, 0.68, 0.24, 1820, 2800, 10));
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		
		//One machine used for six hours, other machine used for 7 hours and a third one used for 2 hours
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		usageData.add(1);
		for(int i = 0; i < 5; i++){
			usageData.add(2);
		}
		usageData.add(3);
		usageData.add(3);
		
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);

		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(0, (int)plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithAWellUsedServerAndOneTier() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 0.623287671, 350, 10);
		types.add(typeProvider);//0.4787037037037037 minimum utilisation
		
		types.add(new TypeProvider(1, MachineType.C1_MEDIUM, 0.17, 0.06, 455, 700, 10));
		types.add(new TypeProvider(1, MachineType.M1_XLARGE, 0.68, 0.24, 1820, 2800, 10));
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 17; i++){//One machine used for seventeen hours
			usageData.add(2);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);

		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(2, (int)plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithMixedUsedServersAndMoreThanOneDay() throws ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		
		TypeProvider typeProvider2 = new TypeProvider(1, MachineType.M1_SMALL, 0.17, 0.06, 3.739726027, 700, 10);
		types.add(typeProvider2);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		Provider[] providers = new Provider[]{provider};
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(3l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);

		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[1];
		loadBalancers[0] = lb1;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		
		//One machine used for 72 hours, one machine used for 48 hours and one machine used for 8 hours
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 24; i++){
			usageData.add(1);
		}
		for(int i = 0; i < 8; i++){
			usageData.add(3);
		}
		for(int i = 0; i < 40; i++){
			usageData.add(2);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		/*
		 * Two machines have utilization greater than 47%, so initially three machines are reserved. 
		 * Evaluating resource consumption and costs the heuristic verifies that 2 machines are used most of the time and 
		 * reduces the amount reserved to two machines.
		 */
		assertEquals(2, (int)plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithWellUsedServersAndMoreThanOneExecution() throws IOException, ClassNotFoundException, ConfigurationException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 2);
		SimulationInfo simulationInfo2 = new SimulationInfo(2, 0, 2);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider2 = new TypeProvider(1, MachineType.M1_SMALL, 0.68, 0.24, 9.972602, 2800, 10);
		types.add(typeProvider2);

		TypeProvider typeProvider3 = new TypeProvider(1, MachineType.M1_LARGE, 0.085, 0.03, 1.246575, 350, 10);
		types.add(typeProvider3);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		//Machine usage data to be used in the second day
		Map<MachineType, Map<Long, Double>> machineUsagePerType = new HashMap<MachineType, Map<Long,Double>>();
		Map<Long, Double> value = new HashMap<Long, Double>();
		value.put(1l, 48d);//One machine used for 48 hours
		machineUsagePerType.put(MachineType.M1_LARGE, value);
		
		Map<Long, Double> value3 = new HashMap<Long, Double>();
		value3.put(2l, 24d);//Two machines used for 24 hours
		machineUsagePerType.put(MachineType.M1_SMALL, value3);
		
		MachineUsageData machineData = new MachineUsageData(machineUsagePerType);
		Provider[] providers = new Provider[]{provider};
		
		PowerMock.mockStatic(PlanIOHandler.class);
		EasyMock.expect(PlanIOHandler.getMachineData()).andReturn(null);
		EasyMock.expect(PlanIOHandler.getMachineData()).andReturn(machineData);
		Map<MachineType, Integer> map = new HashMap<MachineType, Integer>();
		map.put(MachineType.M1_LARGE, 1);
		map.put(MachineType.M1_SMALL, 4);
		PlanIOHandler.clear();
		PlanIOHandler.createPlanFile(map, providers);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		//Configuration
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(6);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo2);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(2l).times(4);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0).times(2);
		
		PowerMock.replay(Configuration.class, PlanIOHandler.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb2 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb3 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[3];
		loadBalancers[0] = lb1;
		loadBalancers[1] = lb2;
		loadBalancers[2] = lb3;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		EasyMock.expectLastCall().times(2);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 24; i++){
			usageData.add(2);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, lb2, lb3, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(0, plan.size());
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertTrue(output.exists());
		
		//Second day
		output.delete();
		
		heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(2, plan.size());
		/*
		 * Two small machines have utilization greater than 47%, so initially five machines are reserved. Two
		 * large machines are also initially reserved. Evaluating the plans with one less machines the heuristic
		 * calculates a better utility with 4 small and 1 large machines. Other configurations are not tested
		 * since the large machines are in the limit of reservation.
		 * 
		 */
		assertEquals(4, (int)plan.get(MachineType.M1_SMALL));
		assertEquals(1, (int)plan.get(MachineType.M1_LARGE));
		
		output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithPreviousWellUsedServersAndMultipleTiers() throws ClassNotFoundException, ConfigurationException, IOException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.17, 0.06, 1.246575, 700, 10);
		types.add(typeProvider);
		
		TypeProvider typeProvider2 = new TypeProvider(1, MachineType.M1_XLARGE, 0.68, 0.24, 4.986301, 2800, 10);
		types.add(typeProvider2);
		
		TypeProvider typeProvider3 = new TypeProvider(1, MachineType.M1_LARGE, 0.085, 0.03, 0.6232877, 350, 10);
		types.add(typeProvider3);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		//Adding previous machine usage data
		Map<MachineType, Map<Long, Double>> machineUsagePerType = new HashMap<MachineType, Map<Long,Double>>();
		Map<Long, Double> value2 = new HashMap<Long, Double>();
		value2.put(1l, 4 * 24d);//One machine used for 24 hours
		machineUsagePerType.put(MachineType.M1_XLARGE, value2);
		Map<Long, Double> value3 = new HashMap<Long, Double>();
		value3.put(1l, 2 * 24d);//One machine used for 24 hours
		machineUsagePerType.put(MachineType.M1_LARGE, value3);
		
		MachineUsageData machineData = new MachineUsageData(machineUsagePerType);
		Provider[] providers = new Provider[]{provider};
		
		PowerMock.mockStatic(PlanIOHandler.class);
		EasyMock.expect(PlanIOHandler.getMachineData()).andReturn(machineData);
		Map<MachineType, Integer> map = new HashMap<MachineType, Integer>();
		map.put(MachineType.M1_LARGE, 1);
		map.put(MachineType.M1_XLARGE, 1);
		map.put(MachineType.M1_SMALL, 1);
		PlanIOHandler.clear();
		PlanIOHandler.createPlanFile(map, providers);
		
		//Configuration
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb2 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb3 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[3];
		loadBalancers[0] = lb1;
		loadBalancers[1] = lb2;
		loadBalancers[2] = lb3;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		for(int i = 0; i < 24; i++){
			usageData.add(1);
		}
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, lb2, lb3, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(3, plan.size());
		/*
		 * Each machine type have utilization greater than 47%, so initially three machines are reserved per type. Evaluating 
		 * resource consumption the heuristic calculates that one machine of each type is enough to process the demand with
		 * a better utility since only one machine was really consumed.
		 */
		assertEquals(1, (int)plan.get(MachineType.M1_XLARGE));
		assertEquals(1, (int)plan.get(MachineType.M1_SMALL));
		assertEquals(1, (int)plan.get(MachineType.M1_LARGE));
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithPreviousUnderUsedServersAndMultipleTiers() throws ClassNotFoundException, ConfigurationException, IOException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.M1_SMALL, 0.085, 0.03, 0.6232877, 350, 10);
		types.add(typeProvider);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		
		//Adding previous machine usage data
		Map<MachineType, Map<Long, Double>> machineUsagePerType = new HashMap<MachineType, Map<Long,Double>>();
		Map<Long, Double> value = new HashMap<Long, Double>();
		value.put(1l, 1d);//One machine used for 1 hour
		value.put(2l, 2d);//Two machines used for 2 hour
		value.put(3l, 1d);//Three machines used for 1 hour
		machineUsagePerType.put(MachineType.M1_SMALL, value);
		
		MachineUsageData machineData = new MachineUsageData(machineUsagePerType);
		Provider[] providers = new Provider[]{provider};
		
		PowerMock.mockStatic(PlanIOHandler.class);
		EasyMock.expect(PlanIOHandler.getMachineData()).andReturn(machineData);
		Map<MachineType, Integer> map = new HashMap<MachineType, Integer>();
		map.put(MachineType.M1_SMALL, 0);
		PlanIOHandler.clear();
		PlanIOHandler.createPlanFile(map, providers);
		
		//Configuration
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb2 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb3 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[3];
		loadBalancers[0] = lb1;
		loadBalancers[1] = lb2;
		loadBalancers[2] = lb3;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, lb2, lb3, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(1, plan.size());
		assertEquals(new Integer(0), plan.get(MachineType.M1_SMALL));
		
		File output = new File(Checkpointer.MACHINE_DATA_DUMP);
		assertFalse(output.exists());
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testFindPlanWithPreviousMixedUsedServersAndMultipleTiers() throws ClassNotFoundException, ConfigurationException, IOException{
		
		SimulationInfo simulationInfo = new SimulationInfo(1, 0, 1);
		
		//Provider configuration
		List<TypeProvider> types = new ArrayList<TypeProvider>();
		TypeProvider typeProvider = new TypeProvider(1, MachineType.T1_MICRO, 0.17, 0.06, 1.246575, 700, 10);
		types.add(typeProvider);
		
		TypeProvider typeProvider2 = new TypeProvider(1, MachineType.M1_SMALL, 0.68, 0.24, 4.986301, 2800, 10);
		types.add(typeProvider2);
		
		Provider provider = new Provider(1, "p1", 10, 20, 0.15, new long[]{0}, new double[]{0, 0}, new long[]{0}, new double[]{0, 0}, 
				types);
		User user = new User(1, new Contract("p1", 0, 100, 120, 500 * 1000 * 60 * 60 * 24, 0.1, new long[]{0}, new double[]{0},
				600 * 1024 * 1024, 0.1), 500 * 1024 * 1024);
		Provider[] providers = new Provider[]{provider};
		
		//Adding previous machine usage data
		Map<MachineType, Map<Long, Double>> machineUsagePerType = new HashMap<MachineType, Map<Long,Double>>();
		Map<Long, Double> value = new HashMap<Long, Double>();
		value.put(1l, 2d);//One machine used for 2 hours
		value.put(2l, 20d);//Two machines used for 20 hours
		machineUsagePerType.put(MachineType.M1_SMALL, value);
		Map<Long, Double> value2 = new HashMap<Long, Double>();
		value2.put(1l, 3d);//One machine used for 3 hours
		value2.put(2l, 21d);//Two machines used for 20 hours
		machineUsagePerType.put(MachineType.T1_MICRO, value2);
		
		MachineUsageData machineData = new MachineUsageData(machineUsagePerType);
		
		PowerMock.mockStatic(PlanIOHandler.class);
		EasyMock.expect(PlanIOHandler.getMachineData()).andReturn(machineData);
		Map<MachineType, Integer> map = new HashMap<MachineType, Integer>();
		map.put(MachineType.M1_SMALL, 2);
		map.put(MachineType.T1_MICRO, 2);
		PlanIOHandler.clear();
		PlanIOHandler.createPlanFile(map, providers);
		
		//Configuration
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders");
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
		EasyMock.expect(config.getLong(SimulatorProperties.DPS_MONITOR_INTERVAL)).andReturn(5000l);
		EasyMock.expect(config.getString(SaaSAppProperties.APPLICATION_FACTORY)).andReturn("commons.sim.util.SimpleApplicationFactory");
		EasyMock.expect(config.getInt(SaaSAppProperties.APPLICATION_NUM_OF_TIERS)).andReturn(1);
		Class<?>[] classes = new Class<?>[]{Class.forName(RoundRobinHeuristic.class.getCanonicalName())};
		EasyMock.expect(config.getApplicationHeuristics()).andReturn(classes);
		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_MAX_SERVER_PER_TIER)).andReturn(new int[]{1});
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(providers).times(2);
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(simulationInfo);
		EasyMock.expect(config.getProviders()).andReturn(providers).times(2);
		EasyMock.expect(config.getUsers()).andReturn(new User[]{user}).times(2);
		EasyMock.expect(config.getParserPageSize()).andReturn(TimeUnit.MINUTE);
		EasyMock.expect(config.getLong(SimulatorProperties.PLANNING_PERIOD)).andReturn(1l).times(3);
		EasyMock.expect(config.getDouble(SimulatorProperties.PLANNING_ERROR)).andReturn(0.0);
		
		PowerMock.replay(Configuration.class);
		EasyMock.replay(config);
		
		//Load balancer
		LoadBalancer lb1 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb2 = EasyMock.createStrictMock(LoadBalancer.class);
		LoadBalancer lb3 = EasyMock.createStrictMock(LoadBalancer.class);
		
		LoadBalancer[] loadBalancers = new LoadBalancer[3];
		loadBalancers[0] = lb1;
		loadBalancers[1] = lb2;
		loadBalancers[2] = lb3;
		
		//Simulator
		SimpleSimulator simulator = EasyMock.createStrictMock(SimpleSimulator.class);
		simulator.start();
		EasyMock.expect(simulator.getTiers()).andReturn(loadBalancers);
		
		PowerMock.mockStatic(SimulatorFactory.class);
		EasyMock.expect(SimulatorFactory.buildSimulator(EasyMock.isA(JEEventScheduler.class))).andReturn(simulator);
		
		//Provisioning system
		OptimalProvisioningSystemForHeterogeneousMachines dps = EasyMock.createStrictMock(OptimalProvisioningSystemForHeterogeneousMachines.class);
		dps.registerConfigurable(simulator);
		LinkedList<Integer> usageData = new LinkedList<Integer>();
		dps.machinesPerHour = usageData;
		
		PowerMock.replay(SimulatorFactory.class);
		PowerMock.replayAll(lb1, lb2, lb3, simulator, dps);
		
		Checkpointer.loadData();
		JEEventScheduler scheduler = Checkpointer.loadScheduler();
		
		HistoryBasedHeuristic heuristic = new HistoryBasedHeuristic(scheduler, dps, loadBalancers);
		heuristic.findPlan(null, null);
		
		Map<MachineType, Integer> plan = heuristic.getPlan(null);
		assertNotNull(plan);
		assertEquals(2, plan.size());
		/*
		 * Each machine type have utilization greater than 47%, so initially four machines are reserved per type. Evaluating 
		 * resource consumption the heuristic calculates that two machines of each type are enough to process the demand with
		 * a better utility since only two machines were really consumed.
		 */
		assertEquals(2, (int)plan.get(MachineType.M1_SMALL));
		assertEquals(2, (int)plan.get(MachineType.T1_MICRO));
		
		PowerMock.verifyAll();
	}
}

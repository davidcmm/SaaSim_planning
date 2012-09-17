package provisioning;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import commons.cloud.MachineType;
import commons.cloud.Provider;
import commons.cloud.User;
import commons.config.Configuration;
import commons.config.PropertiesTesting;
import commons.io.Checkpointer;
import commons.io.GEISTWorkloadParser;
import commons.io.WorkloadParser;
import commons.sim.AccountingSystem;
import commons.sim.SimpleSimulator;
import commons.sim.components.MachineDescriptor;
import commons.sim.provisioningheuristics.MachineStatistics;
import commons.sim.util.SaaSAppProperties;
import commons.util.SimulationInfo;
import edu.uah.math.distributions.BinomialDistribution;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Configuration.class, Checkpointer.class})
public class OptimalProvisioningSystemForHeterogeneousMachinesTest {
	
	@Test
	public void testConstructor() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.WORKLOAD});
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info);

		PowerMock.replayAll(config);
		
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		Field field = OptimalProvisioningSystemForHeterogeneousMachines.class.getDeclaredField("parsers");
		field.setAccessible(true);
		assertNull(((WorkloadParser[])field.get(op)));
		
		field = OptimalProvisioningSystemForHeterogeneousMachines.class.getDeclaredField("currentTick");
		field.setAccessible(true);
		assertEquals(new Long(1000 * 60 * 60), (Long)field.get(op));
		
		PowerMock.verifyAll();
	}
	
	@Test
	public void testIsOptimal(){
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.WORKLOAD});
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info);

		PowerMock.replayAll(config);
		
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		assertTrue(op.isOptimal());
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithoutRequests() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(6);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.WORKLOAD}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		Provider provider = EasyMock.createMock(Provider.class);
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info);
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		GEISTWorkloadParser parser = EasyMock.createMock(GEISTWorkloadParser.class);
		EasyMock.expect(parser.hasNext()).andReturn(false);
		
		PowerMock.expectNew(GEISTWorkloadParser.class, PropertiesTesting.WORKLOAD).andReturn(parser);
		
		MachineStatistics statistics = EasyMock.createMock(MachineStatistics.class);
		
		PowerMock.replayAll(config, provider, user, accounting, parser, statistics);
		
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithSequentialRequestsAndReservedResources() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(7);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.ONE_SERVER_WORKLOAD_POINTER}).times(2);
		config.setRisk(PropertiesTesting.ONE_SERVER_WORKLOAD);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(true).times(2);
		EasyMock.expect(provider.buyMachine(true, MachineType.M1_SMALL)).andReturn(machineDescriptor);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithSequentialRequestsAndOnDemandResourcesWithoutRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.ONE_SERVER_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.ONE_SERVER_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.0);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(2);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithSequentialRequestsAndOnDemandResourcesWithRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.ONE_SERVER_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.ONE_SERVER_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(1.0);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({BinomialDistribution.class, Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithSequentialRequestsAndOnDemandResourcesWithtRisk2() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		BinomialDistribution dist = EasyMock.createMock(BinomialDistribution.class);
		PowerMock.expectNew(BinomialDistribution.class, 1, 0.9).andReturn(dist);
		EasyMock.expect(dist.simulate()).andReturn(1.0);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.ONE_SERVER_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.ONE_SERVER_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.1);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(1);//Probability distribution in use
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(1);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(1);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(BinomialDistribution.class, dist, config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithParallelRequestsAndReservedResources() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(7);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.FOUR_SERVERS_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.FOUR_SERVERS_WORKLOAD);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(true).times(5);
		EasyMock.expect(provider.buyMachine(true, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(4);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(4);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithParallelRequestsAndOnDemandResourcesWithoutRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.FOUR_SERVERS_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.FOUR_SERVERS_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.0);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(5);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(4);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(4);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({BinomialDistribution.class, Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithParallelRequestsAndOnDemandResourcesWithRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		BinomialDistribution dist = EasyMock.createMock(BinomialDistribution.class);
		PowerMock.expectNew(BinomialDistribution.class, 4, 0.5).andReturn(dist);
		EasyMock.expect(dist.simulate()).andReturn(2.0);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.FOUR_SERVERS_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.FOUR_SERVERS_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.5);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(2);//Probability distribution in use!
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(2);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(2);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(BinomialDistribution.class, dist, config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithParallelRequestsAndMixedResourcesWithoutRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.FOUR_SERVERS_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.FOUR_SERVERS_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.0);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(true).times(3);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.buyMachine(true, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(3);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(2);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(4);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
	
	@Test
	@PrepareForTest({BinomialDistribution.class, Configuration.class, Checkpointer.class, OptimalProvisioningSystemForHeterogeneousMachines.class})
	public void testSendStatisticsWithParallelRequestsAndMixedResourcesWithRisk() throws Exception{
		SimulationInfo info = new SimulationInfo(0, 0, 2);
		
		BinomialDistribution dist = EasyMock.createMock(BinomialDistribution.class);
		PowerMock.expectNew(BinomialDistribution.class, 1, 0.7).andReturn(dist);
		EasyMock.expect(dist.simulate()).andReturn(1.0);
		
		Configuration config = EasyMock.createMock(Configuration.class);
		PowerMock.mockStatic(Configuration.class);
		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(8);
		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.FOUR_SERVERS_WORKLOAD_POINTER}).times(2);
		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(8000l);
		EasyMock.expect(config.isRiskConfigured()).andReturn(false);
		config.setRisk(PropertiesTesting.FOUR_SERVERS_WORKLOAD);
		EasyMock.expect(config.getIaaSOnDemandRisk()).andReturn(0.3);
		EasyMock.expect(config.isDebugMode()).andReturn(false).times(2);
		EasyMock.expect(config.getOptimalDPSPercentile()).andReturn(0.95);
		
		MachineDescriptor machineDescriptor = EasyMock.createMock(MachineDescriptor.class);
		
		Provider provider = EasyMock.createMock(Provider.class);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_MEDIUM)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.C1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_LARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(true).times(3);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.buyMachine(true, MachineType.M1_SMALL)).andReturn(machineDescriptor).times(3);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M1_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_2XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.M2_4XLARGE)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(true, MachineType.T1_MICRO)).andReturn(false);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(true).times(1);
		EasyMock.expect(provider.canBuyMachine(false, MachineType.M1_SMALL)).andReturn(false);
		EasyMock.expect(provider.buyMachine(false, MachineType.M1_SMALL)).andReturn(machineDescriptor);
		
		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
		configurable.addServer(0, machineDescriptor, true);
		EasyMock.expectLastCall().times(4);
		
		User user = EasyMock.createMock(User.class);
		AccountingSystem accounting = EasyMock.createMock(AccountingSystem.class);
		
		PowerMock.mockStaticPartial(Checkpointer.class, "loadSimulationInfo", "loadProviders", "loadUsers", "loadAccountingSystem");
		EasyMock.expect(Checkpointer.loadSimulationInfo()).andReturn(info).anyTimes();
		EasyMock.expect(Checkpointer.loadProviders()).andReturn(new Provider[]{provider});
		EasyMock.expect(Checkpointer.loadUsers()).andReturn(new User[]{user});
		EasyMock.expect(Checkpointer.loadAccountingSystem()).andReturn(accounting);

		PowerMock.replayAll(BinomialDistribution.class, dist, config, provider, user, accounting, machineDescriptor, configurable);
		
		MachineStatistics statistics = new MachineStatistics(0.5, 100, 100, 0);
		OptimalProvisioningSystemForHeterogeneousMachines op = new OptimalProvisioningSystemForHeterogeneousMachines();
		op.configurable = configurable;
		op.sendStatistics(0, statistics, 0);
		
		PowerMock.verifyAll();
	}
}

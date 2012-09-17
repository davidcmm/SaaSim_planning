package planning.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import util.MockedConfigurationTest;

import commons.cloud.MachineType;


public class OptimumPlanningTest extends MockedConfigurationTest{
	
	private static final double ERROR = 0.000001;

	@Before
	public void setUp() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field field = OptimumPlanning.class.getDeclaredField("totalDemand");
		field.setAccessible(true);
		field.set(null, new Integer(0));
		
		field = OptimumPlanning.class.getDeclaredField("maximumConsumed");
		field.setAccessible(true);
		field.set(null, new Integer(0));
		
		field = OptimumPlanning.class.getDeclaredField("minimumConsumed");
		field.setAccessible(true);
		field.set(null, new Integer(0));
	}
	
	@Test
	public void testInitValues(){
		OptimumPlanning.initValues();
		assertEquals(0, OptimumPlanning.totalDemand, ERROR);
		assertEquals(3, OptimumPlanning.onDemandFees.size());
		assertEquals(3, OptimumPlanning.reservationFees.size());
		assertEquals(3, OptimumPlanning.reservedFees.size());
		
		assertEquals(4136.363636, OptimumPlanning.minimumConsumption.get(MachineType.M1_SMALL), ERROR);
		assertEquals(4136.363636, OptimumPlanning.minimumConsumption.get(MachineType.M1_LARGE), ERROR);
		assertEquals(4136.363636, OptimumPlanning.minimumConsumption.get(MachineType.M1_XLARGE), ERROR);
	}
	
	@Test
	public void testReadMachineUsageAndCalcTotalDemand() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		String file = "src/test/resources/optimum/machineUsage_1.dat";
		
		Map<Long, Double> machineUsage = OptimumPlanning.readMachineUsage(file);
		assertNotNull(machineUsage);
		assertEquals(2, machineUsage.size());
		assertEquals(5d, machineUsage.get(5l), ERROR);
		assertEquals(5d, machineUsage.get(10l), ERROR);
		
		Field field = OptimumPlanning.class.getDeclaredField("totalDemand");
		field.setAccessible(true);
		assertEquals(5 * 5d + 10 * 5d, (Double)field.get(null), ERROR);
	}
	
	@Test
	public void testReadMachineUsageAndCalcTotalDemand2() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		String file = "src/test/resources/optimum/machineUsage_2.dat";
		
		Map<Long, Double> machineUsage = OptimumPlanning.readMachineUsage(file);
		assertNotNull(machineUsage);
		assertEquals(6, machineUsage.size());
		assertEquals(1d, machineUsage.get(40l), ERROR);
		assertEquals(1d, machineUsage.get(30l), ERROR);
		assertEquals(3d, machineUsage.get(20l), ERROR);
		
		Field field = OptimumPlanning.class.getDeclaredField("totalDemand");
		field.setAccessible(true);
		assertEquals(5 * 5d + 10 * 5d + 6 * 5d + 20 * 3d + 30d + 40d, (Double)field.get(null), ERROR);
	}
	
	@Test(expected=RuntimeException.class)
	public void testReadMachineUsageWithInexistentFile(){
		String file = "src/test/resources/optimum/machineUsage_.dat";
		
		Map<Long, Double> machineUsage = OptimumPlanning.readMachineUsage(file);
	}
	
	@Test(expected=RuntimeException.class)
	public void testReadMachineUsageWithEmptyFileName(){
		String file = "";
		
		Map<Long, Double> machineUsage = OptimumPlanning.readMachineUsage(file);
	}
	
	@Test
	public void testReadReceipt() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		String file = "src/test/resources/optimum/receipt_1.dat";
		
		assertEquals(78000, OptimumPlanning.readReceipt(file), ERROR);
	}
	
	@Test(expected=RuntimeException.class)
	public void testReadReceiptWithInexistentFile() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		String file = "src/test/resources/optimum/receipt_.dat";
		
		assertEquals(78000, OptimumPlanning.readReceipt(file), ERROR);
	}
	
	@Test(expected=RuntimeException.class)
	public void testReadReceiptWithEmptyFileName() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		String file = "";
		assertEquals(78000, OptimumPlanning.readReceipt(file), ERROR);
	}
	
	/**
	 * This method checks that a high machine utilization with a low demand 
	 * makes that only one machine is able to be reserved
	 */
	@Test
	public void testFindReservationLimit(){
		double demand = 10000;
		Map<MachineType, Double> minimumConsumption = new HashMap<MachineType, Double>();
		minimumConsumption.put(MachineType.M1_SMALL, 20000d);
		minimumConsumption.put(MachineType.M1_LARGE, 20000d);
		minimumConsumption.put(MachineType.M1_XLARGE, 20000d);
		
		String file = "src/test/resources/optimum/machineUsage_2.dat";
		OptimumPlanning.readMachineUsage(file);
		
		Map<MachineType, Long> reservationLimit = OptimumPlanning.findReservationLimit(demand, minimumConsumption);
		assertNotNull(reservationLimit);
		assertEquals(3, reservationLimit.size());
		assertEquals(40, reservationLimit.get(MachineType.M1_SMALL), ERROR);
		assertEquals(40, reservationLimit.get(MachineType.M1_LARGE), ERROR);
		assertEquals(40, reservationLimit.get(MachineType.M1_XLARGE), ERROR);
	}
	
	/**
	 * This method checks that machine utilization compatible with demand 
	 * makes that multiple machines are able to be reserved
	 */
	@Test
	public void testFindReservationLimit2(){
		double demand = 10000;
		Map<MachineType, Double> minimumConsumption = new HashMap<MachineType, Double>();
		minimumConsumption.put(MachineType.M1_SMALL, 100d);
		minimumConsumption.put(MachineType.M1_LARGE, 200d);
		minimumConsumption.put(MachineType.M1_XLARGE, 250d);
		
		String file = "src/test/resources/optimum/machineUsage_1.dat";
		OptimumPlanning.readMachineUsage(file);
		
		Map<MachineType, Long> reservationLimit = OptimumPlanning.findReservationLimit(demand, minimumConsumption);
		assertNotNull(reservationLimit);
		assertEquals(3, reservationLimit.size());
//		assertEquals(100, reservationLimit.get(MachineType.M1_SMALL), ERROR);
//		assertEquals(50, reservationLimit.get(MachineType.M1_LARGE), ERROR);
//		assertEquals(40, reservationLimit.get(MachineType.M1_XLARGE), ERROR);
		assertEquals(10, reservationLimit.get(MachineType.M1_SMALL), ERROR);
		assertEquals(10, reservationLimit.get(MachineType.M1_LARGE), ERROR);
		assertEquals(10, reservationLimit.get(MachineType.M1_XLARGE), ERROR);
	}
	
	/**
	 * This method verifies if one machine that is well used is evaluated as 
	 * viable to be reserved
	 */
	@Test
	public void testOptimumReservationPlan(){
		OptimumPlanning.initValues();
		
		double totalReceipt = 100000;
		Map<MachineType, Long> reservationLimit = new HashMap<MachineType, Long>();
		reservationLimit.put(MachineType.M1_SMALL, 10l);
		reservationLimit.put(MachineType.M1_LARGE, 5l);
		reservationLimit.put(MachineType.M1_XLARGE, 3l);
		
		Map<Long, Double> machinesUsage = new HashMap<Long, Double>();
		machinesUsage.put(1l, 4400d);
		
		Map<MachineType, Long> reservationPlan = OptimumPlanning.findOptimumReservationPlan(totalReceipt, reservationLimit, machinesUsage);
		assertNotNull(reservationPlan);
		assertEquals(1, reservationPlan.get(MachineType.M1_SMALL), ERROR);
		assertEquals(0, reservationPlan.get(MachineType.M1_LARGE), ERROR);
		assertEquals(0, reservationPlan.get(MachineType.M1_XLARGE), ERROR);
	}
	
	/**
	 * This method verifies if different amounts of machines are well used the
	 * highest amount of machines is reserved
	 */
	@Test
	public void testOptimumReservationPlan2(){
		OptimumPlanning.initValues();
		
		double totalReceipt = 100000;
		Map<MachineType, Long> reservationLimit = new HashMap<MachineType, Long>();
		reservationLimit.put(MachineType.M1_SMALL, 10l);
		reservationLimit.put(MachineType.M1_LARGE, 5l);
		reservationLimit.put(MachineType.M1_XLARGE, 3l);
		
		Map<Long, Double> machinesUsage = new HashMap<Long, Double>();
		machinesUsage.put(1l, 4400d);
		machinesUsage.put(2l, 4400d);
		
		Map<MachineType, Long> reservationPlan = OptimumPlanning.findOptimumReservationPlan(totalReceipt, reservationLimit, machinesUsage);
		assertNotNull(reservationPlan);
		assertEquals(2, reservationPlan.get(MachineType.M1_SMALL), ERROR);
		assertEquals(0, reservationPlan.get(MachineType.M1_LARGE), ERROR);
		assertEquals(0, reservationPlan.get(MachineType.M1_XLARGE), ERROR);
	}
	
	/**
	 * This method verifies if different amounts of machines have mixed usage
	 * the amount well used is reserved
	 */
	@Test
	public void testOptimumReservationPlan3(){
		OptimumPlanning.initValues();
		
		double totalReceipt = 100000;
		Map<MachineType, Long> reservationLimit = new HashMap<MachineType, Long>();
		reservationLimit.put(MachineType.M1_SMALL, 10l);
		reservationLimit.put(MachineType.M1_LARGE, 5l);
		reservationLimit.put(MachineType.M1_XLARGE, 3l);
		
		Map<Long, Double> machinesUsage = new HashMap<Long, Double>();
		machinesUsage.put(5l, 4400d);
		machinesUsage.put(9l, 1000d);
		
		Map<MachineType, Long> reservationPlan = OptimumPlanning.findOptimumReservationPlan(totalReceipt, reservationLimit, machinesUsage);
		assertNotNull(reservationPlan);
		assertEquals(1, reservationPlan.get(MachineType.M1_SMALL), ERROR);
		assertEquals(0, reservationPlan.get(MachineType.M1_LARGE), ERROR);
		assertEquals(1, reservationPlan.get(MachineType.M1_XLARGE), ERROR);
	}
	
	@Test
	public void testSaveData() throws IOException{
		OptimumPlanning.initValues();
		
		Map<MachineType, Long> reservationPlan = new HashMap<MachineType, Long>();
		reservationPlan.put(MachineType.M1_SMALL, 2l);
		reservationPlan.put(MachineType.M1_LARGE, 1l);
		reservationPlan.put(MachineType.M1_XLARGE, 0l);
		
		double totalReceipt = 10000;
		double cost = 4500;
		
		String usageFile = "workload_1.dat";
		
		OptimumPlanning.saveData(reservationPlan, totalReceipt, cost, usageFile);
		
		//Checking if file was saved
		File file = new File("data_1.dat");
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		assertTrue(reader.ready());
		
		String[] split = reader.readLine().split("\\s+");
		assertEquals((totalReceipt - cost), Double.valueOf(split[0]), ERROR);
		assertEquals(totalReceipt, Double.valueOf(split[1]), ERROR);
		assertEquals(cost, Double.valueOf(split[2]), ERROR);
		
		reader.close();
		
		file.deleteOnExit();
	}
}

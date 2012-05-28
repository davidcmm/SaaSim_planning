package commons.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class used to create a sequence of 1000 prime numbers that are used in Geist while
 * generating synthetic workload 
 *
 * @author David Candeia Medeiros Maia - davidcmm@lsd.ufcg.edu.br
 */
public class PrimeNumbersGenerator {
	
	private static final String OUT_FILE = "primes.txt";
	
	public static void main(String[] args) throws IOException {
		
		int counter = 0;
		int value = 1;
		
		FileWriter writer = new FileWriter(new File(OUT_FILE));
		
		while(counter < 1000){
			  boolean isPrime = checkIsPrime(value);
			  if(isPrime){
				  writer.write(value+"\n");
				  counter++;
			  }
			  value++;
		}	  
		
		writer.close();
	}

	private static boolean checkIsPrime(int value) {
		int j;
		for (j = 2; j < value; j++){
		  int n = value % j;
		  if (n == 0){
			  return false;
		  }
		}
		return true;
	}

}

package commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PenaltyAnalyser {
	
	public static void main(String[] args) {
		if(args.length != 1){
			System.err.println("Usage: <simulation output file>");
			System.exit(1);
		}
		
		for (int i = 1; i <= 72; i++){
			System.out.println("h"+i+"=t(data.matrix(count["+i+", ]))");
		}
		System.out.print("dados <- c(");
		for (int i = 1; i <= 72; i++){
			System.out.print("h"+i+", ");
		}
		System.out.println(")");

		System.out.print("ind <- c( ");
		for (int i = 1; i <= 72; i++){
			System.out.print("array(\"hour"+i+"\", c(1,36001)), ");
		}
		System.out.println(")");
		
		System.out.print("data <- data.frame(dados=dados, indice=ind)");
		System.exit(0);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
			
			reader.readLine();//profit line
			reader.readLine();//total penalty and reservation line
			reader.readLine();//description line
			
			while(reader.ready()){
				int index = 1;
				int finished = 7;
				int total = 8;
				
				String[] data = reader.readLine().split("\t");
				
				int currentFinished = 0;
				int currentTotal = 0;
				
				while(index < 50){
					currentFinished += Integer.valueOf(data[finished]);
					currentTotal += Integer.valueOf(data[total]);
					
					index++;
					finished = total + 10;
					total = finished + 1;
				}
				
				System.out.println(currentFinished+"\t"+currentTotal);
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

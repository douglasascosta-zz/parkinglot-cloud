package br.com.unicamp.parkinglot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class ParkingLot {

	public static void main(String[] args) {
		
		int max = 10;
		if (args.length > 0) {
			try {
				max = Integer.parseInt(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Random rdm = new Random();
		int vagas = rdm.nextInt(max);
		
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("vagas.txt");
			writer.println(vagas);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println(vagas);
	}

}

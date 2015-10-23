package br.com.unicamp.parkinglot;

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
		int places = rdm.nextInt(max);
		System.out.println(places);
	}

}

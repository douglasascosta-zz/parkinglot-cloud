package it.polito.appeal.traci.examples;

import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;

import java.util.Collection;

public class Manhattan {

	/** main method */
	public static void main(String[] args) {
		SumoTraciConnection conn = new SumoTraciConnection(
				"test/sumo_maps/manhattan/600_49.sumo.cfg",  // config file
				12345                                  // random seed
				);
		try {
			conn.runServer();
			
			// the first two steps of this simulation have no vehicles.
			conn.nextSimStep();
			conn.nextSimStep();
			
			Collection<Vehicle> vehicles = conn.getVehicleRepository().getAll().values();

			Vehicle aVehicle = vehicles.iterator().next();
			
			System.out.println("Vehicle " + aVehicle
					+ " will traverse these edges: "
					+ aVehicle.getCurrentRoute());
			
			conn.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

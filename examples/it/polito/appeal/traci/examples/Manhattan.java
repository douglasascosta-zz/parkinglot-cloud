package it.polito.appeal.traci.examples;

import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.appeal.traci.POI;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;
import it.polito.appeal.traci.VehicleLifecycleObserver;

public class Manhattan {

	/**
	 * main method
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		// define caminho do sumo instalado
		System.setProperty(SumoTraciConnection.SUMO_EXE_PROPERTY, "/home/douglas/sumo-0.24.0/bin/sumo");

		final SumoTraciConnection conn = new SumoTraciConnection("test/sumo_maps/manhattan/200_0.sumo.cfg", 12345);

		final String vagasFile = "vagas.txt";

		final Double CAR = Math.random() * 200;

		final int STEPS = 1111;

		final double DIST = 110;

		Map<String, Integer> available = new HashMap<>();

		Boolean found = false;

		try {
			conn.addOption("additional-files", "test/sumo_maps/manhattan/mini/additional.add.xml");
			conn.addOption("gui-settings-file", "test/sumo_maps/manhattan/mini/gui-settings.cfg");
			conn.addOption("S", "true");
			conn.addOption("Q", "true");

			conn.runServer(true);

			System.out.println("Following car " + CAR.intValue());

			conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

				@Override
				public void vehicleTeleportStarting(Vehicle vehicle) {
				}

				@Override
				public void vehicleTeleportEnding(Vehicle vehicle) {
				}

				@Override
				public void vehicleDeparted(Vehicle vehicle) {
					try {
						if (Integer.valueOf(vehicle.getID()).equals(CAR.intValue())) {
							vehicle.changeColor(Color.YELLOW);
						} else {
							vehicle.changeSpeed(BigDecimal.ZERO.doubleValue());
							vehicle.changeColor(Color.BLACK);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void vehicleArrived(Vehicle vehicle) {
				}
			});

			for (int i = 0; i < STEPS; i++) {

				conn.nextSimStep();

				Vehicle vehicle = conn.getVehicleRepository().getByID(CAR.intValue() + "");
				if (vehicle == null) {
					if (!found) {
						System.out.println("Could not find vehicle");
						continue;
					}
					break;
				}

				found = true;

				for (POI poi : conn.getPOIRepository().getAll().values()) {

					double currDist = vehicle.getPosition().distance(poi.getPosition());

					Runtime.getRuntime().exec("python vagas.py 10").waitFor();

					char[] vagasChar = new char[1];
					FileReader reader = new FileReader(vagasFile);
					reader.read(vagasChar);
					reader.close();

					Integer vagasInt = Integer.valueOf(String.valueOf(vagasChar[0]));

					if (currDist <= DIST && vagasInt > 0) {
						available.put(poi.getID(), vagasInt);
						poi.changeColor(Color.GREEN);
					}
					if (currDist > DIST && vagasInt > 0) {
						available.remove(poi.getID());
						poi.changeColor(Color.RED);
					}
				}

				System.out.print("Parking lots available now: ");
				for (Entry<String, Integer> poi : available.entrySet()) {
					System.out.print(poi.getKey() + "(" + poi.getValue() + " vagas) ");
				}
				System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!conn.isClosed())
				conn.close();
		}

	}
}

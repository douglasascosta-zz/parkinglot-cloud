package it.polito.appeal.traci.examples;

import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.unicamp.parkinglot.ParkingLot;
import it.polito.appeal.traci.Edge;
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

		final String placesFile = "vagas.txt";

		final Integer vehicleId = new Double(Math.random() * 200).intValue();

		final double DIST = 110;

		List<ParkingLot> available;

		Edge target = null;

		Boolean avaliated = false;

		ParkingLot nearest = null;

		try {
			conn.addOption("additional-files", "test/sumo_maps/manhattan/mini/additional.add.xml");
			conn.addOption("gui-settings-file", "test/sumo_maps/manhattan/mini/gui-settings.cfg");
			conn.addOption("S", "true");
//			conn.addOption("Q", "true");

			conn.runServer(true);

			System.out.println("Following vehicle " + vehicleId);

			conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

				@Override
				public void vehicleTeleportStarting(Vehicle vehicle) {
				}

				@Override
				public void vehicleTeleportEnding(Vehicle veqhicle) {
				}

				@Override
				public void vehicleDeparted(Vehicle vehicle) {
					try {
						if (Integer.valueOf(vehicle.getID()).equals(vehicleId)) {
							vehicle.changeColor(Color.PINK);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void vehicleArrived(Vehicle vehicle) {
				}
			});

			while (conn.getVehicleRepository().getByID(vehicleId.toString()) == null) {
				System.out.println("Waiting for vehicle to appear...");
				conn.nextSimStep();
			}

			System.out.println("Vehicle found!");

			Vehicle vehicle;

			while ((vehicle = conn.getVehicleRepository().getByID(vehicleId.toString())) != null) {

				int step = conn.getCurrentSimTime() / 1000;

				System.out.println("Step " + step);

				List<Edge> route = vehicle.getCurrentRoute();

				target = route.get(route.size() - 1);

				if (step % 40 == 0) {

					System.out.println("Old target " + target.toString());

					Object[] edges = conn.getEdgeRepository().getAll().values().toArray();

					do {
						int index = (int) (Math.random() * edges.length);

						target = (Edge) edges[index];
					} while (target.toString().contains("_"));

					vehicle.changeTarget(target);
					System.out.println(
							"New target " + vehicle.getCurrentRoute().get(vehicle.getCurrentRoute().size() - 1));

					avaliated = false;
				}

				if (!avaliated) {

					String endPoint = target.toString().split("to")[1];

					double x = Integer.valueOf(endPoint.split("/")[0]) * 100;
					double y = Integer.valueOf(endPoint.split("/")[1]) * 100;

					System.out.println("Destination: " + endPoint);

					avaliated = true;

					available = new ArrayList<ParkingLot>();

					for (POI poi : conn.getPOIRepository().getAll().values()) {

						Double currDist = poi.getPosition().distance(x, y);

						Runtime.getRuntime().exec("python vagas.py 10").waitFor();

						char[] placesChar = new char[1];
						FileReader reader = new FileReader(placesFile);
						reader.read(placesChar);
						reader.close();

						Integer placesInt = Integer.valueOf(String.valueOf(placesChar[0]));

						ParkingLot currPl = new ParkingLot(poi.getID(), placesInt, currDist);

						if (nearest == null || currDist < nearest.getDist()) {
							nearest = currPl;
						}

						if (currDist <= DIST && placesInt > 0) {
							available.add(currPl);
							poi.changeColor(Color.GREEN);
						}
						if (currDist > DIST && placesInt > 0) {
							poi.changeColor(Color.RED);
						}
					}

					if (available.isEmpty()) {
						POI nearestPoi = conn.getPOIRepository().getByID(nearest.getId());
						System.out.println("No near parking lots available. The nearest one is " + nearestPoi.getID()
								+ " (" + nearest.getPlaces() + " places).");

						nearestPoi.changeColor(Color.GREEN);
					} else {
						System.out.print("Parking lots available near " + endPoint + " : ");
						for (ParkingLot parkingLot : available) {
							System.out.print(parkingLot.getId() + "(" + parkingLot.getPlaces() + " vagas) ");
						}
						System.out.println();
					}
				}
				conn.nextSimStep();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!conn.isClosed())
				conn.close();
		}

	}
}

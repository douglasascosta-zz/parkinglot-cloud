package it.polito.appeal.traci.examples;

import java.io.IOException;

import it.polito.appeal.traci.StepAdvanceListener;
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

		final SumoTraciConnection conn = new SumoTraciConnection("test/sumo_maps/manhattan/1_0.sumo.cfg", 12345);

		final int STEPS = 111;

		try {
			conn.runServer(true);
			
			conn.addOption("--additional-files", "test/sumo_maps/manhattan/pois.xml");

			conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

				@Override
				public void vehicleTeleportStarting(Vehicle vehicle) {
					System.out.println("teleport");
				}

				@Override
				public void vehicleTeleportEnding(Vehicle vehicle) {
					System.out.println("Teleport end");
				}

				@Override
				public void vehicleDeparted(Vehicle vehicle) {
					try {
						System.out.println("Vehicle " + vehicle.getID() + " started from "
								+ vehicle.getPosition().getX() + "," + vehicle.getPosition().getY());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void vehicleArrived(Vehicle vehicle) {
					System.out.println("Vehicle " + vehicle.getID() + " arrived");
				}
			});

			conn.addStepAdvanceListener(new StepAdvanceListener() {

				@Override
				public void nextStep(double step) {

				}
			});

			for (int i = 0; i < STEPS; i++) {
				conn.nextSimStep();

				Vehicle vehicle = conn.getVehicleRepository().getByID("0");
				System.out.println("Step " + conn.getCurrentSimTime() / 1000 + ", vehicle " + vehicle.getID() + ": "
						+ vehicle.getPosition().getX() + "," + vehicle.getPosition().getY());
			}

			conn.nextSimStep();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!conn.isClosed())
				conn.close();
		}
	}
}

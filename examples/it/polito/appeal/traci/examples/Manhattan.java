package it.polito.appeal.traci.examples;

import it.polito.appeal.traci.StepAdvanceListener;
import it.polito.appeal.traci.SumoTraciConnection;

public class Manhattan {

	/** main method */
	public static void main(String[] args) {
		
		// define caminho do sumo instalado
		System.setProperty(SumoTraciConnection.SUMO_EXE_PROPERTY, "/home/douglas/sumo-0.24.0/bin/sumo");
		
		SumoTraciConnection conn = new SumoTraciConnection("test/sumo_maps/manhattan/600_49.sumo.cfg", // config
																										// file
				12345 // random seed
		);
		try {
			conn.runServer();

			conn.addStepAdvanceListener(new StepAdvanceListener() {

				@Override
				public void nextStep(double step) {
					System.out.println(step);
//					System.out.println(conn.getVehicleRepository().getAll().values().size());
				}
			});

			conn.addOption("--begin", "10");
			conn.addOption("--end", "10000");
			conn.addOption("--step-length", "1000");
			
			try {
				while (true) {
					conn.nextSimStep();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

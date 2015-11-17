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

		/**
		 * Define caminho do sumo instalado
		 */
		System.setProperty(SumoTraciConnection.SUMO_EXE_PROPERTY, "/home/douglas/sumo-0.24.0/bin/sumo");

		/**
		 * Arquivo de configuração da simulação
		 */
		final SumoTraciConnection conn = new SumoTraciConnection("test/sumo_maps/manhattan/200_0.sumo.cfg", 12345);

		/**
		 * Arquivo que simula comunicação com servidor do estacionamento
		 */
		final String placesFile = "vagas.txt";

		/**
		 * Seleciona veículo aleatório da simulação
		 */
		final Integer vehicleId = new Double(Math.random() * 200).intValue();

		/**
		 * Raio de busca de estacionamentos
		 */
		final double DIST = 110;

		List<ParkingLot> available;

		Edge target = null;

		Boolean avaliated = false;

		ParkingLot nearest = null;

		try {
			// adiciona opções a serem executadas na linha de comando
			
			// arquivo definindo os pontos de interesse que representam os estacionamentos
			// este arquivo é gerado pelo script generate_parkinglots.py
			// exemplo de uso: "python generate_parkinglots.py 10"
			conn.addOption("additional-files", "test/sumo_maps/manhattan/mini/additional.add.xml");
			
			// arquivo de configuração da sumo-gui, parte visual do sumo
			conn.addOption("gui-settings-file", "test/sumo_maps/manhattan/mini/gui-settings.cfg");
			
			// opção que habilita começar simulação automaticamente ao abrir sumo-gui
			conn.addOption("S", "true");
			
			// opção que habilitar fechar sumo-gui automaticamente ao terminar a simulação
			// conn.addOption("Q", "true");

			// inicia a simulação com sumo
			// o parâmetro diz se é para abrir sumo-gui ou não
			conn.runServer(true);

			System.out.println("Following vehicle " + vehicleId);

			// implementa eventos do ciclo de vida dos veículos
			conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

				@Override
				public void vehicleTeleportStarting(Vehicle vehicle) {
				}

				@Override
				public void vehicleTeleportEnding(Vehicle veqhicle) {
				}

				/**
				 * Evento chamado quando o veículo entra na simulação e começa a rota
				 */
				@Override
				public void vehicleDeparted(Vehicle vehicle) {
					try {
						if (!Integer.valueOf(vehicle.getID()).equals(vehicleId)) {
							vehicle.changeColor(Color.PINK);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				/**
				 * Evento chamado quando o veículo termina a rota e é removido da simulação
				 */
				@Override
				public void vehicleArrived(Vehicle vehicle) {
				}
			});

			// veículo não necessariamente é inserido na simulação no primeiro passo
			// aguarda veículo ser encontrado
			while (conn.getVehicleRepository().getByID(vehicleId.toString()) == null) {
				System.out.println("Waiting for vehicle to appear...");
				conn.nextSimStep();
			}

			System.out.println("Vehicle found!");

			Vehicle vehicle;

			while ((vehicle = conn.getVehicleRepository().getByID(vehicleId.toString())) != null) {

				// converte de ms pra s
				int step = conn.getCurrentSimTime() / 1000;

				System.out.println("Step " + step);

				// pega o destino do veículo
				List<Edge> route = vehicle.getCurrentRoute();
				target = route.get(route.size() - 1);

				// simula uma troca de destino a cada 40 passoss
				if (step % 40 == 0) {

					System.out.println("Old target " + target.toString());

					Object[] edges = conn.getEdgeRepository().getAll().values().toArray();

					// escolhe um destino aleatório 
					do {
						int index = (int) (Math.random() * edges.length);

						target = (Edge) edges[index];
						
						//ignora os que contém '_' por ser mais difícil de parsear
					} while (target.toString().contains("_"));

					// muda o destino do veículo
					vehicle.changeTarget(target);
					
					System.out.println(
							"New target " + vehicle.getCurrentRoute().get(vehicle.getCurrentRoute().size() - 1));

					// levanta flag para recalcular estacionamentos mais próximos
					avaliated = false;
				}

				// avalia flag de rota alterada
				if (!avaliated) {

					// parseia destino para pegar as coordenadas x, y
					String endPoint = target.toString().split("to")[1];

					double x = Integer.valueOf(endPoint.split("/")[0]) * 100;
					double y = Integer.valueOf(endPoint.split("/")[1]) * 100;

					System.out.println("Destination: " + endPoint);

					avaliated = true;

					available = new ArrayList<ParkingLot>();

					// itera em cada estacionamento ao alcance
					for (POI poi : conn.getPOIRepository().getAll().values()) {

						// avalia distânca do estacionamento para o destino
						Double currDist = poi.getPosition().distance(x, y);

						// simula comunicação com servidor do estacionamento
						Runtime.getRuntime().exec("python vagas.py 10").waitFor();

						// le número de vagas no arquivo (0 a 9)
						char[] placesChar = new char[1];
						FileReader reader = new FileReader(placesFile);
						reader.read(placesChar);
						reader.close();

						Integer placesInt = Integer.valueOf(String.valueOf(placesChar[0]));

						ParkingLot currPl = new ParkingLot(poi.getID(), placesInt, currDist);

						// verifica se o estacionamento é o mais próximo do destino
						if (nearest == null || currDist < nearest.getDist()) {
							nearest = currPl;
						}

						// muda a cor do POI pra verde e adiciona na lista de estacionamentos possíveis
						// se tiver dentro do raio de alcance
						// ou vermelho se não
						if (currDist <= DIST && placesInt > 0) {
							available.add(currPl);
							poi.changeColor(Color.GREEN);
						}
						if (currDist > DIST && placesInt > 0) {
							poi.changeColor(Color.RED);
						}
					}

					// avisa se não houver estacionamentos com vagas disponíveis dentro do raio de alcance
					// e informa o estacionamento mais próximo fora dele
					if (available.isEmpty()) {
						POI nearestPoi = conn.getPOIRepository().getByID(nearest.getId());
						System.out.println("No near parking lots available. The nearest one is " + nearestPoi.getID()
								+ " (" + nearest.getPlaces() + " places).");

						nearestPoi.changeColor(Color.GREEN);
					} else {
						// informa os estacionamentos disponíveis e quantas vagas tem em cada
						System.out.print("Parking lots available near " + endPoint + " : ");
						for (ParkingLot parkingLot : available) {
							System.out.print(parkingLot.getId() + "(" + parkingLot.getPlaces() + " vagas) ");
						}
						System.out.println();
					}
				}
				
				// avança pro próximo passo na simulação
				conn.nextSimStep();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// fecha a conexão com o sumo
			if (!conn.isClosed())
				conn.close();
		}

	}
}

package br.com.unicamp.parkinglot;

public class ParkingLot {

	public ParkingLot(String id, Integer places, Double dist) {
		this.id = id;
		this.places = places;
		this.dist = dist;
	}
	
	private String id;
	private Integer places;
	private Double dist;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getPlaces() {
		return places;
	}
	public void setPlaces(Integer places) {
		this.places = places;
	}
	public Double getDist() {
		return dist;
	}
	public void setDist(Double dist) {
		this.dist = dist;
	}
	
	
}

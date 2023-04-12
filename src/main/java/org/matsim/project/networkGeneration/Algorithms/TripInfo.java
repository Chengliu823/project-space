package org.matsim.project.networkGeneration.Algorithms;

public class TripInfo extends LinkInfo{
    private String tripId;
    private double fromX;
    private double fromY;
    private double toX;
    private double toY;

    public TripInfo() {

    }

    public TripInfo(String tripId, double fromX, double fromY, double toX, double toY,double networkTravelTime, double validationTravelTime, double networkDistance, double validationDistance) {
        super(networkTravelTime, networkDistance, validationTravelTime, validationDistance);
        this.tripId = tripId;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public double getFromX() {
        return fromX;
    }

    public void setFromX(double fromX) {
        this.fromX = fromX;
    }

    public double getFromY() {
        return fromY;
    }

    public void setFromY(double fromY) {
        this.fromY = fromY;
    }

    public double getToX() {
        return toX;
    }

    public void setToX(double toX) {
        this.toX = toX;
    }

    public double getToY() {
        return toY;
    }

    public void setToY(double toY) {
        this.toY = toY;
    }
}

package org.matsim.project.networkGeneration.algorithms;

public class ScoreInfo {
    private double TravelTimeScore;
    private double DistanceScore;
    private double TravelTimeDeviation;
    private double DistanceDeviation;

    public ScoreInfo() {
    }

    public ScoreInfo(double travelTimeScore, double distanceScore, double travelTimeDeviation, double distanceDeviation) {
        TravelTimeScore = travelTimeScore;
        DistanceScore = distanceScore;
        TravelTimeDeviation = travelTimeDeviation;
        DistanceDeviation = distanceDeviation;
    }

    public double getTravelTimeScore() {
        return TravelTimeScore;
    }

    public void setTravelTimeScore(double travelTimeScore) {
        TravelTimeScore = travelTimeScore;
    }

    public double getDistanceScore() {
        return DistanceScore;
    }

    public void setDistanceScore(double distanceScore) {
        DistanceScore = distanceScore;
    }

    public double getTravelTimeDeviation() {
        return TravelTimeDeviation;
    }

    public void setTravelTimeDeviation(double travelTimeDeviation) {
        TravelTimeDeviation = travelTimeDeviation;
    }

    public double getDistanceDeviation() {
        return DistanceDeviation;
    }

    public void setDistanceDeviation(double distanceDeviation) {
        DistanceDeviation = distanceDeviation;
    }
}

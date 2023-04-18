package org.matsim.project.networkGeneration.algorithms;

//routeInfo
public class RouteInfo {
    private double networkTravelTime;
    private double NetworkDistance;
    private double validationTravelTime;
    private double validationDistance;


    public RouteInfo() {
    }

    public RouteInfo(double networkTravelTime, double networkDistance, double validationTravelTime, double validationDistance) {
        this.networkTravelTime = networkTravelTime;
        this.NetworkDistance = networkDistance;
        this.validationTravelTime = validationTravelTime;
        this.validationDistance = validationDistance;
    }

    public double getNetworkTravelTime() {
        return networkTravelTime;
    }

    public void setNetworkTravelTime(double networkTravelTime) {
        this.networkTravelTime = networkTravelTime;
    }

    public double getNetworkDistance() {
        return NetworkDistance;
    }

    public void setNetworkDistance(double networkDistance) {
        NetworkDistance = networkDistance;
    }

    public double getValidationTravelTime() {
        return validationTravelTime;
    }

    public void setValidationTravelTime(double validationTravelTime) {
        this.validationTravelTime = validationTravelTime;
    }

    public double getValidationDistance() {
        return validationDistance;
    }

    public void setValidationDistance(double validationDistance) {
        this.validationDistance = validationDistance;
    }
}

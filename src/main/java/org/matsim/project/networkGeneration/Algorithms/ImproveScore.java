package org.matsim.project.networkGeneration.Algorithms;

public class ImproveScore {
    private double freeSpeed;
    private double improveScore;

    public ImproveScore() {
    }

    public ImproveScore(double freeSpeed, double improveScore) {
        this.freeSpeed = freeSpeed;
        this.improveScore = improveScore;
    }

    public double getFreeSpeed() {
        return freeSpeed;
    }

    public void setFreeSpeed(double freeSpeed) {
        this.freeSpeed = freeSpeed;
    }

    public double getImproveScore() {
        return improveScore;
    }

    public void setImproveScore(double improveScore) {
        this.improveScore = improveScore;
    }
}

package org.matsim.project.networkGeneration;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class AlgorithmsLink {
    private Id<Link> algorithmsId;
    private double algorithmsFreeSpeed;

    public AlgorithmsLink() {
    }

    public AlgorithmsLink(Id<Link> algorithmsId, double algorithmsFreeSpeed) {
        this.algorithmsId = algorithmsId;
        this.algorithmsFreeSpeed = algorithmsFreeSpeed;
    }

    public Id<Link> getAlgorithmsId() {
        return algorithmsId;
    }

    public void setAlgorithmsId(Id<Link> algorithmsId) {
        this.algorithmsId = algorithmsId;
    }

    public double getAlgorithmsFreeSpeed() {
        return algorithmsFreeSpeed;
    }

    public void setAlgorithmsFreeSpeed(double algorithmsFreeSpeed) {
        this.algorithmsFreeSpeed = algorithmsFreeSpeed;
    }
}

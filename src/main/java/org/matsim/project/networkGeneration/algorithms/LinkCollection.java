package org.matsim.project.networkGeneration.algorithms;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.Objects;

public class LinkCollection {
    private Id<Link> algorithmsId;
    private double algorithmsFreeSpeed;

    public LinkCollection() {
    }

    public LinkCollection(Id<Link> algorithmsId, double algorithmsFreeSpeed) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkCollection that = (LinkCollection) o;
        return Double.compare(that.algorithmsFreeSpeed, algorithmsFreeSpeed) == 0 && Objects.equals(algorithmsId, that.algorithmsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithmsId, algorithmsFreeSpeed);
    }
}

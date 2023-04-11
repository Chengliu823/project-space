package org.matsim.project.networkGeneration;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class AlgorithmsStatisticInfo {
    private Id<Link> algorithmsId;
    private int Count;

    public AlgorithmsStatisticInfo() {
    }

    public AlgorithmsStatisticInfo(Id<Link> algorithmsId, int count) {
        this.algorithmsId = algorithmsId;
        Count = count;
    }

    public Id<Link> getAlgorithmsId() {
        return algorithmsId;
    }

    public void setAlgorithmsId(Id<Link> algorithmsId) {
        this.algorithmsId = algorithmsId;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        Count = count;
    }
}

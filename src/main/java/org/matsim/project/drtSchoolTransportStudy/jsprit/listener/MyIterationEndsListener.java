package org.matsim.project.drtSchoolTransportStudy.jsprit.listener;

import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import org.matsim.project.drtSchoolTransportStudy.jsprit.utils.StatisticCollectorForIterationEndsListener;

import java.util.Collection;

public class MyIterationEndsListener implements IterationEndsListener {

    @Override
    public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {

        // save costs and fleet size from current iteration
        double costs = Solutions.bestOf(solutions).getCost();
        int fleetSize = Solutions.bestOf(solutions).getRoutes().size();
        StatisticCollectorForIterationEndsListener.getCostsMap().put(i, costs);
        StatisticCollectorForIterationEndsListener.getFleetSizeMap().put(i, fleetSize);
        if (i == 1) {
            StatisticCollectorForIterationEndsListener.setBestCosts(costs);
            StatisticCollectorForIterationEndsListener.setBestFleetSize(fleetSize);
        }

        double bestCosts = StatisticCollectorForIterationEndsListener.getBestCosts();
        int bestFleetSize = StatisticCollectorForIterationEndsListener.getBestFleetSize();
        // save best costs and best fleet size
        if (costs < bestCosts) {
            bestCosts = costs;
            StatisticCollectorForIterationEndsListener.setBestCosts(bestCosts);
        }
        if (fleetSize < bestFleetSize) {
            bestFleetSize = fleetSize;
            StatisticCollectorForIterationEndsListener.setBestFleetSize(bestFleetSize);
        }
        StatisticCollectorForIterationEndsListener.getBestCostsMap().put(i, bestCosts);
        StatisticCollectorForIterationEndsListener.getBestFleetSizeMap().put(i, bestFleetSize);
    }
}

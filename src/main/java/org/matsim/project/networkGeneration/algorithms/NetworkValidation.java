package org.matsim.project.networkGeneration.algorithms;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.application.MATSimAppCommand;
import org.matsim.application.analysis.DefaultAnalysisMainModeIdentifier;
import org.matsim.contrib.analysis.vsp.traveltimedistance.GoogleMapRouteValidator;
import org.matsim.contrib.analysis.vsp.traveltimedistance.HereMapsRouteValidator;
import org.matsim.contrib.analysis.vsp.traveltimedistance.TravelTimeDistanceValidator;
import org.matsim.contrib.dvrp.trafficmonitoring.QSimFreeSpeedTravelTime;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.speedy.SpeedyALTFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import picocli.CommandLine;

import java.io.FileWriter;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkValidation implements MATSimAppCommand {
    @CommandLine.Option(names = "--api", description = "path to network file", defaultValue = "GOOGLE_MAP")
    private API api;

    @CommandLine.Option(names = "--api-key", description = "API key", required = true)
    private String apiKey;

    @CommandLine.Option(names = "--network", description = "path to network file", required = true)
    private String networkPath;

    @CommandLine.Option(names = "--plans", description = "plans to be validated", required = true)
    private String plansPath;

    @CommandLine.Option(names = "--output", description = "output folder for the route calculation", required = true)
    private String outputPath;

    @CommandLine.Option(names = "--date", description = "The date to validate travel times for, format: YYYY-MM-DD")
    private LocalDate date;

    @CommandLine.Option(names = "--max-validation", description = "output folder for the route calculation", defaultValue = "2000")
    private int maxValidations;

    @CommandLine.Option(names = "--improveOutput", description = "output network file", required = true)
    private Path improveOutput;
    private enum API {
        HERE, GOOGLE_MAP, NETWORK_FILE
    }

    private final String mode = "car";

    public static void main(String[] args) {
        new NetworkValidation().execute(args);
    }

    @Override
    public Integer call() throws Exception {

        List<RouteInfo> routeInfoList = new ArrayList<>();
        List<AlgorithmsLink> algorithmsLinkList = new ArrayList<>();
        List<AlgorithmsLink> improveLinkList;
        Map<Id<Link>, Double> idCollectionMap = new HashMap<>();
        Database database = new Database();
        List<TripInfo> tripInfoList = new ArrayList<>();
        List<TripInfo> databaseTripInfoList = database.infoList();

        double firstTravelTimeScore;
        double firstDistanceScore;
        double firstTravelTimeDeviation;
        double firstDistanceDeviation;
        int validationZeroCount=0;
        boolean databaseFlag=false;
        double validationTravelTime;
        double validationDistance;
        double fromX;
        double fromY;
        double toX;
        double toY;

        CoordinateTransformation ct = new GeotoolsTransformation("EPSG:25832", "EPSG:4326");
        TravelTimeDistanceValidator validator;
        MainModeIdentifier mainModeIdentifier = new DefaultAnalysisMainModeIdentifier();

        Population population = PopulationUtils.readPopulation(plansPath);
        Network network = NetworkUtils.readNetwork(networkPath);
        TravelTime travelTime = new QSimFreeSpeedTravelTime(1.0);
        LeastCostPathCalculator router = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

        CSVPrinter tsvWriter = new CSVPrinter(new FileWriter(outputPath + "/network-validation-" + api.toString() + ".tsv"), CSVFormat.TDF);
        tsvWriter.printRecord("trip_id", "from_x", "from_y", "to_x", "to_y", "network_travel_time", "validated_travel_time", "network_travel_distance", "validated_travel_distance");

        if (api == API.GOOGLE_MAP) {
            if (date == null) {
                date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
            }
            validator = new GoogleMapRouteValidator(outputPath, mode, apiKey, date.toString(), ct);

        } else if (api == API.HERE) {
            if (date == null) {
                date = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.WEDNESDAY));
            }
            validator = new HereMapsRouteValidator(outputPath, mode, apiKey, date.toString(), ct, false);

        } else {
            throw new RuntimeException("Wrong API used. Allowed values for --api are: GOOGLE_MAP, HERE. Do not use NETWORK_BASED validator in this analysis");
        }


        int validated = 0;
        for (Person person : population.getPersons().values()) {
            int personTripCounter = 0;
            List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan());
            for (TripStructureUtils.Trip trip : trips) {
                if (!mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.car)) {
                    continue;
                }
                String tripId = person.getId().toString() + "_" + personTripCounter;
                Coord from = trip.getOriginActivity().getCoord();
                if (from == null) {
                    from = network.getLinks().get(trip.getOriginActivity().getLinkId()).getToNode().getCoord();
                }
                Link fromLink = NetworkUtils.getNearestLink(network, from);

                Coord to = trip.getDestinationActivity().getCoord();
                if (to == null) {
                    to = network.getLinks().get(trip.getOriginActivity().getLinkId()).getToNode().getCoord();
                }
                Link toLink = NetworkUtils.getNearestLink(network, to);

                double departureTime = trip.getOriginActivity().getEndTime().orElseThrow(RuntimeException::new);

                //这里插入判断，判断database中有没有这个信息

                Tuple<Double, Double> validatedResult = null;
                int databaseTripInfoListIndex = 0;
                for (int i = 0; i < databaseTripInfoList.size(); i++) {
                    if (tripId.equals(databaseTripInfoList.get(i).getTripId())){
                        databaseFlag = true;
                        databaseTripInfoListIndex =i;
                    }
                }
                if (databaseFlag){
                    validationTravelTime =databaseTripInfoList.get(databaseTripInfoListIndex).getValidationTravelTime();
                    validationDistance =databaseTripInfoList.get(databaseTripInfoListIndex).getValidationDistance();
                    fromX =databaseTripInfoList.get(databaseTripInfoListIndex).getFromX();
                    fromY =databaseTripInfoList.get(databaseTripInfoListIndex).getFromY();
                    toX =databaseTripInfoList.get(databaseTripInfoListIndex).getToX();
                    toY =databaseTripInfoList.get(databaseTripInfoListIndex).getToY();
                }else {
                    validatedResult = validator.getTravelTime(from, to, departureTime, tripId);
                    validationTravelTime =validatedResult.getFirst();
                    validationDistance =validatedResult.getSecond();
                    fromX =from.getX();
                    fromY =from.getY();
                    toX =to.getX();
                    toY =to.getY();
                }

                LeastCostPathCalculator.Path route = router.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);
                double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();

                tsvWriter.printRecord(tripId, fromX, fromY, toX, toY, route.travelTime, validationTravelTime, networkTravelDistance, validationDistance);

                //tripInfo instances
                TripInfo tripInfo = new TripInfo(tripId,fromX, fromY, toX,toY,route.travelTime,validationTravelTime,networkTravelDistance,validationDistance);
                if (databaseFlag){
                    continue;
                }else {
                    database.Insert(tripInfo);
                }

                tripInfoList.add(tripInfo);
                databaseFlag = false;

                if (route.travelTime != 0 && validationTravelTime != 0 && networkTravelDistance != 0 && validationDistance != 0){
                    RouteInfo routeInfo = new RouteInfo(route.travelTime,networkTravelDistance,validationTravelTime, validationDistance);
                    routeInfoList.add(routeInfo);

                    for (Link link: route.links){
                        AlgorithmsLink algorithmsLink = new AlgorithmsLink(link.getId(),link.getFreespeed());
                        algorithmsLinkList.add(algorithmsLink);
                        idCollectionMap.put(link.getId(),link.getFreespeed());
                    }
                }else {
                    validationZeroCount++;
                }


                validated++;
                personTripCounter++;
                Thread.sleep(100);

            }
            if (validated >= maxValidations) {
                break;
            }
        }

        int invalidValidation = maxValidations -validationZeroCount;
        System.out.println("valid validation times is: "+invalidValidation);

        firstTravelTimeScore= AlgorithmsUtils.travelTimeScoreCalculation(routeInfoList);
        firstDistanceScore = AlgorithmsUtils.distanceScoreCalculation(routeInfoList);
        firstTravelTimeDeviation = AlgorithmsUtils.travelTimeDeviation(routeInfoList);
        firstDistanceDeviation = AlgorithmsUtils.distanceDeviation(routeInfoList);

        //System.out.println("first travel Time Score:"+firstTravelTimeScore+ "first distance Score: "+firstDistanceScore+"firstTravelTime Deviation:"+firstTravelTimeDeviation +"firstDistanceDeviation:"+firstDistanceDeviation);

        improveLinkList = AlgorithmsUtils.linkChoice(algorithmsLinkList,idCollectionMap);

        List<ScoreInfo> bestImproveScoreList = new ArrayList<>();
        double maximalImproveTimes =improveLinkList.size()-1;

        for (int i = 0; i < improveLinkList.size(); i++) {
            Id<Link> improveLinkId =improveLinkList.get(i).getAlgorithmsId();
            Link improveLink = network.getLinks().get(improveLinkId);
            double linkFreeSpeed = improveLinkList.get(i).getAlgorithmsFreeSpeed();
            System.out.println("link id:"+improveLinkId+" "+ "free Speed:" +linkFreeSpeed);

            List<ImproveScore> improveScoreList = new ArrayList<>();

            for (int freeSpeedRate=50; freeSpeedRate<=150; freeSpeedRate+=5){

                ScoreInfo bestImproveScoreInfo = new ScoreInfo();

                double improvedFreeSpeed = linkFreeSpeed*freeSpeedRate*0.01;
                improveLink.setFreespeed(improvedFreeSpeed);

                LeastCostPathCalculator improvedRouter = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

                int validatedCount =0;
                routeInfoList.clear();

                for (Person person : population.getPersons().values()) {
                    List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan());
                    for (TripStructureUtils.Trip trip : trips) {
                        if (!mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.car)) {
                            continue;
                        }
                        Coord from = trip.getOriginActivity().getCoord();
                        if (from == null) {
                            from = network.getLinks().get(trip.getOriginActivity().getLinkId()).getToNode().getCoord();
                        }
                        Link fromLink = NetworkUtils.getNearestLink(network, from);

                        Coord to = trip.getDestinationActivity().getCoord();
                        if (to == null) {
                            to = network.getLinks().get(trip.getOriginActivity().getLinkId()).getToNode().getCoord();
                        }
                        Link toLink = NetworkUtils.getNearestLink(network, to);

                        double departureTime = trip.getOriginActivity().getEndTime().orElseThrow(RuntimeException::new);

                        LeastCostPathCalculator.Path route = improvedRouter.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);//计算路径的方法
                        double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();

                        double improveValidationTravelTime = tripInfoList.get(validatedCount).getValidationTravelTime();
                        double improveValidationDistance = tripInfoList.get(validatedCount).getValidationDistance();

                        if (i == maximalImproveTimes){
                            tsvWriter.printRecord(tripInfoList.get(validatedCount).getTripId(),tripInfoList.get(validatedCount).getFromX(), tripInfoList.get(validatedCount).getFromY(),tripInfoList.get(validatedCount).getToX(),tripInfoList.get(validatedCount).getToY(), route.travelTime, tripInfoList.get(validatedCount).getValidationTravelTime(), networkTravelDistance,tripInfoList.get(validatedCount).getValidationDistance());
                        }

                        if (route.travelTime != 0 && improveValidationTravelTime != 0 && networkTravelDistance != 0 && improveValidationDistance != 0){
                            RouteInfo improveRouteInfo = new RouteInfo(route.travelTime,networkTravelDistance,improveValidationTravelTime,improveValidationDistance);
                            routeInfoList.add(improveRouteInfo);

                        }else {
                            validationZeroCount++;
                        }
                        validatedCount++;
                    }

                    if (validatedCount >= tripInfoList.size()) {
                        break;
                    }
                }

                //可以增加输出运行完毕后的最高得分的函数
                double secondTravelTimeScore = AlgorithmsUtils.travelTimeScoreCalculation(routeInfoList);
                double secondDistanceScore = AlgorithmsUtils.distanceScoreCalculation(routeInfoList);
                double secondTravelTimeDeviation = AlgorithmsUtils.travelTimeDeviation(routeInfoList);
                double secondDistanceDeviation = AlgorithmsUtils.distanceDeviation(routeInfoList);
                //System.out.println("second Travel Time Score:"+secondTravelTimeScore+"second Distance Score:"+secondDistanceScore + "secondTravelTime Deviation:"+secondTravelTimeDeviation +"second DistanceDeviation:"+secondDistanceDeviation);


                //路允许发生改变
                if (firstDistanceDeviation * 0.8 <= secondDistanceDeviation && secondDistanceDeviation<= firstDistanceDeviation *1.2 && secondTravelTimeScore<=firstTravelTimeScore){
                    if (secondTravelTimeDeviation >= firstTravelTimeDeviation*0.8 && secondTravelTimeDeviation <= firstTravelTimeDeviation*1.2 && secondDistanceScore>= firstDistanceScore*0.8 && secondDistanceScore<= firstDistanceScore*1.2){
                        ImproveScore improveScore = new ImproveScore(improvedFreeSpeed,secondTravelTimeScore);
                        improveScoreList.add(improveScore);
                    }
                }

                if (i == maximalImproveTimes){
                    bestImproveScoreInfo.setTravelTimeScore(secondTravelTimeScore);
                    bestImproveScoreInfo.setDistanceScore(secondDistanceScore);
                    bestImproveScoreInfo.setTravelTimeDeviation(secondTravelTimeDeviation);
                    bestImproveScoreInfo.setDistanceDeviation(secondDistanceDeviation);
                    bestImproveScoreList.add(bestImproveScoreInfo);
                }


            }

            double bestFreeSpeed = AlgorithmsUtils.listSort(improveScoreList);
            improveLink.setFreespeed(bestFreeSpeed);

            System.out.println("improve times ="+i+ "max improve times ="+maximalImproveTimes);
        }

        int bestImproveGroup = AlgorithmsUtils.scoreSort(bestImproveScoreList)+1;
        int bestImproveIndex =bestImproveGroup-1;
        double bestImproveTravelTimeScore = bestImproveScoreList.get(bestImproveIndex).getTravelTimeScore();
        double bestImproveTravelTimeDeviation = bestImproveScoreList.get(bestImproveIndex).getTravelTimeDeviation();
        double bestImproveDistanceScore = bestImproveScoreList.get(bestImproveIndex).getDistanceScore();
        double bestImproveDistanceDeviation = bestImproveScoreList.get(bestImproveIndex).getDistanceDeviation();

        System.out.println();
        System.out.println("Group "+bestImproveGroup+" is the best Group. " +"best improved Travel time score is:"+bestImproveTravelTimeScore +"best Improve TravelTime Deviation:"+bestImproveTravelTimeDeviation +"best Improve Distance Score:"+bestImproveDistanceScore +"best Improve Distance Deviation:"+bestImproveDistanceDeviation);
        System.out.println("first travel Time Score:"+firstTravelTimeScore+ "first distance Score: "+firstDistanceScore+"firstTravelTime Deviation:"+firstTravelTimeDeviation +"firstDistanceDeviation:"+firstDistanceDeviation);

        tsvWriter.close();
        new NetworkWriter(network).write(improveOutput.toString());
        return 0;
    }

}
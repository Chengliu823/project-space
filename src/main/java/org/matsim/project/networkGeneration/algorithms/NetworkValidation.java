
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
import java.io.IOException;
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

    @CommandLine.Option(names = "--typeOfOperation", description = "type of Operation", required = true)
    private OperationTyp typeOfOperation;

    private enum API {
        HERE, GOOGLE_MAP, NETWORK_FILE
    }

    private  enum OperationTyp{
        VALIDATION,IMPROVEMENT
    }


    private final String mode = "car";


    //Definition Value
    List<RouteInfo> routeInfoList = new ArrayList<>();
    List<LinkCollection> linkCollectionList = new ArrayList<>();
    List<LinkCollection> improveLinkList = new ArrayList<>();
    Map<Id<Link>,Double> normalLinkMap = new HashMap<>();
    Map<Id<Link>,Double> largeGapTripLinkMap = new HashMap<>();
    List<LinkCollection> issuesLinkList = new ArrayList<>();

    List<Double>travelTimeScoreList=new ArrayList<>();
    List<TripInfo> tripInfoList = new ArrayList<>();

    Map<Id<Link>, Double> idCollectionMap = new HashMap<>();

    int validated=0;
    int validationZeroCount=0;
    double firstTravelTimeScore;
    double firstDistanceScore;
    double firstTravelTimeDispersion;
    double firstDistanceDispersion;
    double travelTimeScore;
    boolean tsvWriterFlag = false;
    boolean databaseFlag=false;
    boolean issuesFlag =false;
    int iteratorCount =0;

    public static void main(String[] args) {
        new NetworkValidation().execute(args);
    }

    @Override
    public Integer call() throws Exception {
        Database database = new Database();
        List<TripInfo> databaseTripInfoList = database.infoList(api.toString());

        CoordinateTransformation ct = new GeotoolsTransformation("EPSG:25832","EPSG:4326");
        TravelTimeDistanceValidator validator;
        MainModeIdentifier mainModeIdentifier = new DefaultAnalysisMainModeIdentifier();

        Population population = PopulationUtils.readPopulation(plansPath);
        Network network = NetworkUtils.readNetwork(networkPath);
        TravelTime travelTime = new QSimFreeSpeedTravelTime(1.0);
        LeastCostPathCalculator router = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

        CSVPrinter tsvWriter = new CSVPrinter(new FileWriter(outputPath + "/network-validation-" + api.toString() + ".tsv"), CSVFormat.TDF);
        validator = getApiInformation(ct);

        operation(database, databaseTripInfoList, validator, mainModeIdentifier, population, network, travelTime, router, tsvWriter);

        tsvWriter.close();
        new NetworkWriter(network).write(improveOutput.toString());

        return 0;
    }

    private void operation(Database database, List<TripInfo> databaseTripInfoList, TravelTimeDistanceValidator validator, MainModeIdentifier mainModeIdentifier, Population population, Network network, TravelTime travelTime, LeastCostPathCalculator router, CSVPrinter tsvWriter) throws IOException, InterruptedException {
        if (typeOfOperation == OperationTyp.VALIDATION){
            firstValidation(validator, mainModeIdentifier, population, network, router, tsvWriter, databaseTripInfoList, database);
            firstTravelTimeScore= AlgorithmsUtils.travelTimeScoreCalculation(routeInfoList);
            firstDistanceScore = AlgorithmsUtils.distanceScoreCalculation(routeInfoList);
            firstTravelTimeDispersion = AlgorithmsUtils.travelTimeDeviation(routeInfoList);
            firstDistanceDispersion = AlgorithmsUtils.distanceDeviation(routeInfoList);

            System.out.println("first travel Time Score:"+firstTravelTimeScore+ "first distance Score: "+firstDistanceScore+"firstTravelTime Deviation:"+ firstTravelTimeDispersion +"firstDistanceDeviation:"+ firstDistanceDispersion);
        } else if (typeOfOperation == OperationTyp.IMPROVEMENT){
            iterativeAlgorithms(database, databaseTripInfoList, validator, mainModeIdentifier, population, network, travelTime, tsvWriter);
        } else {
            System.out.println("error, Please enter VALIDATION or IMPROVEMENT");
        }
    }


    //Algorithms
    private void iterativeAlgorithms(Database database, List<TripInfo> databaseTripInfoList, TravelTimeDistanceValidator validator, MainModeIdentifier mainModeIdentifier, Population population, Network network, TravelTime travelTime, CSVPrinter tsvWriter) throws IOException, InterruptedException {

        while (true){
            LeastCostPathCalculator router = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

            firstValidation(validator, mainModeIdentifier, population, network, router, tsvWriter, databaseTripInfoList, database);

            firstTravelTimeScore = AlgorithmsUtils.travelTimeScoreCalculation(routeInfoList);
            firstDistanceScore = AlgorithmsUtils.distanceScoreCalculation(routeInfoList);
            firstTravelTimeDispersion = AlgorithmsUtils.travelTimeDeviation(routeInfoList);
            firstDistanceDispersion = AlgorithmsUtils.distanceDeviation(routeInfoList);
            System.out.println("first travel Time Score:" + firstTravelTimeScore + "first distance Score: " + firstDistanceScore + "firstTravelTime Deviation:" + firstTravelTimeDispersion + "firstDistanceDeviation:" + firstDistanceDispersion);

            travelTimeScoreList.add(firstTravelTimeScore);

            if (iteratorCount>=3 && iteratorCount %2 ==0 && travelTimeScoreList.get(iteratorCount)*1.05>=travelTimeScoreList.get(iteratorCount-2)){
                break;
            } else if (travelTimeScoreList.get(iteratorCount) ==0){
                break;
            }

            identifyImproveLink();

            improveValidation(network, population, mainModeIdentifier, tsvWriter, firstTravelTimeScore, firstDistanceScore, firstTravelTimeDispersion, firstDistanceDispersion, travelTime);

            iteratorCount++;
            iteratorClear();
        }
    }

    private void iteratorClear() {
        routeInfoList.clear();
        issuesLinkList.clear();
        linkCollectionList.clear();
        tripInfoList.clear();
        improveLinkList.clear();

        idCollectionMap.clear();
        largeGapTripLinkMap.clear();
        normalLinkMap.clear();

        validated =0;
        validationZeroCount =0;

        databaseFlag=false;
        tsvWriterFlag =false;
    }

    private void identifyImproveLink() {
        improveLinkList = AlgorithmsUtils.getImproveLinkList(linkCollectionList,idCollectionMap);

        //improveLinkList.addAll(AlgorithmsUtils.mapToList(largeGapTripLinkMap));                                 //Original

        if (issuesFlag){
            issuesLinkList =AlgorithmsUtils.importantLinkStatistic(linkCollectionList,normalLinkMap,largeGapTripLinkMap); //Option 1
            improveLinkList.addAll(issuesLinkList);
            issuesFlag =false;
        }else {
            removeImportantLink();                                                                                          //Option 2
            issuesFlag = true;
        }
    }

    private void removeImportantLink() {
        for (int i = 0; i < improveLinkList.size(); i++) {
            for (LinkCollection linkCollection : issuesLinkList) {
                if (improveLinkList.get(i).getAlgorithmsId() == linkCollection.getAlgorithmsId()) {
                    improveLinkList.remove(i);
                    break;
                }
            }
        }
    }

    private void linkCollection(LeastCostPathCalculator.Path route, double networkTravelTime, double validationTravelTime) {
        for (Link link: route.links){
            LinkCollection linkCollection = new LinkCollection(link.getId(),link.getFreespeed());
            if (Math.abs((networkTravelTime/validationTravelTime)-1)<=0.5 && Math.abs((networkTravelTime/validationTravelTime)-1)>=0.05){
                linkCollectionList.add(linkCollection);
                idCollectionMap.put(link.getId(),link.getFreespeed());
            }else if (Math.abs((networkTravelTime/validationTravelTime)-1)>0.5){
                largeGapTripLinkMap.put(link.getId(),link.getFreespeed());
            }else if (Math.abs((networkTravelTime/validationTravelTime)-1)<0.05){
                normalLinkMap.put(link.getId(),link.getFreespeed());
            }
        }
    }


    private TravelTimeDistanceValidator getApiInformation(CoordinateTransformation ct) {
        TravelTimeDistanceValidator validator;
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
        return validator;
    }

    private void firstValidation (TravelTimeDistanceValidator validator, MainModeIdentifier mainModeIdentifier,Population population, Network network, LeastCostPathCalculator router, CSVPrinter tsvWriter,List<TripInfo> databaseTripInfoList,Database database) throws IOException, InterruptedException {
        double validationTravelTime;
        double validationDistance;
        double fromX;
        double fromY;
        double toX;
        double toY;

        tsvWriter.printRecord("trip_id", "from_x", "from_y", "to_x", "to_y", "network_travel_time", "validated_travel_time", "network_travel_distance", "validated_travel_distance");

        for (Person person : population.getPersons().values()) {
            int personTripCounter = 0;
            List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan());
            for (TripStructureUtils.Trip trip : trips) {
                if (!mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.car)
                    && !mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.drt)) {
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
                    to = network.getLinks().get(trip.getDestinationActivity().getLinkId()).getToNode().getCoord();
                }
                Link toLink = NetworkUtils.getNearestLink(network, to);

                double departureTime = trip.getOriginActivity().getEndTime().orElseThrow(RuntimeException::new);


                Tuple<Double, Double> validatedResult;

                int databaseTripInfoListIndex = 0;
                databaseFlag=false;

                if (databaseTripInfoList.size() !=0){
                    for (int i = 0; i < databaseTripInfoList.size(); i++) {
                        if (from.getX() == databaseTripInfoList.get(i).getFromX() && from.getY() == databaseTripInfoList.get(i).getFromY() && to.getX() == databaseTripInfoList.get(i).getToX() && to.getY() ==databaseTripInfoList.get(i).getToY()){
                            databaseFlag = true;
                            databaseTripInfoListIndex =i;
                            break;
                        }
                    }
                }


                //Determine whether the element exists in the database
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
                double networkTravelTime =route.travelTime;

                tsvWriter.printRecord(tripId, fromX, fromY, toX, toY, networkTravelTime, validationTravelTime, networkTravelDistance, validationDistance);

                TripInfo tripInfo = new TripInfo(tripId,fromX, fromY, toX,toY,networkTravelTime,validationTravelTime,networkTravelDistance,validationDistance);

                if (databaseFlag){

                }else {
                    database.Insert(tripInfo,api.toString());
                }

                tripInfoList.add(tripInfo);

                if (networkTravelTime != 0 && validationTravelTime != 0 && networkTravelDistance != 0 && validationDistance != 0){
                    RouteInfo routeInfo = new RouteInfo(networkTravelTime,networkTravelDistance,validationTravelTime, validationDistance);
                    routeInfoList.add(routeInfo);
                    //linkCollection(route);
                    linkCollection(route,networkTravelTime,validationTravelTime);
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
    }




    private void improveValidation(Network network, Population population, MainModeIdentifier mainModeIdentifier, CSVPrinter tsvWriter, double firstTravelTimeScore, double firstDistanceScore, double firstTravelTimeDeviation, double firstDistanceDeviation, TravelTime travelTime) throws IOException {

        double maximalImproveTimes =improveLinkList.size()-1;

        double bestFreeSpeed;
        int minFreeSpeedRate=20;
        int maxFreeSpeedRate=180;
        int freeSpeedLevel=5;

        for (int i = 0; i < improveLinkList.size(); i++) {
            Id<Link> improveLinkId =improveLinkList.get(i).getAlgorithmsId();
            Link improveLink = network.getLinks().get(improveLinkId);
            double linkFreeSpeed = improveLinkList.get(i).getAlgorithmsFreeSpeed();
            //System.out.println("link id:"+improveLinkId+" "+ "free Speed:" +linkFreeSpeed);

            List<ImproveScore> improveScoreList = new ArrayList<>();


            bestFreeSpeed =  getBestFreeSpeed(network, population, mainModeIdentifier,tsvWriter, firstTravelTimeScore, firstDistanceScore, firstTravelTimeDeviation, firstDistanceDeviation, travelTime, improveLink, linkFreeSpeed, improveScoreList, minFreeSpeedRate, maxFreeSpeedRate ,freeSpeedLevel);
            improveLink.setFreespeed(bestFreeSpeed);


            bestFreeSpeed = getBestFreeSpeed(network, population, mainModeIdentifier,tsvWriter, firstTravelTimeScore, firstDistanceScore, firstTravelTimeDeviation, firstDistanceDeviation, travelTime, improveLink, linkFreeSpeed, improveScoreList, 90, 110 ,1);
            improveLink.setFreespeed(bestFreeSpeed);

            //Determine whether to output data
            if (i==improveLinkList.size()-1){
                tsvWriterFlag = true;
            }

            bestFreeSpeed = getBestFreeSpeed(network, population, mainModeIdentifier,tsvWriter, firstTravelTimeScore, firstDistanceScore, firstTravelTimeDeviation, firstDistanceDeviation, travelTime, improveLink, linkFreeSpeed, improveScoreList, 100, 101 ,1);
            improveLink.setFreespeed(bestFreeSpeed);

            System.out.println("improve times ="+i+ " max improve times ="+maximalImproveTimes);
        }

        System.out.println("first travel Time Score:"+firstTravelTimeScore+ "first distance Score: "+firstDistanceScore+"firstTravelTime Deviation:"+firstTravelTimeDeviation +"firstDistanceDeviation:"+firstDistanceDeviation);

    }


    //calculate the best free speed
    public double getBestFreeSpeed(Network network, Population population, MainModeIdentifier mainModeIdentifier, CSVPrinter tsvWriter, double firstTravelTimeScore, double firstDistanceScore, double firstTravelTimeDeviation, double firstDistanceDeviation, TravelTime travelTime, Link improveLink, double linkFreeSpeed, List<ImproveScore> improveScoreList, int minFreeSpeedRate, int maxFreeSpeedRate, int freeSpeedLevel) throws IOException {
        double bestFreeSpeed;

        freeSpeedReplace(network, population, mainModeIdentifier,tsvWriter, firstTravelTimeScore, firstDistanceScore, firstTravelTimeDeviation, firstDistanceDeviation, travelTime, improveLink, linkFreeSpeed, improveScoreList, minFreeSpeedRate, maxFreeSpeedRate ,freeSpeedLevel);

        if (improveScoreList.size() != 0){
            bestFreeSpeed = AlgorithmsUtils.getBestFreeSpeed(improveScoreList);
        }else {
            bestFreeSpeed =linkFreeSpeed;
        }

        improveLink.setFreespeed(bestFreeSpeed);

        return bestFreeSpeed;
    }

    //Replace the link that needs to be replaced
    private void freeSpeedReplace(Network network, Population population, MainModeIdentifier mainModeIdentifier, CSVPrinter tsvWriter, double firstTravelTimeScore, double firstDistanceScore, double firstTravelTimeDeviation, double firstDistanceDeviation, TravelTime travelTime, Link improveLink, double linkFreeSpeed, List<ImproveScore> improveScoreList, int minFreeSpeedRate, int maxFreeSpeedRate, int freeSpeedLevel) throws IOException {

        for (int freeSpeedRate=minFreeSpeedRate; freeSpeedRate<=maxFreeSpeedRate; freeSpeedRate+=freeSpeedLevel){
            //tsvWriter.printRecord("trip_id", "from_x", "from_y", "to_x", "to_y", "network_travel_time", "validated_travel_time", "network_travel_distance", "validated_travel_distance");

            double improvedFreeSpeed = linkFreeSpeed *freeSpeedRate*0.01;
            improveLink.setFreespeed(improvedFreeSpeed);

            LeastCostPathCalculator improvedRouter = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

            int validatedCount =0;
            routeInfoList.clear();

            for (Person person : population.getPersons().values()) {
                int personTripCounter = 0;
                List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan());
                for (TripStructureUtils.Trip trip : trips) {
                    if (!mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.car) && !mainModeIdentifier.identifyMainMode(trip.getTripElements()).equals(TransportMode.drt)) {
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
                        to = network.getLinks().get(trip.getDestinationActivity().getLinkId()).getToNode().getCoord();
                    }
                    Link toLink = NetworkUtils.getNearestLink(network, to);

                    double departureTime = trip.getOriginActivity().getEndTime().orElseThrow(RuntimeException::new);

                    LeastCostPathCalculator.Path route = improvedRouter.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);//计算路径的方法
                    double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();
                    double networkTravelTime=route.travelTime;

                    double improveValidationTravelTime = tripInfoList.get(validatedCount).getValidationTravelTime();
                    double improveValidationDistance = tripInfoList.get(validatedCount).getValidationDistance();

                    if (tsvWriterFlag){
                        tsvWriter.printRecord(tripId,from.getX(),from.getY(),to.getX(),to.getY(), networkTravelTime, improveValidationTravelTime, networkTravelDistance, improveValidationDistance);
                    }

                    if (networkTravelTime != 0 && improveValidationTravelTime != 0 && networkTravelDistance != 0 && improveValidationDistance != 0){
                        RouteInfo improveRouteInfo = new RouteInfo(networkTravelTime,networkTravelDistance,improveValidationTravelTime,improveValidationDistance);
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


            double secondTravelTimeScore = AlgorithmsUtils.travelTimeScoreCalculation(routeInfoList);
            double secondDistanceScore = AlgorithmsUtils.distanceScoreCalculation(routeInfoList);
            double secondTravelTimeDispersion = AlgorithmsUtils.travelTimeDeviation(routeInfoList);
            double secondDistanceDispersion = AlgorithmsUtils.distanceDeviation(routeInfoList);

            if (tsvWriterFlag){
                System.out.println("second Travel Time Score:"+secondTravelTimeScore+"second Distance Score:"+secondDistanceScore + "secondTravelTime Deviation:"+secondTravelTimeDispersion +"second DistanceDeviation:"+secondDistanceDispersion);
            }

            //Filter eligible links
            if ( firstTravelTimeDeviation>=secondTravelTimeDispersion && secondTravelTimeScore <= firstTravelTimeScore && secondDistanceScore<=firstDistanceScore && secondDistanceDispersion <=firstDistanceDeviation){
                    ImproveScore improveScore = new ImproveScore(improvedFreeSpeed, secondTravelTimeScore);
                    improveScoreList.add(improveScore);
            }
        }
    }


}
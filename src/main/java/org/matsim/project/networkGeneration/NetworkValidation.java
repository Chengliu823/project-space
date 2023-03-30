package org.matsim.project.networkGeneration;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
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
import java.security.Key;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;



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

    enum API {
        HERE, GOOGLE_MAP, NETWORK_FILE
    }

    private final String mode = "car";

    public static void main(String[] args) {
        new NetworkValidation().execute(args);
    }

    public int validated = 0;

    public int validatedZero = 0;//样本中有多少个数据事无效的，需要时可导出

    //定义条件
    public double networkTravelTime = 0;
    public double validatedTravelTime = 0;
    public double networkDistance = 0;
    public double validatedDistance = 0;
    public double travelTimeDifference =0;
    public double distanceDifference =0;
    public double scoreTravelTime=0;
    public double scoreDistance =0;
    //平均数

    public double advScore =0.0;

    //double adv_networkTravelTimeSum =0.0;

    //定义arraylist
    public List<Double> networkTravelTimeList = new ArrayList<>();
    public List<Double> validatedTravelTimeList = new ArrayList<>();
    public List<Double> networkDistanceList = new ArrayList<>();
    public List<Double> validatedDistanceList = new ArrayList<>();
    public List<Double> travelTimeDifferenceList = new ArrayList<>();
    public List<Double> distanceDifferenceList = new ArrayList<>();
    public List<Double> scoreTravelTimeList = new ArrayList<>();
    public List<Double> scoreDistanceList = new ArrayList<>();

    public List<Double> allValidatedTravelTimeList = new ArrayList<>();
    public List<Double> allValidatedDistanceList = new ArrayList<>();

    public List<Id<Link>> idCollectionList = new ArrayList<>();


    //比例
    public double networkTravelTimeDifference;
    public double networkDistanceDifference;
    public double networkTravelTimeDifferenceSum = 0;
    public double networkDistanceDifferenceSum = 0;
    public double networkTravelTimeDeviation;

    public double originalNetworkTravelTimeDeviation;
    public double networkDistanceDeviation;

    public double originalNetworkDistanceDeviation;
    public double finalScore;

    public double originalFinalScore;

    //定义比例的的arraylist
    public List<Double> networkTravelTimeDifferenceList = new ArrayList<>();
    public List<Double> networkDistanceDifferenceList = new ArrayList<>();

    public Map<Id<Link>, Double> idCollectionMap = new HashMap<>();
    public Map<Id<Link>, Double> idImproveMap = new HashMap<>();
    public Map<String, Double> allValidatedTravelTimeMap = new HashMap<>();
    public Map<String, Double> allValidatedDistanceMap = new HashMap<>();

    //Id Collection
    public List<Id<Link>> idRecordList = new ArrayList<>();

    public Map<Id<Link>, Integer> idAnalyseMap = new HashMap<>();

    public List<Id<Link>> idStatisticsList = new ArrayList<>();
    public List<Integer> idStatisticsTimeList = new ArrayList<>();

    public List<Double> freeSpeedCollectionList = new ArrayList<>();

    public List<Id<Link>> IdCollectionList =new ArrayList<>();

    //替换link的算法部分
    public double improvedTravelTimeDifference =0.0;
    public double improvedDistanceDifference =0.0;
    public double improvedScoreTravelTime =0.0;
    public double improvedScoreDistance =0.0;
    public double improvedNetworkTravelTime = 0.0;
    public double improvedValidatedTravelTime = 0.0;
    public double improvedNetworkDistance =0.0;
    public double improvedValidatedDistance =0.0;
    public double improvedNetworkDistanceDeviation=0.0;
    public double improvedFinalScore = 0.0;
    public double improvedFreeSpeed =0.0;
    public double improvedNetworkTravelTimeDeviation =0.0;
    public double bestImprovedFreeSpeed = 0.0;
    public double linkFreeSpeed;

    public int improvedValidatedTimes =0;
    public int improvedZeroCounter=0;
    public int improvedCounter=0;


    public List<Double> improvedNetworkTravelTimeList = new ArrayList<>();
    public List<Double> improvedNetworkDistanceList = new ArrayList<>();
    public List<Double> improvedValidatedTravelTimeList = new ArrayList<>();
    public List<Double> improvedValidatedDistanceList = new ArrayList<>();
    public List<Double> improvedTravelTimeDifferenceList = new ArrayList<>();
    public List<Double> improvedDistanceDifferenceList = new ArrayList<>();
    public List<Double> improvedScoreTravelTimeList = new ArrayList<>();
    public List<Double> improvedScoreDistanceList = new ArrayList<>();
    public List<Double> improvedFreeSpeedList = new ArrayList<>();
    public List<Double> improvedFinalScoreList = new ArrayList<>();



    public Map<Double,Double> scoreRecordMap = new HashMap<>();

    @Override
    public Integer call() throws Exception {
        CoordinateTransformation ct = new GeotoolsTransformation("EPSG:25832", "EPSG:4326");
        TravelTimeDistanceValidator validator;
        MainModeIdentifier mainModeIdentifier = new DefaultAnalysisMainModeIdentifier();
        Database database = new Database();
        database.deleteTripInfoTable();
        database.createTripInfoTable();


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


        Population population = PopulationUtils.readPopulation(plansPath);
        Network network = NetworkUtils.readNetwork(networkPath);
        TravelTime travelTime = new QSimFreeSpeedTravelTime(1.0);
        LeastCostPathCalculator router = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);


        CSVPrinter tsvWriter = new CSVPrinter(new FileWriter(outputPath + "/network-validation-" + api.toString() + ".tsv"), CSVFormat.TDF);
        tsvWriter.printRecord("trip_id", "from_x", "from_y", "to_x", "to_y", "network_travel_time", "validated_travel_time", "network_travel_distance", "validated_travel_distance");

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

                Tuple<Double, Double> validatedResult = validator.getTravelTime(from, to, departureTime, tripId);
                LeastCostPathCalculator.Path route = router.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);//计算路径的方法
                double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();


                tsvWriter.printRecord(tripId, from.getX(), from.getY(), to.getX(), to.getY(), route.travelTime, validatedResult.getFirst(), networkTravelDistance, validatedResult.getSecond());

                networkTravelTime = route.travelTime;
                validatedTravelTime = validatedResult.getFirst();
                networkDistance = networkTravelDistance;
                validatedDistance = validatedResult.getSecond();

                // insert validate Travel time and distance in the database or Hashmap
                database.Insert(tripId,validatedTravelTime,validatedDistance);
                allValidatedTravelTimeMap.put(tripId, validatedTravelTime);
                allValidatedDistanceMap.put(tripId, validatedDistance);

                AbsolutDifference();


                if (networkTravelTime != 0 && validatedTravelTime != 0 && networkDistance != 0 && validatedDistance != 0) {
                    addElement(networkTravelTimeList, validatedTravelTimeList, networkDistanceList, validatedDistanceList, travelTimeDifferenceList, distanceDifferenceList, scoreTravelTimeList, scoreDistanceList, networkTravelTimeDifferenceList, networkDistanceDifferenceList);
                    //idList.clear();
                            for (Link link : route.links){
                                idCollectionList.add(link.getId());
                                idCollectionMap.put(link.getId(),link.getFreespeed());
                            }

                } else {
                    validatedZero++;
                    System.out.println("validated Zero ="+validatedZero);
                }
                personTripCounter++;
            }

            validated++;

            Thread.sleep(200);

            if (validated >= maxValidations) {
                break;
            }

        }
            System.out.println("all validate Travel Time Map"+allValidatedTravelTimeMap);

            ScoreCalculation();

            originalNetworkDistanceDeviation=networkDistanceDeviation;
            originalNetworkTravelTimeDeviation=networkDistanceDeviation;
            originalFinalScore=finalScore;

            System.out.println("originalNetworkTravelTimeDeviation: "+originalNetworkTravelTimeDeviation  +"\n originalNetworkDistanceDeviation: "+originalNetworkDistanceDeviation+ "\noriginalFinalScore"+originalFinalScore);

            IdStatistic();

            clearElement(networkTravelTimeList, validatedTravelTimeList, networkDistanceList, validatedDistanceList, travelTimeDifferenceList, distanceDifferenceList,scoreTravelTimeList,scoreDistanceList);


        for (int improveCounter=0 ; improveCounter<idCollectionList.size(); improveCounter++){
            Id<Link> newIdLink = Id.createLinkId(idCollectionList.get(improveCounter));
            Link newLink = network.getLinks().get(newIdLink);
            linkFreeSpeed = freeSpeedCollectionList.get(improveCounter);
            System.out.println("link id:"+idCollectionList.get(improveCounter)+" "+ "free Speed:" +linkFreeSpeed);

            for (int freeSpeedRate=90; freeSpeedRate<=110; freeSpeedRate+=10){
                double realFreeSpeed =freeSpeedRate*0.01;
                improvedFreeSpeed =linkFreeSpeed * realFreeSpeed;
                newLink.setFreespeed(improvedFreeSpeed);

                //此处计算新的分数, direct use validatedTravelTimeList und validatedDistanceList. Here can create a new networkTravelTimeList and networkDistanceList for new freeSpeed.
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

                        LeastCostPathCalculator improvedRouter = new SpeedyALTFactory().createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
                        double departureTime = trip.getOriginActivity().getEndTime().orElseThrow(RuntimeException::new);

                        LeastCostPathCalculator.Path route = improvedRouter.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);//计算路径的方法
                        double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();

                        tsvWriter.printRecord(tripId, from.getX(), from.getY(), to.getX(), to.getY(), route.travelTime, allValidatedTravelTimeMap.get(tripId), networkTravelDistance, allValidatedDistanceMap.get(tripId));

                        System.out.println(tripId);

                        networkTravelTime=route.travelTime;
                        validatedTravelTime =allValidatedTravelTimeMap.get(tripId);
                        networkDistance=networkTravelDistance;
                        validatedDistance=allValidatedDistanceMap.get(tripId);

                        AbsolutDifference();

                        if (networkTravelTime != 0 && validatedTravelTime != 0 && networkDistance != 0 && validatedDistance != 0) {
                            addElement(networkTravelTimeList, validatedTravelTimeList, networkDistanceList, validatedDistanceList, travelTimeDifferenceList, distanceDifferenceList, scoreTravelTimeList, scoreDistanceList, networkTravelTimeDifferenceList, networkDistanceDifferenceList);
                        } else {
                            improvedZeroCounter++;
                        }

                        ScoreCalculation();

                        System.out.println("originalNetworkTravelTimeDeviation: "+networkTravelTimeDeviation  +"\n originalNetworkDistanceDeviation: "+networkDistanceDeviation+ "\noriginalFinalScore"+finalScore);

                        //scoreRecordMap.put(improvedFreeSpeed,improvedFinalScore);

                        //这里开始挑选数据
                        if (originalNetworkDistanceDeviation ==networkDistanceDeviation ){
                            if (finalScore <= originalFinalScore && networkTravelTimeDeviation > originalNetworkTravelTimeDeviation * 0.8 && networkTravelTimeDeviation <originalNetworkTravelTimeDeviation * 1.2){
                                scoreRecordMap.put(improvedFreeSpeed,improvedFinalScore); //此处排序可以用别的方法实现，标记
                            }



                        }

                        clearElement(networkTravelTimeList, validatedTravelTimeList, networkDistanceList, validatedDistanceList, travelTimeDifferenceList, distanceDifferenceList,scoreTravelTimeList,scoreDistanceList);


                        personTripCounter++;
                    }
                    improvedValidatedTimes++;

                    if (improvedValidatedTimes>maxValidations){
                        break;
                    }

                }
/*
ScoreCalculation();

                System.out.println("originalNetworkTravelTimeDeviation: "+networkTravelTimeDeviation  +"\n originalNetworkDistanceDeviation: "+networkDistanceDeviation+ "\noriginalFinalScore"+finalScore);


                //这里开始挑选数据
                    if (originalNetworkDistanceDeviation == networkDistanceDeviation){
                        if (finalScore <= originalFinalScore && networkTravelTimeDeviation > originalNetworkTravelTimeDeviation * 0.8 && networkTravelTimeDeviation <originalNetworkTravelTimeDeviation * 1.2){
                            scoreRecordMap.put(improvedFreeSpeed,improvedFinalScore); //此处排序可以用别的方法实现，标记
                        }

                    }
 */

                }

                System.out.println("Score Record Map:"+scoreRecordMap);
                //找出最优速度，然后添加进network，然后开始下一次循环

                for (Map.Entry<Double, Double> val: scoreRecordMap.entrySet()) {
                    improvedFreeSpeedList.add(val.getKey());
                    improvedFinalScoreList.add(val.getValue());
                }

                scoreRecordMap.clear();

                for (int a=0; a<improvedFinalScoreList.size(); a++){
                    int minPosition =1;
                    Double min = improvedFinalScoreList.get(a);
                    for (int b = a+1; b<improvedFinalScoreList.size(); b++){
                        if (improvedFinalScoreList.get(b).compareTo(min)<0){
                            minPosition = b;
                            min = improvedFinalScoreList.get(b);
                        }
                    }
                    if (minPosition != a){
                        Double tempTimes = improvedFinalScoreList.get(a);
                        improvedFinalScoreList.set(a,min);
                        improvedFinalScoreList.set(minPosition,tempTimes);
                        Double tempFreeSpeed = improvedFreeSpeedList.get(a);
                        improvedFreeSpeedList.set(a,improvedFreeSpeedList.get(minPosition));
                        improvedFreeSpeedList.set(minPosition,tempFreeSpeed);
                    }
                }

                System.out.println("improvedFreeSpeedList:"+improvedFreeSpeedList);
                //目前排序未完成，完成排序后把bestImprovedFreeSpeed赋值即可
                bestImprovedFreeSpeed = improvedFreeSpeedList.get(0);
                System.out.println("best improved free speed"+bestImprovedFreeSpeed);
                newLink.setFreespeed(bestImprovedFreeSpeed);
                improvedCounter++;



                improvedFreeSpeedList.clear();
                improvedFinalScoreList.clear();

                Thread.sleep(100);
                System.out.println("improved times ="+improvedCounter);

                if (improveCounter>=idCollectionList.size()){
                    break;
                }

            }



        tsvWriter.close();
        return 0;
    }

    private void AbsolutDifference() {
        travelTimeDifference =Math.abs(networkTravelTime-validatedTravelTime);
        distanceDifference = Math.abs(networkDistance-validatedDistance);

        scoreTravelTime=travelTimeDifference/validatedTravelTime;
        scoreDistance =distanceDifference/validatedDistance;

        networkTravelTimeDifference = Math.abs((networkTravelTime -validatedTravelTime) / validatedTravelTime);
        networkDistanceDifference = Math.abs((networkDistance -validatedDistance)/ validatedDistance);
    }


    private void addElement(List<Double> networkTravelTimeList, List<Double> validatedTravelTimeList, List<Double> networkDistanceList, List<Double> validatedDistanceList, List<Double> travelTimeDifferenceList, List<Double> distanceDifferenceList, List<Double> scoreTravelTimeList, List<Double> scoreDistanceList, List<Double>networkTravelTimeDifferenceList, List<Double>networkDistanceDifferenceList) {
        networkTravelTimeList.add(networkTravelTime);
        validatedTravelTimeList.add(validatedTravelTime);
        networkDistanceList.add(networkDistance);
        validatedDistanceList.add(validatedDistance);
        travelTimeDifferenceList.add(travelTimeDifference);
        distanceDifferenceList.add(distanceDifference);
        scoreTravelTimeList.add(scoreTravelTime);
        scoreDistanceList.add(scoreDistance);
        networkTravelTimeDifferenceList.add(networkTravelTimeDifference);
        networkDistanceDifferenceList.add(networkDistanceDifference);
    }

    private void clearElement(List<Double>networkTravelTimeList, List<Double>validatedTravelTimeList, List<Double>networkDistanceList,List<Double> validatedDistanceList, List<Double>travelTimeDifferenceList, List<Double>distanceDifferenceList, List<Double>scoreTravelTimeList, List<Double>scoreDistanceList){
        networkTravelTimeList.clear();
        validatedTravelTimeList.clear();
        networkDistanceList.clear();
        validatedDistanceList.clear();
        travelTimeDifferenceList.clear();
        distanceDifferenceList.clear();
        scoreTravelTimeList.clear();
        scoreDistanceList.clear();
    }

    private void ScoreCalculation (){
        double finalTravelTimeScore =0.0;
        double finalDistanceScore =0.0;

        for (Double valueNTTS : scoreTravelTimeList) {
            double networkTravelTimeScore = Math.abs(valueNTTS);
            finalTravelTimeScore += networkTravelTimeScore;
        }

        for (Double valueNDS : scoreDistanceList) {
            double networkDistanceScore = Math.abs(valueNDS);
            finalDistanceScore += networkDistanceScore;
        }


        int actualValidation =maxValidations-validatedZero;
        finalScore =Math.abs((((finalTravelTimeScore*0.5+finalDistanceScore*0.5)/actualValidation)-1));

        System.out.println("final score ="+finalScore);


        //计算评分误差值

        for (double valueNTTD : networkTravelTimeDifferenceList) {
            double networkTravelTimeDifferences =Math.abs(valueNTTD);
            networkTravelTimeDifferenceSum = networkTravelTimeDifferenceSum + networkTravelTimeDifferences;
        }

        for (double valueNDD : networkDistanceDifferenceList) {
            double networkDistanceDifferences =Math.abs(valueNDD);
            networkDistanceDifferenceSum = networkDistanceDifferenceSum + networkDistanceDifferences;
        }

        networkTravelTimeDeviation =networkTravelTimeDifferenceSum/actualValidation;
        networkDistanceDeviation =networkDistanceDifferenceSum/actualValidation;

        System.out.println("Network Travel Time Deviation is: "+ networkTravelTimeDeviation);
        System.out.println("Network Distance Deviation is: "+networkDistanceDeviation);
    }

    //
    private void IdStatistic() {
        for (Id<Link> id : idCollectionList){
            Integer idTimes = idAnalyseMap.get(id);
            idAnalyseMap.put(id,(idTimes == null) ? 1: idTimes +1);
        }

        for (Map.Entry<Id<Link>, Integer> val: idAnalyseMap.entrySet()) {
            System.out.println("Element:" + val.getKey() + " times:" + val.getValue());

            idStatisticsList.add(val.getKey());
            idStatisticsTimeList.add(val.getValue());
        }
        for (int statisticCounter=0; statisticCounter<idStatisticsTimeList.size(); statisticCounter++){
            int minPosition =1;
            Integer min = idStatisticsTimeList.get(statisticCounter);
            for (int statisticTimeCounter = statisticCounter+1; statisticTimeCounter<idStatisticsTimeList.size(); statisticTimeCounter++){
                if (idStatisticsTimeList.get(statisticTimeCounter).compareTo(min)<0){
                    minPosition = statisticTimeCounter;
                    min = idStatisticsTimeList.get(statisticTimeCounter);
                }
            }
            if (minPosition != statisticCounter){
                Integer tempTimes = idStatisticsTimeList.get(statisticCounter);
                idStatisticsTimeList.set(statisticCounter,min);
                idStatisticsTimeList.set(minPosition,tempTimes);
                Id<Link> tempId = idStatisticsList.get(statisticCounter);
                idStatisticsList.set(statisticCounter,idStatisticsList.get(minPosition));
                idStatisticsList.set(minPosition,tempId);
            }
        }

        Collections.reverse(idStatisticsList);
        Collections.reverse(idStatisticsTimeList);

        for (int idRecordListCounter =0; idRecordListCounter<=idStatisticsList.size()*0.1; idRecordListCounter++){
            idRecordList.add(idStatisticsList.get(idRecordListCounter));
            //idRecordList.add(val.getKey());
        }
        //System.out.println("id Record List"+idRecordList);
        System.out.println("idStatisticsList size:" +idStatisticsList.size());


        for (int idRecord =0; idRecord<idRecordList.size(); idRecord++){
            double improveFreeSpeed = idCollectionMap.get(idRecordList.get(idRecord));

            idCollectionList.add(idRecordList.get(idRecord));
            freeSpeedCollectionList.add(improveFreeSpeed);
            idImproveMap.put(idRecordList.get(idRecord), improveFreeSpeed);
        }

 /*
        for (Id<Link> id : idRecordList) {
            double improveFreeSpeed = idCollectionMap.get(id);

            idCollectionList.add(id);
            freeSpeedCollectionList.add(improveFreeSpeed);
            idImproveMap.put(id, improveFreeSpeed);

        }
*/

        System.out.println("idImproveMap: "+idImproveMap);
        System.out.println("id RecordList size:" + idRecordList.size());
        System.out.println("id Record List" +idRecordList);
        System.out.println("id improved Map size: " +idImproveMap.size());
        System.out.println("id Collection List"+idCollectionList);
        System.out.println("free Speed Collection List :" +freeSpeedCollectionList);
    }
}

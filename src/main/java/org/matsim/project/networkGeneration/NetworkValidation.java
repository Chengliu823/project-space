package org.matsim.project.networkGeneration;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.matsim.api.core.v01.Coord;
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

    @Override
    public Integer call() throws Exception {
        CoordinateTransformation ct = new GeotoolsTransformation("EPSG:25832", "EPSG:4326");
        TravelTimeDistanceValidator validator;
        MainModeIdentifier mainModeIdentifier = new DefaultAnalysisMainModeIdentifier();


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

        int validated = 0;

        int validatedZero = 0;//样本中有多少个数据事无效的，需要时可导出

        //定义条件
        double networkTravelTime = 0;
        double validatedTravelTime = 0;
        double networkDistance = 0;
        double validatedDistance = 0;
        double travelTimeDifference =0;
        double distanceDifference =0;
        double scoreTravelTime=0;
        double scoreDistance =0;
        //平均数


        double advScore =0.0;

        //double adv_networkTravelTimeSum =0.0;

        //定义arraylist
        List<Double> networkTravelTimeList = new ArrayList<>();
        List<Double> validatedTravelTimeList = new ArrayList<>();
        List<Double> networkDistanceList = new ArrayList<>();
        List<Double> validatedDistanceList = new ArrayList<>();
        List<Double> travelTimeDifferenceList = new ArrayList<>();
        List<Double> distanceDifferenceList = new ArrayList<>();
        List<Double> scoreTravelTimeList = new ArrayList<>();
        List<Double> scoreDistanceList = new ArrayList<>();
        List<Double> allValidatedTravelTimeList = new ArrayList<>();
        List<Double> allValidatedDistanceList = new ArrayList<>();

        List<Id<Link>> idCollectionList = new ArrayList<>();


        //比例
        double networkTravelTimeDifference;
        double networkDistanceDifference;
        double networkTravelTimeDifferenceSum = 0;
        double networkDistanceDifferenceSum = 0;
        double networkTravelTimeDeviation;
        double networkDistanceDeviation;
        double finalScore =0.0;

        //定义比例的的arraylist
        List<Double> networkTravelTimeDifferenceList = new ArrayList<>();
        List<Double> networkDistanceDifferenceList = new ArrayList<>();

        Map<Id<Link>, Double> idCollectionMap = new HashMap<>();
        Map<Id<Link>, Double> idImproveMap = new HashMap<>();
        Map<String, Double> allValidatedTravelTimeMap = new HashMap<>();
        Map<String, Double> allValidatedDistanceMap = new HashMap<>();

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
//              double departureTime = 3600;   // To test free speed travel time

                Tuple<Double, Double> validatedResult = validator.getTravelTime(from, to, departureTime, tripId);
                LeastCostPathCalculator.Path route = router.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(), departureTime, null, null);//计算路径的方法
                double networkTravelDistance = route.links.stream().mapToDouble(Link::getLength).sum();
                //route.travelTime
                //route.links //查看当前经过的路径


                /*Map<String, MutableInt> counterMap = new HashMap<>();
                for (Link link : route.links) {
                    //if (!counterMap.containsKey(link.getId().toString())) {
                        counterMap.put(link.getId().toString(), new MutableInt());
                    }
                    counterMap.get(link.getId().toString()).increment();//

                    counterMap.computeIfAbsent(link.getId().toString(), v -> new MutableInt()).increment();//检查是否存在变量，存在才采取操作
                    link.getId().toString();
                    link.setFreeSpeed(130);
                }

                Id<Link> myLinkId = Id.createLinkId("12345");
                Link myLink = network.getLinks().get(myLinkId);
                myLink.setFreeSpeed(100);*/
                /*Id<Link> myLinkId = Id.createLinkId("12345");
                Link myLink = network.getLinks().get(myLinkId);*/

                tsvWriter.printRecord(tripId, from.getX(), from.getY(), to.getX(), to.getY(), route.travelTime, validatedResult.getFirst(), networkTravelDistance, validatedResult.getSecond());

                /*networkTravelTimeSum = networkTravelTimeSum + route.travelTime;
                validatedTravelTimeSum = validatedTravelTimeSum + validatedResult.getFirst();
                networkDistanceSum = networkDistanceSum +networkTravelDistance;
                validatedDistanceSum = validatedDistanceSum +validatedResult.getSecond();*/

                networkTravelTime = route.travelTime;
                validatedTravelTime = validatedResult.getFirst();
                networkDistance = networkTravelDistance;
                validatedDistance = validatedResult.getSecond();

                allValidatedTravelTimeMap.put(tripId, validatedTravelTime);
                allValidatedDistanceMap.put(tripId, validatedDistance);

                travelTimeDifference =Math.abs(networkTravelTime-validatedTravelTime);
                distanceDifference = Math.abs(networkDistance-networkDistanceDifferenceSum);

                scoreTravelTime=travelTimeDifference/validatedTravelTime;
                scoreDistance =distanceDifference/validatedDistance;

                if (networkTravelTime != 0 && validatedTravelTime != 0 && networkDistance != 0 && validatedDistance != 0) {
                    networkTravelTimeList.add(networkTravelTime);
                    validatedTravelTimeList.add(validatedTravelTime);
                    networkDistanceList.add(networkDistance);
                    validatedDistanceList.add(validatedDistance);
                    travelTimeDifferenceList.add(travelTimeDifference);
                    distanceDifferenceList.add(distanceDifference);
                    scoreTravelTimeList.add(scoreTravelTime);
                    scoreDistanceList.add(scoreDistance);
                            //idList.clear();
                            for (Link link : route.links){
                                idCollectionList.add(link.getId());
                                idCollectionMap.put(link.getId(),link.getFreespeed());
                            }

                } else {
                    validatedZero++;
                    System.out.println("validated Zero ="+validatedZero);
                }
            }

            validated++;
            personTripCounter++;
            Thread.sleep(200);

            if (validated >= maxValidations) {
                break;
            }

        }

        double finalTravelTimeScore =0.0;
        double finalDistanceScore =0.0;

        for (int i = 0; i < scoreTravelTimeList.size(); i++) {
            double nts = Math.abs(scoreTravelTimeList.get(i));
            finalTravelTimeScore +=nts;
        }

        for (int i = 0; i < scoreDistanceList.size(); i++) {
            double nts = Math.abs(scoreDistanceList.get(i));
            finalDistanceScore +=nts;
        }


        int actualValidation =maxValidations-validatedZero;
        finalScore =Math.abs(((finalTravelTimeScore*0.5+finalDistanceScore*0.5)/actualValidation-1));

        System.out.println("final score ="+finalScore);

        //System.out.println("NetworkTravelTimeList 1:"+networkTravelTimeList+"size"+networkTravelTimeList.size());
        //System.out.println("NetworkTravelTimeList 1:"+validatedTravelTimeList);
        //System.out.println("NetworkTravelTimeList 1:"+networkDistanceList);
        //System.out.println("NetworkTravelTimeList 1:"+validatedDistanceList);

            /*double networkTravelTimeSum = 0.0;
            double validatedTravelTimeSum = 0.0;
            double networkDistanceSum = 0.0;
            double validatedDistanceSum = 0.0;

            for (int i = 0; i < networkTravelTimeList.size(); i++) {
                double nts = networkTravelTimeList.get(i);
                networkTravelTimeSum +=nts;
            }

            for (int i = 0; i < validatedTravelTimeList.size(); i++) {
                double vts = validatedTravelTimeList.get(i);
                validatedTravelTimeSum = validatedTravelTimeSum + vts;
            }

            for (int i = 0; i < networkDistanceList.size(); i++) {
                double nds = networkDistanceList.get(i);
                networkDistanceSum = networkDistanceSum + nds;
            }

            for (int i = 0; i < validatedDistanceList.size(); i++) {
                double vds = validatedDistanceList.get(i);
                validatedDistanceSum = validatedDistanceSum + vds;
            }


            //System.out.println("NtworkTravelTimeList"+networkTravelTimeList);
            //System.out.println("NetworkTravelTimeSize="+ validated);
            //System.out.println("validatedTravelTimelist"+validatedTravelTimeList);



            //计算平均数
            double adv_networkTravelTimeSum = networkTravelTimeSum / networkTravelTimeList.size();
            double adv_validatedTravelTimeSum = validatedTravelTimeSum / validatedTravelTimeList.size();
            double adv_networkDistanceSum = networkDistanceSum / networkDistanceList.size();
            double adv_validatedDistanceSum = validatedDistanceSum / validatedDistanceList.size();
            double abs_adv_nts_vts = Math.abs(adv_networkTravelTimeSum - adv_validatedTravelTimeSum);
            double abs_adv_nds_vds = Math.abs(adv_networkDistanceSum - adv_validatedDistanceSum);


            double scoreTs = 1 - abs_adv_nts_vts / adv_validatedTravelTimeSum;
            double scoreDs = 1 - abs_adv_nds_vds / adv_validatedDistanceSum;
            double advScoreSum = scoreTs + scoreDs;


            finalScore =Math.abs((advScoreSum-1));//减去一相当于百分之多少的偏差
            System.out.println("Mean Deviation = " + finalScore);*/

            //计算评分误差值

            networkTravelTimeDifference = Math.abs((networkTravelTime -validatedTravelTime) / validatedTravelTime);
            networkTravelTimeDifferenceList.add(networkTravelTimeDifference);
            networkDistanceDifference = Math.abs((networkDistance -validatedDistance)/ validatedDistance);
            networkDistanceDifferenceList.add(networkDistanceDifference);


            for (int i = 0; i < networkTravelTimeDifferenceList.size(); i++) {
                double ntss = networkTravelTimeDifferenceList.get(i);
                networkTravelTimeDifferenceSum = networkTravelTimeDifferenceSum + ntss;
            }

            for (int i = 0; i < networkDistanceDifferenceList.size(); i++) {
                double ndss = networkDistanceDifferenceList.get(i);
                networkDistanceDifferenceSum = networkDistanceDifferenceSum + ndss;
            }

            networkTravelTimeDeviation =networkTravelTimeDifferenceSum/actualValidation;
            networkDistanceDeviation =networkDistanceDifferenceSum/actualValidation;

            System.out.println("Network Travel Time Deviation is: "+ networkTravelTimeDeviation);
            System.out.println("Network Distance Deviation is: "+networkDistanceDeviation);


            //统计所需id

            List<Id> idRecordList = new ArrayList<>();
            //获取需要优化的link 的id
            Map<Id, Integer> idAnalyseMap = new HashMap<>();
            for (Id id : idCollectionList){
                Integer idTimes = idAnalyseMap.get(id);
                idAnalyseMap.put(id,(idTimes == null) ? 1: idTimes +1);
            }

            Map<String, Integer> idRecordMap = new HashMap<>();

            List<Id> idStatisticsList = new ArrayList<>();
            List<Integer> idStatisticsTimeList = new ArrayList<>();

            for (Map.Entry<Id, Integer> val: idAnalyseMap.entrySet()) {
                System.out.println("Element:" + val.getKey() + " times:" + val.getValue());

                idStatisticsList.add(val.getKey());
                idStatisticsTimeList.add(val.getValue());
            }
            for (int i=0; i<idStatisticsTimeList.size(); i++){
                int minPosition =1;
                Integer min = idStatisticsTimeList.get(i);
                for (int j = i+1; j<idStatisticsTimeList.size(); j++){
                    if (idStatisticsTimeList.get(j).compareTo(min)<0){
                        minPosition = j;
                        min = idStatisticsTimeList.get(j);
                    }
                }
                if (minPosition != i){
                    Integer tempTimes = idStatisticsTimeList.get(i);
                    idStatisticsTimeList.set(i,min);
                    idStatisticsTimeList.set(minPosition,tempTimes);
                    Id tempId = idStatisticsList.get(i);
                    idStatisticsList.set(i,idStatisticsList.get(minPosition));
                    idStatisticsList.set(minPosition,tempId);
                }
            }

            Collections.reverse(idStatisticsList);
            Collections.reverse(idStatisticsTimeList);

                    /*idRecordMap.put(val.getKey(),val.getValue());

                    List<Map.Entry<String,Integer>> idRecordMapList = new ArrayList<>(idRecordMap.entrySet());
                    Collections.sort(idRecordMapList, (o1, o2) -> o2.getValue() - o1.getValue());

                    System.out.println("id Record Map List"+idRecordMapList);*/


            for (int i =0; i<=idStatisticsList.size()*0.1; i++){
                idRecordList.add(idStatisticsList.get(i));
                //idRecordList.add(val.getKey());
            }
            //System.out.println("id Record List"+idRecordList);
            System.out.println("idStatisticsList size:" +idStatisticsList.size());



            for (int i =0; i<idRecordList.size(); i++){
                double improveFreeSpeed = idCollectionMap.get(idRecordList.get(i));
                idImproveMap.put(idRecordList.get(i),improveFreeSpeed);
            }

            System.out.println("idImproveMap: "+idImproveMap);
            System.out.println("id RecordList size:" + idRecordList.size());
            System.out.println("id Record List" +idRecordList);


            //替换link的算法部分
            double improvedTravelTimeDifference =0.0;
            double improvedDistanceDifference =0.0;
            double improvedScoreTravelTime =0.0;
            double improvedScoreDistance =0.0;
            double improvedNetworkTravelTime = 0.0;
            double improvedValidatedTravelTime = 0.0;
            double improvedNetworkDistance =0.0;
            double improvedValidatedDistance =0.0;
            double improvedNetworkDistanceDeviation=0.0;
            double improvedFinalScore = 0.0;
            double improvedFreeSpeed =0.0;
            double improvedNetworkTravelTimeDeviation =0.0;
            double bestImprovedFreeSpeed = 0.0;
            double linkFreeSpeed =0.0;

            int improvedValidatedTimes =0;
            int improvedZeroCounter=0;
            int improvedCounter=0;


            List<Double> improvedNetworkTravelTimeList = new ArrayList<>();
            List<Double> improvedNetworkDistanceList = new ArrayList<>();
            List<Double> improvedValidatedTravelTimeList = new ArrayList<>();
            List<Double> improvedValidatedDistanceList = new ArrayList<>();
            List<Double> improvedTravelTimeDifferenceList = new ArrayList<>();
            List<Double> improvedDistanceDifferenceList = new ArrayList<>();
            List<Double> improvedScoreTravelTimeList = new ArrayList<>();
            List<Double> improvedScoreDistanceList = new ArrayList<>();
            List<Double> improvedFreeSpeedList = new ArrayList<>();
            List<Double> improvedFinalScoreList = new ArrayList<>();



            Map<Double,Double> scoreRecordMap = new HashMap<>();

            for (int i=0 ; i<idImproveMap.size(); i++){
                Id<Link> newIdLink = Id.createLinkId(idRecordList.get(i));
                Link newLink = network.getLinks().get(newIdLink);
                linkFreeSpeed = idImproveMap.get(idRecordList.get(i));
                System.out.println("link id:"+idRecordList.get(i)+" "+ "free Speed:" +linkFreeSpeed);

                for (int freeSpeedRate=90; freeSpeedRate<=110; freeSpeedRate+=10){
                    improvedFreeSpeed =linkFreeSpeed*(freeSpeedRate/100);
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

                            improvedNetworkTravelTime=route.travelTime;
                            improvedValidatedTravelTime =allValidatedTravelTimeMap.get(tripId);
                            improvedNetworkDistance=networkTravelDistance;
                            improvedValidatedDistance=allValidatedDistanceMap.get(tripId);

                            improvedTravelTimeDifference =Math.abs(improvedNetworkTravelTime-improvedValidatedTravelTime);
                            improvedDistanceDifference = Math.abs(improvedNetworkDistance-improvedValidatedDistance);

                            improvedScoreTravelTime=improvedTravelTimeDifference/improvedValidatedTravelTime;
                            improvedScoreDistance =improvedDistanceDifference/improvedValidatedDistance;

                            if (improvedNetworkTravelTime != 0 && improvedValidatedTravelTime != 0 && improvedNetworkDistance != 0 && improvedValidatedDistance != 0) {
                                improvedNetworkTravelTimeList.add(networkTravelTime);
                                improvedValidatedTravelTimeList.add(validatedTravelTime);
                                improvedNetworkDistanceList.add(networkDistance);
                                improvedValidatedDistanceList.add(validatedDistance);
                                improvedTravelTimeDifferenceList.add(travelTimeDifference);
                                improvedDistanceDifferenceList.add(distanceDifference);
                                improvedScoreTravelTimeList.add(scoreTravelTime);
                                improvedScoreDistanceList.add(scoreDistance);
                            } else {
                                improvedZeroCounter++;
                            }
                        }

                        improvedValidatedTimes++;
                        if (improvedValidatedTimes>maxValidations){
                            break;
                        }

                        double improvedFinalTravelTimeScore =0.0;
                        double improvedFinalDistanceScore =0.0;

                        for (int Counter1 = 0; Counter1 < scoreTravelTimeList.size(); Counter1++) {
                            double nts = Math.abs(scoreTravelTimeList.get(Counter1));
                            improvedFinalTravelTimeScore +=nts;
                        }

                        for (int Counter2 = 0; Counter2 < scoreDistanceList.size(); Counter2++) {
                            double nts = Math.abs(scoreDistanceList.get(Counter2));
                            improvedFinalDistanceScore +=nts;
                        }

                        double improvedNetworkTravelTimeDifference =0.0;
                        double improvedNetworkDistanceDifference =0.0;
                        double improvedNetworkTravelTimeDifferenceSum =0.0;
                        double improvedNetworkDistanceDifferenceSum =0.0;

                        List<Double> improvedNetworkTravelTimeDifferenceList = new ArrayList<>();
                        List<Double> improvedNetworkDistanceDifferenceList = new ArrayList<>();


                        int actualImproved =maxValidations-improvedZeroCounter;
                        improvedFinalScore =Math.abs(((improvedFinalTravelTimeScore*0.5+improvedFinalDistanceScore*0.5)/actualImproved-1));

                        improvedNetworkTravelTimeDifference = Math.abs((improvedNetworkTravelTime -improvedValidatedTravelTime)/improvedValidatedTravelTime);
                        improvedNetworkTravelTimeDifferenceList.add(improvedNetworkTravelTimeDifference);
                        improvedNetworkDistanceDifference = Math.abs((improvedNetworkDistance -improvedValidatedDistance)/ improvedValidatedDistance);
                        improvedNetworkDistanceDifferenceList.add(improvedNetworkDistanceDifference);


                        for (int Counter3 = 0; Counter3 < improvedNetworkTravelTimeDifferenceList.size(); Counter3++) {
                            double intss = improvedNetworkTravelTimeDifferenceList.get(Counter3);
                            improvedNetworkTravelTimeDifferenceSum = improvedNetworkTravelTimeDifferenceSum + intss;
                        }

                        for (int Counter4 = 0; Counter4< improvedNetworkDistanceDifferenceList.size(); Counter4++) {
                            double indss = improvedNetworkDistanceDifferenceList.get(Counter4);
                            improvedNetworkDistanceDifferenceSum = improvedNetworkDistanceDifferenceSum + indss;
                        }

                        improvedNetworkTravelTimeDeviation =improvedNetworkTravelTimeDifferenceSum/actualImproved;
                        improvedNetworkDistanceDeviation =improvedNetworkDistanceDifferenceSum/actualImproved;
                    }

                    //这里开始挑选数据
                    if (networkDistanceDeviation == improvedNetworkDistanceDeviation){
                        if (improvedFinalScore <= finalScore && improvedNetworkTravelTimeDeviation > networkTravelTimeDeviation * 0.8 && improvedNetworkTravelTimeDeviation <networkTravelTimeDeviation * 1.2){
                            scoreRecordMap.put(improvedFreeSpeed,improvedFinalScore); //此处排序可以用别的方法实现，标记
                        }

                    }

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
                //目前排序未完成，完成排序后把bestimprovedfreespeed赋值即可
                bestImprovedFreeSpeed = improvedFreeSpeedList.get(0);
                System.out.println("best improved free speed"+bestImprovedFreeSpeed);
                newLink.setFreespeed(bestImprovedFreeSpeed);
                improvedCounter++;

                improvedNetworkTravelTimeList.clear();
                improvedValidatedTravelTimeList.clear();
                improvedNetworkDistanceList.clear();
                improvedValidatedDistanceList.clear();
                improvedTravelTimeDifferenceList.clear();
                improvedDistanceDifferenceList.clear();
                improvedScoreTravelTimeList.clear();
                improvedScoreDistanceList.clear();

                improvedFreeSpeedList.clear();
                improvedFinalScoreList.clear();

                Thread.sleep(200);
                System.out.println("improved times ="+improvedCounter);
            }



        tsvWriter.close();
        return 0;
    }

    public void calculation(){

    }

}

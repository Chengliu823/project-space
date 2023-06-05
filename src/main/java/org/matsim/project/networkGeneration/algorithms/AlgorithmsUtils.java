package org.matsim.project.networkGeneration.algorithms;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.*;

public class AlgorithmsUtils{
    private AlgorithmsUtils(){}

    public static double listSort (List<ImproveScore> improveScoreList){
        double bestFreeSpeed;
        int index =0;
        double score =improveScoreList.get(index).getImproveScore();

        for (int i = 0; i < improveScoreList.size(); i++) {
            if (score >= improveScoreList.get(i).getImproveScore()){
                score = improveScoreList.get(i).getImproveScore();
                index =i;
            }
        }
        bestFreeSpeed = improveScoreList.get(index).getFreeSpeed();
        return  bestFreeSpeed;
    }

    public static List<Id<Link>> sort (List<LinkCollection> linkCollectionList){
        List<Id<Link>> idStatisticsList = new ArrayList<>();
        List<Integer> idStatisticsCountList = new ArrayList<>();
        HashMap<Id<Link>,Integer> idStatisticMap = new HashMap<>();


        for (int i = 0; i < linkCollectionList.size(); i++) {
            Id<Link> id = linkCollectionList.get(i).getAlgorithmsId();
            Integer idCount = idStatisticMap.get(id);
            idStatisticMap.put(id,(idCount == null)? 1 : idCount++);
        }

        for (Map.Entry<Id<Link>, Integer> val: idStatisticMap.entrySet()) {
            idStatisticsList.add(val.getKey());
            idStatisticsCountList.add(val.getValue());
        }

        for (int i=0; i<idStatisticsCountList.size(); i++){
            int index =1;
            Integer min = idStatisticsCountList.get(i);
            for (int j = i+1; j<idStatisticsCountList.size(); j++){
                if (idStatisticsCountList.get(j).compareTo(min)<0){
                    index = j;
                    min = idStatisticsCountList.get(j);
                }
            }
            if (index != i){
                Integer temp = idStatisticsCountList.get(i);
                Id<Link> tempId = idStatisticsList.get(i);

                idStatisticsCountList.set(i,min);
                idStatisticsCountList.set(index,temp);

                idStatisticsList.set(i,idStatisticsList.get(index));
                idStatisticsList.set(index,tempId);
            }
        }

        Collections.reverse(idStatisticsList);
        Collections.reverse(idStatisticsCountList);

        return idStatisticsList;
    }

        /*
        1.Select the desired link
     */

    public static List<LinkCollection> linkChoice (List<LinkCollection> linkCollectionList, Map<Id<Link>, Double> idCollectionMap){
        List<LinkCollection> improveLinkList = new ArrayList<>();

        List<Id<Link>>idStatisticsList = sort(linkCollectionList);

        for (int i = 0; i < idStatisticsList.size()*0.2; i++) {
            Id<Link> id = idStatisticsList.get(i);
            LinkCollection linkCollection = new LinkCollection(id,idCollectionMap.get(id));
            improveLinkList.add(linkCollection);
        }

        return improveLinkList;
    }


    /*
       Calculate the degree of dispersion of travelTime, the lower the degree of dispersion, the better the optimization effect
     */
    public static double travelTimeDeviation (List<RouteInfo> routeInfoList){
        List<Double> travelTimeQuotientList =new ArrayList<>();

        double travelTimeQuotientSum =0;
        double travelTimeDeviation;
        double travelTimeQuotient;

        for (int i = 0; i < routeInfoList.size(); i++) {
            travelTimeQuotient=Math.abs((routeInfoList.get(i).getNetworkTravelTime()/ routeInfoList.get(i).getValidationTravelTime())-1);
            travelTimeQuotientList.add(travelTimeQuotient);
        }

        for (int i = 0; i < travelTimeQuotientList.size(); i++) {
            travelTimeQuotientSum+= travelTimeQuotientList.get(i);
        }
        travelTimeDeviation =travelTimeQuotientSum/travelTimeQuotientList.size();

        return travelTimeDeviation;
    }

    /*
       Calculate the degree of dispersion of distance, the lower the degree of dispersion, the better the optimization effect
     */

    public static double distanceDeviation (List<RouteInfo> routeInfoList){
        List<Double> distanceQuotientList = new ArrayList<>();
        double distanceQuotientSum =0;
        double result;
        for (int i = 0; i < routeInfoList.size(); i++) {
            distanceQuotientList.add(Math.abs((routeInfoList.get(i).getNetworkDistance()/ routeInfoList.get(i).getValidationDistance())-1));
        }
        for (int i = 0; i < distanceQuotientList.size(); i++) {
            distanceQuotientSum += distanceQuotientList.get(i);
        }
        result = distanceQuotientSum/distanceQuotientList.size();
        return result;
    }


    /*
       求network的得分情况,该评分将用于比较network优化前后的效果，该评分越低越好
       1.计算validation的平均数
       2.计算Network减去Validation的差值，将差值转化为绝对值
       3.Network与validation之间差值的绝对值的和的平均数除以validation值的平均数
 */


    //calculate the score of the travel time,This score will be used to compare the effect of the network before and after optimization. The lower the score, the better.
    //Design three different object functions for easy comparison

/*
    public static double travelTimeScoreCalculation(List<RouteInfo> routeInfoList){// Algorithms3
        double scoreNetworkTravelTime;
        double validationTravelTime;
        double networkTravelTime;
        double travelTimeScoreSum = 0;

        for (RouteInfo routeInfo : routeInfoList){
            networkTravelTime = routeInfo.getNetworkTravelTime();
            validationTravelTime = routeInfo.getValidationTravelTime();
            travelTimeScoreSum +=Math.pow((Math.abs(networkTravelTime-validationTravelTime)/validationTravelTime),2)+Math.abs((networkTravelTime-validationTravelTime)/validationTravelTime);
        }

        scoreNetworkTravelTime =(travelTimeScoreSum/ routeInfoList.size());

        return scoreNetworkTravelTime;
    }

 */







/*
       public static double travelTimeScoreCalculation(List<RouteInfo> routeInfoList){// Algorithms2
        double scoreNetworkTravelTime;
        double validationTravelTime;
        double networkTravelTime;
        double travelTimeScoreSum = 0;

        for (RouteInfo routeInfo : routeInfoList){
            networkTravelTime = routeInfo.getNetworkTravelTime();
            validationTravelTime = routeInfo.getValidationTravelTime();
            travelTimeScoreSum +=Math.pow((Math.abs(networkTravelTime-validationTravelTime)/validationTravelTime),2);
        }
        scoreNetworkTravelTime =(travelTimeScoreSum/ routeInfoList.size());

        return scoreNetworkTravelTime;
    }


 */








        public static double travelTimeScoreCalculation(List<RouteInfo> routeInfoList){// Algorithms1
        double validationTravelTimeSum=0.0;
        double avg_validationTravelTime;
        double abs_NetworkValidationTravelTimeDifferenceSum =0.0;
        double scoreNetworkTravelTime;
        double validationTravelTime;
        double networkTravelTime;

        List<Double> abs_NetworkValidationTravelTimeDifferenceList = new ArrayList<>();

        for (RouteInfo routeInfo : routeInfoList) {
            networkTravelTime = routeInfo.getNetworkTravelTime();
            validationTravelTime=routeInfo.getValidationTravelTime();


            validationTravelTimeSum += validationTravelTime;
            abs_NetworkValidationTravelTimeDifferenceList.add(Math.abs(networkTravelTime - validationTravelTime));
        }

        avg_validationTravelTime =validationTravelTimeSum/ routeInfoList.size();

        for (Double abs_NetworkValidationTravelTimeDifference : abs_NetworkValidationTravelTimeDifferenceList) {
            abs_NetworkValidationTravelTimeDifferenceSum += abs_NetworkValidationTravelTimeDifference;
        }

        //scoreNetworkTravelTime =(avg_validationTravelTime- (abs_NetworkValidationTravelTimeDifferenceSum/abs_NetworkValidationTravelTimeDifferenceList.size()))/avg_validationTravelTime;
        scoreNetworkTravelTime = (abs_NetworkValidationTravelTimeDifferenceSum/abs_NetworkValidationTravelTimeDifferenceList.size())/avg_validationTravelTime;

        return scoreNetworkTravelTime;
    }











    public static double distanceScoreCalculation(List<RouteInfo> routeInfoList){
        double validationDistanceSum=0.0;
        double avg_validationDistance;
        double abs_NetworkValidationDistanceDifferenceSum =0.0;
        double scoreNetworkDistance;


        List<Double> abs_NetworkValidationDistanceDifferenceList = new ArrayList<>();

        for (RouteInfo routeInfo : routeInfoList) {
            validationDistanceSum += routeInfo.getValidationDistance();
            abs_NetworkValidationDistanceDifferenceList.add(Math.abs(routeInfo.getNetworkDistance() - routeInfo.getValidationDistance()));
        }

        avg_validationDistance =validationDistanceSum/ routeInfoList.size();

        for (Double NetworkValidationDistanceDifference : abs_NetworkValidationDistanceDifferenceList) {
            abs_NetworkValidationDistanceDifferenceSum += NetworkValidationDistanceDifference;
        }
        scoreNetworkDistance =(abs_NetworkValidationDistanceDifferenceSum/abs_NetworkValidationDistanceDifferenceList.size())/avg_validationDistance;


        return scoreNetworkDistance;
    }

}

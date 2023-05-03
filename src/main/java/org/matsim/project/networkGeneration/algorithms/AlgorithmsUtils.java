package org.matsim.project.networkGeneration.algorithms;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.*;

public class AlgorithmsUtils {
    private AlgorithmsUtils(){}

    public static int scoreSort(List<ScoreInfo> scoreList){
        int index =0;
        double score =scoreList.get(index).getTravelTimeScore();

        for (int i = 0; i < scoreList.size(); i++) {
            double tempScore = scoreList.get(i).getTravelTimeScore();
            if (score<= tempScore){
                index =i;
            }
        }

        return index;
    }
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

    public static List<Id<Link>> sort (List<AlgorithmsLink> algorithmsLinkList){
        List<Id<Link>> idStatisticsList = new ArrayList<>();
        List<Integer> idStatisticsCountList = new ArrayList<>();
        HashMap<Id<Link>,Integer> idStatisticMap = new HashMap<>();


        for (int i = 0; i < algorithmsLinkList.size(); i++) {
            Id<Link> id = algorithmsLinkList.get(i).getAlgorithmsId();
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
        1.得出评分后需要筛选出需要优化的link，获取link需要首先在person的for loop中完成
        2.选出经过次数最多的link
     */

    public static List<AlgorithmsLink> linkChoice (List<AlgorithmsLink> algorithmsLinkList, Map<Id<Link>, Double> idCollectionMap){
        List<AlgorithmsLink> improveLinkList = new ArrayList<>();
        List<Id<Link>>idStatisticsList = sort(algorithmsLinkList);

        for (int i = 0; i < idStatisticsList.size()*0.1; i++) {
            Id<Link> id = idStatisticsList.get(i);
            AlgorithmsLink algorithmsLink = new AlgorithmsLink(id,idCollectionMap.get(id));
            improveLinkList.add(algorithmsLink);
        }

        return improveLinkList;
    }


    /*
       1.计算travelTime的离散程度，离散程度越低优化效果越好
       2.故做出如下设计：
       3.将每组travelTime简单相除，即travelTime/validation
       4.相加结果，再除以有效验证次数
     */
    public static double travelTimeDeviation (List<RouteInfo> routeInfoList){
        List<Double> travelTimeQuotientList =new ArrayList<>();
        double travelTimeQuotientSum =0;
        double result;
        for (int i = 0; i < routeInfoList.size(); i++) {
            travelTimeQuotientList.add(routeInfoList.get(i).getNetworkTravelTime()/ routeInfoList.get(i).getValidationTravelTime());
        }

        for (int i = 0; i < travelTimeQuotientList.size(); i++) {
            travelTimeQuotientSum+= travelTimeQuotientList.get(i);
        }
        result =travelTimeQuotientSum/travelTimeQuotientList.size();

        return result;
    }

    /*
       计算距离的差值，用于判断network的路径是否变化
       1.如果行走路径一致则结果相同
       2.故设计，将每一组distance进行简单的相除，即network/validation
       3.相加结果，再将结果除以有效验证次数
     */

    public static double distanceDeviation (List<RouteInfo> routeInfoList){
        List<Double> distanceQuotientList = new ArrayList<>();
        double distanceQuotientSum =0;
        double result;
        for (int i = 0; i < routeInfoList.size(); i++) {
            distanceQuotientList.add(routeInfoList.get(i).getNetworkDistance()/ routeInfoList.get(i).getValidationDistance());
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

    //这一步可以用抽象解决，之后改
    public static double travelTimeScoreCalculation(List<RouteInfo> routeInfoList){
        double validationTravelTimeSum=0.0;
        double avg_validationTravelTime;
        double abs_NetworkValidationTravelTimeDifferenceSum =0.0;
        double scoreNetworkTravelTime;
        double validationTravelTime;
        double networkTravelTime;


        List<Double> abs_NetworkValidationTravelTimeDifferenceList = new ArrayList<>();

        for (RouteInfo routeInfo : routeInfoList) {
            validationTravelTime=routeInfo.getValidationTravelTime();
            validationTravelTimeSum += validationTravelTime;
            networkTravelTime = routeInfo.getNetworkTravelTime();

            abs_NetworkValidationTravelTimeDifferenceList.add(Math.abs(networkTravelTime - validationTravelTime));
        }

        avg_validationTravelTime =validationTravelTimeSum/ routeInfoList.size();

        for (Double aDouble : abs_NetworkValidationTravelTimeDifferenceList) {
            abs_NetworkValidationTravelTimeDifferenceSum += aDouble;
        }
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

        for (Double aDouble : abs_NetworkValidationDistanceDifferenceList) {
            abs_NetworkValidationDistanceDifferenceSum += aDouble;
        }
        scoreNetworkDistance =(abs_NetworkValidationDistanceDifferenceSum/abs_NetworkValidationDistanceDifferenceList.size())/avg_validationDistance;


        return scoreNetworkDistance;
    }
}

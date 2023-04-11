package org.matsim.project.networkGeneration;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.*;

public class AlgorithmsUtils {
    private AlgorithmsUtils(){}

    public static double listSort (List<ImproveScore> improveScoreList){
        double bestFreeSpeed =0;
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
            int minPosition =1;
            Integer min = idStatisticsCountList.get(i);
            for (int j = i+1; j<idStatisticsCountList.size(); j++){
                if (idStatisticsCountList.get(j).compareTo(min)<0){
                    minPosition = j;
                    min = idStatisticsCountList.get(j);
                }
            }
            if (minPosition != i){
                Integer tempTimes = idStatisticsCountList.get(i);
                idStatisticsCountList.set(i,min);
                idStatisticsCountList.set(minPosition,tempTimes);
                Id<Link> tempId = idStatisticsList.get(i);
                idStatisticsList.set(i,idStatisticsList.get(minPosition));
                idStatisticsList.set(minPosition,tempId);
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
       3.将每组travelTime简单相处，即travelTime/validation
       4.相加结果，再除以有效验证次数
     */
    public static double travelTimeDeviation (List<LinkInfo> linkInfoList){
        List<Double> travelTimeQuotientList =new ArrayList<>();
        double travelTimeQuotientSum =0;
        double result;
        for (int i = 0; i < linkInfoList.size(); i++) {
            travelTimeQuotientList.add(linkInfoList.get(i).getNetworkTravelTime()/linkInfoList.get(i).getValidationTravelTime());
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

    public static double distanceDeviation (List<LinkInfo> linkInfoList){
        List<Double> distanceQuotientList = new ArrayList<>();
        double distanceQuotientSum =0;
        double result =0;
        for (int i = 0; i < linkInfoList.size(); i++) {
            distanceQuotientList.add(linkInfoList.get(i).getNetworkDistance()/linkInfoList.get(i).getValidationDistance());
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
    public static double scoreCalculation (List<LinkInfo> linkInfoList){
        double validationTravelTimeSum=0.0;
        double validationDistanceSum=0.0;
        double avg_validationTravelTime;
        double avg_validationDistance;
        double abs_NetworkValidationTravelTimeDifferenceSum =0.0;
        double abs_NetworkValidationDistanceDifferenceSum =0.0;
        double scoreNetworkTravelTime;
        double scoreNetworkDistance;
        double totalScore;

        List<Double> abs_NetworkValidationTravelTimeDifferenceList = new ArrayList<>();
        List<Double> abs_NetworkValidationDistanceDifferenceList = new ArrayList<>();

        for (LinkInfo linkInfo : linkInfoList) {
            validationTravelTimeSum += linkInfo.getValidationTravelTime();
            validationDistanceSum += linkInfo.getValidationDistance();

            abs_NetworkValidationTravelTimeDifferenceList.add(Math.abs(linkInfo.getNetworkTravelTime() - linkInfo.getValidationTravelTime()));
            abs_NetworkValidationDistanceDifferenceList.add(Math.abs(linkInfo.getNetworkDistance() - linkInfo.getValidationDistance()));
        }

        avg_validationTravelTime =validationTravelTimeSum/linkInfoList.size();
        avg_validationDistance=validationDistanceSum/linkInfoList.size();

        for (Double aDouble : abs_NetworkValidationTravelTimeDifferenceList) {
            abs_NetworkValidationTravelTimeDifferenceSum += aDouble;
        }
        scoreNetworkTravelTime = (abs_NetworkValidationTravelTimeDifferenceSum/abs_NetworkValidationTravelTimeDifferenceList.size())/avg_validationTravelTime;

        for (Double aDouble : abs_NetworkValidationDistanceDifferenceList) {
            abs_NetworkValidationDistanceDifferenceSum += aDouble;
        }
        scoreNetworkDistance =(abs_NetworkValidationDistanceDifferenceSum/abs_NetworkValidationDistanceDifferenceList.size())/avg_validationDistance;

        totalScore =scoreNetworkDistance+scoreNetworkTravelTime;

        return totalScore;
    }
}

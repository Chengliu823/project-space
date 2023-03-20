package org.matsim.project.networkGeneration;

public class SpeedData {

        double freeSpeed_Motorway;
        double freeSpeed_MotorwayLink;

        double freeSpeed_Trunk;
        double freeSpeed_TrunkLink;

        double freeSpeed_Primary;
        double freeSpeed_PrimaryLink;

        double freeSpeed_Secondary;
        double freeSpeed_SecondaryLink;

        double freeSpeed_Tertiary;
        double freeSpeed_TertiaryLink;

        double freeSpeed_Residential;

        double freeSpeed_Unclassified;


        public double getFreeSpeed_Motorway(){
                return freeSpeed_Motorway;
        }


        public void setFreeSpeed_Motorway(double freeSpeed_Motorway){
                this.freeSpeed_Motorway=freeSpeed_Motorway;
        }

        public double getFreeSpeed_MotorwayLink() {
                return freeSpeed_MotorwayLink;
        }

        public void setFreeSpeed_MotorwayLink(double freeSpeed_MotorwayLink){
                this.freeSpeed_MotorwayLink =freeSpeed_MotorwayLink;
        }

        public double getFreeSpeed_Trunk(){
                return freeSpeed_Trunk;
        }

        public void setFreeSpeed_Trunk(double freeSpeed_Trunk){
                this.freeSpeed_Trunk=freeSpeed_Trunk;
        }

        public double getFreeSpeed_TrunkLink() {
                return freeSpeed_TrunkLink;
        }

        public void setFreeSpeed_TrunkLink(double freeSpeed_TrunkLink) {
                this.freeSpeed_TrunkLink = freeSpeed_TrunkLink;
        }

        public double getFreeSpeed_Primary() {
                return freeSpeed_Primary;
        }

        public void setFreeSpeed_Primary(double freeSpeed_Primary) {
                this.freeSpeed_Primary = freeSpeed_Primary;
        }

        public double getFreeSpeed_PrimaryLink() {
                return freeSpeed_PrimaryLink;
        }

        public void setFreeSpeed_PrimaryLink(double freeSpeed_PrimaryLink) {
                this.freeSpeed_PrimaryLink = freeSpeed_PrimaryLink;
        }

        public double getFreeSpeed_Secondary() {
                return freeSpeed_Secondary;
        }

        public void setFreeSpeed_Secondary(double freeSpeed_Secondary) {
                this.freeSpeed_Secondary = freeSpeed_Secondary;
        }

        public double getFreeSpeed_SecondaryLink() {
                return freeSpeed_SecondaryLink;
        }

        public void setFreeSpeed_SecondaryLink(double freeSpeed_SecondaryLink) {
                this.freeSpeed_SecondaryLink = freeSpeed_SecondaryLink;
        }

        public double getFreeSpeed_Tertiary() {
                return freeSpeed_Tertiary;
        }

        public void setFreeSpeed_Tertiary(double freeSpeed_Tertiary) {
                this.freeSpeed_Tertiary = freeSpeed_Tertiary;
        }

        public double getFreeSpeed_TertiaryLink() {
                return freeSpeed_TertiaryLink;
        }

        public void setFreeSpeed_TertiaryLink(double freeSpeed_TertiaryLink) {
                this.freeSpeed_TertiaryLink = freeSpeed_TertiaryLink;
        }

        public double getFreeSpeed_Residential() {
                return freeSpeed_Residential;
        }

        public void setFreeSpeed_Residential(double freeSpeed_Residential) {
                this.freeSpeed_Residential = freeSpeed_Residential;
        }

        public double getFreeSpeed_Unclassified() {
                return freeSpeed_Unclassified;
        }

        public void setFreeSpeed_Unclassified(double freeSpeed_Unclassified) {
                this.freeSpeed_Unclassified = freeSpeed_Unclassified;
        }


        public void speedGroup(int i) {
        }
}


/*
                    for (int i =0; i<networkTravelTimeList.size(); i++){
                        double nts= networkTravelTimeList.get(i);
                        networkTravelTimeSum = networkTravelTimeSum + nts;
                    }

                    for (int i =0; i<validatedTravelTimeList.size(); i++){
                        double vts= validatedTravelTimeList.get(i);
                        validatedTravelTimeSum = validatedTravelTimeSum + vts;
                    }

                    for (int i =0; i<networkDistanceList.size(); i++){
                        double nds= networkDistanceList.get(i);
                        networkDistanceSum = networkDistanceSum + nds;
                    }

                    for (int i =0; i<validatedDistanceList.size(); i++){
                        double vds= validatedDistanceList.get(i);
                        validatedDistanceSum = validatedDistanceSum + vds;
                    }


                    //System.out.println("NtworkTravelTimeList"+networkTravelTimeList);
                    //System.out.println("NetworkTravelTimeSize="+ validated);
                    //System.out.println("validatedTravelTimelist"+validatedTravelTimeList);



                    //计算平均数
                    double adv_networkTravelTimeSum = networkTravelTimeSum / (double) networkTravelTimeList.size();
                    double adv_validatedTravelTimeSum = validatedTravelTimeSum / (double) validatedTravelTimeList.size();
                    double adv_networkDistanceSum = networkDistanceSum / (double) networkDistanceList.size();
                    double adv_validatedDistanceSum = validatedDistanceSum /(double) validatedDistanceList.size();
                    double abs_adv_nts_vts = Math.abs(adv_networkTravelTimeSum - adv_validatedTravelTimeSum);
                    double abs_adv_nds_vds =Math.abs(adv_networkDistanceSum-adv_validatedDistanceSum);

                    double scoreTs = 1-abs_adv_nts_vts/adv_validatedTravelTimeSum;
                    //System.out.println("ScoreTs ="+scoreTs);
                    double scoreDs = 1-abs_adv_nds_vds/adv_validatedDistanceSum;
                    //System.out.println("ScoreDs ="+scoreDs);
                    scoreAdv=scoreTs+scoreDs;


                    //计算比例
                    networkTravelTimeScale = Math.abs(networkTravelTime) / Math.abs(validatedTravelTime);
                    networkTravelTimeScaleList.add(networkTravelTimeScale);
                    networkDistanceScale = Math.abs(networkDistance) / Math.abs(validatedDistance);
                    networkDistanceScaleList.add(networkDistanceScale);


                    for (int i = 0; i < networkTravelTimeScaleList.size(); i++) {
                        double ntss = networkTravelTimeScaleList.get(i);
                        networkTravelTimeScaleSum = networkTravelTimeScaleSum + ntss;
                    }
                    adv_networkTravelTimeScaleSum = networkTravelTimeScaleSum / networkTravelTimeScaleList.size();

                    for (int i = 0; i < networkDistanceScaleList.size(); i++) {
                        double ndss = networkDistanceScaleList.get(i);
                        networkDistanceScaleSum = networkDistanceScaleSum + ndss;
                    }
                    adv_networkDistanceScaleSum = networkDistanceScaleSum / networkDistanceScaleList.size();
                    */
package org.matsim.project.networkGeneration.createnetworkfromosmdata;

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

        public SpeedData() {
        }

        public SpeedData(double freeSpeed_Motorway, double freeSpeed_MotorwayLink, double freeSpeed_Trunk, double freeSpeed_TrunkLink, double freeSpeed_Primary, double freeSpeed_PrimaryLink, double freeSpeed_Secondary, double freeSpeed_SecondaryLink, double freeSpeed_Tertiary, double freeSpeed_TertiaryLink, double freeSpeed_Residential, double freeSpeed_Unclassified) {
                this.freeSpeed_Motorway = freeSpeed_Motorway;
                this.freeSpeed_MotorwayLink = freeSpeed_MotorwayLink;
                this.freeSpeed_Trunk = freeSpeed_Trunk;
                this.freeSpeed_TrunkLink = freeSpeed_TrunkLink;
                this.freeSpeed_Primary = freeSpeed_Primary;
                this.freeSpeed_PrimaryLink = freeSpeed_PrimaryLink;
                this.freeSpeed_Secondary = freeSpeed_Secondary;
                this.freeSpeed_SecondaryLink = freeSpeed_SecondaryLink;
                this.freeSpeed_Tertiary = freeSpeed_Tertiary;
                this.freeSpeed_TertiaryLink = freeSpeed_TertiaryLink;
                this.freeSpeed_Residential = freeSpeed_Residential;
                this.freeSpeed_Unclassified = freeSpeed_Unclassified;
        }

        public double getFreeSpeed_Motorway() {
                return freeSpeed_Motorway;
        }

        public void setFreeSpeed_Motorway(double freeSpeed_Motorway) {
                this.freeSpeed_Motorway = freeSpeed_Motorway;
        }

        public double getFreeSpeed_MotorwayLink() {
                return freeSpeed_MotorwayLink;
        }

        public void setFreeSpeed_MotorwayLink(double freeSpeed_MotorwayLink) {
                this.freeSpeed_MotorwayLink = freeSpeed_MotorwayLink;
        }

        public double getFreeSpeed_Trunk() {
                return freeSpeed_Trunk;
        }

        public void setFreeSpeed_Trunk(double freeSpeed_Trunk) {
                this.freeSpeed_Trunk = freeSpeed_Trunk;
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

        public void choiceSpeedGroup(int speedGroup){
                switch (speedGroup){
                        case 1:
                                setFreeSpeed_Motorway(108/3.6);
                                setFreeSpeed_MotorwayLink(90/3.6);
                                setFreeSpeed_Trunk(90/3.6);
                                setFreeSpeed_TrunkLink(90/3.6);
                                setFreeSpeed_Primary(72/3.6);
                                setFreeSpeed_PrimaryLink(72/3.6);
                                setFreeSpeed_Secondary(54/3.6);
                                setFreeSpeed_SecondaryLink(54/3.6);
                                setFreeSpeed_Tertiary(54/3.6);
                                setFreeSpeed_TertiaryLink(54/3.6);
                                setFreeSpeed_Residential(27/3.6);
                                setFreeSpeed_Unclassified(27/3.6);
                                break;

                        case 2:
                                setFreeSpeed_Motorway(114/3.6);
                                setFreeSpeed_MotorwayLink(95/3.6);
                                setFreeSpeed_Trunk(95/3.6);
                                setFreeSpeed_TrunkLink(95/3.6);
                                setFreeSpeed_Primary(76/3.6);
                                setFreeSpeed_PrimaryLink(76/3.6);
                                setFreeSpeed_Secondary(57/3.6);
                                setFreeSpeed_SecondaryLink(57/3.6);
                                setFreeSpeed_Tertiary(57/3.6);
                                setFreeSpeed_TertiaryLink(57/3.6);
                                setFreeSpeed_Residential(28.5/3.6);
                                setFreeSpeed_Unclassified(28.5/3.6);
                                break;

                        case 3:
                        default:
                                setFreeSpeed_Motorway(120/3.6);
                                setFreeSpeed_MotorwayLink(100/3.6);
                                setFreeSpeed_Trunk(100/3.6);
                                setFreeSpeed_TrunkLink(100/3.6);
                                setFreeSpeed_Primary(80/3.6);
                                setFreeSpeed_PrimaryLink(80/3.6);
                                setFreeSpeed_Secondary(60/3.6);
                                setFreeSpeed_SecondaryLink(60/3.6);
                                setFreeSpeed_Tertiary(60/3.6);
                                setFreeSpeed_TertiaryLink(60/3.6);
                                setFreeSpeed_Residential(30/3.6);
                                setFreeSpeed_Unclassified(30/3.6);
                                break;

                        case 4:
                                setFreeSpeed_Motorway(126/3.6);
                                setFreeSpeed_MotorwayLink(105/3.6);
                                setFreeSpeed_Trunk(105/3.6);
                                setFreeSpeed_TrunkLink(105/3.6);
                                setFreeSpeed_Primary(84/3.6);
                                setFreeSpeed_PrimaryLink(84/3.6);
                                setFreeSpeed_Secondary(63/3.6);
                                setFreeSpeed_SecondaryLink(63/3.6);
                                setFreeSpeed_Tertiary(63/3.6);
                                setFreeSpeed_TertiaryLink(63/3.6);
                                setFreeSpeed_Residential(31.5/3.6);
                                setFreeSpeed_Unclassified(31.5/3.6);
                                break;

                        case 5:
                                setFreeSpeed_Motorway(132/3.6);
                                setFreeSpeed_MotorwayLink(110/3.6);
                                setFreeSpeed_Trunk(110/3.6);
                                setFreeSpeed_TrunkLink(110/3.6);
                                setFreeSpeed_Primary(88/3.6);
                                setFreeSpeed_PrimaryLink(88/3.6);
                                setFreeSpeed_Secondary(66/3.6);
                                setFreeSpeed_SecondaryLink(66/3.6);
                                setFreeSpeed_Tertiary(66/3.6);
                                setFreeSpeed_TertiaryLink(66/3.6);
                                setFreeSpeed_Residential(33/3.6);
                                setFreeSpeed_Unclassified(33/3.6);
                                break;

                        case 6:
                                setFreeSpeed_Motorway(125/3.6);
                                setFreeSpeed_MotorwayLink(104/3.6);
                                setFreeSpeed_Trunk(104/3.6);
                                setFreeSpeed_TrunkLink(104/3.6);
                                setFreeSpeed_Primary(80/3.6);
                                setFreeSpeed_PrimaryLink(80/3.6);
                                setFreeSpeed_Secondary(60/3.6);
                                setFreeSpeed_SecondaryLink(60/3.6);
                                setFreeSpeed_Tertiary(60/3.6);
                                setFreeSpeed_TertiaryLink(60/3.6);
                                setFreeSpeed_Residential(30/3.6);
                                setFreeSpeed_Unclassified(30/3.6);
                                break;

                        case 7:
                                setFreeSpeed_Motorway(105/3.6);
                                setFreeSpeed_MotorwayLink(88/3.6);
                                setFreeSpeed_Trunk(88/3.6);
                                setFreeSpeed_TrunkLink(88/3.6);
                                setFreeSpeed_Primary(70/3.6);
                                setFreeSpeed_PrimaryLink(70/3.6);
                                setFreeSpeed_Secondary(52/3.6);
                                setFreeSpeed_SecondaryLink(52/3.6);
                                setFreeSpeed_Tertiary(52/3.6);
                                setFreeSpeed_TertiaryLink(52/3.6);
                                setFreeSpeed_Residential(27/3.6);
                                setFreeSpeed_Unclassified(27/3.6);
                                break;

                        case 8:
                                setFreeSpeed_Motorway(63/3.6);
                                setFreeSpeed_MotorwayLink(63/3.6);
                                setFreeSpeed_Trunk(58/3.6);
                                setFreeSpeed_TrunkLink(58/3.6);
                                setFreeSpeed_Primary(15/3.6);
                                setFreeSpeed_PrimaryLink(15/3.6);
                                setFreeSpeed_Secondary(45/3.6);
                                setFreeSpeed_SecondaryLink(45/3.6);
                                setFreeSpeed_Tertiary(50/3.6);
                                setFreeSpeed_TertiaryLink(50/3.6);
                                setFreeSpeed_Residential(35/3.6);
                                setFreeSpeed_Unclassified(5/3.6);
                                break;
                }
        }
}

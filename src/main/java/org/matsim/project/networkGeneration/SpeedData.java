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
                                freeSpeed_Motorway =108/3.6;
                                freeSpeed_MotorwayLink =90/3.6;
                                freeSpeed_Trunk =90/3.6;
                                freeSpeed_TrunkLink =90/3.6;
                                freeSpeed_Primary =72/3.6;
                                freeSpeed_PrimaryLink =72/3.6;
                                freeSpeed_Secondary =54/3.6;
                                freeSpeed_SecondaryLink=54/3.6;
                                freeSpeed_Tertiary =54/3.6;
                                freeSpeed_TertiaryLink =54/3.6;
                                freeSpeed_Residential =27/3.6;
                                freeSpeed_Unclassified =27/3.6;
                                break;

                        case 2:
                                freeSpeed_Motorway =(114/3.6);
                                freeSpeed_MotorwayLink =(95/3.6);
                                freeSpeed_Trunk =(95/3.6);
                                freeSpeed_TrunkLink =(95/3.6);
                                freeSpeed_Primary =(76/3.6);
                                freeSpeed_PrimaryLink =(76/3.6);
                                freeSpeed_Secondary =(57/3.6);
                                freeSpeed_SecondaryLink=(57/3.6);
                                freeSpeed_Tertiary =(57/3.6);
                                freeSpeed_TertiaryLink =(57/3.6);
                                freeSpeed_Residential =(28.5/3.6);
                                freeSpeed_Unclassified =(28.5/3.6);
                                break;

                        case 3:
                        default:
                                freeSpeed_Motorway =(120/3.6);
                                freeSpeed_MotorwayLink =(100/3.6);
                                freeSpeed_Trunk =(100/3.6);
                                freeSpeed_TrunkLink =(100/3.6);
                                freeSpeed_Primary =(80/3.6);
                                freeSpeed_PrimaryLink =(80/3.6);
                                freeSpeed_Secondary =(60/3.6);
                                freeSpeed_SecondaryLink=(60/3.6);
                                freeSpeed_Tertiary =(60/3.6);
                                freeSpeed_TertiaryLink =(60/3.6);
                                freeSpeed_Residential =(30/3.6);
                                freeSpeed_Unclassified =(30/3.6);
                                break;

                        case 4:
                                freeSpeed_Motorway =(126/3.6);
                                freeSpeed_MotorwayLink =(105/3.6);
                                freeSpeed_Trunk =(105/3.6);
                                freeSpeed_TrunkLink =(105/3.6);
                                freeSpeed_Primary =(84/3.6);
                                freeSpeed_PrimaryLink =(84/3.6);
                                freeSpeed_Secondary =(63/3.6);
                                freeSpeed_SecondaryLink=(63/3.6);
                                freeSpeed_Tertiary =(63/3.6);
                                freeSpeed_TertiaryLink =(63/3.6);
                                freeSpeed_Residential =(31.5/3.6);
                                freeSpeed_Unclassified =(31.5/3.6);
                                break;

                        case 5:
                                freeSpeed_Motorway =(132/3.6);
                                freeSpeed_MotorwayLink =(110/3.6);
                                freeSpeed_Trunk =(110/3.6);
                                freeSpeed_TrunkLink =(110/3.6);
                                freeSpeed_Primary =(88/3.6);
                                freeSpeed_PrimaryLink =(88/3.6);
                                freeSpeed_Secondary =(66/3.6);
                                freeSpeed_SecondaryLink=(66/3.6);
                                freeSpeed_Tertiary =(66/3.6);
                                freeSpeed_TertiaryLink =(66/3.6);
                                freeSpeed_Residential =(33/3.6);
                                freeSpeed_Unclassified =(33/3.6);
                                break;

                        case 6:
                                freeSpeed_Motorway =(125/3.6);
                                freeSpeed_MotorwayLink =(104/3.6);
                                freeSpeed_Trunk =(104/3.6);
                                freeSpeed_TrunkLink =(104/3.6);
                                freeSpeed_Primary =(80/3.6);
                                freeSpeed_PrimaryLink =(80/3.6);
                                freeSpeed_Secondary =(60/3.6);
                                freeSpeed_SecondaryLink=(60/3.6);
                                freeSpeed_Tertiary =(60/3.6);
                                freeSpeed_TertiaryLink =(60/3.6);
                                freeSpeed_Residential =(30/3.6);
                                freeSpeed_Unclassified =(30/3.6);
                                break;

                        case 7:
                                freeSpeed_Motorway =(105/3.6);
                                freeSpeed_MotorwayLink =(88/3.6);
                                freeSpeed_Trunk =(88/3.6);
                                freeSpeed_TrunkLink =(88/3.6);
                                freeSpeed_Primary =(70/3.6);
                                freeSpeed_PrimaryLink =(70/3.6);
                                freeSpeed_Secondary =(52/3.6);
                                freeSpeed_SecondaryLink=(52/3.6);
                                freeSpeed_Tertiary =(52/3.6);
                                freeSpeed_TertiaryLink =(52/3.6);
                                freeSpeed_Residential =(27/3.6);
                                freeSpeed_Unclassified =(27/3.6);
                                break;

                        case 8:
                                freeSpeed_Motorway =(100/3.6);
                                freeSpeed_MotorwayLink =(82/3.6);
                                freeSpeed_Trunk =(82/3.6);
                                freeSpeed_TrunkLink =(82/3.6);
                                freeSpeed_Primary =(65/3.6);
                                freeSpeed_PrimaryLink =(65/3.6);
                                freeSpeed_Secondary =(50/3.6);
                                freeSpeed_SecondaryLink=(50/3.6);
                                freeSpeed_Tertiary =(50/3.6);
                                freeSpeed_TertiaryLink =(50/3.6);
                                freeSpeed_Residential =(25/3.6);
                                freeSpeed_Unclassified =(25/3.6);
                                break;
                }
        }
}

package org.matsim.project.networkGeneration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.application.MATSimAppCommand;
import org.matsim.application.options.CrsOptions;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.matsim.contrib.osm.networkReader.OsmTags;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.MultimodalNetworkCleaner;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateNetworkFromOsmData implements MATSimAppCommand {
    private static final Logger log = LogManager.getLogger(CreateNetworkFromOsmData.class);

    @CommandLine.Option(names = "--input", description = "input pbf file", required = true)
    private Path input;

    @CommandLine.Option(names = "--urban-area", description = "Path to urban area shape file", defaultValue = "")
    private String urbanAreaPath;

    @CommandLine.Option(names = "--output", description = "output network file", required = true)
    private Path output;

    @CommandLine.Option(names = "--Speed-group", description = "select for the Speed Group", defaultValue = "1")
    private int SpeedGroup;

    @CommandLine.Mixin
    private CrsOptions crs = new CrsOptions();
    // Input CRS: WGS84 (EPSG:4326). Target CRS: EPSG:25832

    public static void main(String[] args) {
        new CreateNetworkFromOsmData().execute(args);
    }

    // private static final String OUTPUT_NETWORK = "./senario/vulkaneifel-v1.1network.xml.gz";

    @Override
    public Integer call() throws Exception {
        SpeedData speedData = new SpeedData();
        ChoiceSpeedGroup(SpeedGroup);
        Network network = new SupersonicOsmNetworkReader.Builder()
                .addOverridingLinkProperties(OsmTags.MOTORWAY, new LinkProperties(1, 2, speedData.getFreeSpeed_Motorway(), 3600, true))
                .addOverridingLinkProperties(OsmTags.MOTORWAY_LINK, new LinkProperties(1, 1, speedData.getFreeSpeed_MotorwayLink(), 3600, true))
                .addOverridingLinkProperties(OsmTags.TRUNK, new LinkProperties(1, 1, speedData.getFreeSpeed_Trunk(), 3600, false))
                .addOverridingLinkProperties(OsmTags.TRUNK_LINK, new LinkProperties(1, 1, speedData.getFreeSpeed_TrunkLink(), 3600, false))
                .addOverridingLinkProperties(OsmTags.PRIMARY, new LinkProperties(1, 1, speedData.getFreeSpeed_Primary(), 1800, false))
                .addOverridingLinkProperties(OsmTags.PRIMARY_LINK, new LinkProperties(1, 1, speedData.getFreeSpeed_PrimaryLink(), 1800, false))
                .addOverridingLinkProperties(OsmTags.SECONDARY, new LinkProperties(1, 1, speedData.getFreeSpeed_Secondary(), 1200, false))
                .addOverridingLinkProperties(OsmTags.SECONDARY_LINK, new LinkProperties(1, 1, speedData.getFreeSpeed_SecondaryLink(), 1200, false))
                .addOverridingLinkProperties(OsmTags.TERTIARY, new LinkProperties(1, 1, speedData.getFreeSpeed_Tertiary(), 900, false))
                .addOverridingLinkProperties(OsmTags.TERTIARY_LINK, new LinkProperties(1, 1,speedData.getFreeSpeed_TertiaryLink(), 900, false))
                .addOverridingLinkProperties(OsmTags.RESIDENTIAL, new LinkProperties(1, 1, speedData.getFreeSpeed_Residential(), 600, false))
                .addOverridingLinkProperties(OsmTags.UNCLASSIFIED, new LinkProperties(1, 1, speedData.getFreeSpeed_Unclassified(), 600, false))
                .setCoordinateTransformation(crs.getTransformation())
                .setPreserveNodeWithId(id -> id == 2)
                .setAfterLinkCreated((link, osmTags, isReverse) -> link.setAllowedModes(new HashSet<>(List.of(TransportMode.car))))
                .build()
                .read(input.toString());

        var cleaner = new MultimodalNetworkCleaner(network);
        cleaner.run(Set.of(TransportMode.car));

        log.info("Modifying network speed for urban area...");
        log.info("Loading urban area geometry file");
        List<Geometry> urbanAreaGeometries = new ArrayList<>();
        if (!urbanAreaPath.equals("")) {
            for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urbanAreaPath)) {
                Geometry subArea = (Geometry) feature.getDefaultGeometry();
                if (subArea.isValid()) {
                    urbanAreaGeometries.add(subArea);
                }
            }
        }

        log.info("There are " + network.getLinks().size() + " links to be processed");
        int counter = 0;
        for (Link link : network.getLinks().values()) {
            Point from = MGC.coord2Point(link.getFromNode().getCoord());
            Point to = MGC.coord2Point(link.getToNode().getCoord());
            Point center = MGC.coord2Point(link.getCoord());

            for (Geometry geometry : urbanAreaGeometries) {
                if (from.within(geometry) || to.within(geometry) || center.within(geometry)) {
                    if (link.getAttributes().getAttribute(NetworkUtils.TYPE).equals(OsmTags.RESIDENTIAL)) {
                        double freeSpeed = 18 / 3.6;
                        link.setFreespeed(freeSpeed);
                    } else if (!link.getAttributes().getAttribute(NetworkUtils.TYPE).equals(OsmTags.MOTORWAY)
                            && !link.getAttributes().getAttribute(NetworkUtils.TYPE).equals(OsmTags.MOTORWAY_LINK)
                            && !link.getAttributes().getAttribute(NetworkUtils.TYPE).equals(OsmTags.TRUNK)
                            && !link.getAttributes().getAttribute(NetworkUtils.TYPE).equals(OsmTags.TRUNK_LINK)) {
                        double freeSpeed = 45 / 3.6;
                        link.setFreespeed(freeSpeed);
                    }
                    break;
                }
            }
            counter++;
            if (counter % 10000 == 0) {
                log.info("Processing: " + counter + "completed");
            }
        }

        new NetworkWriter(network).write(output.toString());
        return 0;
    }

    public static void ChoiceSpeedGroup(int speedGroup){
        SpeedData speedData = new SpeedData();
        switch (speedGroup){
            case 1:
                speedData.setFreeSpeed_Motorway(108/3.6);
                speedData.setFreeSpeed_MotorwayLink(90/3.6);
                speedData.setFreeSpeed_Trunk(90/3.6);
                speedData.setFreeSpeed_TrunkLink(90/3.6);
                speedData.setFreeSpeed_Primary(72/3.6);
                speedData.setFreeSpeed_PrimaryLink(72/3.6);
                speedData.setFreeSpeed_Secondary(54/3.6);
                speedData.setFreeSpeed_SecondaryLink(54/3.6);
                speedData.setFreeSpeed_Tertiary(54/3.6);
                speedData.setFreeSpeed_TertiaryLink(54/3.6);
                speedData.setFreeSpeed_Residential(27/3.6);
                speedData.setFreeSpeed_Unclassified(27/3.6);
                break;

            case 2:
                speedData.setFreeSpeed_Motorway(114/3.6);
                speedData.setFreeSpeed_MotorwayLink(95/3.6);
                speedData.setFreeSpeed_Trunk(95/3.6);
                speedData.setFreeSpeed_TrunkLink(95/3.6);
                speedData.setFreeSpeed_Primary(76/3.6);
                speedData.setFreeSpeed_PrimaryLink(76/3.6);
                speedData.setFreeSpeed_Secondary(57/3.6);
                speedData.setFreeSpeed_SecondaryLink(57/3.6);
                speedData.setFreeSpeed_Tertiary(57/3.6);
                speedData.setFreeSpeed_TertiaryLink(57/3.6);
                speedData.setFreeSpeed_Residential(28.5/3.6);
                speedData.setFreeSpeed_Unclassified(28.5/3.6);
                break;

            case 3:
            default:
                speedData.setFreeSpeed_Motorway(120/3.6);
                speedData.setFreeSpeed_MotorwayLink(100/3.6);
                speedData.setFreeSpeed_Trunk(100/3.6);
                speedData.setFreeSpeed_TrunkLink(100/3.6);
                speedData.setFreeSpeed_Primary(80/3.6);
                speedData.setFreeSpeed_PrimaryLink(80/3.6);
                speedData.setFreeSpeed_Secondary(60/3.6);
                speedData.setFreeSpeed_SecondaryLink(60/3.6);
                speedData.setFreeSpeed_Tertiary(60/3.6);
                speedData.setFreeSpeed_TertiaryLink(60/3.6);
                speedData.setFreeSpeed_Residential(30/3.6);
                speedData.setFreeSpeed_Unclassified(30/3.6);
                break;

            case 4:
                speedData.setFreeSpeed_Motorway(126/3.6);
                speedData.setFreeSpeed_MotorwayLink(105/3.6);
                speedData.setFreeSpeed_Trunk(105/3.6);
                speedData.setFreeSpeed_TrunkLink(105/3.6);
                speedData.setFreeSpeed_Primary(84/3.6);
                speedData.setFreeSpeed_PrimaryLink(84/3.6);
                speedData.setFreeSpeed_Secondary(63/3.6);
                speedData.setFreeSpeed_SecondaryLink(63/3.6);
                speedData.setFreeSpeed_Tertiary(63/3.6);
                speedData.setFreeSpeed_TertiaryLink(63/3.6);
                speedData.setFreeSpeed_Residential(31.5/3.6);
                speedData.setFreeSpeed_Unclassified(31.5/3.6);
                break;

            case 5:
                speedData.setFreeSpeed_Motorway(132/3.6);
                speedData.setFreeSpeed_MotorwayLink(110/3.6);
                speedData.setFreeSpeed_Trunk(110/3.6);
                speedData.setFreeSpeed_TrunkLink(110/3.6);
                speedData.setFreeSpeed_Primary(88/3.6);
                speedData.setFreeSpeed_PrimaryLink(88/3.6);
                speedData.setFreeSpeed_Secondary(66/3.6);
                speedData.setFreeSpeed_SecondaryLink(66/3.6);
                speedData.setFreeSpeed_Tertiary(66/3.6);
                speedData.setFreeSpeed_TertiaryLink(66/3.6);
                speedData.setFreeSpeed_Residential(33/3.6);
                speedData.setFreeSpeed_Unclassified(33/3.6);
                break;

            case 6:
                speedData.setFreeSpeed_Motorway(125/3.6);
                speedData.setFreeSpeed_MotorwayLink(104/3.6);
                speedData.setFreeSpeed_Trunk(104/3.6);
                speedData.setFreeSpeed_TrunkLink(104/3.6);
                speedData.setFreeSpeed_Primary(80/3.6);
                speedData.setFreeSpeed_PrimaryLink(80/3.6);
                speedData.setFreeSpeed_Secondary(60/3.6);
                speedData.setFreeSpeed_SecondaryLink(60/3.6);
                speedData.setFreeSpeed_Tertiary(60/3.6);
                speedData.setFreeSpeed_TertiaryLink(60/3.6);
                speedData.setFreeSpeed_Residential(30/3.6);
                speedData.setFreeSpeed_Unclassified(30/3.6);
                break;

            case 7:
                speedData.setFreeSpeed_Motorway(105/3.6);
                speedData.setFreeSpeed_MotorwayLink(88/3.6);
                speedData.setFreeSpeed_Trunk(88/3.6);
                speedData.setFreeSpeed_TrunkLink(88/3.6);
                speedData.setFreeSpeed_Primary(70/3.6);
                speedData.setFreeSpeed_PrimaryLink(70/3.6);
                speedData.setFreeSpeed_Secondary(52/3.6);
                speedData.setFreeSpeed_SecondaryLink(52/3.6);
                speedData.setFreeSpeed_Tertiary(52/3.6);
                speedData.setFreeSpeed_TertiaryLink(52/3.6);
                speedData.setFreeSpeed_Residential(27/3.6);
                speedData.setFreeSpeed_Unclassified(27/3.6);
                break;

            case 8:
                speedData.setFreeSpeed_Motorway(100/3.6);
                speedData.setFreeSpeed_MotorwayLink(82/3.6);
                speedData.setFreeSpeed_Trunk(82/3.6);
                speedData.setFreeSpeed_TrunkLink(82/3.6);
                speedData.setFreeSpeed_Primary(65/3.6);
                speedData.setFreeSpeed_PrimaryLink(65/3.6);
                speedData.setFreeSpeed_Secondary(50/3.6);
                speedData.setFreeSpeed_SecondaryLink(50/3.6);
                speedData.setFreeSpeed_Tertiary(50/3.6);
                speedData.setFreeSpeed_TertiaryLink(50/3.6);
                speedData.setFreeSpeed_Residential(25/3.6);
                speedData.setFreeSpeed_Unclassified(25/3.6);
                break;
        }
    }
}

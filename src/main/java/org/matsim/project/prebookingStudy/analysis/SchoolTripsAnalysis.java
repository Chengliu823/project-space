package org.matsim.project.prebookingStudy.analysis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math.stat.StatUtils;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.application.MATSimAppCommand;
import org.matsim.contrib.common.util.DistanceUtils;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.QSimFreeSpeedTravelTime;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.speedy.SpeedyALTFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import picocli.CommandLine;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.matsim.application.ApplicationUtils.globFile;

@CommandLine.Command(
        name = "analyze-drt-service",
        description = "Analyze DRT service quality"
)
public class SchoolTripsAnalysis implements MATSimAppCommand {
    @CommandLine.Option(names = "--directory", description = "path to output directory", required = true)
    private Path directory;

    private final List<String> titleRowKPI = Arrays.asList
            ("fleet_size", "number_of_trips", "arrival_punctuality",
                    "actual_in_vehicle_time_mean", "estimated_direct_in_vehicle_time_mean", "onboard_delay_ratio_mean",
                    "actual_travel_distance_mean", "estimated_direct_network_distance_mean", "detour_distance_ratio_mean",
                    "fleet_total_distance", "fleet_efficiency");
    private final List<String> outputKPIRow = new ArrayList<>();

    public static void main(String[] args) {
        new SchoolTripsAnalysis().execute(args);
    }

    public List<String> getTitleRowKPI() {
        return titleRowKPI;
    }

    public List<String> getOutputKPIRow() {
        return outputKPIRow;
    }

    public void analyze(Path directory) throws IOException {
        outputKPIRow.clear();

        Path configPath = globFile(directory, "*output_config.*");
        Path networkPath = globFile(directory, "*output_network.*");
        Path eventPath = globFile(directory, "*output_events.*");
        Path outputFolder = Path.of(directory + "/analysis-drt-service-quality");

        if (!Files.exists(outputFolder)) {
            Files.createDirectory(outputFolder);
        }

        Config config = ConfigUtils.loadConfig(configPath.toString());
        int lastIteration = config.controler().getLastIteration();
        String runId = config.controler().getRunId();
        Path folderOfLastIteration = Path.of(directory.toString() + "/ITERS/it." + lastIteration);
        MultiModeDrtConfigGroup multiModeDrtConfigGroup = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);
        List<String> modes = new ArrayList<>();
        for (DrtConfigGroup drtCfg : multiModeDrtConfigGroup.getModalElements()) {
            modes.add(drtCfg.getMode());
        }

        Network network = NetworkUtils.readNetwork(networkPath.toString());
//        TravelTime travelTime = TrafficAnalysis.analyzeTravelTimeFromEvents(network, eventPath.toString());
        TravelTime travelTime = new QSimFreeSpeedTravelTime(1);  // Free speed travel time used for this study // TODO update this when traffic congestion is included
        TravelDisutility travelDisutility = new OnlyTimeDependentTravelDisutility(travelTime);
        LeastCostPathCalculator router = new SpeedyALTFactory().
                createPathCalculator(network, travelDisutility, travelTime);

        for (String mode : modes) {
            Path tripsFile = globFile(folderOfLastIteration, "*drt_legs_" + mode + ".*");
            Path distanceStatsFile = globFile(directory, "*drt_vehicle_stats_" + mode + ".*");
            Path outputTripsPath = Path.of(outputFolder + "/" + mode + "_trips.tsv");
            Path outputStatsPath = Path.of(outputFolder + "/" + mode + "_KPI.tsv");

            List<Double> inVehicleTimes = new ArrayList<>();
            List<Double> estimatedDirectInVehicleTimes = new ArrayList<>();
            List<Double> actualTravelDistances = new ArrayList<>();
            List<Double> estimatedDirectTravelDistances = new ArrayList<>();
            List<Double> onboardDelayRatios = new ArrayList<>();
            List<Double> detourDistanceRatios = new ArrayList<>();
            List<Double> arrivalTimes = new ArrayList<>();

            CSVPrinter tsvWriter = new CSVPrinter(new FileWriter(outputTripsPath.toString()), CSVFormat.TDF);
            List<String> titleRow = Arrays.asList
                    ("earliest_boarding_time", "actual_boarding_time", "actual_arrival_time",
                            "actual_in_vehicle_time", "est_direct_in_vehicle_time", "onboard_delay_ratio",
                            "actual_travel_distance", "est_direct_network_distance", "detour_distance_ratio",
                            "from_x", "from_y", "to_x", "to_y", "euclidean_distance");
            tsvWriter.printRecord(titleRow);

            int numOfTrips = 0;
            try (CSVParser parser = new CSVParser(Files.newBufferedReader(tripsFile),
                    CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
                for (CSVRecord record : parser.getRecords()) {
                    Link fromLink = network.getLinks().get(Id.createLinkId(record.get(3)));
                    Coord fromCoord = fromLink.getToNode().getCoord();
                    Link toLink = network.getLinks().get(Id.createLinkId(record.get(6)));
                    Coord toCoord = toLink.getToNode().getCoord();
                    double departureTime = Double.parseDouble(record.get(0));
                    LeastCostPathCalculator.Path path = router.calcLeastCostPath(fromLink.getToNode(), toLink.getToNode(),
                            departureTime, null, null);
                    double estimatedDirectInVehicleTime = path.travelTime;
                    double estimatedDirectTravelDistance = path.links.stream().map(Link::getLength).mapToDouble(l -> l).sum();
                    double waitingTime = Double.parseDouble(record.get(9));
                    double boardingTime = departureTime + waitingTime;
                    double actualInVehicleTime = Double.parseDouble(record.get(11));
                    double totalTravelTime = waitingTime + actualInVehicleTime;
                    double actualTravelDistance = Double.parseDouble(record.get(12));
                    double euclideanDistance = DistanceUtils.calculateDistance(fromCoord, toCoord);
                    double onboardDelayRatio = actualInVehicleTime / estimatedDirectInVehicleTime - 1;
                    double detourRatioDistance = actualTravelDistance / estimatedDirectTravelDistance - 1;
                    double arrivalTime = departureTime + totalTravelTime;

                    arrivalTimes.add(arrivalTime);
                    inVehicleTimes.add(actualInVehicleTime);
                    estimatedDirectInVehicleTimes.add(estimatedDirectInVehicleTime);
                    actualTravelDistances.add(actualTravelDistance);
                    estimatedDirectTravelDistances.add(estimatedDirectTravelDistance);
                    onboardDelayRatios.add(onboardDelayRatio);
                    detourDistanceRatios.add(detourRatioDistance);

                    List<String> outputRow = new ArrayList<>();

                    outputRow.add(Double.toString(departureTime));
                    outputRow.add(Double.toString(boardingTime));
                    outputRow.add(Double.toString(arrivalTime));

                    outputRow.add(Double.toString(actualInVehicleTime));
                    outputRow.add(Double.toString(estimatedDirectInVehicleTime));
                    outputRow.add(Double.toString(onboardDelayRatio));

                    outputRow.add(Double.toString(actualTravelDistance));
                    outputRow.add(Double.toString(estimatedDirectTravelDistance));
                    outputRow.add(Double.toString(detourRatioDistance));

                    outputRow.add(Double.toString(fromCoord.getX()));
                    outputRow.add(Double.toString(fromCoord.getY()));
                    outputRow.add(Double.toString(toCoord.getX()));
                    outputRow.add(Double.toString(toCoord.getY()));
                    outputRow.add(Double.toString(euclideanDistance));

                    tsvWriter.printRecord(outputRow);

                    numOfTrips++;
                }
            }
            tsvWriter.close();

            double totalFleetDistance;
            int fleetSize;
            try (CSVParser parser = new CSVParser(Files.newBufferedReader(distanceStatsFile),
                    CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
                List<CSVRecord> records = parser.getRecords();
                int size = records.size();
                CSVRecord record = records.get(size - 1);
                fleetSize = Integer.parseInt(record.get(2));
                totalFleetDistance = Double.parseDouble(record.get(3));
            }

            CSVPrinter tsvWriterKPI = new CSVPrinter(new FileWriter(outputStatsPath.toString()), CSVFormat.TDF);
            tsvWriterKPI.printRecord(titleRowKPI);

            int inVehicleTimeMean = (int) inVehicleTimes.stream().mapToDouble(t -> t).average().orElse(-1);
            int estDirectInVehicleTimeMean = (int) estimatedDirectInVehicleTimes.stream().mapToDouble(t -> t).average().orElse(-1);

            int actualTravelDistanceMean = (int) actualTravelDistances.stream().mapToDouble(t -> t).average().orElse(-1);
            int estDirectTravelDistanceMean = (int) estimatedDirectTravelDistances.stream().mapToDouble(t -> t).average().orElse(-1);

            DecimalFormat formatter = new DecimalFormat("0.00");
            String meanDelayRatio = formatter.format(onboardDelayRatios.stream().mapToDouble(r -> r).average().orElse(-1));
            String meanDetourDistanceRatio = formatter.format(detourDistanceRatios.stream().mapToDouble(d -> d).average().orElse(-1));
            String fleetEfficiency = formatter.format(totalFleetDistance / estimatedDirectTravelDistances.stream().mapToDouble(d -> d).sum());

            DecimalFormat formatter2 = new DecimalFormat("0.000");
            String arrivalPunctuality = formatter2.format(arrivalTimes.stream().filter(a -> a <= 28800).count() / (double) numOfTrips);
            //TODO Currently 28800 (08:00) is used as the single starting time of school

            outputKPIRow.add(Integer.toString(fleetSize));
            outputKPIRow.add(Integer.toString(numOfTrips));
            outputKPIRow.add(arrivalPunctuality);

            outputKPIRow.add(Integer.toString(inVehicleTimeMean));
            outputKPIRow.add(Integer.toString(estDirectInVehicleTimeMean));
            outputKPIRow.add(meanDelayRatio);

            outputKPIRow.add(Integer.toString(actualTravelDistanceMean));
            outputKPIRow.add(Integer.toString(estDirectTravelDistanceMean));
            outputKPIRow.add(meanDetourDistanceRatio);

            outputKPIRow.add(Double.toString(totalFleetDistance));
            outputKPIRow.add(fleetEfficiency);  // Total fleet distance / sum est direct travel distance   (if > 1 --> better than private cars)

            tsvWriterKPI.printRecord(outputKPIRow);

            tsvWriterKPI.close();
        }
    }

    @Override
    public Integer call() throws Exception {
        analyze(directory);
        return 0;
    }
}
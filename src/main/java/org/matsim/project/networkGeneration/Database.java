package org.matsim.project.networkGeneration;

import com.beust.jcommander.IValueValidator;
import org.jaitools.numeric.Statistic;
import org.junit.jupiter.api.Test;
import org.matsim.api.core.v01.network.Link;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Database {
    List<Double> TripInfoList = new ArrayList<>();

    public static void main(String[] args) {
        Database database= new Database();
        database.createTripInfoTable();
        database.Insert("T1",5.4,5.7);
        database.Insert("T2",5.5,5.7);
        database.Insert("T3",5.4,5.7);
        //database.deleteTripInfoTable();
        database.selectTripInfoTable("T2");


    }



    public Connection connection(){
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db";

        Connection connection =null;
        try{
            connection =DriverManager.getConnection(url);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public void createTripInfoTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db";

        // SQL statement for creating a new table
        String sqlCreate = "CREATE TABLE IF NOT EXISTS TripInfo (\n"
                + "	TripId String,\n"
                + "	ValidationTravelTime real,\n"
                + "	ValidationDistance real\n"
                + ");";

        try (Connection connection = this.connection();
             Statement statement = connection.createStatement()) {
            // create a new table
            statement.execute(sqlCreate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteTripInfoTable(){
        String sqlDelete="DELETE FROM TripInfo";

        try (Connection connection = this.connection();
             Statement statement = connection.createStatement()) {
            // create a new table
            statement.execute(sqlDelete);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void selectTripInfoTable(String TripId){
        String sqlSelectTripInfoTable ="SELECT ValidationTravelTime FROM TripInfo WHERE TripId=?";


        try (Connection connection=this.connection();
             PreparedStatement preparedStatement =connection.prepareStatement(sqlSelectTripInfoTable))
        {
            preparedStatement.setString(1,TripId);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                TripInfoList.add(resultSet.getDouble("ValidationTravelTime"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    public void Insert(String TriPId, Double ValidationTravelTime, Double ValidationDistance){
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db";

        String sqlInsert ="INSERT INTO TripInfo(TripId,ValidationTravelTime,ValidationDistance)VALUES(?,?,?)";

        try (Connection connection=this.connection();
             PreparedStatement preparedStatement =connection.prepareStatement(sqlInsert))
        {
            preparedStatement.setString(1,TriPId);
            preparedStatement.setDouble(2,ValidationTravelTime);
            preparedStatement.setDouble(3,ValidationDistance);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}

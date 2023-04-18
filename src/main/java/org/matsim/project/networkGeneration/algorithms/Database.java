package org.matsim.project.networkGeneration.algorithms;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Database {


    public static void main(String[] args) {
        Database database= new Database();
        database.dropTripInfoTable();
        database.createTripInfoTable();
        TripInfo tripInfo = new TripInfo("A001",21.5,22.5,21,5,8,6,4,8);
        database.Insert(tripInfo);
        //database.deleteTripInfoTable();

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

        // SQL statement for creating a new table
        String sqlCreate = "CREATE TABLE IF NOT EXISTS TripInfo (\n"
                + "	TripId String,\n"
                + "	FromX real ,\n"
                + "	FromY real ,\n"
                + "	ToX real,\n"
                + "	ToY real,\n"
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
    public void dropTripInfoTable(){
        String sqlDelete="DROP TABLE TripInfo";

        try (Connection connection = this.connection();
             Statement statement = connection.createStatement()) {
            statement.execute(sqlDelete);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void deleteTripInfoTable(){
        String sqlDelete="DELETE FROM TripInfo";

        try (Connection connection = this.connection();
             Statement statement = connection.createStatement()) {
            statement.execute(sqlDelete);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void selectTripInfoTable(String TripId){
        String sqlSelectTripInfoTable ="SELECT TripId FROM TripInfo WHERE TripId=?";
        List<Double> TripInfoList = new ArrayList<>();


        try (Connection connection=this.connection();
             PreparedStatement preparedStatement =connection.prepareStatement(sqlSelectTripInfoTable))
        {
            preparedStatement.setString(1,TripId);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                System.out.println(resultSet.getString(1)+resultSet.getDouble(2));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    public void Insert(TripInfo tripInfo){
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db";

        String sqlInsert ="INSERT INTO TripInfo(TripId,FromX,FromY,ToX,ToY,ValidationTravelTime,ValidationDistance)VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection=this.connection();
             PreparedStatement preparedStatement =connection.prepareStatement(sqlInsert))
        {
            preparedStatement.setString(1,tripInfo.getTripId());
            preparedStatement.setDouble(2,tripInfo.getFromX());
            preparedStatement.setDouble(3,tripInfo.getFromY());
            preparedStatement.setDouble(4,tripInfo.getToX());
            preparedStatement.setDouble(5,tripInfo.getToY());
            preparedStatement.setDouble(6,tripInfo.getValidationTravelTime());
            preparedStatement.setDouble(7,tripInfo.getValidationDistance());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}

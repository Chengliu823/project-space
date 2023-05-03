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
        Database database = new Database();
        database.createTripInfoTable();
        List<TripInfo> tripInfoList = infoList();

        for (int i = 0; i < tripInfoList.size(); i++) {
            System.out.println(tripInfoList.get(i).getTripId());
        }
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
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db";

        // SQL statement for creating a new table
        String sqlCreate = "CREATE TABLE IF NOT EXISTS TripInfo (\n"
                + "	TripId String PRIMARY KEY,\n"
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

    /*
        先SELECE COUNT(*) FROM table获取表格一共多少数据，然后写一个循环，循环次数是SELECE COUNT(*) FROM table的查询结果，循环的内容是SELECT id FROM table

        使用语句select id form TripInfo 获取所有已存在id， 然后把所有id存放到一个list中，对list进行遍历，如果list中不存在该id则插入数据，如果存在该id则不插入数据

     */

    public static List<TripInfo> infoList(){
        List<TripInfo> tripInfoList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection =DriverManager.getConnection("jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\TripInfo.db");
            preparedStatement =connection.prepareStatement("SELECT * FROM TripInfo");
            resultSet =preparedStatement.executeQuery();
            while (resultSet.next()){
                String tripId =resultSet.getString(1);
                double fromX = resultSet.getDouble(2);
                double fromY = resultSet.getDouble(3);
                double toX = resultSet.getDouble(4);
                double toY = resultSet.getDouble(5);
                double validationTravelTime = resultSet.getDouble(6);
                double validationDistance = resultSet.getDouble(7);
                TripInfo tripInfo = new TripInfo(tripId,fromX,fromY,toX,toY,0,validationTravelTime,0,validationDistance);
                tripInfoList.add(tripInfo);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tripInfoList;
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

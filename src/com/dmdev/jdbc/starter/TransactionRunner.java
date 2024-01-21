package com.dmdev.jdbc.starter;

import com.dmdev.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionRunner {
    public static void main(String[] args) throws SQLException {
        long fligId = 8;

        String deliteFly = "DELETE FROM flight WHERE id = " + fligId;
        String deliteTice = "DELETE FROM ticket WHERE flight_id = " + fligId;

        Connection connection = null;
        Statement statement =null;



        try {

            connection = ConnectionManager.get();
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.addBatch(deliteTice);
            statement.addBatch(deliteFly);

            int[] ints = statement.executeBatch();

            connection.commit();

        } catch (Exception e){
                if (connection != null) {
                    connection.rollback();
                }
                throw e;
        } finally {
            if (connection != null){
                connection.close();
            }
            if (statement != null){
                statement.close();
            }
        }
    }
}

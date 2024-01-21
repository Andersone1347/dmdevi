package com.dmdev.jdbc.starter;

import com.dmdev.jdbc.starter.util.ConnectionManager;
import org.postgresql.Driver;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {
//        Long flyId = 2L;
//        List<Long> res = getBeeeet(LocalDate.of(2020, 1,1).atStartOfDay(), LocalDateTime.now() );
//        System.out.println(res);
        try {
            checkMetaData();
        } finally {
            ConnectionManager.closePool();
        }
    }

    private static void checkMetaData() throws SQLException {
        try (Connection connection = ConnectionManager.get() ) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet cata = metaData.getCatalogs();
            while (cata.next()){
                String catalog = cata.getString(1);
                System.out.println(catalog);

                ResultSet schemas = metaData.getSchemas();
                while (schemas.next()){
                    String schema = schemas.getString("TABLE_SCHEM");
                    System.out.println(schema);


                    ResultSet table = metaData.getTables(catalog,schema,"%",new String[] {"TABLE"});
                    if (schema.equals("public")) {
                        while (table.next()) {
                            System.out.println(table.getString("TABLE_NAME"));
                            ResultSet col = metaData.getColumns(catalog,schema,null,"%");
                        }
                    }
                }
            }
        }
    }

    private static  List<Long> getBeeeet(LocalDateTime s, LocalDateTime end) throws SQLException {
        String sql = """ 
                select id 
                from flight 
                where departure_date 
                BETWEEN ? and ?
                """;
        List<Long> result = new ArrayList<>();
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            System.out.println(preparedStatement);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(s));
            System.out.println(preparedStatement);
            preparedStatement.setTimestamp(2,Timestamp.valueOf(end));
            System.out.println(preparedStatement);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getLong("id"));
            }
        }

        return result;
    }
    private static List<Long> getiis(Long flyid) throws SQLException {
        String sql = """
               select id from ticket where flight_id = ?
                """;
        List<Long> result = new ArrayList<>();
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1,flyid);



            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                result.add(resultSet.getObject(1, Long.class));
            }
        }
        return result;
    }
}

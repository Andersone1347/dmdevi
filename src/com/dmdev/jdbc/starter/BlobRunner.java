package com.dmdev.jdbc.starter;

import com.dmdev.jdbc.starter.util.ConnectionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;

public class BlobRunner {
    public static void main(String[] args) throws SQLException, IOException {
getImage();
    }

    public static void getImage() throws SQLException, IOException {
        var sql = """
                select image from aircraft where id = ?
                """;
        try (Connection connection = ConnectionManager.get();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setInt(1,1);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                byte[] images = resultSet.getBytes("image");
                Files.write(Path.of("resources","boing.jpg"), images, StandardOpenOption.CREATE);

            }
        }
    }

    private static void saveImage() throws SQLException, IOException {
        var sql = """
                update aircraft SET image = ? WHERE id = 1
                """;
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
       preparedStatement.setBytes(1,Files.readAllBytes(Path.of("resources","rhea-ripley-4k-australian-wrestler-wwe-orange-neon-lights.jpg")));
       preparedStatement.executeUpdate();
        }
    }


//    private static void saveImage() throws SQLException, IOException {
//        var sql = """
//                update aircraft SET image = ? WHERE id = 1
//                """;
//        try (Connection connection = ConnectionManager.open();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql)
//        ) {
//            connection.setAutoCommit(false);
//            Blob blob = connection.createBlob();
//            blob.setBytes(1, Files.readAllBytes(Path.of("resources","rhea-ripley-4k-australian-wrestler-wwe-orange-neon-lights.jpg")));
//            preparedStatement.setBlob(1,blob);
//            preparedStatement.executeUpdate();
//            connection.commit();
//
//        }
//    }
}

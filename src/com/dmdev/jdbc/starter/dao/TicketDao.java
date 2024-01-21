package com.dmdev.jdbc.starter.dao;

import com.dmdev.jdbc.starter.dto.TicketFilter;
import com.dmdev.jdbc.starter.entity.Flight;
import com.dmdev.jdbc.starter.entity.Ticket;
import com.dmdev.jdbc.starter.exception.DaoException;
import com.dmdev.jdbc.starter.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class TicketDao implements Dao<Long, Ticket> {
    private static final TicketDao INSTANCE = new TicketDao();
    private static final String DELETE_SQL = """
            DELETE FROM ticket where id = ?
            """;
    private static final String SAVE_SQL = """
            Insert Into ticket (passenger_no, passenger_name, flight_id, seat_no, cost)
              VALUES (?,?,?,?,?) 
            """;

    private static final String FIND_ALL_SQL = """
            select  ticket.id,
             passenger_no,
              passenger_name,
               flight_id,
                seat_no,
                 cost,
                  f.status,
                   f.aircraft_id,
                    f.arrival_airport_code,                    
            f.arrival_date,
             f.departure_airport_code,
              f.departure_date ,
              f.flight_no
            from ticket
            join flight f 
            on ticket.flight_id = f.id
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            where ticket.id = ?
            """;
    private static final String UPDATE_SQL = """
            UPDATE ticket 
            set passenger_no = ?,
            passenger_name = ?,
            flight_id = ?,
            seat_no = ?,
            cost = ?
            WHERE id = ?
            """;

    private  final FlightDao flightDao = FlightDao.getInstance();
    private TicketDao() {
    }

     public List<Ticket> findAll(TicketFilter filter){
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.seatNo() != null) {
            whereSql.add("seat_no LIKE ?");
            parameters.add("%"+filter.seatNo() + "%");
        }
        if (filter.passengerName() != null) {
            whereSql.add("passenger_name = ?");
            parameters.add("%"+filter.passengerName() + "%");
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());
         var where = whereSql.stream()
                 .collect(joining(" AND "," WHERE "," LIMIT ? OFFSET ? "));

        var sql = FIND_ALL_SQL + where;
         try (Connection connection = ConnectionManager.get();
              PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
             for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setObject(i+1,parameters.get(i));
             }
             System.out.println(preparedStatement);
             ResultSet resultSet = preparedStatement.executeQuery();
             List<Ticket> tickets = new ArrayList<>();
             while (resultSet.next()){
                 tickets.add(buldTic(resultSet));
             }
             return tickets;
         } catch (SQLException e) {
             throw new DaoException(e);
         }
     }

    public List<Ticket> findAll(){
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Ticket> tickets = new ArrayList<>();
            while (resultSet.next()){
                tickets.add(buldTic(resultSet));
            }
            return tickets;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Ticket> findById(Long id){
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1,id);
            var resultSet = preparedStatement.executeQuery();
            Ticket ticket = null;
            if(resultSet.next()){
               ticket = buldTic(resultSet);
            }
            return Optional.ofNullable(ticket);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Ticket ticket){
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1,ticket.getPassengerNo());
            preparedStatement.setString(2,ticket.getPassengerName());
            preparedStatement.setLong(3,ticket.getFlight().id());
            preparedStatement.setString(4,ticket.getSeatNo());
            preparedStatement.setBigDecimal(5,ticket.getCost());
            preparedStatement.setLong(6,ticket.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    public Ticket save(Ticket ticket){
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1,ticket.getPassengerNo());
            preparedStatement.setString(2,ticket.getPassengerName());
            preparedStatement.setLong(3,ticket.getFlight().id());
            preparedStatement.setString(4,ticket.getSeatNo());
            preparedStatement.setBigDecimal(5,ticket.getCost());

            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                ticket.setId(generatedKeys.getLong("id"));
            }
            return ticket;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public boolean dtlete(Long id) {
        try (var connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1,id);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }

    private  Ticket buldTic(ResultSet resultSet) throws SQLException {
        var flight = new Flight(
                resultSet.getLong("flight_id"),
                resultSet.getString("flight_no"),
                resultSet.getTimestamp("departure_date").toLocalDateTime(),
                resultSet.getString("departure_airport_code"),
                resultSet.getTimestamp("arrival_date").toLocalDateTime(),
                resultSet.getString("arrival_airport_code"),
                resultSet.getInt("aircraft_id"),
                resultSet.getString("status")
        );
        return new Ticket(
                resultSet.getLong("id"),
                resultSet.getString("passenger_no"),
                resultSet.getString("passenger_name"),
                flightDao.findById(resultSet.getLong("flight_id"),
                        resultSet.getStatement().getConnection()).orElse(null),
                resultSet.getString("seat_no"),
                resultSet.getBigDecimal("cost")
        );
    }
}

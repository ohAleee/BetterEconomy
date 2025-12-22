package me.hsgamer.bettereconomy.transaction;

import me.hsgamer.bettereconomy.database.MetaverseDBClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionQueue implements Runnable {

    /*
        create table lifesteal_transactions
        (
            id          int auto_increment
                primary key,
            sender_id   int                                        not null,
            receiver_id int                                        null,
            amount      double                                     not null,
            type        enum ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') not null,
            timestamp   timestamp                                  not null,
            constraint fk_receiver
                foreign key (receiver_id) references players (id)
                    on update cascade on delete cascade,
            constraint fk_sender
                foreign key (sender_id) references players (id)
                    on update cascade on delete cascade
        );

        create index idx_timestamp
            on lifesteal_transactions (timestamp);
    * */

    private static final String QUERY = "INSERT INTO lifesteal_transactions (sender_id, receiver_id, amount, type, timestamp) VALUES (player_from_uuid(?), player_from_uuid(?), ?, ?, ?)";

    private static final Logger LOGGER = Logger.getLogger("Eco-Logger");

    private final Queue<Transaction> queue = new ConcurrentLinkedQueue<>();
    private final MetaverseDBClient database;

    public TransactionQueue(MetaverseDBClient database) {
        this.database = database;
    }

    @Override
    public void run() {
        if (queue.isEmpty()) return;

        List<Transaction> batch = new ArrayList<>();

        Transaction transaction;
        while ((transaction = queue.poll()) != null) {
            batch.add(transaction);
        }

        if (!batch.isEmpty()) {
            publishBatch(batch);
        }
    }

    public void enqueue(Transaction transaction) {
        queue.offer(transaction);
    }

    private void publishBatch(List<Transaction> transactions) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(QUERY)) {

            connection.setAutoCommit(false);

            for (Transaction tx : transactions) {
                stmt.setString(1, tx.sender().toString());

                if (tx.receiver() != null) {
                    stmt.setString(2, tx.receiver().toString());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }

                stmt.setDouble(3, tx.amount());
                stmt.setString(4, tx.type().name());
                stmt.setTimestamp(5, tx.timestamp());
                stmt.addBatch();
            }

            stmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to publish transaction batch", e);
        }
    }
}

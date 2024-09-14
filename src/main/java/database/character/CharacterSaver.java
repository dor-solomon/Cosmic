package database.character;

import client.Character;
import database.PgDatabaseConnection;
import database.monsterbook.MonsterCardRepository;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

public class CharacterSaver {
    private static final Logger log = LoggerFactory.getLogger(CharacterSaver.class);
    private final PgDatabaseConnection pgConnection;
    private final MonsterCardRepository monsterCardRepository;

    public CharacterSaver(PgDatabaseConnection pgConnection,
                          MonsterCardRepository monsterCardRepository) {
        this.pgConnection = pgConnection;
        this.monsterCardRepository = monsterCardRepository;
    }

    public void save(Character chr) {
        if (!chr.isLoggedin()) {
            return;
        }

        log.debug("Saving chr {}", chr.getName());
        saveToMysql(chr);
        saveToPostgres(chr);
    }

    private void saveToMysql(Character chr) {
        Instant before = Instant.now();
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            try {
                chr.saveCharToDB(con);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Error saving chr {}, level: {}, job: {}", chr.getName(), chr.getLevel(), chr.getJob().getId(), e);
        }
        Duration saveDuration = Duration.between(before, Instant.now());
        log.debug("Saved {} to MySQL in {} ms", chr.getName(), saveDuration.toMillis());
    }

    private void saveToPostgres(Character chr) {
        Instant before = Instant.now();

        try (Handle handle = pgConnection.getHandle()) {
            handle.useTransaction(h -> doPostgresSave(h, chr));
        }

        Duration saveDuration = Duration.between(before, Instant.now());
        log.debug("Saved {} to Postgres in {} ms", chr.getName(), saveDuration.toMillis());
    }

    private void doPostgresSave(Handle handle, Character chr) {
        monsterCardRepository.save(handle, chr.getId(), chr.getMonsterBook().getCards());
    }

}

package database.character;

import client.Character;
import database.monsterbook.MonsterCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class CharacterSaver {
    private static final Logger log = LoggerFactory.getLogger(CharacterSaver.class);
    private final MonsterCardRepository monsterCardRepository;

    public CharacterSaver(MonsterCardRepository monsterCardRepository) {
        this.monsterCardRepository = monsterCardRepository;
    }

    public void save(Character chr) {
        if (!chr.isLoggedin()) {
            return;
        }

        log.debug("Saving chr {}", chr.getName());
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
            return;
        }

        // Saving monster cards to both MySQL and Postgres for now
        monsterCardRepository.save(chr.getId(), chr.getMonsterBook().getCards());
    }

}

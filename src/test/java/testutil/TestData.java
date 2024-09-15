package testutil;

import client.CharacterStats;
import database.PgDatabaseConnection;
import database.character.CharacterRepository;
import org.jdbi.v3.core.Handle;

import java.time.LocalDate;

public class TestData {

    public static GeneratedIds create(PgDatabaseConnection connection) {
        try (Handle handle = connection.getHandle()) {
            int accountId = insertAccount(handle);
            int chrId = insertChr(handle, accountId);
            return new GeneratedIds(accountId, chrId);
        }
    }

    private static int insertAccount(Handle handle) {
        String sql = """
                INSERT INTO account (name, password, birthday)
                VALUES  (:name, :password, :birthday)""";
        return handle.createUpdate(sql)
                .bind("name", "accountname")
                .bind("password", "accountpassword")
                .bind("birthday", LocalDate.of(2005, 5, 11))
                .executeAndReturnGeneratedKeys()
                .mapTo(Integer.class)
                .one();
    }

    private static int insertChr(Handle handle, int accountId) {
        CharacterRepository chrRepository = new CharacterRepository();
        CharacterStats stats = CharacterStats.builder()
                .account(accountId)
                .name("chrname")
                .build();
        return chrRepository.insert(handle, stats);
    }
}

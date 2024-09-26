package testutil;

import client.CharacterStats;
import database.PgDatabaseConnection;
import database.account.Account;
import database.account.AccountRepository;
import database.character.CharacterRepository;
import org.jdbi.v3.core.Handle;

import java.time.LocalDate;

public class TestData {

    public static GeneratedIds create(PgDatabaseConnection connection) {
        int accountId = insertAccount(connection);
        try (Handle handle = connection.getHandle()) {
            int chrId = insertChr(handle, accountId);
            return new GeneratedIds(accountId, chrId);
        }
    }

    private static int insertAccount(PgDatabaseConnection connection) {
        Account account = Account.builder()
                .name("accountname")
                .password("accountpassword")
                .birthdate(LocalDate.now())
                .build();
        return new AccountRepository(connection).insert(account);
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

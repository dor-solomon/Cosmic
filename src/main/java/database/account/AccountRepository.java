package database.account;

import database.PgDatabaseConnection;
import org.jdbi.v3.core.Handle;

public class AccountRepository {
    private final PgDatabaseConnection connection;

    public AccountRepository(PgDatabaseConnection connection) {
        this.connection = connection;
    }

    public Account getByName(String name) {
        return null; // TODO
    }

    public Integer insert(Account account) {
        String sql = """
                INSERT INTO account (name, password, birthdate)
                VALUES (:name, :password, :birthdate)""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("name", account.name())
                    .bind("password", account.password())
                    .bind("birthdate", account.birthdate())
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Integer.class)
                    .one();
        }
    }
}

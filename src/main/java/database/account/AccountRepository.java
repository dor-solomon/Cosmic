package database.account;

import database.PgDatabaseConnection;
import org.jdbi.v3.core.Handle;

import java.util.Optional;

/**
 * @author Ponk
 */
public class AccountRepository {
    private final PgDatabaseConnection connection;

    public AccountRepository(PgDatabaseConnection connection) {
        this.connection = connection;
    }

    public Optional<Account> findById(int accountId) {
        String sql = """
                SELECT id, name, password, pin, pic, logged_in, last_login, birthdate, banned, gender, tos_accepted
                FROM account
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createQuery(sql)
                    .bind("id", accountId)
                    .mapTo(Account.class)
                    .findOne();
        }
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

    public void setTos(int accountId, boolean acceptedTos) {
        String sql = """
                UPDATE account
                SET tos_accepted = :acceptedTos
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("acceptedTos", acceptedTos)
                    .execute();
        }
    }
}

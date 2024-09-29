package database.account;

import client.LoginState;
import database.PgDatabaseConnection;
import org.jdbi.v3.core.Handle;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Ponk
 */
public class AccountRepository {
    private final PgDatabaseConnection connection;

    public AccountRepository(PgDatabaseConnection connection) {
        this.connection = connection;
    }

    public Optional<Account> findByNameIgnoreCase(String name) {
        String sql = """
                SELECT id, name, password, pin, pic, birthdate, gender, tos_accepted, chr_slots, login_state,
                    last_login, banned, banned_until, ban_reason, ban_description
                FROM account
                WHERE lower(name) = lower(:name)""";
        try (Handle handle = connection.getHandle()) {
            return handle.createQuery(sql)
                    .bind("name", name)
                    .mapTo(Account.class)
                    .findOne();
        }
    }

    public Optional<Account> findById(int accountId) {
        String sql = """
                SELECT id, name, password, pin, pic, birthdate, gender, tos_accepted, chr_slots, login_state,
                    last_login, banned, banned_until, ban_reason, ban_description
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
                INSERT INTO account (name, password, birthdate, chr_slots, login_state)
                VALUES (:name, :password, :birthdate, :chrSlots, :loginState)""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("name", account.name())
                    .bind("password", account.password())
                    .bind("birthdate", account.birthdate())
                    .bind("chrSlots", account.chrSlots())
                    .bind("loginState", account.loginState().getValue())
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

    public boolean setGender(int accountId, byte gender) {
        String sql = """
                UPDATE account
                SET gender = :gender
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("gender", gender)
                    .execute() > 0;
        }
    }

    public boolean setPin(int accountId, String pin) {
        String sql = """
                UPDATE account
                SET pin = :pin
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("pin", pin)
                    .execute() > 0;
        }
    }

    public boolean setPic(int accountId, String pic) {
        String sql = """
                UPDATE account
                SET pic = :pic
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("pic", pic)
                    .execute() > 0;
        }
    }

    public boolean setChrSlots(int accountId, int chrSlots) {
        String sql = """
                UPDATE account
                SET chr_slots = :chrSlots
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("chrSlots", chrSlots)
                    .execute() > 0;
        }
    }

    public boolean setLoginState(int accountId, LoginState loginState, Instant lastLogin) {
        String sql = """
                UPDATE account
                SET login_state = :loginState, last_login = :lastLogin
                WHERE id = :id""";
        try (Handle handle = connection.getHandle()) {
            return handle.createUpdate(sql)
                    .bind("id", accountId)
                    .bind("loginState", loginState.getValue())
                    .bind("lastLogin", lastLogin)
                    .execute() > 0;
        }
    }
}

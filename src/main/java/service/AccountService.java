package service;

import database.account.Account;
import database.account.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import tools.BCrypt;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * @author Ponk
 */
@Slf4j
public class AccountService {
    private static final int PASSWORD_HASH_SALT_LOG_ROUNDS = 12;
    private static final LocalDate GMS_RELEASE = LocalDate.of(2005, 5, 11);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public int createNew(String name, String password) {
        Account newAccount = Account.builder()
                .name(name)
                .password(hashPassword(password))
                .birthdate(GMS_RELEASE)
                .build();

        Integer accountId;
        try {
            accountId = accountRepository.insert(newAccount);
        } catch (Exception e) {
            log.error("Failed to create new account", e);
            throw new RuntimeException("Failed to create new account");
        }

        return Optional.ofNullable(accountId)
                .orElseThrow(() -> new RuntimeException("Failed to create new account - missing id"));
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(PASSWORD_HASH_SALT_LOG_ROUNDS));
    }

    public Optional<Account> getAccount(int accountId) {
        return accountRepository.findById(accountId);
    }

    public boolean acceptTos(int accountId) {
        acceptTosMysql(accountId);
        acceptTosPostgres(accountId);
        return true;
    }

    private boolean acceptTosMysql(int accountId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT `tos` FROM accounts WHERE id = ?")) {
                ps.setInt(1, accountId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getByte("tos") == 1) {
                            return false;
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tos = 1 WHERE id = ?")) {
                ps.setInt(1, accountId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean acceptTosPostgres(int accountId) {
        Optional<Account> account = getAccount(accountId);
        if (account.isEmpty()) {
            return false;
        }

        if (account.get().acceptedTos()) {
            return false;
        }

        accountRepository.setTos(accountId, true);
        return true;
    }

    public void setPin(int accountId, String pin) {
        setPinMysql(accountId, pin);
        setPinPostgres(accountId, pin);
    }

    private void setPinMysql(int accountId, String pin) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?")) {
            ps.setString(1, pin);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPinPostgres(int accountId, String pin) {
        boolean success = false;
        try {
            success = accountRepository.setPin(accountId, pin);
        } catch (Exception e) {
            log.error("Failed to set pin due to error - account:{}, pin:{}", accountId, pin, e);
        }
        if (!success) {
            log.warn("Failed to set pin due to no updated rows - account:{}, pin:{}", accountId, pin);
        }
    }
}

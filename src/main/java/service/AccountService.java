package service;

import database.account.Account;
import database.account.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import tools.BCrypt;

import java.time.LocalDate;
import java.util.Optional;

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
}

package database.account;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record Account(String name, String password, boolean acceptedTos, LocalDate birthdate, String pin, String pic,
                      int loggedIn) {
}

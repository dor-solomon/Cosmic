package database.account;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Ponk
 */
@Builder
public record Account(int id, String name, String password, boolean acceptedTos, byte gender, LocalDate birthdate,
                      String pin, String pic, int chrSlots, int loggedIn, LocalDateTime lastLogin, boolean banned) {
}

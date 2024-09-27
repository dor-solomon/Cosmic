package database.account;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Ponk
 */
@Builder
public record Account(int id, String name, String password, boolean acceptedTos, Byte gender, LocalDate birthdate,
                      String pin, String pic, byte chrSlots, byte loginState, LocalDateTime lastLogin, boolean banned,
                      LocalDateTime tempBanTimestamp) {
    public Account {
        Objects.requireNonNull(name);
        Objects.requireNonNull(password);
        Objects.requireNonNull(birthdate);
    }
}

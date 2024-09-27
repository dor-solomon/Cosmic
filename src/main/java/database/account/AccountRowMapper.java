package database.account;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * @author Ponk
 */
public class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Account.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .password(rs.getString("password"))
                .pin(rs.getString("pin"))
                .pic(rs.getString("pic"))
                .birthdate(rs.getDate("birthdate").toLocalDate())
                .gender(Optional.ofNullable(rs.getObject("gender", Short.class))
                        .map(Short::byteValue)
                        .orElse(null))
                .acceptedTos(rs.getBoolean("tos_accepted"))
                .chrSlots(rs.getByte("chr_slots"))
                .loginState(rs.getByte("login_state"))
                .lastLogin(Optional.ofNullable(rs.getTimestamp("last_login"))
                        .map(Timestamp::toLocalDateTime)
                        .orElse(null))
                .banned(rs.getBoolean("banned"))
                .tempBanTimestamp(Optional.ofNullable(rs.getTimestamp("temp_ban_timestamp"))
                        .map(Timestamp::toLocalDateTime)
                        .orElse(null))
                .build();
    }
}

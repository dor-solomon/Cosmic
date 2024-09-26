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
                .loggedIn(rs.getInt("logged_in"))
                .lastLogin(Optional.ofNullable(rs.getTimestamp("last_login"))
                        .map(Timestamp::toLocalDateTime)
                        .orElse(null))
                .birthdate(rs.getDate("birthdate").toLocalDate())
                .banned(rs.getBoolean("banned"))
                .gender(rs.getByte("gender"))
                .acceptedTos(rs.getBoolean("tos_accepted"))
                .build();
    }
}

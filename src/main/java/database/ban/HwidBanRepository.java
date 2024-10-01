package database.ban;

import database.PgDatabaseConnection;
import org.jdbi.v3.core.Handle;

import java.util.List;

public class HwidBanRepository {
    private final PgDatabaseConnection connection;

    public HwidBanRepository(PgDatabaseConnection connection) {
        this.connection = connection;
    }

    public List<HwidBan> getAllHwidBans() {
        String sql = """
                SELECT hwid, account_id
                FROM hwid_ban""";
        try (Handle handle = connection.getHandle()) {
            return handle.createQuery(sql)
                    .mapTo(HwidBan.class)
                    .list();
        }
    }
}

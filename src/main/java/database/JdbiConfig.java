package database;

import database.drop.GlobalMonsterDropRowMapper;
import database.drop.MonsterDropRowMapper;
import database.maker.MakerIngredientRowMapper;
import database.maker.MakerReagentRowMapper;
import database.maker.MakerRecipeRowMapper;
import database.note.NoteRowMapper;
import database.shop.ShopItemRowMapper;
import database.shop.ShopRowMapper;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public final class JdbiConfig {
    private JdbiConfig() {}

    public static Jdbi createConfigured(DataSource dataSource) {
        final Jdbi jdbi = Jdbi.create(dataSource);
        rowMappers().forEach(jdbi::registerRowMapper);
        return jdbi;
    }

    private static List<RowMapper<?>> rowMappers() {
        return List.of(
                new NoteRowMapper(),
                new MakerReagentRowMapper(),
                new MakerRecipeRowMapper(),
                new MakerIngredientRowMapper(),
                new MonsterDropRowMapper(),
                new GlobalMonsterDropRowMapper(),
                new ShopRowMapper(),
                new ShopItemRowMapper()
        );
    }
}

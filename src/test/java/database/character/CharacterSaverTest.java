package database.character;

import client.Character;
import client.CharacterStats;
import client.MonsterBook;
import config.ServerConfig;
import config.YamlConfig;
import database.PgDatabaseConfig;
import database.PgDatabaseConnection;
import database.migration.FlywayRunner;
import database.monsterbook.MonsterCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutil.GeneratedIds;
import testutil.TestData;
import tools.DatabaseConnection;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Testcontainers
class CharacterSaverTest {
    private static final String MYSQL_VERSION = "8.4";
    private static final String POSTGRES_VERSION = "16.4";
    private static final String SCHEMA_NAME = "cosmic";

    @Container
    static MySQLContainer<?> mySql = new MySQLContainer<>("mysql:%s".formatted(MYSQL_VERSION));

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:%s".formatted(POSTGRES_VERSION));

    private PgDatabaseConnection pgConnection;
    private CharacterSaver characterSaver;

    @BeforeEach
    void setUp() {
        prepareMysqlConnection();
        runDbMigrations();
        PgDatabaseConnection pgDatabaseConnection = createPgConnection();
        this.pgConnection = pgDatabaseConnection;
        this.characterSaver = new CharacterSaver(pgDatabaseConnection, new CharacterRepository(),
                new MonsterCardRepository(pgDatabaseConnection));
    }

    // Not using this, but due to the nature of how the db connections are set up, the application requires
    // a real database to connect to.
    private void prepareMysqlConnection() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.DB_URL_FORMAT = "%s";
        serverConfig.DB_HOST = mySql.getJdbcUrl();
        serverConfig.DB_USER = mySql.getUsername();
        serverConfig.DB_PASS = mySql.getPassword();
        serverConfig.INIT_CONNECTION_POOL_TIMEOUT = 60;
        YamlConfig.config.server = serverConfig;
        DatabaseConnection.initializeConnectionPool();
    }

    private void runDbMigrations() {
        PgDatabaseConfig config = PgDatabaseConfig.builder()
                .url(postgres.getJdbcUrl())
                .schema(SCHEMA_NAME)
                .adminUsername(postgres.getUsername())
                .adminPassword(postgres.getPassword())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .poolInitTimeout(Duration.ofSeconds(60))
                .clean(false)
                .build();
        new FlywayRunner(config).migrate();
    }

    private PgDatabaseConnection createPgConnection() {
        return new PgDatabaseConnection(createDataSource());
    }

    private PGSimpleDataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setCurrentSchema(SCHEMA_NAME);
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }

    @Test
    void saveCharacter_shouldUpdateChrTable() {
        GeneratedIds ids = TestData.create(pgConnection);
        Character mockChr = Mockito.mock(Character.class);
        when(mockChr.isLoggedin()).thenReturn(true);
        addEmptyMonsterBook(mockChr);
        when(mockChr.getCharacterStats()).thenReturn(CharacterStats.builder()
                .id(ids.chrId())
                .level(200)
                .build());
        assertEquals(0, getChrLevel(ids.chrId()));

        characterSaver.save(mockChr);

        assertEquals(200, getChrLevel(ids.chrId()));
    }

    private static void addEmptyMonsterBook(Character mockChr) {
        MonsterBook mockMonsterBook = Mockito.mock(MonsterBook.class);
        when(mockMonsterBook.getCards()).thenReturn(Collections.emptyList());
        when(mockChr.getMonsterBook()).thenReturn(mockMonsterBook);
    }

    private int getChrLevel(int chrId) {
        String sql = """
                SELECT level
                FROM chr
                WHERE id = :id""";
        return pgConnection.getHandle().createQuery(sql)
                .bind("id", chrId)
                .mapTo(Integer.class)
                .one();
    }

}

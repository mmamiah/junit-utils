package lu.mms.common.quality.assets.db;

import lu.mms.common.quality.assets.db.hsql.DefaultSqlRoutines;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Set;

/**
 * Enum with common InMemory databases configuration.
 */
public enum InMemoryDb {

    /** The H2 with ORACLE compatibility mode. */
    H2_ORACLE(EmbeddedDatabaseType.H2, DBDriverName.ORACLE, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=Oracle;"),
    /** The H2 with DB2 compatibility mode. */
    H2_DB2(EmbeddedDatabaseType.H2, DBDriverName.DB2,"jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=DB2;"),

    /** The HSQL with ORACLE compatibility mode. */
    HSQL_ORACLE(EmbeddedDatabaseType.HSQL, DBDriverName.ORACLE, "jdbc:hsqldb:mem:%s;sql.syntax_ora=true"),
    /** The HSQL with DB2 compatibility mode. */
    HSQL_DB2(
            EmbeddedDatabaseType.HSQL,
            DBDriverName.DB2,
            "jdbc:hsqldb:mem:%s;sql.syntax_db2=true",
            Set.of(DefaultSqlRoutines.ROUTINE_DIGIT)
    );

    private final EmbeddedDatabaseType dbType;
    private final DBDriverName dbMode;
    private final String urlTemplate;
    private final Set<String> sqlRoutines;

    InMemoryDb(final EmbeddedDatabaseType dbType, final DBDriverName dbMode, final String urlTemplate) {
        this(dbType, dbMode, urlTemplate, Set.of());
    }

    InMemoryDb(final EmbeddedDatabaseType dbType, final DBDriverName dbMode, final String urlTemplate, final Set<String> sqlRoutines) {
        this.dbType = dbType;
        this.dbMode = dbMode;
        this.urlTemplate = urlTemplate;
        this.sqlRoutines = sqlRoutines;
    }

    public EmbeddedDatabaseType getDbType() {
        return dbType;
    }

    public DBDriverName getDbMode() {
        return dbMode;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public Set<String> getSqlRoutines() {
        return sqlRoutines;
    }
}

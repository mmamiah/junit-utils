package lu.mms.common.quality.assets.db;

import java.util.stream.Stream;

public enum DBDriverName {

    DERBY("Apache Derby"),
    DB2("DB2"),
    DB2ZOS("DB2ZOS"),
    HSQL("HSQL Database Engine"),
    SQLSERVER("Microsoft SQL Server"),
    MYSQL("MySQL"),
    ORACLE("Oracle"),
    POSTGRES("PostgreSQL"),
    SYBASE("Sybase"),
    H2("H2 JDBC Driver");

    //A description is necessary due to the nature of database descriptions
    //in metadata.
    private final String driverName;

    DBDriverName(final String driverName) {
        this.driverName = driverName;
    }

    public String getDriverName() {
        return driverName;
    }

    public static DBDriverName from(final String productName) {
        return Stream.of(DBDriverName.values())
                .filter(val -> val.getDriverName().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow();
    }
}

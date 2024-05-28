package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.DBDriverName;
import lu.mms.common.quality.assets.db.MetadataFactory;
import lu.mms.common.quality.assets.db.db2.DB2Metadata;
import lu.mms.common.quality.assets.db.h2.H2Metadata;
import lu.mms.common.quality.assets.db.oracle.OracleMetadata;
import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Record;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import lu.mms.common.quality.assets.db.re.script.Dql;
import lu.mms.common.quality.assets.db.re.script.Relation;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate the DDL and the DML from given Database, Schema, Table and record.
 */
@API(
        status = API.Status.EXPERIMENTAL,
        since = "1.0.0"
)
public class ReverseEngineeringWizard {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseEngineeringWizard.class);

    private static final Set<String> TRUE_VALUES = Set.of(BooleanUtils.TRUE, BooleanUtils.ON, BooleanUtils.YES);

    private int level = 0;
    private boolean ddlOnly;
    private final DataSource dataSource;
    private final String schemaName;

    /* ****** CONFIG ****** */
    private final Map<String, Collection<Statement>> filters = new HashMap<>();
    private final Map<String, Set<Relation>> joins = new HashMap<>();
    /* ****** CONFIG ****** */

    public ReverseEngineeringWizard(final DataSource dataSource, final String schema) {
        this.dataSource = dataSource;
        this.schemaName = schema.toUpperCase();
    }

    public ReverseEngineeringWizard(final DataSource dataSource, final String schema, final int level) {
        this(dataSource, schema);
        this.level = level;
    }

    public ReverseEngineeringWizard withLevel(int level) {
        this.level = level;
        return this;
    }

    public ReverseEngineeringWizard ddlOnly(boolean ddlOnly) {
        this.ddlOnly = ddlOnly;
        return this;
    }

    /**
     * Add a table to the scanning context. Only the records matching the provided {@code values} will be included as
     * table records. If no match is found, the table will remain empty.
     * @param tableName The table name
     * @param filters The SQL expression to filter the tables rows.
     * @return  The {@link ReverseEngineeringWizard} object
     */
    public ReverseEngineeringWizard withTable(final String tableName, final Statement... filters) {
        if (StringUtils.isNotBlank(tableName) && ArrayUtils.isNotEmpty(filters)) {
            for (final Statement statement : filters) {
                this.filters.merge(
                        tableName,
                        List.of(statement),
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList())
                );
            }
        } else {
            this.filters.merge(tableName, List.of(), (a, b) -> a);
        }

        return this;
    }

    /**
     * Add a table to the scanning context. Only the records matching the provided {@code values} will be included as
     * table records. If no match is found, the table will remain empty.
     * @param tableName The table name
     * @param sqlRelation The SQL join template.
     * @return  The {@link ReverseEngineeringWizard} object
     */
    public ReverseEngineeringWizard withTable(final String tableName, final Relation sqlRelation) {
        sqlRelation.setSourceTable(tableName);
        Set<Relation> relations = new HashSet<>();
        if (StringUtils.isNotBlank(tableName)) {
            relations.add(sqlRelation);
        }
        joins.merge(
                tableName, relations,
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())
        );
        filters.merge(tableName, List.of(), (a, b) -> a);

        return this;
    }

    public Schema build() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // get connection
        try (final Connection connection = dataSource.getConnection()){
            final DatabaseMetaData metadata = connection.getMetaData();

            final Schema schema = createSchema(schemaName.toUpperCase(), metadata);

            // add default config definitions & values
            for (Map.Entry<String, Collection<Statement>> entry : filters.entrySet()) {
                schema.appendTable(entry.getKey(), metadata, this::collectTableDefinition);
            }

            // Add PK & FK tables definition depending on level.
            final Set<String> skip = new HashSet<>();
            while(--this.level >= 0) {
                // add relations definitions
                final Collection<Table> scope = new HashSet<>(schema.getTables().values());
                for (Table table : scope) {
                    if (skip.contains(table.getName())) {
                        continue;
                    }
                    skip.add(table.getName());

                    // Explorer exported Keys: Foreign keys
                    appendImportedKeys(metadata, schema, table);

                    // Explorer exported Keys: Primary keys
                    appendExportedKeys(metadata, schema, table);

                    LOGGER.info("Table [{}] added in the context.", table.getName());

                    this.joins.merge(
                            table.getAlias(), table.getAllRelations(),
                            (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())
                    );
                }
            }

            // collect each table records
            if (!this.ddlOnly) {
                for (final Table table : schema.getTables().values()) {
                    // collect the columns values
                    collectColumnsValues(connection, table);
                }
            }

            // Clean table constraints (make the difference between Table constraints and Columns constraints)
            schema.cleanConstraints();

            // sort the tables columns
            stopWatch.stop();
            schema.setElapsedTime(stopWatch);
            return schema;
        } catch (SQLException ex){
            LOGGER.error("Failed to extract the data. Code: {}, Cause: {}.", ex.getErrorCode(), ex.getMessage());
            throw new IllegalStateException(ex.getMessage(), ex);
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
        }
    }

    private Schema createSchema(final String schemaName, final DatabaseMetaData metadata) throws SQLException {
        final ResultSet schemaResultSet = metadata.getSchemas(null, schemaName);
        if (!schemaResultSet.next()) {
            throw new IllegalArgumentException(String.format("Can't find [%s] schema in the DB.", schemaName));
        }
        return new Schema(schemaName);
    }


    /**
     * This method will find where the table (in parameters) Primary keys are used, and add the found tables in the
     * context.
     * @param metadata  The DB metadata
     * @param schema    The DB schema to enrich
     * @param table The source table
     * @throws SQLException The exception thrown when accessing the data
     */
    private void appendExportedKeys(final DatabaseMetaData metadata, final Schema schema, final Table table) throws SQLException {
        LOGGER.info("Adding table [{}] in the context.", table.getName());

        // Handle the exported Keys
        final ResultSet exportedKeys = metadata.getExportedKeys(null, schema.getName(), table.getName());
        while (exportedKeys.next()) {
            final String targetTableName = exportedKeys.getString(7);
            final String targetColumnName = exportedKeys.getString(8);

            final String originColumnName = exportedKeys.getString(4);

            // retrieve the target table
            final Table targetTable = schema.appendTable(targetTableName, metadata, this::collectTableDefinition);
            targetTable.appendForeignKey(targetColumnName, table, originColumnName);
            targetTable.getStatements().addAll(table.getStatements());

            table.appendPrimaryKey(originColumnName, targetTable, targetColumnName);
        }
    }

    /**
     * This method will find where the table (in parameters) foreign keys come from, and add the found tables in the
     * context.
     * @param metadata  The DB metadata
     * @param schema    The DB schema to enrich
     * @param table The source table
     * @throws SQLException The exception thrown when accessing the data
     */
    private void appendImportedKeys(final DatabaseMetaData metadata, final Schema schema, final Table table) throws SQLException {
        LOGGER.info("Adding table [{}] in the context.", table.getName());

        // Imported Keys
        final ResultSet importedKeys = metadata.getImportedKeys(null, schema.getName(), table.getName());
        while (importedKeys.next()) {
            final String targetTableName = importedKeys.getString(3);
            final String targetColumnName = importedKeys.getString(4);

            final String originColumnName = importedKeys.getString(8);

            // retrieve the target table
            final Table targetTable = schema.appendTable(targetTableName, metadata, this::collectTableDefinition);
            targetTable.appendPrimaryKey(targetColumnName, table, originColumnName);
            targetTable.getStatements().addAll(table.getStatements());

            table.appendForeignKey(originColumnName, targetTable, targetColumnName);
        }
    }

    private void collectTableDefinition(final DatabaseMetaData metadata, final Table table) {
        LOGGER.info("Collecting table [{}] definition.", table.getName());
        final DBDriverName driverName = retrieveDriverName(dataSource);
        MetadataFactory metadataFactory;
        if (DBDriverName.ORACLE == driverName) {
            metadataFactory = new OracleMetadata(dataSource, table.getSchema().getName(), table.getName());
        } else if (DBDriverName.DB2 == driverName) {
            metadataFactory = new DB2Metadata(dataSource, table.getSchema().getName(), table.getName());
        } else /*if (DatabaseType.H2 == dbType) */ {
            metadataFactory = new H2Metadata(dataSource, table.getSchema().getName(), table.getName());
        }

        // collect PKs
        final Set<String> pkTableConstraint = new HashSet<>();
        try (final ResultSet resultSet = metadata.getPrimaryKeys(null, table.getSchema().getName(), table.getName())) {
            while (resultSet.next()) {
                pkTableConstraint.add(resultSet.getString("COLUMN_NAME"));
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }

        final String uniqueTableConstraint = metadataFactory.getUniqueTableConstraint();

        // collect the table columns
        try (final ResultSet resultSet = metadata.getColumns(null, null, table.getName(), null)) {
            String tableDescription = null;
            while (resultSet.next()) {
                tableDescription = resultSet.getString("REMARKS");

                final String columnName = resultSet.getString("COLUMN_NAME");
                final Map<String, Object> columnMetadata = metadataFactory.getColumnMetadata(columnName);
                if (MapUtils.isNotEmpty(columnMetadata)) {
                    final Column column = new Column(
                            Integer.parseInt(columnMetadata.get(metadataFactory.getKeyColumnId()).toString()),
                            columnName,
                            metadataFactory.computeColumnType(columnMetadata),
                            pkTableConstraint.contains(columnName),
                            uniqueTableConstraint.contains(columnName),
                            TRUE_VALUES.contains(StringUtils.defaultString(resultSet.getString("IS_AUTOINCREMENT")).toLowerCase()),
                            columnMetadata.get(metadataFactory.getKeyColumnDefault())
                    );
                    table.addColumn(column);
                }
            }

            table.appendPrimaryConstraint(pkTableConstraint)
                    .appendCheckConstraint(metadataFactory.getCheckConstraints())
                    .appendUniqueConstraint(uniqueTableConstraint)
                    .setDescription(tableDescription);

            final List<Statement> tableFilters = ObjectUtils.defaultIfNull(filters.get(table.getName()), List.<Expression>of())
                    .stream()
                    .map(statement -> {
                        statement.applyAlias(table.getAlias());
                        return statement;
                    })
                    .collect(Collectors.toList());
            table.getStatements().addAll(tableFilters);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }
        LOGGER.info("Table [{}] definition collected.", table.getName());
    }

    /**
     * Determine the database type from a given {@code datasource}.
     * @param dataSource    The datasource
     * @return  The database type object
     */
    private static DBDriverName retrieveDriverName(final DataSource dataSource) {
        String driverName = null;
        try {
            driverName = JdbcUtils.extractDatabaseMetaData(
                    dataSource, DatabaseMetaData::getDriverName
            );
        } catch (MetaDataAccessException ex) {
            LOGGER.error(ex.getMessage());
        }
        return DBDriverName.from(driverName);
    }

    private void collectColumnsValues(final Connection connection, final Table table) {
        final String sql = Dql
                .select(table.getColumns().values())
                .from(table)
                .join(collectActiveRelations(table))
                .where(table.getStatements())
                .build();

        LOGGER.debug("SQL query: \n" + sql);

        // execute the statement
        try (final PreparedStatement statement = connection.prepareStatement(sql)){
            try (final ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final Record record = new Record();
                    for (Map.Entry<String, Column> entry: table.getColumns().entrySet()){
                        record.appendColumnValue(entry.getKey(), resultSet.getObject(entry.getKey()));
                    }
                    table.addRecord(record);
                }
            }
        } catch (final SQLException ex) {
            final String msg = String.format("Failed to extract the data. Code: %s, Cause: %s.", ex.getErrorCode(), ex.getMessage());
            LOGGER.error(msg);
            throw new IllegalStateException(msg, ex);
        } catch (final Exception exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }

    }

    private HashSet<Relation> collectActiveRelations(Table table) {
        final HashSet<String> activeAlias = new HashSet<>(Set.of(table.getAlias()));
        activeAlias.addAll(
                table.getAllRelations().stream()
                        .flatMap(rel -> Stream.of(rel.getSourceTableAlias(), rel.getTargetTableAlias()))
                        .collect(Collectors.toSet())
        );

        final HashSet<Relation> relations = new HashSet<>(table.getAllRelations());
        table.getStatements().stream()
                // retrieve all the alias from table
                .map(Statement::getAlias)
                // keep the alias that are missing
                .filter(a -> !activeAlias.contains(a))
                // collect the related relation(s)
                .map(a -> this.joins.get(a).stream()
                        .filter(r -> activeAlias.contains(r.getSourceTableAlias()) || activeAlias.contains(r.getTargetTableAlias()))
                        .findFirst()
                        .orElse(null)
                )
                .filter(ObjectUtils::allNotNull)
                .findFirst()
                // add the found relation to the final result
                .ifPresent(relations::add);
        return relations;
    }

}

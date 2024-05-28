package lu.mms.common.quality.assets.db.re.schema;

import lu.mms.common.quality.assets.db.re.Statement;
import lu.mms.common.quality.assets.db.re.script.Relation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table implements Comparable<Table> {

    private final String name;
    private final String alias;
    private final List<Record> records = new ArrayList<>();
    private final Map<String, Column> columns = new LinkedHashMap<>();
    private final Map<String, Set<Relation>> primaryKeys = new HashMap<>();
    private final Map<String, Set<Relation>> foreignKeys = new HashMap<>();
    private String primaryKeyConstraint = StringUtils.EMPTY;
    private String uniqueConstraint = StringUtils.EMPTY;
    private String checkConstraint = StringUtils.EMPTY;

    /*
     * Filters for the SQL WHERE clause.
     */
    private final Set<Statement> statements = new HashSet<>();
    private Schema schema;
    private String description;

    public Table(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The table name can not be blank or empty.");
        }
        this.name = name.toUpperCase();
        this.alias = RandomStringUtils.randomAlphabetic(4);
    }

    public void addRecord(final Record record) {
        final boolean found = this.records.stream()
                .anyMatch(entry -> entry.equalTo(record));
        if (!found) {
            this.records.add(record);
        }
    }

    public Map<String, Set<Relation>> getRelations() {
        final Map<String, Set<Relation>> relations = new HashMap<>();
        primaryKeys.forEach((key, value) -> relations.merge(
                key, value,
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())));
        foreignKeys.forEach((key, value) -> relations.merge(
                key, value,
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())));
        return relations;
    }

    public Set<Relation> getAllRelations() {
        return getRelations().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<Statement> getStatements() {
        return statements;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void addColumn(final Column column) {
        column.setParent(this);
        this.columns.put(column.getName(), column);
    }

    public String getPrimaryKeyConstraint() {
        return primaryKeyConstraint;
    }

    public Table appendPrimaryConstraint(final Collection<String> constraints) {
        final Collection<String> safeConstraints = ObjectUtils.defaultIfNull(constraints, Collections.emptyList());

        // if the table have only one PK, no need to add it at Table level
        if (safeConstraints.size() > 1) {
            this.primaryKeyConstraint = safeConstraints
                    .stream()
                    .reduce((a, b) -> StringUtils.joinWith(",", a, b))
                    .orElse(StringUtils.EMPTY);
        }
        return this;
    }

    public String getCheckConstraint() {
        return checkConstraint;
    }

    public void appendPrimaryKey(final String columnName, final Table refTable, final String refColumnName) {
        final Relation relation = new Relation(this.name, columnName, refTable.getName(), refColumnName);
        relation.setSourceTableAlias(this.alias);
        relation.setTargetTableAlias(refTable.getAlias());

        this.primaryKeys.merge(
                columnName, Set.of(relation),
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())
        );
    }

    public void appendForeignKey(final String columnName, final Table refTable, final String refColumnName) {
        final Relation relation = new Relation(this.name, columnName, refTable.getName(), refColumnName);
        relation.setSourceTableAlias(this.alias);
        relation.setTargetTableAlias(refTable.getAlias());

        this.foreignKeys.merge(
                columnName, Set.of(relation),
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet())
        );
    }

    public Map<String, Set<Relation>> getForeignKeys() {
        return foreignKeys;
    }

    public Table appendCheckConstraint(final String constraint) {
        this.checkConstraint = ObjectUtils.defaultIfNull(constraint, StringUtils.EMPTY);
        return this;
    }

    public String getUniqueConstraint() {
        return uniqueConstraint;
    }

    public Table appendUniqueConstraint(final String constraint) {
        this.uniqueConstraint = ObjectUtils.defaultIfNull(constraint, StringUtils.EMPTY);
        return this;
    }

    /**
     * @return The table name.
     */
    public String getName() {
        return name;
    }

    public void setSchema(final Schema schema) {
        this.schema = schema;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getAlias() {
        return alias;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    /**
     * Clean the Table level constraints if necessary, so that they will be add at column level.
     */
    public void cleanConstraints() {
        // clean PK
        if (primaryKeyConstraint.contains(",")) {
            // disable PK at column level
            columns.values().forEach(Column::setNotPrimaryKey);
        } else {
            // disable PK at table level
            primaryKeyConstraint = StringUtils.EMPTY;
        }

        // clean unique constraint
        if (uniqueConstraint.contains(",")) {
            // disable PK at column level
            columns.values().forEach(Column::setNotUnique);
        } else {
            // disable PK at table level
            uniqueConstraint = StringUtils.EMPTY;
        }
    }

    public List<Object> getValue(final String columnName) {
        return records.parallelStream()
                .map(record -> record.getValue(columnName))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ": " + records +
                '}';
    }

    @Override
    public int compareTo(final Table table) {
        return Comparator.comparing(Table::getName).compare(this, table);
    }

}

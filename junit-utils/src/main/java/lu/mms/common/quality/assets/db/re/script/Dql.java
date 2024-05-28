package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Column;

import java.util.Collection;

/**
 * The Data Query Language class.
 */
public interface Dql {

    static From select(final Collection<Column> selectColumns) {
        return new From(selectColumns);
    }

}

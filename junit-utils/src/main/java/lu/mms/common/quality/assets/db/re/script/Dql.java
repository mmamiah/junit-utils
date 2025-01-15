package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.Expression;
import lu.mms.common.quality.assets.db.re.schema.Column;

/**
 * The Data Query Language class.
 */
public interface Dql {

    static From select(final Column... selectColumns) {
        return new From(selectColumns);
    }

    static From select(final Expression... functions) {
        return new From(functions);
    }

}

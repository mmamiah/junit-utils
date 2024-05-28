package lu.mms.common.quality.junit.assets.db.re.script;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class DataQueryOperation implements SqlScript {

    /**
     * DQL Template.
     */
    private static final String DQL_STATEMENT = "\n%S\n(%s)";

    private String dataQueryOperationExpression;

    protected String getDqoExpression() {
        return ObjectUtils.defaultIfNull(dataQueryOperationExpression, StringUtils.EMPTY).trim();
    }

    public DataQueryOperation intersect(final SqlScript sqlScript) {
        this.dataQueryOperationExpression = format("intersect", sqlScript.build());
        return this;
    }

    public DataQueryOperation union(final SqlScript sqlScript) {
        this.dataQueryOperationExpression = format("union", sqlScript.build());
        return this;
    }

    public DataQueryOperation except(final SqlScript sqlScript) {
        this.dataQueryOperationExpression = format("except", sqlScript.build());
        return this;
    }

    private String format(final String operation, final String script) {
        return String.format(DQL_STATEMENT, operation, script);
    }

}

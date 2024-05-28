package lu.mms.common.quality.assets.db.re;

public interface Statement extends CanBuild {

    String getAlias();

    void applyAlias(final String alias);

}

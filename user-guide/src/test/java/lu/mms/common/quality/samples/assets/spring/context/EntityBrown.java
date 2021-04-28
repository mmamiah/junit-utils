package lu.mms.common.quality.samples.assets.spring.context;

import org.springframework.beans.factory.annotation.Value;

/**
 * Sample entity.
 */
public class EntityBrown {

    @Value("${entity.colorName}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}

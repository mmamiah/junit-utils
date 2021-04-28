package lu.mms.common.quality.samples.assets.spring.context;

import org.springframework.stereotype.Component;

/**
 * An annotated component.
 */
@Component
public class AnnotatedComponent {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}

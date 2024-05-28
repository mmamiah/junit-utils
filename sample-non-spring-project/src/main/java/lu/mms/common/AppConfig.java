package lu.mms.common;

/**
 * A sample APP.
 */
public final class AppConfig {

    private AppConfig() {
        // Hidden constructor
    }

    /**
     * @param args The app arguments.
     */
    public static void main(final String[] args) {
        final AppController controller = new AppController(new ArgumentsService(new EmptyVerifier()));
        System.out.println("args is valid: " + controller.handle(args));
    }
}

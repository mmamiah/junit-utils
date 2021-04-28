package lu.mms.common;

/**
 * A sample APP.
 */
public class AppConfig {

    public static void main(final String[] args ) {
        final AppController controller = new AppController(new ArgumentsService(new EmptyVerifier()));
        System.out.println( "args is valid: " + controller.handle(args) );
    }
}

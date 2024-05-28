package lu.mms.common;

/**
 * The App Controller.
 */
public class AppController {

    private final ArgumentsService argumentsService;

    /**
     * @param argumentsService The service
     */
    public AppController(final ArgumentsService argumentsService) {
        this.argumentsService = argumentsService;
    }

    public ArgumentsService getArgumentsService() {
        return argumentsService;
    }

    /**
     * @param args The arguments
     * @return  true/false
     */
    public boolean handle(final Object[] args) {
        return argumentsService.countArgs(args) > 0;
    }
}

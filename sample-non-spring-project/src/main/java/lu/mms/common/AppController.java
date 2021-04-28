package lu.mms.common;

public class AppController {

    private final ArgumentsService argumentsService;

    public AppController(final ArgumentsService argumentsService) {
        this.argumentsService = argumentsService;
    }

    public ArgumentsService getArgumentsService() {
        return argumentsService;
    }

    public boolean handle(final Object[] args) {
        return argumentsService.countArgs(args) > 0;
    }
}

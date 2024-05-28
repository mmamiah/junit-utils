package lu.mms.common.quality.assets.bdd;

class FailureDetails {
    private final String message;
    private final String methodName;

    public FailureDetails(final String message, final String methodName) {
        this.message = message;
        this.methodName = methodName;
    }

    public String getMessage() {
        return message;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "[" + methodName + "] " + message + '\'';
    }
}

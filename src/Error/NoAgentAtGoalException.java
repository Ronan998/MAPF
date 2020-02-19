package Error;

public class NoAgentAtGoalException extends RuntimeException{

    public NoAgentAtGoalException(String errorMessage) {
        super(errorMessage);
    }
}

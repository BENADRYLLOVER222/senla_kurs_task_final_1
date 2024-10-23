package first.second.third.ecosystem.exception;

public class UnknownCategoryException extends RuntimeException {
    public UnknownCategoryException(String category) {
        super("Unknown category: " + category);
    }
}
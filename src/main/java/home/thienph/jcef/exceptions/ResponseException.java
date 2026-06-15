package home.thienph.jcef.exceptions;

import lombok.Getter;

@Getter
public class ResponseException extends RuntimeException {
    int status;

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }
}

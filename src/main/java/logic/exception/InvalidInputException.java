package logic.exception;

import java.io.IOException;

public class InvalidInputException extends IOException {

    public InvalidInputException(String mess){
        super(mess);
    }
}

package logic.exception;

public class UnexistingFileException extends Exception {

    public UnexistingFileException(String msg){
        super(msg);
    }
}

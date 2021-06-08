package logic.exception;

public class UnexistingFileException extends Throwable {

    public UnexistingFileException(String msg){
        super(msg);
    }
}

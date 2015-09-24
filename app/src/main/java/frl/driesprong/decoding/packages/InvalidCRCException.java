package frl.driesprong.decoding.packages;

public class InvalidCRCException extends ParseException {
    public InvalidCRCException(String message) {
        super(message);
    }
}

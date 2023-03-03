package hello.jdbc.repository.ex;


//런타임 익셉션을 상속받았으니 이제 언체크 예외다
public class MyDbException extends RuntimeException {
    public MyDbException() {

    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}

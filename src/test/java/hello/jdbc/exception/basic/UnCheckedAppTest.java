package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void Unchecked(){
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request()).isInstanceOf(RuntimeException.class);
        //Exception의 자식이면 테스트 성공.
    }

    @Test
    void printEx(){
        Controller controller = new Controller();
        try{
            controller.request();
        }catch(Exception e){
            //e.printStackTrace() 로 하는것도 좋지만 이거는 System.out.print에서 사용해라. 실무에서는 log다.
            log.info("ex", e);
        }
    }

    //지금 컨트롤러는 SQLException을 의존하고 있다. SQLException은 JDBC기술이다. 향후 리포지토리를 JDBC가 아닌 다른기술로 변경하면,
    // 모든 컨트롤러 코드를 JPAException으로 변경해야 한다.
    static class Controller{
        Service service = new Service();

        public void request()  {
            service.logic();
        }
    }


    static class Service{

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic()  {
            repository.call();      //여기서 예외가 터지면, 아래는 호출 안된다
            networkClient.call();
        }

    }

    static class NetworkClient{
        //나의 입장에서는 클라이언트고 실제 어떤 네트워크를 통해서 호출한다고 보면 된다. 상대가 서버가되고 내가 클라이언트가 된다.

        public void call()  {
            throw new RuntimeConnectException("연결 실패");
        }
    }


    static class Repository{
        public void call(){
            //내부적으로 runSQL을 호출하고 이 repository에서 예외를 잡을 거다.
            try {
                runSQL();
            }catch(SQLException e){
                throw new RuntimeSQLException(e);   //예외를 던질때는 항상 기존 예외를 넣어줘야함.
                //나중에 스택트레이스 출력할때 SQLException이랑 RuntimeSQLException 둘다 출력할 수 있다.
                /**
                 * 체크 예외를 언체크 예외로 바꿔서 던졌다.
                 */

            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {

        public RuntimeSQLException() {
        }

        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
        //Throwable cause 왜 발생했는지 이전예외를 넣어줄 수있다.
    }

}

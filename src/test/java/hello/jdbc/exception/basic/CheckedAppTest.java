package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class CheckedAppTest {

    @Test
    void checked(){
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request()).isInstanceOf(Exception.class);
        //Exception의 자식이면 테스트 성공.
    }

    //지금 컨트롤러는 SQLException을 의존하고 있다. SQLException은 JDBC기술이다. 향후 리포지토리를 JDBC가 아닌 다른기술로 변경하면,
    // 모든 컨트롤러 코드를 JPAException으로 변경해야 한다.
    static class Controller{
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }


    static class Service{

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }

    }

    static class NetworkClient{
        //나의 입장에서는 클라이언트고 실제 어떤 네트워크를 통해서 호출한다고 보면 된다. 상대가 서버가되고 내가 클라이언트가 된다.

        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }
    static class Repository{
        public void call()throws SQLException{
            throw new SQLException("ex");       //SQL exception은 체크예외다.
        }
    }
}

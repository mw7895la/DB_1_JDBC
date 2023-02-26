package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {
    public static Connection getConnection(){
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);       //데이터베이스에 연결.
            //반환 된것은 connection인데 이건 인터페이스다. 뭔가 구현체를 가져온것이겠지
            log.info("get connection ={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            //체크 익셉션을 런타임 익셉션으로 던진다.
            throw new IllegalStateException();
        }
    }
}

package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        log.info("connection ={} ,  class={}", con1, con1.getClass());
        log.info("connection ={} ,  class={}", con2, con2.getClass());
    }

    //DriverManagerDataSource
    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득.     //스프링에서 제공하는 것.     DirverManagerDataSource는 DataSource를 구현하고 있음.
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);

    }


    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //커넥션 풀링 HiKari 사용  //스프링에서 JDBC를 쓰면 자동으로 Hikari가 임포트 된다.  // DataSource를 구현했다.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);      //원래 기본 10개임
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);  //그대로 실행하면 안된다.
        Thread.sleep(1000);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        Connection con3 = dataSource.getConnection();
        Connection con4 = dataSource.getConnection();
        Connection con5 = dataSource.getConnection();
        Connection con6 = dataSource.getConnection();
        Connection con7 = dataSource.getConnection();
        Connection con8 = dataSource.getConnection();
        Connection con9 = dataSource.getConnection();


        log.info("connection ={} ,  class={}", con1, con1.getClass());
        log.info("connection ={} ,  class={}", con2, con2.getClass());
    }
}

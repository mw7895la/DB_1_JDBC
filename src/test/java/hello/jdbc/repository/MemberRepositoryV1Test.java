package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach//각 테스트가 실행되기 직전에 실행
    void beforeEach(){
        //기본 DriverManager - 항상 새로운 커넥션 획득 . 쿼리 하나 할때마다 새로운 커넥션을 맺는다.
        //DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        //인자로 넘겨서 MemberRepositoryV1의 DataSource는 DirverManagerDataSource가 됨.
        //커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        //한개의 conn0 만 쓰고있다. save 수행 후 conn0을 close할때 닫는게 아니라 반환하는 것이다. hikari가 감싸고 있어서 close요청이 오면 반환하는 로직이 있다.
        //그리고 또 conn0을 꺼내서 findById 수행 후 close할때 반환.

        repository = new MemberRepositoryV1(dataSource);



    }

    @Test
    void curd() throws SQLException, InterruptedException {
        Member member = new Member("memberV6", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember ={}", findMember);
        assertThat(findMember).isEqualTo(member);       //2개는 서로 다른 인스턴스인데 equals는 왜 true?  원래대로라면  hashcode를 구현해야된다. 근데 @Data에 equals와 HashCode가 구현되어있다.

/*        if(true){
            throw new IllegalArgumentException("......");
        }*/


        //update : money 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);
        //NoSuchElement ...  익셉션이 터지면 된다

        //Member deleteMember = repository.findById(member.getMemberId()); //지웠기 때문에 얘가 조회가 되면 안된다.

        Thread.sleep(1000);
    }


}
package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void curd() throws SQLException {
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


    }


}
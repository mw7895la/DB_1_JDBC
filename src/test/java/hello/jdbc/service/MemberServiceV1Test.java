package hello.jdbc.service;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 기본 동작,  트랜잭션이 없어서 문제 발생
 */
public class MemberServiceV1Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepository;

    private MemberServiceV1 memberService;


    //각각의 테스트 전에 실행된다.
    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
    }
    //각각의 테스트 후에 실행된다.
    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given - 이런 데이터들이 준비되있을 때
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);


        //when - 이걸 수행해서
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then - 검증해라
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외발생")
    void accountTransferEx() throws SQLException {
        //given - 이런 데이터들이 준비되있을 때
        Member memberA = new Member(MEMBER_A, 10000);
        Member MEMBER_EX = new Member(MemberServiceV1Test.MEMBER_EX, 10000);          // MEMBER_EX로 수정
        //변수 여러군데 있는거 이름 한번에 바꾸기  Shift + F6
        memberRepository.save(memberA);
        memberRepository.save(MEMBER_EX);


        //when - 이걸 수행해서
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), MEMBER_EX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);     //수행 결과가 예외가 터져야 테스트가 성공하게 됨.



        //then - 검증해라
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(MEMBER_EX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}

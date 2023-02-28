package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.extern.slf4j.Slf4j;
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
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화(같은걸 쓴다는 것)
 */
@Slf4j
public class MemberServiceV2Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV2 memberRepository;

    private MemberServiceV2 memberService;


    //각각의 테스트 전에 실행된다.
    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        memberRepository = new MemberRepositoryV2(dataSource);
        memberService = new MemberServiceV2(memberRepository,dataSource);
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
        log.info("start tx");           //같은 커넥션인지 확인 로그.
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("end tx");
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
        Member MEMBER_EX = new Member(MemberServiceV2Test.MEMBER_EX, 10000);          // MEMBER_EX로 수정
        //변수 여러군데 있는거 이름 한번에 바꾸기  Shift + F6


        memberRepository.save(memberA);
        memberRepository.save(MEMBER_EX);

        log.info("---------------------------------커넥션 확인");
        //when - 이걸 수행해서
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), MEMBER_EX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);     //수행 결과가 예외가 터져야 테스트가 성공하게 됨.

        //MemberServiceV2의 accountTransfer()에서 1번 업데이트 후 익셉션이 터져서 계속 익셉션을 던져서 여기 isInstanceOf(Illegal~~.class) 까지 왔다.
        // 어쨌든 예외가 터지면 catch 해서 accountTransfer에서 rollback을 했다. 그러면 MemberA의 돈도 다시 만원이 된다.

        log.info("---------------------------------커넥션 확인");

        //then - 검증해라
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(MEMBER_EX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}

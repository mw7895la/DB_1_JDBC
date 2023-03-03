package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException이 제거될 것.
 *
 * MemberRepository 인터페이스에 의존하는 것으로 바꾸자.
 */
@Slf4j
@SpringBootTest
public class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberServiceV4 memberService;

    //JUnit5 에선 필드주입은 되지만 생성자를 통한 의존성 주입이 안된다.
    // https://pinokio0702.tistory.com/189 참고

    @TestConfiguration      // 테스트 클래스 내부에서 스프링 부트가 자동으로 만들어주는 빈에 추가로 스프링 빈들을 생성해서 등록한다.
    static class TestConfig{

        private final DataSource dataSource;

        //스프링 컨테이너에 자동으로 등록된 dataSource를 의존관계 주입해준다.
        @Autowired
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository(){
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4(){
            return new MemberServiceV4(memberRepository());
        }
    }





    @Test       //프록시가 적용이 됬나?
    void AopCheck(){
        log.info("memberService class ={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        /*
        MemberServiceV3_3 에 클래스 단위로든, 메소드 단위로든 @Transactional 이 적용되면 프록시가 생성된다.
        스프링이 코드를 쫙 보고 클래스나 메소드 @Transactioanl이 있으면 넌 AOP 적용 대상이구나 하고 프록시를 만들어서 적용을 해준다.
        MemberService 에 찍힌 클래스를 보면 $$EnhancerBySpringCGLIB$$5b98cd9c가 찍혀있다. 이러면 스프링 빈에 proxy가 들어가 있는거다. 우리가 위에서 MemberServiceV3_3 의존관계 주입을 받았는데 실제 서비스(MemberServiceV3_3)를 받는게 아닌 스프링 빈으로 등록된 TransactionProxy code부분을 받게된다.
        프록시를 도입하면 스프링이 서비스 로직을 상속을 받아서 강의자료에 있는 TransactionProxy같은 코드를 만들어낸다.
        주입받은건 실제 우리의 MemberServiceV3_3이 아니고 트랜잭션 프록시 코드다. 이 안에는 트랜잭션을 처리하는 로직을 가지고 있다. target.logic() 부분은 우리의 실제 서비스의 로직을(accountTansfer() ) 호출한다.

        2023-03-02 10:04:52.915  INFO 16252 --- [           main] h.jdbc.service.MemberServiceV3_3Test     : memberService class =class hello.jdbc.service.MemberServiceV3_3$$EnhancerBySpringCGLIB$$5b98cd9c
        2023-03-02 10:04:52.916  INFO 16252 --- [           main] h.jdbc.service.MemberServiceV3_3Test     : memberRepository class=class hello.jdbc.repository.MemberRepositoryV3
         */

        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();

    }

    //각각의 테스트 후에 실행된다.
    @AfterEach
    void after()  {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer()   {
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
    void accountTransferEx()   {
        //given - 이런 데이터들이 준비되있을 때
        Member memberA = new Member(MEMBER_A, 10000);
        Member MEMBER_EX = new Member(MemberServiceV4Test.MEMBER_EX, 10000);          // MEMBER_EX로 수정
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

package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 트랜잭션 템플릿 사용
 *
 * 트랜잭션 AOP를 사용하기 위해서는 스프링 빈으로 다 등록을 해줘야 한다.
 * 기존처럼 스프링 빈으로 등록을 안해도 간단하게 해당 클래스들만 임포트해서 테스트를 했지만 여기는 다르다.(MemberServiceV3_3은 임포트 따로 안해도 되지, 패키지 순서가 같아서)
 */
@Slf4j
@SpringBootTest
public class MemberServiceV3_3Test {
    /**
     * @SpringBootTest를 사용하면 @SpringBootApplication이 있는 CoreApplication을 찾아서 사용합니다.
     *
     * 그리고 이렇게 찾은 @SpringBootApplication 안에는 @ComponentScan이 존재합니다.
     *
     * 이곳의 package 위치는 hello.core이기 때문에 우리가 작성한 전에 애플리케이션 코드가 컴포넌트 스캔의 대상이 됩니다. 여기에는 @Configuration도 포함됩니다.
     *
     * 그리고 테스트에서 실행했기 때문에 test 중에서도 hello.core를 포함한 그 하위 패키지는 컴포넌트 스캔의 대상이 됩니다.
     *
     * 두번째 질문하신 것은 @SpringBootTest가 있으면 해당 테스트 클래스는 특수하게 @Autowired를 허용해줍니다.
     * 이것은 스프링 빈으로 등록되어서 그런 것은 아니고, JUnit과 스프링이 예외적으로 테스트를 편리하게 하도록 허용하는 기능입니다.
     */

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    //@Autowired 는 타입으로 의존성을 주입 // 타입이 여러 개면 필드 또는 파라미터 이름으로 빈 이름을 매칭한다.
    //@Component, @Service, @Repository 등의 어노테이션을 사용하여 스프링이 관리하는 빈으로 등록을 한 객체를 주입받을 떄 @Autowired를 사용합니다.
    //아래 @TestConfiguration에서 MemberRepositoryV3 타입을 보고 매칭 하게 된 것.
    @Autowired
    private MemberServiceV3_3 memberService;        //얘네 둘 @Autowired만 하면 스프링 빈으로 등록을 아직 전혀 안했다. 그래서 의존관계 주입 자체를 못받는다. 그래서 아래 빈으로 등록 해준것.

    //JUnit5 에선 필드주입은 되지만 생성자를 통한 의존성 주입이 안된다.
    // https://pinokio0702.tistory.com/189 참고

    @TestConfiguration      // 테스트 클래스 내부에서 스프링 부트가 자동으로 만들어주는 빈에 추가로 스프링 빈들을 생성해서 등록한다.
    static class TestConfig{
        @Bean
        DataSource dataSource(){
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager(){
            return new DataSourceTransactionManager(dataSource());
        }

        //위에 MemberRepositoryV3 과 MemberServiceV3_3 을 빈으로 등록해준것.
        @Bean
        MemberRepositoryV3 memberRepositoryV3(){
            return new MemberRepositoryV3(dataSource());        //dataSource() 주입받아서 return 함.
        }


        @Bean
        MemberServiceV3_3 memberServiceV3_3(){
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }
    //스프링 빈에 dataSource와 transactionManager가 등록됨 그럼 프록시가 가져다 쓴다.
    //트랜잭션 AOP는 스프링 빈에 등록된 dataSource 말고도, transactionManager를 찾아서 사용하기 때문에 transactionManager도 스프링 빈으로 등록해야 한다.
    //트랜잭션 매니저는 트랜잭션 프록시에서 트랜잭션 시작시 필요하다.
    //dataSource는 트랜잭션 매니저에서도 필요하고 리포지토리에서도 필요하다.
    //트랜잭션 AOP를 제공하려면 필요하다.




    @Test       //프록시가 적용이 됬나?
    void AopCheck(){
        log.info("memberService class ={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        /*
        MemberServiceV3_3 에 클래스 단위로든, 메소드 단위로든 @Transactional 이 적용되면 프록시가 생성된다.
        스프링이 코드를 쫙 보고 클래스나 메소드 @Transactioanl이 있으면 넌 AOP 적용 대상이구나 하고 프록시를 만들어서 적용을 해준다.
        MemberService 에 찍힌 클래스를 보면 $$EnhancerBySpringCGLIB$$5b98cd9c가 찍혀있다. 이러면 스프링 빈에 proxy가 들어가 있는거다. 우리가 위에서 MemberServiceV3_3 의존관계 주입을 받았는데 실제 서비스(MemberServiceV3_3)를 받는게 아닌 스프링 빈으로 등록된 TransactionProxy code부분을 받게된다.
        프록시를 도입하면 스프링이 서비스 로직을 상속을 받아서 강의자료에 있는 TransactionProxy같은 코드를 만들어낸다.
        주입받은건 실제 우리의 순수 MemberServiceV3_3이 아니고 MemberServiceV3_3을 상속받은 트랜잭션 프록시 코드다. 이 안에는 트랜잭션을 처리하는 로직을 가지고 있다. target.logic() 부분은 우리의 실제 서비스의 로직을(accountTansfer() ) 호출한다.

        2023-03-02 10:04:52.915  INFO 16252 --- [           main] h.jdbc.service.MemberServiceV3_3Test     : memberService class =class hello.jdbc.service.MemberServiceV3_3$$EnhancerBySpringCGLIB$$5b98cd9c
        2023-03-02 10:04:52.916  INFO 16252 --- [           main] h.jdbc.service.MemberServiceV3_3Test     : memberRepository class=class hello.jdbc.repository.MemberRepositoryV3
         */

        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();

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
        Member MEMBER_EX = new Member(MemberServiceV3_3Test.MEMBER_EX, 10000);          // MEMBER_EX로 수정
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

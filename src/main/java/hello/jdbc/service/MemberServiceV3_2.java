package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
//@RequiredArgsConstructor
public class MemberServiceV3_2 {


    private final MemberRepositoryV3 memberRepository;
    //private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;

    //생성자 만들자 위에 어노테이션 주석처리

    //TransactionTemplate txTemplate을 주입받는게 아니라 PlatformTransactionManager를 주입받는다.
    //트랜잭션 템플릿을 쓰려면 트랜잭션 매니저가 필요하다.  이 패턴으로 많이 쓴다.
    //트랜잭션 템플릿 클래스 참고.
    public MemberServiceV3_2(MemberRepositoryV3 memberRepository, PlatformTransactionManager transactionManager) {
        this.memberRepository = memberRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);      //트랜잭션 매니저를 주입받고 내부에선 트랜잭션 템플릿을 쓴다.
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        //트랜잭션 템플릿을 써보자.  트랜잭션 매니저 관련 로직을 트랜잭션 템플릿으로 사용할 수 있다.
        txTemplate.executeWithoutResult((status)->{
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        /**
         * 위에 코드 설명, executeWithoutResult 이 코드 안에서 트랜잭션 시작하고, try문의 비지니스 로직을 수행한다.
         * 이 코드가 끝났을때 이 로직이 성공적으로 반환이 되면  executeWithoutResult이 코드 안에서 커밋 , 예외가 터진다 -> 롤백한다.
         */


    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        //계좌이체니까 일단 내돈을 깎아야지  from의 돈을 깎고 to의 돈을 올리는것
        memberRepository.update(fromId, fromMember.getMoney() - money);     // 1번 업데이트
        //중간에 오류케이스도 한번 만들어봤다.
        validation(toMember);           //1번 업데이트 후 검증에 문제 생기면 2번으로 못 넘어감.

        memberRepository.update(toId, toMember.getMoney() + money);     // 2번 업데이트
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외가 발생");
        }
    }
}

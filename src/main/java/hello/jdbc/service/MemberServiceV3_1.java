package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final MemberRepositoryV3 memberRepository;
    private final PlatformTransactionManager transactionManager;

    //private final DataSource dataSource;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        //트랜잭션 시작.
        //반환된 TransactionStatus는 커밋이나 롤백 할때 넣어줘야함.
        // V3_1Test에서 구현체인 DataSourceTransactionManager를 통해서 트랜잭션을 획득한다. 트랜잭션 매니저는 데이터소스를 알고 있다.  데이터소스를 통해서 커넥션을 생성, 셋오토커밋 펄스도 하고, 트랜잭션 동기화 매니저에 보관한다.
        //그 다음에 로직을 수행하는데 findById() 를 수행한다 하자. 커넥션 획득하는데,어디서 획득? DataSourceUtils.getConnection(dataSource)하면  리포지토리에서 트랜잭션 동기화 매니저의 트랜잭션 시작한 커넥션을 획득하고 로직을 수행한다.
        //로직을 다 수행하고 트랜잭션 매니저가 커밋이나 롤백을 하면 리소스를 다 릴리즈하고 트랜잭션 동기화 매니저에도 커넥션을 제거해준다.
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());//속성을 넣어주면 된다

        try{

            //비즈니스 로직  ( 커넥션도 같이 넘기자.
            Member fromMember = memberRepository.findById(fromId);              //con Argument 추가
            Member toMember = memberRepository.findById(toId);

            //계좌이체니까 일단 내돈을 깎아야지  from의 돈을 깎고 to의 돈을 올리는것
            memberRepository.update(fromId, fromMember.getMoney() - money);     // 1번 업데이트
            //중간에 오류케이스도 한번 만들어봤다.
            validation(toMember);           //1번 업데이트 후 검증에 문제 생기면 2번으로 못 넘어감.

            memberRepository.update(toId, toMember.getMoney() + money);     // 2번 업데이트

            //위에 로직이 정상적으로 수행되었다면,
            transactionManager.commit(status);  //성공 시, Commit 한다.
        }catch(Exception e){
            transactionManager.rollback(status);     //실패시 RollBack
            throw new IllegalStateException(e);     //예외를 던진다.
        }

    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외가 발생");
        }
    }
}

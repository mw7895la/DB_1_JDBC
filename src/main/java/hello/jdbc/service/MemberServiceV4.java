package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryEx;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 예외 누수 문제 해결
 * SQLException이 제거될 것.
 *
 * MemberRepository 인터페이스에 의존하는 것으로 바꾸자.
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepository memberRepository;



    //생성자 만들자 위에 어노테이션 주석처리

    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;

    }
    @Transactional      //이 어노테이션을 걸면, 스프링이 트랜잭션을 적용하는 프록시를 만들어준다 (AOP 프록시 여기서 트랜잭션 로직 다 처리). 이 메소드 호출될때 트랜잭션 걸고 시작하겠다. 성공하면 커밋 예외터지면 롤백 하겠다.
    public void accountTransfer(String fromId, String toId, int money)   {
        /**
         * 메서드에 throws SQLException 삭제.
         */

        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money)   {
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

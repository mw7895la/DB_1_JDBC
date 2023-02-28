package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

//@RequiredArgsConstructor
public class MemberServiceV1 {
    private final MemberRepositoryV1 memberRepository;

    @Autowired
    public MemberServiceV1(MemberRepositoryV1 memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // from에서 to에게 얼마 보낼꺼냐

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

package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터를 연동하고, 풀을 고려한 종료까지.
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final MemberRepositoryV2 memberRepository;
    private final DataSource dataSource;



/*    @Autowired
    public MemberServiceV2(MemberRepositoryV1 memberRepository) {
        this.memberRepository = memberRepository;
    }*/

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // from에서 to에게 얼마 보낼꺼냐
        //트랜잭션 시작.
        Connection con = dataSource.getConnection();
        log.info("conn ={}",con);
        try{
            con.setAutoCommit(false);       //트랜잭션의 시작. 우리가 H2DB에서 실제 했던것.

            //비즈니스 로직  ( 커넥션도 같이 넘기자.
            Member fromMember = memberRepository.findById(con,fromId);              //con Argument 추가
            Member toMember = memberRepository.findById(con,toId);

            //계좌이체니까 일단 내돈을 깎아야지  from의 돈을 깎고 to의 돈을 올리는것
            memberRepository.update(con,fromId, fromMember.getMoney() - money);     // 1번 업데이트
            //중간에 오류케이스도 한번 만들어봤다.
            validation(toMember);           //1번 업데이트 후 검증에 문제 생기면 2번으로 못 넘어감.

            memberRepository.update(con,toId, toMember.getMoney() + money);     // 2번 업데이트

            //위에 로직이 정상적으로 수행되었다면,
            con.commit();  //성공 시, Commit 한다.
        }catch(Exception e){
            con.rollback();     //실패시 RollBack
            throw new IllegalStateException(e);     //예외를 던진다.
        }finally{
            // 릴리즈 해줘야 함.
            if(con != null){
                try{
                    con.setAutoCommit(true);        //커넥션 풀 고려
                    con.close();        //커넥션을 종료하거나 풀에 반환하는 메서드, 여기선 풀에 반환.
                }catch(Exception e){
                    log.info("error",e);
                }
            }
        }



    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외가 발생");
        }
    }
}

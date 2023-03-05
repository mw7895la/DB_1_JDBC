package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import javax.sql.DataSource;
/**
 * JDBC Template 사용 !
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{


    private final JdbcTemplate template;

    @Autowired
    public MemberRepositoryV5(DataSource dataSource) {      //주입을 받았다.
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member)  {
        String sql = "insert into member(member_id,money) values(?,?)";
        template.update(sql, member.getMemberId(), member.getMoney());      //sql과 안에 필요한 파라미터를 넘기면 됨.
        return member;
        /**
         * 이 jdbctemplate이 아래 과정 커넥션 받아오고 실행하고 예외변환까지 다 해준다.
         */
        /*Connection con = null;      //Connection이 있어야 연결을 하지
        PreparedStatement pstmt = null;     //DB에 쿼리를 날린다.      //PreparedStatement 인터페이스다 이것의 부모는 Statement다 // ?를 통한 파라미터 바인딩을 가능하게 해줌.
        try{
            con = getConnection();      //DriverManager를 통해서 커넥션을 획득하게 된다.
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,member.getMemberId());  //여기는 파라미터 바인딩을 하는 곳 위의 (?, ?)  첫번째 자리의 index는 1
            pstmt.setInt(2, member.getMoney());     // 2번째 자리의 index는 2
            pstmt.executeUpdate();      // 위에 준비된게 DB에 실행이 된다. 얘가 숫자를 반환하는데  영향받은 row 수만큼 반환해준다.
            return member; //반환 타입이 ember 리턴.
        }catch (SQLException e){

            DataAccessException ex = exTranslator.translate("save", sql, e);
            throw ex;
            //이것으로 인해 스프링이 제공하는 방대한 예외 계층으로 다 바뀐다.
        }finally{

            close(con,pstmt,null);
        }*/
    }

    @Override
    public Member findById(String memberId)  {
        String sql = "select * from member where member_id = ?";
        Member member = template.queryForObject(sql, memberRowMapper(), memberId);//한 건 조회는 queryForObject()를 사용한다.
        //sql 결과가 resultSet이 나오면 이걸 던져서 아래 메소드로 부터 결과가 반환이된다.
        return member;

    }

    private RowMapper<Member> memberRowMapper() {
        //rs 는 resultSet (반환은 ResultSet 이다. select 쿼리의 결과를 담고 있는 통.) Member클래스는 인스턴스 필드가 2개.
        //rowNum은 몇번째 로우인지 번호가 들어옴.
        return (rs,rowNum)->{
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money)  {
        String sql = "update member set money=? where member_id=?";

        template.update(sql, money, memberId);
    }
    @Override
    public void delete(String memberId)   {
        String sql = "delete from member where member_id=?";

        template.update(sql, memberId);
    }

    /**
     * JdbcTemplate를 쓰면 닫는것도 안해도되고, 커넥션 동기화도 안해도 된다.
     */
}

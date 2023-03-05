package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{


    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;      //스프링 예외 변환을 위한 인터페이스다.

    @Autowired
    public MemberRepositoryV4_2(DataSource dataSource) {      //주입을 받았다.
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
        // SQLExceptionTranslator의 구현체 SQLErrorCodeSQLExceptionTranslator
        // dataSource를 넣어주는 이유는 어떤 디비를 쓰는지 이런 정보도 찾아서 쓰기 때문.
        // 예외를 잡아서 스프링의 예외 계층으로 변환해주는 작업을 한다. (강의자료 21장 )
    }

    @Override
    public Member save(Member member)  {
        String sql = "insert into member(member_id,money) values(?,?)";

        Connection con = null;      //Connection이 있어야 연결을 하지
        PreparedStatement pstmt = null;     //DB에 쿼리를 날린다.      //PreparedStatement 인터페이스다 이것의 부모는 Statement다 // ?를 통한 파라미터 바인딩을 가능하게 해줌.
        try{
            con = getConnection();      //DriverManager를 통해서 커넥션을 획득하게 된다.
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,member.getMemberId());  //여기는 파라미터 바인딩을 하는 곳 위의 (?, ?)  첫번째 자리의 index는 1
            pstmt.setInt(2, member.getMoney());     // 2번째 자리의 index는 2
            pstmt.executeUpdate();      // 위에 준비된게 DB에 실행이 된다. 얘가 숫자를 반환하는데  영향받은 row 수만큼 반환해준다.
            return member; //반환 타입이 Member라서 리턴.
        }catch (SQLException e){
            /**
             * 변환부분
             */
            DataAccessException ex = exTranslator.translate("save", sql, e);
            throw ex;
            //이것으로 인해 스프링이 제공하는 방대한 예외 계층으로 다 바뀐다.
        }finally{

            close(con,pstmt,null);
        }
    }

    @Override
    public Member findById(String memberId)  {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs  =null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1,memberId);
            rs = pstmt.executeQuery();//executeUpdate 는 뭔가 테이블에 변경이 있을 insert delete update 같은것에 쓰고  단순 조회는 executeQuery()를 쓴다.
            //반환은 ResultSet 이다. select 쿼리의 결과를 담고 있는 통.

            if(rs.next()){      //처음에 cursor는 데이터를 가리키지 않다가 rs.next() 해줘야 첫번째 데이터를 가리킨다. 여기는 조회라서 조회는 어짜피 1개만 조회하는거라 if문을 썼다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        }catch(SQLException e){
            DataAccessException ex = exTranslator.translate("findById", sql, e);
            throw ex;
        }finally{
            close(con,pstmt,rs);
        }
    }
    @Override
    public void update(String memberId, int money)  {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,money);  //여기는 파라미터 바인딩을 하는 곳 위의 (?, ?)  첫번째 자리의 index는 1
            pstmt.setString(2, memberId);     // 2번째 자리의 index는 2
            int resultSize=pstmt.executeUpdate();      // 위에 준비된게 DB에 실행이 된다. 얘가 숫자를 반환하는데  영향받은 row 수만큼 반환해준다.
            log.info("resultSize ={}", resultSize);
        }catch (SQLException e){
            DataAccessException ex = exTranslator.translate("update", sql, e);
            throw ex;
        }finally{
            close(con,pstmt,null);
        }
    }
    @Override
    public void delete(String memberId)   {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);     // 2번째 자리의 index는 2
            int resultSize=pstmt.executeUpdate();      // 위에 준비된게 DB에 실행이 된다. 얘가 숫자를 반환하는데  영향받은 row 수만큼 반환해준다.
            log.info("resultSize ={}", resultSize);
        }catch (SQLException e){
            DataAccessException ex = exTranslator.translate("delete", sql, e);
            throw ex;
        }finally{
            close(con,pstmt,null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {      //항상 역순으로 close ()
        
        //기존 코드를 대체 내부를 봐라.
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        //주의!! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다. -> 트랜잭션 동기화 매니저가 관리하는 커넥션이면 닫지않고 유지해준다. 관리 안하는 커넥션이면 닫아버린다.
        DataSourceUtils.releaseConnection(con,dataSource);

        //JdbcUtils.closeConnection(con);     //이렇게 하면 커넥션을 팍 꺼버린다. DB랑 연결이 다 끝나버린다. 세션도 사라지고..

    }

    private  Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        //트랜잭션 동기화 매니저를 통해 보관된 커넥션을 얻는다.
        //트랜잭션 동기화 매니저가 보관하는 커넥션이 있으면 그걸 반환, 없으면 생성해서 반환


        log.info("get connection ={} class={}", con, con.getClass());
        return con;
    }
}

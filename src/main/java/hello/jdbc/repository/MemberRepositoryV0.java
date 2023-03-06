package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용해서 "저장"해보자.
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id,money) values(?,?)";

        Connection con = null;      //Connection이 있어야 연결을 하지
        PreparedStatement pstmt = null;     //DB에 쿼리를 날린다.
        // PreparedStatement 인터페이스다 이것의 부모는 Statement다 // ?를 통한 파라미터 바인딩을 가능하게 해줌.
        // statement를 상속받는 인터페이스로 SQL구문을 실행시키는 기능을 갖는 객체
        try{
            con = getConnection();      //DriverManager를 통해서 커넥션을 획득하게 된다.
            pstmt = con.prepareStatement(sql);  // PreparedStatement 객체 생성시 인자값으로 실행할 sql문을 지정해야 한다.
            pstmt.setString(1,member.getMemberId());  //여기는 파라미터 바인딩을 하는 곳 위의 (?, ?)  첫번째 자리의 index는 1
            pstmt.setInt(2, member.getMoney());     // 2번째 자리의 index는 2
            pstmt.executeUpdate();      // 위에 준비된게 DB에 실행이 된다. 얘가 숫자를 반환하는데  영향받은 row 수만큼 반환해준다.
            return member; //반환 타입이 Member라서 리턴.
        }catch (SQLException e){
            log.error("db error", e);
            throw e;
        }finally{
            //여기서 사용한것을 close 해줘야 해. 시작과 역순으로 클로즈 해라.
            //안닫으면, 지금 외부 리소스를 쓰는건데. 잘못하면 계속 떠다니면서 연결이 안끊어지고 유지가 된다.  리소스 누수로 인해 커넥션 부족으로 장애 발생할 수 있음.

            //pstmt.close();      //근데 만약에 close에서  Exception이 터지면? 호출이 안되는 문제가 발생할 수 있다. 그래서 아래 close() 로 따로 뺴놨다.
            //con.close();
            close(con,pstmt,null);
        }
    }

    public Member findById(String memberId) throws SQLException {
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
            log.error("db error",e);
            throw e;
        }finally{
            close(con,pstmt,rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
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
            log.error("db error", e);
            throw e;
        }finally{
            close(con,pstmt,null);
        }
    }

    public void delete(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
        }finally{
            close(con,pstmt,null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {      //항상 역순으로 close ()
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("Error", e);
            }
        }
        if(stmt != null){
            try {
                stmt.close();       // SQLException이 터지면?? 터진다 하더라도 catch로 잡아버린다. 그럼 여기서 끝나기 때문에 con에 영향을 주지 않는다.
            } catch (SQLException e) {
                log.info("Error", e);
            }
        }
        if(con!=null){
            try {
                con.close();
            } catch (SQLException e) {
                log.info("Error", e);
            }
        }

    }

    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}

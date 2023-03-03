package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

public interface MemberRepositoryEx {
    Member save(Member member);
    // throws SQLException; // 이게 throws되어있어야 구현 클래스에서도 예외를 던질수 있다. 근데 이제 인터페이스가 JDBC기술에 종속적이게 되었다.

    Member findById(String memberId);

    void update(String memberId, int money);

    void delete(String memberId);
}

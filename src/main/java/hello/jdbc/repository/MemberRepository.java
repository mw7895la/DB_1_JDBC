package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    // throws SQLException; // 이게 throws되어있어야 구현 클래스에서도 예외를 던질수 있다. 근데 이렇게 하면, 인터페이스가 JDBC기술에 종속적이게 된다.

    Member findById(String memberId);

    void update(String memberId, int money);

    void delete(String memberId);
}

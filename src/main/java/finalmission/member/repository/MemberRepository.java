package finalmission.member.repository;

import finalmission.member.domain.Member;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @SQL("""
        SELECT  *
        FROM member m
        WHERE m.email = :email AND m.password = :password
        """)
    public Optional<Member> findByEmailAndPassword(String email, String password);
}

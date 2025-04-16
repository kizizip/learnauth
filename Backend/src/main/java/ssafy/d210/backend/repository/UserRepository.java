package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByEmail(String email);
    // 강의 등록 시 orElseThrow 사용 위해 Optional 사용
    Optional<User> findOptionalUserByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}

package org.example.usersvc.infrastructure.repository;

import org.example.usersvc.domain.entity.RoleEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsById(Long id);

    @Query(
            value = """
                    delete from users.user_roles where role_id = :roleId
                    """,
            nativeQuery = true
    )
    @Modifying
    void deleteUserRolesByRoleId(Long roleId);
}

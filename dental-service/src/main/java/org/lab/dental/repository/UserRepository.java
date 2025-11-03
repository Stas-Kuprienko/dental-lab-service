package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.users SET login = :login WHERE id = :id", nativeQuery = true)
    void updateLogin(@Param("id") UUID id, @Param("login") String name);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.users SET name = :name WHERE id = :id", nativeQuery = true)
    void updateName(@Param("id") UUID id, @Param("name") String name);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.users SET status = :status WHERE id = :id", nativeQuery = true)
    void updateStatus(@Param("id") UUID id, @Param("status") String status);
}

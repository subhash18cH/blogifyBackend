package com.subhash.backend.repository;

import com.subhash.backend.model.Userr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Userr,Integer> {

    Optional<Userr> findByUserName(String username);
    boolean existsByUserName(String userName);
    Optional<Userr> findByEmail(String email);
}

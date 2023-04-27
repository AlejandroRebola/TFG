package org.thirty.blogapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thirty.blogapp.model.Authority;

import java.util.Optional;


public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Optional<Authority> findByAuthority(String authority);

}


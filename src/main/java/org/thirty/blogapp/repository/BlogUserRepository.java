package org.thirty.blogapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thirty.blogapp.model.BlogUser;

import java.util.Optional;

public interface BlogUserRepository extends JpaRepository<BlogUser, Long> {

    Optional<BlogUser> findByUsername(String username);

}

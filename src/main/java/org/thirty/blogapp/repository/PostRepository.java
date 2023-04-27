package org.thirty.blogapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thirty.blogapp.model.Post;

import java.util.Collection;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Collection<Post> findAllByOrderByCreationDateDesc();

    Optional<Post> findById(Long id);
}

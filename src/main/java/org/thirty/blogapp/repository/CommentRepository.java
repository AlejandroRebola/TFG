package org.thirty.blogapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thirty.blogapp.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}

package org.thirty.blogapp.service;

import org.thirty.blogapp.model.Comment;

public interface CommentService {

    Comment save(Comment comment);

    void delete(Comment comment);

}

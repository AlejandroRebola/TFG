package org.thirty.blogapp.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.thirty.blogapp.model.BlogUser;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;

public interface BlogUserService extends UserDetailsService {

    Optional<BlogUser> findByUsername(String username);

    BlogUser saveNewBlogUser(BlogUser blogUser) throws RoleNotFoundException;

}

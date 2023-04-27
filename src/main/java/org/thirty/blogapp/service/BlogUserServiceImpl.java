package org.thirty.blogapp.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thirty.blogapp.model.Authority;
import org.thirty.blogapp.model.BlogUser;
import org.thirty.blogapp.repository.AuthorityRepository;
import org.thirty.blogapp.repository.BlogUserRepository;

import javax.management.relation.RoleNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class BlogUserServiceImpl implements BlogUserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private final BCryptPasswordEncoder bcryptEncoder;
    private final BlogUserRepository blogUserRepository;
    private final AuthorityRepository authorityRepository;

    public BlogUserServiceImpl(BCryptPasswordEncoder bcryptEncoder, BlogUserRepository blogUserRepository, AuthorityRepository authorityRepository) {
        this.bcryptEncoder = bcryptEncoder;
        this.blogUserRepository = blogUserRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<BlogUser> blogUser = blogUserRepository.findByUsername(username);
        if (blogUser.isPresent()) {
            return blogUser.get();
        } else {
            throw new UsernameNotFoundException("No se ha encontrado username " + username);
        }
    }

    @Override
    public Optional<BlogUser> findByUsername(String username) {
        return blogUserRepository.findByUsername(username);
    }

    @Override
    public BlogUser saveNewBlogUser(BlogUser blogUser) throws RoleNotFoundException {
        System.err.println("saveNewBlogUser: " + blogUser);  // Para test
        // Con password encoder
//        blogUser.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(blogUser.getPassword()).substring(8));
        blogUser.setPassword(this.bcryptEncoder.encode(blogUser.getPassword())); // explicit bcrypt encoder so better approach ?
        // Cuenta habilitada por defecto
        blogUser.setEnabled(true);
        // Autoridad/rol predeterminados para el nuevo usuario del blog
        Optional<Authority> optionalAuthority = this.authorityRepository.findByAuthority(DEFAULT_ROLE);
        System.err.println("optionalAuthority: " + optionalAuthority);  // Para test
        if (optionalAuthority.isPresent()) {
            Authority authority = optionalAuthority.get();
            Collection<Authority> authorities = Collections.singletonList(authority);
            blogUser.setAuthorities(authorities);
            System.err.println("blogUser after Roles: " + blogUser);  // Para test
            return this.blogUserRepository.saveAndFlush(blogUser);
        } else {
            throw new RoleNotFoundException("Default role no encontrado en el blog con el username " + blogUser.getUsername());
        }
    }
}

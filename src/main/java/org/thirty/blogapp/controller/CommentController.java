package org.thirty.blogapp.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thirty.blogapp.model.BlogUser;
import org.thirty.blogapp.model.Comment;
import org.thirty.blogapp.model.Post;
import org.thirty.blogapp.service.BlogUserService;
import org.thirty.blogapp.service.CommentService;
import org.thirty.blogapp.service.PostService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@Controller
@SessionAttributes("comment")
public class CommentController {

    private final PostService postService;
    private final BlogUserService blogUserService;
    private final CommentService commentService;

    public CommentController(PostService postService, BlogUserService blogUserService, CommentService commentService) {
        this.postService = postService;
        this.blogUserService = blogUserService;
        this.commentService = commentService;
    }

    @Secured("ROLE_USER")
    @GetMapping("/comment/{id}")
    public String showComment(@PathVariable Long id, Model model, Principal principal) {

        // Just curious  what if we get username from Principal instead of SecurityContext
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }
        // the end of curiosity //

//        // Nombre de usuario de la session actual
//        String authUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // Encontrar por usuario
        Optional<BlogUser> optionalBlogUser = this.blogUserService.findByUsername(authUsername);
        // Encontrar post por id
        Optional<Post> postOptional = this.postService.getById(id);
        // si ambas opciones están presentes, configure el usuario y publique en un nuevo comentario y coloque el anterior en el modelo
        if (postOptional.isPresent() && optionalBlogUser.isPresent()) {
            Comment comment = new Comment();
            comment.setPost(postOptional.get());
            comment.setUser(optionalBlogUser.get());
            
            model.addAttribute("comment", comment);
            System.err.println("GET comentario/{id}: " + comment + "/" + id); // Para test
            return "commentForm";
        } else {
            System.err.println("No se ha podido encontrar post por el id : " + id + " o por usuario loggueado : " + authUsername); // Para test
            return "error";
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/comment")
    public String validateComment(@Valid @ModelAttribute Comment comment, BindingResult bindingResult, SessionStatus sessionStatus) {
        System.err.println("POST comentario: " + comment); // Para test
        if (bindingResult.hasErrors()) {
            System.err.println("Comentario no validado");
            return "commentForm";
        } else {
            this.commentService.save(comment);
            System.err.println("SAVE comentario: " + comment); // Para test
            sessionStatus.setComplete();
            return "redirect:/post/" + comment.getPostId();
        }
    }

}

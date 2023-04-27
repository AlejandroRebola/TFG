package org.thirty.blogapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thirty.blogapp.model.Post;
import org.thirty.blogapp.service.PostService;

import java.util.Collection;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/home")
    public String displayAllPosts(Model model) {

        Collection<Post> posts = this.postService.getAll();
        model.addAttribute("posts", posts);

        return "home";
    }

}

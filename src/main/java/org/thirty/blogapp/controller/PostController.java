package org.thirty.blogapp.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thirty.blogapp.model.BlogUser;
import org.thirty.blogapp.model.Post;
import org.thirty.blogapp.service.BlogUserService;
import org.thirty.blogapp.service.PostService;
import org.springframework.http.MediaType;

import javax.validation.Valid;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Controller
@SessionAttributes("post")
public class PostController {

    private final PostService postService;
    private final BlogUserService blogUserService;

    public PostController(PostService postService, BlogUserService blogUserService) {
        this.postService = postService;
        this.blogUserService = blogUserService;
    }
    
    @GetMapping("/post/image/{postId}")
    @ResponseBody
    public ResponseEntity<byte[]> getImageForPost(@PathVariable Long postId) {
        Optional<Post> optionalPost = postService.getById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            byte[] imageBytes = post.getImage();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(MediaType.IMAGE_JPEG_VALUE));
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/post/{id}")
    public String getPost(@PathVariable Long id, Model model, Principal principal) {

        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }

        // Busca por ID
        Optional<Post> optionalPost = this.postService.getById(id);
        // Si existe lo pone en el modelo.
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);
            if (authUsername.equals(post.getUser().getUsername())) {
                model.addAttribute("isOwner", true);
            }
            return "post";
        } else {
            return "404";
        }
    }
    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("image") MultipartFile file, @RequestParam("postId") Long postId, RedirectAttributes redirectAttributes) {
        try {
            // Comprueba si el archivo está vacío
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Selecciona imagen para subir.");
                return "redirect:/post/" + postId;
            }

            // Comprueba si el archivo es una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("message", "No es una imagen lo que estas intentando subir.");
                return "redirect:/post/" + postId;
            }

            // Define los tamaños mínimo y máximo en bytes
            long minSize = 1024; // 1 KB
            long maxSize = 2048576; // 2 MB

            // Comprueba si el tamaño de la imagen está dentro del rango permitido
            long imageSize = file.getSize();
            if (imageSize < minSize || imageSize > maxSize) {
                redirectAttributes.addFlashAttribute("message", "La imagen debe tener un tamaño entre 1 KB y 2 MB.");
                return "redirect:/post/" + postId;
            }

            // Obtiene el post por postId
            Optional<Post> optionalPost = postService.getById(postId);
            if (!optionalPost.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "El post no se ha encontrado.");
                return "redirect:/post/" + postId;
            }
            Post post = optionalPost.get();

            // Asigna la imagen al post y guarda el post
            post.setImage(file.getBytes());
            postService.save(post);

            redirectAttributes.addFlashAttribute("message", "La imagen se ha subido correctamente");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "La subida de la imagen ha fallado");
        }

        return "redirect:/post/" + postId;
    }
    @Secured("ROLE_USER")
    @GetMapping("/createNewPost")
    public String createNewPost(Model model, Principal principal) {

        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }

        // Busca al usuario por el username
        Optional<BlogUser> optionalBlogUser = this.blogUserService.findByUsername(authUsername);
        // añade el usuario al post
        if (optionalBlogUser.isPresent()) {
            Post post = new Post();
            post.setUser(optionalBlogUser.get());
            model.addAttribute("post", post);
            return "postForm";
        } else {
            return "error";
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/createNewPost")
    public String createNewPost(@Valid @ModelAttribute Post post, BindingResult bindingResult, SessionStatus sessionStatus) {
        System.err.println("POST post: " + post);
        if (bindingResult.hasErrors()) {
            System.err.println("Post no validado");
            return "postForm";
        }
        // Aqui guarda el post si todo ha sido correcto.
        this.postService.save(post);
        System.err.println("SAVE post: " + post);
        sessionStatus.setComplete();
        return "redirect:/post/" + post.getId();
    }

    @Secured("ROLE_USER")
    @GetMapping("editPost/{id}")
    public String editPost(@PathVariable Long id, Model model, Principal principal) {
       
        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }

        // Busqueda del post por id.
        Optional<Post> optionalPost = this.postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (authUsername.equals(post.getUser().getUsername())) {
                model.addAttribute("post", post);
                System.err.println("EDIT post: " + post);
                return "postForm";
            } else {
                System.err.println("Este usuario no tiene permisos para la edicion: " + id); 
                return "403";
            }
        } else {
            System.err.println("No se ha podido encontrar un post con esta ID: " + id); 
            return "error";
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/deletePost/{id}")
    public String deletePost(@PathVariable Long id, Principal principal) {

        String authUsername = "anonymousUser";
        if (principal != null) {
            authUsername = principal.getName();
        }


        // Busca el post por ID
        Optional<Post> optionalPost = this.postService.getById(id);
        // Comprueba si el usuario esta loggeado para darke kis derechos de modificacion. 
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            // Comprueba si el usuario logeado es el dueño.
            if (authUsername.equals(post.getUser().getUsername())) {
                // Si no lo es borra el post de la bbdd.
                this.postService.delete(post);
                System.err.println("DELETED post: " + post);
                return "redirect:/";
            } else {
                System.err.println("El usuario actual no tiene permisos para editar nada en la publicación de: " + id);
                return "403";
            }
        } else {
            System.err.println("No se ha podido encontrar post por el id: " + id); 
            return "error";
        }
    }

}

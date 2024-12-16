package com.subhash.backend.controller;

import com.subhash.backend.dtos.BlogDto;
import com.subhash.backend.model.Blog;
import com.subhash.backend.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BlogController {

    @Autowired
    BlogService blogService;

    @GetMapping("/blogs/all")
    public List<Blog> getAllBlogs(){
        return blogService.findAllBlogs();
    }

    @GetMapping("/blogs")
    public List<Blog> getBlog(@AuthenticationPrincipal UserDetails userDetails){
        String username=userDetails.getUsername();
        return blogService.getBlog(username);
    }

    @GetMapping("/blogs/{blogId}")
    public Blog getBlogById(@PathVariable Integer blogId){
        return blogService.getBlogById(blogId);
    }

    @PostMapping("/blogs")
    public Blog createBlog(@AuthenticationPrincipal UserDetails userDetails, @RequestBody BlogDto blogDto){
        String username=userDetails.getUsername();
        return blogService.createBlog(username,blogDto);
    }

    @PutMapping("/blogs/{blogId}")
    public Blog updateBlog(@AuthenticationPrincipal UserDetails userDetails, @RequestBody String content, @PathVariable Integer blogId){
        String username=userDetails.getUsername();
        return blogService.updateBlog(username,content,blogId);
    }

    @DeleteMapping("/blogs/{blogId}")
    public String deleteBlog(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer blogId){
        String username=userDetails.getUsername();
        return blogService.deleteBlog(username,blogId);
    }
}


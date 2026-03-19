package com.project.revconnect.controller;


import com.project.revconnect.dto.PostResponseDTO;
import com.project.revconnect.model.Post;
import com.project.revconnect.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/revconnect/users", "/posts"})
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/addPost")
    public ResponseEntity<PostResponseDTO> save(@RequestBody Post post) {
        return postService.addPost(post);
    }

    @GetMapping({"/getAllposts", "/myPosts"})
    public ResponseEntity<List<PostResponseDTO>> getMyPosts() {
        return postService.findMyPosts();
    }

    @GetMapping("/posts/user/{username}")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(@PathVariable String username) {
        return postService.findPostsByUsername(username);
    }

    @GetMapping("/posts/tagged/{username}")
    public ResponseEntity<List<PostResponseDTO>> getTaggedPosts(@PathVariable String username) {
        return postService.findTaggedPostsByUsername(username);
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long postId,
            @RequestBody Post post) {
        return postService.updatePost(postId, post);
    }

    @DeleteMapping({"/posts/{postId}", "/delete/{postId}", "/{postId}"})
    public ResponseEntity<PostResponseDTO> deletePost(
            @PathVariable Long postId) {
        return postService.deletePost(postId);
    }

    @PutMapping("/posts/{postId}/pin")
    public ResponseEntity<PostResponseDTO> pinPost(@PathVariable Long postId) {
        return postService.pinPost(postId);
    }

    @PutMapping("/posts/{postId}/unpin")
    public ResponseEntity<PostResponseDTO> unpinPost(@PathVariable Long postId) {
        return postService.unpinPost(postId);
    }

    @GetMapping("/posts/collab/pending")
    public ResponseEntity<List<PostResponseDTO>> getPendingCollabPosts() {
        return postService.getPendingCollabPosts();
    }

    @PutMapping("/posts/{postId}/collab/accept")
    public ResponseEntity<PostResponseDTO> acceptCollabPost(@PathVariable Long postId) {
        return postService.acceptCollabPost(postId);
    }

    @PutMapping("/posts/{postId}/collab/reject")
    public ResponseEntity<PostResponseDTO> rejectCollabPost(@PathVariable Long postId) {
        return postService.rejectCollabPost(postId);
    }

    @PutMapping("/posts/{postId}/collab/remove")
    public ResponseEntity<PostResponseDTO> removeSelfFromCollab(@PathVariable Long postId) {
        return postService.removeSelfFromCollab(postId);
    }

    @GetMapping("/posts/series/me")
    public ResponseEntity<Map<String, List<PostResponseDTO>>> getMyVideoSeries() {
        return postService.getMyVideoSeries();
    }

    @DeleteMapping("/posts/deleteAll")
    public ResponseEntity<List<PostResponseDTO>> deleteAllPosts() {
        return postService.deleteAllPost();
    }
}

package com.project.revconnect.controller;

import com.project.revconnect.dto.CommentRequestDTO;
import com.project.revconnect.dto.CommentResponseDTO;
import com.project.revconnect.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDTO request) {

        CommentResponseDTO response = commentService.addComment(postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {

        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }
}

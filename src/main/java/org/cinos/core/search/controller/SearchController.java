package org.cinos.core.search.controller;

import lombok.RequiredArgsConstructor;
import org.cinos.core.search.dto.SearchResultDTO;
import org.cinos.core.search.service.impl.SearchService;
import org.cinos.core.posts.dto.PostDTO;
import org.cinos.core.posts.service.IPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final IPostService postService;

    @GetMapping("/users")
    public ResponseEntity<List<SearchResultDTO>> searchUsers(@RequestParam final String q) {
        List<SearchResultDTO> results = searchService.searchUsers(q);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam final String q) {
        List<PostDTO> results = postService.searchPosts(q);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<List<SearchResultDTO>> search(@RequestParam final String q) {
        List<SearchResultDTO> results = searchService.search(q);
        return ResponseEntity.ok(results);
    }

}

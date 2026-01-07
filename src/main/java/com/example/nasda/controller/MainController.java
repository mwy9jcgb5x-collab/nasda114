package com.example.nasda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(Model model) {
        // 1. 가짜 게시물 데이터 생성 (화면 확인용)
        List<PostDto> posts = new ArrayList<>();
        posts.add(new PostDto(1L, "첫 번째 영감", "멋진 디자인입니다.", "디자인", "user1", Arrays.asList("https://via.placeholder.com/300")));
        posts.add(new PostDto(2L, "맛있는 요리", "건강한 레시피 공유해요.", "음식", "user2", Arrays.asList("https://via.placeholder.com/300")));

        // 2. HTML로 데이터 전달
        model.addAttribute("posts", posts);
        model.addAttribute("username", "모아나"); // 로그인한 사용자 이름 예시
        model.addAttribute("category", "전체"); // 현재 카테고리

        return "index"; // templates/index.html을 찾아감
    }

    // 데이터를 담을 임시 클래스 (나중에는 별도 파일로 분리하세요)
    static class PostDto {
        public Long id;
        public String title;
        public String description;
        public String category;
        public Author author;
        public List<String> images;

        public PostDto(Long id, String title, String description, String category, String username, List<String> images) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.author = new Author(username);
            this.images = images;
        }
    }

    static class Author {
        public String username;
        public Author(String username) { this.username = username; }
    }

}

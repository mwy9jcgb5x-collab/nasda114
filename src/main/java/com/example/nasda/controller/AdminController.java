package com.example.nasda.controller;

import com.example.nasda.dto.manager.CategoryDTO;
import com.example.nasda.dto.manager.CommentReportDTO;
import com.example.nasda.dto.manager.ForbiddenWordDTO;
import com.example.nasda.dto.manager.PostReportDTO;
import com.example.nasda.service.manager.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequestMapping("/admin")
@Log4j2
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String adminMain(Model model,
                            @RequestParam(value = "section", defaultValue = "users") String section,
                            @RequestParam(value = "type", defaultValue = "post") String type,
                            @RequestParam(value = "postPage", defaultValue = "0") int postPage,
                            @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
                            @RequestParam(value = "wordPage", defaultValue = "0") int wordPage,
                            @RequestParam(value = "catPage", defaultValue = "0") int catPage,
                            // 🔍 검색 파라미터 추가
                            @RequestParam(value = "wordKeyword", required = false) String wordKeyword,
                            @RequestParam(value = "catKeyword", required = false) String catKeyword) {

        log.info("대시보드 실행 - 검색어: word={}, cat={}", wordKeyword, catKeyword);

        try {
            model.addAttribute("section", section);
            model.addAttribute("type", type);
            // 검색어 유지용으로 다시 전달
            model.addAttribute("wordKeyword", wordKeyword);
            model.addAttribute("catKeyword", catKeyword);

            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());
            Pageable wordPageable = PageRequest.of(wordPage, 10, Sort.by("wordId").descending());
            Pageable catPageable = PageRequest.of(catPage, 10, Sort.by("categoryId").descending());

            model.addAttribute("userList", adminService.getUserStatusList());

            // 1. 신고 목록
            Page<PostReportDTO> postReportPage = adminService.getPendingPostReports(postPageable);
            model.addAttribute("postReportList", postReportPage.getContent());
            model.addAttribute("postTotalPages", postReportPage.getTotalPages());
            model.addAttribute("postCurrentPage", postReportPage.getNumber());

            Page<CommentReportDTO> commentReportPage = adminService.getPendingCommentReports(commentPageable);
            model.addAttribute("commentReportList", commentReportPage.getContent());
            model.addAttribute("commentTotalPages", commentReportPage.getTotalPages());
            model.addAttribute("commentCurrentPage", commentReportPage.getNumber());

            // 2. 금지어 목록 (검색 로직 추가)
            Page<ForbiddenWordDTO> wordPageResult;
            if (wordKeyword != null && !wordKeyword.isEmpty()) {
                // 서비스에 searchBannedWords 메서드가 있다고 가정
                wordPageResult = adminService.searchBannedWords(wordKeyword, wordPageable);
            } else {
                wordPageResult = adminService.getBannedWords(wordPageable);
            }
            model.addAttribute("wordList", wordPageResult.getContent());
            model.addAttribute("wordCurrentPage", wordPageResult.getNumber());
            model.addAttribute("wordTotalPages", wordPageResult.getTotalPages());

            // 3. 카테고리 목록 (검색 로직 추가)
            Page<CategoryDTO> catPageResult;
            if (catKeyword != null && !catKeyword.isEmpty()) {
                // 서비스에 searchCategories 메서드가 있다고 가정
                catPageResult = adminService.searchCategories(catKeyword, catPageable);
            } else {
                catPageResult = adminService.getCategories(catPageable);
            }
            model.addAttribute("categoryList", catPageResult.getContent());
            model.addAttribute("catCurrentPage", catPageResult.getNumber());
            model.addAttribute("catTotalPages", catPageResult.getTotalPages());

        } catch (Exception e) {
            log.error("데이터 로딩 오류: " + e.getMessage());
            // 에러 시 빈 리스트 처리 로직 유지
        }
        return "admin/dashboard";
    }

    @PostMapping("/report/process")
    public String processReport(@RequestParam("reportId") Integer reportId,
                                @RequestParam("action") String action,
                                @RequestParam("type") String type,
                                RedirectAttributes rttr) {
        adminService.processPostReport(reportId, action, "관리자 승인 처리");
        rttr.addAttribute("section", "reports");
        rttr.addAttribute("type", type);
        rttr.addFlashAttribute("result", "processed");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/register")
    public String registerGET(@RequestParam(value = "type", required = false, defaultValue = "word") String type, Model model) {
        model.addAttribute("type", type);
        return "admin/register";
    }

    @PostMapping("/register")
    public String registerPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        String section = "category".equals(type) ? "categories" : "banned";
        try {
            if ("category".equals(type)) {
                adminService.registerCategory(categoryDTO);
            } else if ("word".equals(type)) {
                adminService.registerWord(wordDTO);
            }
            rttr.addFlashAttribute("result", "success");
        } catch (RuntimeException e) {
            log.error("등록 중 중복 발생: " + e.getMessage());
            rttr.addFlashAttribute("error", e.getMessage());
        }
        rttr.addAttribute("section", section);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/modify")
    public String modifyGET(@RequestParam(value = "type", required = false) String type, @RequestParam(value = "id", required = false) Integer id, Model model) {
        if (type == null || id == null) return "redirect:/admin/dashboard";
        model.addAttribute("type", type);
        if ("category".equals(type)) model.addAttribute("dto", adminService.readOneCategory(id));
        else if ("word".equals(type)) model.addAttribute("dto", adminService.readOneWord(id));
        return "admin/modify";
    }

    @PostMapping("/modify")
    public String modifyPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        String section = "";
        if ("category".equals(type)) {
            adminService.modifyCategory(categoryDTO);
            section = "categories";
        } else if ("word".equals(type)) {
            adminService.modifyWord(wordDTO);
            section = "banned";
        }
        rttr.addAttribute("section", section);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("type") String type, @RequestParam("id") Integer id, RedirectAttributes rttr) {
        String section = "";
        if ("word".equals(type)) {
            adminService.removeWord(id);
            section = "banned";
        } else if ("category".equals(type)) {
            adminService.removeCategory(id);
            section = "categories";
        }
        rttr.addAttribute("section", section);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/user-check")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> checkUserStatus() {
        return adminService.getUserStatusList();
    }
}
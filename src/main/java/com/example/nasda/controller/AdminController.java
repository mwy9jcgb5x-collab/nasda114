package com.example.nasda.controller;

import com.example.nasda.dto.manager.CategoryDTO;
import com.example.nasda.dto.manager.CommentReportDTO;
import com.example.nasda.dto.manager.ForbiddenWordDTO;
import com.example.nasda.dto.manager.PostReportDTO;
import com.example.nasda.service.manager.AdminService;
import jakarta.servlet.http.HttpSession;
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

//    @GetMapping("/dashboard")
//    public String adminMain(Model model,
//                            @RequestParam(value = "section", defaultValue = "accounts") String section,
//                            @RequestParam(value = "type", defaultValue = "post") String type,
//                            @RequestParam(value = "postPage", defaultValue = "0") int postPage,
//                            @RequestParam(value = "commentPage", defaultValue = "0") int commentPage){
//        log.info("대시보드 실행 - 섹션: {}, 타입: {}", section, type);
//
//        try {
//            model.addAttribute("section", section);
//            model.addAttribute("type", type);
//
//            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
//            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());
//
//            model.addAttribute("categoryList", adminService.getAllCategories());
//            model.addAttribute("wordList", adminService.getAllWords());
//            model.addAttribute("userList", adminService.getUserStatusList());
//
//            Page<PostReportDTO> postReportPage = adminService.getPendingPostReports(postPageable);
//            model.addAttribute("postReportList", postReportPage.getContent());
//            model.addAttribute("postCurrentPage", postReportPage.getNumber());
//            model.addAttribute("postTotalPages", postReportPage.getTotalPages());
//
//            Page<CommentReportDTO> commentReportPage = adminService.getPendingCommentReports(commentPageable);
//            model.addAttribute("commentReportList", commentReportPage.getContent());
//            model.addAttribute("commentCurrentPage", commentReportPage.getNumber());
//            model.addAttribute("commentTotalPages", commentReportPage.getTotalPages());
//
//        } catch (Exception e) {
//            log.error("데이터 로딩 중 오류 발생: " + e.getMessage());
//            model.addAttribute("categoryList", Collections.emptyList());
//            model.addAttribute("wordList", Collections.emptyList());
//            model.addAttribute("postReportList", Collections.emptyList());
//            model.addAttribute("commentReportList", Collections.emptyList());
//            model.addAttribute("userList", Collections.emptyList());
//        }
//
//        return "admin/dashboard";
//    }

    @GetMapping("/dashboard")
    public String adminMain(Model model,
                            HttpSession session, // 🚩 세션 확인을 위해 추가
                            @RequestParam(value = "section", defaultValue = "users") String section, // HTML ID와 맞춤
                            @RequestParam(value = "type", defaultValue = "post") String type,
                            @RequestParam(value = "postPage", defaultValue = "0") int postPage,
                            @RequestParam(value = "commentPage", defaultValue = "0") int commentPage){

        // 🚩 [핵심] 팀원들이 만든 세션 키("loginUser")로 유저 정보를 가져옵니다.
        com.example.nasda.domain.UserEntity loginUser = (com.example.nasda.domain.UserEntity) session.getAttribute("loginUser");

        // 관리자가 아니면 메인으로 튕겨내기 (보안 및 403 에러 방지)
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole().name())) {
            log.warn("관리자 권한 없음 - 메인 페이지로 리다이렉트");
            return "redirect:/";
        }

        log.info("대시보드 실행 - 섹션: {}, 타입: {}", section, type);

        try {
            // 탭 유지를 위한 값 전달
            model.addAttribute("section", section);
            model.addAttribute("type", type);

            // 페이징 및 데이터 로딩 (기존 로직 유지)
//            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
//            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());

            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
            // PageRequest.of(현재페이지, 페이지당개수, 정렬기준)
// 두 번째 인자가 10이어야 10개씩 끊어서 가져옵니다.
            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());

            model.addAttribute("categoryList", adminService.getAllCategories());
            model.addAttribute("wordList", adminService.getAllWords());
            model.addAttribute("userList", adminService.getUserStatusList());

            Page<PostReportDTO> postReportPage = adminService.getPendingPostReports(postPageable);
            model.addAttribute("postReportList", postReportPage.getContent());
            model.addAttribute("postCurrentPage", postReportPage.getNumber());
            model.addAttribute("postTotalPages", postReportPage.getTotalPages());

            Page<CommentReportDTO> commentReportPage = adminService.getPendingCommentReports(commentPageable);
            model.addAttribute("commentReportList", commentReportPage.getContent());
            model.addAttribute("commentCurrentPage", commentReportPage.getNumber());
            model.addAttribute("commentTotalPages", commentReportPage.getTotalPages());

        } catch (Exception e) {
            log.error("데이터 로딩 중 오류 발생: " + e.getMessage());
            model.addAttribute("categoryList", Collections.emptyList());
            model.addAttribute("wordList", Collections.emptyList());
            model.addAttribute("postReportList", Collections.emptyList());
            model.addAttribute("commentReportList", Collections.emptyList());
            model.addAttribute("userList", Collections.emptyList());
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

    // 🚩 [수정] 등록 처리 시 중복 예외(RuntimeException) 캐치 로직 추가
//    @PostMapping("/register")
//    public String registerPost(@RequestParam("type") String type,
//                               CategoryDTO categoryDTO,
//                               ForbiddenWordDTO wordDTO,
//                               RedirectAttributes rttr) {
//        try {
//            if ("category".equals(type)) {
//                adminService.registerCategory(categoryDTO);
//                rttr.addAttribute("section", "categories");
//            } else if ("word".equals(type)) {
//                adminService.registerWord(wordDTO);
//                rttr.addAttribute("section", "banned");
//            }
//            rttr.addFlashAttribute("result", "success");
//        } catch (RuntimeException e) {
//            // 🚩 핵심: Service에서 던진 "이미 존재하는..." 메시지를 화면으로 전달
//            log.error("등록 중 중복 발생: " + e.getMessage());
//            rttr.addFlashAttribute("error", e.getMessage());
//            rttr.addAttribute("section", "category".equals(type) ? "categories" : "banned");
//        }
//        return "redirect:/admin/dashboard";
//    }

    @PostMapping("/register")
    public String registerPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        // 🚩 먼저 어디로 돌아갈지 정해둡니다.
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
            rttr.addFlashAttribute("error", e.getMessage()); // [3번 기능] 중복 메시지 유지
        }

        // 🚩 이 section 값이 주소창에 ?section=categories 처럼 붙어서 튕김을 방지합니다.
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

//    @PostMapping("/modify")
//    public String modifyPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
//        if ("category".equals(type)) {
//            adminService.modifyCategory(categoryDTO);
//            rttr.addAttribute("section", "categories");
//        } else if ("word".equals(type)) {
//            adminService.modifyWord(wordDTO);
//            rttr.addAttribute("section", "banned");
//        }
//        return "redirect:/admin/dashboard";
//    }

    @PostMapping("/modify")
    public String modifyPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        String section = "";
        if ("category".equals(type)) {
            adminService.modifyCategory(categoryDTO);
            section = "categories"; // 카테고리 탭으로
        } else if ("word".equals(type)) {
            adminService.modifyWord(wordDTO);
            section = "banned";     // 금지어 탭으로
        }

        rttr.addAttribute("section", section);
        return "redirect:/admin/dashboard";
    }

//    @GetMapping("/delete")
//    public String delete(@RequestParam("type") String type, @RequestParam("id") Integer id, RedirectAttributes rttr) {
//        if ("word".equals(type)) {
//            adminService.removeWord(id);
//            rttr.addAttribute("section", "banned");
//        } else if ("category".equals(type)) {
//            adminService.removeCategory(id);
//            rttr.addAttribute("section", "categories");
//        }
//        return "redirect:/admin/dashboard";
//    }

    @GetMapping("/delete")
    public String delete(@RequestParam("type") String type, @RequestParam("id") Integer id, RedirectAttributes rttr) {
        String section = "";
        if ("word".equals(type)) {
            adminService.removeWord(id);
            section = "banned";     // 삭제 후 금지어 탭 유지
        } else if ("category".equals(type)) {
            adminService.removeCategory(id);
            section = "categories"; // 삭제 후 카테고리 탭 유지
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
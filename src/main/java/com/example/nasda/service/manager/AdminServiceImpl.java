package com.example.nasda.service.manager;

import com.example.nasda.domain.*;
import com.example.nasda.dto.*;
import com.example.nasda.dto.manager.CategoryDTO;
import com.example.nasda.dto.manager.CommentReportDTO;
import com.example.nasda.dto.manager.ForbiddenWordDTO;
import com.example.nasda.dto.manager.PostReportDTO;
import com.example.nasda.repository.*;
import com.example.nasda.repository.manager.CommentReportRepository;
import com.example.nasda.repository.manager.ForbiddenWordRepository;
import com.example.nasda.repository.manager.NotificationRepository;
import com.example.nasda.repository.manager.PostReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class AdminServiceImpl implements AdminService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ForbiddenWordRepository wordRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    // 1. 관리자 권한 확인
    @Override
    public boolean isAdmin(String userId) {
        UserEntity user = userRepository.findById(Integer.parseInt(userId)).orElseThrow();
        return user.getRole() == UserRole.ADMIN;
    }

    // 2. 신고 목록 조회 (페이징 적용)
    @Override
    public Page<PostReportDTO> getPendingPostReports(Pageable pageable) {
        log.info("게시글 신고 페이징 조회 중...");
        return postReportRepository.findAll(pageable)
                .map(report -> modelMapper.map(report, PostReportDTO.class));
    }

    @Override
    public Page<CommentReportDTO> getPendingCommentReports(Pageable pageable) {
        log.info("댓글 신고 페이징 조회 중...");
        return commentReportRepository.findAll(pageable)
                .map(report -> modelMapper.map(report, CommentReportDTO.class));
    }

    // 3. 신고 처리 및 유저 정지 로직
    @Override
    public void processPostReport(Integer reportId, String action, String adminComment) {
        PostReportEntity report = postReportRepository.findById(reportId).orElseThrow();
        if ("APPROVE".equals(action)) {
            UserEntity writer = report.getPost().getUser();
            LocalDateTime suspensionEnd = LocalDateTime.now().plusDays(7);
            log.info("신고 승인: " + writer.getNickname() + " 7일 정지 예정 (" + suspensionEnd + ")");
            postRepository.delete(report.getPost());
        }
    }

    @Override
    public List<Map<String, Object>> getUserStatusList() {
        return userRepository.findAllUserStatusRaw();
    }

    @Override
    public void processCommentReport(Integer reportId, String action, String adminComment) {}

    // 4. 금지어 관리 (중복 체크 추가)
    @Override
    public List<ForbiddenWordDTO> getAllWords() {
        return wordRepository.findAll().stream()
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void registerWord(ForbiddenWordDTO wordDTO) {
        // 🚩 [수정] DB 저장 전 중복 검사 (existsByWord 사용)
        if (wordRepository.existsByWord(wordDTO.getWord())) {
            throw new RuntimeException("이미 등록된 금지어입니다."); // 컨트롤러의 catch문으로 전달됨
        }
        wordRepository.save(ForbiddenWordEntity.builder().word(wordDTO.getWord()).build());
    }

    @Override
    public void modifyWord(ForbiddenWordDTO dto) {
        wordRepository.findById(dto.getForbiddenwordId().intValue())
                .ifPresent(word -> wordRepository.save(ForbiddenWordEntity.builder()
                        .wordId(word.getWordId()).word(dto.getWord()).build()));
    }

    @Override
    public void removeWord(Integer id) {
        wordRepository.deleteById(id);
    }

    @Override
    public boolean checkForbiddenWords(String content) {
        return wordRepository.findAll().stream().anyMatch(w -> content.contains(w.getWord()));
    }

    // 5. 카테고리 관리 (중복 체크 추가)
    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(e -> modelMapper.map(e, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void registerCategory(CategoryDTO dto) {
        // 🚩 [수정] 카테고리 이름 중복 체크 (existsByCategoryName 사용)
        if (categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new RuntimeException("이미 존재하는 카테고리입니다.");
        }
        categoryRepository.save(modelMapper.map(dto, CategoryEntity.class));
    }

    @Override
    public void modifyCategory(CategoryDTO dto) {
        categoryRepository.findById(dto.getCategoryId()).orElseThrow();
        categoryRepository.save(CategoryEntity.builder()
                .categoryId(dto.getCategoryId()).categoryName(dto.getCategoryName()).isActive(true).build());
    }

    @Override
    public void removeCategory(Integer id) {
        postReportRepository.deleteByCategoryId(id);
        postRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
    }

    // 6. 단건 조회
    @Override
    public CategoryDTO readOneCategory(Integer id) {
        return categoryRepository.findById(id)
                .map(e -> modelMapper.map(e, CategoryDTO.class)).orElseThrow();
    }

    @Override
    public ForbiddenWordDTO readOneWord(Integer id) {
        return wordRepository.findById(id)
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId()).word(e.getWord()).build()).orElseThrow();
    }
}
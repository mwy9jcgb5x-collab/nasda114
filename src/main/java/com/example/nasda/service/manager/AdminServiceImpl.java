package com.example.nasda.service.manager;

import com.example.nasda.domain.*;
import com.example.nasda.dto.manager.CategoryDTO;
import com.example.nasda.dto.manager.CommentReportDTO;
import com.example.nasda.dto.manager.ForbiddenWordDTO;
import com.example.nasda.dto.manager.PostReportDTO;
import com.example.nasda.repository.CategoryRepository;
import com.example.nasda.repository.CommentRepository;
import com.example.nasda.repository.PostRepository;
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
    private final CommentRepository commentRepository;

    // 1. 관리자 권한 확인
    @Override
    public boolean isAdmin(
            String userId
    ) {
        UserEntity user = userRepository
                .findById(Integer.parseInt(userId))
                .orElseThrow();
        return user.getRole() == UserRole.ADMIN;
    }

    // 2. 신고 목록 조회 (페이징 적용)
    @Override
    public Page<PostReportDTO> getPendingPostReports(
            Pageable pageable
    ) {
        log.info("게시글 신고 페이징 조회 중...");
        return postReportRepository
                .findAll(pageable)
                .map(report -> modelMapper.map(report, PostReportDTO.class));
    }

    @Override
    public Page<CommentReportDTO> getPendingCommentReports(
            Pageable pageable
    ) {
        log.info("댓글 신고 페이징 조회 중...");
        return commentReportRepository
                .findAll(pageable)
                .map(report -> modelMapper.map(report, CommentReportDTO.class));
    }


    @Override
    @Transactional
    public void processPostReport(
            Integer reportId,
            String action,
            String reason
    ) {
        PostReportEntity report = postReportRepository
                .findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));

        if ("APPROVE".equals(action)) {
            PostEntity post = report.getPost();

            if (post != null) {
                log.info("게시글 삭제 시작: ID {}", post.getPostId());

                // 1. 해당 게시글에 달린 '모든 게시글 신고 내역' 삭제 (댓글 신고 지울 때랑 같은 원리)
                postReportRepository.deleteByPost(post);

                // 2. [필수] 게시글에 달린 이미지 먼저 삭제 (이미지가 있으면 post만 삭제 시 에러남)
                // PostRepository에 이 메서드가 이미 있으시죠?
                postRepository.deletePostImagesByPostId(post.getPostId());

                // 3. 원본 게시글 삭제 (댓글 삭제할 때 commentRepository.delete 쓴 거랑 똑같음!)
                postRepository.delete(post);
            }
            log.info("게시글 신고 승인 및 원본 삭제 완료");

        } else if ("REJECT".equals(action)) {
            NotificationEntity alarm = NotificationEntity
                    .builder()
                    .receiver(report.getReporter())
                    .message("신고하신 게시글 건이 반려되었습니다. 사유: " + reason)
                    .isRead(false)
                    .build();
            notificationRepository.save(alarm);

            postReportRepository.delete(report);
        }
        postReportRepository.flush();
    }


    @Override
    public List<Map<String, Object>> getUserStatusList() {
        return userRepository.findAllUserStatusRaw();
    }


    @Override
    @Transactional
    public void processCommentReport(Integer reportId, String action, String reason) {
        CommentReportEntity report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));

        if ("APPROVE".equals(action)) {
            CommentEntity comment = report.getComment();

            if (comment != null) {
                log.info("댓글 삭제 시작: ID {}", comment.getCommentId());

                // 1순위: 해당 댓글에 달린 '모든 댓글 신고 내역' 삭제 (나 자신 포함)
                commentReportRepository.deleteByComment(comment);

                // 2순위: 원본 댓글 삭제
                commentRepository.delete(comment);
            }
            log.info("댓글 신고 승인 및 원본 삭제 완료");

        } else if ("REJECT".equals(action)) {
            // 반려 로직 (기존 유지)
            NotificationEntity alarm = NotificationEntity.builder()
                    .receiver(report.getReporter())
                    .message("신고하신 댓글 건이 반려되었습니다. 사유: " + reason)
                    .isRead(false)
                    .build();
            notificationRepository.save(alarm);

            commentReportRepository.delete(report);
        }
        commentReportRepository.flush();
    }

    // 4. 금지어 관리
    @Override
    public Page<ForbiddenWordDTO> getBannedWords(
            Pageable pageable
    ) {
        return wordRepository
                .findAll(pageable)
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build());
    }

    @Override
    public Page<ForbiddenWordDTO> searchBannedWords(
            String keyword,
            Pageable pageable
    ) {
        log.info("금지어 검색 중...");
        return wordRepository
                .findByWordContaining(keyword, pageable)
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build());
    }

    @Override
    public List<ForbiddenWordDTO> getAllWords() {
        return wordRepository
                .findAll()
                .stream()
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void registerWord(
            ForbiddenWordDTO wordDTO
    ) {
        if (wordRepository.existsByWord(wordDTO.getWord())) {
            throw new RuntimeException("이미 등록된 금지어입니다.");
        }
        wordRepository.save(
                ForbiddenWordEntity.builder()
                        .word(wordDTO.getWord())
                        .build()
        );
    }

    @Override
    public void modifyWord(
            ForbiddenWordDTO dto
    ) {
        wordRepository
                .findById(dto.getForbiddenwordId().intValue())
                .ifPresent(word -> wordRepository.save(
                        ForbiddenWordEntity.builder()
                                .wordId(word.getWordId())
                                .word(dto.getWord())
                                .build()
                ));
    }

    @Override
    public void removeWord(
            Integer id
    ) {
        wordRepository.deleteById(id);
    }

    @Override
    public boolean checkForbiddenWords(
            String content
    ) {
        return wordRepository
                .findAll()
                .stream()
                .anyMatch(w -> content.contains(w.getWord()));
    }

    // 5. 카테고리 관리
    @Override
    public Page<CategoryDTO> getCategories(
            Pageable pageable
    ) {
        return categoryRepository
                .findAll(pageable)
                .map(e -> modelMapper.map(e, CategoryDTO.class));
    }

    @Override
    public Page<CategoryDTO> searchCategories(
            String keyword,
            Pageable pageable
    ) {
        log.info("카테고리 검색 중...");
        return categoryRepository
                .findByCategoryNameContaining(keyword, pageable)
                .map(e -> modelMapper.map(e, CategoryDTO.class));
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository
                .findAll()
                .stream()
                .map(e -> modelMapper.map(e, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void registerCategory(
            CategoryDTO dto
    ) {
        if (categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new RuntimeException("이미 존재하는 카테고리입니다.");
        }
        categoryRepository.save(
                modelMapper.map(dto, CategoryEntity.class)
        );
    }

    @Override
    public void modifyCategory(
            CategoryDTO dto
    ) {
        categoryRepository
                .findById(dto.getCategoryId())
                .orElseThrow();
        categoryRepository.save(
                CategoryEntity.builder()
                        .categoryId(dto.getCategoryId())
                        .categoryName(dto.getCategoryName())
                        .isActive(true)
                        .build()
        );
    }

    @Override
    public void removeCategory(
            Integer id
    ) {
        log.info("카테고리 삭제 시작...");

        postRepository.deletePostImagesByCategoryId(id);
        postReportRepository.deleteByCategoryId(id);
        postRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);

        log.info("카테고리 삭제 완료");
    }

    // 6. 단건 조회
    @Override
    public CategoryDTO readOneCategory(
            Integer id
    ) {
        return categoryRepository
                .findById(id)
                .map(e -> modelMapper.map(e, CategoryDTO.class))
                .orElseThrow();
    }

    @Override
    public void processPostReport(Integer reportId, String action) {

    }

    @Override
    public void processCommentReport(Integer reportId, String action) {

    }

    @Override
    public ForbiddenWordDTO readOneWord(
            Integer id
    ) {
        return wordRepository
                .findById(id)
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build())
                .orElseThrow();
    }
}
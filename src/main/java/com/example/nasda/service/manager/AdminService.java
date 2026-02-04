package com.example.nasda.service.manager;

import com.example.nasda.dto.manager.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface AdminService {

    // 관리자 확인
    boolean isAdmin(String userId);

    // 계정 상태 목록
    List<Map<String, Object>> getUserStatusList();

    // 게시글 신고 페이징 조회
    Page<PostReportDTO> getPendingPostReports(Pageable pageable);

    // [수정] 게시글 신고 처리 (사유 포함)
    void processPostReport(
            Integer reportId,
            String action,
            String reason
    );

    // 댓글 신고 페이징 조회
    Page<CommentReportDTO> getPendingCommentReports(Pageable pageable);

    // [수정] 댓글 신고 처리 (사유 포함)
    void processCommentReport(
            Integer reportId,
            String action,
            String reason
    );

    // 금지어 관련
    Page<ForbiddenWordDTO> getBannedWords(Pageable pageable);
    Page<ForbiddenWordDTO> searchBannedWords(String keyword, Pageable pageable);
    List<ForbiddenWordDTO> getAllWords();
    void registerWord(ForbiddenWordDTO wordDTO);
    void modifyWord(ForbiddenWordDTO dto);
    void removeWord(Integer id);
    ForbiddenWordDTO readOneWord(Integer id);
    boolean checkForbiddenWords(String content);

    // 카테고리 관련
    Page<CategoryDTO> getCategories(Pageable pageable);
    Page<CategoryDTO> searchCategories(String keyword, Pageable pageable);
    List<CategoryDTO> getAllCategories();
    void registerCategory(CategoryDTO dto);
    void modifyCategory(CategoryDTO dto);
    void removeCategory(Integer id);
    CategoryDTO readOneCategory(Integer id);

    // 오버로딩 (사유 없는 처리를 위해 남겨둠)
    void processPostReport(Integer reportId, String action);
    void processCommentReport(Integer reportId, String action);
}
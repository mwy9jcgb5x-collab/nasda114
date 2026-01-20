package com.example.nasda.service;

import com.example.nasda.domain.CategoryEntity;
import com.example.nasda.dto.manager.CategoryDTO;
import com.example.nasda.dto.manager.ForbiddenWordDTO;
import com.example.nasda.dto.manager.PostReportDTO;
import com.example.nasda.repository.CategoryRepository;
import com.example.nasda.repository.manager.ForbiddenWordRepository;
import com.example.nasda.service.manager.AdminService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Log4j2
public class AdminServiceTests {

    @Autowired
    private AdminService adminService;

    @Autowired // 🚩 이 줄이 없어서 빨간 줄이 떴던 거예요!
    private ForbiddenWordRepository forbiddenWordRepository;

    @Autowired // 🚩 직접 DB 저장을 위해 Repository를 주입합니다.
    private CategoryRepository categoryRepository;

    // 1. 카테고리 등록 테스트
    @Test
    public void testRegisterCategory() {
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .categoryName("테스트 카테고리")
                .build();

        adminService.registerCategory(categoryDTO);
        log.info("카테고리 등록 테스트 완료");
    }

    // 2. 금지어 등록 테스트
    @Test
    public void testRegisterWord() {
        ForbiddenWordDTO wordDTO = ForbiddenWordDTO.builder()
                .word("테스트금지어1")
                .build();

        adminService.registerWord(wordDTO);
        log.info("금지어 등록 테스트 완료");
    }

    // 3. 카테고리 수정 테스트
    @Test
    public void testModifyCategory() {
        // 1. 수정할 대상을 먼저 하나 등록 (그래야 번호를 알 수 있음)
        CategoryDTO registerDTO = CategoryDTO.builder()
                .categoryName("수정 전 이름")
                .build();

        // 등록 메서드를 실행 (이때 DB에 데이터가 들어감)
        adminService.registerCategory(registerDTO);

        // 2. DB에 방금 들어간 데이터 중 아무거나 하나를 가져와서 진짜 ID 확인
        // (findAll로 가져온 리스트의 첫 번째 녀석의 ID를 씁니다)
        CategoryEntity savedCategory = categoryRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("테스트용 데이터가 없습니다."));

        Integer realId = savedCategory.getCategoryId(); // 진짜 살아있는 번호!
        log.info("추출된 실제 ID: " + realId);

        // 3. 그 진짜 번호를 사용해서 수정을 진행
        CategoryDTO modifyDTO = CategoryDTO.builder()
                .categoryId(realId) // 🚩 수동으로 적은 1 대신 진짜 번호 대입!
                .categoryName("수정된 카테고리명")
                .build();

        adminService.modifyCategory(modifyDTO);
        log.info("카테고리 수정 테스트 최종 성공!");
    }
    // 4. 금지어 수정 테스트
    @Test
    public void testModifyWord() {
        // 1. 현재 시간을 숫자로 바꿔서 붙여줍니다. (절대 안 겹치게!)
        String uniqueWord = "금지어" + System.currentTimeMillis();

        ForbiddenWordDTO registerDTO = ForbiddenWordDTO.builder()
                .word(uniqueWord)
                .build();
        adminService.registerWord(registerDTO);

        // 2. DB에서 내가 방금 넣은 유니크한 단어로 찾아옵니다.
        var savedWord = forbiddenWordRepository.findAll().stream()
                .filter(w -> w.getWord().equals(uniqueWord))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("데이터가 없습니다."));

        Integer realFno = savedWord.getWordId();

        // 3. 수정 진행 (수정할 이름도 겹치지 않게 시간을 붙여줍니다)
        ForbiddenWordDTO modifyDTO = ForbiddenWordDTO.builder()
                .forbiddenwordId(realFno)
                .word("수정" + System.currentTimeMillis())
                .build();

        adminService.modifyWord(modifyDTO);
        log.info("금지어 수정 테스트 최종 성공! ID: " + realFno);
    }

    // 5. 기초 데이터 생성 (서비스 테스트를 돕기 위한 보조 테스트)
    @Test
    @Rollback(false) // 🚩 이 데이터를 DB에 진짜로 남기고 싶다면 추가!
    public void testInsertCategory() {
        CategoryEntity category = CategoryEntity.builder()
                .categoryName("기본 카테고리")
                .isActive(true)
                .build();

        categoryRepository.save(category);
        log.info("DB에 실제 데이터 저장 완료 (롤백 안 됨)");
    }

    @Test
    @DisplayName("신고 내역 페이징 데이터가 실제로 넘어오는지 확인")
    public void testGetPostReportsPaging() {
        // 1. 테스트 설정: 0페이지에서 10개, reportId 역순
        Pageable pageable = PageRequest.of(0, 10, Sort.by("reportId").descending());

        // 2. 서비스 실행
        Page<PostReportDTO> result = adminService.getPendingPostReports(pageable);

        // 3. 로그 출력 (이게 찍혀야 성공!)
        log.info("---------------------------------------");
        log.info("총 신고 수: " + result.getTotalElements());
        log.info("현재 페이지 데이터 수: " + result.getContent().size());
        log.info("전체 페이지 수: " + result.getTotalPages());
        log.info("---------------------------------------");

        // 만약 데이터가 있다면 첫 번째 신고 사유 확인
        if(!result.isEmpty()) {
            log.info("첫 번째 신고 사유: " + result.getContent().get(0).getReason());
        }
    }
    
}
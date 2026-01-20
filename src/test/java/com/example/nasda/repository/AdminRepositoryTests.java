package com.example.nasda.repository;

import com.example.nasda.domain.*;
import com.example.nasda.repository.manager.CommentReportRepository;
import com.example.nasda.repository.manager.ForbiddenWordRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
//@Transactional // ✅ 데이터 안 바꿔도 무한 재실행 가능하게 해주는 치트키
public class AdminRepositoryTests {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ForbiddenWordRepository forbiddenWordRepository;
    @Autowired private CommentReportRepository commentReportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;

    private UserEntity commonUser;
    private CategoryEntity commonCategory;

//    @BeforeEach
//    void setUp() {
//        // 1. 삭제 순서: 자식 테이블을 가장 먼저 삭제해야 합니다.
//        // 만약 금지어(ForbiddenWord)가 유저나 카테고리를 참조한다면 얘를 1번으로 지우세요.
//        forbiddenWordRepository.deleteAll();
//
//        // 2. 그 다음 부모 테이블 삭제
//        userRepository.deleteAll();
//        categoryRepository.deleteAll();
//
//        // 3. 데이터 다시 삽입
//        commonCategory = CategoryEntity.builder()
//                .categoryName("고정 카테고리")
//                .isActive(true)
//                .build();
//        categoryRepository.save(commonCategory);
//
//        commonUser = UserEntity.builder()
//                .nickname("관리자123")
//                .email("admin_fixed23@test.com")
//                .password("1233455")
//                .loginId("admin_fixed_id123")
//                .role(UserRole.ADMIN)
//                .status(UserStatus.ACTIVE)
//                .build();
//        userRepository.save(commonUser);
//    }

    @BeforeEach
    void setUp() {
        // 🚩 [수정] 삭제 순서: 자식(댓글/게시글)부터 지워야 부모(유저)를 지울 수 있습니다.
        commentRepository.deleteAll();
        postRepository.deleteAll();
        forbiddenWordRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // 🚩 데이터 다시 삽입 (기존 원본 코드 유지)
        commonCategory = CategoryEntity.builder()
                .categoryName("고정 카테고리")
                .isActive(true)
                .build();
        categoryRepository.save(commonCategory);

        commonUser = UserEntity.builder()
                .nickname("관리자1234")
                .email("admin_fixed234@test.com")
                .password("12334455")
                .loginId("admin_fixed_id1234")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(commonUser);
    }

    // 1. 카테고리 관리 (반복문)
    @Test
    void testCategory() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            categoryRepository.save(CategoryEntity.builder()
                    .categoryName("신규 카테고리_" + i)
                    .isActive(true)
                    .build());
        });
    }

    // 2. 금지어 관리 (반복문)
    @Test
    void testForbiddenWord() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            forbiddenWordRepository.save(ForbiddenWordEntity.builder()
                                                            .word("금지어_1" + i)
                                                            .word("금지어_22134")
                                                            .build());
        });
    }

    // 3. 유저 관리
    @Test
    void testUser() {
        UserEntity user = UserEntity.builder()
                .email("test_user@nasda.com").loginId("test_user").nickname("테스터").password("1234").build();
        userRepository.save(user);
    }


    // 4. 게시글(Post) 생성 테스트
    @Test
    @Rollback(false) // 👈 여기에 추가하세요! (org.springframework.test.annotation.Rollback 임포트)
    void testPost() {
        PostEntity post = PostEntity.builder()
                .title("화면 확인용 테스트 글") // 제목을 알아보기 쉽게 바꿨어요
                .user(commonUser)
                .category(commonCategory)
                .description("이 글이 보이면 성공입니다.")
                .viewCount(0)
                .isMain(false)
                .build();
        postRepository.save(post);

        log.info("생성된 게시글 번호(postId): " + post.getPostId());
    }

    // 5. 댓글(Comment/Reply) 생성
    // 5. 댓글(Comment/Reply) 생성
    @Test
    void testComment() {
        PostEntity post = PostEntity.builder().title("댓글용").user(commonUser).category(commonCategory).build();
        postRepository.save(post);

        // 🚩 수정: 빌더 대신 팀원들이 만든 create() 메서드 사용
        // 팀원들 엔티티 구조상 user 객체가 아니라 userId(Integer)를 직접 받습니다.
        CommentEntity comment = CommentEntity.create(post, commonUser.getUserId(), "댓글 테스트");

        commentRepository.save(comment);
    }

    // 6. 신고(Report) 생성
    @Test
    void testReport() {
        PostEntity post = PostEntity.builder().title("신고용").user(commonUser).category(commonCategory).build();
        postRepository.save(post);

        // 🚩 수정: 여기도 마찬가지로 빌더 대신 create() 사용
        CommentEntity comment = CommentEntity.create(post, commonUser.getUserId(), "신고대상");

        commentRepository.save(comment);

        CommentReportEntity report = CommentReportEntity.builder()
                .reason("부적절함")
                .status(ReportStatus.PENDING)
                .reporter(commonUser)
                .comment(comment)
                .build();
        commentReportRepository.save(report);
    }

    @Test
    public void testUpdate() {
        // 1. 수정 테스트를 위해 임시 데이터를 하나 먼저 저장합니다.
        CategoryEntity temp = CategoryEntity.builder()
                .categoryName("수정 전 이름")
                .isActive(true)
                .build();
        CategoryEntity saved = categoryRepository.save(temp); // DB가 번호를 새로 따줍니다.

        // 2. DB가 준 '진짜 번호'를 꺼냅니다.
        Integer realId = saved.getCategoryId();

        // 3. 그 번호를 그대로 사용해서 수정할 데이터를 만듭니다.
        CategoryEntity updateTarget = CategoryEntity.builder()
                .categoryId(realId) // 🚩 수동 번호(12) 대신 진짜 번호를 넣음!
                .categoryName("리포지토리에서 수정 성공")
                .isActive(true)
                .build();

        // 4. 저장 (JPA가 ID가 있는 것을 보고 Update 쿼리를 날립니다)
        categoryRepository.save(updateTarget);

        log.info("수정 완료된 ID: " + realId);
    }
}
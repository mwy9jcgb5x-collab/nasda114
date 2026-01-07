// ========== 댓글 관련 기능 ==========
document.addEventListener('DOMContentLoaded', function() {
    // 댓글 작성 폼 확인
    const commentForms = document.querySelectorAll('.comment-form form');
    commentForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const textarea = this.querySelector('textarea');
            if (textarea.value.trim() === '') {
                alert('댓글 내용을 입력해주세요.');
                e.preventDefault();
                return false;
            }
        });
    });

    // 댓글 삭제 확인
    const deleteForms = document.querySelectorAll('.btn-comment-delete');
    deleteForms.forEach(btn => {
        btn.closest('form').addEventListener('submit', function(e) {
            if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) {
                e.preventDefault();
                return false;
            }
        });
    });
});

// ========== 댓글 실시간 카운트 ==========
function updateCommentCount() {
    const commentItems = document.querySelectorAll('.comment-item');
    const commentCount = document.querySelector('.comments-section h3 span');
    if (commentCount) {
        commentCount.textContent = commentItems.length;
    }
}

// 페이지 로드 시 카운트 업데이트
document.addEventListener('DOMContentLoaded', updateCommentCount);
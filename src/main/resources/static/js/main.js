// ========== 모바일 검색 모달 ==========
function openSearchModal() {
    const modal = document.getElementById('searchModal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeSearchModal() {
    const modal = document.getElementById('searchModal');
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

// 모달 외부 클릭 시 닫기
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('searchModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeSearchModal();
            }
        });
    }

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeSearchModal();
        }
    });
});

// ========== Masonry 레이아웃 ==========
function initMasonryLayout() {
    const grid = document.getElementById('masonry-grid');
    if (!grid) return;

    const images = grid.querySelectorAll('img');
    images.forEach(img => {
        img.addEventListener('error', function() {
            this.src = 'https://via.placeholder.com/400x600?text=No+Image';
        });
    });
}

// ========== 페이지 초기화 ==========
document.addEventListener('DOMContentLoaded', function() {
    console.log('페이지 로드 완료');
    initMasonryLayout();

    // 로고 클릭 시 상단으로 스크롤
    const logo = document.querySelector('.logo');
    if (logo) {
        logo.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }
});

// ========== 윈도우 리사이즈 ==========
let resizeTimeout;
window.addEventListener('resize', function() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(initMasonryLayout, 250);
});
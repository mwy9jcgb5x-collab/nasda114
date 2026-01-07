// ========================================
// NASDA - 나의 스티커 다이어리
// 메인 JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {

    // ========== 검색 모달 관리 ==========
    const searchModal = document.getElementById('searchModal');
    const mobileSearchBtn = document.getElementById('mobileSearchBtn');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const searchInput = document.getElementById('searchInput');
    const mobileSearchInput = document.getElementById('mobileSearchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const clearMobileSearchBtn = document.getElementById('clearMobileSearchBtn');

    // 모바일 검색 모달 열기
    if (mobileSearchBtn) {
        mobileSearchBtn.addEventListener('click', function() {
            searchModal.classList.remove('hidden');
            setTimeout(() => {
                mobileSearchInput.focus();
            }, 100);
        });
    }

    // 모달 닫기
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeSearchModal);
    }

    // 모달 외부 클릭 시 닫기
    if (searchModal) {
        searchModal.addEventListener('click', function(e) {
            if (e.target === searchModal) {
                closeSearchModal();
            }
        });
    }

    function closeSearchModal() {
        searchModal.classList.add('hidden');
        if (mobileSearchInput) {
            mobileSearchInput.value = '';
        }
        hideSuggestions();
    }

    // 검색어 초기화
    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', function() {
            searchInput.value = '';
            this.classList.add('hidden');
            hideSuggestions();
        });
    }

    if (clearMobileSearchBtn) {
        clearMobileSearchBtn.addEventListener('click', function() {
            mobileSearchInput.value = '';
            this.classList.add('hidden');
            hideSuggestions();
        });
    }

    // 검색어 입력 감지
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            if (this.value.length > 0) {
                clearSearchBtn.classList.remove('hidden');
                showSuggestions(this.value);
            } else {
                clearSearchBtn.classList.add('hidden');
                hideSuggestions();
            }
        });

        searchInput.addEventListener('focus', function() {
            if (this.value.length > 0) {
                showSuggestions(this.value);
            }
        });
    }

    if (mobileSearchInput) {
        mobileSearchInput.addEventListener('input', function() {
            if (this.value.length > 0) {
                clearMobileSearchBtn.classList.remove('hidden');
                showMobileSuggestions(this.value);
            } else {
                clearMobileSearchBtn.classList.add('hidden');
                hideSuggestions();
            }
        });
    }

    // ========== 검색 제안 관리 ==========
    const suggestions = document.getElementById('searchSuggestions');
    const mobileSuggestions = document.getElementById('mobileSuggestions');

    const suggestionList = [
        '미니멀 디자인',
        '빈티지 인테리어',
        '자연 풍경',
        '모던 아트',
        '감성 사진',
        '북유럽 스타일',
        '카페 인테리어',
        '수채화'
    ];

    function showSuggestions(query) {
        if (!suggestions) return;

        const filtered = suggestionList.filter(item =>
            item.toLowerCase().includes(query.toLowerCase())
        );

        if (filtered.length > 0) {
            suggestions.innerHTML = filtered.map(item =>
                `<div class="search-suggestion-item" onclick="selectSuggestion('${item}')">${item}</div>`
            ).join('');
            suggestions.classList.remove('hidden');
        } else {
            hideSuggestions();
        }
    }

    function showMobileSuggestions(query) {
        if (!mobileSuggestions) return;

        const filtered = suggestionList.filter(item =>
            item.toLowerCase().includes(query.toLowerCase())
        );

        if (filtered.length > 0) {
            mobileSuggestions.innerHTML = filtered.map(item =>
                `<div class="search-modal-suggestion-item" onclick="selectMobileSuggestion('${item}')">${item}</div>`
            ).join('');
            mobileSuggestions.classList.remove('hidden');
        } else {
            hideSuggestions();
        }
    }

    function hideSuggestions() {
        if (suggestions) suggestions.classList.add('hidden');
        if (mobileSuggestions) mobileSuggestions.classList.add('hidden');
    }

    // 외부 클릭 시 제안 닫기
    document.addEventListener('click', function(e) {
        if (!searchInput || !searchInput.contains(e.target)) {
            if (suggestions && !suggestions.contains(e.target)) {
                hideSuggestions();
            }
        }
    });

    // ========== 드롭다운 메뉴 관리 ==========
    const userAvatarBtn = document.getElementById('userAvatarBtn');
    const dropdownMenu = document.getElementById('dropdownMenu');
    const chevronIcon = document.getElementById('chevronIcon');

    if (userAvatarBtn) {
        userAvatarBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            const isHidden = dropdownMenu.classList.contains('hidden');

            if (isHidden) {
                dropdownMenu.classList.remove('hidden');
                chevronIcon.classList.add('rotate');
            } else {
                dropdownMenu.classList.add('hidden');
                chevronIcon.classList.remove('rotate');
            }
        });
    }

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function(e) {
        if (dropdownMenu && !dropdownMenu.contains(e.target) && !userAvatarBtn.contains(e.target)) {
            dropdownMenu.classList.add('hidden');
            if (chevronIcon) chevronIcon.classList.remove('rotate');
        }
    });

    // ========== 카테고리 스크롤 ==========
    const categoryContainer = document.querySelector('.category-container');
    if (categoryContainer) {
        // 모바일에서 터치 스크롤 활성화
        let isDown = false;
        let startX;
        let scrollLeft;

        categoryContainer.addEventListener('mousedown', (e) => {
            isDown = true;
            startX = e.pageX - categoryContainer.offsetLeft;
            scrollLeft = categoryContainer.scrollLeft;
        });

        categoryContainer.addEventListener('mouseleave', () => {
            isDown = false;
        });

        categoryContainer.addEventListener('mouseup', () => {
            isDown = false;
        });

        categoryContainer.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - categoryContainer.offsetLeft;
            const walk = (x - startX) * 2;
            categoryContainer.scrollLeft = scrollLeft - walk;
        });
    }

    // ========== 플래시 메시지 자동 숨김 ==========
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.3s ease';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });

    // ========== 부드러운 스크롤 ==========
    const logoElement = document.querySelector('.logo');
    if (logoElement) {
        logoElement.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

});

// ========== 전역 함수 ==========
function selectSuggestion(text) {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = text;
        document.getElementById('searchForm').submit();
    }
}

function selectMobileSuggestion(text) {
    const mobileSearchInput = document.getElementById('mobileSearchInput');
    if (mobileSearchInput) {
        mobileSearchInput.value = text;
        document.getElementById('mobileSearchForm').submit();
    }
}
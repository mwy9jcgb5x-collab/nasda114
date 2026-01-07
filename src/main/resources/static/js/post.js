// ========== 이미지 미리보기 ==========
function previewImages(event) {
    const files = event.target.files;
    const preview = document.getElementById('imagePreview');
    preview.innerHTML = '';

    if (files.length === 0) return;

    Array.from(files).forEach(file => {
        if (!file.type.startsWith('image/')) return;

        const reader = new FileReader();
        reader.onload = function(e) {
            const div = document.createElement('div');
            div.className = 'preview-item';

            const img = document.createElement('img');
            img.src = e.target.result;

            div.appendChild(img);
            preview.appendChild(div);
        };
        reader.readAsDataURL(file);
    });
}

// ========== 폼 제출 전 확인 ==========
document.addEventListener('DOMContentLoaded', function() {
    const createForm = document.getElementById('createForm');
    const editForm = document.getElementById('editForm');

    if (createForm) {
        createForm.addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const category = document.getElementById('category').value;
            const images = document.getElementById('images').files;

            if (!title) {
                alert('제목을 입력해주세요.');
                e.preventDefault();
                return;
            }

            if (!category) {
                alert('카테고리를 선택해주세요.');
                e.preventDefault();
                return;
            }

            if (images.length === 0) {
                alert('이미지를 최소 1장 선택해주세요.');
                e.preventDefault();
                return;
            }

            // 이미지 크기 확인 (10MB)
            for (let i = 0; i < images.length; i++) {
                if (images[i].size > 10 * 1024 * 1024) {
                    alert('이미지 크기는 10MB를 초과할 수 없습니다.');
                    e.preventDefault();
                    return;
                }
            }
        });
    }

    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const category = document.getElementById('category').value;

            if (!title) {
                alert('제목을 입력해주세요.');
                e.preventDefault();
                return;
            }

            if (!category) {
                alert('카테고리를 선택해주세요.');
                e.preventDefault();
                return;
            }
        });
    }
});
package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.CategoryRequest;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.Category;
import com.chien.agricultural.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // Import này

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MinioService minioService; // 1. Inject MinioService

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrue();
    }

    // 2. Sửa hàm create để nhận thêm MultipartFile
    @Transactional
    public Category createCategory(CategoryRequest request, MultipartFile imageFile) {
        // Xử lý upload ảnh nếu có
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = minioService.uploadFile(imageFile);
        } else {
            // Nếu không gửi file, có thể lấy URL string từ request (nếu FE gửi link ảnh có sẵn)
            imageUrl = request.getImage();
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(toSlug(request.getName()))
                .description(request.getDescription())
                .image(imageUrl) // Lưu URL vừa upload
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }

    // 3. Sửa hàm update để nhận thêm MultipartFile
    @Transactional
    public Category updateCategory(String id, CategoryRequest request, MultipartFile imageFile) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy danh mục", HttpStatus.NOT_FOUND));

        category.setName(request.getName());
        category.setSlug(toSlug(request.getName()));
        category.setDescription(request.getDescription());

        // Chỉ cập nhật ảnh nếu có file mới được gửi lên
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = minioService.uploadFile(imageFile);
            category.setImage(newImageUrl);
        } else if (request.getImage() != null) {
            // Trường hợp muốn update bằng link ảnh string (ít dùng nhưng cứ để logic này)
            category.setImage(request.getImage());
        }

        return categoryRepository.save(category);
    }

    // ... (Giữ nguyên các hàm deleteCategory và toSlug) ...
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy danh mục", HttpStatus.NOT_FOUND));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
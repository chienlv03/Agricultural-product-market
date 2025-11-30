package com.chien.agricultural.dto.response;

import com.chien.agricultural.model.UserRole;
import com.chien.agricultural.model.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;
    private UserRole userRole;
    private UserStatus status;
}
package com.chien.agricultural.service;

import com.chien.agricultural.dto.response.AuthResponse;
import com.chien.agricultural.exception.AppException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    // Mật khẩu bí mật của hệ thống dành cho user OTP
    // Trong thực tế nên để biến môi trường phức tạp hơn
    private static final String SYSTEM_PASSWORD_PREFIX = "Agri_Secr3t_";

    public String createKeycloakUser(String phoneNumber, String roleName) {
        UsersResource usersResource = keycloakAdmin.realm(realm).users();

        // 1. Kiểm tra tồn tại
        if (!usersResource.searchByUsername(phoneNumber, true).isEmpty()) {
            throw new AppException("User đã tồn tại trên hệ thống định danh", HttpStatus.CONFLICT);
        }

        // 2. Chuẩn bị tạo User
        UserRepresentation user = new UserRepresentation();
        user.setUsername(phoneNumber);
        user.setEnabled(true);

        // Set Password ngầm
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(SYSTEM_PASSWORD_PREFIX + phoneNumber);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        // 3. Thực hiện tạo User
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new AppException("Lỗi tạo user Keycloak: " + response.getStatusInfo(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        // 4. Gán Role (Có cơ chế Rollback)
        RealmResource realmResource = keycloakAdmin.realm(realm);
        String kcRoleName = roleName.equals("SELLER") ? "ROLE_SELLER" : "ROLE_BUYER"; // Map đúng tên role trên Keycloak

        try {
            // Tìm Role trên Keycloak
            RoleRepresentation roleRep = realmResource.roles().get(kcRoleName).toRepresentation();

            // Thực hiện gán
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRep));

        } catch (Exception e) {
            log.error("Lỗi gán role {}, đang rollback xóa user {}", kcRoleName, userId);

            // --- ROLLBACK THỦ CÔNG: XÓA USER VỪA TẠO ---
            try {
                usersResource.get(userId).remove();
            } catch (Exception ex) {
                log.error("Lỗi nghiêm trọng: Không thể rollback xóa user {}. Cần xóa thủ công!", userId);
            }

            // Ném lỗi ra ngoài để Transaction bên AuthService cũng rollback (không lưu vào DB Postgres)
            throw new AppException("Không tìm thấy role " + kcRoleName + " trên Keycloak hoặc lỗi gán quyền. Đã hủy thao tác tạo User.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return userId;
    }

    public AuthResponse exchangeToken(String phoneNumber) {
        // Gọi API /token của Keycloak để lấy Access Token
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "password");
        map.add("username", phoneNumber);
        map.add("password", SYSTEM_PASSWORD_PREFIX + phoneNumber);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            return restTemplate.postForObject(tokenUrl, request, AuthResponse.class);
        } catch (Exception e) {
            throw new AppException("Lỗi lấy token: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void logout(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        String logoutUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            restTemplate.postForObject(logoutUrl, request, Object.class);
        } catch (Exception e) {
            log.error("Lỗi logout trên Keycloak: {}", e.getMessage());
        }
    }
}
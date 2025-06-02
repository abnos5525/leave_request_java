package ir.isiran.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import ir.isiran.project.util.HttpRequestUtil;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${auth.realm}")
    private String authRealm;
    @Value("${auth.server.url}")
    private String authServerUrl;
    @Value("${auth.client.id}")
    private String authClientId;
    @Value("${auth.client.secret}")
    private String authClientSecret;

    private final RestTemplate restTemplate;
    private final HttpRequestUtil httpRequestUtil;

    @GetMapping("/api/auth/user")
    public Map<String, Object> getUser(Authentication authentication) {
        return Map.of(
                "principal", authentication.getPrincipal(),
                "authorities", authentication.getAuthorities());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh an access token")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestParam("refresh_token") String refreshToken,
            HttpServletRequest request) {

        try {
            String url = authServerUrl + "realms/%s/protocol/openid-connect/token".formatted(authRealm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Add Basic Auth Header (client_id:client_secret)
            String auth = authClientId + ":" + authClientSecret;
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", refreshToken);
            formData.add("client_id", authClientId);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            // Use Map to accept any response from Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token", "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get access token")
    public ResponseEntity<?> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "grant_type", defaultValue = "password") String grantType,
            HttpServletRequest request) {
        try {
            String url = authServerUrl + "realms/%s/protocol/openid-connect/token".formatted(authRealm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = authClientId + ":" + authClientSecret;
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
            headers.set("X-Real-IP", httpRequestUtil.getIpAddress(request));

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", grantType);
            formData.add("username", username);
            formData.add("password", password);
            formData.add("client_id", authClientId);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "message", e.getMessage()));
        }
    }
}
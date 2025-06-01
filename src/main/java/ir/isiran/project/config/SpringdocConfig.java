package ir.isiran.project.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        name = "Basic",
        scheme = "basic")
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        name = "JWT",
        scheme = "bearer",
        bearerFormat = "JWT")
@SecurityScheme(
        type = SecuritySchemeType.OAUTH2,
        name = "OAuth2",
        flows = @OAuthFlows(
                password = @OAuthFlow(
                        tokenUrl = "${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID Connect"),
                                @OAuthScope(name = "profile", description = "Profile information"),
                                @OAuthScope(name = "email", description = "Email information"),
                                @OAuthScope(name = "roles", description = "User roles")
                        }
                )
        ))
public class SpringdocConfig {
}

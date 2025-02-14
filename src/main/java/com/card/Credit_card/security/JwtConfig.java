package com.card.Credit_card.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;


@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JwtConfig {
    private String secretKey;
    private String tokenPrefix;
    private Integer tokenExpirationAfterDays;


    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}

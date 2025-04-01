package com.example.orderservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class FeignClientConfig {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public FeignClientConfig(OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public RequestInterceptor productServiceRequestInterceptor() {
        return requestTemplate -> {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient("product-service");
            if (authorizedClient != null) {
                requestTemplate.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
            }
        };
    }

    @Bean
    public RequestInterceptor customerServiceRequestInterceptor() {
        return requestTemplate -> {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient("customer-service");
            if (authorizedClient != null) {
                requestTemplate.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
            }
        };
    }

    private OAuth2AuthorizedClient getAuthorizedClient(String clientRegistrationId) {
        try {
            OAuth2AuthorizedClientManager authorizedClientManager = authorizedClientManager();
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistrationId)
                    .principal("order-service")
                    .build();
            return authorizedClientManager.authorize(authorizeRequest);
        } catch (Exception e) {
            return null;
        }
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    // 테스트를 위한 public
    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        try {

            String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

            OAuth2Provider provider;
            try {
                provider = OAuth2Provider.valueOf(registrationId);
            } catch (IllegalArgumentException e) {
                throw new InvalidProviderException();
            }

            OAuth2Response oAuth2Response = OAuth2Factory.createResponse(
                    provider,
                    oAuth2User.getAttributes()
            );

            // 사용자 정보 저장 or 업데이트
            User user = updateOrCreateUser(oAuth2Response);
            return new OAuth2UserPrincipal(user, oAuth2Response);

        } catch (AuthenticationException e) {
            log.error("OAuth2 인증 실패: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 처리 중 내부 오류: {}", e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private User updateOrCreateUser(OAuth2Response oAuth2Response) {
        return userRepository.findByProviderId(oAuth2Response.getProviderId())
                .map(user -> {
                    updateUser(user);  // 기존 유저
                    return user;
                })
                .orElseGet(() -> userRepository.save(createUser(oAuth2Response)));  // 신규 유저
    }

    private User createUser(OAuth2Response response) {
        return User.builder()
                .name(response.getName())             // 소셜에서 가져온 이름
                .nickname(response.getName())         // 초기 닉네임은 소셜 이름으로 설정
                .profileImage(response.getProfileImageUrl())
                .provider(Provider.valueOf(response.getProvider().name()))
                .providerId(response.getProviderId())
                .status(Status.OFFLINE)
                .build();
    }

    private void updateUser(User user) {
        user.updateLastLogin();
    }
}

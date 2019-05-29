package chat.tamtam.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RefreshScope
@RequiredArgsConstructor
public class ResourcesService {
    @Value("${tamtam.bot.registration.url}")
    private String registrationBotUrl;

    public SuccessResponse getRegistrationBotUrl() {
        return new SuccessResponseWrapper<>(new Object() {
            @Getter
            private String url = registrationBotUrl;
        });
    }
}

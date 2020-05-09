package chat.tamtam.bot.service;

import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
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

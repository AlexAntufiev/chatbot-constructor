package chat.tamtam.bot.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import lombok.AllArgsConstructor;

@Service
@RefreshScope
@AllArgsConstructor
public class ResourcesService {
    @Value("${tamtam.registration.bot.url}")
    private String registrationBotUrl; 

    public SuccesResponse getRegistrationBotUrl() {
        return new SuccessResponseWrapper<>(registrationBotUrl); 
    } 
}

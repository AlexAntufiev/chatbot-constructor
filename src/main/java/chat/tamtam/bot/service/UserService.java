package chat.tamtam.bot.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.user.UserAuthEntity;
import chat.tamtam.bot.domain.user.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Loggable
    public boolean addUser(final UserAuthEntity userAuthEntity) {
        String login = userAuthEntity.getLogin();
        if (!userRepository.findUserEntitiesByLogin(login).isEmpty()) {
            return false;
        }
        // @todo #CC-4 expand filters on addUser action
        String password = userAuthEntity.getPassword();
        if (login.isEmpty() || password.isEmpty()) {
            return false;
        }
        UserEntity user = new UserEntity(login, bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
        return true;
    }

    @Loggable
    public long getUserIdByToken(final String token) {
        return sessionRepository.findByToken(token).getUserId();
    }

    @Bean
    @Profile(Profiles.DEVELOPMENT)
    InitializingBean populateDevelopmentDatabase() {
        return () -> addUser(new UserAuthEntity("admin", "admin"));
    }

    @Bean
    @Profile(Profiles.TEST)
    InitializingBean populateTestDatabase() {
        return () -> {
            addUser(new UserAuthEntity("test1", "B&]LhfC2bP^Joh.{b,g\\b\"a,%)'DW;x\""));
            addUser(new UserAuthEntity("test2", "_vq#4g7K<w*k*hFRuX]upQ2Grz{_K?WH"));
            addUser(new UserAuthEntity("test3", "fP6~)w=U]d=FL&5s=E{$w`=ej[]',,5,"));
            addUser(new UserAuthEntity("test4", "}NeP#<A6J(a$LM/3Yh\\-/;oTf~`#Y?uJ"));
        };
    }
}

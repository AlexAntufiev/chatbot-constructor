package chat.tamtam.bot.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.UserAuthEntity;
import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean addUser(final UserAuthEntity userAuthEntity) {
        String login = userAuthEntity.getLogin();
        if (!userRepository.findUserEntitiesByLogin(login).isEmpty()) {
            return false;
        }
        //todo expand filters
        String password = userAuthEntity.getPassword();
        if (login.isEmpty() || password.isEmpty()) {
            return false;
        }
        UserEntity user = new UserEntity(login, bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
        return true;
    }

    public Integer getUserIdByToken(final String token) {
        return sessionRepository.findByToken(token).getUserId();
    }
}

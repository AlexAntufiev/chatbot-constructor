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
        if (!userRepository.findUserEntitiesByLogin(userAuthEntity.getLogin()).isEmpty()) {
            return false;
        }
        //todo expand filters
        if (userAuthEntity.getLogin().isEmpty() || userAuthEntity.getPassword().isEmpty()) {
            return false;
        }
        UserEntity user = new UserEntity(
                userAuthEntity.getLogin(),
                bCryptPasswordEncoder.encode(userAuthEntity.getPassword())
        );
        userRepository.save(user);
        return true;
    }

    public Integer getUserIdByToken(final String token) {
        return sessionRepository.findByToken(token).getUserId();
    }
}

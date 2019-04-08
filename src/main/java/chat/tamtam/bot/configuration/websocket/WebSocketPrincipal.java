package chat.tamtam.bot.configuration.websocket;

import java.security.Principal;

import javax.security.auth.Subject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebSocketPrincipal implements Principal {
    private final String userId;

    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof WebSocketPrincipal) {
            return ((WebSocketPrincipal) another).userId.equals(userId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "User id = " + userId;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String getName() {
        return userId;
    }
}

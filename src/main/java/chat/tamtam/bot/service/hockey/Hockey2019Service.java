package chat.tamtam.bot.service.hockey;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.hockey.Calendar;
import chat.tamtam.bot.domain.bot.hockey.Match;
import chat.tamtam.bot.domain.bot.hockey.News;
import chat.tamtam.bot.domain.bot.hockey.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(
        prefix = "tamtam.bot.hockey2019",
        name = "enabled",
        havingValue = "true"
)
public class Hockey2019Service {

    @Value("${tamtam.bot.hockey2019.external_url.news}")
    private String newsPath;

    @Value("${tamtam.bot.hockey2019.external_url.news_team}")
    private String newsTeamPath;

    @Value("${tamtam.bot.hockey2019.external_url.calendar}")
    private String calendarPath;

    @Value("${tamtam.bot.hockey2019.external_url.results}")
    private String resultsPath;

    @Value("${tamtam.bot.hockey2019.external_url.match}")
    private String matchPath;

    @Autowired
    private RestTemplate restTemplate;

    public News getNews() {
        return getBodyFromRequest(newsPath, News.class);
    }

    public News getNewsOfTeam(int teamId) {
        return getBodyFromRequest(newsTeamPath, News.class, teamId);
    }

    public Results getResults() {
        return getBodyFromRequest(resultsPath, Results.class);
    }

    public Calendar getCalendar() {
        return getBodyFromRequest(calendarPath, Calendar.class);
    }

    public Match getMatch(int matchId) {
        return getBodyFromRequest(matchPath, Match.class, matchId);
    }

    @Loggable
    private <T> T getBodyFromRequest(String path, Class<T> tClass, Object... uriVariables) {
        return restTemplate.getForEntity(path, tClass, uriVariables).getBody();
    }

    @Loggable
    private <T> T getBodyFromRequest(String path, Class<T> tClass) {
        return restTemplate.getForEntity(path, tClass).getBody();
    }
}

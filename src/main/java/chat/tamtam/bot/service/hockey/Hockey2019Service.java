package chat.tamtam.bot.service.hockey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.hockey.Calendar;
import chat.tamtam.bot.domain.bot.hockey.Match;
import chat.tamtam.bot.domain.bot.hockey.News;
import chat.tamtam.bot.domain.bot.hockey.Results;

@Service
public class Hockey2019Service {

    @Value("${tamtam.bot.hockey2019.url}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    public News getNews() {
        return getBodyFromRequest(getNewsPath(), News.class);
    }

    public News getNewsOfTeam(int teamId) {
        return getBodyFromRequest(getNewsOfTeamPath(), News.class, teamId);
    }

    public Results getResults() {
        return getBodyFromRequest(getResultsPath(), Results.class);
    }

    public Calendar getCalendar() {
        return getBodyFromRequest(getCalendarPath(), Calendar.class);
    }

    public Match getMatch(int matchId) {
        return getBodyFromRequest(getMatchPath(), Match.class, matchId);
    }

    private String getNewsPath() {
        return url + "/news";
    }

    private String getNewsOfTeamPath() {
        return getNewsPath() + "/%s";
    }

    private String getResultsPath() {
        return url + "/results";
    }

    private String getCalendarPath() {
        return url + "/calendar";
    }

    private String getMatchPath() {
        return url + "/match/%s";
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

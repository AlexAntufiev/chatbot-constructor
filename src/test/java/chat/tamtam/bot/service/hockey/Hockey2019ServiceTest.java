package chat.tamtam.bot.service.hockey;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import chat.tamtam.bot.TestContext;
import chat.tamtam.bot.domain.bot.hockey.Calendar;
import chat.tamtam.bot.domain.bot.hockey.Match;
import chat.tamtam.bot.domain.bot.hockey.News;
import chat.tamtam.bot.domain.bot.hockey.Results;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class Hockey2019ServiceTest extends TestContext {

    private static final int TEAM_ID = 1;
    private static final int MATCH_ID = 1;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private Hockey2019Service hockey2019Service;

    @Value("classpath:data/news.json")
    private Resource newsFile;

    @Value("classpath:data/news_team.json")
    private Resource newsOfTeamFile;

    @Value("classpath:data/results.json")
    private Resource resultsFile;

    @Value("classpath:data/calendar.json")
    private Resource calendarFile;

    @Value("classpath:data/match.json")
    private Resource matchFile;

    @BeforeEach
    void setUp() throws IOException {

        String newsPath = String.valueOf(getField(hockey2019Service, "newsPath"));
        News news = objectMapper.readValue(newsFile.getFile(), News.class);
        assertNotNull(news);
        when(restTemplate.getForEntity(eq(newsPath), eq(News.class))).thenReturn(ResponseEntity.ok(news));

        String newsOfTeamPath = String.valueOf(getField(hockey2019Service, "newsTeamPath"));
        News newsOfTeam = objectMapper.readValue(newsOfTeamFile.getFile(), News.class);
        assertNotNull(newsOfTeam);
        when(restTemplate.getForEntity(eq(newsOfTeamPath), eq(News.class), eq(TEAM_ID))).thenReturn(ResponseEntity.ok(
                newsOfTeam));

        String resultsPath = String.valueOf(getField(hockey2019Service, "resultsPath"));
        Results results = objectMapper.readValue(resultsFile.getFile(), Results.class);
        assertNotNull(results);
        when(restTemplate.getForEntity(eq(resultsPath), eq(Results.class))).thenReturn(ResponseEntity.ok(results));

        String calendarPath = String.valueOf(getField(hockey2019Service, "calendarPath"));
        Calendar calendar = objectMapper.readValue(calendarFile.getFile(), Calendar.class);
        assertNotNull(calendar);
        when(restTemplate.getForEntity(eq(calendarPath), eq(Calendar.class))).thenReturn(ResponseEntity.ok(calendar));

        String matchPath = String.valueOf(getField(hockey2019Service, "matchPath"));
        Match match = objectMapper.readValue(matchFile.getFile(), Match.class);
        assertNotNull(match);
        when(restTemplate.getForEntity(eq(matchPath),
                eq(Match.class),
                eq(MATCH_ID)
        )).thenReturn(ResponseEntity.ok(match));

    }

    @Test
    void getNews() {
        assertNotNull(hockey2019Service.getNews());
    }

    @Test
    void getNewsOfTeam() {
        assertNotNull(hockey2019Service.getNewsOfTeam(TEAM_ID));
    }

    @Test
    void getResults() {
        assertNotNull(hockey2019Service.getResults());
    }

    @Test
    void getCalendar() {
        assertNotNull(hockey2019Service.getCalendar());
    }

    @Test
    void getMatch() {
        assertNotNull(hockey2019Service.getMatch(MATCH_ID));
    }
}
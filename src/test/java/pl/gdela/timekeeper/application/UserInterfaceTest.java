package pl.gdela.timekeeper.application;

import com.codeborne.selenide.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class UserInterfaceTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("http://localhost:${local.server.port}/")
	private String baseUrl;

	@Test
	public void smoke_test() {
		Configuration.browser = "phantomjs";
		open(baseUrl);
		assertThat(title()).contains("Timekeeper");
		$("#start-race").click();
		$("#race-log tbody").shouldBe(empty);
		try { Thread.sleep(5000); } catch (InterruptedException ignore) { } // todo: instead of sleep wait for race start event
		restTemplate.postForEntity("/interrupt-photocell", null, String.class);
		$("#race-log tbody").shouldNotBe(empty);
	}

	// todo: test double start (restart during race)
}

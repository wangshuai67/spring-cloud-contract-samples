package com.example;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {ClientApplication.class, BeerVerificationListenerTest.Config.class})
@AutoConfigureStubRunner(stubsMode = StubRunnerProperties.StubsMode.LOCAL, ids = "com.example:beer-api-producer")
@DirtiesContext
@DisabledIfEnvironmentVariable(named = "SKIP_COMPATIBILITY_TESTS", matches = "true")
public class BeerVerificationListenerTest extends AbstractTest {


	@Autowired
	StubTrigger stubTrigger;

	@Autowired
	BeerVerificationListener listener;

	@Before
	public void setup() {
		Assume.assumeFalse("Skip compatibility tests env var was set to true", "true".equals(System.getenv("SKIP_COMPATIBILITY_TESTS")));
	}


	@Test
	public void should_increase_the_eligible_counter_when_verification_was_accepted() {
		int initialCounter = this.listener.eligibleCounter.get();

		this.stubTrigger.trigger("accepted_verification");

		then(this.listener.eligibleCounter.get()).isGreaterThan(initialCounter);
	}

	@Test
	public void should_increase_the_noteligible_counter_when_verification_was_rejected() {
		int initialCounter = this.listener.notEligibleCounter.get();

		this.stubTrigger.trigger("rejected_verification");

		then(this.listener.notEligibleCounter.get()).isGreaterThan(initialCounter);
	}

	@TestConfiguration
	@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
	static class Config {

	}

}

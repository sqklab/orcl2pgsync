package com.lguplus.fleta.domain.service.secret;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.net.URI;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Oct 2021
 */
@Slf4j
@Profile({"test", "local", "integrationTest"})
@Component
public class LocalSecretValueClient extends SecretValueClient {
	private final SsmClient ssmClient;

	@Override
	public SsmClient getSsmClient() {
		return ssmClient;
	}

	public LocalSecretValueClient(
			@Value("${aws.region}") String region,
			@Value("${aws.endpoint-url:}") String endpointUrl
	) {
		if (endpointUrl.isEmpty()) {
			this.ssmClient = SsmClient.builder()
					.region(Region.of(region))
					.build();
		} else {
			this.ssmClient = SsmClient.builder()
					.endpointOverride(URI.create(endpointUrl))
					.region(Region.of(region))
					.build();
		}
	}
}

package com.lguplus.fleta.domain.service.secret;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Oct 2021
 */
@Slf4j
@Profile("!test & !local & !integrationTest")
@Component
public class EksSecretValueClient extends SecretValueClient {

	private final SsmClient ssmClient;

	@Override
	public SsmClient getSsmClient() {
		return ssmClient;
	}

	public EksSecretValueClient(@Value("${aws.region}") String region) {
		this.ssmClient = SsmClient.builder()
				.region(Region.of(region))
				.credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
				.build();
	}
}

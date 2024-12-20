package com.lguplus.fleta.domain.service.secret;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Oct 2021
 */
@Slf4j
public abstract class SecretValueClient {

	public abstract SsmClient getSsmClient();

	public String parseSecretValue(final String secretKey) {
		if (StringUtils.isBlank(secretKey)) {
			return null;
		}
		try {
			GetParameterResponse response = getSsmClient().getParameter(GetParameterRequest.builder().name(secretKey).withDecryption(true).build());
			return response.parameter().value();
		} catch (Exception ex) {
			log.warn("secretKey {} does not exist in aws parameter store", secretKey, ex);
		}
		return secretKey;
	}
}

package com.lguplus.fleta.config;

import com.logviewer.api.LvPathResolver;
import com.logviewer.config.LogViewerAutoConfig;
import com.logviewer.data2.LogPath;
import com.logviewer.services.LvFileAccessManagerImpl;
import com.logviewer.services.PathPattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
@Configuration
public class MyLogViewerAutoConfig extends LogViewerAutoConfig {

	private static final String LOG_ARCHIVED = "archived";

	@Value("${logging.file.path}")
	private String logPath;

	@Bean
	@Primary
	@Override
	public LvFileAccessManagerImpl lvLogManager(@Value("${log-viewer.accessible-files.pattern:}") List<String> accessiblePatterns) {
		LvFileAccessManagerImpl res = new LvFileAccessManagerImpl(null);
		try {
			res.setPaths(Arrays.asList(PathPattern.directory(Paths.get(logPath)), PathPattern.directory(Paths.get(logPath, LOG_ARCHIVED))));
		} catch (IllegalArgumentException ex) {
			res.setPaths(List.of(PathPattern.directory(Paths.get("/var/log/dbsync/logs"))));
		}
		return res;
	}

	@Bean
	public LvPathResolver pathResolver() {
		return new LvPathResolver() {
			@Override
			public Collection<LogPath> resolvePath(@NonNull String pathFromHttpParameter) {
				List<LogPath> logPaths = new ArrayList<>();
				logPaths.add(new LogPath(null, Paths.get(pathFromHttpParameter).toString()));
				logPaths.add(new LogPath(null, Paths.get(logPath, pathFromHttpParameter).toString()));
				logPaths.add(new LogPath(null, Paths.get(logPath, LOG_ARCHIVED, pathFromHttpParameter).toString()));
				return logPaths;
			}
		};
	}
}

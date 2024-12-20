package com.lguplus.fleta.domain.service;

import com.lguplus.fleta.domain.dto.ui.LogInfo;
import com.lguplus.fleta.ports.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

	private static final String LOG_EXTENSION = ".log";

	private final ResourceLoader resourceLoader;

	@Value("${logging.file.path:logs}")
	public String loggingPath;

	public LogServiceImpl(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public FileSystemResource downloadLogByPath(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		return new FileSystemResource(path);
	}

	@Override
	public Resource findLogByTopic(@NotEmpty String topic) {
		try {
			String path = loggingPath + File.separator + topic + LOG_EXTENSION;
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}
			return resourceLoader.getResource(path);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public List<LogInfo> findAllLogsByTopic(String topic) {
		List<LogInfo> logs = new LinkedList<>();

		String path = loggingPath + File.separator + topic + LOG_EXTENSION;
		File file = new File(path);
		if (file.exists()) {
			logs.add(LogInfo.builder()
					.name(file.getName())
					.length(file.length())
					.path(path)
					.build());
		}

		String archived = loggingPath + "/archived";
		File archivedDirectory = new File(archived);
		if (file.exists()) {
			File[] files = archivedDirectory.listFiles();
			if (null != files) {
				Arrays.sort(files, Collections.reverseOrder());
				for (File f : files) {
					if (f.getName().startsWith(topic)) {
						logs.add(LogInfo.builder()
								.name(f.getName())
								.length(f.length())
								.path(f.getPath())
								.build());
					}
				}
			}
		}
		return logs;
	}

	public static class SortByName implements Comparator<File> {
		private static final String FILE_NAME_PATTERN = ".*-([\\d]{4}-[\\d]{2}-[\\d]{2})\\.(\\d{1,3})\\..*";

		private int compare(String a, String b) {
			Pattern pattern = Pattern.compile(FILE_NAME_PATTERN);
			Matcher matcher = pattern.matcher(a);
			Matcher matcherB = pattern.matcher(b);

			boolean found = matcher.find();
			boolean foundB = matcherB.find();

			if (!found) {
				return -1;
			} else if (!foundB) {
				return 1;
			}
			if (matcher.group(1).compareTo(matcherB.group(1)) == 0) {
				int ga = Integer.parseInt(matcher.group(2));
				int gb = Integer.parseInt(matcherB.group(2));
				return ga - gb;
			}
			return matcher.group(1).compareTo(matcherB.group(1));
		}

		public int compare(File a, File b) {
			return compare(a.getName(), b.getName());
		}
	}

}

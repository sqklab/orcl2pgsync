package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.ui.LogInfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;

public interface LogService {

	FileSystemResource downloadLogByPath(String path);

	Resource findLogByTopic(String topic);

	List<LogInfo> findAllLogsByTopic(String topic);
}

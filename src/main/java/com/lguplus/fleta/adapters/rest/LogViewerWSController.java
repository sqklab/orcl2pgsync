package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.config.IDefaultLogPath;
import com.lguplus.fleta.config.context.DbSyncContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Oct 2021
 */
@Slf4j
@Component
// TODO: important
// If this bean is prototype-scoped, you will get new instance each time you call it.
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ServerEndpoint("/dbsync/logs2/{fileName}/{numberOfLines}")
public class LogViewerWSController {

	private static final int MINIMUM_DELAY_TO_DETECT_CHANGES = 1000;

	private static final Map<String, Process> tailers = new ConcurrentHashMap<>();

	// Default log file extension
	private static final String LOG_EXTENSION = ".log";
	private static final String LOG_ARCHIVED = "archived";

	private long retryInterval = 500;

	@OnOpen
	public void onOpen(@PathParam("fileName") String fileName,
					   @PathParam("numberOfLines") int numberOfLines, Session session) {
		File logfile;
		try {
			logfile = getCorrespondingLogFromKafkaTopic(fileName);
		} catch (FileNotFoundException ex) {
			log.warn(ex.getMessage(), ex);

			try {
				session.getBasicRemote().sendText(Strings.EMPTY);
			} catch (IOException ioe) {
				log.error(ioe.getMessage(), ioe);
			}
			return;
		}

		if (numberOfLines < 0) {
			numberOfLines = 999;
		}

		// Create new a tailer listener
		String cmd = "tail -f -n " + numberOfLines + " " + logfile.getAbsolutePath();
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					String line;
					try {
						while ((line = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)).readLine()) != null) {
							session.getBasicRemote().sendText(line + "<br>");
						}
					} catch (IOException ex) {
						log.warn(ex.getMessage(), ex);
					}
				}
			});
			tailers.put(session.getId(), process);

			thread.start();
			// this is required, because the file modification date granularity may
			// be too low so that immediate changes are not detected
			Thread.sleep(MINIMUM_DELAY_TO_DETECT_CHANGES);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@OnClose
	public void onClose(@PathParam("fileName") String fileName, Session session) {
		Process process = tailers.get(session.getId());
		if (null != process) {
			process.destroy();
		}
		tailers.remove(session.getId());
	}

	@OnError
	public void onError(Throwable ex) {
		log.error(ex.getMessage(), ex);
	}

	/**
	 * Return corresponding file path for give a file name
	 *
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	private File getCorrespondingLogFromKafkaTopic(String fileName) throws FileNotFoundException {
		if (!StringUtils.hasText(fileName) || !fileName.contains(LOG_EXTENSION)) {
			throw new FileNotFoundException(String.format("File %s not found", fileName));
		}

		String rootPath = DbSyncContext.getBean(IDefaultLogPath.class).getRootPath();
		File logFile = new File(rootPath + File.separator + fileName);
		if (logFile.exists()) {
			return logFile;
		}

		// Looking inside archived folder
		File archivedLog = new File(rootPath + File.separator + LOG_ARCHIVED + File.separator + fileName);
		if (archivedLog.exists()) {
			return archivedLog;
		}
		throw new FileNotFoundException(String.format("File %s not found", fileName));
	}
}

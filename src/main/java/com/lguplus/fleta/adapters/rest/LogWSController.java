package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.config.IDefaultLogPath;
import com.lguplus.fleta.config.context.DbSyncContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The class {@link LogWSController} is a implementation that allow view the log in real time on the page, eliminating
 * the need to connect to the server and search the log.
 * <p>
 * Usage:
 * <script>
 * $(document).ready(function() {
 * var websocket = new WebSocket('ws://127.0.0.1:9000/dbsync/logs/MYLGDB.SMARTUX.PT_VL_STORYTEL_GROUP_ASSIGN.log');
 * websocket.onmessage = function(event) {
 * // do something here!!!
 * };
 * });
 * </script>
 *
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
@Slf4j
@Component
// TODO: important
// If this bean is prototype-scoped, you will get new instance each time you call it.
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ServerEndpoint("/dbsync/logs/{fileName}")
public class LogWSController {

	private static final int MINIMUM_DELAY_TO_DETECT_CHANGES = 1000;

	private static final Map<String, Tailer> tailers = new ConcurrentHashMap<>();

	// Default log file extension
	private static final String LOG_EXTENSION = ".log";
	private static final String LOG_ARCHIVED = "archived";

	private long retryInterval = 500;

	@OnOpen
	public void onOpen(@PathParam("fileName") String fileName, Session session) throws InterruptedException {
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

		// Create new a tailer listener
		TailerListener listener = new TailerListenerAdapter() {
			@Override
			public void handle(String line) {
				try {
					session.getBasicRemote().sendText(line);
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
			}

			@Override
			public void fileNotFound() {
				log.error("** File '{}' not found", fileName);
			}

			@Override
			public void handle(Exception ex) {
				throw new RuntimeException("Error tailing file", ex);
			}
		};
		Tailer tailer = new Tailer(logfile, listener, retryInterval, false, true);
		tailers.put(session.getId(), tailer);

		Thread thread = new Thread(tailer);
		thread.setDaemon(true);
		thread.start();
		// this is required, because the file modification date granularity may
		// be too low so that immediate changes are not detected
		Thread.sleep(MINIMUM_DELAY_TO_DETECT_CHANGES);
	}

	@OnClose
	public void onClose(@PathParam("fileName") String fileName, Session session) {
		Tailer tailer = tailers.get(session.getId());
		if (null != tailer) {
			tailer.stop();
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

	static class LogTailer extends Thread {

		private long sampleInterval = 2000;

		private File logfile;

		private boolean startAtBeginning;

		private Consumer<String> callback;

		/**
		 * Monitoring switch, true = turn on monitoring
		 */
		private boolean tailing;

		/**
		 * @param file           text file to be monitored
		 * @param sampleInterval read time interval
		 *                       Does @param startAtBeginning show the file header? Or only show the part that changes later
		 */
		public LogTailer(String file, long sampleInterval, boolean startAtBeginning) {
			this.logfile = new File(file);

			this.sampleInterval = sampleInterval;
			this.startAtBeginning = startAtBeginning;
		}

		/**
		 * Set callback event
		 *
		 * @param callback callback event
		 */
		public void addListener(Consumer<String> callback) {
			this.callback = callback;
		}

		/**
		 * Monitoring switch, true = turn on monitoring
		 *
		 * @param tailing true = turn on monitoring
		 */
		public void setTailing(boolean tailing) {
			this.tailing = tailing;
		}

		@Override
		public void run() {
			long filePointer = startAtBeginning ? 0 : logfile.length();
			try {
				RandomAccessFile file = new RandomAccessFile(logfile, "r");
				while (tailing) {
					long fileLength = logfile.length();

					if (fileLength < filePointer) {
						file = new RandomAccessFile(logfile, "r");
						filePointer = 0;
					}

					if (fileLength > filePointer) {
						file.seek(filePointer);
						String line = file.readLine();

						while (line != null) {
							line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

							if (callback != null)
								callback.accept(line);
							line = file.readLine();
						}

						filePointer = file.getFilePointer();
					}
					sleep(sampleInterval);
				}
				file.close();
			} catch (IOException | InterruptedException ex) {

			}
		}
	}
}

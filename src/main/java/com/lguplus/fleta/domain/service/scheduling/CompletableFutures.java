package com.lguplus.fleta.domain.service.scheduling;

import com.google.auto.value.AutoValue;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.ForwardingFuture;
import net.autobuilder.AutoBuilder;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
public final class CompletableFutures {
	private static final Logger logger = LoggerFactory.getLogger(CompletableFutures.class);

	/**
	 * Returns an {@link ScheduledExecutorService} that uses the global scheduler, hidden within
	 * {@link CompletableFuture#delayedExecutor}, to delay on. This allows scheduling a task without
	 * creating dedicated threads. The tasks are executed on the global
	 * {@link ForkJoinPool#commonPool}.
	 */
	public static ScheduledExecutorService scheduler() {
		return new Scheduler(ForkJoinPool.commonPool());
	}

	/**
	 * Returns an {@link ScheduledExecutorService} that uses the global scheduler, hidden within
	 * {@link CompletableFuture#delayedExecutor}, to delay on. This allows scheduling a task without
	 * creating dedicated threads. The tasks are executed on the given executor.
	 */
	public static ScheduledExecutorService scheduler(ExecutorService executor) {
		return new Scheduler(executor);
	}

	static final class Scheduler extends ForwardingExecutorService implements ScheduledExecutorService {
		private final ExecutorService delegate;

		Scheduler(ExecutorService delegate) {
			this.delegate = Objects.requireNonNull(delegate);
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			return schedule(Executors.callable(command), delay, unit);
		}

		@Override
		public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
			var executor = CompletableFuture.delayedExecutor(delay, unit, delegate());
			var future = CompletableFuture.supplyAsync(Unchecked.supplier(callable::call), executor);
			return new ScheduledCompletableFuture<>(future, delay, unit);
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
													  long initialDelay, long period, TimeUnit unit) {
			var task = FixedRateRunnable.builder()
					.period(Duration.ofNanos(unit.toNanos(period)))
					.scheduler(this)
					.task(command)
					.build();
			var future = schedule(task, initialDelay, unit);
			task.scheduledFuture().set(future);
			return future;
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
														 long initialDelay, long delay, TimeUnit unit) {
			var task = FixedDelayRunnable.builder()
					.delay(Duration.ofNanos(unit.toNanos(delay)))
					.scheduler(this)
					.task(command)
					.build();
			var future = schedule(task, initialDelay, unit);
			task.scheduledFuture().set(future);
			return future;
		}

		@Override
		protected ExecutorService delegate() {
			return delegate;
		}
	}

	static final class ScheduledCompletableFuture<V> extends ForwardingFuture<V> implements ScheduledFuture<V> {
		private final CompletableFuture<V> delegate;
		private final AtomicBoolean cancelled;
		private final long time;

		ScheduledCompletableFuture(CompletableFuture<V> delegate, long delay, TimeUnit unit) {
			this.time = System.nanoTime() + unit.toNanos(delay);
			this.delegate = Objects.requireNonNull(delegate);
			this.cancelled = new AtomicBoolean();
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
		}

		@Override
		public int compareTo(Delayed other) {
			if (other == this) {
				return 0;
			} else if (other instanceof ScheduledCompletableFuture) {
				return Longs.compare(time, ((ScheduledCompletableFuture<?>) other).time);
			}
			return Longs.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean wasCancelled = cancelled.getAndSet(true);
			super.cancel(mayInterruptIfRunning);
			return !wasCancelled;
		}

		@Override
		public boolean isCancelled() {
			return cancelled.get();
		}

		@Override
		public boolean isDone() {
			return isCancelled() || delegate().isDone();
		}

		@Override
		protected Future<? extends V> delegate() {
			return delegate;
		}
	}

	@AutoValue
	@AutoBuilder
	static abstract class FixedRateRunnable implements Runnable {
		static CompletableFutures_FixedRateRunnable_Builder builder() {
			return CompletableFutures_FixedRateRunnable_Builder.builder()
					.scheduledFuture(new AtomicReference<>());
		}

		abstract AtomicReference<ScheduledFuture<?>> scheduledFuture();

		abstract Scheduler scheduler();

		abstract Duration period();

		abstract Runnable task();

		@Override
		@SuppressWarnings("FutureReturnValueIgnored")
		public void run() {
			try {
				var future = scheduledFuture().get();
				if ((future == null) || !future.isCancelled()) {
					long next = System.nanoTime() + period().toNanos();
					task().run();
					long delay = next - System.nanoTime();
					scheduler().schedule(this, delay, TimeUnit.NANOSECONDS);
				}
			} catch (Exception e) {
				logger.warn("Scheduled task with fixed rate failed; not rescheduled ({})",
						task().getClass().getName(), e);
				throw e;
			}
		}
	}

	@AutoValue
	@AutoBuilder
	static abstract class FixedDelayRunnable implements Runnable {
		static CompletableFutures_FixedDelayRunnable_Builder builder() {
			return CompletableFutures_FixedDelayRunnable_Builder.builder()
					.scheduledFuture(new AtomicReference<>());
		}

		abstract AtomicReference<ScheduledFuture<?>> scheduledFuture();

		abstract Scheduler scheduler();

		abstract Duration delay();

		abstract Runnable task();

		@Override
		@SuppressWarnings("FutureReturnValueIgnored")
		public void run() {
			try {
				var future = scheduledFuture().get();
				if ((future == null) || !future.isCancelled()) {
					task().run();
					scheduler().schedule(this, delay().toNanos(), TimeUnit.NANOSECONDS);
				}
			} catch (Exception e) {
				logger.warn("Scheduled task with fixed delay failed; not rescheduled ({})",
						task().getClass().getName(), e);
				throw e;
			}
		}
	}
}

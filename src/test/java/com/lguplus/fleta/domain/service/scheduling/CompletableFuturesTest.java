package com.lguplus.fleta.domain.service.scheduling;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
@SuppressWarnings("FutureReturnValueIgnored")
public final class CompletableFuturesTest {

	@Test
	public void scheduler_schedule() {
		AtomicBoolean done = new AtomicBoolean();
		Duration delay = Duration.ofMillis(500);
		long startTime = System.nanoTime();
		CompletableFutures.scheduler().schedule(() -> done.set(true),
				delay.toNanos(), TimeUnit.NANOSECONDS);
		await().untilTrue(done);
		assertThat(System.nanoTime() - startTime, greaterThanOrEqualTo(delay.toNanos()));
	}

	@Test
	public void scheduler_scheduleAtFixedRate() throws InterruptedException {
		AtomicInteger done = new AtomicInteger();
		Duration initialDelay = Duration.ofMillis(250);
		Duration period = Duration.ofMillis(500);
		long startTime = System.nanoTime();
		var future = CompletableFutures.scheduler().scheduleAtFixedRate(() -> done.incrementAndGet(),
				initialDelay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
		await().untilAtomic(done, greaterThanOrEqualTo(1));
		assertThat(System.nanoTime() - startTime, greaterThanOrEqualTo(initialDelay.toNanos()));

		await().untilAtomic(done, greaterThanOrEqualTo(2));
		assertThat(System.nanoTime() - startTime, greaterThanOrEqualTo(period.toNanos()));
		future.cancel(false);

		int completed = done.get();
		TimeUnit.NANOSECONDS.sleep(period.toNanos());
		assertThat(done.get(), is(completed));
	}

	@Test
	public void scheduler_scheduleAtFixedDelay() throws InterruptedException {
		AtomicInteger done = new AtomicInteger();
		Duration initialDelay = Duration.ofMillis(250);
		Duration delay = Duration.ofMillis(500);
		long startTime = System.nanoTime();
		var future = CompletableFutures.scheduler().scheduleAtFixedRate(() -> done.incrementAndGet(),
				initialDelay.toNanos(), delay.toNanos(), TimeUnit.NANOSECONDS);
		await().untilAtomic(done, greaterThanOrEqualTo(1));
		assertThat(System.nanoTime() - startTime, greaterThanOrEqualTo(initialDelay.toNanos()));

		await().untilAtomic(done, greaterThanOrEqualTo(2));
		assertThat(System.nanoTime() - startTime, greaterThanOrEqualTo(
				initialDelay.plus(delay).toNanos()));
		future.cancel(false);

		int completed = done.get();
		TimeUnit.NANOSECONDS.sleep(delay.toNanos());
		assertThat(done.get(), is(completed));
	}
}
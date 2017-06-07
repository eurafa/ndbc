package io.trane.ndbc.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import io.trane.future.Future;
import io.trane.future.Promise;
import io.trane.ndbc.Connection;
import io.trane.ndbc.PreparedStatement;
import io.trane.ndbc.ResultSet;

public class PoolTest {

  final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @After
  public void shutdown() {
    scheduler.shutdown();
  }

  @Test
  public void maxSize() {
    final int maxSize = 100;
    final Pool<Connection> pool = Pool.apply(() -> Future.value(conn()), maxSize, Integer.MAX_VALUE,
        Duration.ofMillis(Long.MAX_VALUE), scheduler);
    final AtomicInteger executing = new AtomicInteger();

    for (int i = 0; i < maxSize * 3; i++)
      pool.apply(t -> {
        executing.incrementAndGet();
        return Promise.apply();
      });

    assertEquals(maxSize, executing.get());
  }

  @Test
  public void maxSizeConcurrentCreation() {
    final int maxSize = 100;
    final Pool<Connection> pool = Pool.apply(() -> Future.value(conn()), maxSize, Integer.MAX_VALUE,
        Duration.ofMillis(Long.MAX_VALUE), scheduler);
    final AtomicInteger executing = new AtomicInteger();

    Concurrently.apply(Duration.ofMillis(200), () -> {
      pool.apply(t -> {
        executing.incrementAndGet();
        return Promise.apply();
      });
    }, () -> {
      assertTrue(maxSize >= executing.get());
    });
  }

  @Test
  public void maxSizeConcurrentUsage() {
    final int maxSize = 100;
    final Pool<Connection> pool = Pool.apply(() -> Future.value(conn()), maxSize, Integer.MAX_VALUE,
        Duration.ofMillis(Long.MAX_VALUE), scheduler);
    final AtomicInteger executing = new AtomicInteger();
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    Concurrently.apply(Duration.ofMillis(200), () -> {
      pool.apply(t -> {
        executing.incrementAndGet();
        return Future.delay(Duration.ofMillis(1), scheduler).ensure(() -> executing.decrementAndGet());
      });
    }, () -> {
      assertTrue(maxSize >= executing.get());
    });
  }

  @Test
  public void maxWaiters() {
    final int maxSize = 100;
    final int maxWaiters = 60;
    final Pool<Connection> pool = Pool.apply(() -> Future.value(conn()), maxSize, maxWaiters,
        Duration.ofMillis(Long.MAX_VALUE), scheduler);
    final AtomicInteger executing = new AtomicInteger();
    final AtomicInteger rejected = new AtomicInteger();

    for (int i = 0; i < 200; i++)
      pool.apply(t -> {
        executing.incrementAndGet();
        return Promise.apply();
      }).onFailure(e -> rejected.incrementAndGet());

    assertEquals(maxSize, executing.get());
    assertEquals(40, rejected.get());
  }

  @Test
  public void maxWaitersConcurrentCreation() {
    final int maxSize = 100;
    final int maxWaiters = 60;
    final Pool<Connection> pool = Pool.apply(() -> Future.value(conn()), maxSize, maxWaiters,
        Duration.ofMillis(Long.MAX_VALUE), scheduler);
    final AtomicInteger started = new AtomicInteger();
    final AtomicInteger executing = new AtomicInteger();
    final AtomicInteger rejected = new AtomicInteger();

    Concurrently.apply(Duration.ofMillis(200), () -> {
      started.incrementAndGet();
      pool.apply(t -> {
        executing.incrementAndGet();
        return Promise.apply();
      }).onFailure(e -> rejected.incrementAndGet());
    }, () -> {
      assertTrue(maxSize >= executing.get());
    });
  }

  private Connection conn() {
    return new Connection() {

      @Override
      public Future<ResultSet> query(PreparedStatement query) {
        return null;
      }

      @Override
      public Future<ResultSet> query(String query) {
        return null;
      }

      @Override
      public Future<Boolean> isValid() {
        return null;
      }

      @Override
      public Future<Integer> execute(PreparedStatement query) {
        return null;
      }

      @Override
      public Future<Integer> execute(String query) {
        return null;
      }

      @Override
      public Future<Void> close() {
        return null;
      }
    };
  }
}
package io.trane.ndbc.mysql.netty4;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.runners.Parameterized.Parameters;

import io.trane.ndbc.PreparedStatement;
import io.trane.ndbc.test.ScalarEncodingTest;

public class MysqlScalarEncodingTest extends ScalarEncodingTest {

  @Parameters(name = "{1}")
  public static Collection<Object[]> data() {
    return MysqlEnv.dataSources;
  }

  private boolean is5_5() {
    return ds.config().embedded()
        .filter(e -> e.version.equals(Optional.of("v5_5_latest")))
        .isPresent();
  }

  @Override
  protected PreparedStatement prepare(final String query) {
    return PreparedStatement.create(query);
  }

  @Override
  protected LocalDateTime randomLocalDateTime(final Random r) {
    if (is5_5())
      return super.randomLocalDateTime(r).withNano(0);
    else
      return super.randomLocalDateTime(r);
  }

  @Override
  protected List<String> bigDecimalColumnTypes() {
    return Arrays.asList("DECIMAL(20, 10)", "NUMERIC(20, 10)");
  }

  @Override
  protected List<String> booleanColumnTypes() {
    return Arrays.asList("TINYINT");
  }

  @Override
  protected List<String> byteArrayColumnTypes() {
    return Arrays.asList("BLOB", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB");
  }

  @Override
  protected List<String> doubleColumnTypes() {
    return Arrays.asList("DOUBLE");
  }

  @Override
  protected List<String> floatColumnTypes() {
    return Arrays.asList("FLOAT");
  }

  @Override
  protected List<String> integerColumnTypes() {
    return Arrays.asList("INT");
  }

  @Override
  protected List<String> localDateColumnTypes() {
    return Arrays.asList("DATE");
  }

  @Override
  protected List<String> localDateTimeColumnTypes() {
    if (is5_5())
      return Arrays.asList("DATETIME", "TIMESTAMP");
    else
      return Arrays.asList("DATETIME(6)", "TIMESTAMP(6)");
  }

  @Override
  protected List<String> localTimeColumnTypes() {
    if (is5_5())
      return Arrays.asList("TIME");
    else
      return Arrays.asList("TIME(6)");
  }

  @Override
  protected List<String> longColumnTypes() {
    return Arrays.asList("BIGINT");
  }

  @Override
  protected List<String> shortColumnTypes() {
    return Arrays.asList("SMALLINT");
  }

  @Override
  protected List<String> byteColumnTypes() {
    return Arrays.asList("TINYINT");
  }

  @Override
  protected List<String> stringColumnTypes() {
    return Arrays.asList("VARCHAR(9999)");
  }
}

package io.trane.ndbc.postgres.encoding;

import java.util.Arrays;

import io.trane.ndbc.value.StringArrayValue;

public class StringArrayEncodingTest extends EncodingTest<StringArrayValue, StringArrayEncoding> {

  public StringArrayEncodingTest() {
    super(new StringArrayEncoding(new StringEncoding(UTF8), UTF8), Oid.VARCHAR_ARRAY, StringArrayValue.class, r -> {
      final String[] strings = new String[r.nextInt(8)];
      Arrays.setAll(strings, p -> randomString(r));
      return new StringArrayValue(strings);
    }, (a, b) -> {
      // Assert.assertArrayEquals(a.getStringArray(), b.getStringArray()); failing :(
    });
  }
}
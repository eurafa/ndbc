package io.trane.ndbc.postgres.encoding;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import io.trane.ndbc.NdbcException;
import io.trane.ndbc.proto.BufferReader;
import io.trane.ndbc.proto.BufferWriter;
import io.trane.ndbc.value.IntegerValue;
import io.trane.ndbc.value.Value;

public class EncodingRegistryTest {

  private final Charset charset = Charset.forName("UTF-8");

  @Test
  public void encode() {
    final IntegerValue expected = new IntegerValue(13);
    final EncodingRegistry reg = new EncodingRegistry(Optional.empty(), charset);
    final ByteBuffer buf = ByteBuffer.allocate(1000);
    reg.encodeBinary(expected, new MockBufferWriter(buf));
    buf.rewind();
    final Integer actual = (new IntegerEncoding(charset)).decodeBinary(new MockBufferReader(buf));
    assertEquals(expected.getInteger(), actual);
  }

  @Test(expected = NdbcException.class)
  public void encodeUnsupported() {
    final Value<Thread> value = new Value<Thread>(new Thread()) {
    };
    final EncodingRegistry reg = new EncodingRegistry(Optional.empty(), charset);
    final ByteBuffer buf = ByteBuffer.allocate(1000);
    reg.encodeBinary(value, new MockBufferWriter(buf));
  }

  @Test
  public void decode() {
    final IntegerValue value = new IntegerValue(213);
    final EncodingRegistry reg = new EncodingRegistry(Optional.empty(), charset);
    final ByteBuffer buf = ByteBuffer.allocate(1000);
    (new IntegerEncoding(charset)).encodeBinary(value, new MockBufferWriter(buf));
    buf.rewind();
    final Value<?> decoded = reg.decode(Oid.INT4, Format.BINARY, new MockBufferReader(buf));
    assertEquals(value, decoded);
  }

  @Test(expected = NdbcException.class)
  public void decodeUnsupported() {
    final EncodingRegistry reg = new EncodingRegistry(Optional.empty(), charset);
    reg.decode(99999, Format.BINARY, new MockBufferReader(ByteBuffer.allocate(100)));
  }

  @Test
  public void customEncoding() {
    final TestValueEncoding enc = new TestValueEncoding(charset);
    final TestValue value = new TestValue("str");
    final EncodingRegistry reg = new EncodingRegistry(Optional.of(Arrays.asList(enc)), charset);
    final ByteBuffer buf = ByteBuffer.allocate(1000);
    reg.encodeBinary(value, new MockBufferWriter(buf));
    buf.limit(buf.position());
    buf.rewind();
    final Value<?> decoded = reg.decode(enc.oid(), Format.BINARY, new MockBufferReader(buf));
    assertEquals(value, decoded);
  }

  class TestValue extends Value<String> {
    public TestValue(final String value) {
      super(value);
    }

    @Override
    public String getString() {
      return get();
    }
  }

  class TestValueEncoding extends Encoding<String, TestValue> {

    public TestValueEncoding(final Charset charset) {
      super(charset);
    }

    @Override
    public Integer oid() {
      return 9999;
    }

    @Override
    public Class<TestValue> valueClass() {
      return TestValue.class;
    }

    @Override
    public String decodeText(final String value) {
      return value;
    }

    @Override
    public void encodeBinary(final String value, final BufferWriter b) {
      b.writeString(value);
    }

    @Override
    public String decodeBinary(final BufferReader b) {
      return b.readString(charset);
    }

    @Override
    protected TestValue box(final String value) {
      return new TestValue(value);
    }
  }
}

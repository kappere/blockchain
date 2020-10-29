package com.wataru.blockchain.core.primitive.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.SerializeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ByteArraySerializer implements ByteSerializable {
    @Getter
    protected byte[] data;
    public ByteArraySerializer() {}
    public ByteArraySerializer(byte[] d) {
        data = d;
    }

    @Override
    public byte[] serialize() {
        return data;
    }

    @Override
    public int deserialize(byte[] data) {
        this.data = data;
        return data.length;
    }

    @Override
    public int hashCode() {
        if (data == null || data.length == 0) {
            return 0;
        }
        return Byte.hashCode(data[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteArraySerializer)) {
            return false;
        }
        ByteArraySerializer output = (ByteArraySerializer) o;
        if (data == null && output.getData() == null) {
            return true;
        }
        if (data == null || output.getData() == null) {
            return false;
        }
        if (data.length != output.getData().length) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != output.getData()[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return toHex();
    }

    public String toHex() {
        return EncodeUtil.bytesToHexString(data);
    }

    public String toLittleEndianNumberHex() {
        byte[] reverseByteArray = EncodeUtil.reverseByteArray(Arrays.copyOf(data, data.length));
        return EncodeUtil.bytesToHexString(reverseByteArray);
    }

    public static class ByteArrayToHexSerializer extends JsonSerializer<ByteArraySerializer> {
        @Override
        public void serialize(ByteArraySerializer d,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeObject(d.toHex());
        }
    }

    public static class Builder {
        private ByteArrayOutputStream bos;
        public Builder() {
            bos = new ByteArrayOutputStream();
        }
        private void doError(Exception e) {
            try {
                bos.flush();
                bos.close();
            } catch (IOException ioException) {
                log.error("", ioException);
                throw new RuntimeException(e);
            }
            throw new RuntimeException(e);
        }
        public Builder push(byte[] d, boolean withLength) {
            try {
                if (withLength) {
                    SerializeUtil.writeByte(bos, (byte) d.length);
                }
                SerializeUtil.writeByteArray(bos, d);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(byte d) {
            try {
                SerializeUtil.writeByte(bos, d);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(boolean d) {
            try {
                SerializeUtil.writeByte(bos, (byte) (d ? 0x01 : 0x00));
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(int d) {
            try {
                SerializeUtil.writeInt(bos, d);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(long d) {
            try {
                SerializeUtil.writeLong(bos, d);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(String d) {
            try {
                SerializeUtil.writeString(bos, d);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public Builder push(Map map) {
            try {
                SerializeUtil.writeInt(bos, map.size());
                map.forEach((k, v) -> {
                    dealMapInPush(k);
                    dealMapInPush(v);
                });
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }

        private void dealMapInPush(Object k) {
            if (k instanceof Integer) {
                push((Integer) k);
            } else if (k instanceof Long) {
                push((Long) k);
            } else if (k instanceof String) {
                push((String) k);
            } else if (k instanceof List) {
                push(4, (List) k);
            } else if (k instanceof Map) {
                push((Map) k);
            } else if (k instanceof ByteSerializable) {
                push((ByteSerializable) k, false);
            }
        }

        public <T extends ByteSerializable> Builder push(T d, boolean withLength) {
            try {
                byte[] b = d.serialize();
                if (withLength) {
                    SerializeUtil.writeByte(bos, (byte) b.length);
                }
                SerializeUtil.writeByteArray(bos, b);
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }

        /**
         * 序列化列表
         * @param length 列表长度的大小
         */
        public Builder push(int length, List d) {
            try {
                if (length == 1) {
                    SerializeUtil.writeByte(bos, (byte) d.size());
                } else if (length == 4) {
                    SerializeUtil.writeInt(bos,  d.size());
                } else if (length == 8) {
                    SerializeUtil.writeLong(bos, d.size());
                }
                for (Object t : d) {
                    dealMapInPush(t);
                }
            } catch (Exception e) {
                doError(e);
            }
            return this;
        }
        public <T extends ByteSerializable> T build(Function<byte[], T> function) {
            try {
                return function.apply(bos.toByteArray());
//                return new Serializer(bos.toByteArray());
            } finally {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    public static class Extractor {
        private byte[] bytes;
        private int index;
        private AtomicInteger tmpSize = new AtomicInteger();
        public Extractor(byte[] bytes) {
            this.bytes = bytes;
            this.index = 0;
        }
        public Extractor pullByte(Consumer<Byte> consumer) {
            consumer.accept(bytes[index]);
            index += 1;
            return this;
        }
        public Extractor pullInt(Consumer<Integer> consumer) {
            consumer.accept(bytes[index] & 0xff
                    | ((bytes[index + 1] & 0xff) << 8)
                    | ((bytes[index + 2] & 0xff) << 16)
                    | ((bytes[index + 3] & 0xff) << 24)
            );
            index += 4;
            return this;
        }
        public Extractor pullString(Consumer<String> consumer) {
            StringBuilder sb = new StringBuilder();
            byte b;
            while ((b = bytes[index++]) != (byte) 0x00) {
                sb.append((char) b);
            }
            consumer.accept(sb.toString());
            return this;
        }
        public Extractor forEach(int count, Consumer<Extractor> action) {
            for (int i = 0; i < count; i++) {
                action.accept(this);
            }
            return this;
        }
        public Extractor pullLong(Consumer<Long> consumer) {
            consumer.accept(bytes[index] & 0xff
                    | ((bytes[index + 1] & 0xff) << 8)
                    | ((bytes[index + 2] & 0xff) << 16)
                    | ((long) (bytes[index + 3] & 0xff) << 24)
                    | ((long) (bytes[index + 4] & 0xff) << 32)
                    | ((long) (bytes[index + 5] & 0xff) << 40)
                    | ((long) (bytes[index + 6] & 0xff) << 48)
                    | ((long) (bytes[index + 7] & 0xff) << 56)
            );
            index += 8;
            return this;
        }
        public Extractor pullByteArray(int length, Consumer<byte[]> consumer) {
            consumer.accept(Arrays.copyOfRange(bytes, index, index + length));
            index += length;
            return this;
        }
        public Extractor pullByteArrayWithSize(Consumer<byte[]> consumer) {
            pullByte(d -> tmpSize.set(d & 0xff));
            consumer.accept(Arrays.copyOfRange(bytes, index, index + tmpSize.get()));
            index += tmpSize.get();
            return this;
        }
        public <T extends ByteSerializable> Extractor pullObject(Supplier<T> supplier, Consumer<T> consumer) {
            T t = supplier.get();
            index += t.deserialize(Arrays.copyOfRange(bytes, index, bytes.length));
            consumer.accept(t);
            return this;
        }
        public <T extends ByteSerializable> Extractor pullObjectWithSize(Supplier<T> supplier, Consumer<T> consumer) {
            pullByte(d -> tmpSize.set(d & 0xff));
            T t = supplier.get();
            t.deserialize(Arrays.copyOfRange(bytes, index, index + tmpSize.get()));
            index += tmpSize.get();
            consumer.accept(t);
            return this;
        }
        /**
         * 反序列化列表
         * @param length 列表长度的大小
         */
        public <T extends ByteSerializable> Extractor pullList(int length, Supplier<T> supplier, Consumer<List<T>> consumer) {
            List<T> result = new ArrayList<>();
            if (length == 1) {
                pullByte(d -> tmpSize.set(d & 0xff));
            } else if (length == 4) {
                pullInt(d -> tmpSize.set(d));
            } else if (length == 8) {
                pullLong(d -> tmpSize.set(Math.toIntExact(d)));
            }
            for (int i = 0; i < tmpSize.get(); i++) {
                pullObject(supplier, result::add);
            }
            consumer.accept(result);
            return this;
        }
        public <T extends ByteSerializable> Extractor pullListWithObjectSize(int length, Supplier<T> supplier, Consumer<List<T>> consumer) {
            List<T> result = new ArrayList<>();
            if (length == 1) {
                pullByte(d -> tmpSize.set(d & 0xff));
            } else if (length == 4) {
                pullInt(d -> tmpSize.set(d));
            } else if (length == 8) {
                pullLong(d -> tmpSize.set(Math.toIntExact(d)));
            }
            for (int i = 0; i < tmpSize.get(); i++) {
                pullObjectWithSize(supplier, result::add);
            }
            consumer.accept(result);
            return this;
        }

        public int complete() {
            return index;
        }
    }
}

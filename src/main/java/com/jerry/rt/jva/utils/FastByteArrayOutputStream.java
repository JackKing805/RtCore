package com.jerry.rt.jva.utils;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class FastByteArrayOutputStream extends OutputStream {

    private final FastByteBuffer buffer;


    public FastByteArrayOutputStream() {
        this(1024);
    }


    public FastByteArrayOutputStream(int size) {
        buffer = new FastByteBuffer(size);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.append(b, off, len);
    }

    @Override
    public void write(int b) {
        buffer.append((byte) b);
    }

    public int size() {
        return buffer.size();
    }


    @Override
    public void close() {
        // nop
    }

    public void reset() {
        buffer.reset();
    }


    public void writeTo(OutputStream out) throws IOException {
        final int index = buffer.index();
        if(index < 0){
            // 无数据写出
            return;
        }
        byte[] buf;
        try {
            for (int i = 0; i < index; i++) {
                buf = buffer.array(i);
                out.write(buf);
            }
            out.write(buffer.array(index), 0, buffer.offset());
        } catch (IOException e) {
            throw new IOException(e);
        }
    }



    public byte[] toByteArray() {
        return buffer.toArray();
    }

    @Override
    public String toString() {
        return toString(Charset.defaultCharset());
    }


    public String toString(String charsetName) {
        if (charsetName.length()==0){
            return toString(Charset.defaultCharset());
        }else {
            return toString(Charset.forName(charsetName));
        }
    }


    public String toString(Charset charset) {
        Charset charset1 = charset;
        if (charset1==null){
            charset1 = Charset.defaultCharset();
        }
        return new String(toByteArray(), charset1);
    }

}

package com.jerry.rt.jva.utils;

import com.jerry.rt.core.http.request.impl.NoWaitBufferedInputStream;
import com.jerry.rt.core.http.request.model.MultipartFileHeader;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;


public class MultipartRequestInputStream extends NoWaitBufferedInputStream {

    public MultipartRequestInputStream(InputStream in) {
        super(in);
    }


    public byte readByte() throws IOException {
        int i = super.read();
        if (i == -1) {
            throw new IOException("End of HTTP request stream reached");
        }
        return (byte) i;
    }

    public void skipBytes(long i) throws IOException {
        long len = super.skip(i);
        if (len != i) {
            throw new IOException("Unable to skip data in HTTP request");
        }
    }


    protected byte[] boundary;


    public byte[] readBoundary() throws IOException {
        ByteArrayOutputStream boundaryOutput = new ByteArrayOutputStream(1024);
        byte b;
        // skip optional whitespaces
        //noinspection StatementWithEmptyBody
        while ((b = readByte()) <= ' ') {
        }
        boundaryOutput.write(b);

        // now read boundary chars
        while ((b = readByte()) != '\r') {
            boundaryOutput.write(b);
        }
        if (boundaryOutput.size() == 0) {
            throw new IOException("Problems with parsing request: invalid boundary");
        }
        skipBytes(1);
        boundary = new byte[boundaryOutput.size() + 2];
        System.arraycopy(boundaryOutput.toByteArray(), 0, boundary, 2, boundary.length - 2);
        boundary[0] = '\r';
        boundary[1] = '\n';
        return boundary;
    }

    // ---------------------------------------------------------------- data header

    protected MultipartFileHeader lastHeader;

    public MultipartFileHeader getLastHeader() {
        return lastHeader;
    }


    public MultipartFileHeader readDataHeader(Charset encoding) throws IOException {
        String dataHeader = readDataHeaderString(encoding);
        if (dataHeader != null) {
            lastHeader = new MultipartFileHeader(dataHeader);
        } else {
            lastHeader = null;
        }
        return lastHeader;
    }


    protected String readDataHeaderString(Charset charset) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte b;
        while (true) {
            // end marker byte on offset +0 and +2 must be 13
            if ((b = readByte()) != '\r') {
                data.write(b);
                continue;
            }
            mark(4);
            skipBytes(1);
            int i = read();
            if (i == -1) {
                // reached end of stream
                return null;
            }
            if (i == '\r') {
                reset();
                break;
            }
            reset();
            data.write(b);
        }
        skipBytes(3);
        return charset == null ? data.toString() : data.toString(charset.name());
    }


    public String readString(Charset charset) throws IOException {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        copy(out);
        return out.toString(charset);
    }

    public long copy(OutputStream out) throws IOException {
        long count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
        }
        return count;
    }


    public long copy(OutputStream out, long limit) throws IOException {
        long count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
            if (count > limit) {
                break;
            }
        }
        return count;
    }


    public long skipToBoundary() throws IOException {
        long count = 0;
        while (true) {
            byte b = readByte();
            count++;
            if (isBoundary(b)) {
                break;
            }
        }
        return count;
    }


    public boolean isBoundary(byte b) throws IOException {
        int boundaryLen = boundary.length;
        mark(boundaryLen + 1);
        int bpos = 0;
        while (b == boundary[bpos]) {
            b = readByte();
            bpos++;
            if (bpos == boundaryLen) {
                return true; // boundary found!
            }
        }
        reset();
        return false;
    }
}

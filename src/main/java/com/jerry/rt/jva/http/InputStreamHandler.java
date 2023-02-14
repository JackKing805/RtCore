package com.jerry.rt.jva.http;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @className: InputStreamHandler
 * @description: TODO 类描述
 * @author: Jerry
 * @date: 2023/2/13:22:28
 **/
public class InputStreamHandler {
    private String startLine;
    private InputStream is;

    OutputStream os;
    char[] buf = new char[2048];
    int pos;
    StringBuffer lineBuf;
    Headers hdrs = null;



    public InputStreamHandler(InputStream inputStream,OutputStream outputStream) throws IOException {
        this.is = inputStream;
        this.os = outputStream;
        do {
            this.startLine = this.readLine();
            if (this.startLine == null) {
                return;
            }
        } while(this.startLine != null && this.startLine.equals(""));
    }


    public Headers headers() throws IOException {
        if (this.hdrs != null) {
            return this.hdrs;
        } else {
            this.hdrs = new Headers();
            char[] s = new char[10];
            int len = 0;
            int firstc = this.is.read();
            int keyend;
            if (firstc == 13 || firstc == 10) {
                keyend = this.is.read();
                if (keyend == 13 || keyend == 10) {
                    return this.hdrs;
                }

                s[0] = (char)firstc;
                len = 1;
                firstc = keyend;
            }

            while(firstc != 10 && firstc != 13 && firstc >= 0) {
                keyend = -1;
                boolean inKey = firstc > 32;
                s[len++] = (char)firstc;

                label116:
                while(true) {
                    int c;
                    if ((c = this.is.read()) < 0) {
                        firstc = -1;
                        break;
                    }

                    switch (c) {
                        case 9:
                            c = 32;
                        case 32:
                            inKey = false;
                            break;
                        case 10:
                        case 13:
                            firstc = this.is.read();
                            if (c == 13 && firstc == 10) {
                                firstc = this.is.read();
                                if (firstc == 13) {
                                    firstc = this.is.read();
                                }
                            }

                            if (firstc == 10 || firstc == 13 || firstc > 32) {
                                break label116;
                            }

                            c = 32;
                            break;
                        case 58:
                            if (inKey && len > 0) {
                                keyend = len;
                            }

                            inKey = false;
                    }

                    if (len >= s.length) {
                        char[] ns = new char[s.length * 2];
                        System.arraycopy(s, 0, ns, 0, len);
                        s = ns;
                    }

                    s[len++] = (char)c;
                }

                while(len > 0 && s[len - 1] <= ' ') {
                    --len;
                }

                String k;
                if (keyend <= 0) {
                    k = null;
                    keyend = 0;
                } else {
                    k = String.copyValueOf(s, 0, keyend);
                    if (keyend < len && s[keyend] == ':') {
                        ++keyend;
                    }

                    while(keyend < len && s[keyend] <= ' ') {
                        ++keyend;
                    }
                }

                String v;
                if (keyend >= len) {
                    v = new String();
                } else {
                    v = String.copyValueOf(s, keyend, len - keyend);
                }

                if (this.hdrs.size() >= 200) {
                    throw new IOException("Maximum number of request headers (sun.net.httpserver.maxReqHeaders) exceeded, " + 200 + ".");
                }

                if (k == null) {
                    k = "";
                }

                this.hdrs.add(k, v);
                len = 0;
            }

            return this.hdrs;
        }
    }

    public InputStream inputStream() {
        return this.is;
    }
    public OutputStream outputStream() {
        return this.os;
    }

    public String readLine() throws IOException {
        boolean gotCR = false;
        boolean gotLF = false;
        this.pos = 0;
        this.lineBuf = new StringBuffer();

        while(!gotLF) {
            int c = this.is.read();
            if (c == -1) {
                return null;
            }

            if (gotCR) {
                if (c == 10) {
                    gotLF = true;
                } else {
                    gotCR = false;
                    this.consume(13);
                    this.consume(c);
                }
            } else if (c == 13) {
                gotCR = true;
            } else {
                this.consume(c);
            }
        }

        this.lineBuf.append(this.buf, 0, this.pos);
        return new String(this.lineBuf);
    }

    private void consume(int c) {
        if (this.pos == 2048) {
            this.lineBuf.append(this.buf);
            this.pos = 0;
        }

        this.buf[this.pos++] = (char)c;
    }

    public String requestLine() {
        return this.startLine;
    }
}

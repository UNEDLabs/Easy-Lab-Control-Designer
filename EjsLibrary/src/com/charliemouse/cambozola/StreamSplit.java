/**
 ** com/charliemouse/cambozola/shared/StreamSplit.java
 **  Copyright (C) Andy Wilcock, 2001.
 **  Available from http://www.charliemouse.com
 **
 ** This file m_inputStream part of the CamViewer package (c) Andy Wilcock, 2001.
 ** Available from http://www.charliemouse.com
 **
 **  Cambozola m_inputStream free software; you can redistribute it and/or modify
 **  it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  (at your option) any later version.
 **
 **  Cambozola m_inputStream distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU General Public License for more details.
 **
 **  You should have received a copy of the GNU General Public License
 **  along with Cambozola; if not, write to the Free Software
 **  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **/
package com.charliemouse.cambozola;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;

public class StreamSplit {

    private DataInputStream m_dis;
    private boolean m_streamEnd;
    private char lineBuffer[];//Hector

    public StreamSplit(DataInputStream dis) {
        m_dis = dis;
        m_streamEnd = false;
    }

    private final String readLine(InputStream in) throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

        loop:
        while (true) {
            switch (c = in.read()) {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = in.read();
                    if ((c2 != '\n') && (c2 != -1)) {
                        if (!(in instanceof PushbackInputStream)) {
                            //this.in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream) in).unread(c2);
                    }
                    break loop;

                default:
                    if (--room < 0) {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        lineBuffer = buf;
                    }
                    buf[offset++] = (char) c;
                    break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    public Hashtable<String, String> readHeaders() throws IOException {
        Hashtable<String, String> ht = new Hashtable<String, String>();
        String response = null;
        boolean satisfied = false;

        do {
            //response = m_dis.readLine();
            response = readLine(m_dis);
            if (response == null) {
                m_streamEnd = true;
                break;
            } else if (response.equals("")) {
                if (satisfied) {
                    break;
                }
//                else {
//                    // Carry on...
//                }
            } else {
                satisfied = true;
            }
            int idx = response.indexOf(":");
            if (idx == -1) {
                continue;
            }
            String tag = response.substring(0, idx);
            String val = response.substring(idx + 1).trim();
            ht.put(tag.toLowerCase(), val);
        } while (true);
        return ht;
    }

    public void skipToBoundary(String boundary) throws IOException {
        readToBoundary(boundary);
    }

    public byte[] readToBoundary(String boundary) throws IOException {
        ResizableByteArrayOutputStream baos = new ResizableByteArrayOutputStream();
        StringBuffer lastLine = new StringBuffer();
        int lineidx = 0;
        int chidx = 0;
        byte ch;
        do {
            try {
                ch = m_dis.readByte();
            } catch (EOFException e) {
                m_streamEnd = true;
                break;
            }
            if (ch == '\n' || ch == '\r') {
                //
                // End of line...
                //
                String lls = lastLine.toString();
                if (boundary != null && lls.startsWith(boundary)) {
                    String btest = lls.substring(boundary.length());
                    if (btest.equals("--")) {
                        m_streamEnd = true;
                    }
                    chidx = lineidx;
                    break;
                }
                lastLine = new StringBuffer();
                lineidx = chidx + 1;
            } else {
                lastLine.append((char) ch);
            }
            chidx++;
            baos.write(ch);
        } while (true);
        //
        baos.close();
        baos.resize(chidx);
        return baos.toByteArray();
    }

    public boolean isAtStreamEnd() {
        return m_streamEnd;
    }
}

class ResizableByteArrayOutputStream extends ByteArrayOutputStream {

    public ResizableByteArrayOutputStream() {
        super();
    }

    public void resize(int size) {
        count = size;
    }
}

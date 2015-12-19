/*
 * Copyright 2015 Ignacio del Valle Alles idelvall@brutusin.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class MetaDataInputStream extends InputStream {

    private final String name;
    private final String contentType;
    private final Long length;
    private final Long lastModified;
    private final InputStream is;

    public MetaDataInputStream(InputStream is, String name, String contentType, Long length, Long lastModified) {
        this.contentType = contentType;
        this.length = length;
        this.is = is;
        this.name = name;
        this.lastModified = lastModified;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getLength() {
        return length;
    }

    public InputStream getIs() {
        return is;
    }

    public int read() throws IOException {
        return is.read();
    }

    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return is.skip(n);
    }

    public int available() throws IOException {
        return is.available();
    }

    public void close() throws IOException {
        is.close();
    }

    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        is.reset();
    }

    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public int hashCode() {
        return is.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetaDataInputStream other = (MetaDataInputStream) obj;
        if (this.is != other.is && (this.is == null || !this.is.equals(other.is))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (name != null ? (name + ": ") : "") + is.toString();
    }

}

/*
 * Copyright 2015 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.brutusin.commons.io.LineReader;

public final class Miscellaneous {

    private final static Logger LOGGER = Logger.getLogger(Miscellaneous.class.getName());

    private static final ErrorHandler LOG_HANDLER = new ErrorHandler() {
        public void onThrowable(Throwable th) {
            LOGGER.log(Level.WARNING, th.getMessage(), th);
        }
    };

    private Miscellaneous() {
    }

    public static String getStrackTrace(Throwable th) {
        if (th == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        th.printStackTrace(pw);
        pw.close();
        return baos.toString();
    }

    public static String append(String token, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the specified object array.
     *
     * @param array Array to obtain its representation.
     * @return the elements of array separated by commas.
     */
    public static String arrayToString(Object array) {
        return arrayToString(array, ",");
    }

    public static final String arrayToString(Object arr, String separator) {
        if (arr == null) {
            return null;
        }
        if (!arr.getClass().isArray()) {
            throw new IllegalArgumentException("arr must be an array");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Array.getLength(arr); i++) {
            Object element = Array.get(arr, i);
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(element);
        }
        return sb.toString();
    }

    public static String getRootCauseMessage(final Throwable th) {
        Throwable root = getRootCause(new ArrayList(4), th);
        if (root == null) {
            return null;
        }
        return root.getMessage();
    }

    private static Charset toCharset(Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    public static Charset toCharset(String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }

    public static InputStream toInputStream(String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(String input, Charset encoding) {
        if (input == null) {
            return null;
        }
        return new ByteArrayInputStream(input.getBytes(toCharset(encoding)));
    }

    public static String toString(InputStream is, String encoding)
            throws IOException {

        if (is == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        LineReader lr = new LineReader(is, encoding) {
            @Override
            protected void processLine(String line) throws Exception {
                if (getLineNumber() > 1) {
                    sb.append("\n");
                }
                sb.append(line);
            }

            @Override
            protected void onExceptionFound(Exception ex) {
                throw new RuntimeException(ex);
            }
        };
        try {
            try {
                lr.run();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }

    private static Throwable getRootCause(final List<Throwable> visited, final Throwable th) {
        if (th == null || visited.contains(th)) {
            return null;
        }
        Throwable cause = th.getCause();
        if (cause == null) {
            return th;
        }
        visited.add(th);
        return getRootCause(visited, cause);
    }

    public static <T> List<T> createList(T... elements) {
        if (elements == null) {
            return null;
        }
        ArrayList<T> ret = new ArrayList(elements.length);
        for (int i = 0; i < elements.length; i++) {
            ret.add(elements[i]);
        }
        return ret;
    }

    /**
     * <p>
     * Counts how many times the substring appears in the larger String.</p>
     *
     * <p>
     * A <code>null</code> or empty ("") String input returns
     * <code>0</code>.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
     * </pre>
     * <p>
     *
     * @param str the String to check, may be null
     * @param subStrRegExp the substring reg expression to count, may be null
     * @return the number of occurrences, 0 if either String is
     * <code>null</code>
     */
    public static int countMatches(String str, String subStrRegExp) {
        if (isEmpty(str) || isEmpty(subStrRegExp)) {
            return 0;
        }
        Pattern p = Pattern.compile(subStrRegExp);
        Matcher m = p.matcher(str);
        int count = 0;
        while (m.find()) {
            count += 1;
        }
        return count;
    }

    /**
     * <p>
     * Checks if a String is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the
     * String. That functionality is available in isBlank().</p>
     * <p>
     * NOTE: Copied from apache commons-lang StringUtils.</p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * The parent directory will be created if it does not exist. The file will
     * be created if it does not exist. An exception is thrown if the file
     * object exists but is a directory. An exception is thrown if the file
     * exists but cannot be written to. An exception is thrown if the parent
     * directory cannot be created.
     * <p>
     * NOTE: Copied from apache commons-io FileUtils.</p>
     *
     * @param file the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the end of the
     * file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param charset the encoding to use, {@code null} means platform default
     * @throws IOException in case of an I/O error
     */
    public static void writeStringToFile(File file, String data, String charset) throws IOException {
        FileOutputStream fos = openOutputStream(file, false);
        fos.write(data.getBytes(charset));
        fos.close();
    }

    /**
     * Determines whether the specified file is a Symbolic Link rather than an
     * actual file.
     * <p>
     * Will not return true if there is a Symbolic Link anywhere in the path,
     * only if the specific file is.
     * <p>
     * <b>Note:</b> the current implementation always returns {@code false} if
     * the system is detected as Windows.
     *
     * Copied from org.apache.commons.io.FileuUils
     *
     * @param file the file to check
     * @return true if the file is a Symbolic Link
     * @throws IOException if an IO error occurs while checking the file
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (File.separatorChar == '\\') {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * Copied from org.apache.commons.io.FileuUils
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all
     * sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * Copied from org.apache.commons.io.FileuUils
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            file.setWritable(true);
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message
                        = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * Copied from org.apache.commons.io.FileuUils
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message
                    = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Asynchronous buffered writing from is to os. Finally closes inputstream.
     *
     * @param is
     * @param logger
     * @param os
     */
    public static Thread pipeAsynchronously(final InputStream is, final ErrorHandler errorHandler, final boolean closeResources, final OutputStream... os) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    pipeSynchronously(is, closeResources, os);
                } catch (Throwable th) {
                    if (errorHandler != null) {
                        errorHandler.onThrowable(th);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
        return t;
    }

    public static Thread pipeAsynchronously(final InputStream is, final OutputStream... os) {
        return pipeAsynchronously(is, LOG_HANDLER, true, os);
    }

    public static Thread pipeAsynchronously(final InputStream is, boolean closeResources, final OutputStream... os) {
        return pipeAsynchronously(is, LOG_HANDLER, closeResources, os);
    }

    public static long pipeSynchronously(final InputStream is, final OutputStream... os) throws IOException {
        return pipeSynchronously(is, 1024, true, os);
    }

    public static long pipeSynchronously(final InputStream is, boolean closeResources, final OutputStream... os) throws IOException {
        return pipeSynchronously(is, 1024, closeResources, os);
    }

    public static long pipeSynchronously(final InputStream is, int bufferSize, boolean closeResources, final OutputStream... os) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int r = -1;
            long read = 0;
            while ((r = is.read(buffer)) > 0) {
                for (int i = 0; i < os.length; i++) {
                    if (os[i] != null) {
                        os[i].write(buffer, 0, r);
                    }
                }
                read += r;
            }
            for (int i = 0; i < os.length; i++) {
                if (os[i] != null) {
                    os[i].flush();
                }
            }
            return read;
        } finally {
            for (int i = 0; i < os.length; i++) {
                if (os[i] != null) {
                    os[i].flush();
                }
            }
            if (closeResources) {
                is.close();
                for (int i = 0; i < os.length; i++) {
                    if (os[i] != null) {
                        os[i].close();
                    }
                }
            }
        }
    }

    public static long pipeSynchronously(final BufferedReader br, final OutputStream... os) throws IOException {
        return pipeSynchronously(br, true, os);
    }

    public static long pipeSynchronously(final BufferedReader br, boolean closeResources, final OutputStream... os) throws IOException {
        long lineCounter = 0;
        String line;
        try {
            while ((line = br.readLine()) != null) {
                lineCounter++;
                for (int i = 0; i < os.length; i++) {
                    if (os[i] != null) {
                        synchronized (os[i]) {
                            os[i].write((line + "\n").getBytes());
                        }
                    }
                }
            }
            return lineCounter;
        } finally {
            for (int i = 0; i < os.length; i++) {
                if (os[i] != null) {
                    os[i].flush();
                }
            }
            if (closeResources) {
                br.close();
                for (int i = 0; i < os.length; i++) {
                    if (os[i] != null) {
                        os[i].close();
                    }
                }
            }
        }
    }

    /**
     * Replaces all "\" and "/" in the specified file path by
     * <code>file.separator</code> system property.
     *
     * @param filePath the original file path.
     * @return the formatted file path.
     */
    public static String formatFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return normalizePath(filePath.replaceAll("//*", "/").replaceAll("\\*", "\\").replaceAll("/", "\\" + System.getProperty("file.separator")).replaceAll("\\\\", "\\" + System.getProperty("file.separator")), System.getProperty("file.separator").equals("/"));

    }

    private static String normalizePath(String path, boolean linuxStyle) {
        String separator = linuxStyle ? "/" : "\\\\";
        String[] tokens = path.split(separator);
        Stack<String> stk = new Stack<String>();
        boolean tokenPassed = false;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(".")) {
                continue;
            }
            if (tokenPassed) {
                if (tokens[i].equals("..")) {
                    stk.pop();
                } else {
                    stk.add(tokens[i]);
                }
            } else {
                if (!tokens[i].equals("..")) {
                    tokenPassed = true;
                }
                stk.add(tokens[i]);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stk.size(); i++) {
            String element = stk.get(i);
            if (i > 0) {
                sb.append(linuxStyle ? "/" : "\\");
            }
            sb.append(element);
        }
        return sb.toString();
    }

    /**
     * Creates a file in the specified path. Creates also any necessary folder
     * needed to achieve the file level of nesting.
     *
     * @param filePath the path of file to create.
     * @return the new created file. <code>null</code> if the file can no be
     * created.
     * @throws IOException if an IO error occurs.
     */
    public static File createFile(String filePath) throws IOException {

        boolean isDirectory = filePath.endsWith("/") || filePath.endsWith("\\");

        String formattedFilePath = formatFilePath(filePath);

        File f = new File(formattedFilePath);

        if (f.exists()) {
            return f;
        }

        if (isDirectory) {
            f.mkdirs();
        } else {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        if (f.exists()) {
            f.setExecutable(true, false);
            f.setReadable(true, false);
            f.setWritable(true, false);
            return f;
        }
        throw new IOException("Error creating file: " + f.getAbsolutePath());
    }

    public static File createDirectory(File file) throws IOException {
        return createDirectory(file.getAbsolutePath());
    }

    public static File createDirectory(String folderPath) throws IOException {
        if (!folderPath.endsWith("/")) {
            folderPath = folderPath + "/";
        }
        return createFile(folderPath);
    }

    public static <T> T getInstance(Class<T> service, boolean required) {
        ServiceLoader<T> sl = ServiceLoader.load(service);
        Iterator<T> it = sl.iterator();
        List<T> instances = new ArrayList();
        while (it.hasNext()) {
            instances.add(it.next());
        }
        if (instances.isEmpty()) {
            if (required) {
                throw new Error("No '" + service.getName() + "' service provider found.");
            }
            return null;
        } else if (instances.size() > 1) {
            throw new Error("Multiple '" + service.getName() + "' service providers found: " + instances);
        }
        return instances.get(0);
    }

    public static <T> T getInstance(Class<T> service) {
        return getInstance(service, true);
    }

    public static Class getClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return getClass(pt.getRawType());
        }
        return Object.class;
    }

    public static int getUnixId(Process p) {
        if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Field fPid = p.getClass().getDeclaredField("pid");
                if (!fPid.isAccessible()) {
                    fPid.setAccessible(true);
                }
                return fPid.getInt(p);
            } catch (Exception ex) {
                return -1;
            }
        }
        return -1;
    }

    public static long getGlobalAutoIncremental(File file) throws IOException {
        if (!file.exists()) {
            createFile(file.getAbsolutePath());
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rws");
        FileLock lock = null;
        long value = 0;
        try {
            while ((lock = raf.getChannel().tryLock()) == null) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (raf.length() == 0) {
                value = 0;
            } else {
                value = raf.readLong();
            }
            value++;
            raf.seek(0);
            raf.writeLong(value);
        } finally {
            if (lock != null) {
                lock.release();
            }
            raf.close();
        }
        return value;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.ROOT, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static long parseHumanReadableByteCount(String s) {
        Pattern pattern = Pattern.compile("([0-9.]+)\\s*([kKMGTPE]?)(i?)B?");
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            float value = Float.parseFloat(matcher.group(1));
            int exp;
            if (matcher.group(2).isEmpty()) {
                exp = -1;
            } else {
                exp = "KMGTPE".indexOf(matcher.group(2).toUpperCase());
            }
            int unit;
            if (matcher.group(3).isEmpty()) {
                unit = 1000;
            } else {
                unit = 1024;
            }
            return (long) (value * Math.pow(unit, exp + 1));
        }
        throw new IllegalArgumentException("Invalid unit in memory representation '" + s + "' ");
    }

    public static void main(String[] args) {

        System.out.println(humanReadableByteCount(1899999976158l, true));
        System.out.println(parseHumanReadableByteCount("134K"));
    }
}

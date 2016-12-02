/*
 * Copyright 2016 Ignacio del Valle Alles idelvall@brutusin.org.
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
package org.brutusin.commons.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class ProcessUtils {

    private ProcessUtils() {
    }

    public static String executeProcess(String... command) throws ProcessException, InterruptedException {
        return executeProcess(null, null, command);
    }

    public static String executeProcess(File workingFolder, String... command) throws ProcessException, InterruptedException {
        return executeProcess(null, workingFolder, command);
    }

    /**
     * Executes a native process with small stdout and stderr payloads
     *
     * @param env
     * @param workingFolder
     * @param command
     * @return Merged stderr and stdout
     * @throws ProcessException
     * @throws InterruptedException
     */
    public static String executeProcess(Map<String, String> env, File workingFolder, String... command) throws ProcessException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingFolder != null) {
            pb.directory(workingFolder);
        }
        if (env != null) {
            pb.environment().clear();
            pb.environment().putAll(env);
        }
        pb.redirectErrorStream(true);
        int code;
        try {
            Process process = pb.start();
            String payload;
            try {
                code = process.waitFor();
            } catch (InterruptedException ex) {
                process.destroy();
                throw ex;
            }
            payload = Miscellaneous.toString(process.getInputStream(), "UTF-8");
            if (code == 0) {
                return payload;
            } else {
                StringBuilder sb = new StringBuilder("Process returned code: " + code + ".");
                if (payload != null) {
                    sb.append("\n").append(payload);
                }
                throw new ProcessException(code, sb.toString());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static void createPOSIXNamedPipes(File... files) throws ProcessException, IOException {
        try {
            String[] mkfifo = new String[files.length + 1];
            String[] chmod = new String[files.length + 2];
            mkfifo[0] = "mkfifo";
            chmod[0] = "chmod";
            chmod[1] = "777";
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.getParentFile().exists()) {
                    Miscellaneous.createDirectory(f.getParentFile());
                }
                mkfifo[i + 1] = f.getAbsolutePath();
                chmod[i + 2] = f.getAbsolutePath();
            }
            executeProcess(mkfifo);
            executeProcess(chmod);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}

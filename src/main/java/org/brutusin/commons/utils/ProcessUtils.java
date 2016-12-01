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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class ProcessUtils {

    private ProcessUtils() {
    }

    /**
     * Executes a native process with small stdout and stderr payloads
     *
     * @param process
     * @return an array of two elements containing stdout and stderr messages
     * respectively
     * @throws RuntimeException if the process return code is not 0
     * @throws IOException
     * @throws InterruptedException
     */
    public static String[] execute(Process process) throws IOException, InterruptedException {
        String stdout;
        String stderr;
        int code;
        try {
            code = process.waitFor();
            stdout = Miscellaneous.toString(process.getInputStream(), "UTF-8");
            stderr = Miscellaneous.toString(process.getErrorStream(), "UTF-8");
        } catch (InterruptedException ex) {
            process.destroy();
            throw ex;
        }
        if (code == 0) {
            return new String[]{stdout, stderr};
        } else {
            StringBuilder sb = new StringBuilder("Process returned code: " + code + ".");
            if (stderr != null) {
                sb.append("\n").append(stderr);
            }
            throw new ProcessException(sb.toString());
        }
    }

    public static void createPOSIXNamedPipes(File... files) throws IOException {
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
            Process p = Runtime.getRuntime().exec(mkfifo);
            ProcessUtils.execute(p);
            p = Runtime.getRuntime().exec(chmod);
            ProcessUtils.execute(p);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}

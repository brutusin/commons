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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;

public final class Miscellaneous {

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
        Throwable root = ExceptionUtils.getRootCause(th);
        root = root == null ? th : root;
        return root.getMessage();
    }
    
    public static <T> List<T> createList(T... elements){
        ArrayList<T> ret = new ArrayList(elements.length);
        for (int i = 0; i < elements.length; i++) {
            ret.add(elements[i]);
        }
        return ret;
    }

    public static void main(String[] args) {
        Exception ex = new Exception("hi");
        System.out.println(ExceptionUtils.getRootCauseMessage(ex));
        System.out.println(getRootCauseMessage(ex));
    }
}

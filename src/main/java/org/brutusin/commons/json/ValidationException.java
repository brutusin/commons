/*
 * Copyright (c) 2011, DREAMgenics and/or its affiliates. All rights reserved.
 */

package org.brutusin.commons.json;

import com.github.fge.jsonschema.core.report.ProcessingReport;

/**
 *
 * @author Ignacio del Valle Alles idelvall@dreamgenics.com
 */
public class ValidationException extends Exception {

    private final ProcessingReport report;
    
    public ValidationException(ProcessingReport report) {
        if(report.isSuccess()){
            throw new IllegalArgumentException();
        }
        this.report = report;
    }

    public ProcessingReport getReport() {
        return report;
    }
}

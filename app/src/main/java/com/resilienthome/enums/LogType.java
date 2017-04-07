package com.resilienthome.enums;

public enum LogType {

    /**
     * Indicates that the Log Type is Raw.
     */
    RAW,

    /**
     * Indicates that the Log Type is Inferred; i.e., interpreted by applying logic on past raw logs.
     */
    INFERRED;

    public static LogType from(final int type) {
        switch (type) {
            case 0:
                return RAW;

            case 1:
                return INFERRED;
        }

        return null;
    }
}

package fr.socolin.applicationinsights.metricdata;

import java.util.ArrayList;
import java.util.HashMap;

public class ExceptionData implements ITelemetryData {

    public class ExceptionDetailData {
        public class Stack {
            public int level;
            public String method;
            public String assembly;
            public String fileName;
            public String line;
        }

        public String message;
        public long id;
        public String typeName;
        public boolean hasFullStack;
        public ArrayList<Stack> parsedStack;
    }

    public String severityLevel;
    public ArrayList<ExceptionDetailData> exceptions;
    public HashMap<String, String> properties;
}

package org.coodex.file.helper;

import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

public class ParameterParser {
    private static Logger log = LoggerFactory.getLogger(ParameterParser.class);

    public static void parseParameterMap(Map<String, String[]> parameterMap, Object param) {
        Field[] fields = ReflectHelper.getAllDeclaredFields(param.getClass());
        for (Field field : fields) {
            FileParameter fileParameter = field.getAnnotation(FileParameter.class);
            if (fileParameter != null) {
                String paramName = fileParameter.value();
                if (Common.isBlank(paramName)) {
                    paramName = field.getName();
                }
                String[] paramValue = parameterMap.get(paramName);
                if (paramValue != null) {
                    log.debug("found parameter: {}", paramName);
                    if (String.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            field.set(param, paramValue[0]);
                        } catch (IllegalAccessException e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                    } else if (String[].class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            field.set(param, paramValue);
                        } catch (IllegalAccessException e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }
        }
    }

}

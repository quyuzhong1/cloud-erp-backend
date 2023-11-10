package com.erp.server.dmp.convert.tool;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Named("TypeConversionWorker")
public class TypeConversionWorker {


    @Named("stringToInt")
    public Integer stringToInt(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

}

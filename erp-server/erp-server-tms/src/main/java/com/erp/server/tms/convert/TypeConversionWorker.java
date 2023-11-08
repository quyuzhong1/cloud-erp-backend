package com.erp.server.tms.convert;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zdy
 * @ClassName TypeConversionWorker
 * @description: TODO
 * @date 2023年11月08日
 * @version: 1.0
 */
@Component
@Named("TypeConversionWorker")
public class TypeConversionWorker {
    /**
     * 递四方费用模式转换
     *
     * @param obj
     * @return
     */
    @Named("taxModelToDSF")
    public String toJsonString(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (Objects.equals("DDU", obj)){
            return "U";
        }else if (Objects.equals("DDP", obj)){
            return "P";
        }else {
            return null;
        }
    }

    /**
     * 递四方费用模式转换
     *
     * @param obj
     * @return
     */
    @Named("yOrNToBoolean")
    public Boolean yOrNToBoolean(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (Objects.equals("Y", obj)){
            return true;
        }else if (Objects.equals("N", obj)){
            return false;
        }else {
            return false;
        }
    }
}

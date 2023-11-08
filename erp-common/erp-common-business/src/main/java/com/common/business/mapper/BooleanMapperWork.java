package com.common.business.mapper;

import org.mapstruct.Named;

/**
 * @author liuruipeng
 * @date 2023年11月07日 15:43
 */
@Named("BooleanMapperWork")
public class BooleanMapperWork {

    @Named("strToBooleanByYN")
    public Boolean toBooleanByYN(String bool) {
        return null == bool ?
                null : "Y".equals(bool);

    }

    @Named("boolToString")
    public String toString(Boolean bool) {
        return null == bool ?
                null : (bool ?
                "Y" : "N"
        );
    }

    @Named("boolToInteger")
    public Integer toInteger(Boolean bool) {
        return null == bool ?
                null : (bool ?
                1 : 0
        );
    }

}

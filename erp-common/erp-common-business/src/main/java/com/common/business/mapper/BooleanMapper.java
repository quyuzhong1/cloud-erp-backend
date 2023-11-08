package com.common.business.mapper;

/**
 * @author liuruipeng
 * @date 2023年11月07日 15:43
 */
public class BooleanMapper {

    public String toString(Boolean bool) {
        return null == bool ?
                null : (bool ?
                "Y" : "N"
        );
    }

    public Integer toInteger(Boolean bool) {
        return null == bool ?
                null : (bool ?
                1 : 0
        );
    }
}

package com.erp.model.plm.enums;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum PrintEanTypeEnum {

    /**
     * 50 * 30
     */
    FIFTY_THIRTY,
    /**
     * 40*30
     */
    FORTY_THIRTY,
    /**
     * 60*40
     */
    SIXTY_FORTY,
    /**
     * 80*30
     */
    EIGHTY_THIRTY,
    /**
     * 100*30
     */
    HUNDRED_THIRTY
    ;


    public static PrintEanTypeEnum of (String code) {
        return Arrays.stream(PrintEanTypeEnum.values()).filter(v -> v.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_9028));
    }
}

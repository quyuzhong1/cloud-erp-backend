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
    FIFTY_THIRTY("FIFTY_THIRTY",591,354),
    /**
     * 40*30
     */
    FORTY_THIRTY("FORTY_THIRTY",472,354),
    /**
     * 60*40
     */
    SIXTY_FORTY("SIXTY_FORTY",708,40),
    /**
     * 80*30
     */
    EIGHTY_THIRTY("EIGHTY_THIRTY",944,354),
    /**
     * 100*30
     */
    HUNDRED_THIRTY("HUNDRED_THIRTY",1181,354)
    ;

    private final String code;

    private final Integer width;

    private final Integer height;
    public static PrintEanTypeEnum of (String code) {
        return Arrays.stream(PrintEanTypeEnum.values()).filter(v -> v.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_9028));
    }
}

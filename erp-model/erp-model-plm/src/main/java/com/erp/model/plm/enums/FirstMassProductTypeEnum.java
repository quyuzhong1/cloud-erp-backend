package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.ObjectUtils;

@Getter
@AllArgsConstructor
public enum FirstMassProductTypeEnum implements EnumMessage {
    //试产首批
    FIRST_PILOT_BATCH("firstPilotBatch", "试产首批"),
    //量产首批
    FIRST_MASS_PRODUCTION_BATCH("firstMassProductionBatch", "量产首批"),
    //试量产首批
    FIRST_TRIAL_PRODUCTION_BATCH("firstTrialProductionBatch", "试量产首批"),
    //非首批
    SUBSEQUENT_BATCH("subsequentBatch", "非首批");
    private final String code;
    private final String name;
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (ObjectUtils.isEmpty(code)) {
            return "";
        }
        for (FirstMassProductTypeEnum item : FirstMassProductTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        if (ObjectUtils.isEmpty(name)) {
            return "";
        }
        for (FirstMassProductTypeEnum item : FirstMassProductTypeEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }
}

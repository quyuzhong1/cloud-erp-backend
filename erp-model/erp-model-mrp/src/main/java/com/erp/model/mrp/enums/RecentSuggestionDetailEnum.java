package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecentSuggestionDetailEnum implements EnumMessage {
    RECENT_OUT_OF_STOCK,RECENT_SUGGESTION_SHIPPING,RECENT_SUGGESTION_PURCHASE
    ;

    @Override
    public Object getCode() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}

package com.erp.model.scm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 开模通知单附件类型
 */
@Getter
@AllArgsConstructor
public enum AssetNoticeAttachmentTypeEnum {

    /** 原「附件」字段，现展示为 3D附件+CFM附件（同一组附件，可多文件） */
    THREE_D_CFM("asset_notice", "3D附件+CFM附件"),
    DFM("asset_notice_dfm", "DFM附件");

    private final String code;
    private final String name;

    public static AssetNoticeAttachmentTypeEnum getByCode(String code) {
        for (AssetNoticeAttachmentTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}

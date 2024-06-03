
package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.constant.MongoTableNameContant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>
 * 处理mongo业务数据任务处理类型
 * </p>
 *
 * @author Jim
 * @since 2024-05-07
 */
@Getter
@AllArgsConstructor
public enum DmpMongoTaskHandleTypeEnum {

    // 亚马逊报告相关
    AMZ_REPORT_LISTING("amzListing", "亚马逊LISTING下载处理", MongoTableNameContant.DATA_REPORT_AMZ_LISTING, ""),
    AMZ_REPORT_FBA_INVENTORY_PLANNING("AmzReportFbaInventoryPlanning", "亚马逊库龄报告更新处理",MongoTableNameContant.DATA_REPORT_AMZ_FBA_INVENTORY_PLANNING, ""),
    AMZ_REPORT_FBA_MYI_ALL_INVENTORY("AmzReportFbaMyiAllInventory", "亚马逊库存管理报告更新处理",MongoTableNameContant.DATA_REPORT_AMZ_FBA_MYI_ALL_INVENTORY, ""),
    AMZ_FBA_SO_STOCK("AmzFbaSoStock", "亚马逊FBA生成销售出库单",MongoTableNameContant.DATA_REPORT_AMZ_RESERVED, ""),
    AMZ_REPORT_RESERVED_INVENTORY("AmzReportReservedInventory", "亚马逊预留库存报告处理",MongoTableNameContant.DATA_REPORT_AMZ_FULFILLED_SHIPMENTS, ""),
    //    AMZ_REPORT_LEDGER_DETAIL_VIEW("AmzReportLedgerDetailView", "亚马逊库存分账报告"),

    ;

    /**
     * 代号
     */
    @EnumValue
    private final String code;

    /**
     * 名称
     */
    private final String name;

    /**
     * mongo表名
     */
    private final String mongoTableName;

    /**
     * 发送的mq tag
     */
    private final String mqTag;


    public static DmpMongoTaskHandleTypeEnum getByCode(String code){
        return Arrays.stream(DmpMongoTaskHandleTypeEnum.values())
                .filter(e-> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}

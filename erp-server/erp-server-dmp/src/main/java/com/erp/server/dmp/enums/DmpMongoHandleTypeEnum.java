package com.erp.server.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.constant.MongoTableNameContant;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>
 * 处理mongo业务数据任务类型
 * </p>
 *
 * @author Jim
 * @since 2024-05-28
 */
@Getter
@AllArgsConstructor
public enum DmpMongoHandleTypeEnum {

    AMZ_REPORT_FULFILLED_SHIPMENTS("amzReportFulfilledShipments", "亚马逊物流销售报告处理", "amzFulfilledShipmentsMongoHandler", MongoTableNameContant.DATA_REPORT_AMZ_FULFILLED_SHIPMENTS),
    AMZ_REPORT_ALL_LISTING("amzReportAllListing", "亚马逊Listing报告处理", "amzReportAllListingHandler", MongoTableNameContant.DATA_REPORT_AMZ_LISTING),
    AMZ_REPORT_FBA_MYI_UNSUPPRESSED_INVENTORY("amzReportFbaMyiUnsuppressedInventory", "亚马逊库存管理报告", "amzReportFbaMyiUnsuppressedInventoryHandler", MongoTableNameContant.DATA_REPORT_AMZ_FBA_MYI_UNSUPPRESSED_INVENTORY),

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
     * DmpMongoHandler实现的Handler名称
     */
    private final String handlerName;


    /**
     * DmpMongoHandler处理的mongoTable名称
     */
    private final String mongoTableName;


    public static DmpMongoHandleTypeEnum getByCode(String code, boolean nullThrow) {
        DmpMongoHandleTypeEnum dmpMongoHandleTypeEnum = Arrays.stream(values())
                .filter(value -> value.getCode().equals(code))
                .findFirst().orElse(null);
        if (null == dmpMongoHandleTypeEnum && nullThrow) {
            throw new ServiceException("未找到处理mongo业务数据任务类型");
        }
        return dmpMongoHandleTypeEnum;
    }
}

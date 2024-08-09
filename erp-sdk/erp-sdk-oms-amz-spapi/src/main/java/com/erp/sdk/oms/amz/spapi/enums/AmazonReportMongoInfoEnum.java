
package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.constant.MongoTableNameContant;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.csv.*;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 亚马逊SP-API中使用的报告类型对应mongo信息
 *
 * @author Jim
 * @date 2024/05/23
 */
@Getter
@AllArgsConstructor
public enum AmazonReportMongoInfoEnum {

    MERCHANT_LISTINGS_ALL_DATA_MONGO_INFO("所有商品信息报告", MongoTableNameContant.DATA_REPORT_AMZ_LISTING, ReportListingCsvEntity.class, ReportListingMongoDTO.class, "amzReportAllListingHandler"),
    FBA_MYI_ALL_INVENTORY_DATA_MONGO_INFO("亚马逊物流管理库存", MongoTableNameContant.DATA_REPORT_AMZ_FBA_MYI_ALL_INVENTORY, ReportFbaMyiAllInventoryCsvEntity.class, ReportFbaMyiAllInventoryMongoDTO.class, "amzReportFbaMyiAllInventoryHandler"),
    FBA_MYI_ALL_UNSUPPRESSED_INVENTORY_DATA_MONGO_INFO("亚马逊物流管理库存(未归档)", MongoTableNameContant.DATA_REPORT_AMZ_FBA_MYI_UNSUPPRESSED_INVENTORY, ReportFbaMyiUnsuppressedInventoryCsvEntity.class, ReportFbaMyiUnsuppressedInventoryMongoDTO.class, "amzReportFbaMyiUnsuppressedInventoryHandler"),
    RESERVED_INVENTORY_DATA_MONGO_INFO("亚马逊物流预留库存报告", MongoTableNameContant.DATA_REPORT_AMZ_RESERVED, ReportReservedCsvEntity.class, ReportReservedMongoDTO.class, "amzReportReservedInventoryHandler"),
    FBA_INVENTORY_PLANNING_DATA_MONGO_INFO("亚马逊物流管理库存状况报告", MongoTableNameContant.DATA_REPORT_AMZ_FBA_INVENTORY_PLANNING, ReportFbaInventoryPlanningCsvEntity.class, ReportFbaInventoryPlanningMongoDTO.class, "amzReportFbaInventoryPlanningHandler"),
    LEDGER_DETAIL_VIEW_DATA_MONGO_INFO("亚马逊物流库存账本详情报告", null, null, null, "amzReportLedgerDetailViewHandler"),
    AMAZON_FULFILLED_SHIPMENTS_DATA_GENERAL_MONGO_INFO("亚马逊物流销售报告", MongoTableNameContant.DATA_REPORT_AMZ_FULFILLED_SHIPMENTS, ReportFulfilledShipmentsCsvEntity.class, ReportFulfilledShipmentsMongoDTO.class, "amzReportFulfilledShipmentsHandler");

    /**
     * 描述
     */
    private final String desc;

    /**
     * mongo表名
     */
    private final String mongoTableName;

    /**
     * CSV实体
     */
    private final Class<?> cvsClass;

    /**
     * mongo表实体
     */
    private final Class<? extends ReportSuperMongoDTO> mongoDTOClass;

    /**
     * Listing报告处理服务实现类名称
     */
    private final String amzReportBusinessHandlerName;


}

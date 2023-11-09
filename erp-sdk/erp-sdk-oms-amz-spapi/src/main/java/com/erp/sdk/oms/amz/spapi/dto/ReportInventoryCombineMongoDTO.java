package com.erp.sdk.oms.amz.spapi.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class ReportInventoryCombineMongoDTO {

    /**
     * 店铺
     */
    @Panno(findType = PannoEnum.EQ, field = "shopId")
    private String shopId;

    /**
     * 市场IDS
     */
    @Panno(findType = PannoEnum.IN, field = "marketplaceIds")
    private List<String> marketplaceIds;

    /**
     * 数据开始时间
     */
    @Panno(findType = PannoEnum.EQ, field = "dataStartTime")
    private String dataStartTime;

    /**
     * 数据结束时间
     */
    @Panno(findType = PannoEnum.EQ, field = "dataEndTime")
    private String dataEndTime;

    /**
     * 亚马逊物流管理库存报告ID
     */
    @Panno(findType = PannoEnum.EQ, field = "myiAllInventoryReportId")
    private String myiAllInventoryReportId;

    /**
     * 亚马逊物流管理库存状况报告ID
     */
    @Panno(findType = PannoEnum.EQ, field = "reportId")
    private String inventoryPlanningReportId;

    /**
     * 亚马逊物流预留库存报告ID
     */
    @Panno(findType = PannoEnum.EQ, field = "reportDocumentId")
    private String reservedReportId;

    /**
     * 报告组合状态: 0=未组合，1=可组合, 2=已组合, 3=丢弃(数据时间低于主数据丢弃)
     */
    @Panno(findType = PannoEnum.EQ, field = "combineStatus")
    private Integer combineStatus;


    public ReportInventoryCombineMongoDTO(String dataStartTime, String dataEndTime, List<String> marketplaceIds) {
        this.dataStartTime = dataStartTime;
        this.dataEndTime = dataEndTime;
        this.marketplaceIds = marketplaceIds;
    }

    public static ReportInventoryCombineMongoDTO unCombineStatus() {
        ReportInventoryCombineMongoDTO combineInventoryDTO = new ReportInventoryCombineMongoDTO();
        combineInventoryDTO.setCombineStatus(0);
        return combineInventoryDTO;
    }
}

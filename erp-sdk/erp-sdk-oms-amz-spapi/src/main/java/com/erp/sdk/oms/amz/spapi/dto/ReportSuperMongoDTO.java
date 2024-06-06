package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.MongoSuperDTO;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 亚马逊报表mongo超类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReportSuperMongoDTO extends MongoSuperDTO {

    @Panno(findType = PannoEnum.GT,field = "_id")
    private String id;

    @Panno(findType = PannoEnum.IN,field = "reportMarketplaceIds")
    private List<String> reportMarketplaceIds;

    @Panno(findType = PannoEnum.EQ, field = "reportId")
    private String reportId;

    @Panno(findType = PannoEnum.EQ, field = "reportDataStartTime")
    private String reportDataStartTime;

    @Panno(findType = PannoEnum.EQ, field = "reportDataEndTime")
    private String reportDataEndTime;

    @Panno(findType = PannoEnum.EQ, field = "reportScheduleId")
    private String reportScheduleId;

    /**
     * 报告行号
     */
    @Panno(findType = PannoEnum.EQ, field = "reportRowNum")
    private Integer reportRowNum;

    /**
     * 亚马逊账号代号
     */
    @Panno(findType = PannoEnum.EQ, field = "platformShopCode")
    private String platformShopCode;

    @Panno(findType = PannoEnum.EQ, field = "requestShopId")
    private String requestShopId;

    public static ReportSuperMongoDTO initLastId(String lastId) {
        ReportSuperMongoDTO mongoDTO = new ReportSuperMongoDTO();
        mongoDTO.setId(lastId);
        return mongoDTO;
    }
}

package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.model.wms.entity.FbaTransitCalculateDetailReportEntity;
import com.common.business.service.SuperService;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
public interface FbaTransitCalculateDetailReportService extends SuperService<FbaTransitCalculateDetailReportEntity> {

    /**
     * 查询在途货件数据列表
     * @param reportMonth
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    List<FbaTransitCalculateDetailReportEntity> listTransitDetail(LocalDate reportMonth, String shipmentCode, String asin, String msku);

    /**
     * 更新明细调整数量
     *
     * @param adjustDTO
     * @param detailReportEntity
     */
    void updateAdjustQty(FbaTransitCalculateReportDTO.AdjustDTO adjustDTO, FbaTransitCalculateDetailReportEntity detailReportEntity);
}

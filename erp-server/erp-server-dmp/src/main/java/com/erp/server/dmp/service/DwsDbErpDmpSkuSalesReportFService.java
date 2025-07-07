package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;

import java.util.List;

/**
 * <p>
 * SKU销量报告 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-06-23
 */
public interface DwsDbErpDmpSkuSalesReportFService extends SuperService<DwsDbErpDmpSkuSalesReportFEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-06-23
    * @param dto 请求参数
    * @return 统计信息
    */
    List<DwsDbErpDmpSkuSalesReportFEntity> reportList(DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO dto);
}

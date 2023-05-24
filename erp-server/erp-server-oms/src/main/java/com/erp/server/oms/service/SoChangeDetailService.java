package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售订单变更明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoChangeDetailService extends SuperService<SoChangeDetailEntity> {

    
    /**
     * 添加变更详情信息
     * @author yl
     * @date 2023-05-24 14:12
     * @param mainId
     * @param detailList
     * @return void
     */
    void addDetailList(String mainId, List<SoChangeDetailDTO.AddDTO> detailList);
}

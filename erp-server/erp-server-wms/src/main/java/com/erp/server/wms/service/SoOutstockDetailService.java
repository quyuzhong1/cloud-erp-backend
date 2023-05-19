package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售订单出库明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoOutstockDetailService extends SuperService<SoOutstockDetailEntity> {
    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 14:56
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    List<SoOutstockDetailEntity> listDetailBySourceDetailId(List<String> sourceDetailId);

    
    /**
     * 保存销售出库单明细
     * @author yl
     * @date 2023-05-19 10:18
     * @param mainId
     * @param detailList
     * @return void
     */
    void add(String mainId, List<SoOutstockDetiailDTO.AddDTO> detailList);
}

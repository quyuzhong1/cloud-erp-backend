package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;

import java.util.List;

/**
 * <p>
 * 销量去噪信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesDenoisingService extends SuperService<CfgRuleSalesDenoisingEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param salesDenoisingList
    * @return
    */
    Boolean update(List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList,String salesQtyId);

    /**
     * 根据销量id集合查询
     * @author will
     * @date 2024/8/24 10:16
     * @param salesQtyIdList
     * @return List<CfgRuleSalesDenoisingEntity>
     */
    List<CfgRuleSalesDenoisingEntity> listBySalesQtyIdList(List<String> salesQtyIdList);
}

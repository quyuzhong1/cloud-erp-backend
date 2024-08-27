package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;

import java.util.List;

/**
 * <p>
 * 备货物流明细（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleLogisticsDetailService extends SuperService<CfgRuleLogisticsDetailEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param detailList
    * @return
    */
    Boolean update(List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList,String mainId);

    /**
     * 根据主表id
     * @author will
     * @date 2024/8/27 11:01
     * @param mainIdList
     */
    void deleteByMainIdList(List<String> mainIdList);
}

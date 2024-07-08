package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgRuleOutDTO;

/**
 * <p>
 * 出库配置规则 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
 */
public interface CfgRuleOutService extends SuperService<CfgRuleOutEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-06-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(CfgRuleOutDTO.CommonDTO dto);

    CfgRuleOutDTO.CommonDTO view();

    /**
     * 根据出库配置返回分拣口
     */
    String getSortingPort(CfgRuleOutDTO.SortingPortRuleDTO dto);

    /**
     * 校验装箱超重
     * @return true可以出库，false 不可以出库
     */
    CfgRuleOutDTO.CheckDTO handleOverweight(CfgRuleOutDTO.OverweightDTO dto);
}

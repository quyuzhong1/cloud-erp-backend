package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;

import java.util.List;

/**
 * <p>
 * 公共配置（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleCommonService extends SuperService<CfgRuleCommonEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param commonList
    * @return
    */
    Boolean add(List<CfgRuleCommonDTO.AddDTO> commonList);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param commonList
    * @return
    */
    Boolean update(List<CfgRuleCommonDTO.UpdateDTO> commonList);

    /**
     * 查询详情
     * @author will
     * @date 2024/8/26 9:19
     * @param platformType
     * @return List<CfgRuleCommonDTO.ViewDTO>
     */
    List<CfgRuleCommonDTO.ViewDTO> view(String platformType);
}

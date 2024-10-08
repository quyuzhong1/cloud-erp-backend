package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param updateList
    * @return
    */
    Boolean update(List<CfgRuleCommonDTO.UpdateDTO> updateList);

    /**
     * 查询详情
     * @author will
     * @date 2024/8/26 9:19
     * @param platformType
     * @return List<CfgRuleCommonDTO.ViewDTO>
     */
    List<CfgRuleCommonDTO.ViewDTO> view(String platformType,String type);

    /**
     * 根据平台和类型获取建议或库存
     *
     * @param platformType 平台类型
     * @param type         类型
     */
    List<CfgRuleCommonDTO.StrategyResultDTO> getCfgRuleCommon(String platformType, String type);

    /**
     * 获取描述
     * @param platformType 平台类型
     */
    Map<String, List<CfgRuleCommonDTO.DescriptionDTO>> description(String platformType);

    /**
     * 根据传入的key查询对应数据
     * @param baseKey 基础前缀
     * @param list 获取的树结构
     * @param findKey key全长
     */
    Set<String> findByKey(String baseKey, List<CfgRuleCommonDTO.StrategyResultDTO> list, String findKey);
}

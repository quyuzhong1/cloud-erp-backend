package com.erp.server.mrp.service;

import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 时效配置表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
public interface CfgRuleExpireTimeService extends SuperService<CfgRuleExpireTimeEntity> {

    /**
     * 修改
     */
    void update(CfgRuleExpireTimeDTO.UpdateDTO dto);
    /**
     * 查看详情
     */
    CfgRuleExpireTimeDTO.ViewDTO view();

    /**
     * 根据关联id查询配置
     * @param refId 关联id
     */
    CfgRuleExpireTimeEntity getByRefId(String refId);

    /**
     * 删除关联数据
     * @param id 删除关联数据
     */
    void deleteByRefId(String id);

    /**
     * 批量查询根据关联id
     * @param refIdList 关联id
     */
    List<CfgRuleExpireTimeEntity> listByRefIdList(List<String> refIdList);

}

package com.erp.server.sys.service;
import com.erp.model.sys.dto.CfgConditionDTO;
import com.erp.model.sys.entity.CfgConditionEntity;
import com.common.business.service.SuperService;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {
    /**
     * 新增规则条件
     * @param dto 规则条件
     */
    void add(CfgConditionDTO.AddDTO dto);
    /**
     * 编辑规则条件
     * @param dto 规则条件
     */
    void update(CfgConditionDTO.UpdateDTO dto);
    /**
     * 根据类型获取所有条件
     * @param type 类型
     */
    List<CfgConditionDTO.CommonDTO> listByType(String type);
    /**
     * 根据类型获取树结构
     * @param type 类型
     */
    List<CfgConditionDTO.TreeDTO> tree(String type);

    List<CfgConditionEntity> listByFields(List<String> fieldList);
}

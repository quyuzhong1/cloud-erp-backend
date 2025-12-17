package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CfgConditionDTO;
import com.erp.model.oms.entity.CfgConditionEntity;

import java.util.List;

/**
 * <p>
 * 条件配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    String add(CfgConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-30
    * @param dto
    * @return
    */
    Boolean update(CfgConditionDTO.UpdateDTO dto);

    /**
     * 根据添加code 获取到逻辑关系
     * @param conditionCode
     * @return
     */
    List<CfgConditionDTO.CommonDTO> listByConditionCode(String conditionCode);

    /**
     * 获取到所有的条件值
     * @author yl
     * @date 2023-10-08 14:45
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.ListDTO>
     */
    List<CfgConditionDTO.ListDTO> listAllCondition();

    /**
     * 申报规则下拉列表
     * @return
     */
    List<CfgConditionDTO.ListDTO> listDeclareCondition();

    /**
     * 条件树结构
     * @author yl
     * @date 2023-10-08 15:09
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CfConditionDTO.TreeDTO>
     */
    List<CfgConditionDTO.TreeDTO> tree();

    /**
     * 根据字段获取信息
     *@parms
     *@return
     *@author yl
     *@date 2023-12-06
     */
    List<CfgConditionEntity> listByFields(List<String> fieldList);
    /**
     * @description: 订单处理的条件下拉
     * @author Will
     * @date: 2024/5/9 14:34
     * @return List<ListDTO>
     */
    List<CfgConditionDTO.ListDTO> listOrderHandleCondition();

    List<CfgConditionDTO.ListDTO> listInvoiceHandleCondition();

    List<CfgConditionDTO.ListDTO> listHandleConditionByType(List<String> typeList);
}

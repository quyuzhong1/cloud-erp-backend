package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;

import java.util.List;

/**
 * <p>
 * 备货（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleStockUpService extends SuperService<CfgRuleStockUpEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleStockUpDTO.UpdateDTO dto);
    /**
     * 查看详情
     * @author will
     * @date 2024/8/23 17:07
     * @param platformType
     * @return ViewDTO
     */
    CfgRuleStockUpDTO.ViewDTO view(String platformType,String refId);
    /**
     * 根据来源id删除
     * @author will
     * @date 2024/8/29 16:13
     * @param refId
     */
    void deleteByRefId(String refId);
    /**
     * 自定义更新
     * @author will
     * @date 2024/8/30 9:26
     * @param stockUpUpdateDTO
     */
    void customUpdate(CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO);
    /**
     * 根据来源id查询
     * @author will
     * @date 2024/8/30 9:54
     * @param refId
     * @return CfgRuleStockUpEntity
     */
    CfgRuleStockUpEntity getByRefId(String refId);
    /**
     * 根据来源id集合查询
     * @author will
     * @date 2024/9/4 15:40
     * @param refIdList
     * @return List<CfgRuleStockUpEntity>
     */
    List<CfgRuleStockUpEntity> listByRefIdList(List<String> refIdList);

    /**
     * 获取默认配置
     * @param platformType 平台类型
     */
    CfgRuleStockUpEntity getDefaultCfgRuleStockUp(String platformType);
    /**
     * 获取默认配置
     */
    List<CfgRuleStockUpEntity> getDefaultCfgRuleStockUp();

    /**
     * 根据平台类型获取配置
     * @param platformType 平台类型
     */
    CfgRuleStockUpEntity getDefaultByPlatform(String platformType);
}

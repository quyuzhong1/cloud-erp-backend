package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;

import java.util.List;

/**
 * <p>
 * 销量（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesQtyService extends SuperService<CfgRuleSalesQtyEntity> {


    /**
    * 常规品和新品修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean batchUpdate(CfgRuleSalesQtyDTO.UpdateDTO dto);

    /**
     * 修改
     * @author will
     * @date 2024/8/27 9:56
     * @param updateDTO
     * @return Boolean
     */
    Boolean update(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO);

    /**
     * 查看详情
     * @author will
     * @date 2024/8/24 9:21
     * @param platformType
     * @return ViewDTO
     */
    CfgRuleSalesQtyDTO.ViewDTO view(String platformType);
    /**
     * 根据来源id删除
     * @author will
     * @date 2024/8/29 16:16
     * @param refId
     */
    void deleteByRefId(String refId);
    /**
     * 自定义更新
     * @author will
     * @date 2024/8/30 9:33
     * @param salesQtyUpdateDTO
     */
    void customUpdate(CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO);

    /**
     * 获取建议得配置
     * @param refId 建议id
     * @return
     */
    CfgRuleSalesQtyEntity getByRefId(String refId);

    /**
     * 获取默认销量配置
     * @param platformType 平台类型
     * @param type sku类型
     */
    CfgRuleSalesQtyDTO.StrategyResultDTO getDefaultCfgRuleSalesQty(String platformType, String type);
    /**
     *
     * @author will
     * @date 2024/9/4 16:34
     * @param refIdList
     * @return List<CfgRuleSalesQtyEntity>
     */
    List<CfgRuleSalesQtyEntity> listByRefIdList(List<String> refIdList);
}

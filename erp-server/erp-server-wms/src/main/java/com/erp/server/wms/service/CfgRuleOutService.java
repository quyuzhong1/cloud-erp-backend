package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;

import java.util.List;

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
    CfgRuleOutDTO.SortingPortResultDTO getSortingPort(CfgRuleOutDTO.SortingPortRuleDTO dto, SoB2cDeliveryEntity entity);
    /**
     * 处理偏差
     * @author will
     * @date 2024/7/3 14:22
     * @param b2cAllowableDeviations
     * @param dto
     * @return Boolean
     */
    Boolean handleB2cAllowableDeviations(CfgRuleOutDTO.B2cAllowableDeviations b2cAllowableDeviations, CfgRuleOutDTO.SortingPortRuleDTO dto);

    /**
     * 校验装箱超重
     * @return true可以出库，false 不可以出库
     */
    CfgRuleOutDTO.CheckDTO handleOverweight(CfgRuleOutDTO.OverweightDTO dto);

    /**
     * 获取装箱超重配置
     * @param type
     * @return
     */
    CfgRuleOutDTO.CfgOverweightDetailDTO getCfgOverweightDetailDTOByType(String type);

    /**
     * 获取产品装箱配置
     *
     * @param type
     * @return
     */
    List<CfgRuleOutDTO.CfgProductPackingDetail> getCfgProductPackingDetailByType(String type);

    Boolean matchTransferRule(CfgRuleOutDTO.MatchTransferRuleDTO dto);

    /**
     * 匹配中转规则和仓库
     * @author will
     * @date 2024/7/23 14:07
     * @param dto
     * @return  CfgRuleOutDTO.MatchTransferResultDTO
     */
    CfgRuleOutDTO.MatchTransferResultDTO matchTransferAndWarehouse(CfgRuleOutDTO.MatchTransferDTO dto);

}

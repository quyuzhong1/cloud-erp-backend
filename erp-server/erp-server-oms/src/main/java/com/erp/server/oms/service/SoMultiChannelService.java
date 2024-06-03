package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 多渠道订单 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
 */
public interface SoMultiChannelService extends SuperService<SoMultiChannelEntity> {

    /**
     * 消费处理
     *
     * @param dto DTO
     * @author Jim
     * @date: 2024-05-30
     */
    SoMultiChannelEntity handleSave(PlatformOrderDTO dto);


    /**
     * 保存或更新
     *
     * @param dto DTO
     * @author Jim
     * @date: 2024-05-30
     */
    SoMultiChannelEntity saveOrUpdateEntity(PlatformOrderDTO dto, ShopInfoEntity shopInfo);

    /**
     * 获取多渠道订单
     *
     * @param platformCode 平台单号
     * @param dictPlatform 平台
     * @param shopId       店铺ID
     * @param sourceType   来源类型
     * @return entity
     * @author Jim
     * @date: 2024-05-30
     */
    SoMultiChannelEntity getByPlatformInfo(String platformCode, String dictPlatform, String shopId, String sourceType);
}

package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.common.business.service.SuperService;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.model.oms.entity.SkuMappingEntity;

import java.util.List;

/**
 * <p>
 * 店铺与第三方平台对照表 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-02-19
 */
public interface ShopInfoMappingService extends SuperService<ShopInfoMappingEntity> {


    /**
     * 通过thirdPlatformType查询列表
     */
    List<ShopInfoMappingEntity> listByType(String thirdPlatformType);

    /**
     * 检查任务和记录平台店铺ID映射关系
     */
    void saveAndHandle(ShopEntity ext);

    /**
     * 根据店铺ID和平台类型查询映射关系
     */
    ShopInfoMappingEntity getByShopIdAndType(String shopId, String thirdPlatformType);
}

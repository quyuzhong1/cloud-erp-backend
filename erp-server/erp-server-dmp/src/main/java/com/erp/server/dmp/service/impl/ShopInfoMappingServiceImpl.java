package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.TaskConstant;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.sellers.Marketplace;
import com.erp.server.dmp.mapper.ShopInfoMappingMapper;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.ShopInfoMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
/**
 * <p>
 * 店铺与第三方平台对照表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-02-19
 */
@Slf4j
@Service
public class ShopInfoMappingServiceImpl extends SuperServiceImpl<ShopInfoMappingMapper, ShopInfoMappingEntity> implements ShopInfoMappingService {

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private PlatformApiTaskService platformApiTaskService;


    @Override
    public List<ShopInfoMappingEntity> listByType(String thirdPlatformType) {
        return lambdaQuery()
                .eq(ShopInfoMappingEntity::getThirdPlatformType, thirdPlatformType)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void saveAndHandle(ShopEntity sourceEntity) {
        // 查询是否有对应店铺
        // 查询指定或所有已授权店铺
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listByParams(
                new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null)
        );
        if (CollectionUtil.isEmpty(shopInfoEntityList)){
            log.warn("处理领星店铺信息消费失败:当前数据库无授权数据");
            return;
        }
        ShopInfoEntity shopInfo = shopInfoEntityList.stream()
                .filter(e -> e.getPlatformShopCode().equalsIgnoreCase(sourceEntity.getSellerId())
                        && AmazonMarketplaceEnum.getByCountryCode(e.getDictCountryCode()).getMarketplaceId().equalsIgnoreCase(sourceEntity.getMarketplaceId()))
                .findFirst().orElse(null);
        if (null == shopInfo){
            log.error("数据处理异常：未找到对应店铺, msg={}", JSONUtil.toJsonStr(sourceEntity));
            return;
        }
        // 记录映射关系
        ShopInfoMappingEntity entity = new ShopInfoMappingEntity();

        entity.setThirdPlatformType(PlatformEnum.LINGXING.getName());
        entity.setThirdPlatformShopId(sourceEntity.getSid().toString());
        entity.setShopId(shopInfo.getId());
        if (!this.save(entity)){
            throw new ServiceException("[保存店铺映射关系失败]");
        }

        // 查询需要当前平台需要增加的任务
        platformApiTaskService.createOrEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), TaskConstant.LX_PULL_DATA_TASK));

    }

    @Override
    public ShopInfoMappingEntity getByShopIdAndType(String shopId, String thirdPlatformType) {
        return lambdaQuery()
                .eq(ShopInfoMappingEntity::getThirdPlatformType, thirdPlatformType)
                .eq(ShopInfoMappingEntity::getShopId, shopId)
                .last("LIMIT 1")
                .one();
    }
}

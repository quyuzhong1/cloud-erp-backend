package com.erp.server.oms.rocketmq;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.convert.OmsListingConverter;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 下载平台商品消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_product_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_products_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformListingConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ListingInfoService listingInfoService;
    @Resource
    private SkuMappingService skuMappingService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformProductDTO dto = JSONUtil.toBean(ext.toString(), PlatformProductDTO.class);
        // 组合信息
        // 添加到sku_mapping
        ListingInfoEntity oldEntity = listingInfoService.getByPlatformSkuNo(dto.getPlatform(), dto.getPlatformSkuNo());

        // 转换
        ListingInfoEntity entity = OmsListingConverter.INSTANCE.listingDtoToEntity(dto);

        if (null == oldEntity) {
            if (!listingInfoService.save(entity)) {
                throw new ServiceException("Listing 产品保存失败");
            }
            // 添加到映射
            SkuMappingEntity skuMappingEntity = new SkuMappingEntity(entity);
            if (!skuMappingService.save(skuMappingEntity)) {
                throw new ServiceException("SkuMapping保存失败");
            }
        } else {
            // 是否修改
            if (!oldEntity.toString().equals(entity.toString())) {
                oldEntity.setPlatformProductNo(entity.getPlatformProductNo());
                oldEntity.setProductImageUrl(entity.getProductImageUrl());
                oldEntity.setProductSpec(entity.getProductSpec());
                oldEntity.setProductPacking(entity.getProductPacking());
                oldEntity.setPlatformUpdateTime(entity.getPlatformUpdateTime());
                if (!listingInfoService.updateById(oldEntity)) {
                    throw new ServiceException("Listing 产品更新失败");
                }
            }

        }
        return ApiResult.success();
    }
}

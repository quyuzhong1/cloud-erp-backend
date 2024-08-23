package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.server.oms.convert.OmsListingConverter;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private ListingInfoService listingInfoService;
    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        try {
            dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
        }catch (Exception e){
            throw new ServiceException("erp-dmp更新dmp_pull_task异常："+ e.getMessage());
        }
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity, msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        log.info("[Listing] 消费: dto={}", JSONUtil.toJsonStr(ext));
        PlatformProductDTO dto = JSONUtil.toBean(ext.toString(), PlatformProductDTO.class);
            // ALiExpress,Shopify来源卖家sku可能为空
            if (StringUtils.isBlank(dto.getPlatformSkuNo())) {
                log.warn("[Listing] 消费:来源数据异常PlatformSkuNo为空, msg={}", JSONUtil.toJsonStr(dto));
//                return ApiResult.success()
                // 防止来源为null
                dto.setPlatformSkuNo("");
            }
            ListingInfoEntity oldEntity = null;
            if (OmsPlatformEnum.getByCode(dto.getPlatform()) != null) {
                oldEntity = listingInfoService.getByPlatformSkuNo(dto.getPlatform(), dto.getPlatformSkuNo());
            } else {
                ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
                paramDTO.setPlatform(dto.getPlatform());
                paramDTO.setShopIdList(Collections.singletonList(dto.getShopId()));
                paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
                paramDTO.setPlatformSkuNoList(Collections.singletonList(dto.getPlatformSkuNo()));
                // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
                if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getPlatform()) ||
                        PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getPlatform()) ||
                        PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getPlatform())){
                    paramDTO.setPlatformSpuNoList(Collections.singletonList(dto.getPlatformProductNo()));
                    paramDTO.setPlatformSkuIdList(StringUtils.isNotBlank(dto.getPlatformSkuId()) ? Collections.singletonList(dto.getPlatformSkuId()) : null);
                }
                paramDTO.setIsExpire(false);
                List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);

                if (!CollectionUtils.isEmpty(listDto)) {
                    oldEntity = listingInfoService.getById(listDto.get(0).getListingId());
                }
            }
            // 亚马逊保存FNSKU
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform()) && StringUtils.isNotBlank(dto.getPlatformSkuNo())){
                // 查询关联的FNSKU
                List<FbaInventoryEntity> fbaInventoryEntityList = wmsFbaInventoryFeign.findList(Collections.singletonList(dto.getPlatformSkuNo()));
                FbaInventoryEntity fbaInventoryEntity = fbaInventoryEntityList.stream().findFirst().orElse(null);
                dto.setPlatformFnSku(null == fbaInventoryEntity ? "" : fbaInventoryEntity.getFnSku());
            }

            // 转换
            ListingInfoEntity entity = OmsListingConverter.INSTANCE.listingDtoToEntity(dto);

            if (null == oldEntity) {
                if (!listingInfoService.save(entity)) {
                    throw new ServiceException("【listing消费】Listing 产品保存失败");
                }
                // 添加到映射
                SkuMappingEntity skuMappingEntity = new SkuMappingEntity(entity, dto.getShopId());
                if (OmsPlatformEnum.OMS_GOOD_CANG.getCode().equals(dto.getPlatform())
                        || OmsPlatformEnum.OMS_IML.getCode().equals(dto.getPlatform())) {
                    skuMappingEntity.setHasMappingAll(true);
                }
                if (!skuMappingService.save(skuMappingEntity)) {
                    throw new ServiceException("【listing消费】SkuMapping保存失败");
                }
                String msg = StrUtil.format("拉取第三方产品新增【{}】，平台sku为【{}】", "平台sku表",entity.getPlatformSkuNo());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), entity.getId(), "新增操作");
            } else {
                // 是否修改
                if (!oldEntity.toString().equals(entity.toString())) {
                    ListingInfoEntity oldLogInfo = OmsListingConverter.INSTANCE.copyListingInfo(oldEntity);
                    if (StringUtils.isNotBlank(entity.getPlatformSpuNo())) {
                        oldEntity.setPlatformSpuNo(entity.getPlatformSpuNo());
                    }
                    if (StringUtils.isNotBlank(entity.getProductImageUrl())) {
                        oldEntity.setProductImageUrl(entity.getProductImageUrl());
                    }
                    if (StringUtils.isNotBlank(entity.getProductSpec())) {
                        oldEntity.setProductSpec(entity.getProductSpec());
                    }
                    if (StringUtils.isNotBlank(entity.getProductPacking())) {
                        oldEntity.setProductPacking(entity.getProductPacking());
                    }
                    if (StringUtils.isNotBlank(entity.getPlatformFnSku())) {
                        oldEntity.setPlatformFnSku(entity.getPlatformFnSku());
                    }
                    if (StringUtils.isNotBlank(entity.getPlatformSkuName())) {
                        oldEntity.setPlatformSkuName(entity.getPlatformSkuName());
                    }
                    oldEntity.setPlatformUpdateTime(entity.getPlatformUpdateTime());
                    listingInfoService.updateById(oldEntity);
//                    if (!listingInfoService.updateById(oldEntity)) {
//                        throw new ServiceException("Listing 产品更新失败");
//                    }
                    //记录更新日志
                    String msg = StrUtil.format("拉取第三方产品更新【{}】 ", "平台sku表");
                    operateLogService.addModuleOperateLogByObj(oldLogInfo, oldEntity, ModuleTypeEnum.LISTING_INFO.getCode(), oldEntity.getId(), msg);
                }

            }
        return ApiResult.success();
    }
    @Override
    public void updateMongodbData(String platform,String uniqueId, Integer isClean){
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }
    /**
     * 根据平台组装表名
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.PRODUCT.getCode());
    }
    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle(StrUtil.format("平台产品消息消费失败，来源平台:{},目标平台:{}", dmpPullTaskEntity.getSourcePlatformName(), dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.THIRD_WAREHOUSE_GET_SKU.getTableName());
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(StringUtils.isBlank(msg) ? "" : msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}

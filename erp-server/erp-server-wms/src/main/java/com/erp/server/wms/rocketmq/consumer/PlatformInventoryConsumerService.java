package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasInventoryService;
import com.erp.server.wms.service.OverseasProviderService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下载平台库存消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_inventory_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_inventory_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformInventoryConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private OverseasInventoryService overseasInventoryService;

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(uniqueId) || org.apache.commons.lang3.StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
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
        return CharSequenceUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.INBOUND.getCode());
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }


    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity,msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformInventoryDTO dto = JSONUtil.toBean(ext.toString(), PlatformInventoryDTO.class);
        // 查询仓库ID
        List<OverseasProviderDTO.ListWithWarehouseDTO> overseasWareHouseList = overseasProviderService.listAllMatch();
        if (overseasWareHouseList.isEmpty()){
            return ApiResult.success();
        }
        List<OverseasProviderDTO.ListWithWarehouseDTO> warehouseDTOS = overseasWareHouseList.stream()
                .filter(e-> e.getCode().equalsIgnoreCase(dto.getPlatform()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseDTOS)){
            if (overseasWareHouseList.isEmpty()){
                return ApiResult.success();
            }
        }
        OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = warehouseDTOS.stream()
                .filter(e -> e.getPlatformWarehouseCode().equalsIgnoreCase(dto.getPlatformWarehouseCode()))
                .findFirst().orElse(null);
        if (null == warehouseDTO){
            return ApiResult.success();
        }
        dto.setWarehouseId(warehouseDTO.getWarehouseId());

        List<String> warehouseIds = warehouseDTOS.stream().map(OverseasProviderDTO.ListWithWarehouseDTO::getWarehouseId).collect(Collectors.toList());

        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            //转换成数据库实体对象
            OverseasInventoryEntity entity = OverseasWarehouseConverter.INSTANCE.inventoryDtoToDb(dto);
            if (CharSequenceUtil.isNotBlank(entity.getPlatformSku())){
                ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
                paramDTO.setPlatform(entity.getDictPlatform());
                paramDTO.setWarehouseIdList(warehouseIds);
                paramDTO.setPlatformSkuNoList(Collections.singletonList(entity.getPlatformSku()));
                paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
                paramDTO.setIsExpire(false);
                // 查询ListingInfo和skuMapping的关系
                List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
                if(CollectionUtils.isNotEmpty(listingedInfoWithSkuMappingList)){
                    ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingedInfoWithSkuMappingList.stream()
                            .filter(e-> (e.getHasMappingAll() && e.getPlatformSkuNo().equalsIgnoreCase(dto.getProductSku()))
                                    || (e.getWarehouseId().equalsIgnoreCase(dto.getWarehouseId()) && e.getPlatformSkuNo().equalsIgnoreCase(dto.getProductSku()))
                            ).findFirst().orElse(null);
                    if (null != listingInfoWithSkuMappingDTO){
                        entity.setPlatformSkuName(listingInfoWithSkuMappingDTO.getPlatformSkuName().trim());
                        entity.setProductName(listingInfoWithSkuMappingDTO.getProductName().trim());
                        entity.setSkuId(listingInfoWithSkuMappingDTO.getProductSkuId().trim());
                        entity.setSkuNo(listingInfoWithSkuMappingDTO.getProductSkuNo().trim());
                    }
                }
            }
            overseasInventoryService.saveOrUpdateByPlatform(entity);
            //可能首次平台sku没有配置映射关系，在拉取数据时查没有映射关系的重新配置
            overseasInventoryService.handleNotMapping(entity.getDictPlatform());
        }
        return ApiResult.success();
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("库存消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(CharSequenceUtil.isBlank(msg)?"":msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}

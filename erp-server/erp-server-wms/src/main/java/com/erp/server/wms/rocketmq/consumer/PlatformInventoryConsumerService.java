package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.server.wms.convert.OverseasWarehouseConverter;
import com.erp.server.wms.service.OverseasInventoryService;
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
    private OmsListingInfoFeign omsListingInfoFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformInventoryDTO dto = JSONUtil.toBean(ext.toString(), PlatformInventoryDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            //转换成数据库实体对象
            OverseasInventoryEntity entity = OverseasWarehouseConverter.INSTANCE.inventoryDtoToDb(dto);
            if (StringUtils.isNotBlank(entity.getPlatformSku())){
                ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
                paramDTO.setPlatform(entity.getDictPlatform());
                paramDTO.setPlatformSkuNoList(Collections.singletonList(entity.getPlatformSku()));
                // 查询ListingInfo和skuMapping的关系
                List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = omsListingInfoFeign.listingInfoWithSkuMappingList(paramDTO);
                if(CollectionUtils.isNotEmpty(listingedInfoWithSkuMappingList)){
                    ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingedInfoWithSkuMappingList.get(0);
                    entity.setPlatformSkuName(listingInfoWithSkuMappingDTO.getPlatformSkuName());
                    entity.setProductName(listingInfoWithSkuMappingDTO.getProductName());
                    entity.setSkuId(listingInfoWithSkuMappingDTO.getProductSkuId());
                    entity.setSkuNo(listingInfoWithSkuMappingDTO.getProductSkuNo());
                }
            }
            overseasInventoryService.saveOrUpdateByPlatform(entity);
        }
        return ApiResult.success();
    }


    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(StrUtil.format("库存消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo("");
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}

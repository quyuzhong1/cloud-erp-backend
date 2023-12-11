package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.FbaShipmentConsumerConverter;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.FbaShipmentStatusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 下载FBA货件消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_fba_shipment_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_fba_shipment_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformFbaShipmentConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Resource
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Resource
    private FbaShipmentStatusService fbaShipmentStatusService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
//        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformFbaShipmentDTO dto = JSONUtil.toBean(ext.toString(), PlatformFbaShipmentDTO.class);
        // 组合信息
        FbaShipmentEntity entity = FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToEntity(dto);

        // 签收信息
        List<PlatformFbaShipmentReceiveDTO> receiveDTOList = dto.getReceiveDTOList();

        // 填充最新签收时间
        if (!CollectionUtils.isEmpty(receiveDTOList)){
            LocalDateTime maxReceiveTime = receiveDTOList.stream()
                    .map(PlatformFbaShipmentReceiveDTO::getReceiveDate)
                    .max(LocalDateTime::compareTo)
                    .get();
            entity.setShipmentReceiveTime(maxReceiveTime);
        }

        // 卖家SKU列表
        List<String> sellerSkuList = receiveDTOList.stream().map(PlatformFbaShipmentReceiveDTO::getSellerSku).distinct().collect(Collectors.toList());
        // SKU绑定的信息
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = new HashMap<>();
        // sku是否是组合类型
        List<String> hasChildrenSkuIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(sellerSkuList)){
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            // 查询ListingInfo和skuMapping的关系
            List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = omsListingInfoFeign.listingInfoWithSkuMappingList(paramDTO);

            // SKU相关信息
            listingInfoWithSkuMappingDTOMap = listingedInfoWithSkuMappingList
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));

            List<String> erpSkuIds = listingedInfoWithSkuMappingList
                    .stream()
                    .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                    .filter(e -> !StringUtils.isEmpty(e))
                    .distinct()
                    .collect(Collectors.toList());

            // 组合类型skuIds
            hasChildrenSkuIds = plmTaskFeign.listBomChildBySkuIds(erpSkuIds)
                    .stream()
                    .map(BomChildrenSkuDTO::getParentSkuId)
                    .collect(Collectors.toList());
        }
        // 查询国家信息
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(dto.getCountryId());
        entity.setCountryName(null != countryEntity ? countryEntity.getNameCn() : "");

        // 新增或更新
        FbaShipmentEntity oldEntity = fbaShipmentService.getByFbaShipmentId(entity.getFbaShipmentId());
        if (null == oldEntity){
            // 新增
            fbaShipmentService.checkAndSaveAll(entity, listingInfoWithSkuMappingDTOMap, hasChildrenSkuIds,   receiveDTOList, dto.checkAndGetDetailList());
        } else {
            // 修改
            fbaShipmentService.checkAndUpdateAll(oldEntity, entity, listingInfoWithSkuMappingDTOMap, hasChildrenSkuIds, receiveDTOList, dto.checkAndGetDetailList());
        }

        return ApiResult.success();
    }
}

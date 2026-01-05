package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
public class PlatformOutboundToOmsPushDownWmsConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Lazy
    @Resource
    private AsyncService asyncService;
    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private PlatformOutboundConsumerService platformOutboundConsumerService;

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
                platform, BusinessTypeEnum.OUTBOUND.getCode());
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity, msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    public static void main(String[] args) {
        String a = "{\n" +
                "  \"authId\": \"1976194554104430593\",\n" +
                "  \"provider\": \"iml\",\n" +
                "  \"orderCode\": \"OT80565-20251021-000001\",\n" +
                "  \"referenceNo\": \"WFHD20251021001\",\n" +
                "  \"orderStatus\": \"waitShipped\",\n" +
                "  \"interceptStatus\": \"\",\n" +
                "  \"thirdOrderStatus\": \"WAIT_OUTBOUND\",\n" +
                "  \"outBoundTime\": \"\",\n" +
                "  \"trackNo\": \"123456\",\n" +
                "  \"abnormalProblemReason\": \"\"\n" +
                "}";
        PlatformOutboundDTO dto = JSONUtil.toBean(a, PlatformOutboundDTO.class);
        System.out.println(dto);
    }
    @Override
    public ApiResult<?> handle(Object ext) {
        PlatformOutboundDTO dto = JSONUtil.toBean(ext.toString(), PlatformOutboundDTO.class);
        log.warn("三方仓自动出库参数>>>>>>>{}",JSONUtil.toJsonStr(dto));
        //判断出库单是否已发货
        if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
            return ApiResult.success();
        }

        //平台订单号
        String swOrderNumber = dto.getSwOrderNumber();
        //客户参考号
        String referenceNo = dto.getReferenceNo();
        if(StringUtils.isBlank(swOrderNumber) && StringUtils.isBlank(referenceNo)){
            log.error("三方仓自动出库: 平台订单号为空 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }

        // 查询已有订单
        List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).in (SoB2cEntity::getPlatformCode, Arrays.asList(swOrderNumber,referenceNo)).list();
        if (CollUtil.isEmpty(list)) {
            log.error("三方仓自动出库: 未找到B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }
        if (list.size() > 1) {
            log.error("三方仓自动出库: 找到多条B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }

        //根据平台不同，实际平台订单号可能存在referenceNo中
        SoB2cEntity mainEntity = list.get(0);
        if(mainEntity.getPlatformCode().equals(referenceNo)){
            dto.setSwOrderNumber(referenceNo);
        }else {
            dto.setReferenceNo(swOrderNumber);
        }

        //1.校验B2C销售订单数是否已经审核通过
        ApproveStatusEnum approveStatus = mainEntity.getApproveStatus();
        if(!Objects.equals(approveStatus,ApproveStatusEnum.APPROVE)){
            //更新异常订单信息
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    mainEntity.getId(),
                    SoB2cErrorTypeEnum.AUTO_OUTBOUND_ERROR.getCode(),
                    null,
                    "自动生成销售出库单失败：订单未审核或审核不通过",
                    JSONUtil.toJsonStr(dto),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }

        //2.根据平台ID+平台SKU是否配对映射产品SKU，若存在未配对则标记B2C销售订单为“自动出库异常”且产品SKU显示“SKU未匹配”
        List<SoB2cDetailEntity> detailList = FeignQuery.create(SoB2cDetailEntity.class)
                .eq(SoB2cDetailEntity::getMainId, mainEntity.getId()).list();
        if(CollUtil.isEmpty(detailList)){
            log.error("三方仓自动出库: 未找到B2C销售订单明细 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }

        // 查询明细所有历史映射关系
        List<String> platformSkuList = detailList.stream()
                .map(SoB2cDetailEntity::getPlatformSkuNo)
                .distinct()
                .collect(Collectors.toList());
        // 查询明细所有历史映射关系
        List<String> platformSpuList = detailList.stream()
                .map(SoB2cDetailEntity::getPlatformSpuNo)
                .distinct()
                .collect(Collectors.toList());
        SkuMappingDTO.PlatformSkuNoParamDTO paramDTO = new SkuMappingDTO.PlatformSkuNoParamDTO();
        paramDTO.setPlatformSkuList(platformSkuList);
        paramDTO.setPlatformSpuList(platformSpuList);
        paramDTO.setDictPlatform(mainEntity.getDictPlatform());
        paramDTO.setShopId(mainEntity.getShopId());
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = skuMappingFeign.mapListingByPlatformSkuNo(paramDTO);

        StringBuffer sb = new StringBuffer();
        for (SoB2cDetailEntity detailItem : detailList) {
            List<ListingInfoWithSkuMappingDTO> mappingList;
            if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(mainEntity.getDictPlatform())
                    || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(mainEntity.getDictPlatform())){
                mappingList = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSpuNo(), Collections.emptyList());
                mappingList = mappingList.stream().filter(v->v.getPlatformSkuNo().equals(detailItem.getPlatformSkuNo())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(mappingList)){
                    mappingList = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSpuNo(), Collections.emptyList());
                }
            }else{
                mappingList = listingInfoWithSkuMappingDTOMap.getOrDefault(detailItem.getPlatformSkuNo(), Collections.emptyList());
            }

            if(CollUtil.isEmpty(mappingList)){
                sb.append(StrUtil.format("【{}】",detailItem.getSkuNo()));
                sb.append(";");
            }
        }
        String skuMappingError = sb.toString();
        if(StringUtils.isNotBlank(skuMappingError)){
            //更新异常订单信息
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    mainEntity.getId(),
                    SoB2cErrorTypeEnum.AUTO_OUTBOUND_ERROR.getCode(),
                    null,
                    StrUtil.format("自动生成销售出库单失败：存在【{}】平台未映射SKU",PlatformDictEnum.getNameByCode(mainEntity.getDictPlatform()),skuMappingError),
                    JSONUtil.toJsonStr(dto),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }

        //3.判断安兔/速派通订单查询接口返回的仓库代码是否已绑定数大臣仓库代码
        String platformWarehouseCode = dto.getWarehouseCode();
        if(StringUtils.isBlank(platformWarehouseCode)){
            log.error("三方仓自动出库: 三方仓代码warehouseCode为空 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }
        OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
        feignDTO.setCode(dto.getPlatform());
        feignDTO.setPlatformWarehouseCode(platformWarehouseCode);
        OverseasProviderDTO.FeignDTO overseasWarehouse = overseasProviderService.getOverseasWarehouse(feignDTO);
        if(Objects.isNull(overseasWarehouse)){
            //更新异常订单信息
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    mainEntity.getId(),
                    SoB2cErrorTypeEnum.AUTO_OUTBOUND_ERROR.getCode(),
                    null,
                    "自动生成销售出库单失败：三方仓库未映射",
                    JSONUtil.toJsonStr(dto),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            return ApiResult.success();
        }
        //4.“三方仓发货单”
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = generateThirdWarehouseDelivery(detailList, dto, mainEntity);
        //5.“销售出库单”、“物流轨迹单”、“虚拟仓库存流水”、“出货仓库存流水”
        platformOutboundConsumerService.generateSoOut(mainEntity, thirdWarehouseDeliveryEntity, dto,"");
        return ApiResult.success();
    }


    private ThirdWarehouseDeliveryEntity generateThirdWarehouseDelivery(List<SoB2cDetailEntity> detailList, PlatformOutboundDTO dto, SoB2cEntity mainEntity) {
        List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDeliveryDetailEntities = new ArrayList<>(detailList.size());
        for (SoB2cDetailEntity soB2cDetailEntity : detailList) {
            ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
            thirdWarehouseDeliveryDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setDeliveryQty(soB2cDetailEntity.getQty());
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(soB2cDetailEntity.getWarehouseId());
            thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo(soB2cDetailEntity.getPlatformSkuNo());
            thirdWarehouseDeliveryDetailEntity.setPlatformWarehouseCode(dto.getWarehouseCode());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setSoDetailId(soB2cDetailEntity.getId());
            thirdWarehouseDeliveryDetailEntities.add(thirdWarehouseDeliveryDetailEntity);
        }
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        thirdWarehouseDeliveryEntity.setCode(dto.getReferenceNo());
        thirdWarehouseDeliveryEntity.setSoCode(mainEntity.getCode());
        thirdWarehouseDeliveryEntity.setSoId(mainEntity.getId());
        thirdWarehouseDeliveryEntity.setDictPlatform(mainEntity.getDictPlatform());
        thirdWarehouseDeliveryEntity.setPlatformCode(dto.getSwOrderNumber());
        thirdWarehouseDeliveryEntity.setThirdWarehousePlatform(dto.getPlatform());
        thirdWarehouseDeliveryEntity.setShippingMethod(dto.getShippingMethod());
        thirdWarehouseDeliveryEntity.setStatus(dto.getOrderStatus());
        thirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDeliveryDetailEntities);
        return thirdWarehouseDeliveryService.add(thirdWarehouseDeliveryEntity,true);
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity, String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(CharSequenceUtil.format("平台出库消息消费失败，来源平台:{},目标平台:{}", dmpPullTaskEntity.getSourcePlatformName(), dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(CharSequenceUtil.isBlank(msg) ? "" : msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

}

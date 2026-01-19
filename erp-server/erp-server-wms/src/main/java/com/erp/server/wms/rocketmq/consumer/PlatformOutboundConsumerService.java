package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.DistributeLocker;
import com.common.business.constant.BusinessNoConstant;
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
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
//@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
//        selectorExpression = "third_system_outbound_tag",
//        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_outbound_consumer",
//        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOutboundConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private PlatformOutboundConsumerService platformOutboundConsumerService;

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
    private ThirdWarehouseDeliveryDetailService thirdWarehouseDeliveryDetailService;

    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

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
        log.warn("第三方出库单参数>>>>>>>{}",JSONUtil.toJsonStr(dto));
        //这个是B2c销售订单code
        String referenceNo = dto.getReferenceNo();
        String billStatus = dto.getOrderStatus();
        // 查询已有订单
        Map<SoB2cEntity ,ThirdWarehouseDeliveryEntity > map = new HashMap<>();
        if(OmsPlatformEnum.WEI_SHI.getCode().equals(dto.getPlatform()) && referenceNo.contains("_")){
            //截取_前面的字符串
            referenceNo = referenceNo.split("_")[0];
        }

        if(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())
                && (OmsPlatformEnum.OMS_ANTU.getCode().equals(dto.getPlatform()) || OmsPlatformEnum.OMS_SPT.getCode().equals(dto.getPlatform()))){
            map = checkAndBuildMap(dto);
        }else if(referenceNo.contains(BusinessNoConstant.WFHD)){
            //查询三方仓发货单
            ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(referenceNo);
            if(Objects.isNull(thirdWarehouseDeliveryEntity)){
                log.error("第三方出库单: 未找到三方仓发货单 >>>>>>>{}",JSONUtil.toJsonStr(dto));
                return ApiResult.success();
            }else {
                if(Objects.nonNull(thirdWarehouseDeliveryEntity) && thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus())){
                    return ApiResult.success();
                }
                String soCode = thirdWarehouseDeliveryEntity.getSoCode();
                SoB2cEntity mainEntity = soB2cFeign.getSoCode(soCode);

                SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
                updateStatus.setSoCode(mainEntity.getCode());
                updateStatus.setSoId(mainEntity.getId());
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
                    updateStatus.setBillStatus(billStatus);
                    updateStatus.setAddOperationLog(true);
                }
                updateStatus.setTrackNo(dto.getTrackNo());
                soB2cFeign.updateSoB2cStatusByParams(updateStatus);

                map.put(mainEntity ,thirdWarehouseDeliveryEntity);
            }
        }else {
            SoB2cEntity mainEntity = soB2cFeign.getSoCode(referenceNo);
            if(null == mainEntity){
                if (CharSequenceUtil.isBlank(referenceNo)){
                    return ApiResult.success();
                }
                // 非ERP单号前缀
                if (!referenceNo.startsWith(BusinessNoConstant.XSDS) && !referenceNo.startsWith(BusinessNoConstant.XSDD)){
                    return ApiResult.success();
                }
                log.error("第三方出库单: 未找到B2C销售订单 >>>>>>>{}",JSONUtil.toJsonStr(dto));
                return ApiResult.success();
            }
            ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getByCodeAndSoId(mainEntity.getShippingOrderNo(),mainEntity.getId());
            if(Objects.nonNull(thirdWarehouseDeliveryEntity) && thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus())){
                return ApiResult.success();
            }

            SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
            updateStatus.setSoCode(mainEntity.getCode());
            updateStatus.setSoId(mainEntity.getId());
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
                updateStatus.setBillStatus(billStatus);
                updateStatus.setAddOperationLog(true);
            }
            updateStatus.setTrackNo(dto.getTrackNo());
            soB2cFeign.updateSoB2cStatusByParams(updateStatus);

            map.put(mainEntity ,thirdWarehouseDeliveryEntity);
        }

        //校验map不为null并且不为空
        if(Objects.isNull(map) || map.isEmpty()){
            log.error("第三方出库单: 未找到B2C销售订单或三方仓发货单 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return ApiResult.success();
        }

        for (Map.Entry<SoB2cEntity, ThirdWarehouseDeliveryEntity> entry : map.entrySet()) {
            SoB2cEntity mainEntity = entry.getKey();
            ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = entry.getValue();


            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                //通邮仓跟踪号取订单跟踪号
                if (CharSequenceUtil.equals(PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode(),dto.getPlatform())) {
                    List<SoB2cLogisticsEntity> list = FeignQuery.create(SoB2cLogisticsEntity.class).eq(SoB2cLogisticsEntity::getMainId, mainEntity.getId()).list();
                    if (CollUtil.isNotEmpty(list)) {
                        String trackNo = list.get(0).getTrackNo();
                        dto.setTrackNo(CharSequenceUtil.isBlank(trackNo) ? dto.getTrackNo() : trackNo);
                    }
                }

                // 明细的存在没有标发的情况触发
                List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(mainEntity.getId()));
                if(detailList.stream().anyMatch(v->!v.getIsSignShipped())){
                    // 校验平台来源明细
                    if (soB2cFeign.checkPlatformShipOrder(mainEntity.getId())) {
                        // 调用第三方平台SDK标记发货(独立事务)
                        String businessDesc = "第三方仓出库";
                        asyncService.asyncShipOrder(mainEntity.getId(),
                                mainEntity.getCode(),
                                mainEntity.getDictPlatform(),
                                mainEntity.convertSubmitPlatformUniqueKey(),
                                JSONUtil.toJsonStr(dto),
                                businessDesc, false, false);
                    }
                }
                //拦截中清除拦截状态
                if(mainEntity.getIsIntercept()){
                    mainEntity.setIsIntercept(false);
                    mainEntity.setIsFrozen(false);
                    soB2cFeign.updateStatus(mainEntity);
                }
                //清除三方仓异常
                if(SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode().equals(mainEntity.getSignOrderError())){
                    String type = SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode();
                    SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
                    deleteDTO.setMainId(mainEntity.getId());
                    deleteDTO.setType(type);
                    soB2cFeign.deleteError(deleteDTO);
                }

                platformOutboundConsumerService.generateSoOut(mainEntity, thirdWarehouseDeliveryEntity, dto,"");
            }

            if (SoB2cBillStatusEnum.ENUM_EXCEPTION.getCode().equals(dto.getOrderStatus())) {
                //更新异常订单信息
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                        mainEntity.getId(),
                        SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode(),
                        JSONUtil.toJsonStr(dto),
                        dto.getAbnormalProblemReason(),
                        JSONUtil.toJsonStr(dto),
                        ""
                );
                soB2cFeign.addSoB2cError(addError);
                //异步取消海外仓订单
                asyncService.asyncCancelThirdWarehouseOrder(mainEntity,dto.getAbnormalProblemReason());
            }
            if (SoB2cBillStatusEnum.ENUM_DISUSE.getCode().equals(dto.getOrderStatus())) {
                if(mainEntity.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode())){
                    OperateLogDTO.AddModuleOperateLogDTO operateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
                    operateLogDTO.setOperation("三方仓出库单废弃");
                    operateLogDTO.setModuleType(ModuleTypeEnum.SO_B2C.getCode());
                    operateLogDTO.setBusinessId(mainEntity.getId());
                    //订单如果为拦截中，直接更新订单状态为
                    mainEntity.setApproveStatus(ApproveStatusEnum.REJECT);
                    mainEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
                    mainEntity.setIsIntercept(false);
                    mainEntity.setIsFrozen(false);
                    mainEntity.setShippingOrderNo("");
                    mainEntity.setRemark("三方仓出库单废弃,拦截成功");
                    if(mainEntity.getIsCancel()){
                        mainEntity.setInvalidStatus(Boolean.TRUE);
                        mainEntity.setInvalidRemark("平台订单取消,拦截成功自动作废");
                    }
                    soB2cFeign.updateStatus(mainEntity);
                    operateLogDTO.setContent("三方仓出库单废弃");
                    soB2cFeign.addModuleOperateLog(operateLogDTO);
                    if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
                        thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
                        operateLogService.addModuleOperateLog("状态变更为取消发货", ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(),thirdWarehouseDeliveryEntity.getId(), "状态变更");

                        thirdWarehouseDeliveryService.updateById(thirdWarehouseDeliveryEntity);
                    }
                }
            }
        }
        return ApiResult.success();
    }


    private Map<SoB2cEntity,ThirdWarehouseDeliveryEntity> checkAndBuildMap(PlatformOutboundDTO dto) {
        Map<SoB2cEntity,ThirdWarehouseDeliveryEntity> resultMap = new HashMap<>();
        List<SoB2cEntity> mainEntityList = new ArrayList<>();
        //平台订单号
        String swOrderNumber = dto.getSwOrderNumber();
        String referenceNo = dto.getReferenceNo();
        if(StringUtils.isBlank(swOrderNumber) && StringUtils.isBlank(referenceNo)){
            log.error("三方仓自动出库: 平台订单号为空 >>>>>>>{}",JSONUtil.toJsonStr(dto));
            return null;
        }

        List<SoB2cEntity> listBySwOrderNumber = new ArrayList<>();
        List<SoB2cEntity> listByReferenceNo = new ArrayList<>();
        if(StringUtils.isNotBlank(swOrderNumber)) {
            // 查询已有订单
            listBySwOrderNumber = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getPlatformCode, swOrderNumber).ne(SoB2cEntity::getSourceType, SoB2cSourcePlatformEnum.ENUM_SELF_ADD.getCode()).list();
        }
        if(StringUtils.isNotBlank(referenceNo)){
            // 查询已有订单
            listByReferenceNo = FeignQuery.create(SoB2cEntity.class).eq (SoB2cEntity::getPlatformCode, referenceNo).ne(SoB2cEntity::getSourceType, SoB2cSourcePlatformEnum.ENUM_SELF_ADD.getCode()).list();
        }

        if (CollUtil.isEmpty(listBySwOrderNumber) && CollUtil.isEmpty(listByReferenceNo)) {
            log.error("三方仓自动出库: 未找到B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
            return null;
        }

        //根据平台不同，实际平台订单号可能存在referenceNo中
        if(CollUtil.isNotEmpty(listBySwOrderNumber)){
            mainEntityList = listBySwOrderNumber;
        }else {
            mainEntityList = listByReferenceNo;
        }

        List<String> soB2cIds = mainEntityList.stream().map(SoB2cEntity::getId).collect(Collectors.toList());

        Map<String, SoB2cDeliveryEntity> soB2cDeliveryMap = soB2cDeliveryService.lambdaQuery().in(SoB2cDeliveryEntity::getSourceId, soB2cIds).list().stream().collect(Collectors.toMap(SoB2cDeliveryEntity::getSourceId, Function.identity(), (o1, o2) -> o1));

        for (SoB2cEntity mainEntity : mainEntityList) {
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryMap.get(mainEntity.getId());
            if(Objects.nonNull(soB2cDeliveryEntity)){
                log.error("三方仓自动出库:B2C销售订单【{}】 已存在B2C发货单【{}】，无法生成三方仓放货单 ",mainEntity.getCode(),soB2cDeliveryEntity.getCode());
                continue;
            }

            String platformCode ="";
            if(mainEntity.getPlatformCode().equals(referenceNo)){
                platformCode = referenceNo;
            }else {
                platformCode = swOrderNumber ;
            }

            //1.校验B2C销售订单数是否已经审核通过
            ApproveStatusEnum approveStatus = mainEntity.getApproveStatus();
            if(!Objects.equals(approveStatus,ApproveStatusEnum.APPROVE)){
                //更新异常订单信息
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                        mainEntity.getId(),
                        SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode(),
                        JSONUtil.toJsonStr(dto),
                        "自动生成销售出库单失败：订单未审核或审核不通过",
                        JSONUtil.toJsonStr(dto),
                        ""
                );
                soB2cFeign.addSoB2cError(addError);
                continue;
            }

            //2.根据平台ID+平台SKU是否配对映射产品SKU，若存在未配对则标记B2C销售订单为“自动出库异常”且产品SKU显示“SKU未匹配”
            List<SoB2cDetailEntity> detailList = FeignQuery.create(SoB2cDetailEntity.class)
                    .eq(SoB2cDetailEntity::getMainId, mainEntity.getId())
                    .list();
            if(CollUtil.isEmpty(detailList)){
                log.error("三方仓自动出库: 未找到B2C销售订单明细 >>>>>>>{}",JSONUtil.toJsonStr(dto));
                continue;
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
                        SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode(),
                        JSONUtil.toJsonStr(dto),
                        StrUtil.format("自动生成销售出库单失败：存在【{}】平台未映射SKU",PlatformDictEnum.getNameByCode(mainEntity.getDictPlatform()),skuMappingError),
                        JSONUtil.toJsonStr(dto),
                        ""
                );
                soB2cFeign.addSoB2cError(addError);
                continue;
            }

            //3.判断安兔/速派通订单查询接口返回的仓库代码是否已绑定数大臣仓库代码
            String platformWarehouseCode = dto.getWarehouseCode();
            if(StringUtils.isBlank(platformWarehouseCode)){
                log.error("三方仓自动出库: 三方仓代码warehouseCode为空 >>>>>>>{}",JSONUtil.toJsonStr(dto));
                //更新异常订单信息
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                        mainEntity.getId(),
                        SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode(),
                        JSONUtil.toJsonStr(dto),
                        "自动生成销售出库单失败：三方仓代码warehouseCode为空",
                        JSONUtil.toJsonStr(dto),
                        ""
                );
                soB2cFeign.addSoB2cError(addError);
                continue;
            }
            OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
            feignDTO.setCode(dto.getPlatform());
            feignDTO.setPlatformWarehouseCode(platformWarehouseCode);
            OverseasProviderDTO.FeignDTO overseasWarehouse = overseasProviderService.getOverseasWarehouse(feignDTO);
            if(Objects.isNull(overseasWarehouse)){
                //更新异常订单信息
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                        mainEntity.getId(),
                        SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode(),
                        JSONUtil.toJsonStr(dto),
                        "自动生成销售出库单失败：三方仓库未映射",
                        JSONUtil.toJsonStr(dto),
                        ""
                );
                soB2cFeign.addSoB2cError(addError);
                continue;
            }
            //4.更新B2C销售订单状态
            SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
            updateStatus.setSoCode(mainEntity.getCode());
            updateStatus.setSoId(mainEntity.getId());
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
                updateStatus.setBillStatus(dto.getOrderStatus());
                updateStatus.setAddOperationLog(true);
            }
            updateStatus.setTrackNo(dto.getTrackNo());
            soB2cFeign.updateSoB2cStatusByParams(updateStatus);

            //5.“三方仓发货单”
            ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = generateThirdWarehouseDelivery(detailList, dto, mainEntity, platformCode);
            resultMap.put(mainEntity,thirdWarehouseDeliveryEntity);

            //6.清除自动出库异常异常
            if(SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode().equals(mainEntity.getSignOrderError())){
                String type = SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode();
                SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
                deleteDTO.setMainId(mainEntity.getId());
                deleteDTO.setType(type);
                soB2cFeign.deleteError(deleteDTO);
            }
        }
        return resultMap;
    }


    //生成三方仓发货单
    private ThirdWarehouseDeliveryEntity generateThirdWarehouseDelivery(List<SoB2cDetailEntity> detailList, PlatformOutboundDTO dto, SoB2cEntity mainEntity,String platformCode) {
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getSoId, mainEntity.getId())
                .orderByDesc(ThirdWarehouseDeliveryEntity::getCreateTime).last("LIMIT 1").one();

        if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
            return thirdWarehouseDeliveryEntity;
        }

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
        thirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        thirdWarehouseDeliveryEntity.setCode(dto.getReferenceNo());
        thirdWarehouseDeliveryEntity.setSoCode(mainEntity.getCode());
        thirdWarehouseDeliveryEntity.setSoId(mainEntity.getId());
        thirdWarehouseDeliveryEntity.setDictPlatform(mainEntity.getDictPlatform());
        thirdWarehouseDeliveryEntity.setPlatformCode(platformCode);
        thirdWarehouseDeliveryEntity.setThirdWarehousePlatform(dto.getPlatform());
        thirdWarehouseDeliveryEntity.setShippingMethod(dto.getShippingMethod());
        thirdWarehouseDeliveryEntity.setStatus(dto.getOrderStatus());
        thirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDeliveryDetailEntities);
        return thirdWarehouseDeliveryService.add(thirdWarehouseDeliveryEntity,true);
    }

    @DistributeLocker(keyName = "dto.referenceNo")
    public void generateSoOut(SoB2cEntity mainEntity, ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity, PlatformOutboundDTO dto,String warehouseId) {
        // 校验是否已生成销售出库单
        boolean exist = soOutstockService.checkExist(mainEntity.getCode(), SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode(), OrderTypeEnum.B2C.getCode());
        if (exist) {
            log.warn("销售订单{} 已生成销售出库单, 忽略生成", mainEntity.getCode() );
            return;
        }
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutStockByIdAndWarehouseId(mainEntity.getId(),warehouseId);
        //查询三方仓发货明细，重新赋值明细数据
        if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
            List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDeliveryDetailEntityList = thirdWarehouseDeliveryDetailService.listByMainId(thirdWarehouseDeliveryEntity.getId());
            if(CollectionUtils.isNotEmpty(thirdWarehouseDeliveryDetailEntityList)){
                LinkedList<SoOutstockDetailDTO.AddDTO> wantDetailList = new LinkedList<>();
                List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Arrays.asList(mainEntity.getId()));
                for (ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity : thirdWarehouseDeliveryDetailEntityList) {
                    //表示有啊
                    SoOutstockDetailDTO.AddDTO addDTO = new SoOutstockDetailDTO.AddDTO();
                    // 明细记录平台单号
                    addDTO.setPlatformCode(mainEntity.getPlatformCode());
                    addDTO.setSkuId(thirdWarehouseDeliveryDetailEntity.getSkuId());
                    addDTO.setSkuNo(thirdWarehouseDeliveryDetailEntity.getSkuNo());
                    SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(v->v.getSkuId().equals(thirdWarehouseDeliveryDetailEntity.getSourceSkuId())).findFirst().orElse(new SoB2cDetailEntity());
                    addDTO.setSourceDetailId(thirdWarehouseDeliveryDetailEntity.getId());
                    addDTO.setSoDetailId(soB2cDetailEntity.getId());
                    addDTO.setPlanQty(thirdWarehouseDeliveryDetailEntity.getDeliveryQty());
                    addDTO.setActualQty(thirdWarehouseDeliveryDetailEntity.getDeliveryQty());
                    addDTO.setWarehouseLocation(soB2cDetailEntity.getWarehouseLocation());
                    if(StringUtils.isNotBlank(warehouseId)){
                        addDTO.setWarehouseId(warehouseId);
                    }
                    if(StringUtils.isNotBlank(thirdWarehouseDeliveryEntity.getActualDeliveryCode())){
                        addDTO.setRemark(thirdWarehouseDeliveryEntity.getActualDeliveryCode());
                    }else{
                        addDTO.setRemark("三方仓出库自动生成");
                    }
                    wantDetailList.add(addDTO);
                }
                generateB2cDTO.setDetailList(wantDetailList);
            }
            generateB2cDTO.setSourceCode(thirdWarehouseDeliveryEntity.getCode());
            generateB2cDTO.setSourceId(thirdWarehouseDeliveryEntity.getId());
        }
        // 第三方仓出库生成销售出库单（独立事务）
        generateB2cDTO.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode());
        soOutstockService.thirdWarehouseCheckAndGenerate(generateB2cDTO, dto);
        if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
            if(!SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus().equals(thirdWarehouseDeliveryEntity.getStatus())){
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus());
                operateLogService.addModuleOperateLog("状态变更已发货", ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(),thirdWarehouseDeliveryEntity.getId(), "状态变更");

                thirdWarehouseDeliveryService.updateById(thirdWarehouseDeliveryEntity);
            }
        }
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

package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.*;
import com.sdk.wms.antu.dto.request.AntuGetOutboundRefReq;
import com.sdk.wms.antu.dto.response.AntuOutboundResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.service.AntuService;
import com.common.business.threadlocal.ThirdWarehouseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private VirtualWarehouseChannelService virtualWarehouseChannelService;
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private AntuService antuService;

    @Resource
    private RedissonClient redissonClient;

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
        log.warn("第三方出库单参数>>>>>>>{}", JSONUtil.toJsonStr(dto));
        //这个是B2c销售订单code
        String referenceNo = dto.getReferenceNo();
        String billStatus = dto.getOrderStatus();

        // 查询已有订单
        Map<SoB2cEntity, ThirdWarehouseDeliveryEntity> map = new HashMap<>();
        if (OmsPlatformEnum.WEI_SHI.getCode().equals(dto.getPlatform()) && referenceNo.contains("_")) {
            //截取_前面的字符串
            referenceNo = referenceNo.split("_")[0];
        }
        // 3. 获取分布式锁（阻塞等待）
        String lockKey = "lock:third:outbound:" + referenceNo;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁，最多等待 30 秒；获取锁后租期自动续期（-1 表示看门狗自动续期）
            if (!lock.tryLock(30, -1, TimeUnit.SECONDS)) {
                log.error("订单 {} 等待锁超时，系统繁忙", referenceNo);
                return ApiResult.error("系统繁忙，请稍后重试");
            }
            //是否需要走标发业务
            Boolean isSignShipped = true;
            if (!referenceNo.contains(BusinessNoConstant.WFHD)
                    && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())
                    && (OmsPlatformEnum.OMS_ANTU.getCode().equals(dto.getPlatform()) || OmsPlatformEnum.OMS_SPT.getCode().equals(dto.getPlatform()))) {
                map = checkAndBuildMap(dto);

                //不标发
                isSignShipped = false;
            } else if (referenceNo.contains(BusinessNoConstant.WFHD)) {
                //查询三方仓发货单
                ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(referenceNo);
                if (Objects.isNull(thirdWarehouseDeliveryEntity)) {
                    log.error("第三方出库单: 未找到三方仓发货单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                    return ApiResult.success();
                } else {
                    if (Objects.nonNull(thirdWarehouseDeliveryEntity) && thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus())) {
                        return ApiResult.success();
                    }
                    String soCode = thirdWarehouseDeliveryEntity.getSoCode();
                    SoB2cEntity mainEntity = soB2cFeign.getSoCode(soCode);
                    if (Objects.isNull(mainEntity)) {
                        log.error("第三方出库单: 未找到B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                        return ApiResult.success();
                    }
                    if (Objects.equals(mainEntity.getInvalidStatus(), true)) {
                        log.error("第三方出库单: B2C销售订单已作废 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                        return ApiResult.success();
                    }

                    SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
                    updateStatus.setSoCode(mainEntity.getCode());
                    updateStatus.setSoId(mainEntity.getId());
                    if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                        //只有已发货才更新
                        updateStatus.setBillStatus(billStatus);

                        //防止同一个单多次来取重复记录日志，只有一开始订单状态不是已发货才记录日志
                        if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(mainEntity.getBillStatus())) {
                            updateStatus.setAddOperationLog(true);
                        }
                    }
                    updateStatus.setTrackNo(dto.getTrackNo());
                    soB2cFeign.updateSoB2cStatusByParams(updateStatus);

                    map.put(mainEntity, thirdWarehouseDeliveryEntity);
                }
            } else {
                SoB2cEntity mainEntity = soB2cFeign.getSoCode(referenceNo);
                if (null == mainEntity) {
                    if (CharSequenceUtil.isBlank(referenceNo)) {
                        return ApiResult.success();
                    }
                    // 非ERP单号前缀
                    if (!referenceNo.startsWith(BusinessNoConstant.XSDS) && !referenceNo.startsWith(BusinessNoConstant.XSDD)) {
                        return ApiResult.success();
                    }
                    log.error("第三方出库单: 未找到B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                    return ApiResult.success();
                }
                if (Objects.equals(mainEntity.getInvalidStatus(), true)) {
                    log.error("第三方出库单: B2C销售订单已作废 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                    return ApiResult.success();
                }
                ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getByCodeAndSoId(mainEntity.getShippingOrderNo(), mainEntity.getId());
                if (Objects.nonNull(thirdWarehouseDeliveryEntity) && thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus())) {
                    return ApiResult.success();
                }

                SoB2cDTO.UpdateStatusDTO updateStatus = new SoB2cDTO.UpdateStatusDTO();
                updateStatus.setSoCode(mainEntity.getCode());
                updateStatus.setSoId(mainEntity.getId());
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                    //只有已发货才更新
                    updateStatus.setBillStatus(billStatus);
                    //防止同一个单多次来取重复记录日志，只有一开始订单状态不是已发货才记录日志
                    if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(mainEntity.getBillStatus())) {
                        updateStatus.setAddOperationLog(true);
                    }
                }
                updateStatus.setTrackNo(dto.getTrackNo());
                soB2cFeign.updateSoB2cStatusByParams(updateStatus);

                map.put(mainEntity, thirdWarehouseDeliveryEntity);
            }

            //校验map不为null并且不为空
            if (Objects.isNull(map) || map.isEmpty()) {
                log.error("第三方出库单: 未找到B2C销售订单或三方仓发货单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
                return ApiResult.success();
            }

            for (Map.Entry<SoB2cEntity, ThirdWarehouseDeliveryEntity> entry : map.entrySet()) {
                SoB2cEntity mainEntity = entry.getKey();
                ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = entry.getValue();


                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                    //通邮仓跟踪号取订单跟踪号
                    if (CharSequenceUtil.equals(PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode(), dto.getPlatform())) {
                        List<SoB2cLogisticsEntity> list = FeignQuery.create(SoB2cLogisticsEntity.class).eq(SoB2cLogisticsEntity::getMainId, mainEntity.getId()).list();
                        if (CollUtil.isNotEmpty(list)) {
                            String trackNo = list.get(0).getTrackNo();
                            dto.setTrackNo(CharSequenceUtil.isBlank(trackNo) ? dto.getTrackNo() : trackNo);
                        }
                    }

                    if (isSignShipped) {
                        // 明细的存在没有标发的情况触发
                        List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(mainEntity.getId()));
                        if (detailList.stream().anyMatch(v -> !v.getIsSignShipped())) {
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
                    }
                    //清除三方仓异常
                    if (SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode().equals(mainEntity.getSignOrderError())) {
                        String type = SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode();
                        SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
                        deleteDTO.setMainId(mainEntity.getId());
                        deleteDTO.setType(type);
                        soB2cFeign.deleteError(deleteDTO);
                    }
                    //拦截中清除拦截状态
                    if (mainEntity.getIsIntercept()) {
                        mainEntity.setBillStatus(billStatus);
                        mainEntity.setIsIntercept(false);
                        mainEntity.setIsFrozen(false);
                        soB2cFeign.updateStatus(mainEntity);
                    }

                    platformOutboundConsumerService.generateSoOut(mainEntity, thirdWarehouseDeliveryEntity, dto, "");
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
                    asyncService.asyncCancelThirdWarehouseOrder(mainEntity, dto.getAbnormalProblemReason());
                }
                if (SoB2cBillStatusEnum.ENUM_DISUSE.getCode().equals(dto.getOrderStatus())) {
                    if (mainEntity.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode())) {
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
                        if (mainEntity.getIsCancel()) {
                            mainEntity.setInvalidStatus(Boolean.TRUE);
                            mainEntity.setInvalidRemark("平台订单取消,拦截成功自动作废");
                        }
                        soB2cFeign.updateStatus(mainEntity);
                        operateLogDTO.setContent("三方仓出库单废弃");
                        soB2cFeign.addModuleOperateLog(operateLogDTO);
                        if (Objects.nonNull(thirdWarehouseDeliveryEntity)) {
                            thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
                            operateLogService.addModuleOperateLog("状态变更为取消发货", ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(), thirdWarehouseDeliveryEntity.getId(), "状态变更");

                            thirdWarehouseDeliveryService.updateById(thirdWarehouseDeliveryEntity);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断", e);
            return ApiResult.error("系统异常");
        } finally {
            // 确保只有当前线程持有的锁才释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
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
            listBySwOrderNumber = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getPlatformCode, swOrderNumber)
                    .eq(SoB2cEntity::getInvalidStatus,false)
                    .eq(SoB2cEntity::getIsDeleted,false)
                    .list();
        }
        if(StringUtils.isNotBlank(referenceNo)){
            // 查询已有订单
            listByReferenceNo = FeignQuery.create(SoB2cEntity.class).eq (SoB2cEntity::getPlatformCode, referenceNo)
                    .eq(SoB2cEntity::getInvalidStatus,false)
                    .eq(SoB2cEntity::getIsDeleted,false)
                    .list();
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

        List<SoB2cReceiverEntity> soB2cReceiverEntities = FeignQuery.create(SoB2cReceiverEntity.class).in(SoB2cReceiverEntity::getMainId, soB2cIds).list();
        Map<String, SoB2cReceiverEntity> soB2cReceiverMap = soB2cReceiverEntities.stream().collect(Collectors.toMap(SoB2cReceiverEntity::getMainId, Function.identity(), (o1, o2) -> o1));

        //判断安兔/速派通订单查询接口返回的仓库代码是否已绑定数大臣仓库代码
        Boolean platformWarehouseCodeNotExist = false;
        Boolean overseasProviderNotExist = false;
        OverseasProviderDTO.FeignDTO overseasWarehouse = null;
        WarehouseEntity warehouseEntity = null;
        String platformWarehouseCode = dto.getWarehouseCode();
        if(StringUtils.isBlank(platformWarehouseCode)){
            platformWarehouseCodeNotExist = true;
            log.error("三方仓自动出库: 三方仓代码warehouseCode为空 >>>>>>>{}",JSONUtil.toJsonStr(dto));
        }else {
            OverseasProviderDTO.FeignDTO feignDTO = new OverseasProviderDTO.FeignDTO();
            feignDTO.setCode(dto.getPlatform());
            feignDTO.setPlatformWarehouseCode(dto.getWarehouseCode());
            overseasWarehouse = overseasProviderService.getOverseasWarehouse(feignDTO);
            if(Objects.isNull(overseasWarehouse) || StringUtils.isBlank(overseasWarehouse.getWarehouseId())){
                overseasProviderNotExist = true;
            }else{
                warehouseEntity = warehouseService.getById(overseasWarehouse.getWarehouseId());
            }
        }

        ThirdWarehouseSkuValidationContext skuValidationContext = null;
        if(!platformWarehouseCodeNotExist && !overseasProviderNotExist) {
            skuValidationContext = loadThirdWarehouseSkuValidationContext(dto, overseasWarehouse);
        }

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
            List<SoB2cDetailEntity> detailList = FeignQuery.create(SoB2cDetailEntity.class)
                    .eq(SoB2cDetailEntity::getMainId, mainEntity.getId())
                    .list();
            if(CollUtil.isEmpty(detailList)){
                log.error("三方仓自动出库: 未找到B2C销售订单明细 >>>>>>>{}",JSONUtil.toJsonStr(dto));
                continue;
            }

            //1.校验B2C销售订单数是否已经审核通过
            ApproveStatusEnum approveStatus = mainEntity.getApproveStatus();
            if(!Objects.equals(approveStatus,ApproveStatusEnum.APPROVE)){
                addRetryPlatformOutboundError(mainEntity.getId(), dto, "自动生成销售出库单失败：订单未审核或审核不通过");
                continue;
            }

            //3.判断安兔/速派通订单查询接口返回的仓库代码是否已绑定数大臣仓库代码
            if(platformWarehouseCodeNotExist){
                addRetryPlatformOutboundError(mainEntity.getId(), dto, "自动生成销售出库单失败：三方仓代码warehouseCode为空");
                continue;
            }else if(overseasProviderNotExist || Objects.isNull(overseasWarehouse)){
                addRetryPlatformOutboundError(mainEntity.getId(), dto, "自动生成销售出库单失败：三方仓库未映射");
                continue;
            } else if (Objects.nonNull(skuValidationContext) && !skuValidationContext.getSuccess()) {
                addRetryPlatformOutboundError(mainEntity.getId(), dto, skuValidationContext.getErrorMsg());
                continue;
            }

            if (Objects.nonNull(skuValidationContext)) {
                ThirdWarehouseSkuCheckResult orderSkuCheckResult =
                        validateThirdWarehouseSkuMatch(detailList, skuValidationContext, dto);
                if (!orderSkuCheckResult.getSuccess()) {
                    addRetryPlatformOutboundError(mainEntity.getId(), dto, orderSkuCheckResult.getErrorMsg());
                    continue;
                }
            }

            //虚拟仓库查询
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverMap.getOrDefault(mainEntity.getId(),null);
            VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
            platformDTO.setDictPlatform(mainEntity.getDictPlatform());
            platformDTO.setRelationId(mainEntity.getShopId());
            platformDTO.setWarehouseIdList(Collections.singletonList(overseasWarehouse.getWarehouseId()));
            platformDTO.setPartitionId(Objects.nonNull(soB2cReceiverEntity) ? soB2cReceiverEntity.getPartitionId() : "");
            List<VirtualWarehouseRelationEntity> virtualWarehouseList = virtualWarehouseChannelService.getVirtualWarehouse(platformDTO);
            String virtualWarehouseId ="";
            if(CollectionUtils.isNotEmpty(virtualWarehouseList)){
                virtualWarehouseId = virtualWarehouseList.get(0).getVirtualWarehouseId();
            }

            //4.更新B2C销售订单和明细
            SoB2cDTO.B2cByPlatformOutboundDTO updateDto = new SoB2cDTO.B2cByPlatformOutboundDTO();
            LocalDateTime outBoundTime = dto.getOutBoundTime();
            if (Objects.nonNull(outBoundTime)) {
                updateDto.setSoOutstockDate(outBoundTime.toLocalDate());
            }
            updateDto.setWarehouseId(overseasWarehouse.getWarehouseId());
            updateDto.setWarehouseName(overseasWarehouse.getWarehouseName());
            if(Objects.nonNull(warehouseEntity)){
                updateDto.setWarehouseOrgId(warehouseEntity.getOrgId());
                updateDto.setWarehouseOrgName(warehouseEntity.getName());
            }
            updateDto.setIsMatchWarehouseRule(true);
            updateDto.setVirtualWarehouseId(virtualWarehouseId);
            updateDto.setTrackNo(dto.getTrackNo());
            updateDto.setSoB2cId(mainEntity.getId());
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                //只有已发货才更新
                updateDto.setBillStatus(dto.getOrderStatus());

                //防止同一个单多次来取重复记录日志，只有一开始订单状态不是已发货才记录日志
                if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(mainEntity.getBillStatus())){
                    updateDto.setAddOperationLog(true);
                }
            }
            soB2cFeign.updateB2cByPlatformOutbound(updateDto);

            //5.“三方仓发货单”
            ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = generateThirdWarehouseDelivery(detailList, dto, mainEntity, platformCode,overseasWarehouse.getWarehouseId());
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

    private void addRetryPlatformOutboundError(String mainId, PlatformOutboundDTO dto, String msg) {
        SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                mainId,
                SoB2cErrorTypeEnum.RETRY_PLATFORM_OUTBOUND.getCode(),
                JSONUtil.toJsonStr(dto),
                msg,
                JSONUtil.toJsonStr(dto),
                ""
        );
        soB2cFeign.addSoB2cError(addError);
    }

    private ThirdWarehouseSkuValidationContext loadThirdWarehouseSkuValidationContext(PlatformOutboundDTO dto,
                                                                                      OverseasProviderDTO.FeignDTO overseasWarehouse) {
        if (StringUtils.isBlank(dto.getReferenceNo())) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：三方仓参考号为空，无法校验第三方仓SKU映射");
        }
        if (Objects.isNull(overseasWarehouse) || StringUtils.isBlank(overseasWarehouse.getWarehouseId())) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：三方仓库未映射");
        }
        ThirdWarehouseSkuPayload payload = loadThirdWarehouseSkuPayload(dto, overseasWarehouse);
        if (!payload.isSuccess()) {
            return ThirdWarehouseSkuValidationContext.fail(payload.getErrorMsg());
        }
        if (CollUtil.isEmpty(payload.getProductSkuList())) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：未获取到第三方仓SKU");
        }
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setAuthId(payload.getAuthId());
        paramDTO.setPlatform(dto.getPlatform());
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        paramDTO.setIsExpire(false);
        paramDTO.setPlatformSkuNoList(new ArrayList<>(payload.getProductSkuList()));
        List<ListingInfoWithSkuMappingDTO> mappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
        if (CollUtil.isEmpty(mappingList)) {
            mappingList = Collections.emptyList();
        }
        List<ListingInfoWithSkuMappingDTO> effectiveMappingList = mappingList.stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getHasMappingAll())
                        || StrUtil.equals(item.getWarehouseId(), payload.getWarehouseId()))
                .collect(Collectors.toList());
        Set<String> mappedSkuSet = effectiveMappingList.stream()
                .map(ListingInfoWithSkuMappingDTO::getPlatformSkuNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<String> missingSkuList = payload.getProductSkuList().stream()
                .filter(StringUtils::isNotBlank)
                .filter(sku -> !mappedSkuSet.contains(sku))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(missingSkuList)) {
            return ThirdWarehouseSkuValidationContext.success(payload, effectiveMappingList);
        }
        String providerWarehouseName = StrUtil.blankToDefault(payload.getPlatformWarehouseName(), dto.getWarehouseCode());
        return ThirdWarehouseSkuValidationContext.fail(StrUtil.format(
                "自动生成销售出库单失败：【{}】第三方仓SKU未映射SKU，第三方仓SKU：【{}】",
                providerWarehouseName,
                String.join("、", missingSkuList)
        ));
    }

    private ThirdWarehouseSkuCheckResult validateThirdWarehouseSkuMatch(List<SoB2cDetailEntity> detailList,
                                                                        ThirdWarehouseSkuValidationContext validationContext,
                                                                        PlatformOutboundDTO dto) {
        if (CollUtil.isEmpty(detailList) || Objects.isNull(validationContext) || !validationContext.getSuccess()) {
            return ThirdWarehouseSkuCheckResult.success();
        }
        Set<String> orderSkuNoSet = detailList.stream()
                .map(SoB2cDetailEntity::getSkuNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> orderSkuIdSet = detailList.stream()
                .map(SoB2cDetailEntity::getSkuId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<String> uncoveredOrderSkuList = detailList.stream()
                .filter(Objects::nonNull)
                .filter(detail -> validationContext.getMappingList().stream()
                        .noneMatch(item -> (StringUtils.isNotBlank(detail.getSkuNo())
                                && StringUtils.isNotBlank(item.getProductSkuNo())
                                && StrUtil.equals(detail.getSkuNo(), item.getProductSkuNo()))
                                || (StringUtils.isNotBlank(detail.getSkuId())
                                && StringUtils.isNotBlank(item.getProductSkuId())
                                && StrUtil.equals(detail.getSkuId(), item.getProductSkuId()))))
                .map(detail -> StringUtils.isNotBlank(detail.getSkuNo()) ? detail.getSkuNo() : detail.getSkuId())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(uncoveredOrderSkuList)) {
            String providerWarehouseName = StrUtil.blankToDefault(validationContext.getPayload().getPlatformWarehouseName(), dto.getWarehouseCode());
            return ThirdWarehouseSkuCheckResult.fail(StrUtil.format(
                    "自动生成销售出库单失败：【{}】第三方仓SKU未覆盖订单SKU，订单SKU：【{}】，第三方仓SKU：【{}】",
                    providerWarehouseName,
                    String.join("、", uncoveredOrderSkuList),
                    String.join("、", validationContext.getPayload().getProductSkuList())
            ));
        }
        List<String> mismatchSkuList = validationContext.getPayload().getProductSkuList().stream()
                .filter(StringUtils::isNotBlank)
                .filter(platformSku -> validationContext.getMappingList().stream()
                        .filter(item -> StrUtil.equals(platformSku, item.getPlatformSkuNo()))
                        .noneMatch(item -> (StringUtils.isNotBlank(item.getProductSkuNo()) && orderSkuNoSet.contains(item.getProductSkuNo()))
                                || (StringUtils.isNotBlank(item.getProductSkuId()) && orderSkuIdSet.contains(item.getProductSkuId()))))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mismatchSkuList)) {
            return ThirdWarehouseSkuCheckResult.success();
        }
        String providerWarehouseName = StrUtil.blankToDefault(validationContext.getPayload().getPlatformWarehouseName(), dto.getWarehouseCode());
        return ThirdWarehouseSkuCheckResult.fail(StrUtil.format(
                "自动生成销售出库单失败：【{}】第三方仓SKU映射系统SKU与订单SKU不一致，第三方仓SKU：【{}】，订单SKU：【{}】",
                providerWarehouseName,
                String.join("、", mismatchSkuList),
                String.join("、", orderSkuNoSet)
        ));
    }

    private ThirdWarehouseSkuPayload loadThirdWarehouseSkuPayload(PlatformOutboundDTO dto,
                                                                  OverseasProviderDTO.FeignDTO overseasWarehouse) {
        List<OverseasProviderWarehouseEntity> providerWarehouseList = resolveProviderWarehouseList(dto, overseasWarehouse);
        if (CollUtil.isEmpty(providerWarehouseList)) {
            return ThirdWarehouseSkuPayload.fail("自动生成销售出库单失败：三方仓库未映射");
        }
        List<OverseasProviderEntity> providerEntityList = FeignQuery.list(FeignQuery.create(OverseasProviderEntity.class)
                .in(OverseasProviderEntity::getId, providerWarehouseList.stream()
                        .map(OverseasProviderWarehouseEntity::getMainId)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList()))
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, dto.getPlatform())
                .eq(OverseasProviderEntity::getIsDeleted, false)
        );
        Map<String, OverseasProviderEntity> providerMap = providerEntityList.stream()
                .collect(Collectors.toMap(OverseasProviderEntity::getId, Function.identity(), (o1, o2) -> o1));
        for (OverseasProviderWarehouseEntity providerWarehouse : providerWarehouseList) {
            OverseasProviderEntity providerEntity = providerMap.get(providerWarehouse.getMainId());
            if (Objects.isNull(providerEntity) || Objects.isNull(providerEntity.getAuthJson())) {
                continue;
            }
            List<String> productSkuList = queryThirdWarehouseSkuList(dto, providerEntity);
            if (CollUtil.isNotEmpty(productSkuList)) {
                return ThirdWarehouseSkuPayload.success(providerEntity.getId(),
                        providerWarehouse.getWarehouseId(),
                        providerWarehouse.getPlatformWarehouseName(),
                        productSkuList);
            }
        }
        return ThirdWarehouseSkuPayload.fail("自动生成销售出库单失败：未获取到第三方仓SKU");
    }

    private List<OverseasProviderWarehouseEntity> resolveProviderWarehouseList(PlatformOutboundDTO dto,
                                                                               OverseasProviderDTO.FeignDTO overseasWarehouse) {
        List<OverseasProviderWarehouseEntity> providerWarehouseList = new ArrayList<>();
        if (Objects.nonNull(overseasWarehouse) && StringUtils.isNotBlank(overseasWarehouse.getWarehouseId())) {
            providerWarehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                    .eq(OverseasProviderWarehouseEntity::getWarehouseId, overseasWarehouse.getWarehouseId())
                    .eq(OverseasProviderWarehouseEntity::getDisabled, false)
                    .eq(OverseasProviderWarehouseEntity::getIsDeleted, false)
                    .list();
            providerWarehouseList = providerWarehouseList.stream()
                    .filter(item -> StringUtils.isBlank(dto.getWarehouseCode())
                            || StrUtil.equalsIgnoreCase(dto.getWarehouseCode(), item.getPlatformWarehouseCode()))
                    .collect(Collectors.toList());
        }
        if (CollUtil.isNotEmpty(providerWarehouseList)) {
            return providerWarehouseList;
        }
        List<OverseasProviderEntity> providerList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getCode, dto.getPlatform())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(providerList) || StringUtils.isBlank(dto.getWarehouseCode())) {
            return Collections.emptyList();
        }
        List<String> providerIds = providerList.stream().map(OverseasProviderEntity::getId).collect(Collectors.toList());
        return FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .in(OverseasProviderWarehouseEntity::getMainId, providerIds)
                .eq(OverseasProviderWarehouseEntity::getPlatformWarehouseCode, dto.getWarehouseCode())
                .eq(OverseasProviderWarehouseEntity::getDisabled, false)
                .eq(OverseasProviderWarehouseEntity::getIsDeleted, false)
                .list();
    }

    private List<String> queryThirdWarehouseSkuList(PlatformOutboundDTO dto, OverseasProviderEntity providerEntity) {
        OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(dto.getPlatform());
        if (Objects.isNull(platformEnum)) {
            return Collections.emptyList();
        }
        try {
            ThirdWarehouseContext.setAuthMap(providerEntity.getAuthJson());
            ThirdWarehouseContext.setAuthId(providerEntity.getId());
            AntuGetOutboundRefReq request = AntuGetOutboundRefReq.builder()
                    .referenceNo(dto.getReferenceNo())
                    .build();
            AntuResponse<AntuOutboundResp> response = antuService.getOrderByRefCode(request, platformEnum);
            if (Objects.isNull(response) || Objects.isNull(response.getData())) {
                return Collections.emptyList();
            }
            return parseThirdWarehouseSkuList(ThirdWarehouseContext.getResponseJson());
        } catch (Exception e) {
            log.warn("三方仓自动出库: 查询第三方仓订单明细失败, authId={}, referenceNo={}, platform={}",
                    providerEntity.getId(), dto.getReferenceNo(), dto.getPlatform(), e);
            return Collections.emptyList();
        } finally {
            ThirdWarehouseContext.remove();
        }
    }

    private List<String> parseThirdWarehouseSkuList(String responseJson) {
        if (StringUtils.isBlank(responseJson)) {
            return Collections.emptyList();
        }
        JSONObject root = JSON.parseObject(responseJson);
        if (Objects.isNull(root)) {
            return Collections.emptyList();
        }
        Object dataObject = root.get("data");
        JSONObject data = dataObject instanceof JSONObject ? (JSONObject) dataObject : JSON.parseObject(JSON.toJSONString(dataObject));
        JSONArray itemArray = Objects.nonNull(data) ? data.getJSONArray("items") : null;
        if (Objects.isNull(itemArray)) {
            itemArray = root.getJSONArray("items");
        }
        if (Objects.isNull(itemArray)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> skuSet = new LinkedHashSet<>();
        for (int i = 0; i < itemArray.size(); i++) {
            JSONObject item = itemArray.getJSONObject(i);
            if (Objects.isNull(item)) {
                continue;
            }
            String productSku = firstNotBlank(item.getString("productSku"),
                    item.getString("product_sku"),
                    item.getString("skuNo"),
                    item.getString("sku_no"));
            if (StringUtils.isNotBlank(productSku)) {
                skuSet.add(productSku);
            }
        }
        return new ArrayList<>(skuSet);
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static class ThirdWarehouseSkuCheckResult {
        private final boolean success;
        private final String errorMsg;

        private ThirdWarehouseSkuCheckResult(boolean success, String errorMsg) {
            this.success = success;
            this.errorMsg = errorMsg;
        }

        public static ThirdWarehouseSkuCheckResult success() {
            return new ThirdWarehouseSkuCheckResult(true, null);
        }

        public static ThirdWarehouseSkuCheckResult fail(String errorMsg) {
            return new ThirdWarehouseSkuCheckResult(false, errorMsg);
        }

        public boolean getSuccess() {
            return success;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    private static class ThirdWarehouseSkuPayload {
        private final boolean success;
        private final String errorMsg;
        private final String authId;
        private final String warehouseId;
        private final String platformWarehouseName;
        private final List<String> productSkuList;

        private ThirdWarehouseSkuPayload(boolean success, String errorMsg, String authId, String warehouseId,
                                         String platformWarehouseName, List<String> productSkuList) {
            this.success = success;
            this.errorMsg = errorMsg;
            this.authId = authId;
            this.warehouseId = warehouseId;
            this.platformWarehouseName = platformWarehouseName;
            this.productSkuList = productSkuList;
        }

        public static ThirdWarehouseSkuPayload success(String authId, String warehouseId, String platformWarehouseName,
                                                       List<String> productSkuList) {
            return new ThirdWarehouseSkuPayload(true, null, authId, warehouseId, platformWarehouseName, productSkuList);
        }

        public static ThirdWarehouseSkuPayload fail(String errorMsg) {
            return new ThirdWarehouseSkuPayload(false, errorMsg, null, null, null, Collections.emptyList());
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public String getAuthId() {
            return authId;
        }

        public String getWarehouseId() {
            return warehouseId;
        }

        public String getPlatformWarehouseName() {
            return platformWarehouseName;
        }

        public List<String> getProductSkuList() {
            return productSkuList;
        }
    }

    private static class ThirdWarehouseSkuValidationContext {
        private final boolean success;
        private final String errorMsg;
        private final ThirdWarehouseSkuPayload payload;
        private final List<ListingInfoWithSkuMappingDTO> mappingList;

        private ThirdWarehouseSkuValidationContext(boolean success, String errorMsg,
                                                   ThirdWarehouseSkuPayload payload,
                                                   List<ListingInfoWithSkuMappingDTO> mappingList) {
            this.success = success;
            this.errorMsg = errorMsg;
            this.payload = payload;
            this.mappingList = mappingList;
        }

        public static ThirdWarehouseSkuValidationContext success(ThirdWarehouseSkuPayload payload,
                                                                 List<ListingInfoWithSkuMappingDTO> mappingList) {
            return new ThirdWarehouseSkuValidationContext(true, null, payload, mappingList);
        }

        public static ThirdWarehouseSkuValidationContext fail(String errorMsg) {
            return new ThirdWarehouseSkuValidationContext(false, errorMsg, null, Collections.emptyList());
        }

        public boolean getSuccess() {
            return success;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public ThirdWarehouseSkuPayload getPayload() {
            return payload;
        }

        public List<ListingInfoWithSkuMappingDTO> getMappingList() {
            return mappingList;
        }
    }


    //生成三方仓发货单
    private ThirdWarehouseDeliveryEntity generateThirdWarehouseDelivery(List<SoB2cDetailEntity> detailList, PlatformOutboundDTO dto, SoB2cEntity mainEntity,String platformCode,String warehouseId) {
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
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(warehouseId);
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
                    SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(v->v.getId().equals(thirdWarehouseDeliveryDetailEntity.getSoDetailId())).findFirst().orElse(new SoB2cDetailEntity());
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

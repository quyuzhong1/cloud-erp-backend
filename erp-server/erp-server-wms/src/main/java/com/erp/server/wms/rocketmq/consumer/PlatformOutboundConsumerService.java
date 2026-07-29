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
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
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
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;

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
    private B2bThirdDeliveryService b2bThirdDeliveryService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

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
            // 中宝(ZHONG_BAO)不走本分支：出库明细已由 DMP MQ 的 items 携带，不再依赖运行时 Antu 查单
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
                    // WEGO 物流跟踪号仅针对线下下单同步，线上订单跟踪号由销售平台管理；
                    // transactionSubType 为空时视为线下订单（WEGO 手工建单场景），兜底同步跟踪号
                    if (!OmsPlatformEnum.WE_GO.getCode().equals(dto.getPlatform())
                            || CharSequenceUtil.isBlank(mainEntity.getTransactionSubType())
                            || OrderSubTypeEnum.OFFLINE_ORDER.getCode().equals(mainEntity.getTransactionSubType())) {
                        updateStatus.setTrackNo(dto.getTrackNo());
                        // WFHD 已建单回传：渠道未推送海外仓面单且跟踪号不一致时强制覆盖（与 checkAndBuildMap 一致）
                        updateStatus.setForceUpdateLogisticsTrack(
                                shouldForceUpdateLogisticsTrackForOrder(mainEntity.getId(), dto.getTrackNo()));
                    }
                    soB2cFeign.updateSoB2cStatusByParams(updateStatus);
                    //更新物流单跟踪号
                    if (CharSequenceUtil.isNotBlank(dto.getTrackNo()) && !dto.getTrackNo().equals(thirdWarehouseDeliveryEntity.getTrackNo())){
                        thirdWarehouseDeliveryEntity.setTrackNo(dto.getTrackNo());
                        thirdWarehouseDeliveryService.updateById(thirdWarehouseDeliveryEntity);
                    }
                    map.put(mainEntity, thirdWarehouseDeliveryEntity);
                }
            }else if (referenceNo.contains(BusinessNoConstant.SFFH)){
                //B2B三方发货单
                b2bThirdDeliveryService.syncOutboundStatus(dto);
                return ApiResult.success();
            }else {
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
                // 销售订单号回传：渠道未推送海外仓面单且跟踪号不一致时强制覆盖
                updateStatus.setForceUpdateLogisticsTrack(
                        shouldForceUpdateLogisticsTrackForOrder(mainEntity.getId(), dto.getTrackNo()));
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

                    // WEGO：拦截中 → 确认拦截失败。
                    // 前置条件：本段位于外层 ENUM_SHIPPED（dto.orderStatus=shipped）内，
                    // 仅当 DMP 回传已发货/已签收（WEGO 10/11 均映射为 shipped）时才会进入；
                    // 提交失败/出库异常等 exception 态走下方 ENUM_EXCEPTION 分支，不会命中此处。
                    // 内层仅判断「是否处于拦截中」（销售单 isIntercept 或三方发货单 INTERCEPTING），
                    // 与后方 isSignShipped（是否平台标发）无关，勿将标发开关误当作已发货条件。
                    if (OmsPlatformEnum.WE_GO.getCode().equals(dto.getPlatform())
                            && (Boolean.TRUE.equals(mainEntity.getIsIntercept())
                            || (Objects.nonNull(thirdWarehouseDeliveryEntity)
                            && SoB2cWarehouseDeliveryStatusEnum.INTERCEPTING.getStatus()
                            .equals(thirdWarehouseDeliveryEntity.getStatus())))) {
                        operateLogService.addModuleOperateLog(
                                "三方仓拦截失败，出库单已发货，异常信息："
                                        + CharSequenceUtil.blankToDefault(dto.getAbnormalProblemReason(), ""),
                                ModuleTypeEnum.SO_B2C.getCode(), mainEntity.getId(), "拦截失败");
                        mainEntity.setIsIntercept(false);
                        mainEntity.setIsFrozen(false);
                        soB2cFeign.updateStatus(mainEntity);
                        confirmWegoInterceptBills(mainEntity.getId(), false, "出库单已发货，拦截失败");
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

                    platformOutboundConsumerService.generateSoOut(mainEntity, thirdWarehouseDeliveryEntity, dto, "", "");
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
                    // WEGO：提交失败(1)/出库异常(13) 统一走自动截单；
                    // Handler 内会按状态调用 2c.order.errorHandle，并按回查结果返回成功/拦截中/失败；
                    // 拦截中时由后续 DMP 出库轮询（已取消/已出库）确认终态。其他海外仓逻辑不变。
                    ThirdWarehouseDeliveryEntity wegoDeliveryEntity = OmsPlatformEnum.WE_GO.getCode().equals(dto.getPlatform())
                            ? thirdWarehouseDeliveryEntity : null;
                    asyncService.asyncCancelThirdWarehouseOrder(mainEntity, dto.getAbnormalProblemReason(), wegoDeliveryEntity);
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
                        // WEGO：异步截单轮询确认成功——关闭待处理拦截单
                        if (OmsPlatformEnum.WE_GO.getCode().equals(dto.getPlatform())) {
                            confirmWegoInterceptBills(mainEntity.getId(), true, "出库单已取消，拦截成功");
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
        // 安兔等三方仓自动出库仅匹配平台拉单（XSDD），排除手工单（XSDS）及全托管单（XSBH）
        mainEntityList = mainEntityList.stream()
                .filter(entity -> CharSequenceUtil.startWith(entity.getCode(), BusinessNoConstant.XSDD))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mainEntityList)) {
            log.error("三方仓自动出库: 未找到平台拉取的B2C销售订单 >>>>>>>{}", JSONUtil.toJsonStr(dto));
            return null;
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
        ThirdWarehouseLogisticsChannelValidationContext logisticsChannelContext =
                loadThirdWarehouseLogisticsChannelValidationContext(soB2cIds, dto);
        // 订单物流渠道 isPushLabel：未推送海外仓面单时，仓回传跟踪号不一致则覆盖订单物流单号/跟踪号
        Map<String, LogisticsChannelEntity> orderLogisticsChannelById =
                loadOrderLogisticsChannelById(logisticsChannelContext.getLogisticsByMainId());

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

            String logisticsChannelErrorMsg = validateThirdWarehouseLogisticsChannelMapping(
                    mainEntity.getId(), dto, logisticsChannelContext);
            if (StringUtils.isNotBlank(logisticsChannelErrorMsg)) {
                addRetryPlatformOutboundError(mainEntity.getId(), dto, logisticsChannelErrorMsg);
                continue;
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
            updateDto.setShippingMethod(dto.getShippingMethod());
            updateDto.setThirdWarehousePlatform(dto.getPlatform());
            updateDto.setPlatformWarehouseCode(dto.getWarehouseCode());
            LogisticsChannelEntity resolvedLogisticsChannel = logisticsChannelContext.getResolvedLogisticsChannel();
            if (Objects.nonNull(resolvedLogisticsChannel)) {
                updateDto.setResolvedLogisticsChannelId(resolvedLogisticsChannel.getId());
                updateDto.setResolvedLogisticsChannelName(resolvedLogisticsChannel.getName());
            }
            // 本消费者仅处理海外仓出库回传；渠道未配置推送海外仓面单且跟踪号不一致时，强制覆盖物流单号+跟踪号
            updateDto.setForceUpdateLogisticsTrack(shouldForceUpdateLogisticsTrack(
                    logisticsChannelContext.getLogisticsByMainId().get(mainEntity.getId()),
                    orderLogisticsChannelById,
                    resolvedLogisticsChannel,
                    dto.getTrackNo()));
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())) {
                //只有已发货才更新
                updateDto.setBillStatus(dto.getOrderStatus());

                //防止同一个单多次来取重复记录日志，只有一开始订单状态不是已发货才记录日志
                if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(mainEntity.getBillStatus())){
                    updateDto.setAddOperationLog(true);
                }
            }
            try {
                soB2cFeign.updateB2cByPlatformOutbound(updateDto);
            } catch (Exception e) {
                log.error("三方仓自动出库: 更新B2C销售订单失败, orderId={}, code={}", mainEntity.getId(), mainEntity.getCode(), e);
                addRetryPlatformOutboundError(mainEntity.getId(), dto,
                        "自动生成销售出库单失败：" + (e.getMessage() != null ? e.getMessage() : "更新订单异常"));
                continue;
            }

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

    private ThirdWarehouseLogisticsChannelValidationContext loadThirdWarehouseLogisticsChannelValidationContext(
            List<String> soB2cIds, PlatformOutboundDTO dto) {
        List<SoB2cLogisticsEntity> logisticsList = FeignQuery.create(SoB2cLogisticsEntity.class)
                .in(SoB2cLogisticsEntity::getMainId, soB2cIds)
                .list();
        Map<String, SoB2cLogisticsEntity> logisticsByMainId = new HashMap<>();
        if (CollUtil.isNotEmpty(logisticsList)) {
            for (SoB2cLogisticsEntity logisticsEntity : logisticsList) {
                logisticsByMainId.putIfAbsent(logisticsEntity.getMainId(), logisticsEntity);
            }
        }
        Boolean mappingValid = null;
        LogisticsChannelEntity resolvedChannel = resolveThirdWarehouseLogisticsChannel(dto);
        if (StringUtils.isNotBlank(dto.getShippingMethod())
                && StringUtils.isNotBlank(dto.getPlatform())
                && StringUtils.isNotBlank(dto.getWarehouseCode())) {
            mappingValid = Objects.nonNull(resolvedChannel);
        }
        return new ThirdWarehouseLogisticsChannelValidationContext(logisticsByMainId, mappingValid, resolvedChannel);
    }

    /**
     * 三方仓出库批次内仅解析一次 ERP 物流渠道，供校验与 OMS 回写透传。
     */
    private LogisticsChannelEntity resolveThirdWarehouseLogisticsChannel(PlatformOutboundDTO dto) {
        String shippingMethod = dto.getShippingMethod();
        String thirdWarehousePlatform = dto.getPlatform();
        String platformWarehouseCode = dto.getWarehouseCode();
        if (StringUtils.isBlank(shippingMethod)
                || StringUtils.isBlank(thirdWarehousePlatform)
                || StringUtils.isBlank(platformWarehouseCode)) {
            return null;
        }
        LogisticsChannelDTO.ThirdWarehouseLogisticsMappingDTO mappingQuery = new LogisticsChannelDTO.ThirdWarehouseLogisticsMappingDTO();
        mappingQuery.setLogisticsPlatform(thirdWarehousePlatform);
        mappingQuery.setShippingMethod(shippingMethod);
        mappingQuery.setPlatformWarehouseCode(platformWarehouseCode);
        return logisticsFeign.resolveThirdWarehouseLogisticsChannel(mappingQuery);
    }

    /**
     * 订单物流渠道为空时，校验三方仓 shipping_method 是否已映射 ERP 物流渠道。
     *
     * @return 校验失败时的异常文案；null 表示通过或无需校验
     */
    private String validateThirdWarehouseLogisticsChannelMapping(String mainId,
                                                                 PlatformOutboundDTO dto,
                                                                 ThirdWarehouseLogisticsChannelValidationContext context) {
        SoB2cLogisticsEntity logisticsEntity = context.getLogisticsByMainId().get(mainId);
        if (Objects.isNull(logisticsEntity) || StringUtils.isNotBlank(logisticsEntity.getLogisticsChannelId())) {
            return null;
        }
        Boolean mappingValid = context.getMappingValid();
        if (mappingValid == null) {
            return null;
        }
        if (Boolean.TRUE.equals(mappingValid)) {
            return null;
        }
        return buildLogisticsChannelNotMappedErrorMsg(dto);
    }

    private String buildLogisticsChannelNotMappedErrorMsg(PlatformOutboundDTO dto) {
        String providerCode = StrUtil.blankToDefault(dto.getProvider(), dto.getPlatform());
        String providerName = StrUtil.blankToDefault(OmsPlatformEnum.getName(providerCode), providerCode);
        return StrUtil.format("自动出库失败，【{}】物流渠道【{}】未映射", providerName, dto.getShippingMethod());
    }

    /**
     * 批量加载订单已绑定物流渠道（用于读取 isPushLabel）。
     * 一次 FeignQuery.in 拉取，避免按渠道 ID 循环远程调用。
     * 查询失败时返回空 Map，由 {@link #shouldForceUpdateLogisticsTrack} 对已绑定渠道订单禁止强制覆盖。
     */
    private Map<String, LogisticsChannelEntity> loadOrderLogisticsChannelById(
            Map<String, SoB2cLogisticsEntity> logisticsByMainId) {
        Map<String, LogisticsChannelEntity> channelById = new HashMap<>();
        if (CollUtil.isEmpty(logisticsByMainId)) {
            return channelById;
        }
        Set<String> channelIds = logisticsByMainId.values().stream()
                .filter(Objects::nonNull)
                .map(SoB2cLogisticsEntity::getLogisticsChannelId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(channelIds)) {
            return channelById;
        }
        try {
            List<LogisticsChannelEntity> channelList = FeignQuery.create(LogisticsChannelEntity.class)
                    .in(LogisticsChannelEntity::getId, new ArrayList<>(channelIds))
                    .list();
            if (CollUtil.isNotEmpty(channelList)) {
                for (LogisticsChannelEntity channel : channelList) {
                    if (Objects.nonNull(channel) && StringUtils.isNotBlank(channel.getId())) {
                        channelById.put(channel.getId(), channel);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("三方仓自动出库: 批量查询物流渠道失败, channelIds={}", channelIds, e);
        }
        return channelById;
    }

    /**
     * 渠道「是否推送海外仓面单」为否（含未配置）且仓回传跟踪号与订单不一致时，强制覆盖物流单号/跟踪号。
     * 配置为是则不做覆盖。本消费者单据均为海外仓出库回传。
     * 订单已绑定 logisticsChannelId 但渠道未加载成功时禁止覆盖，避免查询失败被当成未配置而误覆盖面单跟踪号。
     */
    private boolean shouldForceUpdateLogisticsTrack(SoB2cLogisticsEntity logisticsEntity,
                                                    Map<String, LogisticsChannelEntity> orderLogisticsChannelById,
                                                    LogisticsChannelEntity resolvedLogisticsChannel,
                                                    String warehouseTrackNo) {
        if (CharSequenceUtil.isBlank(warehouseTrackNo) || Objects.isNull(logisticsEntity)) {
            return false;
        }
        LogisticsChannelEntity channel;
        if (StringUtils.isNotBlank(logisticsEntity.getLogisticsChannelId())) {
            channel = orderLogisticsChannelById.get(logisticsEntity.getLogisticsChannelId());
            if (Objects.isNull(channel)) {
                // 已绑定渠道但查询失败/未返回：禁止强制覆盖（不回退 resolved，也不按未配置处理）
                log.warn("三方仓自动出库: 订单物流渠道未加载成功，跳过强制覆盖跟踪号, soMainId={}, logisticsChannelId={}",
                        logisticsEntity.getMainId(), logisticsEntity.getLogisticsChannelId());
                return false;
            }
        } else {
            channel = resolvedLogisticsChannel;
        }
        // 推送海外仓面单=是：跟踪号以 ERP 面单为准，不覆盖
        if (Objects.nonNull(channel) && Boolean.TRUE.equals(channel.getIsPushLabel())) {
            return false;
        }
        String orderTrack = CharSequenceUtil.blankToDefault(logisticsEntity.getTrackNo(), logisticsEntity.getCode());
        return !StrUtil.equals(warehouseTrackNo, orderTrack);
    }

    /**
     * WFHD / 销售订单号回传路径（单订单）：按订单已绑定物流渠道判断是否强制覆盖跟踪号。
     * 批量建单路径请使用循环外预加载的 logistics/channel 映射，勿循环调用本方法。
     */
    private boolean shouldForceUpdateLogisticsTrackForOrder(String soB2cId, String warehouseTrackNo) {
        if (CharSequenceUtil.isBlank(soB2cId) || CharSequenceUtil.isBlank(warehouseTrackNo)) {
            return false;
        }
        List<SoB2cLogisticsEntity> logisticsList = FeignQuery.create(SoB2cLogisticsEntity.class)
                .eq(SoB2cLogisticsEntity::getMainId, soB2cId)
                .list();
        if (CollUtil.isEmpty(logisticsList)) {
            return false;
        }
        SoB2cLogisticsEntity logisticsEntity = logisticsList.get(0);
        Map<String, SoB2cLogisticsEntity> logisticsByMainId = Collections.singletonMap(soB2cId, logisticsEntity);
        Map<String, LogisticsChannelEntity> channelById = loadOrderLogisticsChannelById(logisticsByMainId);
        return shouldForceUpdateLogisticsTrack(logisticsEntity, channelById, null, warehouseTrackNo);
    }

    private ThirdWarehouseSkuValidationContext loadThirdWarehouseSkuValidationContext(PlatformOutboundDTO dto,
                                                                                      OverseasProviderDTO.FeignDTO overseasWarehouse) {
        if (Objects.isNull(overseasWarehouse) || StringUtils.isBlank(overseasWarehouse.getWarehouseId())) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：三方仓库未映射");
        }
        ThirdWarehouseSkuPayload payload = buildThirdWarehouseSkuPayload(dto, overseasWarehouse);
        if (CollUtil.isEmpty(payload.getDetailList())) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：三方仓DTO出库明细为空");
        }
        // SKU 映射校验依赖 DTO items，不再要求 referenceNo（订单定位仍在 checkAndBuildMap 中校验 swOrderNumber/referenceNo）
        String authId = resolveProviderAuthId(dto, overseasWarehouse);
        if (StringUtils.isBlank(authId)) {
            return ThirdWarehouseSkuValidationContext.fail("自动生成销售出库单失败：三方仓库未映射");
        }
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setAuthId(authId);
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
        Map<String, Integer> missingSkuQtyMap = payload.getDetailList().stream()
                .collect(Collectors.toMap(ThirdWarehouseSkuDetail::getProductSku,
                        ThirdWarehouseSkuDetail::getQty,
                        (oldQty, newQty) -> oldQty + newQty));
        String providerWarehouseName = StrUtil.blankToDefault(payload.getPlatformWarehouseName(), dto.getWarehouseCode());
        return ThirdWarehouseSkuValidationContext.fail(StrUtil.format(
                "自动生成销售出库单失败：【{}】第三方仓SKU未映射SKU，第三方仓SKU：【{}】",
                providerWarehouseName,
                missingSkuList.stream()
                        .map(sku -> formatSkuWithQty(sku, missingSkuQtyMap.getOrDefault(sku, 0)))
                        .collect(Collectors.joining("、"))
        ));
    }

    private ThirdWarehouseSkuCheckResult validateThirdWarehouseSkuMatch(List<SoB2cDetailEntity> detailList,
                                                                        ThirdWarehouseSkuValidationContext validationContext,
                                                                        PlatformOutboundDTO dto) {
        if (CollUtil.isEmpty(detailList) || Objects.isNull(validationContext) || !validationContext.getSuccess()) {
            return ThirdWarehouseSkuCheckResult.success();
        }
        Map<String, Integer> dtoSkuQtyMap = validationContext.getPayload().getDetailList().stream()
                .collect(Collectors.toMap(ThirdWarehouseSkuDetail::getProductSku,
                        ThirdWarehouseSkuDetail::getQty,
                        (oldQty, newQty) -> oldQty + newQty));
        Map<String, List<ListingInfoWithSkuMappingDTO>> mappingMap = validationContext.getMappingList().stream()
                .filter(item -> StringUtils.isNotBlank(item.getPlatformSkuNo()))
                .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));
        Map<String, List<BomChildrenSkuDTO>> bomChildrenMap = loadCombinationBomChildrenMap(detailList);
        List<String> unmatchedOrderSkuList = new ArrayList<>();
        for (SoB2cDetailEntity detail : detailList) {
            if (!matchSingleOrderDetail(detail, dtoSkuQtyMap, mappingMap)
                    && !matchCombinationOrderDetail(detail, dtoSkuQtyMap, mappingMap, bomChildrenMap)) {
                String sku = StringUtils.isNotBlank(detail.getSkuNo()) ? detail.getSkuNo() : detail.getSkuId();
                unmatchedOrderSkuList.add(formatSkuWithQty(sku, Objects.nonNull(detail.getQty()) ? detail.getQty() : 0));
            }
        }
        if (CollUtil.isEmpty(unmatchedOrderSkuList) && dtoSkuQtyMap.values().stream().allMatch(qty -> Objects.equals(qty, 0))) {
            return ThirdWarehouseSkuCheckResult.success();
        }
        String providerWarehouseName = StrUtil.blankToDefault(validationContext.getPayload().getPlatformWarehouseName(), dto.getWarehouseCode());
        return ThirdWarehouseSkuCheckResult.fail(StrUtil.format(
                "自动生成销售出库单失败：【{}】第三方仓明细或者数量未完全匹配订单明细，未匹配订单SKU：【{}】，未匹配三方仓SKU：【{}】",
                providerWarehouseName,
                unmatchedOrderSkuList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining("、")),
                dtoSkuQtyMap.entrySet().stream()
                        .filter(entry -> !Objects.equals(entry.getValue(), 0))
                        .map(entry -> formatSkuWithQty(entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("、"))
        ));
    }

    private String formatSkuWithQty(String sku, Integer qty) {
        return StrUtil.format("{}×{}", sku, Objects.nonNull(qty) ? qty : 0);
    }

    private ThirdWarehouseSkuPayload buildThirdWarehouseSkuPayload(PlatformOutboundDTO dto,
                                                                   OverseasProviderDTO.FeignDTO overseasWarehouse) {
        List<ThirdWarehouseSkuDetail> detailList = Optional.ofNullable(dto.getItems()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getProductSku()))
                .filter(item -> {
                    if (Objects.nonNull(item.getActualQty())) {
                        return true;
                    }
                    log.warn("三方仓出库明细数量缺失, platform={}, referenceNo={}, productSku={}",
                            dto.getPlatform(), dto.getReferenceNo(), item.getProductSku());
                    return false;
                })
                .map(item -> new ThirdWarehouseSkuDetail(item.getProductSku(), item.getActualQty()))
                .collect(Collectors.toList());
        return ThirdWarehouseSkuPayload.success(overseasWarehouse.getWarehouseId(),
                overseasWarehouse.getPlatformWarehouseName(),
                detailList);
    }

    private String resolveProviderAuthId(PlatformOutboundDTO dto, OverseasProviderDTO.FeignDTO overseasWarehouse) {
        // providerWarehouseList 已按 dto.platform + warehouseCode 过滤，再按列表顺序取首个已授权 provider
        List<OverseasProviderWarehouseEntity> providerWarehouseList = resolveProviderWarehouseList(dto, overseasWarehouse);
        if (CollUtil.isEmpty(providerWarehouseList)) {
            return null;
        }
        List<String> mainIds = providerWarehouseList.stream()
                .map(OverseasProviderWarehouseEntity::getMainId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mainIds)) {
            return null;
        }
        List<OverseasProviderEntity> providerEntityList = FeignQuery.list(FeignQuery.create(OverseasProviderEntity.class)
                .in(OverseasProviderEntity::getId, mainIds)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, dto.getPlatform())
                .eq(OverseasProviderEntity::getIsDeleted, false));
        Map<String, OverseasProviderEntity> providerMap = providerEntityList.stream()
                .collect(Collectors.toMap(OverseasProviderEntity::getId, Function.identity(), (left, right) -> left));
        for (OverseasProviderWarehouseEntity providerWarehouse : providerWarehouseList) {
            OverseasProviderEntity provider = providerMap.get(providerWarehouse.getMainId());
            // provider 已按 dto.platform + ALREADY 授权过滤，与 DMP 出库平台一致
            if (Objects.nonNull(provider) && CollUtil.isNotEmpty(provider.getAuthJson())) {
                if (providerWarehouseList.size() > 1) {
                    log.warn("多Provider仓库映射，选用首个已授权Provider, platform={}, warehouseCode={}, authId={}",
                            dto.getPlatform(), dto.getWarehouseCode(), provider.getId());
                }
                return provider.getId();
            }
        }
        return null;
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

    private boolean matchSingleOrderDetail(SoB2cDetailEntity detail,
                                           Map<String, Integer> dtoSkuQtyMap,
                                           Map<String, List<ListingInfoWithSkuMappingDTO>> mappingMap) {
        String matchedPlatformSku = findMappedPlatformSku(detail.getSkuId(), detail.getSkuNo(), dtoSkuQtyMap, mappingMap);
        if (StringUtils.isBlank(matchedPlatformSku)) {
            return false;
        }
        Integer remainQty = dtoSkuQtyMap.getOrDefault(matchedPlatformSku, 0);
        Integer orderQty = Objects.nonNull(detail.getQty()) ? detail.getQty() : 0;
        if (remainQty < orderQty) {
            return false;
        }
        dtoSkuQtyMap.put(matchedPlatformSku, remainQty - orderQty);
        return true;
    }

    private boolean matchCombinationOrderDetail(SoB2cDetailEntity detail,
                                                Map<String, Integer> dtoSkuQtyMap,
                                                Map<String, List<ListingInfoWithSkuMappingDTO>> mappingMap,
                                                Map<String, List<BomChildrenSkuDTO>> bomChildrenMap) {
        List<BomChildrenSkuDTO> childList = bomChildrenMap.get(detail.getSkuId());
        if (CollUtil.isEmpty(childList)) {
            return false;
        }
        Map<String, Integer> matchedSkuQtyMap = new HashMap<>();
        for (BomChildrenSkuDTO childDetail : childList) {
            String matchedPlatformSku = findMappedPlatformSku(childDetail.getSkuId(), childDetail.getSkuNo(), dtoSkuQtyMap, mappingMap);
            if (StringUtils.isBlank(matchedPlatformSku)) {
                return false;
            }
            Integer orderQty = Objects.nonNull(detail.getQty()) ? detail.getQty() : 0;
            Integer bomQty = Objects.nonNull(childDetail.getQuantity()) ? childDetail.getQuantity() : 0;
            Integer childQty = orderQty * bomQty;
            matchedSkuQtyMap.merge(matchedPlatformSku, childQty, (oldQty, newQty) -> oldQty + newQty);
        }
        boolean qtyMatched = matchedSkuQtyMap.entrySet().stream()
                .allMatch(entry -> dtoSkuQtyMap.getOrDefault(entry.getKey(), 0) >= entry.getValue());
        if (!qtyMatched) {
            return false;
        }
        matchedSkuQtyMap.forEach((platformSku, qty) -> dtoSkuQtyMap.put(platformSku, dtoSkuQtyMap.getOrDefault(platformSku, 0) - qty));
        return true;
    }

    private Map<String, List<BomChildrenSkuDTO>> loadCombinationBomChildrenMap(List<SoB2cDetailEntity> detailList) {
        List<String> skuIdList = detailList.stream()
                .map(SoB2cDetailEntity::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)) {
            return Collections.emptyMap();
        }
        // PlmTaskFeign.listBomChildBySkuIds 直接返回 List（非 ApiResult）；Feign 失败由框架抛 FeignServiceException
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        if (CollUtil.isEmpty(bomChildrenList)) {
            log.warn("组合品BOM查询无数据, skuIds={}", skuIdList);
            return Collections.emptyMap();
        }
        Map<String, List<BomChildrenSkuDTO>> combinationMap = bomChildrenList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getParentSkuId()))
                .filter(item -> BomTypeEnum.COMBINATION.getType().equals(item.getType()))
                .collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        log.debug("组合品BOM加载完成, requestSkuCount={}, bomRowCount={}, combinationParentCount={}",
                skuIdList.size(), bomChildrenList.size(), combinationMap.size());
        return combinationMap;
    }

    private String findMappedPlatformSku(String skuId,
                                         String skuNo,
                                         Map<String, Integer> dtoSkuQtyMap,
                                         Map<String, List<ListingInfoWithSkuMappingDTO>> mappingMap) {
        List<String> candidates = listMappedPlatformSkuCandidates(skuId, skuNo, dtoSkuQtyMap, mappingMap);
        if (candidates.size() > 1) {
            log.warn("多候选平台SKU映射，按字典序选用首个, skuId={}, skuNo={}, candidates={}", skuId, skuNo, candidates);
        }
        return CollUtil.isEmpty(candidates) ? null : candidates.get(0);
    }

    private List<String> listMappedPlatformSkuCandidates(String skuId,
                                                         String skuNo,
                                                         Map<String, Integer> dtoSkuQtyMap,
                                                         Map<String, List<ListingInfoWithSkuMappingDTO>> mappingMap) {
        return dtoSkuQtyMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                // 多平台 SKU 均可映射时按字典序固定选取，避免 HashMap 遍历顺序不确定；一对多需业务映射约束
                .sorted()
                .filter(platformSku -> Optional.ofNullable(mappingMap.get(platformSku)).orElse(Collections.emptyList()).stream()
                        .anyMatch(mapping -> (StringUtils.isNotBlank(skuId)
                                && StringUtils.isNotBlank(mapping.getProductSkuId())
                                && StrUtil.equals(skuId, mapping.getProductSkuId()))
                                || (StringUtils.isNotBlank(skuNo)
                                && StringUtils.isNotBlank(mapping.getProductSkuNo())
                                && StrUtil.equals(skuNo, mapping.getProductSkuNo()))))
                .collect(Collectors.toList());
    }

    private static class ThirdWarehouseLogisticsChannelValidationContext {
        private final Map<String, SoB2cLogisticsEntity> logisticsByMainId;
        /** null 表示跳过映射校验 */
        private final Boolean mappingValid;
        /** 批次内单次 Feign 解析结果，透传 OMS 避免 N 次重复调用 */
        private final LogisticsChannelEntity resolvedLogisticsChannel;

        private ThirdWarehouseLogisticsChannelValidationContext(Map<String, SoB2cLogisticsEntity> logisticsByMainId,
                                                                Boolean mappingValid,
                                                                LogisticsChannelEntity resolvedLogisticsChannel) {
            this.logisticsByMainId = logisticsByMainId;
            this.mappingValid = mappingValid;
            this.resolvedLogisticsChannel = resolvedLogisticsChannel;
        }

        public Map<String, SoB2cLogisticsEntity> getLogisticsByMainId() {
            return logisticsByMainId;
        }

        public Boolean getMappingValid() {
            return mappingValid;
        }

        public LogisticsChannelEntity getResolvedLogisticsChannel() {
            return resolvedLogisticsChannel;
        }
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

    private static class ThirdWarehouseSkuDetail {
        private final String productSku;
        private final Integer qty;

        private ThirdWarehouseSkuDetail(String productSku, Integer qty) {
            this.productSku = productSku;
            this.qty = qty;
        }

        public String getProductSku() {
            return productSku;
        }

        public Integer getQty() {
            return qty;
        }
    }

    private static class ThirdWarehouseSkuPayload {
        private final String warehouseId;
        private final String platformWarehouseName;
        private final List<ThirdWarehouseSkuDetail> detailList;

        private ThirdWarehouseSkuPayload(String warehouseId,
                                         String platformWarehouseName,
                                         List<ThirdWarehouseSkuDetail> detailList) {
            this.warehouseId = warehouseId;
            this.platformWarehouseName = platformWarehouseName;
            this.detailList = detailList;
        }

        public static ThirdWarehouseSkuPayload success(String warehouseId, String platformWarehouseName,
                                                       List<ThirdWarehouseSkuDetail> detailList) {
            return new ThirdWarehouseSkuPayload(warehouseId, platformWarehouseName, detailList);
        }

        public String getWarehouseId() {
            return warehouseId;
        }

        public String getPlatformWarehouseName() {
            return platformWarehouseName;
        }

        public List<String> getProductSkuList() {
            return detailList.stream()
                    .map(ThirdWarehouseSkuDetail::getProductSku)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        public List<ThirdWarehouseSkuDetail> getDetailList() {
            return detailList;
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
            thirdWarehouseDeliveryDetailEntity.setVirtualWarehouseId("");
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
        thirdWarehouseDeliveryEntity.setShopId(mainEntity.getShopId());
        thirdWarehouseDeliveryEntity.setDictPlatform(mainEntity.getDictPlatform());
        thirdWarehouseDeliveryEntity.setPlatformCode(platformCode);
        thirdWarehouseDeliveryEntity.setThirdWarehousePlatform(dto.getPlatform());
        thirdWarehouseDeliveryEntity.setShippingMethod(dto.getShippingMethod());
        thirdWarehouseDeliveryEntity.setTrackNo(dto.getTrackNo());
        thirdWarehouseDeliveryEntity.setStatus(dto.getOrderStatus());
        thirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDeliveryDetailEntities);
        return thirdWarehouseDeliveryService.add(thirdWarehouseDeliveryEntity,true);
    }

    @DistributeLocker(keyName = "dto.referenceNo")
    public void generateSoOut(SoB2cEntity mainEntity, ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity, PlatformOutboundDTO dto,String warehouseId,String virtualWarehouseId) {
        // 校验是否已生成销售出库单
        boolean exist = soOutstockService.checkExist(mainEntity.getCode(), SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode(), OrderTypeEnum.B2C.getCode());
        if (exist) {
            log.warn("销售订单{} 已生成销售出库单, 忽略生成", mainEntity.getCode() );
            return;
        }
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutStockByIdAndWarehouseId(mainEntity.getId(),warehouseId);
        // 循环内只做纯内存判断，超发SKU描述先收集到本地列表；出库主流程成功后再统一登记，
        // 避免"提示性"Feign失败阻断出库，也避免出库未成功时提前落异常
        List<String> overShipMessages = new ArrayList<>();
        //查询三方仓发货明细，重新赋值明细数据
        if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
            List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDeliveryDetailEntityList = thirdWarehouseDeliveryDetailService.listByMainId(thirdWarehouseDeliveryEntity.getId());
            if(CollectionUtils.isNotEmpty(thirdWarehouseDeliveryDetailEntityList)){
                LinkedList<SoOutstockDetailDTO.AddDTO> wantDetailList = new LinkedList<>();
                List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Arrays.asList(mainEntity.getId()));
                // 爱亚侧SKU(platformSkuNo) -> 实际发货数量，一次性构建供下面按SKU O(1)查表比较超发，
                // 不在明细循环里发起新的查询/远程调用
                Map<String, Integer> platformSkuActualQtyMap = buildPlatformSkuActualQtyMap(dto);
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
                    Integer deliveryQty = thirdWarehouseDeliveryDetailEntity.getDeliveryQty();
                    addDTO.setPlanQty(deliveryQty);
                    addDTO.setActualQty(resolveActualQtyAndCollectOverShip(thirdWarehouseDeliveryDetailEntity, platformSkuActualQtyMap, deliveryQty, overShipMessages));
                    addDTO.setWarehouseLocation(soB2cDetailEntity.getWarehouseLocation());
                    if(StringUtils.isNotBlank(warehouseId)){
                        addDTO.setWarehouseId(warehouseId);
                        addDTO.setVirtualWarehouseId(virtualWarehouseId);
                    }else {
                        addDTO.setWarehouseId(thirdWarehouseDeliveryDetailEntity.getWarehouseId());
                        addDTO.setVirtualWarehouseId(thirdWarehouseDeliveryDetailEntity.getVirtualWarehouseId());
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
            String newTrackNo = CharSequenceUtil.blankToDefault(dto.getTrackNo(), thirdWarehouseDeliveryEntity.getTrackNo());
            boolean statusChanged = !SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus().equals(thirdWarehouseDeliveryEntity.getStatus());
            boolean trackNoChanged = !StrUtil.equals(newTrackNo, thirdWarehouseDeliveryEntity.getTrackNo());
            if (statusChanged) {
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus());
                operateLogService.addModuleOperateLog("状态变更已发货", ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(),thirdWarehouseDeliveryEntity.getId(), "状态变更");
            }
            thirdWarehouseDeliveryEntity.setTrackNo(newTrackNo);
            if (statusChanged || trackNoChanged) {
                thirdWarehouseDeliveryService.updateById(thirdWarehouseDeliveryEntity);
            }
        }
        // 出库主流程成功后再登记超发：整单合并成一条异常、只发一次Feign；失败只告警不抛出
        reportOverShipIfNeeded(mainEntity, dto, overShipMessages);
    }

    /**
     * 按爱亚侧 SKU（{@link PlatformOutboundDTO.Item#getProductSku()}，与建单时下发给三方仓的 sku
     * 同口径，等价 {@link ThirdWarehouseDeliveryDetailEntity#getPlatformSkuNo()}，<b>不是</b> ERP
     * 自身 skuNo）构建"实际发货数量"查找表，一次构建，供 {@link #resolveActualQtyAndCollectOverShip}
     * 按 SKU O(1) 比较，避免在明细循环里重复解析。
     *
     * @param dto 平台出库单DTO（{@code items} 目前仅爱亚出库单会填充，其他平台为空属正常场景）
     * @return 爱亚侧SKU -> 实际发货数量；dto.items 为空时返回空表
     */
    private Map<String, Integer> buildPlatformSkuActualQtyMap(PlatformOutboundDTO dto) {
        List<PlatformOutboundDTO.Item> items = dto.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> platformSkuActualQtyMap = new HashMap<>(items.size());
        for (PlatformOutboundDTO.Item item : items) {
            if (item == null || StringUtils.isBlank(item.getProductSku()) || item.getActualQty() == null) {
                continue;
            }
            platformSkuActualQtyMap.merge(item.getProductSku(), item.getActualQty(), Integer::sum);
        }
        return platformSkuActualQtyMap;
    }

    /**
     * 超发（超量发货）判定：按 {@code platformSkuNo}（爱亚侧SKU，与 {@code platformSkuActualQtyMap}
     * 同口径，见 {@link #buildPlatformSkuActualQtyMap}）比较三方仓实际发货数量与《三方仓发货单》应发
     * 数量（{@code deliveryQty}）。若实际数量大于应发数量，把该SKU的超发描述追加进
     * {@code overShipMessages}（不在此处调用任何远程接口，纯内存判断），并返回实际数量供该行按
     * 实发数量生成出库单明细；否则维持现状，返回应发数量。异常登记统一由调用方在明细循环结束后
     * 通过 {@link #reportOverShipIfNeeded} 合并成一条一次性上报，避免逐SKU同步调用Feign。
     *
     * @param thirdWarehouseDeliveryDetail 三方仓发货单明细（含应发数量、爱亚侧SKU）
     * @param platformSkuActualQtyMap      爱亚侧SKU -> 实际发货数量查找表
     * @param deliveryQty                  应发数量
     * @param overShipMessages             超发SKU描述收集列表（调用方持有，本方法只追加不上报）
     * @return 该明细行应写入销售出库单的实际数量
     */
    private Integer resolveActualQtyAndCollectOverShip(ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetail,
                                                         Map<String, Integer> platformSkuActualQtyMap,
                                                         Integer deliveryQty,
                                                         List<String> overShipMessages) {
        String platformSkuNo = thirdWarehouseDeliveryDetail.getPlatformSkuNo();
        if (StringUtils.isBlank(platformSkuNo) || platformSkuActualQtyMap == null || platformSkuActualQtyMap.isEmpty()) {
            return deliveryQty;
        }
        Integer actualQty = platformSkuActualQtyMap.get(platformSkuNo);
        if (actualQty == null || deliveryQty == null || actualQty <= deliveryQty) {
            return deliveryQty;
        }
        overShipMessages.add(StrUtil.format("SKU【{}】（ERP SKU【{}】）应发数量{}，三方仓实际发货数量{}",
                platformSkuNo, thirdWarehouseDeliveryDetail.getSkuNo(), deliveryQty, actualQty));
        return actualQty;
    }

    /**
     * 把整单所有超发SKU合并成一条"三方仓超发"异常订单登记（仅提示，不阻断销售出库单自动生成/
     * 审核流程）。须在出库主流程（{@code thirdWarehouseCheckAndGenerate}）成功之后调用：
     * <ul>
     *   <li>一个订单无论有多少SKU超发，只发起一次 {@code soB2cFeign.addSoB2cError}，避免 N+1；</li>
     *   <li>登记失败整体 try-catch 兜底：不向上抛出（避免已成功生成的出库单被 MQ 重试打断），
     *       但除 {@code log.error} 外还会发一条系统预警（{@link MQProducerService#sendWarnMsg}），
     *       保证运维可感知、不至于静默丢失。</li>
     * </ul>
     *
     * @param mainEntity        B2C销售订单主表
     * @param dto               平台出库单DTO，异常登记时用于记录原始报文
     * @param overShipMessages  {@link #resolveActualQtyAndCollectOverShip} 收集到的超发SKU描述；为空则不登记
     */
    private void reportOverShipIfNeeded(SoB2cEntity mainEntity, PlatformOutboundDTO dto, List<String> overShipMessages) {
        if (CollectionUtils.isEmpty(overShipMessages)) {
            return;
        }
        String message = "三方仓超发：" + String.join("；", overShipMessages);
        log.warn("销售订单{} {}", mainEntity.getCode(), message);
        try {
            String dtoJson = JSONUtil.toJsonStr(dto);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    mainEntity.getId(),
                    SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OVER_SHIP.getCode(),
                    dtoJson,
                    message,
                    dtoJson,
                    null
            );
            soB2cFeign.addSoB2cError(addError);
        } catch (Exception e) {
            log.error("销售订单{} 登记三方仓超发异常失败，不阻断已成功的出库主流程，改为发送系统预警，超发详情={}",
                    mainEntity.getCode(), message, e);
            sendOverShipRegisterWarn(mainEntity, message, e);
        }
    }

    /**
     * 超发异常登记 Feign 失败时的可观测性兜底：发系统预警，避免只落本地日志导致运维无感知。
     * 告警本身再失败只打 error 日志，不再向外抛，以免影响出库主流程已成功的消费结果。
     *
     * @param mainEntity B2C销售订单主表
     * @param message    超发详情文案
     * @param cause      原始登记失败异常
     */
    private void sendOverShipRegisterWarn(SoB2cEntity mainEntity, String message, Exception cause) {
        try {
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("三方仓超发异常登记");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
            warnMsgInfo.setTitle(CharSequenceUtil.format("销售订单【{}】三方仓超发异常登记失败", mainEntity.getCode()));
            warnMsgInfo.setTableName("so_b2c");
            warnMsgInfo.setTableId(CharSequenceUtil.blankToDefault(mainEntity.getId(), ""));
            warnMsgInfo.setKeyInfo(CharSequenceUtil.format("{}；登记失败原因：{}",
                    message, cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()));
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            mqProducerService.sendWarnMsg(warnMsgInfo);
        } catch (Exception warnEx) {
            log.error("销售订单{} 三方仓超发异常登记失败后发送系统预警也失败，超发详情={}",
                    mainEntity.getCode(), message, warnEx);
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

    /**
     * 确认 WEGO 待处理拦截单终态（仅 WEGO 异步截单轮询确认使用，不影响其他海外仓）。
     *
     * @param soId    销售订单 ID
     * @param success true=拦截成功，false=拦截失败
     * @param remark  处理备注
     */
    private void confirmWegoInterceptBills(String soId, boolean success, String remark) {
        if (CharSequenceUtil.isBlank(soId)) {
            return;
        }
        List<SoB2cDeliveryInterceptEntity> interceptList = soB2cDeliveryInterceptService.listBySourceIds(
                Collections.singletonList(soId));
        if (CollUtil.isEmpty(interceptList)) {
            return;
        }
        for (SoB2cDeliveryInterceptEntity intercept : interceptList) {
            if (!SoB2cDeliveryInterceptStatusEnum.WAIT_HANDLE.getStatus().equals(intercept.getHandleStatus())) {
                continue;
            }
            try {
                if (success) {
                    soB2cDeliveryInterceptService.apiHandleSuccess(intercept.getId(), remark);
                } else {
                    soB2cDeliveryInterceptService.apiHandleFailure(intercept.getId(), remark);
                }
            } catch (Exception e) {
                log.warn("WEGO拦截单终态确认失败, interceptId={}, soId={}, success={}",
                        intercept.getId(), soId, success, e);
            }
        }
    }
}

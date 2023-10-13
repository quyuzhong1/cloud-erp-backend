package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.mapper.DmpPullTaskMapper;
import com.erp.server.dmp.service.DmpOrderInfoService;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpPullTaskServiceImpl extends SuperServiceImpl<DmpPullTaskMapper, DmpPullTaskEntity> implements DmpPullTaskService {

    @Autowired
    private DmpPullTaskMapper dmpPullTaskMapper;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private CustomerFeign customerFeign;
    @Resource
    private SoInfoFeign soInfoFeign;


    @Resource
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSyncInfo(String id, String syncStatus, String responseMsg) {
        LambdaUpdateWrapper<DmpPullTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPullTaskEntity::getId, id);
        updateWrapper.set(DmpPullTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPullTaskEntity::getStatus, syncStatus);
        updateWrapper.set(StrUtil.isNotBlank(responseMsg), DmpPullTaskEntity::getReturnMsg, responseMsg);
        updateWrapper.set(DmpPullTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public void saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpSyncTaskEntity) {
        DmpPullTaskEntity found = lambdaQuery()
                .eq(DmpPullTaskEntity::getSourceType, dmpSyncTaskEntity.getSourceType())
                .eq(DmpPullTaskEntity::getSourceId, dmpSyncTaskEntity.getSourceId())
                .eq(DmpPullTaskEntity::getSourcePlatformName, dmpSyncTaskEntity.getSourcePlatformName())
                .eq(DmpPullTaskEntity::getTargetPlatformName, dmpSyncTaskEntity.getTargetPlatformName())
                .eq(DmpPullTaskEntity::getMqTopic, dmpSyncTaskEntity.getMqTopic())
                .eq(DmpPullTaskEntity::getMqTag, dmpSyncTaskEntity.getMqTag())
                .last("LIMIT 1")
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            dmpSyncTaskEntity.setId(found.getId());
        }
        this.saveOrUpdate(dmpSyncTaskEntity);
    }

    /**
     * 新增同步金蝶退货单到wms退货入库单的任务
     *
     * @param entity
     * @return void
     * @Author Luo_WG
     * @Date 2023/7/4 19:48
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeReturnOrderToWms(KingdeeReturnOrderEntity entity) {
        //新增发送任务
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
        dmpSyncTaskEntity.setSourceId(entity.getFId());
        dmpSyncTaskEntity.setSourceCode(entity.getFBillNo());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.IN_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(entity);
        dmpSyncTaskEntity.setMqData(mqData);
        this.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public List<String> listKingdeeCode(Map<String, Object> conditon) {
        List<String> result = new ArrayList<>();

        LambdaQueryWrapper<DmpPullTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DmpPullTaskEntity::getSourceCode);
        queryWrapper.eq(DmpPullTaskEntity::getSourcePlatformName, "金蝶云星空")
                .eq(DmpPullTaskEntity::getTargetPlatformName, "自研ERP")
                .eq(null != conditon.get("id"), DmpPullTaskEntity::getId, conditon.get("id"))
                .eq(null != conditon.get("is_deleted"), DmpPullTaskEntity::getIsDeleted, conditon.get("is_deleted"))
                .eq(null != conditon.get("source_type"), DmpPullTaskEntity::getSourceType, conditon.get("source_type"))
                .eq(null != conditon.get("source_code"), DmpPullTaskEntity::getSourceCode, conditon.get("source_code"))
                .eq(null != conditon.get("source_id"), DmpPullTaskEntity::getSourceCode, conditon.get("source_id"))
                .eq(null != conditon.get("status"), DmpPullTaskEntity::getStatus, conditon.get("status"))
                .eq(null != conditon.get("mq_tag"), DmpPullTaskEntity::getMqTag, conditon.get("mq_tag"))
                .like(null != conditon.get("return_msg"), DmpPullTaskEntity::getReturnMsg, conditon.get("return_msg"))
        ;
        queryWrapper.last(null != conditon.get("lastSql"), " and " + conditon.get("lastSql").toString());
        List<DmpPullTaskEntity> queryResult = this.list(queryWrapper);

        if (CollectionUtil.isNotEmpty(queryResult)) {
            queryResult.stream().forEach(item -> result.add(item.getSourceCode()));
        }

        return result;
    }

    @Override
    public void syncOmsOrderToDmp(Map<String, Object> resultMap) {
        //检查推送状态是否已完成，已完成则直接返回
        Object dmpPullTaskId = resultMap.getOrDefault("dmpPullTaskId", null);
        Object id = resultMap.getOrDefault("id", null);
        Object code = resultMap.getOrDefault("code", null);
        Object operate = resultMap.getOrDefault("operate", null);
        if (Objects.isNull(dmpPullTaskId) || Objects.isNull(id) || Objects.isNull(code) || Objects.isNull(operate)) {
            return;
        }
        DmpPullTaskEntity dmpPullTaskEntity = baseMapper.selectById(String.valueOf(dmpPullTaskId));
        if (Objects.equals(dmpPullTaskEntity.getStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())) {
            return;
        }
        //统一处理数据映射问题，并合并到 dmp_order_info
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(String.valueOf(id));
        if (Objects.isNull(soInfoEntity)) {
            return;
        }
        //根据操作类型进行操作
        if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_APPROVE.getCode())) {
            //审核
            try {
                DmpOrderInfoEntity dmpOrderInfoEntity = orderDataConvert(soInfoEntity);
                //订单入库
                dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), null);
            } catch (Exception e) {
                log.error("处理订单广播异常：{}",e.getMessage());
            }
        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DISAPPROVE.getCode())) {
            //反审核
            try {
                dmpOrderInfoService.removeOrderByCode(Collections.singletonList(String.valueOf(code)));
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), null);
            }catch (Exception e){
                log.error("处理订单广播异常：{}",e.getMessage());
            }

        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_INVALID.getCode())) {
            //作废 不处理
            log.info("作废状态，直接忽略同步dmp订单操作");
            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(String.valueOf(code));
            if (Objects.nonNull(dmpOrderInfoEntity)){
                dmpOrderInfoEntity.setOrderStatus(5);
            }
        }
        log.info("推送订单数据完成");

    }

    private DmpOrderInfoEntity orderDataConvert(SoInfoEntity soInfoEntity) {
//        //检查是否已存在订单信息
//        LambdaQueryWrapper<DmpOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DmpOrderInfoEntity::getSalesRecordNumber, soInfoEntity.getCode());
//        queryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, soInfoEntity.getCode());
//        queryWrapper.eq(DmpOrderInfoEntity::getPlatformSign, PlatformEnum.ERP_OMS.getName());
//        DmpOrderInfoEntity old = dmpOrderInfoMapper.selectOne(queryWrapper);
//        if (Objects.nonNull(old)) {
//            dmpOrderInfoEntity.setId(old.getId());
//        }
        DmpOrderInfoEntity dmpOrderInfoEntity = DmpOrderConverter.INSTANCE.soInfoToDmpOrder(soInfoEntity);
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
        SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
        BigDecimal exchangeRate;
        if (Optional.ofNullable(detailEntity).isPresent()) {
            exchangeRate = detailEntity.getExchangeRate();
        } else {
            exchangeRate = BigDecimal.ONE;
        }
        //订单业务字段设置
        if (Objects.nonNull(soInfoEntity.getInvalidStatus()) && soInfoEntity.getInvalidStatus()) {
//            OrderStateEnum
            dmpOrderInfoEntity.setOrderStatus(5);
        } else {
            //默认待配货
            dmpOrderInfoEntity.setOrderStatus(1);
        }
        dmpOrderInfoEntity.setPaidTime(soInfoEntity.getReceiveDate().atStartOfDay());
        CustomerInfoEntity customerInfo = null;
        try {
            customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
            if (Objects.nonNull(customerInfo)) {
                dmpOrderInfoEntity.setBuyerName(customerInfo.getName());
                dmpOrderInfoEntity.setBuyerUserId(customerInfo.getCode());
                dmpOrderInfoEntity.setShopName(customerInfo.getName());
                dmpOrderInfoEntity.setShopNo(customerInfo.getCode());
            }
        } catch (Exception e) {
            log.error("请求erp-oms customerFeign.getCustomerById异常:{}", e.getMessage());
        }


        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTotalOrigin = BigDecimal.ZERO;
        BigDecimal orderCost = BigDecimal.ZERO;
        BigDecimal itemTotalCost = BigDecimal.ZERO;
        soDetailEntities.stream().forEach(
                soDetailEntity -> {
                    orderCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                    itemTotal.add(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(Optional.ofNullable(soDetailEntity.getQty()).orElse(0))).multiply(exchangeRate));
                    itemTotalOrigin.add(Optional.ofNullable(soDetailEntity.getTaxAmountBefore()).orElse(BigDecimal.ZERO));
                    itemTotalCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO));
                }
        );
        //订单成本价
        dmpOrderInfoEntity.setOrderCost(orderCost);
        //缺货订单
//        dmpOrderInfoEntity.setHasGoods(0);
        dmpOrderInfoEntity.setCurrencyRate(exchangeRate);
        dmpOrderInfoEntity.setItemTotal(itemTotal);
        //先计算运费收入（原币）
        if (Optional.ofNullable(soInfoEntity.getIsCollectShippingFee()).isPresent()) {
            dmpOrderInfoEntity.setShippingTotalOrigin(soInfoEntity.getShippingFee());
        } else {
            dmpOrderInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        }
        //运费收入（本位币）
        dmpOrderInfoEntity.setShippingFee(dmpOrderInfoEntity.getShippingTotalOrigin().multiply(exchangeRate));
        //商品销售总金额(原币)
        dmpOrderInfoEntity.setItemTotalOrigin(itemTotalOrigin);
        //商品总成本(原币)
        dmpOrderInfoEntity.setItemTotalCost(itemTotalCost);
        //补贴金额
//        dmpOrderInfoEntity.setSubsidyAmount(BigDecimal.ZERO);
        //国家字典
        if (Objects.nonNull(customerInfo) && StringUtils.isNotEmpty(customerInfo.getCountryId())) {
            try {
                DictCountryEntity country = sysUserFeign.getCountryById(customerInfo.getCountryId());
                if (Objects.nonNull(country)) {
                    dmpOrderInfoEntity.setCountryNameCn(country.getNameCn());
                    dmpOrderInfoEntity.setCountryNameEn(country.getNameEn());
                    dmpOrderInfoEntity.setSite(country.getId());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.getCountryById {}异常：{}", customerInfo.getCountryId(), e.getMessage());
            }
        }
        //平台标识
//        dmpOrderInfoEntity.setPlatformSign(PlatformEnum.ERP_OMS.getName());
        //平台创建时间
        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());
        //部门名称
        if (StringUtils.isNotEmpty(soInfoEntity.getSalesDeptId())) {
            try {
                List<SysDepartmentEntity> dept = sysUserFeign.listDeptByIds(Collections.singletonList(soInfoEntity.getSalesDeptId()));
                if (CollectionUtil.isNotEmpty(dept)) {
                    dmpOrderInfoEntity.setDeptName(dept.get(0).getName());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.listDeptByIds {}异常：{}", soInfoEntity.getSalesDeptId(), e.getMessage());

            }
        }

        dmpOrderInfoEntity.setCnySettleRate(exchangeRate);
        dmpOrderInfoEntity.setSourceId(soInfoEntity.getId());
        //订单明细
        List<DmpOrderItemEntity> orderItemEntities = new ArrayList<>(soDetailEntities.size());
        //明细字段转换
        if (CollectionUtil.isNotEmpty(soDetailEntities)) {
            soDetailEntities.forEach(soDetailEntity -> {
                DmpOrderItemEntity dmpOrderItemEntity = DmpOrderConverter.INSTANCE.soDetailToDmpOrderItem(soDetailEntity);
                dmpOrderItemEntity.setOrderId(dmpOrderInfoEntity.getId());
//                dmpOrderItemEntity.setItemId(soDetailEntity.getSkuNo());
//                dmpOrderItemEntity.setPlatformSku(soDetailEntity.getSkuNo());
//                dmpOrderItemEntity.setQuantity(soDetailEntity.getQty());
//                dmpOrderItemEntity.setPlatformQuantity(soDetailEntity.getQty());
                if (StringUtils.isNotEmpty(soDetailEntity.getSkuId())) {
                    ProductDetailEntity productDetail = productDetailService.getById(soDetailEntity.getSkuId());
                    if (Objects.nonNull(productDetail)) {
                        dmpOrderItemEntity.setItemName(productDetail.getName());
                        dmpOrderItemEntity.setPictureUrl(productDetail.getImagesUrl());
                        dmpOrderItemEntity.setSpecifics(productDetail.getVariantProperty());
                    }
                }
                dmpOrderItemEntity.setCostPrice(Optional.ofNullable(soDetailEntity.getPurchasePrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
//                dmpOrderItemEntity.setSellPriceOrigin(soDetailEntity.getPrice());
                dmpOrderItemEntity.setSellPrice(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
//                dmpOrderItemEntity.setProductUnit("pcs");
                dmpOrderItemEntity.setStockWarehouseId(soInfoEntity.getWarehouseId());
                dmpOrderItemEntity.setAmountAfter(Optional.ofNullable(soDetailEntity.getTaxAmountBefore()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(soDetailEntity.getDiscountAmount()).orElse(BigDecimal.ZERO)));
                orderItemEntities.add(dmpOrderItemEntity);
            });
        }
        dmpOrderInfoEntity.setItemList(orderItemEntities);
        return dmpOrderInfoEntity;
    }

    @Override
    public Boolean sendMqAndSaveTask(DmpPullTaskFeignDTO dto) {
        // 保存任务表
        try {
            DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                    dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                    dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
            this.saveOrUpdateDmpSyncTask(entity);
            // 发送MQ消息
            DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(entity.getId(), dto.getMqData());
            SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), dmpSyncMqDTO, entity.getSourceId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                log.error("发送MQ数据异常，{}", JSONUtil.toJsonStr(result));
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("sendMqAndSaveTask 发送MQ数据异常，{}", e.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public String savePullTask(DmpPullTaskFeignDTO dto) {
        // 保存任务表
        try {
            DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                    dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                    dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
            this.saveOrUpdateDmpSyncTask(entity);
            return entity.getId();
        } catch (Exception e) {
            log.error("savePullTask 保存数据异常，{}", e.getMessage());
        }
        return null;
    }
}

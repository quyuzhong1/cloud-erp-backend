package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.enums.FbaReceiveHandleStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.mapper.FbaShipmentReceiveMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

/**
 * <p>
 * FBA货件签收信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Slf4j
@Service
public class FbaShipmentReceiveServiceImpl extends SuperServiceImpl<FbaShipmentReceiveMapper, FbaShipmentReceiveEntity> implements FbaShipmentReceiveService {

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;
    @Resource
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private MQProducerService mqProducerService;


    @Override
    public List<FbaShipmentReceiveEntity> listByDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaShipmentReceiveEntity::getDetailId, detailIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FbaShipmentReceiveEntity> checkAndSetReceiveSkuMapping(List<FbaShipmentDetailEntity> oldDetailEntityList, List<FbaShipmentReceiveEntity> sourceReceiveEntityList) {
//        Map<Boolean, List<FbaShipmentReceiveEntity>> gourpMap = sourceReceiveEntityList.stream()
//                .collect(Collectors.groupingBy(e -> StringUtils.isBlank(e.getSkuId()) || StringUtils.isBlank(e.getSkuNo())));
//        // 签收记录丢失映射关系的
//        List<FbaShipmentReceiveEntity> missingSkuMappingReceiveList = gourpMap.get(true);
//        if (CollectionUtils.isEmpty(missingSkuMappingReceiveList)){
//            return sourceReceiveEntityList;
//        }
        // 根据明细的新记录调拨
        List<FbaShipmentReceiveEntity> missingSkuMappingReceiveList = sourceReceiveEntityList;

        // 检查详情是否都有映射
        FbaShipmentDetailEntity missingSkuMappingEntity = oldDetailEntityList.stream().filter(e -> StringUtils.isBlank(e.getSkuId()) || StringUtils.isBlank(e.getSkuNo())).findFirst().orElse(null);
        if (null != missingSkuMappingEntity){
            String msg = StrUtil.format("【FBA货件更新】未找到平台sku【{}】映射数据", missingSkuMappingEntity.getMsku());
            throw new ServiceException(msg);
        }
        Map<String, FbaShipmentDetailEntity> detailEntityMap = oldDetailEntityList.stream().collect(Collectors.toMap(FbaShipmentDetailEntity::getMsku, Function.identity()));

        missingSkuMappingReceiveList.forEach(e-> {
            FbaShipmentDetailEntity detailEntity = detailEntityMap.get(e.getMsku());
            if (null == detailEntity){
                throw new ServiceException("签收记录：丢失详情，msku=" +  e.getMsku());
            }
            e.setSkuId(detailEntity.getSkuId());
            e.setSkuNo(detailEntity.getSkuNo());
        });

        if (!this.updateBatchById(missingSkuMappingReceiveList)){
            throw new ServiceException("批量更新签收记录失败");
        }
        // 已匹配关系的签收记录
//        List<FbaShipmentReceiveEntity> alreadySkuMappingReceiveEntityList = gourpMap.get(false);
//        if (CollectionUtils.isEmpty(alreadySkuMappingReceiveEntityList)){
//            return missingSkuMappingReceiveList;
//        }
//        missingSkuMappingReceiveList.addAll(alreadySkuMappingReceiveEntityList);
        return missingSkuMappingReceiveList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAndCheckTransfer(List<FbaShipmentReceiveEntity> entityList, FbaShipmentEntity fbaShipmentEntity) {
        // 检查数据
        long count = entityList.stream().map(FbaShipmentReceiveEntity::getFbaShipmentId).distinct().count();
        if (1 != count){
            throw new ServiceException("消费数据异常,需要FbaShipmentId一致");
        }

        for (FbaShipmentReceiveEntity entity : entityList) {
            if (StringUtils.isBlank(entity.getUniqueMd5()) ||
                    StringUtils.isBlank(entity.getFbaShipmentId()) ||
                    StringUtils.isBlank(entity.getMsku()) ||
                    StringUtils.isBlank(entity.getFnSku()) ||
                    null == entity.getReceiveQty() ||
                    null == entity.getReceiveDate()
            ){
                log.warn("保存FBA签收记录消费异常:存在为空的脏数据. entity={}", JSONUtil.toJsonStr(entity));
                return false;
            }
        }

        // 查询记录是否已存在
        List<String> uniqueMd5List = entityList.stream().map(FbaShipmentReceiveEntity::getUniqueMd5).distinct().collect(Collectors.toList());
        List<FbaShipmentReceiveEntity> oldEntityList = this.lambdaQuery()
                .in(FbaShipmentReceiveEntity::getUniqueMd5, uniqueMd5List)
                .list();
        if (!CollectionUtils.isEmpty(oldEntityList) && oldEntityList.size() == entityList.size()){
            log.warn("FBA签收记录数据已存在跳过:UniqueMd5List={}", uniqueMd5List);
            return true;
        }
        List<String> oldList = oldEntityList.stream().map(FbaShipmentReceiveEntity::getUniqueMd5).collect(Collectors.toList());
        // 过滤得到不存在的记录
        List<FbaShipmentReceiveEntity> saveList = entityList.stream().filter(e -> !oldList.contains(e.getUniqueMd5())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(saveList)){
            throw new ServiceException(StrUtil.format("FBA签收记录消费异常:不存在需要保存的记录, list={}", JSONUtil.toJsonStr(entityList)));
        }

        // 历史货件的签收记录只按指定时间保存
        LocalDate stopReceivedDate = fbaShipmentService.getStopGenReceivedDate(fbaShipmentEntity);
        if (null != stopReceivedDate){
            saveList = saveList.stream()
                    .filter(e -> e.getReceiveDate().toLocalDate().isAfter(stopReceivedDate))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(saveList)){
            log.warn("无按指定时间之后的的签收记录忽略处理: list={}",JSONUtil.toJsonStr(list()));
            return true;
        }

        // 查询对应detailId
        List<FbaShipmentDetailEntity> detailEntityList = fbaShipmentDetailService.listByMainIds(Collections.singletonList(fbaShipmentEntity.getId()));

        // 补充关联数据并保存
        fillData(saveList, detailEntityList, fbaShipmentEntity);
        if (!this.saveBatch(saveList)) {
            throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(entityList));
        }

        // 需要挑拨的列表
        List<FbaShipmentReceiveEntity> handleEntityList = new LinkedList<>(saveList);

        // 判断历史记录是否有处理
        List<FbaShipmentReceiveEntity> updateEntityList = oldEntityList.stream().filter(e -> FbaReceiveHandleStatusEnum.NONE.getCode().equalsIgnoreCase(e.getHandleStatus())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updateEntityList)) {
            // 补充关联数据并保存
            fillData(updateEntityList, detailEntityList, fbaShipmentEntity);
            if (!this.updateBatchById(updateEntityList)) {
                throw new ServiceException("[FbaShipmentDetailEntity] 批量更新失败: entity=" + JSONUtil.toJsonStr(entityList));
            }
            handleEntityList.addAll(updateEntityList);
        }

        // 添加日志
        List<OperateLogDTO.AddModuleOperateLogDTO> logList = handleEntityList.stream().map(entity -> {
            OperateLogDTO.AddModuleOperateLogDTO addModuleOperateLog = new OperateLogDTO.AddModuleOperateLogDTO();
            addModuleOperateLog.setBusinessId(fbaShipmentEntity.getId());
            addModuleOperateLog.setOperation("FBA签收数量变更");
            String msg = StrUtil.format("FBA货件【{}】,平台SKU【{}】签收变化数量【{}】",
                    fbaShipmentEntity.getFbaShipmentId(),
                    entity.getMsku(),
                    entity.getReceiveQty()
            );
            addModuleOperateLog.setContent(msg);
            addModuleOperateLog.setModuleType(ModuleTypeEnum.FBA_SHIPMENT.getCode());
            return addModuleOperateLog;
        }).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(logList);

        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());

        LocalDate billDate = entityList.get(0).getReceiveDate().toLocalDate();
        // 执行调拨逻辑
        fbaShipmentService.handlerWarehouse(fbaShipmentEntity, handleEntityList, billDate, closedDateMap);
        return true;
    }

    /**
     * 补充明细ID和sku信息
     */
    private static void fillData(List<FbaShipmentReceiveEntity> updateEntityList, List<FbaShipmentDetailEntity> detailEntityList, FbaShipmentEntity fbaShipmentEntity) {
        updateEntityList.forEach(entity-> {
            entity.setHandleStatus(FbaReceiveHandleStatusEnum.ALREADY.getCode());
            FbaShipmentDetailEntity currentDetailEntity = detailEntityList.stream()
                    .filter(e -> e.getMsku().equalsIgnoreCase(entity.getMsku()) && e.getFnSku().equalsIgnoreCase(entity.getFnSku()))
                    .findFirst().orElseThrow(() -> new ServiceException(StrUtil.format("[FBA签收记录数据消费异常]：未找到货件对应明细:fba_shipment_id={}, mSku={}, fnSku={}]", fbaShipmentEntity.getFbaShipmentId(), entity.getMsku(), entity.getFnSku())));
            entity.setDetailId(currentDetailEntity.getId());
            entity.setSkuNo(currentDetailEntity.getSkuNo());
            entity.setSkuId(currentDetailEntity.getSkuId());
            entity.setAsin(currentDetailEntity.getAsin());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndRemoveDetailIds(List<String> mainIds, LocalDate checkBillDate) {
        List<FbaShipmentDetailEntity> detailEntityList = fbaShipmentDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(detailEntityList)){
            return;
        }
        List<String> detailIds = detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<FbaShipmentReceiveEntity> oldList = this.lambdaQuery()
                .in(FbaShipmentReceiveEntity::getDetailId, detailIds)
                .ne(FbaShipmentReceiveEntity::getSourceType, "erp")
                .list();
        if (CollectionUtils.isEmpty(oldList)){
            return;
        }
        oldList = oldList.stream()
                .filter(e -> e.getReceiveDate().toLocalDate().isAfter(checkBillDate))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(oldList)) {
            return;
        }

        oldList.forEach(e->{
            e.setHandleStatus(FbaReceiveHandleStatusEnum.NONE.getCode());
            e.setDetailId("");
        });
        if (!this.updateBatchById(oldList)){
            throw new ServiceException("批量更新FBA签收记录失败");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FbaShipmentReceiveEntity> checkAndBindHistory(FbaShipmentEntity entity, List<FbaShipmentDetailEntity> detailEntityList, String sourceType) {
        List<FbaShipmentReceiveEntity> list = this.lambdaQuery()
                .eq(FbaShipmentReceiveEntity::getFbaShipmentId, entity.getFbaShipmentId())
                .eq(FbaShipmentReceiveEntity::getSourceType, sourceType)
                .ne(FbaShipmentReceiveEntity::getHandleStatus, FbaReceiveHandleStatusEnum.ALREADY.getCode())
                .list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        // 补充关联数据并保存
        fillData(list, detailEntityList, entity);
        if (!this.updateBatchById(list)) {
            throw new ServiceException("[FbaShipmentDetailEntity] 批量更新失败: entity=" + JSONUtil.toJsonStr(list));
        }
        return list;
    }



    @Override
    public FbaShipmentEntity getAndPullResend(FbaReceiveGroupEntity groupEntity) {
        String fbaShipmentId = groupEntity.getFbaShipmentId();
        // 检查货件是否是存在
        FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getByFbaShipmentId(groupEntity.getFbaShipmentId());
        if (null != fbaShipmentEntity) {
            return fbaShipmentEntity;
        }
        String shopId = groupEntity.getShopId();
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
        if (null == shopInfoEntity) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        // 不存在货件 拉取货件
        FbaShipmentDTO.PullShipmentDTO dto = new FbaShipmentDTO.PullShipmentDTO(shopId, Collections.singletonList(fbaShipmentId));
        fbaShipmentService.pullShipment(dto);
        // 延时重发mq
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_FBA_SHIPMENT_RECEIVE_TAG.getName(),
                JSONUtil.toJsonStr(groupEntity), groupEntity.getUniqueId(), 1);
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送领星FBA货件签收MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return null;
    }

    @Override
    public List<FbaShipmentReceiveEntity> listByDetailIdsAndSourceType(List<String> detailIds, String sourceType) {
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(FbaShipmentReceiveEntity::getDetailId, detailIds)
                .eq(FbaShipmentReceiveEntity::getSourceType, sourceType)
                .list();
    }
}

package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.excel.DmpPullTaskExportExcelDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.SoReturnInstockFeign;
import com.erp.server.dmp.convert.DmpOrderConverter;
import com.erp.server.dmp.mapper.DmpPullTaskMapper;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;
    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private CustomerFeign customerFeign;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private SoReturnFeign soReturnFeign;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private SoReturnInstockFeign soReturnInstockFeign;
    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSyncInfo(String id, String syncStatus, String responseMsg) {
        DmpPullTaskEntity entity = new DmpPullTaskEntity();
        entity.setId(id);
        entity.setLastSyncTime(LocalDateTime.now());
        entity.setStatus(syncStatus);
        if (StrUtil.isNotBlank(responseMsg)){
            entity.setReturnMsg(responseMsg);
        }

        entity.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(entity);
//        LambdaUpdateWrapper<DmpPullTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
//        updateWrapper.eq(DmpPullTaskEntity::getId, id);
//        updateWrapper.set(DmpPullTaskEntity::getLastSyncTime, LocalDateTime.now());
//        updateWrapper.set(DmpPullTaskEntity::getStatus, syncStatus);
//        updateWrapper.set(StrUtil.isNotBlank(responseMsg), DmpPullTaskEntity::getReturnMsg, responseMsg);
//        updateWrapper.set(DmpPullTaskEntity::getUpdateTime, LocalDateTime.now());
//        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpPullTaskEntity) {
        DmpPullTaskEntity found = lambdaQuery()
                .eq(DmpPullTaskEntity::getSourceType, dmpPullTaskEntity.getSourceType())
                .eq(DmpPullTaskEntity::getSourceId, dmpPullTaskEntity.getSourceId())
                .eq(DmpPullTaskEntity::getSourcePlatformName, dmpPullTaskEntity.getSourcePlatformName())
                .eq(DmpPullTaskEntity::getTargetPlatformName, dmpPullTaskEntity.getTargetPlatformName())
                .eq(DmpPullTaskEntity::getMqTopic, dmpPullTaskEntity.getMqTopic())
                .eq(DmpPullTaskEntity::getMqTag, dmpPullTaskEntity.getMqTag())
                .last("LIMIT 1")
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            dmpPullTaskEntity.setId(found.getId());
            dmpPullTaskEntity.setCreateTime(LocalDateTime.now());
            dmpPullTaskEntity.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(dmpPullTaskEntity);
        return dmpPullTaskEntity.getId();
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
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpPullTaskEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
        dmpPullTaskEntity.setSourceId(entity.getFId());
        dmpPullTaskEntity.setSourceCode(entity.getFBillNo());
        dmpPullTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpPullTaskEntity.setStatus(SyncStatusEnum.IN_SYNC.getCode());
        dmpPullTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpPullTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName());
        String mqData = JSONUtil.toJsonStr(entity);
        dmpPullTaskEntity.setMqData(mqData);
        this.saveOrUpdateDmpSyncTask(dmpPullTaskEntity);
        // 发送推送同步任务消息
        JSONObject jsonObject = JSONUtil.parseObj(dmpPullTaskEntity.getMqData());
        jsonObject.set("dmpSyncTaskId",dmpPullTaskEntity.getId());
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName(),
                jsonObject, StrUtil.uuid().toLowerCase());
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
    @Transactional(rollbackFor = Exception.class)
    public void sendMqAndSaveTask(DmpSyncTaskDTO.AddDTO dto) {
        // 保存任务表
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity(dto);
        this.saveOrUpdateDmpSyncTask(dmpPullTaskEntity);
        // 发送推送同步任务消息
        JSONObject jsonObject = JSONUtil.parseObj(dmpPullTaskEntity.getMqData());
        jsonObject.set("dmpSyncTaskId",dmpPullTaskEntity.getId());
        SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), jsonObject, dmpPullTaskEntity.getSourceId());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }


    @Override
    public List<DmpPullTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<DmpPullTaskDTO.TabListDTO> result = new ArrayList<>(4);
        List<DmpPullTaskDTO.TabListDTO> countList = baseMapper.listStatusCount(dto.getPermissionSql());
        //全部
        int allCount = countList.stream().mapToInt(DmpPullTaskDTO.TabListDTO::getCount).sum();
        DmpPullTaskDTO.TabListDTO all = new DmpPullTaskDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(DmpConstant.ALL);
        result.add(all);

        //同步成功
        DmpPullTaskDTO.TabListDTO success = new DmpPullTaskDTO.TabListDTO();
        success.setTabFlag(SyncStatusEnum.SUCCESS_SYNC.getCode());
        int successCount = countList.stream().filter(a -> a.getTabFlag().equals(success.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        success.setCount(successCount);
        result.add(success);

        //同步失败
        DmpPullTaskDTO.TabListDTO failed = new DmpPullTaskDTO.TabListDTO();
        failed.setTabFlag(SyncStatusEnum.FAILED_SYNC.getCode());
        int failedCount = countList.stream().filter(a -> a.getTabFlag().equals(failed.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        failed.setCount(failedCount);
        result.add(failed);

        //同步中
        DmpPullTaskDTO.TabListDTO syncIng = new DmpPullTaskDTO.TabListDTO();
        syncIng.setTabFlag(SyncStatusEnum.IN_SYNC.getCode());
        int syncIngCount = countList.stream().filter(a -> a.getTabFlag().equals(syncIng.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        syncIng.setCount(syncIngCount);
        result.add(syncIng);
        return result;
    }

    @Override
    public PagingVO<DmpPullTaskDTO.ListDTO> paging(PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        DmpPullTaskDTO.ParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpPullTaskDTO.ListDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(DmpPullTaskDTO.ParamDTO dto, HttpServletResponse response) {
        List<DmpPullTaskDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        doOpHandleDmpPushTask(list);
        List<DmpPullTaskExportExcelDTO> resultList = BeanMapperUtils.copyList(DmpPullTaskExportExcelDTO.class, list);
        String fileName = "中台拉取任务表";
        try {
            ExcelUtil.export(fileName, "中台拉取任务表", resultList, DmpPullTaskExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchSync(List<String> ids) {
        List<DmpPullTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        for (DmpPullTaskEntity dmpPullTaskEntity : list) {
            // 发送推送同步任务消息
            JSONObject jsonObject = JSONUtil.parseObj(dmpPullTaskEntity.getMqData());
            jsonObject.set("dmpSyncTaskId",dmpPullTaskEntity.getId());
            SendResult result = mqProducerService.syncClassMsg(dmpPullTaskEntity.getMqTopic(), dmpPullTaskEntity.getMqTag(), jsonObject, dmpPullTaskEntity.getSourceId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
        return Boolean.TRUE;
    }

    /**
     * @param list
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     */
    private void doOpHandleDmpPushTask(List<DmpPullTaskDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpPullTaskDTO.ListDTO listDTO : list) {
            listDTO.setSyncTypeName("拉取");
            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //同步状态名称
            listDTO.setStatusName(SyncStatusEnum.getNameByCode(listDTO.getStatus()));
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
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
        SoInfoEntity soInfoEntity = null;
        try {
            soInfoEntity = soInfoFeign.getSoInfoById(String.valueOf(id));
        } catch (Exception e) {
            log.error("请求erp-oms soInfoFeign.getSoInfoById 异常:{}", e.getMessage());
            throw new ServiceException(ApiError.NO_PERMISSION.code, "获取原始订单异常");
        }
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
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B审核订单广播异常：{}", e.getMessage());
            }
        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            try {
                dmpOrderInfoService.removeOrderByCode(Collections.singletonList(String.valueOf(code)));
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B审核订单广播异常：{}", e.getMessage());
            }

        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_INVALID.getCode())) {
            //作废 不处理
            log.info("作废状态，直接忽略同步dmp订单操作");
//            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(String.valueOf(code));
//            if (Objects.nonNull(dmpOrderInfoEntity)) {
//                dmpOrderInfoEntity.setOrderStatus(5);
//            }
        }
        log.info("推送订单数据完成");

    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncWmsOutStockToDmp(Map<String, Object> resultMap) {
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
        SoOutstockEntity entity = soOutstockFeign.getSoOutstockEntityById(String.valueOf(id));
        if (Objects.isNull(entity)) {
            return;
        }
        //根据操作类型进行操作
        if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_APPROVE.getCode())) {
            //审核
            try {
                DmpDeliveryDetailInfoEntity dmpOrderInfoEntity = outStockDataConvert(entity);
                //订单入库
                dmpDeliveryDetailInfoService.checkOrder(dmpOrderInfoEntity);
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B出库订单广播异常：{}", e.getMessage());
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
            }
        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            try {
                dmpDeliveryDetailInfoService.removeDeliveryByCodes(Collections.singletonList(String.valueOf(code)));
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B出库订单广播异常：{}", e.getMessage());
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
            }

        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_INVALID.getCode())) {
            //作废 不处理
            log.info("作废状态，直接忽略同步dmp订单操作");
//            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(String.valueOf(code));
//            if (Objects.nonNull(dmpOrderInfoEntity)) {
//                dmpOrderInfoEntity.setOrderStatus(5);
//            }
        }
        log.info("推送发货审核订单数据完成");

    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncOmsReturnToDmp(Map<String, Object> resultMap) {
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
        //统一处理数据映射问题，并合并到 dmp_return_order_info
        SoReturnEntity entity = soReturnFeign.getSoReturnById(String.valueOf(id));
        if (Objects.isNull(entity)) {
            return;
        }
        //根据操作类型进行操作
        if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_APPROVE.getCode())) {
            //审核
            try {
                DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = returnOrderDataConvert(entity);
                //订单入库
                dmpReturnOrderInfoService.checkOrder(dmpReturnOrderInfoEntity);
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B退货入库订单广播异常：{}", e.getMessage());
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
            }
        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DISAPPROVE.getCode()) || Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_DELETE.getCode())) {
            //反审核
            try {
                dmpReturnOrderInfoService.removeReturnOrderByCode(Collections.singletonList(String.valueOf(code)));
                //更新推送状态
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");
            } catch (Exception e) {
                log.error("处理B2B退货入库订单广播异常：{}", e.getMessage());
                this.updateSyncInfo(String.valueOf(dmpPullTaskId), SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
            }

        } else if (Objects.equals(String.valueOf(operate), SyncOperateEnum.OPERATE_INVALID.getCode())) {
            //作废 不处理
            log.info("作废状态，直接忽略同步dmp订单操作");
//            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(String.valueOf(code));
//            if (Objects.nonNull(dmpOrderInfoEntity)) {
//                dmpOrderInfoEntity.setOrderStatus(5);
//            }
        }
        log.info("推送退货通知订单数据完成");

    }

    private DmpOrderInfoEntity orderDataConvert(SoInfoEntity soInfoEntity) {
        DmpOrderInfoEntity dmpOrderInfoEntity = DmpOrderConverter.INSTANCE.soInfoToDmpOrder(soInfoEntity);
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
        SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
        BigDecimal exchangeRate;
        if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
            exchangeRate = detailEntity.getExchangeRate();
        } else {
            exchangeRate = BigDecimal.ONE;
        }
        //订单业务字段设置
        if (Objects.nonNull(soInfoEntity.getInvalidStatus()) && soInfoEntity.getInvalidStatus()) {
            dmpOrderInfoEntity.setOrderStatus(5);
        } else {
            //默认待配货
            dmpOrderInfoEntity.setOrderStatus(1);
        }
        dmpOrderInfoEntity.setPaidTime(Objects.nonNull(soInfoEntity.getReceiveDate()) ? soInfoEntity.getReceiveDate().atStartOfDay() : null);
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
                if (StringUtils.isNotEmpty(soDetailEntity.getSkuId())) {
                    ProductDetailEntity productDetail = productDetailService.getById(soDetailEntity.getSkuId());
                    if (Objects.nonNull(productDetail)) {
                        dmpOrderItemEntity.setItemName(productDetail.getName());
                        dmpOrderItemEntity.setPictureUrl(productDetail.getImagesUrl());
                        dmpOrderItemEntity.setSpecifics(productDetail.getVariantProperty());
                    }
                }
                dmpOrderItemEntity.setCostPrice(Optional.ofNullable(soDetailEntity.getPurchasePrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                dmpOrderItemEntity.setSellPrice(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                dmpOrderItemEntity.setStockWarehouseId(soInfoEntity.getWarehouseId());
                dmpOrderItemEntity.setAmountAfter(Optional.ofNullable(soDetailEntity.getTaxAmountBefore()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(soDetailEntity.getDiscountAmount()).orElse(BigDecimal.ZERO)));
                orderItemEntities.add(dmpOrderItemEntity);
            });
        }
        dmpOrderInfoEntity.setItemList(orderItemEntities);
        return dmpOrderInfoEntity;
    }

    /**
     * 销售出货单字段转换
     *
     * @param soOutstockEntity
     * @return
     */
    private DmpDeliveryDetailInfoEntity outStockDataConvert(SoOutstockEntity soOutstockEntity) {
        DmpDeliveryDetailInfoEntity entity = DmpOrderConverter.INSTANCE.soOutstockToDmpDelivery(soOutstockEntity);
        //原始订单
        SoInfoEntity soInfoEntity = null;
        List<SoDetailEntity> soDetailEntities;
        DmpOrderInfoEntity dmpOrderInfoEntity;
        BigDecimal exchangeRate;
        entity.setDeliveryDate(Objects.nonNull(soOutstockEntity.getActualDeliveryDate()) ? soOutstockEntity.getActualDeliveryDate() : null);
        try {
            if (StringUtils.isNotEmpty(soOutstockEntity.getSoId())) {
                soInfoEntity = soInfoFeign.getSoInfoById(soOutstockEntity.getSoId());
            }
            if (Objects.isNull(soInfoEntity)) {
                throw new ServiceException(ApiError.NO_PERMISSION.code, "获取原始订单异常:[" + soOutstockEntity.getSoId() + "]");
            }
            entity.setOrderNo(soInfoEntity.getCode());
            entity.setManStreet(soInfoEntity.getReceiveAddress());
            entity.setCurrencyCode(soInfoEntity.getCurrency());
            entity.setRemark(soInfoEntity.getRemark());
            soDetailEntities = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
            if (CollectionUtil.isNotEmpty(soDetailEntities)) {
                SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
                if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
                    exchangeRate = detailEntity.getExchangeRate();
                } else {
                    exchangeRate = BigDecimal.ONE;
                }
                entity.setCurrencyRate(exchangeRate);

                //TODO 暂时设置为0等tms接通后补充
                //先计算运费收入（原币）
//                if (Optional.ofNullable(soInfoEntity.getIsCollectShippingFee()).isPresent()) {
//                    //运费收入（本位币）
//                    entity.setShippingFee(soInfoEntity.getShippingFee().multiply(exchangeRate));
//                } else {
                //运费收入（本位币）
                entity.setShippingFee(BigDecimal.ZERO);
//                }

                BigDecimal itemTotalCost = BigDecimal.ZERO;
                BigDecimal orderTotalCost = BigDecimal.ZERO;
                soDetailEntities.stream().forEach(
                        soDetailEntity -> {
                            itemTotalCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO));
                            orderTotalCost.add(Optional.ofNullable(soDetailEntity.getAmount()).orElse(BigDecimal.ZERO));
                        }
                );
                entity.setItemTotalCost(itemTotalCost);
                entity.setOrderTotalCost(orderTotalCost);
            }
            //dmp同步订单
            dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(soInfoEntity.getCode());
            if (Objects.isNull(dmpOrderInfoEntity)) {
                log.error("请求erp-dmp dmpOrderInfoService.getOrderByPlatformOrderId 同步单不存在");
            } else {
                //订单更新
                dmpOrderInfoEntity.setOrderStatus(3);
                dmpOrderInfoEntity.setDeliveryTime(LocalDateTime.now());
                dmpOrderInfoService.updateById(dmpOrderInfoEntity);
            }

        } catch (Exception e) {
            log.error("请求erp-oms soInfoFeign.getSoInfoById 异常:{}", e.getMessage());
            throw new ServiceException(ApiError.NO_PERMISSION.code, "获取原始订单异常");
        }
        //发货通知单
        try {
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeFeign.getDeliveryNoticeBySourceId(soOutstockEntity.getSourceId());
            entity.setLogisticsNo(soDeliveryNoticeEntity.getTrackNo());
            entity.setCompanyId(soDeliveryNoticeEntity.getSalesDeptId());
            entity.setCompanyName(soDeliveryNoticeEntity.getSalesOrgName());

        } catch (Exception e) {
            log.error("请求erp-wms soDeliveryNoticeFeign.getDeliveryNoticeBySourceId 异常:{}", e.getMessage());
        }
        CustomerInfoEntity customerInfo = null;
        try {
            customerInfo = customerFeign.getCustomerById(soOutstockEntity.getCustomerId());
            if (Objects.nonNull(customerInfo)) {
                entity.setShopName(customerInfo.getName());
                entity.setShopNo(customerInfo.getCode());
                entity.setCustomerName(customerInfo.getName());
            }
        } catch (Exception e) {
            log.error("请求erp-oms customerFeign.getCustomerById异常:{}", e.getMessage());
        }
        entity.setOrderTotalCost(Optional.ofNullable(soOutstockEntity.getTotalDiscountAmount()).orElse(BigDecimal.ZERO).add(Optional.ofNullable(entity.getItemTotalCost()).orElse(BigDecimal.ZERO)));
        //国家字典
        if (Objects.nonNull(customerInfo) && StringUtils.isNotEmpty(customerInfo.getCountryId())) {
            try {
                DictCountryEntity country = sysUserFeign.getCountryById(customerInfo.getCountryId());
                if (Objects.nonNull(country)) {
                    entity.setCountryNameCn(country.getNameCn());
                    entity.setCountryNameEn(country.getNameEn());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.getCountryById {}异常：{}", customerInfo.getCountryId(), e.getMessage());
            }
        }
        //销售部门
        if (StringUtils.isNotEmpty(soInfoEntity.getSalesDeptId())) {
            try {
                List<SysDepartmentEntity> dept = sysUserFeign.listDeptByIds(Collections.singletonList(soInfoEntity.getSalesDeptId()));
                if (CollectionUtil.isNotEmpty(dept)) {
                    entity.setSaleDeptName(dept.get(0).getName());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.listDeptByIds {}异常：{}", soInfoEntity.getSalesDeptId(), e.getMessage());

            }
        }

        //获取销售出库单详情
        List<SoOutstockDetailEntity> details = null;
        try {
            details = soOutstockFeign.getSoOutstockDetailByDetailId(soOutstockEntity.getId());
        } catch (Exception e) {
            log.error("erp-wms soOutstockFeign.getSoOutstockDetailByDetailId {}异常：{}", soOutstockEntity.getId(), e.getMessage());
        }
        //明细字段转换
        if (CollectionUtil.isNotEmpty(details)) {
            //订单明细
            List<DmpDeliveryDetailItemEntity> orderItemEntities = new ArrayList<>(details.size());
            SoInfoEntity finalSoInfoEntity = soInfoEntity;
            details.forEach(soDetailEntity -> {
                DmpDeliveryDetailItemEntity dmpOrderItemEntity = DmpOrderConverter.INSTANCE.soOutstockToDmpDeliveryItem(soDetailEntity);
                dmpOrderItemEntity.setDeliveryDetailId(entity.getId());
                if (Objects.nonNull(finalSoInfoEntity)) {
                    dmpOrderItemEntity.setSaleOrderNo(finalSoInfoEntity.getId());
                    dmpOrderItemEntity.setPlatformOrderId(finalSoInfoEntity.getCode());
                }
                if (StringUtils.isNotEmpty(soDetailEntity.getSkuId())) {
                    ProductDetailEntity productDetail = productDetailService.getById(soDetailEntity.getSkuId());
                    if (Objects.nonNull(productDetail)) {
                        dmpOrderItemEntity.setItemName(productDetail.getName());
                        dmpOrderItemEntity.setItemId(productDetail.getProductId());
                        dmpOrderItemEntity.setProductUnit(productDetail.getUnitName());
                        dmpOrderItemEntity.setSpecifics(productDetail.getVariantProperty());
                    }
                }
                //通知详情表
                if (StringUtils.isNotEmpty(soDetailEntity.getSourceDetailId())) {
                    //通知单详情
                    SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = null;
                    try {
                        soDeliveryNoticeDetailEntity = soDeliveryNoticeFeign.getNoticeDetailById(soDetailEntity.getSourceDetailId());
                    } catch (Exception e) {
                        log.error("erp-wms soDeliveryNoticeFeign.getNoticeDetailById {}异常：{}", soDetailEntity.getSourceDetailId(), e.getMessage());
                    }
                    //
                    if (Objects.nonNull(soDeliveryNoticeDetailEntity)) {
                        dmpOrderItemEntity.setItemRemark(soDeliveryNoticeDetailEntity.getRemark());
                    }
                    if (Objects.nonNull(soDeliveryNoticeDetailEntity) && StringUtils.isNotEmpty(soDeliveryNoticeDetailEntity.getSourceDetailId())) {
                        List<SoDetailEntity> soDetailEntities2 = null;
                        try {
                            soDetailEntities2 = soInfoFeign.listSoDetailByIds(Collections.singletonList(soDeliveryNoticeDetailEntity.getSourceDetailId()));
                        } catch (Exception e) {
                            log.error("erp-wms soDeliveryNoticeFeign.getNoticeDetailById {}异常：{}", soDetailEntity.getSourceDetailId(), e.getMessage());
                        }
                        if (CollectionUtils.isNotEmpty(soDetailEntities2)) {
                            dmpOrderItemEntity.setCostPrice(soDetailEntities2.get(0).getSaleCost());
                            dmpOrderItemEntity.setSellPrice(soDetailEntities2.get(0).getPrice());
                            dmpOrderItemEntity.setIsGift(soDetailEntities2.get(0).getIsGift() ? 1 : 2);
                            dmpOrderItemEntity.setAmount(soDetailEntities2.get(0).getAmount());
                        }
                    }
                }
                orderItemEntities.add(dmpOrderItemEntity);
            });
            entity.setDetails(orderItemEntities);
        }
        return entity;
    }

    /**
     * 销售出货单字段转换
     *
     * @param soReturnEntity
     * @return
     */
    private DmpReturnOrderInfoEntity returnOrderDataConvert(SoReturnEntity soReturnEntity) {
        DmpReturnOrderInfoEntity entity = DmpOrderConverter.INSTANCE.soReturnOrderToDmpReturn(soReturnEntity);
        //原始订单
        SoInfoEntity soInfoEntity = null;
        Map<String, SoDetailEntity> soDetailEntityMap = null;
        entity.setRefundTime(Objects.nonNull(soReturnEntity.getBillDate()) ? soReturnEntity.getBillDate().atStartOfDay() : null);
        try {
            if (StringUtils.isNotEmpty(soReturnEntity.getSourceId())) {
                soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
            }
            if (Objects.nonNull(soInfoEntity)) {
                entity.setPaidTime(Objects.nonNull(soInfoEntity.getReceiveDate()) ? soInfoEntity.getReceiveDate().atStartOfDay() : null);
                entity.setOrderTime(soInfoEntity.getCreateTime());
                entity.setOrderCode(soInfoEntity.getCode());
            }
            if (Objects.nonNull(soInfoEntity) && StringUtils.isNotEmpty(soInfoEntity.getId())) {
                List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
                if (CollectionUtil.isNotEmpty(soDetailEntities)) {
                    soDetailEntityMap = soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getId, Function.identity()));
                    SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
                    BigDecimal exchangeRate;
                    if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
                        exchangeRate = detailEntity.getExchangeRate();
                    } else {
                        exchangeRate = BigDecimal.ONE;
                    }
                    entity.setCurrencyRate(exchangeRate);
                    BigDecimal orderFee = BigDecimal.ZERO;
                    soDetailEntities.forEach(
                            soDetailEntity -> orderFee.add(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate))
                    );
                    entity.setOrderFee(orderFee);
                }
            }
        } catch (Exception e) {
            log.error("请求erp-oms soInfoFeign.getSoInfoById 异常:{}", e.getMessage());
            throw new ServiceException(ApiError.NO_PERMISSION.code, "获取原始订单异常");
        }
        CustomerInfoEntity customerInfo = null;
        try {
            if (StringUtils.isNotEmpty(soReturnEntity.getCustomerId())) {
                customerInfo = customerFeign.getCustomerById(soReturnEntity.getCustomerId());
                if (Objects.nonNull(customerInfo)) {
                    entity.setShopNo(customerInfo.getCode());
                    entity.setBuyerUserId(customerInfo.getCode());
                }
            }

        } catch (Exception e) {
            log.error("请求erp-oms customerFeign.getCustomerById异常:{}", e.getMessage());
        }
        //国家字典
        if (Objects.nonNull(customerInfo) && StringUtils.isNotEmpty(customerInfo.getCountryId())) {
            try {
                DictCountryEntity country = sysUserFeign.getCountryById(customerInfo.getCountryId());
                if (Objects.nonNull(country)) {
                    entity.setCountryNameCn(country.getNameCn());
                    entity.setCountryNameEn(country.getNameEn());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.getCountryById {}异常：{}", customerInfo.getCountryId(), e.getMessage());
            }
        }

        //dmp同步订单
        DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(soInfoEntity.getCode());
        if (Objects.nonNull(dmpOrderInfoEntity)) {
            //订单更新
            dmpOrderInfoEntity.setOrderStatus(6);
            dmpOrderInfoEntity.setCorrectionStatus(6);
            dmpOrderInfoEntity.setIsReturned(1);
            dmpOrderInfoService.updateById(dmpOrderInfoEntity);
        }
        //出货单详情
        List<SoReturnDetailEntity> details = soReturnFeign.listDetailByMainId(soReturnEntity.getId());
        //明细字段转换
        if (CollectionUtil.isNotEmpty(details)) {
            //订单明细
            List<DmpReturnOrderItemEntity> orderItemEntities = new ArrayList<>(details.size());

            Map<String, SoDetailEntity> finalSoDetailEntityMap = soDetailEntityMap;
            details.forEach(soReturnDetail -> {
                DmpReturnOrderItemEntity dmpReturnOrderItemEntity = DmpOrderConverter.INSTANCE.soReturnOrderToDmpReturnItem(soReturnDetail);
                //保存时会重置主表id
                dmpReturnOrderItemEntity.setReturnOrderId(entity.getId());
                if (StringUtils.isNotEmpty(soReturnDetail.getSkuId())) {
                    ProductDetailEntity productDetail = productDetailService.getById(soReturnDetail.getSkuId());
                    if (Objects.nonNull(productDetail)) {
                        dmpReturnOrderItemEntity.setItemName(productDetail.getName());
                        dmpReturnOrderItemEntity.setProductUnit(productDetail.getUnitId());
                        dmpReturnOrderItemEntity.setPictureUrl(productDetail.getImagesUrl());
                        dmpReturnOrderItemEntity.setSpecifics(productDetail.getVariantProperty());
                    }
                }
                //获取订单详情表
                if (StringUtils.isNotEmpty(soReturnDetail.getSourceDetailId())) {
                    SoDetailEntity soDetail = finalSoDetailEntityMap.get(soReturnDetail.getSourceDetailId());
                    if (Objects.nonNull(soDetail)) {
                        dmpReturnOrderItemEntity.setSellPrice(soDetail.getAmount());
                        if (Objects.nonNull(soDetail.getTaxAmount()) && Objects.nonNull(soDetail.getQty()) && Objects.nonNull(soReturnDetail.getReturnQty())) {
                            dmpReturnOrderItemEntity.setAmountAfter(soDetail.getTaxAmount().divide(BigDecimal.valueOf(soDetail.getQty())).multiply(BigDecimal.valueOf(soReturnDetail.getReturnQty())));
                        }
                        dmpReturnOrderItemEntity.setCleanCostPrice(soDetail.getSaleCost());
                        if (Objects.nonNull(soDetail.getIsGift()) && soDetail.getIsGift()) {
                            dmpReturnOrderItemEntity.setIsGift(1);
                        } else {
                            dmpReturnOrderItemEntity.setIsGift(2);
                        }
                    }
                }
                orderItemEntities.add(dmpReturnOrderItemEntity);
            });
            entity.setItemList(orderItemEntities);
        }

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendMqAndSaveTask(DmpPullTaskFeignDTO dto) {
        // 保存任务表
        try {
            DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                    dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                    dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
            this.saveOrUpdateDmpSyncTask(entity);
            // 发送推送同步任务消息
            JSONObject jsonObject = JSONUtil.parseObj(entity.getMqData());
            jsonObject.set("dmpSyncTaskId",entity.getId());
            SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), jsonObject, entity.getSourceId());
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
    @Transactional(rollbackFor = Exception.class)
    public String savePullTask(DmpPullTaskFeignDTO dto) {
        DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
        this.saveOrUpdateDmpSyncTask(entity);
        return entity.getId();
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
        DmpPullTaskEntity entity = this.getById(syncTaskId);
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        //查询redis,预警8小时发送一次
        String existKey = StrUtil.format(RedisKeyConstant.DMP_PULL_TASK_WARN, entity.getId());
        boolean isHas = redisUtil.hasKey(existKey);
        if (isHas) {
            return;
        } else {
            //添加缓存
            redisUtil.set(existKey,entity, RedisService.EIGHT_HOURS_CACHE_TIME);
        }
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle(StrUtil.format("单据【{}】从{}推送至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo("");
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
}

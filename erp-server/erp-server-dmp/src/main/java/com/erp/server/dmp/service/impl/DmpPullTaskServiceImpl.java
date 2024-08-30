package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.dmp.mapper.DmpPullTaskHistoryMapper;
import com.erp.server.dmp.mapper.DmpPullTaskMapper;
import com.erp.server.dmp.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PULL_TASK;

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
    @Lazy
    @Resource
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private BiDeliveryDetailInfoService biDeliveryDetailInfoService;
    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;
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
    private DmpPullTaskHistoryMapper dmpPullTaskHistoryMapper;
    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SoB2cFeign soB2cFeign;

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
    @DataIdempotent(keyIdName = "dmpPullTaskEntity.redissonKey", waitTime = 30)
    public String saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpPullTaskEntity) {
        DmpPullTaskEntity found = lambdaQuery()
                .eq(DmpPullTaskEntity::getSourceType, dmpPullTaskEntity.getSourceType())
                .eq(DmpPullTaskEntity::getSourceId, dmpPullTaskEntity.getSourceId())
                .eq(DmpPullTaskEntity::getSourceCode, dmpPullTaskEntity.getSourceCode())
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
        int syncIngCount = countList.stream().filter(a -> a.getTabFlag().equals(syncIng.getTabFlag()) || a.getTabFlag().equals(SyncStatusEnum.TO_BE_SYNC.getCode())).mapToInt(DmpPullTaskDTO.TabListDTO::getCount).sum();
        syncIng.setCount(syncIngCount);
        result.add(syncIng);
        //无需同步
        DmpPullTaskDTO.TabListDTO noNeedSync = new DmpPullTaskDTO.TabListDTO();
        noNeedSync.setTabFlag(SyncStatusEnum.NO_NEED_SYNC.getCode());
        int noNeedSyncCount = countList.stream().filter(a -> a.getTabFlag().equals(noNeedSync.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        noNeedSync.setCount(noNeedSyncCount);
        result.add(noNeedSync);
        //已归档
        DmpPullTaskDTO.TabListDTO archived = dmpPullTaskHistoryMapper.getStatusCount(dto.getPermissionSql());
        result.add(archived);
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
    public Boolean exportExcel(DmpPullTaskDTO.ParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("中台拉取任务表", EXPORT_PULL_TASK.getCode(), dto);
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
    @Override
    public Boolean batchNoNeedSync(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //获取数据
        List<DmpPullTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DATA);
        }
        List<DmpPullTaskEntity> noNeedSyncIds = list.stream().filter(obj ->
                (!SyncStatusEnum.IN_SYNC.getCode().equals(obj.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(obj.getStatus())))
                .collect(Collectors.toList());
        noNeedSyncIds.forEach(dmpPushTaskEntity -> dmpPushTaskEntity.setStatus(SyncStatusEnum.NO_NEED_SYNC.getCode()));
        if (CollectionUtils.isNotEmpty(noNeedSyncIds)) {
            updateBatchById(noNeedSyncIds, 500);
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
    public void sendWarnMsg(String syncTaskId) {
//        DmpPullTaskEntity entity = this.getById(syncTaskId);
//        if (ObjectUtil.isEmpty(entity)) {
//            return;
//        }
//        //查询redis,预警8小时发送一次
//        String existKey = StrUtil.format(RedisKeyConstant.DMP_PULL_TASK_WARN, entity.getId());
//        boolean isHas = redisUtil.hasKey(existKey);
//        if (isHas) {
//            return;
//        } else {
//            //添加缓存
//            redisUtil.set(existKey,entity, RedisService.EIGHT_HOURS_CACHE_TIME);
//        }
//        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
//        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
//        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
//        warnMsgInfo.setTitle(StrUtil.format("单据【{}】从{}拉取至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
//        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
//        warnMsgInfo.setTableId(entity.getSourceId());
//        warnMsgInfo.setKeyInfo(entity.getReturnMsg());
//        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
//        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DmpPullTaskEntity> batchCheckSaveAndUpdate(List<DmpPullTaskEntity> allList, String platform, String sourceType, String targetPlatform, String topic, String tag) {
        List<DmpPullTaskEntity> resultList = new ArrayList<>();
        List<String> sourceIds = allList.stream().map(DmpPullTaskEntity::getSourceId).collect(Collectors.toList());
        // 查询所有(去重)
        List<DmpPullTaskEntity> existTaskList = new ArrayList<>(this.findList(platform, sourceType, targetPlatform, topic, tag, sourceIds)
                .stream()
                .collect(Collectors.toMap(
                        DmpPullTaskEntity::uniqueKey,
                        obj -> obj,
                        (existing, replacement) -> existing
                ))
                .values());

        Map<String, DmpPullTaskEntity> taskMap = existTaskList.stream().collect(Collectors.toMap(DmpPullTaskEntity::uniqueKey, Function.identity()));
        // 需要保存的List
        List<DmpPullTaskEntity> saveList = new LinkedList<>();
        // 需要更新的List
        List<DmpPullTaskEntity> updateList = new LinkedList<>();
        LocalDateTime now = LocalDateTime.now();
        allList.forEach(e->{
            DmpPullTaskEntity entity = taskMap.get(e.uniqueKey());
            if (null == entity){
                // 不存在添加到新增列表
                saveList.add(e);
            } else {
                // 存在添加到更新列表
                entity.setUpdateTime(now);
                updateList.add(entity);
            }
        });
        // 批量保存
        if (CollectionUtils.isNotEmpty(saveList)){
            // 分组
            List<List<DmpPullTaskEntity>> partition = Lists.partition(saveList, 1000);
            for (List<DmpPullTaskEntity> curList : partition) {
                if (!this.saveBatch(curList)){
                    throw new ServiceException("批量保存DmpPullTaskEntity失败");
                }
                // 添加到结果
                resultList.addAll(curList);
            }
        }
        // 批量更新
        if (CollectionUtils.isNotEmpty(updateList)){
            // 分组
            List<List<DmpPullTaskEntity>> partition = Lists.partition(updateList, 1000);
            for (List<DmpPullTaskEntity> curList : partition) {
                if (!this.updateBatchById(curList)){
                    throw new ServiceException("批量更新DmpPullTaskEntity失败");
                }
                // 添加到结果
                resultList.addAll(curList);
            }
        }
        return resultList;
    }

    @Override
    public List<DmpPullTaskEntity> findList(String platform, String sourceType, String targetPlatform, String topic, String tag, List<String> sourceIds) {
        return lambdaQuery()
                .in(DmpPullTaskEntity::getSourceId, sourceIds)
                .eq(DmpPullTaskEntity::getSourceType, sourceType)
                .eq(DmpPullTaskEntity::getSourcePlatformName, platform)
                .eq(DmpPullTaskEntity::getTargetPlatformName, targetPlatform)
                .eq(DmpPullTaskEntity::getMqTopic, topic)
                .eq(DmpPullTaskEntity::getMqTag, tag)
                .list();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        baseMapper.deleteByIds(ids);
    }

    @Override
    public int countMonth(LocalDateTime date) {
        return baseMapper.countMonth(date);
    }

    @Override
    public List<DmpPullTaskEntity> listMonth(LocalDateTime date, int pageSize, int effect) {
        return baseMapper.listMonth(date, pageSize, effect);
    }

    @Override
    public List<DmpPullTaskEntity> getWarnPullTaskList(List<String> statusList) {
        if (CollectionUtils.isEmpty(statusList)){
            return Collections.emptyList();
        }
        return lambdaQuery().select(DmpPullTaskEntity::getSourceId,
                        DmpPullTaskEntity::getSourceType,
                        DmpPullTaskEntity::getSourceCode,
                        DmpPullTaskEntity::getSourcePlatformName,
                        DmpPullTaskEntity::getTargetPlatformName,
                        DmpPullTaskEntity::getReturnMsg,
                        DmpPullTaskEntity::getUpdateTime)
                .in(DmpPullTaskEntity::getStatus, statusList).eq(DmpPullTaskEntity::getIsDeleted, Boolean.FALSE).list();
    }

    @Override
    public PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        Page<DmpPullTaskDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }
}

package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.DmpTaskMsgDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskHistoryEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.mapper.DmpPushTaskHistoryMapper;
import com.erp.server.dmp.mapper.DmpPushTaskMapper;
import com.erp.server.dmp.service.DmpPushTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PUSH_TASK;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@Service
public class DmpPushTaskServiceImpl extends SuperServiceImpl<DmpPushTaskMapper, DmpPushTaskEntity> implements DmpPushTaskService {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private DmpPushTaskHistoryMapper dmpPushTaskHistoryMapper;
    @Resource
    @Lazy
    private DmpPushTaskServiceImpl dmpPushTaskService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity saveTask(DmpPushTaskFeignDTO dto) {
        // 保存任务表
        DmpPushTaskEntity entity = new DmpPushTaskEntity(dto);
        //判断是否存在上级单据
        isSendParentBillTask(entity);
        saveOrUpdateDmpSyncTask(entity);
        return entity;
    }
    @Override
    public void sendTask(List<DmpPushTaskEntity> dmpPushTaskEntityList, Integer delayLevel) {
        if (CollectionUtils.isEmpty(dmpPushTaskEntityList)) {
            return;
        }
        for (DmpPushTaskEntity entity :dmpPushTaskEntityList) {
            if (ObjectUtil.isEmpty(entity)) {
                continue;
            }
            //查询来源上级单据
            Boolean isSend = isSendParentBillTask(entity);
            //判断是否存在上级单据，并且推送成功
            if (Boolean.FALSE.equals(isSend)) {
                return;
            }
            // 发送MQ消息
            String mqData = entity.getMqData();
            JSONObject jsonObject = JSONUtil.parseObj(mqData);
            jsonObject.set("dmpSyncTaskId",entity.getId());
            jsonObject.set("version",entity.getVersion());
            // delayLevel=0 无延时
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(entity.getMqTopic(),
                    entity.getMqTag(),
                    JSONUtil.toJsonStr(jsonObject),
                    entity.getSourceId(),
                    delayLevel
            );
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        LambdaUpdateWrapper<DmpPushTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPushTaskEntity::getId, paramDTO.getDmpSyncTaskId());
        updateWrapper.eq(ObjectUtil.isNotEmpty(paramDTO.getVersion()), DmpPushTaskEntity::getVersion, paramDTO.getVersion());
        updateWrapper.set(DmpPushTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPushTaskEntity::getStatus, paramDTO.getSyncStatus());
        updateWrapper.set(StrUtil.isNotBlank(paramDTO.getResponseMsg()), DmpPushTaskEntity::getReturnMsg, paramDTO.getResponseMsg());
        updateWrapper.set(DmpPushTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);

    }

    @Override
    public List<DmpPushTaskEntity> listNeedPushTask() {
        return lambdaQuery()
                .in(DmpPushTaskEntity::getStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .list();
    }

    @Override
    public DmpPushTaskEntity getByParam(DmpSyncTaskDTO.OneDTO oneDTO) {
        return lambdaQuery()
                .eq(DmpPushTaskEntity::getSourceId, oneDTO.getSourceId())
                .eq(StringUtils.isNotBlank(oneDTO.getSourceType()),DmpPushTaskEntity::getSourceType, oneDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, oneDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, oneDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTopic()),DmpPushTaskEntity::getMqTopic, oneDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTag()),DmpPushTaskEntity::getMqTag, oneDTO.getMqTag())
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<DmpPushTaskEntity> listByParam(DmpSyncTaskDTO.ListDTO listDTO) {
        return lambdaQuery()
                .in(DmpPushTaskEntity::getSourceId, listDTO.getSourceIdList())
                .eq(StringUtils.isNotBlank(listDTO.getSourceType()), DmpPushTaskEntity::getSourceType, listDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, listDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, listDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(listDTO.getMqTopic()), DmpPushTaskEntity::getMqTopic, listDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(listDTO.getMqTag()), DmpPushTaskEntity::getMqTag, listDTO.getMqTag())
                .list();
    }

    @Override
    public List<DmpPushTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<DmpPushTaskDTO.TabListDTO> result = new ArrayList<>(4);
        List<DmpPushTaskDTO.TabListDTO> countList = baseMapper.listStatusCount(dto.getPermissionSql());
        //全部
        int allCount = countList.stream().mapToInt(DmpPushTaskDTO.TabListDTO::getCount).sum();
        DmpPushTaskDTO.TabListDTO all = new DmpPushTaskDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(DmpConstant.ALL);
        result.add(all);

        //同步成功
        DmpPushTaskDTO.TabListDTO success = new DmpPushTaskDTO.TabListDTO();
        success.setTabFlag(SyncStatusEnum.SUCCESS_SYNC.getCode());
        int successCount = countList.stream().filter(a -> a.getTabFlag().equals(success.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        success.setCount(successCount);
        result.add(success);

        //同步失败
        DmpPushTaskDTO.TabListDTO failed = new DmpPushTaskDTO.TabListDTO();
        failed.setTabFlag(SyncStatusEnum.FAILED_SYNC.getCode());
        int failedCount = countList.stream().filter(a -> a.getTabFlag().equals(failed.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        failed.setCount(failedCount);
        result.add(failed);

        //同步中
        DmpPushTaskDTO.TabListDTO syncIng = new DmpPushTaskDTO.TabListDTO();
        syncIng.setTabFlag(SyncStatusEnum.IN_SYNC.getCode());
        int syncIngCount = countList.stream().filter(a -> a.getTabFlag().equals(syncIng.getTabFlag()) || SyncStatusEnum.TO_BE_SYNC.getCode().equals(a.getTabFlag())).mapToInt(DmpPushTaskDTO.TabListDTO::getCount).sum();
        syncIng.setCount(syncIngCount);
        result.add(syncIng);
        //已归档
        DmpPushTaskDTO.TabListDTO archived = dmpPushTaskHistoryMapper.getStatusCount(dto.getPermissionSql());
        result.add(archived);

        //无需同步
        DmpPushTaskDTO.TabListDTO noNeedSync = new DmpPushTaskDTO.TabListDTO();
        noNeedSync.setTabFlag(SyncStatusEnum.NO_NEED_SYNC.getCode());
        int noNeedSyncCount = countList.stream().filter(a -> a.getTabFlag().equals(noNeedSync.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        noNeedSync.setCount(noNeedSyncCount);
        result.add(noNeedSync);
        return result;
    }

    @Override
    public PagingVO<DmpPushTaskDTO.ListDTO> paging(PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        DmpPushTaskDTO.ParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpPushTaskDTO.ListDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(DmpPushTaskDTO.ParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("中台推送任务表", EXPORT_PUSH_TASK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchSync(List<String> ids) {
        List<DmpPushTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        //需要修改备注信息
        List<DmpPushTaskEntity> updateList = new ArrayList<>();
        for (DmpPushTaskEntity dmpPushTaskEntity : list) {
            try {
                //查询来源上级单据
                Boolean isSend = isSendParentBillTask(dmpPushTaskEntity);
                //判断是否存在上级单据，并且推送成功
                if (!isSend) {
                    updateList.add(dmpPushTaskEntity);
                    continue;
                }
                DmpPushTaskHistoryServiceImpl.sendMq(dmpPushTaskEntity.getMqData(), dmpPushTaskEntity.getId(),dmpPushTaskEntity.getVersion(), mqProducerService, dmpPushTaskEntity.getMqTopic(), dmpPushTaskEntity.getMqTag(), dmpPushTaskEntity.getSourceId());
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(dmpPushTaskEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", dmpPushTaskEntity.getSourcePlatformName(), sourceTypeName, dmpPushTaskEntity.getTargetPlatformName(), e);
            }
        }
        //更新信息
        if (CollectionUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchFindDataSync(List<String> ids) {
        List<DmpPushTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        long count = list.stream().filter(obj -> !PlatformEnum.ERP.getDesc().equals(obj.getSourcePlatformName())
                || (!PlatformEnum.KINGDEE.getDesc().equals(obj.getTargetPlatformName())
                && !PlatformEnum.MABANG.getDesc().equals(obj.getTargetPlatformName())
                && !PlatformEnum.WANGDIAN.getDesc().equals(obj.getTargetPlatformName()))).count();
        if (count > 0) {
            throw new ServiceException(new ApiResult(10000,"只允许推送自研ERP>>>>(金蝶、马帮)的数据"));
        }
        Map<String, List<DmpPushTaskEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getTargetPlatformName().concat(obj.getTargetPlatformName()).concat(obj.getSourceType())));
        for (Map.Entry<String, List<DmpPushTaskEntity>> entry : map.entrySet()) {
            List<DmpPushTaskEntity> value = entry.getValue();
            //来源类型
            String sourceType = value.get(0).getSourceType();
            //目的平台
            String targetPlatformName = value.get(0).getTargetPlatformName();

            List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList = value.stream().map(obj -> new DmpSyncMqDTO.SyncParamDetailDTO(obj.getSourceId(), obj.getSyncOperate())).collect(Collectors.toList());
            try {
                //金蝶
                if (PlatformEnum.KINGDEE.getDesc().equals(targetPlatformName)) {
                    // 发送MQ消息
                    findKingdeeDataAndSendMq(paramDetailList,sourceType);
                }
                //马帮
                if (PlatformEnum.MABANG.getDesc().equals(targetPlatformName)) {
                    // 发送MQ消息
                    findMaBangDataAndSendMq(paramDetailList,sourceType);
                }
                //旺店通
                if(PlatformEnum.WANGDIAN.getDesc().equals(targetPlatformName)){
                    paramDetailList = value.stream()
                            .filter(obj -> obj.getStatus().equals(SyncStatusEnum.NO_NEED_SYNC.getCode()) || obj.getStatus().equals(SyncStatusEnum.FAILED_SYNC.getCode()))
                            .map(obj -> new DmpSyncMqDTO.SyncParamDetailDTO(obj.getSourceId(), obj.getSyncOperate()))
                            .collect(Collectors.toList());
                    findWdtDataAndSendMq(paramDetailList,sourceType);
                }
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(sourceType);
                log.error("从{}推送{}到{}发送消息异常", PlatformEnum.ERP.getDesc(), sourceTypeName, PlatformEnum.KINGDEE.getDesc(), e);
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
        List<DmpPushTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DATA);
        }
        List<DmpPushTaskEntity> noNeedSyncIds = list.stream().filter(obj ->
                        (!SyncStatusEnum.IN_SYNC.getCode().equals(obj.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(obj.getStatus())))
                .collect(Collectors.toList());
        noNeedSyncIds.forEach(dmpPushTaskEntity -> dmpPushTaskEntity.setStatus(SyncStatusEnum.NO_NEED_SYNC.getCode()));
        if (CollectionUtils.isNotEmpty(noNeedSyncIds)) {
            updateBatchById(noNeedSyncIds, 500);
        }
        return Boolean.TRUE;
    }
    @Override
    public Boolean batchNoNeedSyncBySourceId(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //获取数据
        List<DmpPushTaskEntity> list = this.list(new LambdaQueryWrapper<DmpPushTaskEntity>().in(DmpPushTaskEntity::getSourceId,sourceIds));
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DATA);
        }
        List<String> noNeedSyncIds = list.stream().filter(obj ->
                        (!SyncStatusEnum.IN_SYNC.getCode().equals(obj.getStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(obj.getStatus())))
                .map(DmpPushTaskEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noNeedSyncIds)) {
            //http://pm.ulanzi.cn:8020/browse/ERP-3637?filter=-1  手动标记分货单同步完结时注意，同时修改dmp_push_task表中的状态为同步成功。
            this.lambdaUpdate().in(DmpPushTaskEntity::getId, noNeedSyncIds)
                    .set(DmpPushTaskEntity::getStatus, SyncStatusEnum.SUCCESS_SYNC.getCode())
                    .set(DmpPushTaskEntity::getReturnMsg,"")
                    .update();
        }
        return Boolean.TRUE;
    }

    /**
     * 根据sourceId重新同步
     * @param sourceIds
     * @return
     */
    @Override
    public Boolean batchSyncBySourceId(List<String> sourceIds) {
        List<DmpPushTaskEntity> list = this.list(new LambdaQueryWrapper<DmpPushTaskEntity>().in(DmpPushTaskEntity::getSourceId,sourceIds));
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        //需要修改备注信息
        List<DmpPushTaskEntity> updateList = new ArrayList<>();
        for (DmpPushTaskEntity dmpPushTaskEntity : list) {
            try {
                //查询来源上级单据
                Boolean isSend = isSendParentBillTask(dmpPushTaskEntity);
                //判断是否存在上级单据，并且推送成功
                if (!isSend) {
                    dmpPushTaskEntity.setStatus(SyncStatusEnum.IN_SYNC.getCode());
                    updateList.add(dmpPushTaskEntity);
                    continue;
                }
                DmpPushTaskHistoryServiceImpl.sendMq(dmpPushTaskEntity.getMqData(), dmpPushTaskEntity.getId(),dmpPushTaskEntity.getVersion(), mqProducerService, dmpPushTaskEntity.getMqTopic(), dmpPushTaskEntity.getMqTag(), dmpPushTaskEntity.getSourceId());
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(dmpPushTaskEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", dmpPushTaskEntity.getSourcePlatformName(), sourceTypeName, dmpPushTaskEntity.getTargetPlatformName(), e);
            }
        }
        //更新信息
        if (CollectionUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<DmpPushTaskEntity> getWarnPushTaskList(List<String> statusList) {
        if (CollectionUtils.isEmpty(statusList)){
            return Collections.emptyList();
        }
        return lambdaQuery().select(DmpPushTaskEntity::getSourceId,
                        DmpPushTaskEntity::getSourceType,
                        DmpPushTaskEntity::getSourceCode,
                        DmpPushTaskEntity::getSourcePlatformName,
                        DmpPushTaskEntity::getTargetPlatformName,
                        DmpPushTaskEntity::getReturnMsg,
                        DmpPushTaskEntity::getUpdateTime)
                .in(DmpPushTaskEntity::getStatus, statusList).eq(DmpPushTaskEntity::getIsDeleted, Boolean.FALSE).list();
    }

    /**
     * 获取飞书预警信息需要推送的(Task汇总报告)
     * @param statusList
     * @return
     */
    @Override
    public List<DmpTaskMsgDTO> getWarnTaskReport(List<String> statusList) {
        if (CollectionUtils.isEmpty(statusList)){
            return Collections.emptyList();
        }
        return baseMapper.getWarnTaskReport(statusList);
    }

    @Override
    public PagingVO<DmpPushTaskDTO.ListDTO> exportPushTask(PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        Page<DmpPushTaskDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }


    @Override
    public void sendWarnMsg(String syncTaskId) {
//        DmpPushTaskEntity entity = this.getById(syncTaskId);
//        if (ObjectUtil.isEmpty(entity)) {
//            return;
//        }
//        //查询redis,预警8小时发送一次
//        String existKey = StrUtil.format(RedisKeyConstant.DMP_PUSH_TASK_WARN, entity.getId());
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
//        warnMsgInfo.setTitle(StrUtil.format("单据【{}】从{}推送至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
//        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
//        warnMsgInfo.setTableId(entity.getSourceId());
//        warnMsgInfo.setKeyInfo(entity.getReturnMsg());
//        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
//        mqProducerService.sendWarnMsg(warnMsgInfo);
    }


    @Override
    public Boolean isSendParentBillTask (DmpPushTaskEntity entity) {
        if (StrUtil.isBlank(entity.getParentId())) {
            return Boolean.TRUE;
        }
        List<String> parentIdList = Arrays.stream(entity.getParentId().split(",")).collect(Collectors.toList());
        LambdaQueryWrapper<DmpPushTaskEntity> queryWrapper = Wrappers.<DmpPushTaskEntity>lambdaQuery().in(DmpPushTaskEntity::getSourceId, parentIdList)
                .eq(DmpPushTaskEntity::getSourcePlatformName, entity.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, entity.getTargetPlatformName());
        List<DmpPushTaskEntity> dmpPushTaskEntities = list(queryWrapper);
        //归档数据
        LambdaQueryWrapper<DmpPushTaskHistoryEntity> historyQueryWrapper = Wrappers.<DmpPushTaskHistoryEntity>lambdaQuery().in(DmpPushTaskHistoryEntity::getSourceId, parentIdList)
                .eq(DmpPushTaskHistoryEntity::getSourcePlatformName, entity.getSourcePlatformName())
                .eq(DmpPushTaskHistoryEntity::getTargetPlatformName, entity.getTargetPlatformName());
        Integer historyCount = dmpPushTaskHistoryMapper.selectCount(historyQueryWrapper);
        if (CollectionUtil.isEmpty(dmpPushTaskEntities) && historyCount == 0) {
            entity.setReturnMsg("未找到上级单据推送任务");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        boolean match = dmpPushTaskEntities.stream()
                .anyMatch(e -> !SyncStatusEnum.SUCCESS_SYNC.getCode().equals(e.getStatus()));
        if (match) {
            entity.setReturnMsg("上级单据未推送成功，不支持推送下级单据");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        return  Boolean.TRUE;
    }


    /**
     * @description: 重新查询数据发送金蝶MQ
     * @author Will
     * @date: 2023/10/30 10:03
     */
    private void findKingdeeDataAndSendMq (List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList,String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList,sourceTypeEnum);
        switch (SourceTypeEnum.getEnum(sourceType)) {
            case BASIC_CATEGORY:
            case PRODUCT_DETAIL:
            case PRODUCT_BOM_INFO:
                plmTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case SYS_USER_INFO:
                sysUserFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case PURCHASE_ORDER:
            case PURCHASE_CHANGE:
            case PURCHASE_PRICE:
            case PURCHASE_PRICE_CHANGE:
            case SUBCONTRACT_CHANGE:
            case SUBCONTRACT_ORDER:
            case SUPPLIER:
                scmTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case MACHINE_INFO:
            case OTHER_OUTSTOCK:
            case OTHER_INSTOCK:
            case PO_INSTOCK:
            case PO_RECEIVE:
            case PO_RETURN:
            case SO_OUTSTOCK:
            case SO_RETURN_INSTOCK:
            case STOCKTAKING_PROFIT_LOSS:
            case TRANSFER_INFO:
            case WAREHOUSE:
            case SUBCONTRACT_ISSUE:
                wmsTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case CUSTOMER_INFO:
            case CUSTOMER_CONTACT:
            case CUSTOMER_GROUP:
            case SO_INFO:
            case SO_CHANGE:
                omsTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            default:
                return;
        }
    }
    /**
     * @description: 重新查询数据发送马帮MQ
     * @author Will
     * @date: 2023/10/30 10:03
     */
    private void findMaBangDataAndSendMq (List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList,String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList,sourceTypeEnum);
        switch (SourceTypeEnum.getEnum(sourceType)) {
            case MACHINE_INFO:
            case TRANSFER_INFO:
                wmsTaskFeign.findMaBangDataSendSyncTask(syncParamDTO);
                return;
            default:
                return;
        }
    }



    /**
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     * @param list
     */
    private void doOpHandleDmpPushTask (List<DmpPushTaskDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpPushTaskDTO.ListDTO listDTO : list) {
            listDTO.setSyncTypeName("推送");
            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //同步状态名称
            listDTO.setStatusName(SyncStatusEnum.getNameByCode(listDTO.getStatus()));
            //同步操作名称
            listDTO.setSyncOperateName(SyncOperateEnum.getDescByCode(listDTO.getSyncOperate()));
        }
    }

    /**
     * 新增或修改
     */
    @Override
    public String saveOrUpdateDmpSyncTask(DmpPushTaskEntity entity) {
        DmpSyncTaskDTO.OneDTO map = BeanMapperUtils.map(DmpSyncTaskDTO.OneDTO.class, entity);
        DmpPushTaskServiceImpl bean = ApplicationContextUtils.getBean(DmpPushTaskServiceImpl.class);
        DmpPushTaskEntity found = bean.queryByParam(map);
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            entity.setId(found.getId());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setVersion(found.getVersion());
            bean.updateDmpSyncTask(entity);
        }else {
			bean.saveDmpSyncTask(entity);
        }
        return entity.getId();
    }

    @GlobalTransactional(rollbackFor = Exception.class , propagation = io.seata.tm.api.transaction.Propagation.NOT_SUPPORTED)
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.NOT_SUPPORTED)
    public DmpPushTaskEntity queryByParam(DmpSyncTaskDTO.OneDTO oneDTO) {
        return lambdaQuery()
                .eq(DmpPushTaskEntity::getSourceId, oneDTO.getSourceId())
                .eq(StringUtils.isNotBlank(oneDTO.getSourceType()),DmpPushTaskEntity::getSourceType, oneDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, oneDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, oneDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTopic()),DmpPushTaskEntity::getMqTopic, oneDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTag()),DmpPushTaskEntity::getMqTag, oneDTO.getMqTag())
                .last("LIMIT 1")
                .one();
    }

    /**
     * 新增
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void saveDmpSyncTask(DmpPushTaskEntity entity) {
    	this.save(entity);
    }

    /**
     * 修改
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class , propagation = io.seata.tm.api.transaction.Propagation.NOT_SUPPORTED)
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.NOT_SUPPORTED)
    public void updateDmpSyncTask(DmpPushTaskEntity entity) {
    	try {
			baseMapper.updateById(entity);
		} catch (Exception e) {
			log.error("更新推送表失败：{}" , entity.getId() , e);
		}
    }

    @Override
    public void deleteByIds(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        baseMapper.deleteByIds(ids);
    }

    @Override
    public List<DmpPushTaskEntity> listByCodeParam(DmpSyncTaskDTO.ListCodeDTO listCodeDTO) {
        return lambdaQuery()
                .in(DmpPushTaskEntity::getSourceCode, listCodeDTO.getSourceCodeList())
                .eq(StringUtils.isNotBlank(listCodeDTO.getSourceType()), DmpPushTaskEntity::getSourceType, listCodeDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, listCodeDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, listCodeDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(listCodeDTO.getMqTopic()), DmpPushTaskEntity::getMqTopic, listCodeDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(listCodeDTO.getMqTag()), DmpPushTaskEntity::getMqTag, listCodeDTO.getMqTag())
                .list();
    }

    @Override
    public List<DmpPushTaskEntity> saveWdtTaskList(List<DmpPushTaskFeignDTO> dtoList) {
    	if(CollUtil.isEmpty(dtoList)) {
    		return null;
    	}
        List<String> sourceIdList = dtoList.stream().map(item -> item.getSourceId()).collect(Collectors.toList());
        DmpPushTaskServiceImpl bean = ApplicationContextUtils.getBean(DmpPushTaskServiceImpl.class);
        List<DmpPushTaskEntity> list = bean.queryList(sourceIdList);
        Map<String, DmpPushTaskEntity> sourceIdEntityMap = list.stream().collect(Collectors.toMap(item1 -> item1.getSourceId() + "#" + item1.getSourceType(), item1 -> item1, (o1,o2) -> o1));

        List<DmpPushTaskEntity> insertEntityList = new ArrayList<>();
        List<DmpPushTaskEntity> updateEntityList = new ArrayList<>();

        for (DmpPushTaskFeignDTO dto : dtoList) {
            DmpPushTaskEntity entity = new DmpPushTaskEntity();
            BeanMapper.copy(dto, entity);
            DmpSyncTaskDTO.OneDTO oneDTO = BeanMapperUtils.map(DmpSyncTaskDTO.OneDTO.class, entity);
            DmpPushTaskEntity found = sourceIdEntityMap.get(oneDTO.getSourceId() + "#" + oneDTO.getSourceType());
            //存在则修改
            if (ObjectUtil.isNotEmpty(found)) {
                entity.setId(found.getId());
                entity.setCreateTime(LocalDateTime.now());
                entity.setUpdateTime(LocalDateTime.now());
                updateEntityList.add(entity);
            }else {
            	insertEntityList.add(entity);
            }
        }
        if(CollUtil.isNotEmpty(insertEntityList)) {
        	bean.insertBatchList(insertEntityList);
        }
        if(CollUtil.isNotEmpty(updateEntityList)) {
        	bean.updateBatchList(updateEntityList);
        }

        insertEntityList.addAll(updateEntityList);
		return insertEntityList;
        }




    private void findWdtDataAndSendMq(List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList, String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList,sourceTypeEnum);
        switch (SourceTypeEnum.getEnum(sourceType)) {
            case OTHER_OUTSTOCK:
            case OTHER_INSTOCK:
                wmsTaskFeign.findWdtDataSendSyncTask(syncParamDTO);
                return;
            default:
                return;
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class , propagation = io.seata.tm.api.transaction.Propagation.NOT_SUPPORTED)
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.NOT_SUPPORTED)
    public List<DmpPushTaskEntity> queryList(List<String> sourceIdList){
    	return lambdaQuery()
                .in(DmpPushTaskEntity::getSourceId, sourceIdList)
//              .eq(DmpPushTaskEntity::getSourceType, SourceTypeEnum.OTHER_OUTSTOCK.getCode())
              .eq(DmpPushTaskEntity::getSourcePlatformName, PlatformEnum.ERP.getDesc())
              .eq(DmpPushTaskEntity::getTargetPlatformName, PlatformEnum.WANGDIAN.getDesc())
              .eq(DmpPushTaskEntity::getMqTopic, RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC)
//              .eq(DmpPushTaskEntity::getMqTag, RocketMqTagEnum.WDT_OTHER_OUT_STOCK_TAG.getName())
              .list();
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void insertBatchList(List<DmpPushTaskEntity> insertEntityList) {
    	this.saveBatch(insertEntityList);
    }

    @GlobalTransactional(rollbackFor = Exception.class , propagation = io.seata.tm.api.transaction.Propagation.NOT_SUPPORTED)
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.NOT_SUPPORTED)
    public void updateBatchList(List<DmpPushTaskEntity> updateEntityList) {
    	try {
			this.updateBatchById(updateEntityList);
		} catch (Exception e) {
			log.error("批量更新失败：{}" , JSON.toJSONString(updateEntityList) , e);
		}
    }
}

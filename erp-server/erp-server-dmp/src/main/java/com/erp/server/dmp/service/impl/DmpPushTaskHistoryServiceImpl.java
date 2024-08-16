package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskHistoryEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.mapper.DmpPushTaskHistoryMapper;
import com.erp.server.dmp.service.DmpPushTaskHistoryService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PUSH_TASK_HISTORY;

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
public class DmpPushTaskHistoryServiceImpl extends ServiceImpl<DmpPushTaskHistoryMapper, DmpPushTaskHistoryEntity> implements DmpPushTaskHistoryService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

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
        downloadTaskFeign.saveDownloadTask("中台推送任务历史表", EXPORT_PUSH_TASK_HISTORY.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSync(List<String> ids) {
        List<DmpPushTaskHistoryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        List<DmpPushTaskEntity> taskEntities = list.stream()
                .map(e -> {
                    DmpPushTaskEntity dmpPushTask = new DmpPushTaskEntity();
                    BeanUtils.copyProperties(e, dmpPushTask);
                    return dmpPushTask;
                })
                .collect(Collectors.toList());
        dmpPushTaskService.saveBatch(taskEntities);
        baseMapper.deleteBatchIds(ids);
        List<String> idList = taskEntities.stream()
                .map(BaseEntity::getId).collect(Collectors.toList());
        return dmpPushTaskService.batchSync(idList);
    }

    public static void sendMq(String mqData2, String id,Integer version, MQProducerService mqProducerService, String mqTopic, String mqTag, String sourceId) {
        String mqData = mqData2;
        JSONObject jsonObject = JSONUtil.parseObj(mqData);
        jsonObject.set("dmpSyncTaskId", id);
        jsonObject.set("version", version);
        SendResult result = mqProducerService.syncClassMsg(mqTopic, mqTag, JSONUtil.toJsonStr(jsonObject), sourceId);
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchFindDataSync(List<String> ids) {
        List<DmpPushTaskHistoryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        long count = list.stream().filter(obj -> !PlatformEnum.ERP.getDesc().equals(obj.getSourcePlatformName()) || !PlatformEnum.KINGDEE.getDesc().equals(obj.getTargetPlatformName())).count();
        if (count > 0) {
            throw new ServiceException(new ApiResult(10000, "只允许推送自研ERP>>>>金蝶的数据"));
        }
        List<DmpPushTaskEntity> entities = list.stream().map(entity -> {
            DmpPushTaskEntity e = new DmpPushTaskEntity();
            BeanUtils.copyProperties(entity, e);
            return e;
        }).collect(Collectors.toList());
        dmpPushTaskService.saveBatch(entities);
        baseMapper.deleteBatchIds(ids);
        Map<String, List<DmpPushTaskHistoryEntity>> map = list.stream().collect(Collectors.groupingBy(DmpPushTaskHistoryEntity::getSourceType));
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                for (Map.Entry<String, List<DmpPushTaskHistoryEntity>> entry : map.entrySet()) {
                    String sourceType = entry.getKey();
                    List<DmpPushTaskHistoryEntity> value = entry.getValue();
                    List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList = value.stream().map(obj -> new DmpSyncMqDTO.SyncParamDetailDTO(obj.getSourceId(), obj.getSyncOperate())).collect(Collectors.toList());
                    try {
                        // 发送MQ消息
                        findDataAndSendMq(paramDetailList, sourceType);
                    } catch (Exception e) {
                        String sourceTypeName = SourceTypeEnum.getName(sourceType);
                        log.error("从{}推送{}到{}发送消息异常", PlatformEnum.ERP.getDesc(), sourceTypeName, PlatformEnum.KINGDEE.getDesc(), e);
                    }
                }
            }
        });

        return Boolean.TRUE;
    }

    /**
     * @description: 重新查询数据发送MQ
     * @author Will
     * @date: 2023/10/30 10:03
     */
    private void findDataAndSendMq(List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList, String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList, sourceTypeEnum);
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
     * @param list
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     */
    private void doOpHandleDmpPushTask(List<DmpPushTaskDTO.ListDTO> list) {
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

    @Override
    public void syncPushTaskHistory() {
        log.info("开始归档3个月前同步成功的数据");
        int count = dmpPushTaskService.count(Wrappers.<DmpPushTaskEntity>lambdaQuery()
                .lt(DmpPushTaskEntity::getCreateTime, LocalDateTime.now().minusMonths(3))
                .in(DmpPushTaskEntity::getStatus, SyncStatusEnum.SUCCESS_SYNC.getCode(), SyncStatusEnum.NO_NEED_SYNC.getCode())
        );
        int pageSize = 500;
        int page = count / pageSize;
        List<CompletableFuture<List<String>>>  futures = new ArrayList<>();
        for (int i = 0; i <= page; i++) {
            int finalI = i;
            // 组装异步任务CompletableFuture
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
                //获取到今天3个月前同步成功的数据
                List<DmpPushTaskEntity> dmpPushTasks = dmpPushTaskService.list(Wrappers.<DmpPushTaskEntity>lambdaQuery()
                        .lt(DmpPushTaskEntity::getCreateTime, LocalDateTime.now().minusMonths(3))
                        .in(DmpPushTaskEntity::getStatus, SyncStatusEnum.SUCCESS_SYNC.getCode(), SyncStatusEnum.NO_NEED_SYNC.getCode())
                        .last(String.format("LIMIT %s OFFSET %s", pageSize, finalI * pageSize))
                );
                log.info("获取数据为第{}开始，到{}条", finalI * pageSize, (finalI + 1) * pageSize);
                if (CollectionUtil.isEmpty(dmpPushTasks)) {
                    return Collections.emptyList();
                }
                // 保存至历史表
                List<DmpPushTaskHistoryEntity> taskHistory = dmpPushTasks.stream().map(entity -> {
                    DmpPushTaskHistoryEntity history = new DmpPushTaskHistoryEntity();
                    BeanUtils.copyProperties(entity, history);
                    history.setId(null);
                    return history;
                }).collect(Collectors.toList());
                saveOrUpdateBatch(taskHistory);
                // 物理删除已经保存数据
                return dmpPushTasks.stream()
                        .map(DmpPushTaskEntity::getId)
                        .collect(Collectors.toList());
            }, threadPoolTaskExecutor);
            futures.add(future);
        }
        // 批量执行异步任务
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        // 等待任务执行完毕，分批删除原表数据
        allFuture.thenRun(() ->{
            for (CompletableFuture<List<String>> future : futures) {
                try {
                    List<String> ids = future.get();
                    dmpPushTaskService.deleteByIds(ids);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("执行失败，请重试");
                    Thread.currentThread().interrupt();
                }
            }
        });
        log.info("完成归档3个月前同步成功的数据");
    }

    @Override
    public PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        Page<DmpPushTaskDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }
}

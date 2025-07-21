package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPullTaskHistoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpPullTaskHistoryMapper;
import com.erp.server.dmp.service.DmpPullTaskHistoryService;
import com.erp.server.dmp.service.DmpPullTaskService;
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
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PULL_TASK_HISTORY;

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
public class DmpPullTaskHistoryServiceImpl extends ServiceImpl<DmpPullTaskHistoryMapper, DmpPullTaskHistoryEntity> implements DmpPullTaskHistoryService {

    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


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
        downloadTaskFeign.saveExportTask("中台拉取任务历史表", EXPORT_PULL_TASK_HISTORY.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSync(List<String> ids) {
        List<DmpPullTaskHistoryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        // 移除历史表数据新增新表数据
        List<DmpPullTaskEntity> entities = list.stream().map(entity -> {
            DmpPullTaskEntity e = new DmpPullTaskEntity();
            BeanUtils.copyProperties(entity, e);
            return e;
        }).collect(Collectors.toList());
        dmpPullTaskService.saveBatch(entities);
        baseMapper.deleteBatchIds(ids);
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                for (DmpPullTaskHistoryEntity dmpPullTaskEntity : list) {
                    // 发送推送同步任务消息
                    JSONObject jsonObject = JSONUtil.parseObj(dmpPullTaskEntity.getMqData());
                    jsonObject.set("dmpSyncTaskId", dmpPullTaskEntity.getId());
                    SendResult result = mqProducerService.syncClassMsg(dmpPullTaskEntity.getMqTopic(), dmpPullTaskEntity.getMqTag(), jsonObject, dmpPullTaskEntity.getSourceId());
                    if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                        throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                    }
                }
            }
        });
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
    public void syncPullTaskHistory(Integer month) {

        LocalDateTime date = LocalDateTime.now().minusMonths(ObjectUtils.isEmpty(month) ? 3 : month);
        log.info("开始归档3个月前拉取成功的数据");
        int count = dmpPullTaskService.countMonth(date);
        int pageSize = 500;
        int page = count / pageSize + 1;
        List<CompletableFuture<List<String>>>  futures = new ArrayList<>();
        for (int i = 0; i <= page; i++) {
            int finalI = i;
            // 组装异步任务CompletableFuture
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
                //获取到今天3个月前同步成功的数据
                List<DmpPullTaskEntity> dmpPullTasks = dmpPullTaskService.listMonth(date, pageSize, finalI * pageSize);
                if (CollectionUtil.isEmpty(dmpPullTasks)) {
                    return new ArrayList<>();
                }
                // 保存至历史表
                List<DmpPullTaskHistoryEntity> taskHistory = dmpPullTasks.stream().map(entity -> {
                    DmpPullTaskHistoryEntity history = new DmpPullTaskHistoryEntity();
                    BeanUtils.copyProperties(entity, history);
                    history.setId(null);
                    return history;
                }).collect(Collectors.toList());
                saveOrUpdateBatch(taskHistory);
                return dmpPullTasks.stream()
                        .map(DmpPullTaskEntity::getId)
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
                    dmpPullTaskService.deleteByIds(ids);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("执行失败，请重试");
                    Thread.currentThread().interrupt();
                }
            }
        });
        log.info("完成归档3个月前拉取成功的数据");
    }

    @Override
    public PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        Page<DmpPullTaskDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }
}

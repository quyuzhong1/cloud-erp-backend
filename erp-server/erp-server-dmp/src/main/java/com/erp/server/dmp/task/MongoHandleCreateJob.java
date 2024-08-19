package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.MongoJobParamDTO;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.server.dmp.enums.DmpMongoHandleTypeEnum;
import com.erp.server.dmp.factory.DmpMongoHandlerFactory;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp_mongo_handle_task任务
 */
@Component
@Slf4j
public class MongoHandleCreateJob {

    @Resource
    private DmpMongoHandleTaskService dmpMongoHandleTaskService;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    /**
     * 处理mongo业务数据任务
     *
     * @Author Jim
     * @Date 2024/05/28
     **/
    @XxlJob("mongoHandleJob")
    public void mongoHandleJob() {
        String jobParam = XxlJobHelper.getJobParam();
        int threadCount = 0;
        Boolean queryIsAddOrUpdate;
        // {"queryIsAddOrUpdate":false, "threadCount":0}
        if (StringUtils.isNotBlank(jobParam)) {
            JSONObject jsonObject = new JSONObject(jobParam);
            threadCount = jsonObject.getInt("threadCount", 0);
            queryIsAddOrUpdate = jsonObject.getBool("queryIsAddOrUpdate", null);
        } else {
            queryIsAddOrUpdate = true;
        }
        XxlJobHelper.log("mongoHandleJob 执行任务列表开始, 指定执行线程数={}(0=无指定), 跳过历史已有数据={}", threadCount, queryIsAddOrUpdate);
        List<DmpMongoHandleTaskEntity> list = dmpMongoHandleTaskService.list();
        if (CollectionUtil.isEmpty(list)) {
            XxlJobHelper.log("mongoHandleJob 需要执行任务列表为空");
            return;
        }
        int runCount = threadCount > 0 ? threadCount : list.size();
        // 按照指定线程数分组
        List<List<DmpMongoHandleTaskEntity>> partition = Lists.partition(list, runCount);

        for (List<DmpMongoHandleTaskEntity> curList : partition) {
            curList.parallelStream().forEach(taskEntity -> {
//            for (DmpMongoHandleTaskEntity taskEntity : curList) {
//                threadPoolTaskExecutor.execute(() -> {
                try {
                    DmpMongoHandleTypeEnum handleTypeEnum = DmpMongoHandleTypeEnum.getByCode(taskEntity.getHandleType(), true);
                    // 指定根据业务类型和当前条件查询对应mongo表处理
                    Integer handleResultCount = DmpMongoHandlerFactory.createHandler(handleTypeEnum).findAndFillDataOrHandle(taskEntity, queryIsAddOrUpdate);
                    XxlJobHelper.log("mongoHandleJob 当前任务执行成功：handleType={}, 当前处理数量={}", taskEntity.getHandleType(), handleResultCount);
                } catch (Exception e) {
                    log.error("mongoHandleJob 当前任务执行成功异常：handleType={}, error={}",
                            taskEntity.getHandleType(),
                            ExceptionUtil.stacktraceToString(e)
                    );
                    XxlJobHelper.log("mongoHandleJob 当前任务执行成功异常：handleType={}, error={}",
                            taskEntity.getHandleType(),
                            ExceptionUtil.stacktraceToString(e)
                    );
                }
            });
//            }
        }
        XxlJobHelper.log("mongoHandleJob 执行任务列表结束");
    }

    /**
     * 清理mongo业务历史数据任务 (默认清理3个月之前的数据)
     *
     * @Author Jim
     * @Date 2024/06/17
     **/
    @XxlJob("mongoHistoryClearJob")
    public void mongoHistoryClearJob() {
        // 每次清理记录数
        Integer size = 1000;
        // 清理的历史天数
        Integer clearHistoryDay = 90;
        String jobParam = XxlJobHelper.getJobParam();
        List<MongoJobParamDTO.ClearDTO> paramsList = Collections.emptyList();
        if (StringUtils.isNotBlank(jobParam)) {
            paramsList = JSONUtil.toList(jobParam, MongoJobParamDTO.ClearDTO.class);
        }
        XxlJobHelper.log("mongoHistoryClearJob 【清理mongo业务历史数据任务】执行任务列表开始, 执行参数={}", jobParam);
        if (CollectionUtil.isEmpty(paramsList)) {
            List<DmpMongoHandleTaskEntity> list = dmpMongoHandleTaskService.list();
            paramsList = list.stream().map(e -> new MongoJobParamDTO.ClearDTO(size, e.getHandleType(), clearHistoryDay)).collect(Collectors.toList());
        }
        if (CollectionUtil.isEmpty(paramsList)) {
            XxlJobHelper.log("mongoHistoryClearJob 【清理mongo业务历史数据任务】 需要执行任务列表为空");
            return;
        }
        paramsList.parallelStream().forEach(param -> {
            try {
                LocalDateTime historyDateTime = LocalDateTime.now().minusDays(param.getClearHistoryDay());
                DmpMongoHandleTypeEnum handleTypeEnum = DmpMongoHandleTypeEnum.getByCode(param.getHandleType(), true);
                Long delSize = dmpMongoHandleTaskService.clearHistory(handleTypeEnum, historyDateTime, param.getSize());
                // 指定根据业务类型和当前条件查询对应mongo表处理
                XxlJobHelper.log("mongoHistoryClearJob 当前任务执行成功：handleType={}, 当前指定处理数量={}, 已删除数量={}", param.getHandleType(), param.getSize(), delSize);
            } catch (Exception e) {
                XxlJobHelper.log("mongoHistoryClearJob 当前任务执行成功异常：handleType={}, error={}",
                        param.getHandleType(),
                        ExceptionUtil.stacktraceToString(e)
                );
                log.error("mongoHistoryClearJob 当前任务执行成功异常：handleType={}, error={}",
                        param.getHandleType(),
                        ExceptionUtil.stacktraceToString(e));
            }
        });
        XxlJobHelper.log("mongoHistoryClearJob 【清理mongo业务历史数据任务】执行任务列表结束");
    }

}

package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONObject;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.server.dmp.enums.DmpMongoHandleTypeEnum;
import com.erp.server.dmp.factory.DmpMongoHandlerFactory;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
        if (StringUtils.isNotBlank(jobParam)) {
            threadCount = new JSONObject(jobParam).getInt("threadCount");
        }
        XxlJobHelper.log("mongoHandleJob 执行任务列表开始, 指定执行线程数={},(0=无指定)", threadCount);
        List<DmpMongoHandleTaskEntity> list = dmpMongoHandleTaskService.list();
        if (CollectionUtil.isEmpty(list)) {
            XxlJobHelper.log("mongoHandleJob 需要执行任务列表为空");
            return;
        }
        int runCount = threadCount > 0 ? threadCount : list.size();
        // 按照指定线程数分组
        List<List<DmpMongoHandleTaskEntity>> partition = Lists.partition(list, runCount);

        for (List<DmpMongoHandleTaskEntity> curList : partition) {
            for (DmpMongoHandleTaskEntity taskEntity : curList) {
                threadPoolTaskExecutor.execute(() -> {
                    try {
                        DmpMongoHandleTypeEnum handleTypeEnum = DmpMongoHandleTypeEnum.getByCode(taskEntity.getHandleType(), true);
                        // 指定根据业务类型和当前条件查询对应mongo表处理
                        Integer handleResultCount = DmpMongoHandlerFactory.createHandler(handleTypeEnum).findAndFillDataOrHandle(taskEntity);
                        XxlJobHelper.log("mongoHandleJob 当前任务执行成功：handleType={}, 当前处理数量={}", taskEntity.getHandleType(), handleResultCount);
                    } catch (Exception e) {
                        XxlJobHelper.log("mongoHandleJob 当前任务执行成功异常：handleType={}, error={}",
                                taskEntity.getHandleType(),
                                ExceptionUtil.stacktraceToString(e)
                        );
                    }
                });
            }
        }
        XxlJobHelper.log("mongoHandleJob 执行任务列表结束");
    }


}

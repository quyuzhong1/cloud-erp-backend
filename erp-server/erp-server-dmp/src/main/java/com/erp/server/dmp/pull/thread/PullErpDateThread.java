package com.erp.server.dmp.pull.thread;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.handler.SaveHandler;
import com.common.business.service.ModelService;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PullErpDateThread {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private ModelService modelService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Autowired
    private RedisTemplate<String, String> template;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MQProducerService mqProducerService;


    @Async("pullErpOpenApi")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            log.info("发起异步{}调用任务{}", dto.getJobTaskDTO().getDictPlatform(),dto.getJobTaskDTO().getApiName());
            modelService.pullDataSave(dto);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 0);
            if (!aBoolean) {
                throw new RuntimeException("修改任务下次执行时间失败！");
            }
        } catch (Exception e) {
            log.error(" {}拉取数据错误dto={}", jobTaskDTO.getDictPlatform(), JSONUtil.toJsonStr(dto), e);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 1);
            String message = e.getMessage();
            if (!aBoolean) {
                message = "更新任务状态失败";
            }
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(jobTaskDTO.getId(), JSONUtil.toJsonStr(dto),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
            // 发送预警
            sendWarnMsg(dmpErrorLogEntity, jobTaskDTO);
        }
    }

    /***
     * 发送预警信息
     */
    public void sendWarnMsg(DmpErrorLogEntity dmpErrorLogEntity, JobTaskDTO jobTaskDTO) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(jobTaskDTO.getApiName());
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle(StrUtil.format("【{}】从{}拉取至{}失败",jobTaskDTO.getApiName(),jobTaskDTO.getDictPlatform(),"ERP"));
        warnMsgInfo.setTableName("dmp_pull_task");
        warnMsgInfo.setTableId(dmpErrorLogEntity.getTaskId());
        warnMsgInfo.setKeyInfo(dmpErrorLogEntity.getReturnMsg());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    public void executeTask(String taskName) {
        // 获取请求任务
        String o = template.opsForList().rightPop(taskName);
        if(ObjectUtils.isEmpty(o) || "null".equals(o)) {
            return;
        }
        JobTaskDTO orderJobTask = JSONObject.parseObject(o, JobTaskDTO.class);
        if (orderJobTask == null) {
            return;
        }
        if(ObjectUtils.isEmpty(template.opsForValue().get(orderJobTask.getApiCode()+""))) {
            pullOrder(orderJobTask);
            //redis调用时间限制 马帮必须限制 调用频率
            template.opsForValue().set(orderJobTask.getApiCode()+"","text",10, TimeUnit.SECONDS);
        }else {
            template.opsForList().leftPush(taskName, JSONObject.toJSONString(orderJobTask));
        }
    }


    public void executeCleanTask(String taskKey, List<String> finalTaskList) {
        // 任务列表
        List<String> tableListByTask = MongoTableNameContant.getTableListByTask(taskKey);
        if(CollectionUtil.isNotEmpty(finalTaskList)){
            tableListByTask = finalTaskList;
        }
        if (CollectionUtil.isEmpty(tableListByTask)) {
            return;
        }
        for (String tableName : tableListByTask) {
            try {
                threadPoolTaskExecutor.execute(()->{
                    // 记录执行时间
                    // 执行完毕后，更新执行时间
                    String key = StrUtil.format("{}:{}", "clean", taskKey);
                    redisUtil.hset(key, tableName, LocalDateTime.now());
                    // 清理数据
                    SaveHandler.cleanDataSave(tableName);
                });
            }catch (Exception e) {
                log.error("清理数据错误tableName={}", tableName, e);
                XxlJobHelper.log("清理数据错误tableName={}", tableName, e);
            }
        }

    }
}

package com.erp.server.dmp.pull.thread;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.ModelService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
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


    @Async("pullErpOpenApi")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            log.info("发起异步{}调用任务{}", dto.getJobTaskDTO().getPlatformName(),dto.getJobTaskDTO().getApiName());
            modelService.pullDataSave(dto);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 0);
            if (!aBoolean) {
                throw new RuntimeException("修改任务下次执行时间失败！");
            }
        } catch (Exception e) {
            log.error(" {}拉取数据错误dto={}", jobTaskDTO.getPlatformName(), JSONUtil.toJsonStr(dto), e);
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO, 1);
            String message = e.getMessage();
            if (!aBoolean) {
                message = "更新任务状态失败";
            }
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(jobTaskDTO.getId(), JSONUtil.toJsonStr(dto),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
        }
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
}

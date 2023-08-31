package com.erp.server.dmp.pull.thread;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.ModelService;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 拉取平台数据线程
 * @author Cloud
 */
@Component
@Slf4j
public class PlatformDataThread {
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    private RedisTemplate<String, String> template;
    @Resource
    private BusinessServiceImpl businessService;


    @Async("pullErpOpenApi")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            log.info("发起异步调用平台【{}】店铺【{}】任务【{}】", dto.getJobTaskDTO().getDictPlatform(),dto.getJobTaskDTO().getShopName(), dto.getJobTaskDTO().getApiName());
            businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(),jobTaskDTO.getDictPlatform(),jobTaskDTO.getBusinessType(), jobTaskDTO);
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
        }
    }

    /**
     * 执行任务
     * @param taskName
     */
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
        String taskKey = StrUtil.format("{}-{}-{}", orderJobTask.getDictPlatform(), orderJobTask.getShopId(), orderJobTask.getApiCode());
        if(ObjectUtils.isEmpty(template.opsForValue().get(taskKey))) {
            pullOrder(orderJobTask);
            template.opsForValue().set(taskKey,"text",10, TimeUnit.SECONDS);
        }else {
            template.opsForList().leftPush(taskName, JSONObject.toJSONString(orderJobTask));
        }
    }

}

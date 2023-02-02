package com.erp.server.dmp.pull.thread;

import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.ModelService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class PullGyyDateThread {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private ModelService modelService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Async("gyy")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            modelService.pullDataSave(dto);
        } catch (Exception e) {
            log.error(" 管易云拉取数据错误dto={}", JSONUtil.toJsonStr(dto), e);
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(jobTaskDTO.getId(), JSONUtil.toJsonStr(dto),e.getMessage(), JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
            return;
        }
        // 修改任务信息
        Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO);
        if (!aBoolean) {
            throw new RuntimeException("修改任务下次执行时间失败！");
        }
    }
}

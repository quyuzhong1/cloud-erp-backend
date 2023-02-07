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
public class PullErpDateThread {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private ModelService modelService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Async("pullErpOpenApi")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
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
}

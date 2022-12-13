package com.erp.server.dmp.pull.thread;

import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.ModelService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class PullMabangDateThread {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private ModelService modelService;

    @Async("mabang")
    public void pullOrder(JobTaskDTO jobTaskDTO) {
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);
        try {
            modelService.pullDataSave(dto);
            // 修改任务信息
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(jobTaskDTO);
            if (!aBoolean) {
                throw new RuntimeException("修改任务下次执行时间失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

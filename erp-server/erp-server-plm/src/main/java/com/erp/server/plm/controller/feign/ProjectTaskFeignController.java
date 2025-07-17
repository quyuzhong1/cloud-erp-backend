package com.erp.server.plm.controller.feign;

import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskFollowerEntity;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskFollowerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 任务列表
 * @author Will
 * @date: 2023/10/19 11:05
 */
@RestController
@RequestMapping("feign/projectTask")
public class ProjectTaskFeignController {

    @Resource
    private ProjectTaskService projectTaskService;

    @Resource
    private TaskFollowerService taskFollowerService;

    /**
     * 更新任务列表负责人名称
     * @author Will
     * @date: 2023/10/19 11:07
     * @param sysUserInfoDTO
     */
    @PostMapping("/updateProjectTaskChargeName")
    public void updateProjectTaskChargeName(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO) {
        projectTaskService.updateProjectTaskChargeName(sysUserInfoDTO);
    }

    /**
     * @description:
     * @author jack
     * @date: 2025-05-30
     * @param preTaskIds
     */
    @PostMapping("/listProjectTaskByTaskIds")
    public List<ProjectTaskEntity> listProjectTaskByTaskIds(@RequestBody List<String> preTaskIds){
        return projectTaskService.listByTaskIds(preTaskIds);
    }
    /**
     * @description:
     * @author jack
     * @date: 2025-05-30
     * @param preTaskIds
     */
    @PostMapping("/listTaskFollowerByTaskIds")
    public List<TaskFollowerEntity> listTaskFollowerByTaskIds(@RequestBody List<String> preTaskIds){
        return taskFollowerService.listByTaskIds(preTaskIds);
    }

}

package com.erp.server.plm.controller.feign;

import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}

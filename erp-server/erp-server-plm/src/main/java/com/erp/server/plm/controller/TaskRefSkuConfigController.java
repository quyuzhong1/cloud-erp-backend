package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.common.business.dto.base.BaseIdDTO;
import com.erp.server.plm.service.TaskRefSkuConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 任务sku配置关系表(TaskRefSkuConfig)表控制层
 *
 * @author Lambda
 * @since 2022-11-21 14:01:00
 */
@RestController
@RequestMapping("taskRefSkuConfig")
public class TaskRefSkuConfigController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private TaskRefSkuConfigService taskRefSkuConfigService;




    /**
     * 删除数据
     *
     * @param dto 主键
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = this.taskRefSkuConfigService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}


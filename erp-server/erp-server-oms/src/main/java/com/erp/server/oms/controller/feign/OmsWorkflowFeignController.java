package com.erp.server.oms.controller.feign;

import cn.hutool.json.JSONUtil;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.web.bind.annotation.*;

/**
 * oms工作流feign
 *
 * @Author Cloud
 * @Date 2023/6/28 9:32
 **/
@RestController
@RequestMapping("feign/omsWorkflow")
public class OmsWorkflowFeignController {

    @PostMapping("/approveEnd")
    public void approveEnd(@RequestBody EndProcessDTO dto) {
        System.out.println("dto = " + JSONUtil.toJsonStr(dto));
    }
}

package com.erp.rpc.workflow;

import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Classname WorkflowFeign
 * @Description TODO
 * @Date 2022-10-18 17:00
 * @Created by yl
 */
@FeignClient("erp-workflow")
public interface WorkflowFeign {


    //启动流程
    @GetMapping("workflow/feign/process/startProcess")
    ProcessNodeDTO startProcess(@RequestBody StartProcessDTO startProcessDTO);
}

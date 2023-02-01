package com.erp.server.workflow.controller.api;

import com.common.core.utils.OkHttpUtils;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.workflow.dto.FindProcessDTO;
import com.erp.model.workflow.dto.WorkflowBusinessDTO;
import com.erp.model.workflow.vo.WorkflowBusinessVO;
import com.erp.server.workflow.service.WorkflowBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname WorkflowBusinessController
 * @Description TODO
 * @Date 2023-01-30 15:37
 * @Created by yl
 */

@RestController
@RequestMapping("workflow/business")
@Slf4j
public class WorkflowBusinessController extends BaseController {

    @Resource
    private WorkflowBusinessService workflowBusinessService;


    /**
     * 获取流程名及相关信息
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<WorkflowBusinessVO>> getProcessList(@RequestBody @Validated FindProcessDTO dto) {
        List<WorkflowBusinessVO> resultList = workflowBusinessService.getBusinessList(dto);
        return success(resultList);
    }


    /**
     * 保存流程信息
     *
     * @return
     */
    @PostMapping("/save")
    public ApiResult saveProcess(@RequestBody WorkflowBusinessDTO dto) {
        Boolean flag = workflowBusinessService.saveBusiness(dto);
        return flag == true ? success() : failure();
    }


    @PostMapping("/test")
    public ApiResult test() {
        String bomProcessPassUrl = "http://172.16.110.187:9050/plm/bom/workflow/pass";
        Map<String, Object> params = new HashMap<>();
        params.put("processId", "");
        params.put("businessTableId", "1620668344204484610");
        OkHttpUtils.doPostJson(bomProcessPassUrl, params, null);
        return success();
    }

}

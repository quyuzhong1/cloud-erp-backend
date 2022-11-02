package com.erp.rpc.plm.feign;

import com.erp.common.modules.sys.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * plm 远程调用接口
 * @Classname PlmTaskFeign
 * @Description TODO
 * @Date 2022-10-21 9:06
 * @Created by yl
 */
@FeignClient("erp-plm")
public interface PlmTaskFeign {

    //流程审核通过 改变任务状态
    @PostMapping("plm/task/feign/process/pass")
    void processPass(@RequestBody String processId);
}

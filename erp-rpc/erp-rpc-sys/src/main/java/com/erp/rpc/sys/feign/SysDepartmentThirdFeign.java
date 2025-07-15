package com.erp.rpc.sys.feign;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/19 18:30
 */

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.entity.SysDepartmentThirdEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-19
 *@Description:
 *@Version: 1.0
 */
@FeignClient(name = "erp-sys", contextId = "sysDepartmentThird",configuration = {FeignErrorDecoder.class})
public interface SysDepartmentThirdFeign {

    @GetMapping("feign/sysDepartmentThird/findByDepartmentId")
    SysDepartmentThirdEntity findByDepartmentId(@RequestParam(value = "platform") String platform, @RequestParam(value = "departmentId")String departmentId);
}

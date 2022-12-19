package com.erp.rpc.sys.feign;

import com.erp.common.dto.base.ApiResult;
import com.erp.common.modules.sys.dto.AccountLoginDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统管理-部门相关信息
 *
 * @Author Cloud
 * @Date 2022/12/19 9:42
 **/
@FeignClient("erp-sys")
public interface SysDeptFeign {
    @PostMapping("sys/feign/user/accountLogin")
    ApiResult<SysUserDTO> accountLogin();

}

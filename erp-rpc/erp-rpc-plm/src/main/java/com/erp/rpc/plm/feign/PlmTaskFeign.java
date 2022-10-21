package com.erp.rpc.plm.feign;

import com.erp.common.modules.sys.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    //获取部门的用户
    @PostMapping("sys/feign/user/getDepUserList")
    List<SysUserDTO> getDepUserList(@RequestBody String userId);
}

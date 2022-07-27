package com.cloud.erp.admin.modules.feign;

import com.cloud.erp.common.modules.sys.dto.FindThirdUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Classname ThirdFeign
 * @Description TODO
 * @Date 2022-07-21 11:56
 * @Created by yl
 */
@FeignClient("erp-third-party")
public interface ThirdFeign {

    //设置登录ip账号登录
    @PostMapping("third/feign/fs/getUser")
    Map<String,Object> getFsUser(@RequestBody FindThirdUserDTO dto);
}

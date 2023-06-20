package com.erp.rpc.sys.feign;

import com.common.business.dto.FindUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Lambda
 * @Classname UserInfoFeign
 * @Description TODO
 * @Date 2023-06-19 19:52
 * @Created by yl
 */
@FeignClient(name = "erp-sys", contextId = "user")
public interface UserInfoFeign {





    @GetMapping("feign/user/listThirdBindUser")
    List<FindUserDTO> listThirdBindUserInfo();
}

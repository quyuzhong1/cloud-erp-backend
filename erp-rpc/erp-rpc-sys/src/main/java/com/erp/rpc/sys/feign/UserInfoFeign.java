package com.erp.rpc.sys.feign;

import com.common.business.dto.FindUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

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



        
    /**
     * 获取第三方绑定的用户信息
     * @author yl
     * @date 2023-06-20 10:16
     * @param
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    @GetMapping("feign/user/listThirdBindUser")
    List<FindUserDTO> listThirdBindUserInfo();

    /**
     * 根据用户ids获取用户信息
     * @param userIds
     * @return
     */
    @GetMapping("feign/user/getUserListByUserIds")
    List<FindUserDTO> listByUserIds(@RequestBody List<String> userIds);
}

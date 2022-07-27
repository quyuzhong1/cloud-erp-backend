package com.cloud.erp.thirdparty.controller.feign;

import com.cloud.erp.common.common.BaseController;
import com.cloud.erp.common.modules.sys.dto.FindThirdUserDTO;
import com.cloud.erp.thirdparty.controller.service.FsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Classname FsFeignController
 * @Description TODO
 * @Date 2022-07-20 17:48
 * @Created by yl
 */
@RestController
@RequestMapping("third/feign/fs")
public class FsFeignController extends BaseController {



    @Autowired
    private FsService fsService;

    @ResponseBody
    @PostMapping("/getUser")
    public Map getOauthToken(@RequestBody  FindThirdUserDTO dto) {
        Map<String,Object > userMap = fsService.getFsUser(dto);
        return userMap;
    }
}

package com.erp.server.sys.controller.api;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.vo.UserFieldVO;
import com.erp.server.sys.service.CustomizeFieldLayoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Classname CustomizeFieldFeignController
 * @Description TODO
 * @Date 2023-02-08 16:07
 * @Created by yl
 */
@RestController
@RequestMapping("sys/customize/field")
public class CustomizeFieldLayoutController extends BaseController {

    @Resource
    private CustomizeFieldLayoutService customizeFieldLayoutService;


    @PostMapping("/fieldSet")
    public Boolean saveHiddenField(@RequestBody CustomizeFieldLayoutDTO dto) {
        Boolean result = customizeFieldLayoutService.add(dto);
        return result;
    }

    @PostMapping("/getByUserId")
    public UserFieldVO getByUserId(@RequestBody FindCustomizeFieldDTO  dto) {
        UserFieldVO vo = customizeFieldLayoutService.getByUserId(dto);
        return vo;
    }
}

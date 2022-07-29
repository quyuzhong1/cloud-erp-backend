package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.entity.SysBaseDicEntity;
import com.cloud.erp.admin.modules.sys.service.SysBaseDicService;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.dto.BaseDicDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname SysBaseDicController
 * @Description TODO
 * @Date 2022-07-20 17:03
 * @Created by yl
 */
@RestController
@RequestMapping("sys/dic")
public class SysBaseDicController extends BaseController {

    @Autowired
    private SysBaseDicService sysBaseDicService;

    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated BaseDicDTO dto){
       List<SysBaseDicEntity> list= sysBaseDicService.listByDicType(dto);
       return success(list);
    }
}

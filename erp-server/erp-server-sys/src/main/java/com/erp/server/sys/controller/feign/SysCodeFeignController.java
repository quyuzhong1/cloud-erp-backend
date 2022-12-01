package com.erp.server.sys.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.server.sys.service.SysCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 19:15
 */
@RestController
@RequestMapping("sys/feign/code")
public class SysCodeFeignController extends BaseController {

    @Autowired
    private SysCodeService sysCodeService;

    /**
     * 根据编码信息生成对应的系统编号
     *
     * @author Will
     * @date: 2022/11/21 19:18
     * @param dto
     * @return String
     */
    @PostMapping("/getSysCode")
    public String getSysCode(@RequestBody SysCodeDTO dto) {
        String sysCode = sysCodeService.getSysCode(dto);
        return sysCode;
    }
}

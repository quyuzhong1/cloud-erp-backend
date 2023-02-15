package com.erp.server.sys.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
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
     * 根据编码信息生成sku编号
     * @author Will
     * @date: 2022/11/21 19:18
     * @param dto
     * @return String
     */
    @PostMapping("/getSkuNo")
    public String getSkuNo(@RequestBody SysCodeSkuDTO dto) {
        String sysCode = sysCodeService.getSkuNo(dto);
        return sysCode;
    }

    /**
     * 根据编码信息生成spu编号
     * @author Will
     * @date: 2023/1/7 10:18
     * @param dto
     * @return String
     */
    @PostMapping("/getSpuNo")
    public String getSpuNo(@RequestBody SysCodeDTO dto) {
        String sysCode = sysCodeService.getSpuNo(dto);
        return sysCode;
    }
}

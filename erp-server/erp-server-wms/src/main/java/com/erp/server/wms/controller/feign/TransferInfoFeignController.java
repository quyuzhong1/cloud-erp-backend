package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.server.wms.service.TransferInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * feign控制器
 *
 */
@RestController
@RequestMapping("/feign/transferInfo")
public class TransferInfoFeignController extends BaseController{

    @Resource
    private TransferInfoService transferInfoService;

    /**
     * 保存并审核
     * @author zdy
     */
    @PostMapping("/addAndApprove")
    public String addAndApprove(@RequestBody @Validated TransferInfoDTO.AddDTO dto){
        return transferInfoService.addAndApprove(dto);
    }
}
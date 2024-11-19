package com.erp.server.dmp.controller.feign;

import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.service.DmpSoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("feign/dmp")
public class DmpSoInfoFeignController {
    @Resource
    private DmpSoInfoService dmpSoInfoService;

    @GetMapping("/shudiyunFieldDmpOrderHandler")
    public List<ShudiyunB2cOrderDTO> shudiyunFieldDmpOrderHandler(@RequestParam("platformCode") String platformCode) {
        return dmpSoInfoService.shudiyunFieldDmpOrderHandler(platformCode);
    }
}

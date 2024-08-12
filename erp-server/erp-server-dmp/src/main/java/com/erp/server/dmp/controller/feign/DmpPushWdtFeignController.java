package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.server.dmp.service.DmpPushWdtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 推送旺店通中间表Feign
 * @date 2024-07-24
 * @author tanmujin
 */
@Slf4j
@RestController
@RequestMapping("/feign/dmpPushWdt")
public class DmpPushWdtFeignController {

    @Resource
    private DmpPushWdtService dmpPushWdtService;

    @PostMapping("/addBatch")
    public Boolean addBatch(@RequestBody List<DmpPushWdtDTO.AddDTO> dtoList){
        return dmpPushWdtService.addBatch(dtoList);
    }
}

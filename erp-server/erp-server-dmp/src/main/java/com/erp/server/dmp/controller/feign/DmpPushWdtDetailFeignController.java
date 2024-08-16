package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.server.dmp.service.DmpPushWdtDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * @date 2024-07-25
 * @author tanmujin
 */
@Slf4j
@RestController
@RequestMapping("/feign/dmpPushWdtDetail")
public class DmpPushWdtDetailFeignController {

    @Resource
    private DmpPushWdtDetailService dmpPushWdtDetailService;

    @PostMapping("/addBatch")
    public Boolean addBatch(@RequestBody List<DmpPushWdtDetailDTO> dtoList){
        return dmpPushWdtDetailService.addBatch(dtoList);
    }
}

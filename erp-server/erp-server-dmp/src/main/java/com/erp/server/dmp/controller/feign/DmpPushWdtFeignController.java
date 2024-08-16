package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.server.dmp.service.DmpPushWdtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add")
    public String add(@RequestBody DmpPushWdtDTO.AddDTO dto){
        return dmpPushWdtService.add(dto);
    }

    /**
     * 根据ID查询中间表数据
     * @param ids
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    @GetMapping("/listByIds")
    List<DmpPushWdtDTO.ViewDTO> listByIds(List<String> ids){
//        return dmpPushWdtService.listByIds(ids);
        return dmpPushWdtService.listByIdList(ids);
    }
}

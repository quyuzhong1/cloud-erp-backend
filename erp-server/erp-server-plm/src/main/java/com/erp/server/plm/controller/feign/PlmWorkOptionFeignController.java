package com.erp.server.plm.controller.feign;

import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.plm.service.WorkOptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("feign/plmWorkOption")
public class PlmWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public Integer getTableNum(@RequestBody WorkOptionDTO.TableNumDTO tableNumDTO) {
        return workOptionService.getTableNum(tableNumDTO);
    }
}

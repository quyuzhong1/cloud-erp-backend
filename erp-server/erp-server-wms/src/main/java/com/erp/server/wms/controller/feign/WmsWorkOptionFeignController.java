package com.erp.server.wms.controller.feign;

import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.service.WorkOptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/wmsWorkOption")
public class WmsWorkOptionFeignController {
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

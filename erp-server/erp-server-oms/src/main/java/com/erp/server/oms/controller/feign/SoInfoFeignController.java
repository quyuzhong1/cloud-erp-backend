package com.erp.server.oms.controller.feign;

import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购单
 * @Author Luo_WG
 * @Date 2023/5/15 9:12
 **/
@RestController
@RequestMapping("feign/soInfo")
public class SoInfoFeignController {
    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    @PostMapping("/getSoInfoById")
    public SoInfoEntity getSoInfoById(@RequestBody String id) {
        return soInfoService.getById(id);
    }

    @PostMapping("/listSoDetailByIds")
    public List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids) {
        return soDetailService.listSoDetailByIds(ids);
    }
}

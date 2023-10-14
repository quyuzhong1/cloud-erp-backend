package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soReturnInstock")
public class SoReturnInstockFeignController {

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    /**
     * 获取退货入库单
     *
     * @param id
     * @return
     */
    @PostMapping("/getSoReturnInstockById")
    SoReturnInstockEntity getSoReturnInstockEntityById(@RequestParam(value = "id") String id) {
        return soReturnInstockService.getById(id);
    }

    /**
     * 获取退货入库单 明细
     *
     * @param mainId
     * @return
     */
    @PostMapping("/getSoReturnInstockByMainId")
    List<SoReturnInstockDetailEntity> getSoReturnInstockDetailByMainId(@RequestParam(value = "mainId") String mainId) {
        return soReturnInstockDetailService.listDetailByMainId(mainId);
    }
}
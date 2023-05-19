package com.erp.server.wms.controller.feign;

import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soReturnReceive")
public class SoReturnReceiveFeignController {

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    /**
     * 根据详情id查询退货签收单详情
     * @Author Luo_WG
     * @Date 2023/5/19 12:03
     * @param ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    @PostMapping("/listSoDetailByIds")
    public List<SoReturnReceiveDetailEntity> listSoDetailByIds(@RequestBody List<String> ids) {
        List<SoReturnReceiveDetailEntity> list = soReturnReceiveDetailService.listSoDetailByIds(ids);
        return list;
    }
}

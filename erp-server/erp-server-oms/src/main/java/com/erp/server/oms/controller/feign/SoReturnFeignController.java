package com.erp.server.oms.controller.feign;

import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soReturn")
public class SoReturnFeignController {
    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    /**
     * 根据主键id查询销售退货单主表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param id id
     * @return com.erp.model.oms.entity.SoReturnEntity
     **/
    @PostMapping("/getSoReturnById")
    public SoReturnEntity getSoReturnById(@RequestBody String id) {
        return soReturnService.getById(id);
    }

    /**
     * 根据来源id查询销售退货单详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("/listDetailBySourceId")
    public List<SoReturnDetailEntity> listDetailBySourceId(@RequestBody List<String> ids) {
        return soReturnDetailService.listDetailBySourceId(ids);
    }
}

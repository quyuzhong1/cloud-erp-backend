package com.erp.server.oms.controller.feign;

import com.common.core.controller.BaseController;
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
public class SoInfoFeignController extends BaseController {
    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    /**
     * 根据主键id查询销售单主表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     * @param id id
     * @return com.erp.model.oms.entity.SoInfoEntity
     **/
    @PostMapping("/getSoInfoById")
    public SoInfoEntity getSoInfoById(@RequestBody String id) {
        return soInfoService.getById(id);
    }

    /**
     * 根据销售单详情id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     **/
    @PostMapping("/listSoDetailByIds")
    public List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids) {
        return soDetailService.listSoDetailByIds(ids);
    }
}

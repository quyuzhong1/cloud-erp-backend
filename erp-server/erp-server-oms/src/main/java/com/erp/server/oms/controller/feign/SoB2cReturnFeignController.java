package com.erp.server.oms.controller.feign;

import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.service.SoB2cReturnDetailService;
import com.erp.server.oms.service.SoB2cReturnService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("feign/soB2cReturn")
public class SoB2cReturnFeignController {
    @Resource
    private SoB2cReturnService soB2cReturnService;

    @Resource
    private SoB2cReturnDetailService soB2cReturnDetailService;

    /**
     * 根据主键ids查询销售退货单主表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return com.erp.model.oms.entity.SoReturnEntity
     **/
    @PostMapping("/listByIds")
    public List<SoB2cReturnEntity> listByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return soB2cReturnService.listByIds(ids);
    }

    /**
     * 根据id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("/listDetailByIds")
    public List<SoB2cReturnDetailEntity> listDetailByIds(@RequestBody List<String> ids) {
        return soB2cReturnDetailService.listByIds(ids);
    }

    /**
     * 根据主表ids查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    @PostMapping("/listDetailByMainIds")
    public List<SoB2cReturnDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds) {
        return soB2cReturnDetailService.listByMainIds(mainIds);
    }
}

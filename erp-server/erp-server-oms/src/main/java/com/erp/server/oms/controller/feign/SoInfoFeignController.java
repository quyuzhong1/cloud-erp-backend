package com.erp.server.oms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购单
 *
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
     *
     * @param id id
     * @return com.erp.model.oms.entity.SoInfoEntity
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/getSoInfoById")
    public SoInfoEntity getSoInfoById(@RequestBody String id) {
        return soInfoService.getById(id);
    }

    /**
     * 根据销售单详情id查询详情表信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/listSoDetailByIds")
    public List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return soDetailService.listSoDetailByIds(ids);
    }

    /**
     * 根据销售单主表id查询详情表信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/listSoDetailByMainIds")
    public List<SoDetailEntity> listSoDetailByMainIds(@RequestBody List<String> ids) {
        return soDetailService.listSoDetailByMainIds(ids);
    }

    /**
     * 根据主表id 获取对应基础信息
     *
     * @param id
     * @return java.util.List<com.erp.model.oms.entity.SoInfoDTO.CustomerDTO>
     * @author yl
     * @date 2023-05-19 11:14
     */
    @PostMapping("/getSoBaseById")
    public SoInfoDTO.CustomerDTO getSoBaseById(@RequestBody String id) {
        return soInfoService.getSoCustomer(id);
    }

    /**
     * 根据销售订单id 集合 获取到对应客户信息
     *
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-22 10:41
     */
    @PostMapping("/listSoCustomerByIds")
    public List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(@RequestBody List<String> soIdList) {
        return soInfoService.listSoCustomerByIds(soIdList);
    }


}

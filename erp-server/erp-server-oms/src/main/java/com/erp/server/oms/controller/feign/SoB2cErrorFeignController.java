package com.erp.server.oms.controller.feign;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.server.oms.service.SoB2cErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * B2C销售订单异常表
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@RestController
@LogSystemModule("B2C销售订单异常表")
@RequestMapping("/feign/soB2cError")
public class SoB2cErrorFeignController extends BaseController {

    @Resource
    private SoB2cErrorService soB2cErrorService;

    /**
     * 添加异常信息
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public Boolean add(@RequestBody SoB2cErrorDTO.AddDTO dto) {
        return soB2cErrorService.add(dto);
    }

    /**
     * 删除异常信息
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public Boolean delete(@RequestBody SoB2cErrorDTO.DeleteDTO dto) {
        return soB2cErrorService.delete(dto);
    }

    /**
     * 获取异常信息
     * @param
     * @return
     */
    @PostMapping("/getB2cError")
    public SoB2cErrorEntity getB2cError(@RequestParam("mainId")String mainId,@RequestParam("errorType") String  errorType) {
        return soB2cErrorService.getByMainIdAndType(mainId,errorType);
    }

}

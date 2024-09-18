package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryInterceptService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * b2c发货拦截
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货拦截")
@RequestMapping("/feign/soB2cDeliveryIntercept")
public class SoB2cDeliveryInterceptFeignController extends BaseController {

    @Resource
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;

    /**
     * 新增拦截单
     * @Author Luo_WG
     * @Date 2023/12/26 13:23
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    @DataIdempotent(keyIdName = "dto.sourceCode",businessType = RedisKeyConstant.SO_B2C_DELIVERY_INTERCEPT_KEY)
    public BaseResultDTO.AddDTO add(@RequestBody SoB2cDeliveryInterceptDTO.AddDTO dto) {
        BaseResultDTO.AddDTO add = soB2cDeliveryInterceptService.add(dto);
        return add;
    }

    /**
     * 查询发货拦截标识
     * @Author Luo_WG
     * @Date 2023/12/26 13:18
     * @param soIds
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.IsInterceptDTO>
     **/
    @PostMapping("/listIsIntercept")
    public List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> listIsIntercept(@RequestBody List<String> soIds) {
        List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> interceptDTOList = soB2cDeliveryInterceptService.listIsIntercept(soIds);
        return interceptDTOList;
    }

    /**
     * 物流拦截
     * @Author Luo_WG
     * @Date 2023/12/26 13:31
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("/logisticsIntercept")
    public BatchResultDTO logisticsIntercept(@RequestBody String id) {
        BatchResultDTO resultDTO = soB2cDeliveryInterceptService.logisticsIntercept(id);
        return resultDTO;
    }

    /**
     * 根据来源id查询拦截单
     * @Author Luo_WG
     * @Date 2024/1/3 19:19
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity>
     **/
    @PostMapping("/listBySourceIds")
    public List<SoB2cDeliveryInterceptEntity> listBySourceIds(@RequestBody List<String> sourceIds) {
        List<SoB2cDeliveryInterceptEntity> resultList = soB2cDeliveryInterceptService.listBySourceIds(sourceIds);
        return resultList;
    }

    /**
     * 根据来源id修改拦截单状态
     * @Author Luo_WG
     * @Date 2024/1/3 19:36
     * @param sourceIds
     * @param status
     * @return void
     **/
    @PostMapping("/updateHandleStatus")
    public Boolean updateHandleStatus(@RequestParam("sourceIds") List<String> sourceIds, @RequestParam("status") String status) {
        Boolean flag = soB2cDeliveryInterceptService.updateHandleStatus(sourceIds, status);
        return flag;
    }

    /**
     * 发货拦截结果确认
     * @Author Luo_WG
     * @Date 2024/1/16 18:56
     * @param dto
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("/interceptResultConfirm")
    public BatchResultDTO interceptResultConfirm(@RequestBody SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto, @RequestParam("id") String id) {
        BatchResultDTO resultDTO = soB2cDeliveryInterceptService.interceptResultConfirm(dto, id);
        return resultDTO;
    }
}

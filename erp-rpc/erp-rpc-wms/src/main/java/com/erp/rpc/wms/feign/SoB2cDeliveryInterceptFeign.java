package com.erp.rpc.wms.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * 发货拦截
 * @Author Luo_WG
 * @Date 2023/12/26 13:20
 **/
@FeignClient(name = "erp-wms", contextId = "soB2cDeliveryInterceptFeign")
public interface SoB2cDeliveryInterceptFeign {

    /**
     * 新增拦截单
     * @Author Luo_WG
     * @Date 2023/12/26 13:23
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/add")
    BaseResultDTO.AddDTO add(@RequestBody SoB2cDeliveryInterceptDTO.AddDTO dto);

    /**
     * 查询发货标识
     * @Author Luo_WG
     * @Date 2023/12/26 13:21
     * @param soIdList
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.IsInterceptDTO>
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/listIsIntercept")
    List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> listIsIntercept(@RequestBody List<String> soIdList);

    /**
     * 物流拦截
     * @Author Luo_WG
     * @Date 2023/12/26 13:31
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/logisticsIntercept")
    BatchResultDTO logisticsIntercept(@RequestBody String id);

    /**
     * 根据来源id查询拦截单
     * @Author Luo_WG
     * @Date 2024/1/3 19:19
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity>
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/listBySourceIds")
    List<SoB2cDeliveryInterceptEntity> listBySourceIds(@RequestBody List<String> sourceIds);

    /**
     * 根据来源id修改拦截单状态
     * @Author Luo_WG
     * @Date 2024/1/3 19:36
     * @param sourceIds
     * @param status
     * @return void
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/updateHandleStatus")
    Boolean updateHandleStatus(@RequestParam("sourceIds") List<String> sourceIds, @RequestParam("status") String status);

    /**
     * 发货拦截结果确认
     * @Author Luo_WG
     * @Date 2024/1/16 18:56
     * @param dto
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/interceptResultConfirm")
    BatchResultDTO interceptResultConfirm(@RequestBody SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto, @RequestParam("id") String id);

    /**
     * 处理拦截成功
     **/
    @PostMapping("feign/soB2cDeliveryIntercept/handleSuccess")
    BatchResultDTO handleSuccess(@RequestBody SoB2cDeliveryEntity soB2cDelivery, @RequestParam("id") String interceptId);
}

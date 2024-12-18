package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logistics",configuration = {FeignErrorDecoder.class})
public interface LogisticsFeign {

    /**
     * 根据虾皮订单号查询物流单号
     * @param logisticsQueryVOList
     * @return
     */
    @PostMapping("/feign/logistics/queryOrderList")
    ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/feign/logistics/listBySupplierId")
    List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @Author Luo_WG
     * @Date 2024/2/1 20:33
     * @param logisticsSupplierIds
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     **/
    @PostMapping("/feign/logistics/listLogisticsChannel")
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@RequestBody List<String> logisticsSupplierIds);

    @PostMapping("/feign/logistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);

    /**
     * 根据渠道id 获取渠道信息
     * @param channelId
     * @return
     */
    @PostMapping("/feign/logistics/getChannelById")
    LogisticsChannelEntity getChannelById(@RequestBody String channelId);
    /**
     * 根据渠道名称 获取渠道信息
     * @param channelName
     * @return
     */
    @PostMapping("/feign/logistics/getChannelByName")
    List<LogisticsChannelEntity> getChannelByName(@RequestBody String channelName);
    @PostMapping("/feign/logistics/getChannelInfoById")
    LogisticsChannelDTO.BaseDTO getChannelInfoById(@RequestBody String channelId);

    @PostMapping("/feign/logistics/listChannelInfoById")
    List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(@RequestBody List<String> channelIds);

    @GetMapping("/feign/logistics/getLogisticsChannelConstraint")
    LogisticsChannelDTO.LogisticsChannelConstraintDTO getLogisticsChannelConstraint(@RequestParam(value = "channelId")String channelId,@RequestParam(value = "country")String country);
    /**
     * 获取地址信息
     * @param id
     * @return
     */
    @GetMapping("/feign/logistics/getLogisticsAddressById")
    LogisticsAddressEntity getLogisticsAddressById(@RequestParam("id")String id);


    @GetMapping("/feign/logistics/getScaleChannelByChannelById")
    LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(@RequestParam("logisticsChannelId") String logisticsChannelId,
                                                                 @RequestParam("dictPlatform") String dictPlatform
    );

    /**
     * 根据地址类型获取地址列表
     *
     * @return
     */
    @PostMapping("/feign/logistics/listAddressByType")
    List<LogisticsAddressDTO.ListDTO> listAddressByType(@RequestBody @Validated LogisticsAddressDTO.AddressByTypeDTO dto);
    /**
     * 根据渠道汇总时间段内未更新运单号记录
     *
     * @return
     */
    @PostMapping("/feign/logistics/getWarnReportByChannel")
    List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(@RequestBody LogisticsBillDetailQueryDTO query);

    /**
     * 接收track123物流轨迹同步数据
     * @return
     */
    @PostMapping("/feign/logistics/webhookByTrack123")
    void webhookByTrack123(@RequestBody LogisticsTrackDTO.TrackWebHookDTO dto);
}

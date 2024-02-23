package com.erp.rpc.tms.feign;

import com.erp.model.tms.dto.LogisticsSupplierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logisticsAuth")
public interface LogisticsAuthFeign {
    /**
     * 根据id获取授权信息
     * @Author Luo_WG
     * @Date 2023/12/26 15:26
     * @param channelId
     * @return com.erp.model.tms.dto.LogisticsSupplierDTO.AuthDTO
     **/
    @PostMapping("/feign/logisticsAuth/getAuthByChannelId")
    LogisticsSupplierDTO.AuthDTO getAuthByChannelId(@RequestBody String channelId);

    /**
     * 根据渠道id查询渠道关联的平台信息
     * @Author Luo_WG
     * @Date 2024/1/25 17:27
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsSupplierDTO.AuthChannelViewDTO>
     **/
    @PostMapping("/feign/logisticsAuth/listAuthChannelView")
    List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelView(@RequestBody List<String> channelIdList);
}

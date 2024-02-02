package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.server.tms.service.LogisticsAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 授权信息
 */
@Slf4j
@RestController
@LogSystemModule("物流授权接口")
@RequestMapping("/feign/logisticsAuth")
public class LogisticsAuthFeignController {
    @Resource
    private LogisticsAuthService logisticsAuthService;

    /**
     * 根据id获取授权信息
     * @Author Luo_WG
     * @Date 2023/12/26 15:26
     * @param channelId
     * @return com.erp.model.tms.dto.LogisticsSupplierDTO.AuthDTO
     **/
    @PostMapping("/getAuthByChannelId")
    public LogisticsSupplierDTO.AuthDTO getAuthByChannelId(@RequestBody String channelId) {
        LogisticsSupplierDTO.AuthDTO authByChannelId = logisticsAuthService.getAuthByChannelId(channelId);
        return authByChannelId;
    }

    /**
     * 根据渠道id查询渠道关联的平台信息
     * @Author Luo_WG
     * @Date 2024/1/25 17:27
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsSupplierDTO.AuthChannelViewDTO>
     **/
    @PostMapping("/listAuthChannelView")
    public List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelView(@RequestBody List<String> channelIdList) {
        List<LogisticsSupplierDTO.AuthChannelViewDTO> authChannelViewDTOS = logisticsAuthService.listAuthChannelView(channelIdList);
        return authChannelViewDTOS;
    }
}

package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流信息（备货规则设置）
 *
 * @author will
 * @since 2024-10-29
 */
@Slf4j
@RestController
@LogSystemModule("物流信息（备货规则设置）")
@RequestMapping("/cfgRuleLogistics")
public class CfgRuleLogisticsController extends BaseController {

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    /**
     * 物流方式、时效下拉
     * @author will
     * @date 2024/10/29 10:18
     * @return ApiResult<List<SelectLogisticsDTO>>
     */
    @PostMapping("/selectLogistics")
    public ApiResult<List<CfgRuleLogisticsDTO.SelectLogisticsDTO>> selectLogistics(@RequestBody @Validated CfgRuleLogisticsDTO.SelectLogisticsParamDTO paramDTO) {
        List<CfgRuleLogisticsDTO.SelectLogisticsDTO> list = cfgRuleLogisticsService.selectLogistics(paramDTO);
        return success(list);
    }
}

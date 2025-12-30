package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.server.sys.service.CfgQueryConditionService;
import com.erp.server.sys.service.CfgThirdNoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询条件配置 Feign 控制器
 * @author jack
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/feign/cfgThirdNotice")
public class CfgThirdNoticeFeignController extends BaseController {

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    /**
     * 根据模具监控类查询通知配置
     * @author jack
     * @date:  2025-12-26
     * @return List<CfgThirdNoticeDTO.DropDownDTO>
     */
    @GetMapping("/dropDownByMoldMonitor")
    public List<CfgThirdNoticeDTO.DropDownDTO> dropDownByMoldMonitor(@RequestParam(value = "sourceType",required = true) String sourceType) {
        return cfgThirdNoticeService.dropDownByMoldMonitor(sourceType);
    }
}

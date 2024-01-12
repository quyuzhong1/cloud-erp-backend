package com.erp.server.srm.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.server.srm.service.CfgSettingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName SrmCfgSettingFeignController
 * @description: srm 配置
 * @date 2024年01月11日
 * @version: 1.0
 */
@RestController
@RequestMapping("/feign/cfgSetting")
public class SrmCfgSettingFeignController extends BaseController {
    @Resource
    private CfgSettingService cfgSettingService;
    /**
     * 获取供应商订单配置信息
     *
     * @return
     */
    @PostMapping("/getConfigList")
    public List<SupplierConfigVO> getConfigList(@RequestBody List<String> supplierIds) {
        return cfgSettingService.getConfigList(supplierIds);
    }
}

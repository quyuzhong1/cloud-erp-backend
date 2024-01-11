package com.erp.rpc.srm.feign;

import com.erp.model.srm.vo.SupplierConfigVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author zdy
 * @ClassName SrmCfgSettingFeign
 * @description: TODO
 * @date 2024年01月11日
 * @version: 1.0
 */
@FeignClient(name = "erp-srm", contextId = "cfgSetting")
public interface SrmCfgSettingFeign {

    @PostMapping("/feign/cfgSetting/list")
    List<SupplierConfigVO> list(@RequestBody List<String> supplierIds);
}

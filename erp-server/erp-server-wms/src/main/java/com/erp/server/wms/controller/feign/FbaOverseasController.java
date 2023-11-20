package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.service.FbaInventoryService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/fbaOverseas")
public class FbaOverseasController extends BaseController {

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
     * 查询海外仓ku
     *
     * @return
     * @parms idList
     * @author yl
     * @date 2023-11-20
     */
    @PostMapping("/listWarehouseByIds")
    public List<OverseasProviderWarehouseEntity> listWarehouseByIds(@RequestBody List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }

        return overseasProviderWarehouseService.listByIds(idList);
    }


}
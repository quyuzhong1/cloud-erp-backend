package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓位远程调用控制器
 * @CreateTime: 2023-05-22  11:55
 * @Author: zhangchunlin
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/warehouseLocation")
public class WarehouseLocationFeignController extends BaseController {

    private final WarehouseLocationService warehouseLocationService;

    /**
     * 引用仓位（删除时会验证是否已经被引用）
     * @param ids
     */
    @PostMapping("/quoteLocation")
    public void quoteLocation(@RequestBody List<String> ids) {
        warehouseLocationService.quoteLocation(ids);
    }

    /**
     * 根据仓位id获取仓位信息
     * @param id
     */
    @PostMapping("/getById")
    public WarehouseLocationEntity quoteLocation(@RequestParam(value = "id")String id) {
      return StrUtils.isEmpty(id) ? new WarehouseLocationEntity() : warehouseLocationService.getById(id);
    }


}
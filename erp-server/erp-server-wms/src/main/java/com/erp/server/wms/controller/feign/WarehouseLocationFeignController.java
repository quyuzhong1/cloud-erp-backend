package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
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

    /**
     * 根据仓库ids查询
     * @Author Luo_WG
     * @Date 2023/9/4 17:38
     * @param warehouseIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseLocationEntity>
     **/
    @PostMapping("/listByWarehouseIds")
    public List<WarehouseLocationEntity> listByWarehouseIds(@RequestBody List<String> warehouseIds) {
        return warehouseLocationService.listByWarehouseIds(warehouseIds);
    }

    /**
     * 查询所有仓位
     * @Author Luo_WG
     * @Date 2023/9/4 17:38
     * @return java.util.List<com.erp.model.wms.entity.WarehouseLocationEntity>
     **/
    @PostMapping("/list")
    public List<WarehouseLocationEntity> list() {
        return warehouseLocationService.list();
    }

}
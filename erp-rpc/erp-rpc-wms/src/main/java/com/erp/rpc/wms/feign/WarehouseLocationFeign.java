package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.WarehouseLocationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Classname: WarehouseLocationFeign
 * @Description: 仓位远程调用接口
 * @CreateTime: 2023-05-22  11:51
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-wms", contextId = "warehouseLocation", path = "/feign/warehouseLocation")
public interface WarehouseLocationFeign {

    /**
     * 引用仓位（删除时会验证是否已经被引用）
     * @param ids
     */
    @PostMapping("/quoteLocation")
    void quoteLocation(@RequestBody List<String> ids);


    /**
     * 根据仓位id获取仓位信息
     * @param id
     */
    @PostMapping("/getById")
    WarehouseLocationEntity quoteLocation(@RequestParam(value = "id")String id);


}



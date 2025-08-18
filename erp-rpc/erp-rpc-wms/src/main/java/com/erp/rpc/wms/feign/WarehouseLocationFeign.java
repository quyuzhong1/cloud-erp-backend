package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.WarehouseLocationDTO;
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
@FeignClient(name = "erp-wms", contextId = "warehouseLocation", path = "/feign/warehouseLocation",configuration = {FeignErrorDecoder.class})
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

    /**
     * 根据仓库ids查询
     * @Author Luo_WG
     * @Date 2023/9/4 17:38
     * @param warehouseIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseLocationEntity>
     **/
    @PostMapping("/listByWarehouseIds")
    List<WarehouseLocationEntity> listByWarehouseIds(@RequestBody List<String> warehouseIds);

    /**
     * 查询所有仓位
     * @Author Luo_WG
     * @Date 2023/9/4 17:38
     * @return java.util.List<com.erp.model.wms.entity.WarehouseLocationEntity>
     **/
    @PostMapping("/list")
    List<WarehouseLocationEntity> list();

    /**
     * 根据仓库id和库位查询仓位信息
     * @param paramList
     * @return
     */
    @PostMapping("/listByWarehouseIdAndCode")
    List<WarehouseLocationEntity> listByWarehouseIdAndCode(@RequestBody List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList);
}



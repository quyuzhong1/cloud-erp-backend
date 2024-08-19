package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.server.wms.service.FbaInventoryService;
import com.erp.server.wms.service.QcInfoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 质检单feign控制器
 * @CreateTime: 2023-06-19  15:48
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("/feign/fbaInventory")
public class FbaInventoryFeignController extends BaseController implements WmsFbaInventoryFeign {

    @Resource
    private FbaInventoryService fbaInventoryService;

    /**
     * 批量保存FBA库存信息和预留明细
     *
     * @Author Jim
     * @Date 2023-11-08
     **/
    @PostMapping("/allBatchSave")
    public Boolean allBatchSave(@RequestBody List<FbaInventoryEntity> inventoryEntityList) {
       return fbaInventoryService.allBatchSave(inventoryEntityList);
    }

    /**
     * 查询FBA库存信息和预留明细列表
     *
     * @Author Jim
     * @Date 2023-11-23
     **/
    @PostMapping("/list")
    public List<FbaInventoryEntity> findList(@RequestBody List<String> sellerSkuList) {
        return fbaInventoryService.findList(sellerSkuList);
    }

}
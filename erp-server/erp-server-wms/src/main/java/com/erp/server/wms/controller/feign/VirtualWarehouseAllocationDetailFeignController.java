package com.erp.server.wms.controller.feign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.utils.CollectionUtils;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehousePushHandleRelationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hyj
 */
@RestController
@RequestMapping("feign/virtualWarehouseAllocationDetail")
public class VirtualWarehouseAllocationDetailFeignController {

    @Resource
    VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;

    @PostMapping("/updateSyncStatus")
    public void updateSyncStatus(@RequestBody VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        virtualWarehouseAllocationDetailService.updateSyncStatus(dto);
    }

    /**
     * 获取明细记录
     * @param handelDetailId
     * @return
     */
    @GetMapping("/getByHandleDetailId")
    List<VirtualWarehouseAllocationDetailEntity> getByHandleDetailId(@RequestParam(value = "handelDetailId") String handelDetailId){
        //根据合单明细id获取拆单信息
        List<VirtualWarehousePushHandleRelationEntity> handleRelationEntityList = virtualWarehousePushHandleRelationService
                .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>().eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handelDetailId));
        List<String> detailIds = handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return virtualWarehouseAllocationDetailService.listByIds(detailIds);
    }
}


package com.erp.server.oms.controller.feign;

import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.server.oms.service.SoB2cReturnDetailService;
import com.erp.server.oms.service.SoB2cReturnService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("feign/soB2cReturn")
public class SoB2cReturnFeignController {
    @Resource
    private SoB2cReturnService soB2cReturnService;

    @Resource
    private SoB2cReturnDetailService soB2cReturnDetailService;

    /**
     * 根据主键ids查询销售退货单主表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return com.erp.model.oms.entity.SoReturnEntity
     **/
    @PostMapping("/listByIds")
    public List<SoB2cReturnEntity> listByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return soB2cReturnService.listByIds(ids);
    }

    /**
     * 根据id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("/listDetailByIds")
    public List<SoB2cReturnDetailEntity> listDetailByIds(@RequestBody List<String> ids) {
        return soB2cReturnDetailService.listByIds(ids);
    }

    /**
     * 根据主表ids查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    @PostMapping("/listDetailByMainIds")
    public List<SoB2cReturnDetailDTO.ViewDTO> listDetailByMainIds(@RequestBody List<String> mainIds) {
        List<SoB2cReturnDetailEntity> detailList = soB2cReturnDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList.stream().map(this::toViewDTO).collect(Collectors.toList());
    }

    /**
     * Entity 与 ViewDTO 业务字段一一对应（无单价/金额字段，金额由 WMS 侧关联 {@code SoB2cDetailEntity} 计算）。
     */
    private SoB2cReturnDetailDTO.ViewDTO toViewDTO(SoB2cReturnDetailEntity entity) {
        SoB2cReturnDetailDTO.ViewDTO viewDTO = new SoB2cReturnDetailDTO.ViewDTO();
        viewDTO.setId(entity.getId());
        viewDTO.setMainId(entity.getMainId());
        viewDTO.setSkuId(entity.getSkuId());
        viewDTO.setSkuNo(entity.getSkuNo());
        viewDTO.setPlatformSkuNo(entity.getPlatformSkuNo());
        viewDTO.setSaleQty(entity.getSaleQty());
        viewDTO.setReturnQty(entity.getReturnQty());
        viewDTO.setRemark(entity.getRemark());
        viewDTO.setSoDetailId(entity.getSoDetailId());
        return viewDTO;
    }


    @PostMapping("/updateBatch")
    void updateBatch(@RequestBody List<SoB2cReturnEntity> list){
        soB2cReturnService.updateBatchById(list);
    }

}

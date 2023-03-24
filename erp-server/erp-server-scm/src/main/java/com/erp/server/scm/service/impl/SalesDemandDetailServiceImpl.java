package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SalesDemandDetailMapper;
import com.erp.server.scm.service.SalesDemandDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售需求明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class SalesDemandDetailServiceImpl extends SuperServiceImpl<SalesDemandDetailMapper, SalesDemandDetailEntity> implements SalesDemandDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void add(List<SalesDemandDetailDTO.AddDTO> details, String salesDemandId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<SalesDemandDetailEntity> list = BeanMapperUtils.copyList(SalesDemandDetailEntity.class, details);
        //仓库信息
        List<String> destWarehouseIdList = list.stream().map(SalesDemandDetailEntity::getDestWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(destWarehouseIdList);

        for (SalesDemandDetailEntity entity : list) {
            entity.setSalesDemandId(salesDemandId);
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDestWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
                entity.setDestWarehouseName(warehouseName);
            }
        }
        this.saveBatch(list);
    }

    @Override
    public void update(List<SalesDemandDetailDTO.UpdateDTO> details,String salesDemandId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<SalesDemandDetailEntity> oldList = this.listBySalesDemandId(salesDemandId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        List<SalesDemandDetailEntity> newList = BeanMapperUtils.copyList(SalesDemandDetailEntity.class, details);
        //仓库信息
        List<String> destWarehouseIdList = newList.stream().map(SalesDemandDetailEntity::getDestWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(destWarehouseIdList);
        for (SalesDemandDetailEntity entity : newList) {
            entity.setSalesDemandId(salesDemandId);
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDestWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
                entity.setDestWarehouseName(warehouseName);
            }
        }
        this.saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SalesDemandDetailDTO.UpdateDTO> newList, List<SalesDemandDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SalesDemandDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SalesDemandDetailEntity::getId).collect(Collectors.toList());
        return newIds.stream().filter(s -> !oldIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据主表id查询
     */
    @Override
    public List<SalesDemandDetailEntity> listBySalesDemandId(String salesDemandId) {
      return  lambdaQuery().eq(SalesDemandDetailEntity::getSalesDemandId,salesDemandId).list();
    }


    @Override
    public void removeBySalesDemandIds(List<String> salesDemandIds) {
        lambdaUpdate().set(SalesDemandDetailEntity::getId,salesDemandIds).remove();
    }

    @Override
    public SalesDemandDetailEntity getBySalesDemandIdAndSkuId(String salesDemandId, String skuId) {
        return lambdaQuery().eq(SalesDemandDetailEntity::getSalesDemandId,salesDemandId).eq(SalesDemandDetailEntity::getSkuId,skuId).one();
    }

}

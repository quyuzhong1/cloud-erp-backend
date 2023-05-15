package com.erp.server.oms.service.impl;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoOutstockDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单详情 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoDetailServiceImpl extends SuperServiceImpl<SoDetailMapper, SoDetailEntity> implements SoDetailService {
    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    /**
     * 根据退货单详情表id查询退货单
     *
     * @param detailIds
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     * @Author Luo_WG
     * @Date 2023/5/11 18:16
     **/
    @Override
    public List<SoDetailEntity> listSoDetailByIds(List<String> detailIds) {
        return lambdaQuery().in(SoDetailEntity::getId, detailIds).list();
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(String id) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(id);
        List<String> detailIds = list.stream().map(SoDetailDTO.AddDetailView::getId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnDetailService.listDetailByMainId(id);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        for (SoDetailDTO.AddDetailView addDetailView : list) {
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(req -> req.getOrgId().equals(addDetailView.getInventoryOrgId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDetailView.setInventoryOrgName(warehouse.getName());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId()) && ReturnTypeEnum.REPLENISHMENT.getCode().equals(req.getReturnTypeDict())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            addDetailView.setAvailableQty(addDetailView.getSalesQty()+returnQty);
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty()+returnQty - actualQty);
        }
        return list;
    }

}

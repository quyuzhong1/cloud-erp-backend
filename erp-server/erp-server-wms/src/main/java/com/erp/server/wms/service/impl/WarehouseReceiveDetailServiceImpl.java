package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.server.wms.mapper.WarehouseReceiveDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Service
public class WarehouseReceiveDetailServiceImpl extends SuperServiceImpl<WarehouseReceiveDetailMapper, WarehouseReceiveDetailEntity> implements WarehouseReceiveDetailService {

    @Resource
    private ProductOrderFeign productOrderFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(WarehouseReceiveDTO.AddDTO dto, String id) {
        //创建保存详情的集合
        List<WarehouseReceiveDetailEntity> listDetail = new ArrayList<>();
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = dto.getWarehouseReceiveDetailList().stream().map(WarehouseReceiveDetailDTO.AddDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(orderDetailIds);
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        for (WarehouseReceiveDetailDTO.AddDTO addDTO : warehouseReceiveDetailList) {
            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            warehouseReceiveDetailEntity.setMain_id(id);
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
                warehouseReceiveDetailEntity.setReceiveQty(addDTO.getReceiveQty());
                warehouseReceiveDetailEntity.setExceedQty(addDTO.getExceedQty());
                warehouseReceiveDetailEntity.setRemark(addDTO.getRemark());
                warehouseReceiveDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
            } else {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            listDetail.add(warehouseReceiveDetailEntity);
        }
        //保存详情信息
        return this.saveBatch(listDetail);
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(WarehouseReceiveDTO.UpdateDTO dto) {
        //创建保存详情的集合
        List<WarehouseReceiveDetailEntity> listDetail = new ArrayList<>();
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = dto.getWarehouseReceiveDetailList().stream().map(WarehouseReceiveDetailDTO.UpdateDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //根据ids查询采购单详情
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = productOrderFeign.listPurchaseOrderDetailById(orderDetailIds);
        List<WarehouseReceiveDetailDTO.UpdateDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        for (WarehouseReceiveDetailDTO.UpdateDTO updateDTO : warehouseReceiveDetailList) {
            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            if (StringUtils.isNotBlank(updateDTO.getId())) {
                warehouseReceiveDetailEntity.setId(updateDTO.getId());
            }
            warehouseReceiveDetailEntity.setMain_id(updateDTO.getMain_id());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());
                warehouseReceiveDetailEntity.setReceiveQty(updateDTO.getReceiveQty());
                warehouseReceiveDetailEntity.setExceedQty(updateDTO.getExceedQty());
                warehouseReceiveDetailEntity.setRemark(updateDTO.getRemark());
                warehouseReceiveDetailEntity.setPurchaseOrderDetailId(updateDTO.getPurchaseOrderDetailId());
            } else {
                throw new ServiceException(ApiError.ERROR_99006);
            }
            listDetail.add(warehouseReceiveDetailEntity);
        }
        return this.saveOrUpdateBatch(listDetail);
    }

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(WarehouseReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(WarehouseReceiveDetailEntity::getMain_id, mainIds)
                .remove();
    }

    /**
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainId mainId
     * @return java.lang.Boolean
     **/
    public List<WarehouseReceiveDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<WarehouseReceiveDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WarehouseReceiveDetailEntity::getMain_id, mainId);
        return this.list(queryWrapper);
    }
    @Override
    public List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(List<String> purchaseDetailIds) {
        return lambdaQuery().in(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId,purchaseDetailIds).list();
    }
}

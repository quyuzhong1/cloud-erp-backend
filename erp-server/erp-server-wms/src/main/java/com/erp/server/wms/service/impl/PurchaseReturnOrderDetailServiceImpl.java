package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDetailDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.PurchaseReturnOrderDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购退货单明细 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Service
public class PurchaseReturnOrderDetailServiceImpl extends SuperServiceImpl<PurchaseReturnOrderDetailMapper, PurchaseReturnOrderDetailEntity> implements PurchaseReturnOrderDetailService {


    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    @Resource
    private PurchaseStockInService purchaseStockInService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    /**
     * @description: 根据来源明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:54
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     */
    @Override
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

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
    public Boolean add(PurchaseReturnOrderDTO.AddDTO dto, String id) {
        Integer returnQty = MathUtil.ZERO;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
            List<PurchaseStockInDetailEntity> stockInDetailEntityList = purchaseStockInDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());

                    if (dto.getSourceType().equals(SourceTypeEnum.WAREHOUSE_RECEIVE.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

                        if (addDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                    purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
                    purchaseReturnOrderDetailEntity.setReplenishQty(addDTO.getReplenishQty());
                    purchaseReturnOrderDetailEntity.setDeductAmountQty(addDTO.getDeductAmountQty());
                    purchaseReturnOrderDetailEntity.setReturnPrice(addDTO.getReturnPrice());
                    purchaseReturnOrderDetailEntity.setRemark(addDTO.getRemark());
                    purchaseReturnOrderDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
                    purchaseReturnOrderDetailEntity.setCurrency(addDTO.getCurrency());
                    purchaseReturnOrderDetailEntity.setSourceDetailId(addDTO.getSourceDetailId());
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderAdd(dto, id, listDetail);
        }

        //保存详情信息
        return this.saveBatch(listDetail);
    }

    /**
     * 无采购单新增
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     * @param dto id
     * @return void
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderAdd(PurchaseReturnOrderDTO.AddDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
        for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(addDTO, purchaseReturnOrderDetailEntity);
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
            listDetail.add(purchaseReturnOrderDetailEntity);
        }
        return listDetail;
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
    public Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto, String id) {
        Integer returnQty = MathUtil.ZERO;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.UpdateDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
            List<PurchaseStockInDetailEntity> stockInDetailEntityList = purchaseStockInDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());

                    if (dto.getSourceType().equals(SourceTypeEnum.WAREHOUSE_RECEIVE.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }

                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getId().equals(updateDTO.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderUpdate(dto, id, listDetail);
        }
        return this.saveOrUpdateBatch(listDetail);
    }

    /**
     * 无采购单新增
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     * @param dto id
     * @return void
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderUpdate(PurchaseReturnOrderDTO.UpdateDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
        for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(updateDTO.getReturnQty());
            listDetail.add(purchaseReturnOrderDetailEntity);
        }
        return listDetail;
    }

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(PurchaseReturnOrderDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(PurchaseReturnOrderDetailEntity::getMainId, mainIds)
                .remove();
    }

    /**
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainId mainId
     * @return java.lang.Boolean
     **/
    @Override
    public List<PurchaseReturnOrderDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<PurchaseReturnOrderDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseReturnOrderDetailEntity::getMainId, mainId);
        return this.list(queryWrapper);
    }

    @Override
    public List<PurchaseReturnOrderDetailEntity> listReturnOrderDetailByPodIds(List<String> podIds) {
        return baseMapper.listReturnOrderDetailByPodIds(podIds);
    }
}

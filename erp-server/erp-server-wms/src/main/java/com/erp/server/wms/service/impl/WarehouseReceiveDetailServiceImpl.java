package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.WarehouseReceiveDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;



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
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //校验sku重复
        checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
        List<WarehouseReceiveDetailEntity> detailEntityList = listWarehouseReceiveByPodIds(orderDetailIds);

        List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(orderDetailIds);

        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();
        for (WarehouseReceiveDetailDTO.AddDTO addDTO : warehouseReceiveDetailList) {

            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            warehouseReceiveDetailEntity.setMainId(id);
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(new PurchaseOrderDetailEntity());
            warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
            warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
            warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());

            Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer receiveQty = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer purchaseQty = purchaseOrderDetailEntity.getPurchaseQty();
            if (receiveQty > purchaseQty + returnQty) {
                throw new ServiceException(ApiError.ERROR_99025.code, String.format(ApiError.ERROR_99025.msg, purchaseOrderDetailEntity.getSkuNo()));
            }

            warehouseReceiveDetailEntity.setReceiveQty(addDTO.getReceiveQty());
            warehouseReceiveDetailEntity.setExceedQty(addDTO.getExceedQty());
            warehouseReceiveDetailEntity.setRemark(addDTO.getRemark());
            warehouseReceiveDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
            warehouseReceiveDetailEntity.setCreateUserId(dto.getCreateUserId());
            warehouseReceiveDetailEntity.setCreateUserName(dto.getCreateUserId());
            warehouseReceiveDetailEntity.setUpdateUserId(dto.getUpdateUserId());
            warehouseReceiveDetailEntity.setUpdateUserName(dto.getUpdateUserName());

          /*  Integer receive = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (addDTO.getReceiveQty() > (purchaseOrderDetailEntity.getPurchaseQty() - receive)) {
                throw new ServiceException(ApiError.ERROR_99013);
            }*/
            listDetail.add(warehouseReceiveDetailEntity);
        }
        //保存详情信息
        return this.saveBatch(listDetail);
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseOrderDetailEntity> list) {
        Map<String, List<PurchaseOrderDetailEntity>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailEntity>> entry : map.entrySet()) {
            List<PurchaseOrderDetailEntity> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
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
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
        //校验sku重复
        checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
        List<WarehouseReceiveDetailDTO.UpdateDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();

        List<WarehouseReceiveDetailEntity> detailEntityList = listWarehouseReceiveByPodIds(orderDetailIds);
        List<PurchaseReturnOrderDetailEntity> returnDetailEntityList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(orderDetailIds);
        for (WarehouseReceiveDetailDTO.UpdateDTO updateDTO : warehouseReceiveDetailList) {
            WarehouseReceiveDetailEntity warehouseReceiveDetailEntity = new WarehouseReceiveDetailEntity();
            if (StringUtils.isNotBlank(updateDTO.getId())) {
                warehouseReceiveDetailEntity.setId(updateDTO.getId());
            }
            warehouseReceiveDetailEntity.setMainId(updateDTO.getMain_id());
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                warehouseReceiveDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                warehouseReceiveDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                warehouseReceiveDetailEntity.setPlanDeliveryDate(purchaseOrderDetailEntity.getPlanDeliveryDate());

                Integer returnQty = returnDetailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

                Integer receiveQty = detailEntityList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);

                Integer purchaseQty = purchaseOrderDetailEntity.getPurchaseQty();
                if ((receiveQty - returnQty) > purchaseQty) {
                    throw new ServiceException(ApiError.ERROR_99025.code, String.format(ApiError.ERROR_99025.msg, purchaseOrderDetailEntity.getSkuNo()));
                }

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
    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(WarehouseReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(WarehouseReceiveDetailEntity::getMainId, mainIds)
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
    public List<WarehouseReceiveDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<WarehouseReceiveDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WarehouseReceiveDetailEntity::getMainId, mainId);
        return this.list(queryWrapper);
    }

    @Override
    public List<WarehouseReceiveDetailEntity> listDetailByPodIds(List<String> podIds) {
        return lambdaQuery().in(WarehouseReceiveDetailEntity::getPurchaseOrderDetailId,podIds).list();
    }

    @Override
    public List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(List<String> purchaseDetailIds) {
        return baseMapper.listWarehouseReceiveByPodIds(purchaseDetailIds);
    }
}

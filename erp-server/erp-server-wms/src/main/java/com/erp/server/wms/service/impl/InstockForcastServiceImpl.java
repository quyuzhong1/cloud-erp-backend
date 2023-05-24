package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.InstockForcastDetailEntity;
import com.erp.model.wms.entity.InstockForcastEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.InstockForcastMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 入库预报表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Slf4j
@Service
public class InstockForcastServiceImpl extends SuperServiceImpl<InstockForcastMapper, InstockForcastEntity> implements InstockForcastService {

    @Autowired
    private InstockForcastDetailService instockForcastDetailService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InstockForcastMapper instockForcastMapper;

    @Autowired
    private WarehouseService warehouseService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateByPurchaseOrder(InstockForcastDTO.AddDTO dto) {
        String purchaseOrderId = dto.getPurchaseOrderId();
        // 此处增加校验，如果下推过，则不允许下推
        InstockForcastEntity queryInstockForcastEntity = findByPurchaseOrderId(dto.getPurchaseOrderId());
        if(Objects.nonNull(queryInstockForcastEntity)) {
            log.warn("采购订单id：【{}】已找到未删除的入库预报", purchaseOrderId);
            throw new ServiceException("采购订单已下推生成入库预报");
        }
        InstockForcastEntity instockForcastEntity = new InstockForcastEntity();
        // 生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.RKYB, BusinessNoTypeEnum.CODE_RKYB.getCode()));
        // feign调用取不到登录人信息，已解决
        LoginUser loginUser = commonService.getUserInfo();
        instockForcastEntity.setCreateUserId(loginUser.getUid());
        instockForcastEntity.setCreateUserName(loginUser.getUserName());
        instockForcastEntity.setUpdateUserId(loginUser.getUid());
        instockForcastEntity.setUpdateUserName(loginUser.getUserName());

        // 默认审核通过
        instockForcastEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        instockForcastEntity.setApproveTime(LocalDateTime.now());
        instockForcastEntity.setApproveUserId(loginUser.getUid());
        instockForcastEntity.setApproveUserName(loginUser.getUserName());

        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(dto.getWarehouseId());
        if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        // 仓库组织
        String orgId = warehouseDetail.getOrgId();
        instockForcastEntity.setCode(code);
        instockForcastEntity.setPurchaseOrderId(purchaseOrderId);
        instockForcastEntity.setOrgId(orgId);
        instockForcastEntity.setWarehouseId(dto.getWarehouseId());
        instockForcastEntity.setBillDate(LocalDate.now());// 单据日期取采购订单日期

        // 保存主单信息
        this.save(instockForcastEntity);
        // 保存入库预报明细
        List<InstockForcastDetailEntity> instockForcastDetails = instockForcastDetailService.add(dto, instockForcastEntity.getId());
        // 调用库存组件，更新库存信息
        InventoryInOutStockDTO inventoryDto = new InventoryInOutStockDTO();
        inventoryDto.setBusinessType(InventoryBusinessTypeEnum.INSTOCK_FORCAST.getCode());
        List<InOutStockDTO> inventorySkus = Lists.newArrayListWithExpectedSize(instockForcastDetails.size());
        instockForcastDetails.stream().forEach(instockForcastDetailEntity -> {
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setWarehouseId(instockForcastEntity.getWarehouseId());
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);
            inOutStockDTO.setSourceId(instockForcastEntity.getId());
            inOutStockDTO.setSourceCode(instockForcastEntity.getCode());
            inOutStockDTO.setBillDate(instockForcastEntity.getBillDate());
            inOutStockDTO.setSourceDetailId(instockForcastDetailEntity.getId());
            inOutStockDTO.setSkuId(instockForcastDetailEntity.getSkuId());
            inOutStockDTO.setSkuNo(instockForcastDetailEntity.getSkuNo());
            inOutStockDTO.setQty(instockForcastDetailEntity.getQty());
            inventorySkus.add(inOutStockDTO);
        });
        inventoryDto.setMembers(inventorySkus);
        inventoryTransCoreService.approveByType(inventoryDto);
    }

    @Override
    public InstockForcastEntity findByPurchaseOrderId(String purchaseOrderId) {
        InstockForcastEntity instockForcastEntity = lambdaQuery().eq(InstockForcastEntity::getPurchaseOrderId, purchaseOrderId).eq(InstockForcastEntity::getIsDeleted, Boolean.FALSE).one();
        return instockForcastEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void purchaseOrderUnApprove(String purchaseOrderId) {
        // 采购订单反审核对入库预报单的影响，只会存在一个，因为是审核通过触发会把所有的明细都一次性传过来的
        InstockForcastEntity instockForcastEntity = findByPurchaseOrderId(purchaseOrderId);
        if(Objects.isNull(instockForcastEntity)) {
            log.warn("采购订单id：【{}】未找到未删除的入库预报，不做库存反审核", purchaseOrderId);
            return;
        }
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
        inventoryUnApproveDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);
        inventoryUnApproveDTO.setBillId(instockForcastEntity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);
        // 更新入库预报为已删除
        instockForcastMapper.updateDeletedById(instockForcastEntity.getId());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void purchaseOrderUnApproveBatch(List<String> purchaseOrderIds) {
        purchaseOrderIds.stream().forEach(purchaseOrderId->{
            purchaseOrderUnApprove(purchaseOrderId);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finishDelivery(InstockForcastDTO.FinishDeliveryDTO dto) {
        String purchaseOrderId = dto.getPurchaseOrderId();
        InstockForcastEntity instockForcastEntity = findByPurchaseOrderId(purchaseOrderId);
        if(Objects.isNull(instockForcastEntity)) {
            log.error("采购订单id：【{}】未找到未删除的入库预报，", purchaseOrderId);
            // 此处报错
            throw new ServiceException("采购订单还未下推生成入库预报");
        }
        // 调用库存组件，更新库存信息
        InventoryInOutStockDTO inventoryDto = new InventoryInOutStockDTO();
        inventoryDto.setBusinessType(InventoryBusinessTypeEnum.PURCHASE_ORDER_FINISH.getCode());
        List<InventoryFinishDeliveryDetailDTO.AddDTO> members = dto.getMembers();
        List<InOutStockDTO> inventorySkus = Lists.newArrayListWithExpectedSize(members.size());
        members.stream().forEach(member -> {
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setWarehouseId(instockForcastEntity.getWarehouseId());
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);
            inOutStockDTO.setSourceId(instockForcastEntity.getId());
            inOutStockDTO.setSourceCode(instockForcastEntity.getCode());
            inOutStockDTO.setBillDate(instockForcastEntity.getBillDate());
            // 根据采购明细找入库预报明细
            InstockForcastDetailEntity instockForcastDetailEntity = instockForcastDetailService.find(instockForcastEntity.getId(), member.getPurchaseOrderDetailId());
            Optional.ofNullable(instockForcastDetailEntity).orElseThrow(()->new ServiceException("未找到入库预报明细信息"));
            inOutStockDTO.setSourceDetailId(instockForcastDetailEntity.getId());
            inOutStockDTO.setSkuId(member.getSkuId());
            inOutStockDTO.setSkuNo(member.getSkuNo());
            inOutStockDTO.setQty(member.getQty());
            inventorySkus.add(inOutStockDTO);
        });
        inventoryDto.setMembers(inventorySkus);
        inventoryTransCoreService.approveByType(inventoryDto);

    }


}

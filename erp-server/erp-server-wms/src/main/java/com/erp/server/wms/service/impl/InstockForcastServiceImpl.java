package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.InstockForcastDetailEntity;
import com.erp.model.wms.entity.InstockForcastEntity;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.InstockForcastMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Autowired
    private PoInstockDetailService poInstockDetailService;

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
        instockForcastEntity.setPurchaseOrderCode(dto.getPurchaseOrderCode());
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
            throw new ServiceException("采购订单未下推生成入库预报");
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
            // 结束交货相当于把剩余在途的数量全部清0
            inOutStockDTO.setQty(member.getQty());
            inventorySkus.add(inOutStockDTO);
        });
        inventoryDto.setMembers(inventorySkus);
        inventoryTransCoreService.approveByType(inventoryDto);

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void poChange(InstockForcastDTO.PoChangeDTO dto) {
        String purchaseOrderId = dto.getPurchaseOrderId();
        InstockForcastEntity instockForcastEntity = findByPurchaseOrderId(purchaseOrderId);
        if(Objects.isNull(instockForcastEntity)) {
            log.error("采购订单id：【{}】未找到未删除的入库预报，", purchaseOrderId);
            // 此处报错
            throw new ServiceException("采购订单未下推生成入库预报");
        }
        // 调用库存组件，更新库存信息，此处注意：不同的SKU规则不一样
        List<InstockForcastPoChangeDetailDTO.AddDTO> members = dto.getMembers();
        for(InstockForcastPoChangeDetailDTO.AddDTO member : members) {
            InventoryInOutStockRuleDTO inventoryDto = new InventoryInOutStockRuleDTO();
            inventoryDto.setBusinessType(InventoryBusinessTypeEnum.PURCHASE_ORDER_CHANGE.getCode());

            List<InOutStockDTO> inventorySkus = Lists.newArrayList();
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setWarehouseId(instockForcastEntity.getWarehouseId());
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);
            inOutStockDTO.setSourceId(instockForcastEntity.getId());
            inOutStockDTO.setSourceCode(instockForcastEntity.getCode());
            inOutStockDTO.setBillDate(instockForcastEntity.getBillDate());
            // 根据采购明细找入库预报明细
            InstockForcastDetailEntity instockForcastDetailEntity = instockForcastDetailService.find(instockForcastEntity.getId(), member.getPurchaseOrderDetailId());
            Optional.ofNullable(instockForcastDetailEntity).orElseThrow(()->new ServiceException("未找到入库预报明细信息"));
            // 更新入库预报明细数量
            instockForcastDetailService.updateQtyByPoChange(instockForcastDetailEntity.getId(), member.getQty());

            inOutStockDTO.setSourceDetailId(instockForcastDetailEntity.getId());
            inOutStockDTO.setSkuId(member.getSkuId());
            inOutStockDTO.setSkuNo(member.getSkuNo());
            inOutStockDTO.setQty(member.getQty());

            // 变更的数量（新旧采购订单数量对比）
            Integer inventoryQty = member.getQty() - member.getOriginQty();
            if(inventoryQty == 0) {
                log.info("SKU【{}】采购订单变更单没有发生数量改变，不处理", member.getSkuNo());
                continue;
            }
            // 采购订单明细id
            String purchaseOrderDetailId = member.getPurchaseOrderDetailId();
            // 获取原采购订单明细的收货信息
            List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(Lists.newArrayList(purchaseOrderDetailId));
            // 获取原采购订单明细的入库信息
            List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(Lists.newArrayList(purchaseOrderDetailId));

            String arriveStatus = member.getArriveStatus();
            Integer changeQty = 0;
            InventoryModeEnum inventoryModeEnum;

            Integer qty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                // 此处收货单需过滤为审核通过的，只有审核通过的才占用库存数量
                qty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(purchaseOrderDetailId)
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            Integer poQty = MathUtil.ZERO;
            if(CollectionUtils.isNotEmpty(poInstockDetailList)) {
                // 采购入库单，只有审核通过的才占用库存数量
                poQty = poInstockDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(purchaseOrderDetailId)
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            // 新的采购订单数量、原在途、待检数量综合计算得出
            // 已到货（包括结束交货），如果存在收货单存在待检的情况下，需考虑扣除待检的数量
            if(Objects.equals(ArrivalStatusEnum.ARRIVED.getCode(), arriveStatus)) {
                // 此处需考虑待检的数量，因为数量被拆分成了在途和待检
                changeQty = member.getQty() - qty - poQty;
                inventoryModeEnum = changeQty > 0 ? InventoryModeEnum.IN_STOCK : InventoryModeEnum.OUT_STOCK;
                changeQty = Math.abs(changeQty);
            } else {
                changeQty = member.getQty() - member.getOriginQty();
                inventoryModeEnum = changeQty > 0 ? InventoryModeEnum.IN_STOCK : InventoryModeEnum.OUT_STOCK;
                changeQty = Math.abs(changeQty);
            }
            inOutStockDTO.setQty(changeQty);
            inventorySkus.add(inOutStockDTO);
            inventoryDto.setMembers(inventorySkus);

            List<TransactionRuleDTO> rules = Lists.newArrayList();
            TransactionRuleDTO transactionRuleDTO = new TransactionRuleDTO();
            transactionRuleDTO.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
            transactionRuleDTO.setInventoryStatus(InventoryStatusEnum.IN_TRANSIT);
            transactionRuleDTO.setTransactionMode(inventoryModeEnum);
            rules.add(transactionRuleDTO);
            inventoryDto.setRules(rules);

            inventoryTransCoreService.approveByRule(inventoryDto);
        }
    }


}

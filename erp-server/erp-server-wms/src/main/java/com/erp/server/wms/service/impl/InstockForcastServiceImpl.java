package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.mapper.InstockForcastMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Resource
    private InstockForcastDetailService instockForcastDetailService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private InstockForcastMapper instockForcastMapper;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

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
        // String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.RKYB, BusinessNoTypeEnum.CODE_RKYB.getCode()));
        String code =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_RKYB);
        // feign调用取不到登录人信息，已解决
        LoginUser loginUser = UserContext.getDefaultLoginUser();
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
        if(Objects.isNull(warehouseDetail) || CharSequenceUtil.isEmpty(warehouseDetail.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        // 仓库组织
        String orgId = warehouseDetail.getOrgId();
        instockForcastEntity.setCode(code);
        instockForcastEntity.setPurchaseOrderId(purchaseOrderId);
        instockForcastEntity.setPurchaseOrderCode(dto.getPurchaseOrderCode());
        instockForcastEntity.setOrgId(orgId);
        instockForcastEntity.setWarehouseId(dto.getWarehouseId());
        // 入库预报取采购订单日期
        instockForcastEntity.setBillDate(dto.getBillDate());

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
        inventoryDto.setParamList(inventorySkus);
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
        inventoryDto.setParamList(inventorySkus);
        inventoryTransCoreService.approveByType(inventoryDto);

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void poChange(InstockForcastDTO.PoChangeDTO dto) {
        String purchaseOrderId = dto.getPurchaseOrderId();
        String purchaseChangeOrderId = dto.getPurchaseChangeOrderId();
        String purchaseChangeOrderCode = dto.getPurchaseChangeOrderCode();
        InstockForcastEntity instockForcastEntity = findByPurchaseOrderId(purchaseOrderId);
        if(Objects.isNull(instockForcastEntity)) {
            log.error("采购订单id：【{}】未找到未删除的入库预报，", purchaseOrderId);
            // 此处报错
            throw new ServiceException("采购订单未下推生成入库预报");
        }
        List<InOutStockDTO> inList = new ArrayList<>();
        List<InOutStockDTO> outList = new ArrayList<>();
        // 调用库存组件，更新库存信息，此处注意：不同的SKU规则不一样
        List<InstockForcastPoChangeDetailDTO.AddDTO> members = dto.getMembers();
        for(InstockForcastPoChangeDetailDTO.AddDTO member : members) {
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setWarehouseId(instockForcastEntity.getWarehouseId());
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.PURCHASE_ORDER_CHANGE);
            inOutStockDTO.setSourceId(purchaseChangeOrderId);
            inOutStockDTO.setSourceCode(purchaseChangeOrderCode);
            inOutStockDTO.setBillDate(instockForcastEntity.getBillDate());
            // 根据采购明细找入库预报明细
            InstockForcastDetailEntity instockForcastDetailEntity = instockForcastDetailService.find(instockForcastEntity.getId(), member.getPurchaseOrderDetailId());
            Optional.ofNullable(instockForcastDetailEntity).orElseThrow(()->new ServiceException("未找到入库预报明细信息"));
            // 更新入库预报明细数量
            instockForcastDetailService.updateQtyByPoChange(instockForcastDetailEntity.getId(), member.getQty());

            inOutStockDTO.setSourceDetailId(member.getPurchaseOrderChangeDetailId());
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
            // 原采购订单入库明细id
            List<String> receiveDetailIds = CollUtil.isNotEmpty(receiveDetailList) ? receiveDetailList.stream().map(WarehouseReceiveDetailEntity::getId).collect(Collectors.toList()) : new ArrayList<>();
            // 获取原采购订单明细的入库信息
            List<PoInstockDetailEntity> poInstockDetailList = poInstockDetailService.listDetailByPodIds(Lists.newArrayList(purchaseOrderDetailId));
            // 获取原退货单明细的退货信息
            List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(Lists.newArrayList(purchaseOrderDetailId));

            String executionStatus = member.getExecutionStatus();
            Integer changeQty = 0;
            InventoryModeEnum inventoryModeEnum;

            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                // 此处收货单需过滤为审核通过的，只有审核通过的才占用库存数量
                receiveQty = receiveDetailList.stream().filter(e -> Objects.equals(e.getPurchaseOrderDetailId(),purchaseOrderDetailId)
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            // 采购入库单（直接下推的无收货单的）入库数量
            Integer poUnRecQty = MathUtil.ZERO;
            // 采购入库单（收货单下推的）入库数量
            Integer poRecQty = MathUtil.ZERO;
            if(CollectionUtils.isNotEmpty(poInstockDetailList)) {
                // 采购入库单（直接下推的无收货单的），会减少在途，增加可用
                poUnRecQty = poInstockDetailList.stream().filter(e -> Objects.equals(e.getPurchaseOrderDetailId(), purchaseOrderDetailId)
                        // 采购入库单的来源明细id=采购订单明细id
                        && Objects.equals(e.getSourceDetailId(), purchaseOrderDetailId)
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                // 采购入库单（收货单下推的）入库数量，会减少待检数量，增加可用
                poRecQty = poInstockDetailList.stream().filter(e -> Objects.equals(e.getPurchaseOrderDetailId(), purchaseOrderDetailId)
                        // 采购入库单的来源明细id在收货单
                        && receiveDetailIds.contains(e.getSourceDetailId())
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            Integer returnQty = MathUtil.ZERO;
            if(CollUtil.isNotEmpty(returnOrderDetailList)) {
                // 退货单（退货补货的才会导致在途数量变化）
                returnQty = returnOrderDetailList.stream().filter(e -> Objects.equals(e.getPurchaseOrderDetailId(), purchaseOrderDetailId)
                        && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode())
                        && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                        && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            log.info("采购订单【{}】对应的采购订单明细【{}】执行状态【{}】",instockForcastEntity.getPurchaseOrderCode(), member.getPurchaseOrderDetailId(), executionStatus);
            log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细采购数量:{}", member.getPurchaseOrderDetailId(), instockForcastEntity.getPurchaseOrderCode(), member.getQty());
            log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细采购数量:{}，收货数量：{}，采购入库[收货单下推]数量：{}, 采购入库[无收货单]数量：{}, ,退货补货退货数量：{}，原采购明细数量：{}", member.getPurchaseOrderDetailId(), instockForcastEntity.getPurchaseOrderCode(), member.getQty(),
                    receiveQty, poRecQty, poUnRecQty, returnQty, member.getOriginQty());
            // 执行状态为已完成、已关闭（包括结束交货）
            if(Objects.equals(ExecutionStatusEnum.FINISH.getCode(), executionStatus) || Objects.equals(ExecutionStatusEnum.CLOSED.getCode(), executionStatus)) {
                // 新采购数量- (待检 + 退货在途 + 可用)
                changeQty = member.getQty() - ((receiveQty - poRecQty) + returnQty + (poUnRecQty + poRecQty));
                inventoryModeEnum = changeQty > 0 ? InventoryModeEnum.IN_STOCK : InventoryModeEnum.OUT_STOCK;
                changeQty = Math.abs(changeQty);
            } else {
                // 新采购订单数量 - (原采购订单数量 - 原采购订单入库数量 + 可用 + 待检)
                // changeQty = member.getQty() - (member.getOriginQty() - (receiveQty + poUnRecQty) + (poUnRecQty + poRecQty) + returnQty + (receiveQty - poRecQty));
                // 直接调整为差额
                changeQty = member.getQty() - member.getOriginQty();
                inventoryModeEnum = changeQty > 0 ? InventoryModeEnum.IN_STOCK : InventoryModeEnum.OUT_STOCK;
                changeQty = Math.abs(changeQty);
            }
            inOutStockDTO.setQty(changeQty);
            //新增
            if (InventoryModeEnum.IN_STOCK.equals(inventoryModeEnum)){
                inList.add(inOutStockDTO);
            }else if (InventoryModeEnum.OUT_STOCK.equals(inventoryModeEnum)){
                outList.add(inOutStockDTO);
            }
        }
        if (CollectionUtils.isNotEmpty(inList)){
            InventoryInOutStockDTO inStockDTO = new InventoryInOutStockDTO();
            inStockDTO.setParamList(inList);
            inStockDTO.setBusinessType(InventoryBusinessTypeEnum.PURCHASE_ORDER_CHANGE_IN.getCode());
            inventoryTransCoreService.approveByType(inStockDTO);
        }
        if (CollectionUtils.isNotEmpty(outList)){
            InventoryInOutStockDTO outStockDTO = new InventoryInOutStockDTO();
            outStockDTO.setParamList(outList);
            outStockDTO.setBusinessType(InventoryBusinessTypeEnum.PURCHASE_ORDER_CHANGE_OUT.getCode());
            inventoryTransCoreService.approveByType(outStockDTO);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finishDeliveryBatch(List<InstockForcastDTO.FinishDeliveryDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            throw new ServiceException("库存交易数据不能为空");
        }
        dataList.stream().forEach(data->finishDelivery(data));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateByPurchaseOrderBatch(List<InstockForcastDTO.AddDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            throw new ServiceException("库存交易数据不能为空");
        }
        dataList.stream().forEach(this::generateByPurchaseOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void poChangeBatch(List<InstockForcastDTO.PoChangeDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            throw new ServiceException("库存交易数据不能为空");
        }
        dataList.stream().forEach(this::poChange);
    }


}

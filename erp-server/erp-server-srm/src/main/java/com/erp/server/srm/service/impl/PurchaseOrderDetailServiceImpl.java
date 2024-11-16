package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.WaitDeliveryCycleEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.PurchaseOrderDetailDTO;
import com.erp.model.srm.entity.PurchaseOrderDetailEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.convert.PurchaseOrderConverter;
import com.erp.server.srm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PurchaseOrderDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购订单明细表（已确认） 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private SupplierFeign supplierFeign;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;
    @Resource
    private DeliveryOrderService deliveryOrderService;
    @Resource
    private DeliveryOrderDetailService deliveryOrderDetailService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PurchaseOrderDetailDTO.AddDTO addDTO) {
        PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, purchaseOrderDetailEntity);

        // 数据处理
        handleData(purchaseOrderDetailEntity);

        log.info("开始新增采购订单明细表（已确认）");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        purchaseOrderDetailEntity.setCode(code);
        boolean save = super.save(purchaseOrderDetailEntity);
        if (!save) {
            throw new ServiceException("采购订单明细表（已确认）保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "采购订单明细表（已确认）", purchaseOrderDetailEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, purchaseOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(purchaseOrderDetailEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PurchaseOrderDetailDTO.UpdateDTO updateDTO) {
        PurchaseOrderDetailEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw  new ServiceException(ApiError.NOT_EXIST_BILL, "采购订单明细表（已确认）");
        }
        PurchaseOrderDetailEntity purchaseOrderDetailEntity = BeanMapperUtils.map(PurchaseOrderDetailEntity.class, updateDTO);

        // 数据处理
        handleData(purchaseOrderDetailEntity);
        log.info("编辑 开始修改采购订单明细表（已确认）数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseOrderDetailEntity);
        if (!save) {
            throw new ServiceException("采购订单明细表（已确认）保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录采购订单明细表（已确认）日志数据，单号：【{}】", purchaseOrderDetailEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), purchaseOrderDetailEntity.getCode(), "采购订单明细表（已确认）");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, purchaseOrderDetailEntity, null, purchaseOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void syncScmPurchaseOrderDetail(String id, List<String> detailIds, String executionStatus) {
        if (StringUtils.isEmpty(id) || CollectionUtils.isEmpty(detailIds) || StringUtils.isEmpty(executionStatus)) {
            return;
        }
        List<PurchaseOrderEntity> purchaseOrderEntities = purchaseOrderFeign.getPurchaseOrderByIds(Collections.singleton(id));
        if (CollectionUtils.isEmpty(purchaseOrderEntities)) {
            return;
        }
        List<com.erp.model.scm.entity.PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderFeign.getPurchaseOrderDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            return;
        }
        List<PurchaseOrderSupplierEntity> supplierByOrderIds = supplierFeign.getSupplierByOrderIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(supplierByOrderIds)) {
            return;
        }
        purchaseOrderDetailList.forEach(purchaseOrderDetailEntity -> {
            PurchaseOrderDetailEntity newEntity = PurchaseOrderConverter.INSTANCE.scmPurchaseOrderToSrmPurchaseOrderDetail(purchaseOrderEntities.get(0), purchaseOrderDetailEntity, supplierByOrderIds.get(0));
            PurchaseOrderDetailEntity oldEntity = this.getBySrmDetailId(purchaseOrderDetailEntity.getId());
            if (Objects.isNull(oldEntity)){
                this.save(newEntity);
            }else {
                newEntity.setId(oldEntity.getId());
                this.updateById(newEntity);
            }
        });
    }

    private PurchaseOrderDetailEntity getBySrmDetailId(String detailId){
        List<PurchaseOrderDetailEntity> list = this.lambdaQuery().eq(PurchaseOrderDetailEntity::getPurchaseOrderDetailId, detailId).list();
        if (CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }else {
            return null;
        }
    }
    @Override
    public void updateBySrmOrderIds(List<String> ids,List<String> detailIds, String executionStatus) {
        if (StringUtils.isEmpty(executionStatus)){
            executionStatus = ExecutionStatusEnum.TO_BE_CONFIRM.getCode();
        }
        if(CollectionUtils.isNotEmpty(ids)){
            lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderId, ids).set(PurchaseOrderDetailEntity::getExecutionStatus,executionStatus).update();
        }
        if (CollectionUtils.isNotEmpty(detailIds)){
            lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderDetailId, detailIds).set(PurchaseOrderDetailEntity::getExecutionStatus,executionStatus).update();
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PurchaseOrderDetailEntity purchaseOrderDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public Integer srmWaitDeliveryCount(String supplierId,String code){
        return baseMapper.srmWaitDeliveryCount(supplierId,code);
    }

    @Override
    public void saveOrUpdatePurchaseOrderDetail(List<com.erp.model.scm.entity.PurchaseOrderDetailEntity> ext) {
        if (CollectionUtils.isEmpty(ext)){
            return;
        }
        List<String> detailIds = ext.stream().map(com.erp.model.scm.entity.PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> list = lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderDetailId, detailIds).list();
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        //同步已存在的详情变更信息
        list.forEach(entity -> {
            com.erp.model.scm.entity.PurchaseOrderDetailEntity old = ext.stream().filter(e -> e.getId().equals(entity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(old)){
                entity.setPurchaseQty(old.getPurchaseQty());
                entity.setPurchaseAmount(old.getPurchaseAmount());
                entity.setTaxRate(old.getTaxRate());
                this.updateById(entity);
            }
        });
    }

    @Override
    public PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> pagingDTO) {
        PurchaseOrderDTO.SrmSearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());

        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        query.setOrders(buildOrders(pagingDTO.getParams().getSortList()));
        IPage<PurchaseOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<PurchaseOrderDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //重新赋值待发货字段逻辑
        doWaitDeliveryPurchaseOrder(records);
        return new PagingVO(pageData);
    }

    @Override
    public PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(PurchaseOrderDTO.SrmSearchParamDTO pagingDTO) {
        PurchaseOrderDTO.ListDTO dto = new PurchaseOrderDTO.ListDTO();
        List<PurchaseOrderDTO.ListDTO> list = this.baseMapper.srmPurchaseOrderList(pagingDTO);
        if (CollectionUtils.isEmpty(list)) {
            return countPurchaseOrder(dto, list);
        }
        //数据赋值处理
        doWaitDeliveryPurchaseOrder(list);
        //汇总
        return countPurchaseOrder(dto, list);
    }

    private void doWaitDeliveryPurchaseOrder(List<PurchaseOrderDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        // 采购订单明细id集合
        List<String> podIds = records.stream().map(PurchaseOrderDTO.ListDTO::getPurchaseDetailId).collect(Collectors.toList());
        List<String> ids = records.stream().map(PurchaseOrderDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = deliveryOrderDetailService.listDetailDTOByDetailSourceIds(podIds);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(ids);
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        records.forEach(obj -> {
            //已收货数量
            Integer receiveQty = MathUtil.ZERO;
            //已送货数量
            Integer deliveryQty = MathUtil.ZERO;
            //有送货单的收货数量
            Integer hasDeliveryReceiveQty = MathUtil.ZERO;
            //无送货单收货数量
            Integer unDeliveryReceiveQty = MathUtil.ZERO;
            //无收货单的入库数量
            Integer unReceiveInstockQty = MathUtil.ZERO;
            //收发差异
            Integer diffSendAndReceive = MathUtil.ZERO;
            //退货补货数量
            Integer returnQty = MathUtil.ZERO;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                hasDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSourceType())
                                && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                unDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            obj.setReceiveQty(receiveQty);
            //无收货单的入库数量
            if (CollectionUtils.isNotEmpty(stockInDetailList)){
                // 采购入库单（无收货单），只有审核通过的才占用库存数量
                unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getSourceDetailId(), obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            // 退货单（退货补货的才会导致在途数量变化）
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)){
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(obj.getPurchaseDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //已送货数量
            if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                deliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId()) )
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                //收发差异
                //发货数量 - 已审核收货数量
                Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(obj.getPurchaseDetailId())
                                && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
            }
            obj.setWaitReceiveQty(deliveryQty);
            //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
            obj.setDeliveryQty(obj.getPurchaseQty() - deliveryQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty );
            //交货周期
            Boolean deliveryCycleFlag = false;
            if(Objects.nonNull(obj.getDeliveryCycle())){
                if (obj.getDeliveryCycle() < 0){
                    obj.setDeliveryCycleName(String.format("已超期%s天", obj.getDeliveryCycle() * -1));
                    deliveryCycleFlag = true;
                }else if (7 >= obj.getDeliveryCycle() && obj.getDeliveryCycle()>= 0){
                    obj.setDeliveryCycleName(String.format("%s天后超期", obj.getDeliveryCycle()));
                    deliveryCycleFlag = true;
                }else if (30 >= obj.getDeliveryCycle() && obj.getDeliveryCycle()> 7){
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_ONE_MONTH.getName());
                }else if (60 >= obj.getDeliveryCycle() && obj.getDeliveryCycle()> 30){
                    obj.setDeliveryCycleName(WaitDeliveryCycleEnum.IN_TWO_MONTH.getName());
                }else if (obj.getDeliveryCycle()> 60){
                    obj.setDeliveryCycleName("2个月以上");
                }
            }
            obj.setDeliveryCycleFlag(deliveryCycleFlag);
        });
    }

    private PurchaseOrderDTO.ListDTO countPurchaseOrder(PurchaseOrderDTO.ListDTO dto, List<PurchaseOrderDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)){
            dto.setPurchaseQty(MathUtil.ZERO);
            dto.setReceiveQty(MathUtil.ZERO);
            dto.setDeliveryQty(MathUtil.ZERO);
            dto.setWaitReceiveQty(MathUtil.ZERO);
            dto.setStockInQty(MathUtil.ZERO);
            dto.setReturnQty(MathUtil.ZERO);
            return dto;
        }
        dto.setPurchaseQty(list.stream().filter(e -> Objects.nonNull(e.getPurchaseQty())).mapToInt(PurchaseOrderDTO.ListDTO::getPurchaseQty).sum());
        dto.setReceiveQty(list.stream().filter(e -> Objects.nonNull(e.getReceiveQty())).mapToInt(PurchaseOrderDTO.ListDTO::getReceiveQty).sum());
        dto.setWaitReceiveQty(list.stream().filter(e -> Objects.nonNull(e.getWaitReceiveQty())).mapToInt(PurchaseOrderDTO.ListDTO::getWaitReceiveQty).sum());
        dto.setDeliveryQty(list.stream().filter(e -> Objects.nonNull(e.getDeliveryQty())).mapToInt(PurchaseOrderDTO.ListDTO::getDeliveryQty).sum());
        dto.setStockInQty(list.stream().filter(e -> Objects.nonNull(e.getStockInQty())).mapToInt(PurchaseOrderDTO.ListDTO::getStockInQty).sum());
        dto.setReturnQty(list.stream().filter(e -> Objects.nonNull(e.getReturnQty())).mapToInt(PurchaseOrderDTO.ListDTO::getReturnQty).sum());
        return dto;
    }
}

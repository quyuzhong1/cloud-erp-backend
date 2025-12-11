package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.inventory.VirtualTransRuleDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.VirtualFlowRefactorEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class VirtualFlowRefactorServiceImpl implements VirtualFlowRefactorService {

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgVirtualTransRulesService cfgVirtualTransRulesService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;


    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;

    @Resource
    @Qualifier("b2bVirtualFlowRefactorPool")
    private ExecutorService b2bVirtualFlowRefactorPool;

    @Resource
    @Qualifier("b2cVirtualFlowRefactorPool")
    private ExecutorService b2cVirtualFlowRefactorPool;

    @Resource
    @Qualifier("firstMileVirtualFlowRefactorPool")
    private ExecutorService firstMileVirtualFlowRefactorPool;

    @Resource
    @Qualifier("allocationVirtualFlowRefactorPool")
    private ExecutorService allocationVirtualFlowRefactorPool;

    @Resource
    @Qualifier("virtualFlowRefactorPool")
    private ExecutorService virtualFlowRefactorPool;

    @Override
    public void rebuildFlow(String jobParam) {
        List<String> orderTypeList = CharSequenceUtil.isNotBlank(jobParam) ? Arrays.asList(jobParam.split(",")) :
                Arrays.stream(VirtualFlowRefactorEnum.values()).map(VirtualFlowRefactorEnum::getCode).collect(Collectors.toList());
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow start");

        // 使用自定义线程池处理订单类型
        List<CompletableFuture<Void>> futures = orderTypeList.stream()
                .map(orderType -> CompletableFuture.runAsync(() -> processOrderType(orderType), virtualFlowRefactorPool))
                .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow end");
    }

    /**
     * 不同订单类型处理
     * @author will
     * @date 2025/4/8 10:18
     * @param orderType
     * @return void
     */
    private void processOrderType(String orderType) {
        List<String> ignoreSkuIds = getIgnoreSkuIds();
        if (VirtualFlowRefactorEnum.B2B.getCode().equals(orderType)) {
            rebuildB2bFlow(ignoreSkuIds);
        } else if (VirtualFlowRefactorEnum.B2C.getCode().equals(orderType)) {
            rebuildB2cFlow(ignoreSkuIds);
        } else if (VirtualFlowRefactorEnum.FIRST_MILE.getCode().equals(orderType)) {
            rebuildFirstMileFlow(ignoreSkuIds);
        } else if (VirtualFlowRefactorEnum.WAREHOUSE_ALLOCATION.getCode().equals(orderType)) {
            rebuildVirtualWarehouseAllocationFlow(ignoreSkuIds);
        }
    }

    /**
     * 分货单
     * @author will
     * @date 2025/4/2 16:17
     */
    private void rebuildVirtualWarehouseAllocationFlow(List<String> ignoreSkuIds) {
        List<VirtualWarehouseAllocationEntity> list =  virtualWarehouseAllocationService.rebuildVirtualWarehouseAllocationFlow();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 使用自定义线程池处理订单类型
        List<CompletableFuture<Void>> futures = list.stream()
                .map(allocationEntity -> CompletableFuture.runAsync(() -> handleAllocation(allocationEntity,ignoreSkuIds)
                , allocationVirtualFlowRefactorPool))
                .collect(Collectors.toList());
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     *
     * @author will
     * @date 2025/4/21 14:57
     * @param allocationEntity
     * @return void
     */
    private void handleAllocation (VirtualWarehouseAllocationEntity allocationEntity,List<String> ignoreSkuIds) {
        //查找所有明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.listByMainIdList(Collections.singletonList(allocationEntity.getId()));

        //查询历史bom信息
        List<String> bomSkuIdList = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(bomSkuIdList);

        if (CollectionUtils.isNotEmpty(detailList)) {
            String type = allocationEntity.getType();
            switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
                case ALLOCATION:
                    VirtualInventoryStockDTO.StockParamDTO allocationDto = getAllocationDto(VirtualInventoryBusinessTypeEnum.IN_USABLE, detailList, allocationEntity);
                    allocationDto.setIsSplitBom(Boolean.FALSE);
                    getSelfBean().approve(allocationDto,ignoreSkuIds,bomChildrenSkuList);
                    break;
                case TRANSFER:
                    VirtualInventoryStockDTO.TransferParamDTO dto = getTransferDTO(allocationEntity, detailList);
                    getSelfBean().approveTransfer(dto,ignoreSkuIds);
                    break;
                case CANCEL:
                    VirtualInventoryStockDTO.StockParamDTO cancelDto = getCancelDto(VirtualInventoryBusinessTypeEnum.OUT_USABLE, detailList, allocationEntity);
                    cancelDto.setIsSplitBom(Boolean.FALSE);
                    getSelfBean().approve(cancelDto,ignoreSkuIds,bomChildrenSkuList);
                    break;
                default:
                    throw new ServiceException(ApiError.ERROR_400);
            }
        }
    }

    /**
     * 新增分货
     * @author will
     * @date 2025/4/2 16:35
     * @param inUsable
     * @param detailList
     * @param allocationEntity
     * @return com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.StockParamDTO
     */
    private static VirtualInventoryStockDTO.StockParamDTO getAllocationDto(VirtualInventoryBusinessTypeEnum inUsable, List<VirtualWarehouseAllocationDetailEntity> detailList, VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
        allocationDto.setBusinessType(inUsable.getCode());
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(ObjectUtil.isEmpty(allocationEntity.getHandleDate()) ? allocationEntity.getCreateTime().toLocalDate(): allocationEntity.getHandleDate() );
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getToVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            outInStockDTO.setTradeTime(allocationEntity.getCreateTime());
            allocationParamList.add(outInStockDTO);
        });
        allocationDto.setParamList(allocationParamList);
        return allocationDto;
    }
    /**
     * 调拨分货
     * @author will
     * @date 2025/4/2 16:36
     * @param allocationEntity
     * @param detailList
     * @return com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.TransferParamDTO
     */
    private static VirtualInventoryStockDTO.TransferParamDTO getTransferDTO(VirtualWarehouseAllocationEntity allocationEntity, List<VirtualWarehouseAllocationDetailEntity> detailList) {
        VirtualInventoryStockDTO.TransferParamDTO dto = new VirtualInventoryStockDTO.TransferParamDTO();
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_USABLE.getCode());
        List<VirtualInventoryStockDTO.TransferStockDTO> paramList = new ArrayList<>();

        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.TransferStockDTO outInStockDTO = new VirtualInventoryStockDTO.TransferStockDTO();
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(ObjectUtil.isEmpty(allocationEntity.getHandleDate()) ? allocationEntity.getCreateTime().toLocalDate(): allocationEntity.getHandleDate());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualCurWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualTargetWarehouseId(detailDto.getToVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            outInStockDTO.setTradeTime(allocationEntity.getCreateTime());
            paramList.add(outInStockDTO);
        });
        dto.setParamList(paramList);
        return dto;
    }

    /**
     * 取消发货
     * @author will
     * @date 2025/4/2 16:36
     * @param inUsable
     * @param detailList
     * @param allocationEntity
     * @return com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO.StockParamDTO
     */
    private static VirtualInventoryStockDTO.StockParamDTO getCancelDto(VirtualInventoryBusinessTypeEnum inUsable, List<VirtualWarehouseAllocationDetailEntity> detailList, VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
        allocationDto.setBusinessType(inUsable.getCode());
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(ObjectUtil.isEmpty(allocationEntity.getHandleDate()) ? allocationEntity.getCreateTime().toLocalDate(): allocationEntity.getHandleDate());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            outInStockDTO.setTradeTime(allocationEntity.getCreateTime());
            allocationParamList.add(outInStockDTO);
        });
        allocationDto.setParamList(allocationParamList);
        return allocationDto;
    }

    /**
     * b2b流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2bFlow(List<String> ignoreSkuIds) {
       List<VirtualFlowRefactorDTO.OutInStockDTO> list =  soDeliveryNoticeService.rebuildB2bVirtualFlow();
       if (CollUtil.isEmpty(list)) {
           return;
       }
        //销售出库单只出单品sku
        List<String> skuIdList = list.stream().filter(obj ->SourceTypeEnum.SO_OUTSTOCK.getCode().equals(obj.getSourceType().getCode())).map(VirtualFlowRefactorDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildList = CollUtil.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.listBomChildBySkuIds(skuIdList);
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildList.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}", obj.getType(), obj.getParentSkuId())));
        List<VirtualFlowRefactorDTO.OutInStockDTO> singleList = list.stream().filter(obj -> {
            String key = CharSequenceUtil.format("{}-{}", BomTypeEnum.COMBINATION.getType(), obj.getSkuId());
            //销售出库单过滤组合品
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(obj.getSourceType().getCode()) && CollUtil.isNotEmpty(bomMap.get(key))) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }).collect(Collectors.toList());

        //查询历史bom信息
        List<String> bomSkuIdList = singleList.stream().map(VirtualFlowRefactorDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(bomSkuIdList);

        Map<String, List<VirtualFlowRefactorDTO.OutInStockDTO>> map = CollUtil.isEmpty(singleList) ? new HashMap<>() : singleList.stream().collect(Collectors.groupingBy(obj -> obj.getSourceType().getCode().concat(obj.getSourceId())));

        List<CompletableFuture<Void>> futures = map.values().stream().map(value -> CompletableFuture.runAsync(() -> {
            String sourceType = value.get(0).getSourceType().getCode();
            List<VirtualInventoryStockDTO.OutInStockDTO> params = BeanUtil.copyToList(value, VirtualInventoryStockDTO.OutInStockDTO.class);
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(params);
            //b2b需冻结
            if (SourceTypeEnum.SO_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            }
            //发货通知单需冻结
            if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_HANDLE.getCode());
            }
            //加工单需出库
            if (SourceTypeEnum.MACHINE_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.MACHINE_INFO_CHILD_OUT.getCode());
            }
            //直接调拨单需出库
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
            }
            //销售出库单需出库
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode());
            }
            //更新库存
            getSelfBean().approve(dto,ignoreSkuIds,bomChildrenSkuList);
        }, b2bVirtualFlowRefactorPool)).collect(Collectors.toList());
        // 等待所有内层任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    }

    /**
     * b2c流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2cFlow(List<String> ignoreSkuIds) {
        List<VirtualFlowRefactorDTO.OutInStockDTO> list =  soB2cDeliveryService.rebuildB2cVirtualFlow();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //查询历史bom信息
        List<String> bomSkuIdList = list.stream().map(VirtualFlowRefactorDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(bomSkuIdList);

        Map<String, List<VirtualFlowRefactorDTO.OutInStockDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceType().getCode().concat(obj.getSourceId()).concat(obj.getBusinessType())));
        List<CompletableFuture<Void>> futures = map.values().stream().map(value -> CompletableFuture.runAsync(() -> {
            String sourceType = value.get(0).getSourceType().getCode();
            List<VirtualInventoryStockDTO.OutInStockDTO> params = BeanUtil.copyToList(value, VirtualInventoryStockDTO.OutInStockDTO.class);
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(params);
            //b2c发货单需冻结
            if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(sourceType) && VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY.getType().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode());
            }
            //发货通知单需冻结
            if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(sourceType) && VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY_CANCEL.getType().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY_CANCEL.getCode());
            }
            //直接调拨单需出库
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
            }
            //销售出库单需出库
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(sourceType) && VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getType().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode());
            }
            //旺店通销售出库单需出库
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(sourceType) && VirtualInventoryBusinessTypeEnum.OUT_USABLE.getType().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.OUT_USABLE.getCode());
            }
            //更新库存
            getSelfBean().approve(dto,ignoreSkuIds,bomChildrenSkuList);
        }, b2cVirtualFlowRefactorPool)).collect(Collectors.toList());
        // 等待所有内层任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 头程流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildFirstMileFlow(List<String> ignoreSkuIds) {
        List<VirtualFlowRefactorDTO.OutInStockDTO> list =  requisitionApplicationService.rebuildFirstMileVirtualFlow();
        if (CollUtil.isEmpty(list)) {
            return;
        }

        //直接调拨单只出单品sku
        List<String> skuIdList = list.stream().filter(obj ->SourceTypeEnum.TRANSFER_INFO.getCode().equals(obj.getSourceType().getCode())).map(VirtualFlowRefactorDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildList = CollUtil.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.listBomChildBySkuIds(skuIdList);
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildList.stream().collect(Collectors.groupingBy(obj -> StrUtil.format("{}-{}", obj.getType(), obj.getParentSkuId())));
        List<VirtualFlowRefactorDTO.OutInStockDTO> singleList = list.stream().filter(obj -> {
            String key = StrUtil.format("{}-{}", BomTypeEnum.COMBINATION.getType(), obj.getSkuId());
            //直接调拨单过滤组合品
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(obj.getSourceType().getCode()) && CollUtil.isNotEmpty(bomMap.get(key))) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }).collect(Collectors.toList());
        Map<String, List<VirtualFlowRefactorDTO.OutInStockDTO>> map = CollUtil.isEmpty(singleList) ? new HashMap<>() : singleList.stream().collect(Collectors.groupingBy(obj -> obj.getSourceType().getCode().concat(obj.getSourceId())));

        //查询历史bom信息
        List<String> bomSkuIdList = singleList.stream().map(VirtualFlowRefactorDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(bomSkuIdList);

        List<CompletableFuture<Void>> futures = map.values().stream().map(value -> CompletableFuture.runAsync(() -> {
            String sourceType = value.get(0).getSourceType().getCode();
            List<VirtualInventoryStockDTO.OutInStockDTO> params = BeanUtil.copyToList(value, VirtualInventoryStockDTO.OutInStockDTO.class);
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(params);
            //要货申请需冻结
            if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            }
            //加工单需出库
            if (SourceTypeEnum.MACHINE_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.MACHINE_INFO_CHILD_OUT.getCode());
            }
            //直接调拨单需出库
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
            }
            //更新库存
            getSelfBean().approve(dto,ignoreSkuIds,bomChildrenSkuList);
        }, firstMileVirtualFlowRefactorPool)).collect(Collectors.toList());
        // 等待所有内层任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 获取自身的bean
     * @author will
     * @date 2025/4/8 10:20
     * @return VirtualFlowRefactorServiceImpl
     */
    private VirtualFlowRefactorServiceImpl getSelfBean() {
        return ApplicationContextUtils.getBean(VirtualFlowRefactorServiceImpl.class);
    }







//---------------------------------------------------------------------------调拨流水---------------------------------------------------------------------------------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTransfer(VirtualInventoryStockDTO.TransferParamDTO dto,List<String> ignoreSkuIds) {
        ValidatorUtil.validateEntity(dto);
        this.approveTransfer(dto.getParamList(), dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollUtil.isEmpty(dto.getRules()) ? true : false,ignoreSkuIds);
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void approveTransfer(List<T> paramList, List<VirtualTransRuleDTO.StockParamDTO> ruleList, VirtualInventoryBusinessTypeEnum businessType, Boolean byType,List<String> ignoreSkuIds) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSON.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<VirtualTransRuleDTO.StockParamDTO> stockParamList = ruleList;
        //如果走配置则取已配置的规则
        if(Objects.equals(byType,Boolean.TRUE)) {
            stockParamList = this.wrapTransactionRule(businessType);
        }
        // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);
        this.stockHandlerTransfer(paramList, businessType, stockParamList, transactionNo,ignoreSkuIds);

        stopwatch.stop();
        log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandlerTransfer(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo,List<String> ignoreSkuIds) {
        // 通过对sku id 仓库id 仓位 顺序执行, 避免多线程死锁
        Comparator<VirtualInventoryStockDTO.StockBaseDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.StockBaseDTO::getSkuId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getVirtualWarehouseId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getWarehouseId)
                .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode() : "");
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            // 当前仓出入库业务处理
            VirtualInventoryStockDTO.TransferStockDTO param = (VirtualInventoryStockDTO.TransferStockDTO)baseParam;

            VirtualInventoryStockDTO.TransferDTO curWareInOrOutStock = this.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryOperationModeEnum.APPROVE);
            if(ignoreSkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            // 目的仓出入库业务处理
            VirtualInventoryStockDTO.TransferDTO targetWareInOrOutStock = this.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET, InventoryOperationModeEnum.APPROVE);
            Stream.of(curWareInOrOutStock, targetWareInOrOutStock)
                    .sorted(Comparator.comparing(VirtualInventoryStockDTO.TransferDTO::getWarehouseId)
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getVirtualWarehouseId()) ? x.getVirtualWarehouseId() : "")
                            .thenComparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): "")
                    ).forEach(wareInOrOutStock -> this.singleHandlerTransfer(wareInOrOutStock, businessType, transactionRuleParams, transactionNo));
        }
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandlerTransfer(T baseParam, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        VirtualInventoryStockDTO.TransferDTO param = (VirtualInventoryStockDTO.TransferDTO)baseParam;
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) {
            // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_99999.code, "交易类型不能为空"));
            // 转换成出入库参数
            VirtualInventoryStockDTO.StockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
            inOutStockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                //入库
                this.inStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "",  transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(inOutStockCoreDTO, businessType, param.getInventoryStatus(), "", transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
            // 判断当前仓是入库还是出库
            transactionRuleParams = transactionRuleParams.stream().filter(r->Objects.equals(r.getWarehouseOption(), param.getWarehouseOptionEnum())).collect(Collectors.toList());
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            transactionRuleParams = transactionRuleParams.stream()
                    .sorted(Comparator.comparing(inventoryStatus -> inventoryStatus.getInventoryStatus().getCode()))
                    .collect(Collectors.toList());
            for(VirtualTransRuleDTO.StockParamDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                VirtualInventoryStockDTO.StockCoreDTO inOutStockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
                inOutStockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                    // 入库
                    this.inStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum, transactionRule.getId(), transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                    // 出库
                    this.outStockCore(inOutStockCoreDTO, businessType, inventoryStatusEnum,  transactionRule.getId(), transactionNo);
                }
            }
        }
    }

    private  VirtualInventoryStockDTO.TransferDTO wrapInOutStockByTransfer(VirtualInventoryStockDTO.TransferStockDTO param, InventoryWarehouseOptionEnum warehouseOption, InventoryOperationModeEnum operationMode) {
        VirtualInventoryStockDTO.TransferDTO transferDTO = new VirtualInventoryStockDTO.TransferDTO();

        if(Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT)) {
            transferDTO.setVirtualWarehouseId(param.getVirtualCurWarehouseId());
        } else if (Objects.equals(warehouseOption, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET)) {
            transferDTO.setVirtualWarehouseId(param.getVirtualTargetWarehouseId());
        }
        transferDTO.setWarehouseId(param.getWarehouseId());
        transferDTO.setSourceType(param.getSourceType());
        transferDTO.setSourceId(param.getSourceId());
        transferDTO.setSourceDetailId(param.getSourceDetailId());
        transferDTO.setSourceCode(param.getSourceCode());
        transferDTO.setBillDate(param.getBillDate());
        transferDTO.setSkuId(param.getSkuId());
        transferDTO.setSkuNo(param.getSkuNo());
        transferDTO.setTradeTime(param.getTradeTime());
        transferDTO.setQty(param.getQty());
        transferDTO.setOperationMode(operationMode);
        transferDTO.setWarehouseOptionEnum(warehouseOption);
        return transferDTO;
    }

//------------------------------------------------------------------------------------出入库审核--------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(VirtualInventoryStockDTO.StockParamDTO dto,List<String> ignoreSkuIds,List<BomChildrenSkuDTO> bomChildrenSkuList) {
        ValidatorUtil.validateEntity(dto);
        //BOM拆分
        List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList = splitBom(dto.getParamList(), dto.getIsSplitBom(),bomChildrenSkuList);
        if (CollUtil.isEmpty(outInStockList)) {
            outInStockList = dto.getParamList();
        }
        this.approve(outInStockList, dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollUtil.isEmpty(dto.getRules()) ? true : false,ignoreSkuIds);
    }

    /**
     * 拆分BOM
     * @author will
     * @date 2024/7/21 16:02
     * @param paramList
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> splitBom (List<VirtualInventoryStockDTO.OutInStockDTO> paramList,Boolean isSplitBom,List<BomChildrenSkuDTO> bomChildrenSkuList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> resultList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(isSplitBom) && !isSplitBom) {
            return resultList;
        }
        //是否拆分bom
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(DictBasicEnum.VIRTUAL_SPLIT_BOM.getKey());
        if (CollUtil.isEmpty(list) || !Boolean.valueOf(list.get(0).getValue())) {
            return resultList;
        }
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            return resultList;
        }
        List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        if (CollUtil.isEmpty(bomList)) {
            return resultList;
        }
        for (VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : paramList) {
            //bom信息
            List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), outInStockDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                resultList.add(outInStockDTO);
                continue;
            }
            //优先取录入的bom版本，没有则取最新bom版本
            if (CharSequenceUtil.isNotBlank(outInStockDTO.getBomVersion())) {
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),outInStockDTO.getBomVersion())).collect(Collectors.toList());
            } else {
                String bomVersion = childList.stream().max(Comparator.comparing(BomChildrenSkuDTO::getBomVersion)).map(BomChildrenSkuDTO::getBomVersion).get();
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),bomVersion)).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(childList)) {
                throw new ServiceException(CharSequenceUtil.format("SKU【】未找到版本为【{}】的BOM",outInStockDTO.getSkuId(),outInStockDTO.getBomVersion()));
            }
            for (BomChildrenSkuDTO bomChildrenSkuDTO: childList) {
                VirtualInventoryStockDTO.OutInStockDTO newOutInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                BeanMapperUtils.copy(outInStockDTO,newOutInStockDTO);
                newOutInStockDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                newOutInStockDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                newOutInStockDTO.setQty(bomChildrenSkuDTO.getQuantity() * outInStockDTO.getQty());
                resultList.add(newOutInStockDTO);
            }
        }
        return resultList;
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void approve(List<T> paramList, List<VirtualTransRuleDTO.StockParamDTO> ruleList, VirtualInventoryBusinessTypeEnum businessType, Boolean byType,List<String> ignoreSkuIds) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSON.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<VirtualTransRuleDTO.StockParamDTO> stockParamList = ruleList;
        //如果走配置则取已配置的规则
        if(Objects.equals(byType,Boolean.TRUE)) {
            stockParamList = this.wrapTransactionRule(businessType);
        }
        // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);
        this.stockHandler(paramList, businessType, stockParamList, transactionNo,ignoreSkuIds);

        stopwatch.stop();
        log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandler(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo,List<String> ignoreSkuIds) {
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<VirtualInventoryStockDTO.StockBaseDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.StockBaseDTO::getSkuId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getVirtualWarehouseId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getWarehouseId);
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            VirtualInventoryStockDTO.OutInStockDTO param = (VirtualInventoryStockDTO.OutInStockDTO)baseParam;
            if(ignoreSkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }
    }

    /**
     * 获取忽略库存计算的sku
     * @return  返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 查询配置的交易规则
     * @author will
     * @date 2024/6/4 11:24
     * @param businessType  业务类型
     * @return List<StockParamDTO> 交易规则
     */
    protected List<VirtualTransRuleDTO.StockParamDTO> wrapTransactionRule(VirtualInventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgVirtualTransRulesEntity> list = cfgVirtualTransRulesService.findByDictBizType(businessType.getCode());
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<VirtualTransRuleDTO.StockParamDTO> ruleList = Lists.newArrayListWithExpectedSize(list.size());
        list.forEach(r->{
            VirtualTransRuleDTO.StockParamDTO stockParamDTO = new VirtualTransRuleDTO.StockParamDTO();
            stockParamDTO.setId(r.getId());
            stockParamDTO.setDictBizType(VirtualInventoryBusinessTypeEnum.getByCode(r.getDictBizType()));
            stockParamDTO.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(r.getWarehouseOption()));
            stockParamDTO.setInventoryStatus(InventoryStatusEnum.getByCode(r.getInventoryStatus()));
            stockParamDTO.setTransactionMode(InventoryModeEnum.getByCode(r.getTransactionMode()));
            ruleList.add(stockParamDTO);
        });
        return ruleList;
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandler( VirtualInventoryStockDTO.OutInStockDTO param, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        if(CollUtil.isEmpty(transactionRuleParams)) {
            throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        log.warn("从配置读取库存交易规则，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
        // 交易规则按照状态排序
        transactionRuleParams = transactionRuleParams.stream()
                .sorted(Comparator.comparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): ""))
                .collect(Collectors.toList());
        for(VirtualTransRuleDTO.StockParamDTO stockParamDTO : transactionRuleParams) {
            //验证枚举是否必填
            checkTransRule(stockParamDTO);

            // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
            VirtualInventoryStockDTO.StockCoreDTO stockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
            stockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);

            InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
            InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                // 入库
                this.inStockCore(stockCoreDTO, businessType, inventoryStatusEnum, stockParamDTO.getId(), transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(stockCoreDTO, businessType, inventoryStatusEnum,  stockParamDTO.getId(), transactionNo);
            }
        }
    }

    public  void inStockCore(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId, String transactionNo) {
        // 实物仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        VirtualInventoryEntity found = virtualInventoryService.findVirtualInventoryStock(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), inventoryStatusEnum.getCode());
        // 登记交易流水
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, found, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        transactionFlowDTO.setVirtualTransRuleId(transRuleId);
        virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.IN_STOCK);
        log.warn("入库成功，交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，入库数量：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName(), param.getQty());
    }

    public  void outStockCore (VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId,
                               String transactionNo) {
        //虚拟仓库信息
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(param.getVirtualWarehouseId());
        if (ObjectUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST);
        }
        //实体仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 待出库数量
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());

        VirtualInventoryEntity found = virtualInventoryService.findVirtualInventoryStock(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), inventoryStatusEnum.getCode());
        // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, found, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.OUT_STOCK);
        log.warn("出库成功，交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，入库数量：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName(), param.getQty());
    }
    /**
     * 验证枚举是否必填
     * @author will
     * @date 2024/6/4 17:37
     * @param stockParamDTO
     */
    private  void checkTransRule(VirtualTransRuleDTO.StockParamDTO stockParamDTO) {
        // 交易规则-选项错误
        InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = stockParamDTO.getWarehouseOption();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));

        // 交易规则-库存状态错误
        InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));

        // 交易规则-交易类型错误
        InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
    }

    /**
     * 格式化流水数据
     * @author will
     * @date 2024/6/4 18:31
     * @param param
     * @param virtualInventoryEntity
     * @param businessType
     * @param inventoryStatusEnum
     * @param qty
     * @param orgId
     * @return AddDTO
     *
     *
     */
    protected VirtualTransFlowDTO.AddDTO wrapTransactionFlow(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryEntity virtualInventoryEntity, VirtualInventoryBusinessTypeEnum businessType,
                                                             InventoryStatusEnum inventoryStatusEnum, Integer qty, String orgId) {
        VirtualTransFlowDTO.AddDTO virtualTransFlowDTO = new VirtualTransFlowDTO.AddDTO();
        // 复制对象性能慢，改为手工赋值
        virtualTransFlowDTO.setOrgId(orgId);
        virtualTransFlowDTO.setVirtualWarehouseId(param.getVirtualWarehouseId());
        virtualTransFlowDTO.setWarehouseId(param.getWarehouseId());
        virtualTransFlowDTO.setSkuId(param.getSkuId());
        virtualTransFlowDTO.setSkuNo(param.getSkuNo());
        virtualTransFlowDTO.setSourceId(param.getSourceId());
        virtualTransFlowDTO.setSourceCode(param.getSourceCode());
        virtualTransFlowDTO.setSourceDetailId(param.getSourceDetailId());
        virtualTransFlowDTO.setVirtualInventoryId(ObjectUtil.isEmpty(virtualInventoryEntity) ? "" : virtualInventoryEntity.getId());
        virtualTransFlowDTO.setDictInventoryStatus(inventoryStatusEnum.getCode());
        virtualTransFlowDTO.setDictBizType(businessType.getCode());
        virtualTransFlowDTO.setBillDate(param.getBillDate());
        virtualTransFlowDTO.setTradeTime(param.getTradeTime());
        virtualTransFlowDTO.setSourceType(param.getSourceType().getCode());
        virtualTransFlowDTO.setQty(qty);
        virtualTransFlowDTO.setOperationMode(Objects.nonNull(param.getOperationMode()) ? param.getOperationMode().getCode() : "");
        return virtualTransFlowDTO;
    }
}

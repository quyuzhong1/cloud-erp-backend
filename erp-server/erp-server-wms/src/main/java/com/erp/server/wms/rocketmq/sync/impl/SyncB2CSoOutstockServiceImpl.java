package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SyncKingdeeDTO;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockRuleDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncB2CSoOutstockServiceImpl
 * @Date 2023-06-27 10:54
 * @Created by yl
 */
@Service
@Slf4j
public class SyncB2CSoOutstockServiceImpl implements SyncB2CSoOutstockService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;
    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private VirtualWarehouseChannelService VirtualWarehouseChannelService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;


    private static final List<String> WDT_NULL_LOCATION = new ArrayList<>();

    static {
        WDT_NULL_LOCATION.add("直发暂存");
        WDT_NULL_LOCATION.add("发货暂存待放回");
        WDT_NULL_LOCATION.add("下架暂存");
        WDT_NULL_LOCATION.add("销退质检");
        WDT_NULL_LOCATION.add("补货暂存");
        WDT_NULL_LOCATION.add("其它未上架");
        WDT_NULL_LOCATION.add("销退暂存");
        WDT_NULL_LOCATION.add("盘亏暂存");
        WDT_NULL_LOCATION.add("发货暂存");
        WDT_NULL_LOCATION.add("采购未上架");
        WDT_NULL_LOCATION.add("空仓位");
    }

    /**
     * 同步金蝶的销售出库单
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-06-27 14:05
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "entity.fBillNo", leaseTime = 30, waitTime = 20)
    public void syncKingdeeSoOutstock(KingdeeDeliveryDetailEntity entity) {
        System.out.println("===============开始执行 单号：" + entity.getFBillNo());
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        if (CollectionUtils.isEmpty(kingdeeDetailList)) {
            return;
        }

        //不需要管的sku
        List<SkuVO> noInventorySkuList = plmTaskFeign.getNoInventorySku();
        //对应不需要的验证的sku no list
        List<String> noInventorySkuNoList = noInventorySkuList.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        //获取到销售出库单的信息
        SyncKingdeeDTO.B2CSoOutstockDTO info = handleWmsSoOutstock(entity, noInventorySkuNoList);
        SoOutstockEntity soOutstock = info.getSoOutstockEntity();
        List<SoOutstockDetailEntity> detailList = soOutstock.getDetailList();
        String flagId = info.getFlagId();
        if (CollectionUtils.isNotEmpty(detailList)) {
            //当是审核通过的时候
            if ("C".equals(entity.getFDocumentStatus())) {
                //当已存在 就删除以前的  并回滚库存
            	if(entity != null && entity.getIsNew()) {
            		soOutstockService.handleNewKingdeeToErp(soOutstock, detailList, flagId);
            	}else {
            		soOutstockService.handleKingdeeToErp(soOutstock, detailList, flagId);
            	}
                InventoryInOutStockRuleDTO inventoryInOutStockDTO = info.getInventoryInOutStockRuleDTO();
                if (CollectionUtils.isNotEmpty(inventoryInOutStockDTO.getParamList())) {
                    //无虚拟仓则不扣减虚拟库存
                    List<InOutStockDTO> virtualInOutList = inventoryInOutStockDTO.getParamList().stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(virtualInOutList)) {
                        //扣减虚拟库存
                        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
                        List<VirtualInventoryStockDTO.OutInStockDTO> stockParamDTOS = BeanMapperUtils.copyList(VirtualInventoryStockDTO.OutInStockDTO.class, virtualInOutList);
                        dto.setParamList(stockParamDTOS);
                        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.OUT_USABLE.getCode());
                        virtualInventoryTransCoreService.approve(dto);
                    }

                    inventoryTransCoreService.approveByRule(inventoryInOutStockDTO);
                }
                //扣减库存成功后 更新状态
                boolean update = soOutstockService.lambdaUpdate()
                        .set(SoOutstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                        .eq(SoOutstockEntity::getId, soOutstock.getId())
                        .update();
            }

        }
        System.out.println("===============结束执行 单号：" + entity.getFBillNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "entity.code")
    public void syncWdtSoOutStock(WdtSoOutStockDTO entity) {
        SoOutstockEntity soOutstockEntity = soOutstockService.getOne(Wrappers.<SoOutstockEntity>lambdaQuery()
                .eq(SoOutstockEntity::getThirdCode, entity.getThirdCode()));
        //单据已经存在
        if (ObjectUtil.isNotEmpty(soOutstockEntity)) {
            return;
        }
        //不需要管的sku
        List<SkuVO> noInventorySkuList = plmTaskFeign.getNoInventorySku();
        //对应不需要的验证的sku no list
        List<String> noInventorySkuNoList = noInventorySkuList.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        //获取到销售出库单的信息
        SoOutstockEntity soOutstock = BeanMapperUtils.map(SoOutstockEntity.class, entity);
        String id = IdWorker.getIdStr();
        soOutstock.setId(id);
        //查询旺店通对应系统店铺
        List<ThirdMappingEntity> shop = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                .eq(ThirdMappingEntity::getThirdId, entity.getShopId()));
        if (CollectionUtils.isEmpty(shop)) {
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_SHOP_MAPPING, entity.getShopId());
        }
        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
        //查询旺店通对应系统仓库
        List<ThirdMappingEntity> warehouseList = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.WAREHOUSE.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                .eq(ThirdMappingEntity::getThirdId, entity.getWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_WAREHOUSE_MAPPING, entity.getWarehouseName());
        }
        List<String> skuNoList = entity.getDetailList().stream().map(WdtSoOutStockDetailDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, warehouseList.get(0).getSysId());
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
        //组织信息
        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouse.getOrgId());
        //库存组织
        soOutstock.setWarehouseOrgId(warehouse.getOrgId());
        if (ObjectUtil.isNotEmpty(company)) {
            soOutstock.setWarehouseOrgName(company.getCompanyName());
        }

        //仓库
        soOutstock.setWarehouseId(warehouse.getId());
        soOutstock.setWarehouseName(warehouse.getName());

        //查询虚拟仓
        String virtualWarehouseId = handleVirtualWarehouse(Collections.singletonList(soOutstock.getWarehouseId()), shopInfo.getDictPlatform(),shopInfo.getId());

        //客户信息
        soOutstock.setCustomerId(shopInfo.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            soOutstock.setCustomerName(customerInfo.getName());
            soOutstock.setSellerId(customerInfo.getSellerId());
            soOutstock.setSellerName(customerInfo.getSellerName());
            SysDepartmentUserNumberDTO dept = sysUserFeign.getDeptByUserId(customerInfo.getSellerId());
            soOutstock.setSalesDeptId(Optional.ofNullable(dept).orElse(new SysDepartmentUserNumberDTO()).getDepartmentId());
        }
        //销售组织
        soOutstock.setSalesOrgId(shopInfo.getSalesOrgId());
        soOutstock.setSalesOrgName(shopInfo.getSalesOrgName());
        soOutstock.setPackingStatus(PackingTaskStatusEnum.WAIT.getCode());
        soOutstock.setInvalidStatus(false);
        soOutstock.setApproveStatus(ApproveStatusEnum.APPROVE);
        soOutstock.setApproveTime(soOutstock.getActualDeliveryDate());

        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        ArrayList<SoOutstockDetailEntity> detailList = new ArrayList<>();
        for (WdtSoOutStockDetailDTO detailDTO : entity.getDetailList()) {
            if (CollectionUtils.isEmpty(detailDTO.getPositionDetailsList())){
                //暂时使用空仓位
                SoOutstockDetailEntity detailEntity = BeanMapperUtils.map(SoOutstockDetailEntity.class, detailDTO);
                detailEntity.setId(IdWorker.getIdStr());
                String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(detailEntity.getSkuNo())).
                        findFirst().map(SkuVO::getSkuId).orElse("");
                if (CharSequenceUtil.isBlank(skuId)) {
                    throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND, detailEntity.getSkuNo());
                }
                String detailId = IdWorker.getIdStr();
                detailEntity.setId(detailId);
                detailEntity.setMainId(id);
                detailEntity.setSkuId(skuId);
                //仓库
                detailEntity.setWarehouseId(warehouse.getId());
                detailEntity.setWarehouseName(warehouse.getName());
                detailEntity.setWarehouseLocation("");
                detailEntity.setVirtualWarehouseId(virtualWarehouseId);
                detailEntity.setPlanQty(detailDTO.getPlanQty());
                detailEntity.setActualQty(detailDTO.getActualQty());
                detailList.add(detailEntity);
                //是否扣减库存 true 就要
                boolean isDeduction = !noInventorySkuNoList.contains(detailEntity.getSkuNo());
                if (Boolean.TRUE.equals(isDeduction)) {
                    //并且扣库存 才执行
                    buildInOutStock(id, detailEntity, soOutstock, virtualWarehouseId, inOutStockList);
                }
            } else {
                for (WdtSoOutStockDetailDTO.PositionDetailsList detail : detailDTO.getPositionDetailsList()) {
                    if (Boolean.FALSE.equals(warehouse.getIsEnableLocation())) {
                        detail.setPositionNo("");
                    }
                    SoOutstockDetailEntity detailEntity = BeanMapperUtils.map(SoOutstockDetailEntity.class, detailDTO);
                    detailEntity.setId(IdWorker.getIdStr());
                    String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(detailEntity.getSkuNo())).
                            findFirst().map(SkuVO::getSkuId).orElse("");
                    if (CharSequenceUtil.isBlank(skuId)) {
                        throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND, detailEntity.getSkuNo());
                    }
                    String detailId = IdWorker.getIdStr();
                    detailEntity.setId(detailId);
                    detailEntity.setMainId(id);
                    detailEntity.setSkuId(skuId);
                    //仓库
                    detailEntity.setWarehouseId(warehouse.getId());
                    detailEntity.setWarehouseName(warehouse.getName());
                    detailEntity.setWarehouseLocation(WDT_NULL_LOCATION.contains(detail.getPositionNo()) ? "" : detail.getPositionNo());
                    detailEntity.setVirtualWarehouseId(virtualWarehouseId);
                    detailEntity.setPlanQty(detail.getPositionGoodsCount());
                    detailEntity.setActualQty(detail.getPositionGoodsCount());
                    detailList.add(detailEntity);
                    //是否扣减库存 true 就要
                    boolean isDeduction = !noInventorySkuNoList.contains(detailEntity.getSkuNo());
                    if (Boolean.TRUE.equals(isDeduction)) {
                        buildInOutStock(id, detailEntity, soOutstock, virtualWarehouseId, inOutStockList);
                    }
                }
            }
        }

        //2024.09.11 jack sdc-erp销售出库单增加旺店通的物流渠道名称
        soOutstock.setLogisticsChannelName(entity.getLogisticsCompanyName());
        //订单标签
        soOutstock.setTradeLabel(entity.getTradeLabel());
        log.info("旺店通同步订单标签到erp："+ JSONUtil.toJsonStr(soOutstock));
        //保存销售出库单
        soOutstockService.save(soOutstock);
        //保存销售出库单详情
        soOutstockDetailService.saveBatch(detailList);
        //根据销售出库单创建物流单和自发货费用
        soOutstockService.saveLogisticsBill(soOutstock);
        //扣减库存
        InventoryInOutStockRuleDTO inventoryInOutStockDTO = getInventoryInOutStockRuleDTO(inOutStockList);
        if (CollectionUtils.isNotEmpty(inventoryInOutStockDTO.getParamList())) {
            //无虚拟仓则不扣减虚拟库存
            List<InOutStockDTO> virtualInOutList = inOutStockList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(virtualInOutList)) {
                //扣减虚拟仓库存
                VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
                List<VirtualInventoryStockDTO.OutInStockDTO> stockParamDTOS = BeanMapperUtils.copyList(VirtualInventoryStockDTO.OutInStockDTO.class, virtualInOutList);
                dto.setParamList(stockParamDTOS);
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.OUT_USABLE.getCode());
                virtualInventoryTransCoreService.approve(dto);
            }
            inventoryTransCoreService.approveByRule(inventoryInOutStockDTO);
        }
        //推送金蝶
        sendPushTask(soOutstock);
    }

    private static void buildInOutStock(String id, SoOutstockDetailEntity detailEntity, SoOutstockEntity soOutstock, String virtualWarehouseId, List<InOutStockDTO> inOutStockList) {
        InOutStockDTO inOutStock = new InOutStockDTO();
        inOutStock.setSourceId(id);
        inOutStock.setSourceDetailId(detailEntity.getId());
        inOutStock.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
        inOutStock.setBillDate(soOutstock.getBillDate());
        inOutStock.setQty(detailEntity.getActualQty());
        inOutStock.setSkuId(detailEntity.getSkuId());
        inOutStock.setSkuNo(detailEntity.getSkuNo());
        inOutStock.setSourceCode(soOutstock.getCode());
        inOutStock.setWarehouseId(soOutstock.getWarehouseId());
        inOutStock.setWarehouseLocation(detailEntity.getWarehouseLocation());
        inOutStock.setVirtualWarehouseId(virtualWarehouseId);
        inOutStockList.add(inOutStock);
    }

    private void sendPushTask(SoOutstockEntity obj) {
        //审核通过发送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSoOutstockService.syncWdtDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }

    /**
     *
     * 构建库存相关数据
     */
    private static InventoryInOutStockRuleDTO getInventoryInOutStockRuleDTO(List<InOutStockDTO> inOutStockList) {
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = new InventoryInOutStockRuleDTO();
        inventoryInOutStockRuleDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        inventoryInOutStockRuleDTO.setParamList(inOutStockList);
        List<TransactionRuleDTO> ruleList = new ArrayList<>(1);
        TransactionRuleDTO transactionRule = new TransactionRuleDTO();
        transactionRule.setInventoryStatus(InventoryStatusEnum.USABLE);
        transactionRule.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
        transactionRule.setTransactionMode(InventoryModeEnum.OUT_STOCK);
        ruleList.add(transactionRule);
        inventoryInOutStockRuleDTO.setRules(ruleList);
        return inventoryInOutStockRuleDTO;
    }


    /**
     * 处理wms 销售出库单需要的数据
     *
     * @param noInventorySkuNoList 不需要的校验的skuno
     * @return com.erp.model.wms.entity.SoOutstockEntity
     * @author yl
     * @date 2023-07-01 10:56
     */
    private SyncKingdeeDTO.B2CSoOutstockDTO handleWmsSoOutstock(KingdeeDeliveryDetailEntity entity, List<String> noInventorySkuNoList) {
        String customerNumber = CharSequenceUtil.isBlank(entity.getFCustomerNumber())?"1":entity.getFCustomerNumber();
        String customerName = CharSequenceUtil.isBlank(entity.getFCustomerName())?"1":entity.getFCustomerName();
        List<CustomerInfoEntity> customerInfoEntityList = customerFeign.getCustomerByCodeAndName(customerNumber, customerName);
    	if(CollUtil.isEmpty(customerInfoEntityList)) {
    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询不到客户信息" , customerNumber , customerName));
    	}else if(customerInfoEntityList.size() > 1){
    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询到多条客户信息" , customerNumber , customerName));
    	}

        SyncKingdeeDTO.B2CSoOutstockDTO result = new SyncKingdeeDTO.B2CSoOutstockDTO();
        List<KingdeeDeliveryDetailItemEntity> kingdeeDetailList = entity.getKingdeeOutStockItemEntityList();
        //金蝶的仓库code
        List<String> kingdeeWarehouseCodeList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFStockNumber).distinct().collect(Collectors.toList());
        //仓库的
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(kingdeeWarehouseCodeList);
        //组织信息
        List<String> warehouseOrgIdList = warehouseList.stream().map(req -> req.getOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(warehouseOrgIdList);

        //虚拟仓库
        List<String> warehouseIdList = warehouseList.stream().map(WarehouseEntity::getId).collect(Collectors.toList());
        String virtualWarehouseId = handleVirtualWarehouse(warehouseIdList, customerInfoEntityList.get(0).getPlatformType(),"");
        /**
         * sku no list
         */
        List<String> skuNoList = kingdeeDetailList.stream().map(KingdeeDeliveryDetailItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        ApproveStatusEnum statusEnum = ApproveStatusEnum.WAIT_SUBMIT;
        String sourceType = SourceTypeEnum.SAL_OUTSTOCK.getCode();
        //销售出库单
        InventorySourceTypeEnum sourceTypeEnum = InventorySourceTypeEnum.SO_OUTSTOCK;
        String code = entity.getFBillNo();
        String flagId = soOutstockService.getByCode(code);
        result.setFlagId(flagId);
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        if (CollUtil.isNotEmpty(customerInfoEntityList)) {
            soOutstock.setCustomerId(customerInfoEntityList.get(0).getId());
        }
        soOutstock.setCustomerName(customerName);
        //单据编号
        soOutstock.setCode(code);
        //运输单号
        soOutstock.setTrackNo(entity.getFCarriageNO());
        //第三方单据编号
        soOutstock.setThirdCode(entity.getFEThirdBillNo());
        //仓管员
        String warehouseKeeperName = entity.getFStockerName();
        soOutstock.setWarehouseKeeperName(warehouseKeeperName);
        soOutstock.setApproveStatus(statusEnum);
        soOutstock.setSourceType(sourceType);
        soOutstock.setOrderType(OrderTypeEnum.B2C.getCode());
        LocalDate billDate = null;
        String billDateStr = entity.getFDate();
        if (CharSequenceUtil.isNotBlank(billDateStr)) {
            LocalDateTime billDateTime = LocalDateUtil.strToLocalDateTime(billDateStr);
            billDate = billDateTime.toLocalDate();
        }
        if (Objects.isNull(billDate)) {
            billDate = LocalDate.now();
        }
        // 出库日期
        soOutstock.setBillDate(billDate);
        String id = IdWorker.getIdStr();
        soOutstock.setId(id);
        List<SoOutstockDetailEntity> addDetailList = new ArrayList<>(kingdeeDetailList.size());
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (KingdeeDeliveryDetailItemEntity detail : kingdeeDetailList) {
            String skuNo = detail.getFMaterialNumber();
            String fStockNumber = detail.getFStockNumber();
            //是否扣减库存 true 就要
            Boolean isDeduction = !noInventorySkuNoList.contains(skuNo);
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).
                    findFirst().map(SkuVO::getSkuId).orElse("");
            if (CharSequenceUtil.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND, skuNo);
            }
            SoOutstockDetailEntity detailEntity = new SoOutstockDetailEntity();
            String detailId = IdWorker.getIdStr();
            detailEntity.setId(detailId);
            detailEntity.setMainId(id);
            detailEntity.setSkuNo(skuNo);
            detailEntity.setSkuId(skuId);
            //实发
            String realQty = detail.getFRealQty();
            Integer actualQty = Integer.parseInt(realQty.split("\\.")[0]);
            detailEntity.setActualQty(actualQty);
            detailEntity.setPlanQty(actualQty);
            String note = detail.getFNote();
            if (CharSequenceUtil.isNotBlank(note)) {
                detailEntity.setPlatformCode(note.trim());
            }
            String warehouseId = "";
            String warehouseName = "";
            String warehouseOrgId = "";
            String warehouseOrgName = "";
            //表示要扣除库存
            if (isDeduction) {
                WarehouseEntity warehouse = warehouseList.stream().filter(w -> w.getKingdeeWarehouseCode().
                        equals(fStockNumber)).findFirst().orElse(null);
                //并且扣库存 才执行
                if (Objects.isNull(warehouse)) {
                    throw new ServiceException(ApiError.ERROR_92054, fStockNumber);
                } else {
                    warehouseId = warehouse.getId();
                    warehouseName = warehouse.getName();
                    warehouseOrgId = warehouse.getOrgId();

                    BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(req -> req.getId().equals(warehouse.getOrgId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(codeDTO)) {
                        warehouseOrgName = codeDTO.getName();
                    }

                    //设置主表仓库和组织
                    soOutstock.setWarehouseId(warehouseId);
                    soOutstock.setWarehouseName(warehouseName);
                    soOutstock.setWarehouseOrgId(warehouseOrgId);
                    soOutstock.setWarehouseOrgName(warehouseOrgName);
                }

                InOutStockDTO inOutStock = new InOutStockDTO();
                inOutStock.setSourceId(id);
                inOutStock.setSourceDetailId(detailId);
                inOutStock.setSourceType(sourceTypeEnum);
                inOutStock.setBillDate(billDate);
                inOutStock.setQty(actualQty);
                inOutStock.setSkuId(skuId);
                inOutStock.setSkuNo(skuNo);
                inOutStock.setSourceCode(code);
                inOutStock.setWarehouseId(warehouseId);
                inOutStock.setVirtualWarehouseId(virtualWarehouseId);
                inOutStockList.add(inOutStock);
            }
            detailEntity.setWarehouseId(warehouseId);
            detailEntity.setWarehouseName(warehouseName);
            detailEntity.setVirtualWarehouseId(virtualWarehouseId);
            addDetailList.add(detailEntity);
        }

        soOutstock.setDetailList(addDetailList);
        result.setSoOutstockEntity(soOutstock);

        //扣库存
        InventoryInOutStockRuleDTO inventoryInOutStockRuleDTO = getInventoryInOutStockRuleDTO(inOutStockList);
        result.setInventoryInOutStockRuleDTO(inventoryInOutStockRuleDTO);
        return result;
    }

    /**
     * 获取虚拟仓
     * @author will
     * @date 2024/6/13 12:07
     * @param warehouseIdList
     */
    private String handleVirtualWarehouse (List<String> warehouseIdList,String  dictPlatform,String shopId) {
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(dictPlatform);
        platformDTO.setRelationId(shopId);
        platformDTO.setWarehouseIdList(warehouseIdList);
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = VirtualWarehouseChannelService.getVirtualWarehouse(platformDTO);
        if (CollectionUtils.isEmpty(virtualWarehouseList)) {
           return CharSequenceUtil.EMPTY;
        }
        return virtualWarehouseList.get(0).getVirtualWarehouseId();
    }
}






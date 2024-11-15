package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.CommonConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import com.erp.server.wms.service.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SyncSoReturnServiceImpl implements SyncSoReturnService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;
    
    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SyncKingdeeSoReturnService syncKingdeeSoReturnService;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private OtherInstockService otherInstockService;

    @Lazy
    @Resource
    private SyncSoReturnService service;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "kingdeeReturnOrderEntity.fBillNo", leaseTime = 30, waitTime = 20)
    public void syncKingdeeReturnOrderToSoReturn(KingdeeReturnOrderEntity kingdeeReturnOrderEntity) {
        //跳过优质胜和小隼科技的单
        if (CharSequenceUtil.isEmpty(kingdeeReturnOrderEntity.getFSaleOrgId()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(kingdeeReturnOrderEntity.getFSaleOrgId()) || ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(kingdeeReturnOrderEntity.getFSaleOrgId())) {
            return;
        }
/*        //如果不是B2C类型的单跳过
        if (!kingdeeReturnOrderEntity.getFBillTypeID().equals("559351ce1d0252")) {
            return;
        }*/
        if (CharSequenceUtil.isNotBlank(kingdeeReturnOrderEntity.getFULZDataSources())) {
            //不同步MWS同步到金蝶的数据
            if (CommonConstants.SYSTEM.equals(kingdeeReturnOrderEntity.getFULZDataSources().trim())) {
                return;
            }
        }

        String retcustNumber = kingdeeReturnOrderEntity.getFRetcustNumber();
        String retcustName = kingdeeReturnOrderEntity.getFRetcustName();
        List<CustomerInfoEntity> customerInfoEntityList = customerFeign.getCustomerByCodeAndName(retcustNumber, retcustName);
//    	if(CollUtil.isEmpty(customerInfoEntityList)) {
//    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询不到客户信息" , retcustNumber , retcustName));
//    	}else if(customerInfoEntityList.size() > 1){
//    		throw new ServiceException(String.format("通过客户编码：%s，客户名称：%s查询到多条客户信息" , retcustNumber , retcustName));
//    	}
        
        List<KingdeeReturnOrderItemEntity> itemEntityList = kingdeeReturnOrderEntity.getItemEntityList();
        List<String> stockNumberList = itemEntityList.stream().map(KingdeeReturnOrderItemEntity::getFStockNumber).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(stockNumberList);
        List<String> kingdeeSkuNoList = itemEntityList.stream().map(KingdeeReturnOrderItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listBySkuNoList(kingdeeSkuNoList);
        //金蝶sku和plm对应不上跳过
        if (CollectionUtils.isEmpty(skuNoList)) {
            throw new ServiceException(ApiError.ERROR_92057, StringUtil.join(skuNoList, ","));
        }

        //获取退货单明细
        List<KingdeeReturnOrderItemEntity> orderItemEntityList = kingdeeReturnOrderEntity.getItemEntityList();

        SoReturnInstockEntity instockEntity = new SoReturnInstockEntity();
        instockEntity.setCode(kingdeeReturnOrderEntity.getFBillNo());
        instockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        instockEntity.setType(BillTypeEnum.B2C.getCode());
        instockEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
        instockEntity.setSalesOrgName(kingdeeReturnOrderEntity.getFSaleOrgName());
        SysDepartmentDTO userDeptByCode = sysUserFeign.getUserDeptByCode(kingdeeReturnOrderEntity.getFSaledeptNumber());
        if (ObjectUtil.isNotEmpty(userDeptByCode)) {
            instockEntity.setSalesDeptId(userDeptByCode.getId());
        }
        instockEntity.setSalesDeptName(kingdeeReturnOrderEntity.getFSaledeptName());
        instockEntity.setSellerName(kingdeeReturnOrderEntity.getFSalesManName());
        instockEntity.setBillDate(LocalDate.parse(kingdeeReturnOrderEntity.getFDate().split("T")[0]));
        if (CollectionUtils.isNotEmpty(orderItemEntityList)) {
            KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity = orderItemEntityList.get(MathUtil.ZERO);
            instockEntity.setSourceCode(kingdeeReturnOrderItemEntity.getFOrderNo());
            instockEntity.setSourceId(kingdeeReturnOrderItemEntity.getFSOEntryId());
            instockEntity.setSoReturnCode(kingdeeReturnOrderItemEntity.getFOrderNo());
            instockEntity.setSoReturnId(kingdeeReturnOrderItemEntity.getFSOEntryId());
        }
        instockEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        if(CollUtil.isNotEmpty(customerInfoEntityList)) {
        	instockEntity.setCustomerId(customerInfoEntityList.get(0).getId());
        }
		instockEntity.setCustomerName(retcustName);
        instockEntity.setId(IdWorker.getIdStr());
        instockEntity.setThirdCode(kingdeeReturnOrderEntity.getFEThirdBillNo());
        List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
        for (KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity : orderItemEntityList) {
            SoReturnInstockDetailEntity instockDetailEntity = new SoReturnInstockDetailEntity();
            //获取仓库信息
            WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getKingdeeWarehouseCode().equals(kingdeeReturnOrderItemEntity.getFStockNumber())).findFirst().orElse(null);
            //如果仓库不存在抛出异常
            if (ObjectUtil.isEmpty(warehouseEntity)) {
                throw new ServiceException(ApiError.ERROR_92056, kingdeeReturnOrderItemEntity.getFStockNumber());
            }

            instockDetailEntity.setWarehouseId(warehouseEntity.getId());
            instockDetailEntity.setWarehouseName(warehouseEntity.getName());
            SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuNo().equals(kingdeeReturnOrderItemEntity.getFMaterialNumber())).findFirst().orElse(null);
            instockDetailEntity.setMainId(instockEntity.getId());
            instockDetailEntity.setSkuNo(kingdeeReturnOrderItemEntity.getFMaterialNumber());
            instockDetailEntity.setSkuId(skuVO.getSkuId());
            instockDetailEntity.setRealQty(Double.valueOf(kingdeeReturnOrderItemEntity.getFRealQty()).intValue());
            instockDetailEntity.setWarehouseLocation(kingdeeReturnOrderItemEntity.getFStockLocId());
            detailEntityList.add(instockDetailEntity);
        }

        List<SoReturnInstockEntity> soReturnInstockEntities = soReturnInstockService.listByCode(Arrays.asList(kingdeeReturnOrderEntity.getFBillNo()));
        List<String> ids = soReturnInstockEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnInstockEntity::getId).collect(Collectors.toList());
        if (kingdeeReturnOrderEntity.getFDocumentStatus().equals("C")) {
            soReturnInstockService.saveKingdeeSoReturn(instockEntity, detailEntityList, ids);
            //更新库存
            inventoryTransCore(Arrays.asList(instockEntity));

            //更新状态
           soReturnInstockService.lambdaUpdate()
                   .set(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                   .eq(SoReturnInstockEntity::getId, instockEntity.getId())
                   .update();
        } else {
            if (CollectionUtils.isNotEmpty(ids)) {
                //回滚库存
                InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_RETURN_INSTOCK, ids);
                inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
                soReturnInstockService.deleteByIds(ids);
                soReturnInstockDetailService.delete(ids);
            }
        }
    }

    @Override
    @DataIdempotent(keyIdName = "dto.thirdCode")
    public void syncWdtReturnOrderToSoReturn(WdtReturnOrderDTO dto) {
        //入库时间根据查询其他入库单是否已存在，存在取修改时间，否则取审核时间
        OtherInstockEntity dbOtherInstockEntity = otherInstockService.getByThirdCode(dto.getThirdCode(), InventoryDirectionEnum.ORDINARY);
        LocalDateTime approveTime = Objects.nonNull(dbOtherInstockEntity)?dto.getModified():dto.getApproveTime();
        if(Objects.isNull(approveTime)){
            throw new ServiceException(CharSequenceUtil.format("{}旺店通退货入库单审核时间为空",dto.getThirdCode()));
        }
        SoReturnInstockEntity entity = soReturnInstockService.getOne(Wrappers.<SoReturnInstockEntity>lambdaQuery()
                .eq(SoReturnInstockEntity::getThirdCode, dto.getThirdCode()));
        //单据已经存在
        if (ObjectUtil.isNotEmpty(entity)){
            if(Objects.nonNull(entity.getApproveTime()) && approveTime.isAfter(entity.getApproveTime())){
                //反审核重新生成
                SoReturnInstockEntity inStockEntity = this.buildWdtReturnStock(dto,approveTime);
                service.disApproveAndGenerate(entity,dbOtherInstockEntity,inStockEntity);
            }
        }else{
            SoReturnInstockEntity inStockEntity = this.buildWdtReturnStock(dto,approveTime);
            service.saveWdtReturnData(inStockEntity,dbOtherInstockEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWdtReturnData(SoReturnInstockEntity inStockEntity,OtherInstockEntity dbOtherInstockEntity) {
        //保存退货入库单和明细
        soReturnInstockService.save(inStockEntity);
        soReturnInstockDetailService.saveBatch(inStockEntity.getDetailEntityList());
        //退库存
        inventoryTransCore(Collections.singletonList(inStockEntity));
        //推送金蝶
        sendPushTask(Collections.singletonList(inStockEntity), SyncOperateEnum.OPERATE_APPROVE.getCode());
        //如果其他入库单已存在，生成一个相反的入库单
        if(Objects.nonNull(dbOtherInstockEntity)){
            OtherInstockEntity dbReturnOtherInstockEntity = otherInstockService.getByThirdCode(dbOtherInstockEntity.getThirdCode(), InventoryDirectionEnum.RETURN_GOODS);
            if(Objects.isNull(dbReturnOtherInstockEntity)){
                dbOtherInstockEntity.setBillDate(inStockEntity.getBillDate());
                dbOtherInstockEntity.setApproveTime(inStockEntity.getApproveTime());
                otherInstockService.generateOpposite(dbOtherInstockEntity,inStockEntity.getCode() );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disApproveAndGenerate(SoReturnInstockEntity entity, OtherInstockEntity dbOtherInstockEntity, SoReturnInstockEntity newEntity) {
        soReturnInstockService.disApprove(entity,Boolean.TRUE);
        soReturnInstockService.delete(Arrays.asList(entity.getId()));
        service.saveWdtReturnData(newEntity,dbOtherInstockEntity);
    }

    private SoReturnInstockEntity buildWdtReturnStock(WdtReturnOrderDTO dto, LocalDateTime approveTime) {
        SoReturnInstockEntity inStockEntity = BeanMapperUtils.map(SoReturnInstockEntity.class, dto);
        List<SoReturnInstockDetailEntity> detailList = BeanMapperUtils.copyList(SoReturnInstockDetailEntity.class, dto.getDetailList());
        //查询旺店通对应系统店铺
        List<ThirdMappingEntity> shop = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                .eq(ThirdMappingEntity::getThirdId, dto.getShopId()));
        if (CollectionUtils.isEmpty(shop)) {
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_SHOP_MAPPING, dto.getShopName());
        }
        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
        if(Objects.isNull(shopInfo)){
            throw new ServiceException("erp店铺信息为空");
        }
        //查询旺店通对应系统仓库
        List<ThirdMappingEntity> warehouseList = FeignQuery.list(FeignQuery.create(ThirdMappingEntity.class)
                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.WAREHOUSE.getCode())
                .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                .eq(ThirdMappingEntity::getThirdId, dto.getWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_WAREHOUSE_MAPPING, dto.getWarehouseName());
        }
        WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, warehouseList.get(0).getSysId());
        //组织信息
        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouse.getOrgId());

        List<String> skuList = detailList.stream().map(SoReturnInstockDetailEntity::getSkuNo).collect(Collectors.toList());
//        List<SkuVO> skuNoList = plmTaskFeign.listBySkuNoList(skuList);
        List<ProductDetailEntity> skuEntityList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuList).list();

        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTH);
        inStockEntity.setCode(code);
        inStockEntity.setApproveTime(approveTime);
        inStockEntity.setBillDate(approveTime.toLocalDate());
        if (Boolean.FALSE.equals(warehouse.getIsEnableLocation())) {
            //暂时使用空仓位
            detailList.forEach(v -> v.setWarehouseLocation(""));
        }
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
        inStockEntity.setSalesOrgId(shopInfo.getSalesOrgId());
        inStockEntity.setSalesOrgName(shopInfo.getSalesOrgName());
        inStockEntity.setInventoryOrgId(company.getId());
        if (ObjectUtil.isNotEmpty(company)) {
            inStockEntity.setInventoryOrgName(company.getCompanyName());
        }
        inStockEntity.setCustomerId(shopInfo.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            inStockEntity.setCustomerName(customerInfo.getName());
            inStockEntity.setSellerId(customerInfo.getSellerId());
            inStockEntity.setSellerName(customerInfo.getSellerName());
        }
        //金蝶sku和plm对应不上跳过
        if (CollectionUtils.isEmpty(skuEntityList)) {
            throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_SKU, StringUtil.join(skuList, ","));
        }
        inStockEntity.setId(IdWorker.getIdStr());
        for (SoReturnInstockDetailEntity detailEntity : detailList) {
            detailEntity.setReturnTypeDict(ReturnTypeEnum.DEDUCTION.getCode());
            //获取仓库信息
            detailEntity.setWarehouseId(warehouse.getId());
            detailEntity.setWarehouseName(warehouse.getName());
            Optional<ProductDetailEntity> skuVoOptional = skuEntityList.stream().filter(req -> req.getSkuNo().equals(detailEntity.getSkuNo())).findFirst();
            if (!skuVoOptional.isPresent()) {
                throw new ServiceException(ApiError.ERROR_WDT_NOT_FOUND_SKU, detailEntity.getSkuNo());
            }
            ProductDetailEntity skuVO =skuVoOptional.get();
            detailEntity.setMainId(inStockEntity.getId());
            detailEntity.setSkuId(skuVO.getId());
            if(Objects.nonNull(detailEntity.getAmount())){
                detailEntity.setPrice(detailEntity.getAmount().divide(new BigDecimal(detailEntity.getRealQty()),4, RoundingMode.HALF_UP));
            }
            if(WmsConstant.WDT_NULL_LOCATION.contains(detailEntity.getWarehouseLocation())){
                detailEntity.setWarehouseLocation("");
            }
        }
        inStockEntity.setDetailEntityList(detailList);
        return inStockEntity;
    }

    private void sendPushTask(List<SoReturnInstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoReturnService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    /**
     * 更新库存
     *
     * @param entityList
     * @return void
     * @Author Luo_WG
     * @Date 2023/5/24 11:25
     **/
    private void inventoryTransCore(List<SoReturnInstockEntity> entityList) {
        for (SoReturnInstockEntity entity : entityList) {
            List<InOutStockDTO> inOutStockList = new ArrayList<>();
            List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailByMainId(entity.getId());
            for (SoReturnInstockDetailEntity detailEntity : returnInstockDetailEntities) {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_RETURN_INSTOCK);
                inOutStockDTO.setSourceId(entity.getId());
                inOutStockDTO.setSourceCode(entity.getCode());
                inOutStockDTO.setSourceDetailId(detailEntity.getId());
                inOutStockDTO.setBillDate(entity.getBillDate());
                inOutStockDTO.setSkuId(detailEntity.getSkuId());
                inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
                inOutStockDTO.setQty(detailEntity.getRealQty());
                inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
                // TODO 金蝶目前没有填仓位
//                inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
                inOutStockList.add(inOutStockDTO);
            }
            //添加冻结库存
            InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
            inventoryInOutStockDTO.setParamList(inOutStockList);
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_RETURN_INSTOCK.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
    }
}

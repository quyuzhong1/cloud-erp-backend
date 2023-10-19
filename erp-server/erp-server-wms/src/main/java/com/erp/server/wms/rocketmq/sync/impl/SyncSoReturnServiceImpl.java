package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.constant.CommonConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.WarehouseService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeReturnOrderToSoReturn(KingdeeReturnOrderEntity kingdeeReturnOrderEntity) {
        //跳过优质胜和小隼科技的单
        if (StrUtil.isEmpty(kingdeeReturnOrderEntity.getFSaleOrgId()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(kingdeeReturnOrderEntity.getFSaleOrgId()) || ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(kingdeeReturnOrderEntity.getFSaleOrgId())) {
            return;
        }
/*        //如果不是B2C类型的单跳过
        if (!kingdeeReturnOrderEntity.getFBillTypeID().equals("559351ce1d0252")) {
            return;
        }*/
        if (StringUtils.isNotBlank(kingdeeReturnOrderEntity.getFULZDataSources())) {
            //不同步MWS同步到金蝶的数据
            if (CommonConstants.SYSTEM.equals(kingdeeReturnOrderEntity.getFULZDataSources().trim())) {
                return;
            }
        }

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
        instockEntity.setWarehouseId("");
        instockEntity.setWarehouseName("");
        if (CollectionUtils.isNotEmpty(orderItemEntityList)) {
            KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity = orderItemEntityList.get(MathUtil.ZERO);
            instockEntity.setSourceCode(kingdeeReturnOrderItemEntity.getFOrderNo());
            instockEntity.setSourceId(kingdeeReturnOrderItemEntity.getFSOEntryId());
            instockEntity.setSoReturnCode(kingdeeReturnOrderItemEntity.getFOrderNo());
            instockEntity.setSoReturnId(kingdeeReturnOrderItemEntity.getFSOEntryId());
        }
        instockEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        instockEntity.setCustomerName(kingdeeReturnOrderEntity.getFRetcustName());
        instockEntity.setId(IdWorker.getIdStr());

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
                inOutStockDTO.setBillDate(LocalDate.now());
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

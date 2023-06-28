package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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

    @Override
    public void syncKingdeeReturnOrderToSoReturn(KingdeeReturnOrderEntity kingdeeReturnOrderEntity) {
        List<KingdeeReturnOrderItemEntity> itemEntityList = kingdeeReturnOrderEntity.getItemEntityList();
        List<String> stockNumberList = itemEntityList.stream().map(KingdeeReturnOrderItemEntity::getFStockNumber).collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(stockNumberList);
        List<String> kingdeeSkuNoList = itemEntityList.stream().map(KingdeeReturnOrderItemEntity::getFMaterialNumber).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listBySkuNoList(kingdeeSkuNoList);
        //如果金蝶退货单明细有不一样的仓库，这里分开成多单存到OMS
        Map<String, List<KingdeeReturnOrderItemEntity>> stockNumberMap = itemEntityList.stream().collect(Collectors.groupingBy(KingdeeReturnOrderItemEntity::getFStockNumber));
        for (Map.Entry<String, List<KingdeeReturnOrderItemEntity>> stringListEntry : stockNumberMap.entrySet()) {
            //获取退货单明细
            List<KingdeeReturnOrderItemEntity> orderItemEntityList = stringListEntry.getValue();
            //获取仓库信息
            WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getKingdeeWarehouseCode().equals(stringListEntry.getKey())).findFirst().orElse(null);
            //如果仓库不存在跳过
            if (ObjectUtil.isNotEmpty(warehouseEntity)) {
                continue;
            }
            //如果不是B2C类型的单跳过
            if (!kingdeeReturnOrderEntity.getFBillTypeID().equals("559351ce1d0252")) {
                continue;
            }
            //如果退货单号不是XSTHD开头的跳过，避免接收到OMS同步到金蝶的单
            if (!kingdeeReturnOrderEntity.getFBillNo().contains("XSTHD")) {
                continue;
            }
            SoReturnInstockEntity instockEntity = new SoReturnInstockEntity();
            instockEntity.setCode(kingdeeReturnOrderEntity.getFBillNo());
            instockEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            instockEntity.setType(BillTypeEnum.B2C.getCode());
            instockEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
            instockEntity.setSalesOrgName(kingdeeReturnOrderEntity.getFSaleOrgName());
            SysDepartmentDTO userDeptByCode = sysUserFeign.getUserDeptByCode(kingdeeReturnOrderEntity.getFSaledeptNumber());
            if (ObjectUtil.isNotEmpty(userDeptByCode)) {
                instockEntity.setSalesDeptId(userDeptByCode.getId());
            }
            instockEntity.setSalesDeptName(kingdeeReturnOrderEntity.getFSaledeptName());
            instockEntity.setSellerName(kingdeeReturnOrderEntity.getFSalesManName());
            instockEntity.setBillDate(LocalDate.parse(kingdeeReturnOrderEntity.getFDate()));

            instockEntity.setWarehouseId(warehouseEntity.getId());
            instockEntity.setWarehouseName(warehouseEntity.getName());
            if (CollectionUtils.isNotEmpty(orderItemEntityList)) {
                KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity = orderItemEntityList.get(MathUtil.ZERO);
                instockEntity.setSourceCode(kingdeeReturnOrderItemEntity.getFOrderNo());
                instockEntity.setSourceId(kingdeeReturnOrderItemEntity.getFSOEntryId());
            }
            instockEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            instockEntity.setCustomerName(kingdeeReturnOrderEntity.getFRetcustName());
            instockEntity.setId(IdWorker.getIdStr());
            List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
            for (KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity : orderItemEntityList) {
                SoReturnInstockDetailEntity instockDetailEntity = new SoReturnInstockDetailEntity();
                SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuNo().equals(kingdeeReturnOrderItemEntity.getFMaterialNumber())).findFirst().orElse(null);
                //金蝶sku和plm对应不上跳过
                if (ObjectUtil.isEmpty(skuVO)) {
                    continue;
                }
                instockDetailEntity.setMainId(instockEntity.getId());
                instockDetailEntity.setSkuNo(kingdeeReturnOrderItemEntity.getFMaterialNumber());
                instockDetailEntity.setSkuId(skuVO.getSkuName());
                instockDetailEntity.setRealQty(Integer.valueOf(kingdeeReturnOrderItemEntity.getFRealQty()));
                detailEntityList.add(instockDetailEntity);
            }
            soReturnInstockService.save(instockEntity);
            soReturnInstockDetailService.saveBatch(detailEntityList);
        }
    }
}

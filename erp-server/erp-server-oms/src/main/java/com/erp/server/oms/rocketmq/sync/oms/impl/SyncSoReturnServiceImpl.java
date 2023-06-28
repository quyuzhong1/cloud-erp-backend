package com.erp.server.oms.rocketmq.sync.oms.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ObjectUtils;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.rocketmq.sync.oms.SyncSoReturnService;
import org.ehcache.core.util.CollectionUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncSoReturnServiceImpl implements SyncSoReturnService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void syncKingdeeReturnOrderToSoReturn(List<KingdeeReturnOrderEntity> list) {
        List<SoReturnEntity> returnEntityList = new ArrayList<>();
        for (KingdeeReturnOrderEntity kingdeeReturnOrderEntity : list) {
            List<KingdeeReturnOrderItemEntity> itemEntityList = kingdeeReturnOrderEntity.getItemEntityList();
            List<String> stockNumberList = itemEntityList.stream().map(KingdeeReturnOrderItemEntity::getFStockNumber).collect(Collectors.toList());
            List<WarehouseEntity> warehouseEntities = wmsTaskFeign.listByKingdeeCodeList(stockNumberList);

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
                SoReturnEntity soReturnEntity = new SoReturnEntity();
                soReturnEntity.setCode(kingdeeReturnOrderEntity.getFBillNo());
                soReturnEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
                soReturnEntity.setType(BillTypeEnum.B2C.getCode());
                soReturnEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
                soReturnEntity.setSalesOrgName(kingdeeReturnOrderEntity.getFSaleOrgName());
                SysDepartmentDTO userDeptByCode = sysUserFeign.getUserDeptByCode(kingdeeReturnOrderEntity.getFSaledeptNumber());
                if (ObjectUtil.isNotEmpty(userDeptByCode)) {
                    soReturnEntity.setSalesDeptId(userDeptByCode.getId());
                }
                soReturnEntity.setSalesDeptName(kingdeeReturnOrderEntity.getFSaledeptName());
                soReturnEntity.setSellerName(kingdeeReturnOrderEntity.getFSalesManName());
                soReturnEntity.setBillDate(LocalDate.parse(kingdeeReturnOrderEntity.getFDate()));

                soReturnEntity.setWarehouseId(warehouseEntity.getId());
                soReturnEntity.setWarehouseName(warehouseEntity.getName());
                if (CollectionUtils.isNotEmpty(orderItemEntityList)) {
                    KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity = orderItemEntityList.get(MathUtil.ZERO);
                    soReturnEntity.setSourceCode(kingdeeReturnOrderItemEntity.getFOrderNo());
                    soReturnEntity.setSourceId(kingdeeReturnOrderItemEntity.getFSOEntryId());
                }
                soReturnEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            /*    kingdeeReturnOrderEntity
                soReturnEntity.setCustomerId()*/
                returnEntityList.add(soReturnEntity);
            }
        }
    }
}

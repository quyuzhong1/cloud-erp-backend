package com.erp.server.wms.util;

import cn.hutool.core.collection.CollUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.enums.SubcontractOrderTypeEnum;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 返修委外订单公共判断与用量解析。
 */
public final class SubcontractRepairHelper {

    private SubcontractRepairHelper() {
    }

    public static boolean isRepairSubcontract(SubcontractOrderEntity subcontractOrderEntity) {
        return Objects.nonNull(subcontractOrderEntity)
                && Objects.equals(SubcontractOrderTypeEnum.REPAIR_SUBCONTRACT.getCode(), subcontractOrderEntity.getType());
    }

    public static boolean needBomLookup(SubcontractOrderEntity subcontractOrderEntity) {
        return !isRepairSubcontract(subcontractOrderEntity);
    }

    public static boolean needBomLookup(Collection<SubcontractOrderEntity> subcontractOrderList) {
        if (CollUtil.isEmpty(subcontractOrderList)) {
            return false;
        }
        return subcontractOrderList.stream().anyMatch(SubcontractRepairHelper::needBomLookup);
    }

    /**
     * 解析父子 SKU 用量：返修委外固定为 1，普通委外从 BOM 获取。
     */
    public static Integer resolveChildSkuQuantity(SubcontractOrderEntity subcontractOrderEntity,
                                                  SubcontractOrderDetailEntity parentDetailEntity,
                                                  SubcontractOrderDetailEntity childDetailEntity,
                                                  List<BomChildrenSkuDTO> bomChildrenSkuList) {
        if (isRepairSubcontract(subcontractOrderEntity)) {
            return MathUtil.ONE;
        }
        return findBomChild(bomChildrenSkuList, parentDetailEntity.getSkuId(), childDetailEntity.getSkuId())
                .map(BomChildrenSkuDTO::getQuantity)
                .orElseThrow(() -> new ServiceException(ApiError.BOM_NOT_FOUND));
    }

    /**
     * 解析父子 SKU 用量（匹配 BOM 版本）：返修委外固定为 1，普通委外从 BOM 获取，未匹配返回 0。
     */
    public static Integer resolveChildSkuQuantityWithBomVersion(SubcontractOrderEntity subcontractOrderEntity,
                                                              SubcontractOrderDetailEntity parentDetailEntity,
                                                              SubcontractOrderDetailEntity childDetailEntity,
                                                              List<BomChildrenSkuDTO> bomList) {
        return resolveChildSkuQuantityWithBomVersion(subcontractOrderEntity, parentDetailEntity.getSkuId(),
                childDetailEntity, bomList);
    }

    /**
     * 解析父子 SKU 用量（匹配 BOM 版本，父级 SKU 由入参指定）：返修委外固定为 1，普通委外从 BOM 获取，未匹配返回 0。
     */
    public static Integer resolveChildSkuQuantityWithBomVersion(SubcontractOrderEntity subcontractOrderEntity,
                                                              String parentSkuId,
                                                              SubcontractOrderDetailEntity childDetailEntity,
                                                              List<BomChildrenSkuDTO> bomList) {
        if (isRepairSubcontract(subcontractOrderEntity)) {
            return MathUtil.ONE;
        }
        return findBomChild(bomList, childDetailEntity.getBomVersion(), parentSkuId, childDetailEntity.getSkuId())
                .map(BomChildrenSkuDTO::getQuantity)
                .orElse(MathUtil.ZERO);
    }

    /**
     * 解析父子 SKU 用量（匹配 BOM 版本，父级 SKU 由入参指定）：返修委外固定为 1，普通委外从 BOM 获取，未匹配抛异常。
     */
    public static Integer resolveChildSkuQuantityWithBomVersionOrThrow(SubcontractOrderEntity subcontractOrderEntity,
                                                                       String parentSkuId,
                                                                       SubcontractOrderDetailEntity childDetailEntity,
                                                                       List<BomChildrenSkuDTO> bomList) {
        if (isRepairSubcontract(subcontractOrderEntity)) {
            return MathUtil.ONE;
        }
        return findBomChild(bomList, childDetailEntity.getBomVersion(), parentSkuId, childDetailEntity.getSkuId())
                .map(BomChildrenSkuDTO::getQuantity)
                .orElseThrow(() -> new ServiceException(ApiError.BOM_CHILD_NOT_FOUND));
    }

    public static Optional<BomChildrenSkuDTO> findBomChild(List<BomChildrenSkuDTO> bomList,
                                                             String parentSkuId,
                                                             String childSkuId) {
        if (CollUtil.isEmpty(bomList)) {
            return Optional.empty();
        }
        return bomList.stream()
                .filter(obj -> obj.getParentSkuId().equals(parentSkuId) && obj.getSkuId().equals(childSkuId))
                .findFirst();
    }

    public static Optional<BomChildrenSkuDTO> findBomChild(List<BomChildrenSkuDTO> bomList,
                                                             String bomVersion,
                                                             String parentSkuId,
                                                             String childSkuId) {
        if (CollUtil.isEmpty(bomList)) {
            return Optional.empty();
        }
        return bomList.stream()
                .filter(obj -> Objects.equals(bomVersion, obj.getBomVersion())
                        && obj.getSkuId().equals(childSkuId)
                        && obj.getParentSkuId().equals(parentSkuId))
                .findFirst();
    }
}

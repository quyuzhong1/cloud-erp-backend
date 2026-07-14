package com.erp.server.wms.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.enums.WarehouseOperationTypeEnum;

import java.util.List;
import java.util.Objects;

/**
 * 通邮 B2B：从仓库操作指令解析是否换标/是否混装。
 */
public final class TongYouB2bOperationResolver {

    private TongYouB2bOperationResolver() {
    }

    public static Boolean resolveRelabel(List<B2bThirdDeliveryDTO.WarehouseOperationTypeDTO> operationList) {
        return resolveYesNo(findOperationDesc(operationList, WarehouseOperationTypeEnum.IS_RELABEL.getCode()));
    }

    public static Boolean resolveMixedPacking(List<B2bThirdDeliveryDTO.WarehouseOperationTypeDTO> operationList) {
        return resolveYesNo(findOperationDesc(operationList, WarehouseOperationTypeEnum.IS_MIXED_PACKING.getCode()));
    }

    public static boolean isRelabel(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return Boolean.TRUE.equals(resolveYesNo(findPushOperationDesc(createOutboundReq, WarehouseOperationTypeEnum.IS_RELABEL.getCode())));
    }

    public static boolean isMixedPacking(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return Boolean.TRUE.equals(resolveYesNo(findPushOperationDesc(createOutboundReq, WarehouseOperationTypeEnum.IS_MIXED_PACKING.getCode())));
    }

    public static void validateRequiredOperations(List<B2bThirdDeliveryDTO.WarehouseOperationTypeDTO> operationList) {
        if (resolveRelabel(operationList) == null) {
            throw new ServiceException("操作指令【是否换标】不能为空，取值仅支持「是」或「否」");
        }
        if (resolveMixedPacking(operationList) == null) {
            throw new ServiceException("操作指令【是否混装】不能为空，取值仅支持「是」或「否」");
        }
    }

    private static String findOperationDesc(List<B2bThirdDeliveryDTO.WarehouseOperationTypeDTO> operationList, String typeCode) {
        if (CollUtil.isEmpty(operationList) || CharSequenceUtil.isBlank(typeCode)) {
            return null;
        }
        return operationList.stream()
                .filter(item -> Objects.equals(typeCode, item.getWarehouseOperationType()))
                .map(B2bThirdDeliveryDTO.WarehouseOperationTypeDTO::getOperationDesc)
                .findFirst()
                .orElse(null);
    }

    private static String findPushOperationDesc(ThirdWarehouseCreateFbaOutboundReq createOutboundReq, String typeCode) {
        if (Objects.isNull(createOutboundReq) || CollUtil.isEmpty(createOutboundReq.getWarehouseOperationTypeDTOList())) {
            return null;
        }
        return createOutboundReq.getWarehouseOperationTypeDTOList().stream()
                .filter(item -> Objects.equals(typeCode, item.getWarehouseOperationType()))
                .map(ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO::getOperationDesc)
                .findFirst()
                .orElse(null);
    }

    private static Boolean resolveYesNo(String operationDesc) {
        if (CharSequenceUtil.isBlank(operationDesc)) {
            return null;
        }
        String value = operationDesc.trim();
        if ("是".equals(value)) {
            return Boolean.TRUE;
        }
        if ("否".equals(value)) {
            return Boolean.FALSE;
        }
        return null;
    }
}

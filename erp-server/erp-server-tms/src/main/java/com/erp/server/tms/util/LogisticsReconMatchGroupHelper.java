package com.erp.server.tms.util;

import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.dto.excel.LogisticsReconMatchImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.server.tms.constant.LogisticsCostImportTargetFieldConstant;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对账匹配识别号分组键（与导入模板唯一键、明细字段映射保持一致）。
 *
 * @author Will
 * @date 2026/6/12
 */
public final class LogisticsReconMatchGroupHelper {

    /**
     * 工具类私有构造，禁止实例化。
     */
    private LogisticsReconMatchGroupHelper() {
    }

    /**
     * 按模板唯一键拼接明细分组键（与导入 Excel 识别号分组规则一致）。
     *
     * @author Will
     * @date 2026/6/12
     * @param detail        对账明细
     * @param uniqueKeyList 模板唯一识别字段配置
     * @return 识别号分组键，参数无效时返回空串
     */
    public static String buildDetailGroupKey(LogisticsReconDetailEntity detail,
                                             List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        if (detail == null || uniqueKeyList == null) {
            return "";
        }
        return uniqueKeyList.stream()
                .map(uniqueKey -> detailIdentifyValue(detail, uniqueKey.getTargetField()))
                .collect(Collectors.joining("_"));
    }

    /**
     * 按模板唯一键拼接导入匹配 Excel 行分组键。
     *
     * @author Will
     * @date 2026/6/12
     * @param row           导入匹配 Excel 行
     * @param uniqueKeyList 模板唯一识别字段配置
     * @return 识别号分组键，参数无效时返回空串
     */
    public static String buildImportMatchExcelGroupKey(LogisticsReconMatchImportExcelDTO row,
                                                       List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        if (row == null || uniqueKeyList == null) {
            return "";
        }
        return uniqueKeyList.stream()
                .map(uniqueKey -> excelIdentifyValue(row, uniqueKey.getTargetField()))
                .collect(Collectors.joining("_"));
    }

    /**
     * 固定模板识别列是否全部为空（空行跳过，不参与计数）。
     *
     * @author Will
     * @date 2026/6/12
     * @param row 导入匹配 Excel 行
     * @return 固定识别列均为空时返回 true
     */
    public static boolean isTemplateRowBlank(LogisticsReconMatchImportExcelDTO row) {
        if (row == null) {
            return true;
        }
        return StrUtil.isAllBlank(row.getSoCode(), row.getPlatformOrderNo(), row.getTransportNo(), row.getTrackNo(),
                row.getSoDeliveryCode());
    }

    /**
     * 导入模板配置的唯一识别字段是否均已填写（用于识别对账明细）。
     *
     * @author Will
     * @date 2026/6/12
     * @param row           导入匹配 Excel 行
     * @param uniqueKeyList 模板唯一识别字段配置
     * @return 配置的唯一识别字段均有值时返回 true
     */
    public static boolean isConfiguredIdentifyComplete(LogisticsReconMatchImportExcelDTO row,
                                                       List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        if (row == null || uniqueKeyList == null || uniqueKeyList.isEmpty()) {
            return false;
        }
        return uniqueKeyList.stream()
                .allMatch(uniqueKey -> StrUtil.isNotBlank(excelIdentifyValue(row, uniqueKey.getTargetField())));
    }

    /**
     * 取明细对应识别字段值（targetField 对齐 LogisticsBillVo / 导入模板）。
     *
     * @author Will
     * @date 2026/6/12
     * @param detail       对账明细
     * @param targetField  模板 targetField
     * @return 识别字段值（已 trim），无法映射时返回空串
     */
    public static String detailIdentifyValue(LogisticsReconDetailEntity detail, String targetField) {
        if (detail == null || StrUtil.isBlank(targetField)) {
            return "";
        }
        switch (targetField) {
            case LogisticsCostImportTargetFieldConstant.SOURCE_CODE:
            case LogisticsCostImportTargetFieldConstant.SO_CODE:
                return StrUtil.trimToEmpty(detail.getSoCode());
            case LogisticsCostImportTargetFieldConstant.PLATFORM_CODE:
            case LogisticsCostImportTargetFieldConstant.PLATFORM_ORDER_NO:
                return StrUtil.trimToEmpty(detail.getPlatformOrderNo());
            case LogisticsCostImportTargetFieldConstant.TRACK_NO:
                return StrUtil.trimToEmpty(detail.getTrackNo());
            case LogisticsCostImportTargetFieldConstant.TRANSPORT_NO:
                return StrUtil.trimToEmpty(detail.getTransportNo());
            case LogisticsCostImportTargetFieldConstant.SO_DELIVERY_CODE:
                return StrUtil.trimToEmpty(detail.getSoDeliveryCode());
            default:
                return "";
        }
    }

    /**
     * 取导入匹配 Excel 行对应识别字段值。
     *
     * @author Will
     * @date 2026/6/12
     * @param row         导入匹配 Excel 行
     * @param targetField 模板 targetField
     * @return 识别字段值（已 trim），无法映射时返回空串
     */
    private static String excelIdentifyValue(LogisticsReconMatchImportExcelDTO row, String targetField) {
        if (row == null || StrUtil.isBlank(targetField)) {
            return "";
        }
        switch (targetField) {
            case LogisticsCostImportTargetFieldConstant.SOURCE_CODE:
            case LogisticsCostImportTargetFieldConstant.SO_CODE:
                return StrUtil.trimToEmpty(row.getSoCode());
            case LogisticsCostImportTargetFieldConstant.PLATFORM_CODE:
            case LogisticsCostImportTargetFieldConstant.PLATFORM_ORDER_NO:
                return StrUtil.trimToEmpty(row.getPlatformOrderNo());
            case LogisticsCostImportTargetFieldConstant.TRACK_NO:
                return StrUtil.trimToEmpty(row.getTrackNo());
            case LogisticsCostImportTargetFieldConstant.TRANSPORT_NO:
                return StrUtil.trimToEmpty(row.getTransportNo());
            case LogisticsCostImportTargetFieldConstant.SO_DELIVERY_CODE:
                return StrUtil.trimToEmpty(row.getSoDeliveryCode());
            default:
                return "";
        }
    }
}

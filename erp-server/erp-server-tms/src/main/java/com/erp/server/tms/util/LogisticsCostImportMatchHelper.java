package com.erp.server.tms.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportCfgTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportIdentifyTypeEnum;

/**
 * 费用项导入 / 对账匹配共用的识别维度规则（平台收窄、唯一命中等）。
 *
 * @author Will
 * @since 2026-06-12
 */
public final class LogisticsCostImportMatchHelper {

    private LogisticsCostImportMatchHelper() {
    }

    /**
     * 是否按配置平台（物流商/销售平台）收窄物流单匹配范围。
     * identify_no 时不收窄；identify_no_supplier 或识别维度为空时按 cfg_type + dict_platform 过滤。
     *
     * @param costImportEntity 费用项导入配置
     * @return true 需按平台过滤；false 仅按识别单号匹配
     */
    public static boolean shouldFilterByCostImportPlatform(CfgLogisticsCostImportEntity costImportEntity) {
        if (costImportEntity == null) {
            return true;
        }
        String identifyType = costImportEntity.getIdentifyType();
        if (CharSequenceUtil.isBlank(identifyType)) {
            return true;
        }
        return CharSequenceUtil.equals(CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO_SUPPLIER.getCode(), identifyType);
    }

    /**
     * 判断物流单是否落在费用项导入配置的平台范围内（物流商模板 / 销售平台模板）。
     *
     * @param costImportEntity 费用项导入配置
     * @param logisticsBillVo  待匹配的物流单
     * @return true 允许参与匹配；false 配置与物流单平台不一致
     */
    public static boolean matchesCostImportConfig(CfgLogisticsCostImportEntity costImportEntity,
                                                  LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        if (costImportEntity == null || logisticsBillVo == null) {
            return false;
        }
        if (!shouldFilterByCostImportPlatform(costImportEntity)) {
            return true;
        }
        String cfgType = costImportEntity.getCfgType();
        String dictPlatform = costImportEntity.getDictPlatform();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(), cfgType)) {
            return CharSequenceUtil.equals(dictPlatform, logisticsBillVo.getLogisticsSupplierId());
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.PLATFORM.getCode(), cfgType)) {
            return CharSequenceUtil.equals(dictPlatform, logisticsBillVo.getSalesPlatform());
        }
        return true;
    }
}

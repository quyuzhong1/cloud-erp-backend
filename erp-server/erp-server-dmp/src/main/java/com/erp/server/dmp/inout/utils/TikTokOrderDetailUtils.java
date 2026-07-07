package com.erp.server.dmp.inout.utils;

import com.erp.model.dmp.entity.DmpSoDetailEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * TikTok 订单明细 sourceDetailId / platformLineNumber 构建规则（DMP 输出侧）。
 * <p>
 * OMS 侧以 {@code platformSkuNo + platformLineNumber} 匹配，语义须与本类一致；匹配/迁移逻辑见
 * {@code SoB2cDetailServiceImpl} TikTok 私有方法。修改任一侧规则须同步另一侧。
 * <p>
 * 构建优先级：① 非空 third_detail_id 排序逗号拼接 → ② {@code emptyThirdDetailFallback}
 * （Fully 传 platformDetailId 分组键）→ ③ platformSku + platformPackageId。
 * OMS 迁移窗口另允许逗号分隔 lineNumber ID 交集匹配（xxxnull→packageId 历史数据）。
 */
public final class TikTokOrderDetailUtils {

    private TikTokOrderDetailUtils() {
    }

    public static String buildSourceDetailId(DmpSoDetailEntity soDetailEntity, List<String> thirdDetailIdList) {
        return buildSourceDetailId(soDetailEntity, thirdDetailIdList, null);
    }

    /**
     * @param emptyThirdDetailFallback third_detail_id 为空时的回退键（Fully 订单传分组键 platformDetailId）
     */
    public static String buildSourceDetailId(DmpSoDetailEntity soDetailEntity, List<String> thirdDetailIdList,
                                             String emptyThirdDetailFallback) {
        if (!CollectionUtils.isEmpty(thirdDetailIdList)) {
            return String.join(",", thirdDetailIdList);
        }
        if (StringUtils.isNotBlank(emptyThirdDetailFallback)) {
            return emptyThirdDetailFallback;
        }
        return defaultString(soDetailEntity.getPlatformSku()) + defaultString(soDetailEntity.getPlatformPackageId());
    }

    public static String defaultString(String value) {
        return value == null ? "" : value;
    }
}

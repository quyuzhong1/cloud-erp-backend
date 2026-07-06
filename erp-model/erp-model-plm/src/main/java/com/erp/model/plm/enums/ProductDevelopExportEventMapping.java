package com.erp.model.plm.enums;

import com.common.business.enums.FileTaskEventEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 产品开发导出：PLM {@code exportDataList} 与下载中心 {@link FileTaskEventEnum} 的单点映射。
 * <p>
 * 供 {@code erp-server-plm} 建下载任务与 {@code erp-server-file} Handler 路由共用，避免 event 字符串在双端各写一份导致分叉。
 * 领域 code 与组合校验见 {@link ProductDevelopExportTypeEnum}；本类仅承载跨服务集成契约，勿将映射复制到 plm/file Service。
 */
public final class ProductDevelopExportEventMapping {

    private static final Integer EXPORT_PRODUCT = ProductDevelopExportTypeEnum.PRODUCT.getCode();
    private static final Integer EXPORT_TASK = ProductDevelopExportTypeEnum.TASK.getCode();

    private ProductDevelopExportEventMapping() {
    }

    /**
     * {@code exportDataList}（须已通过 {@link ProductDevelopExportTypeEnum#validateCombinationMessage}）→ file 侧 event code。
     *
     * @return event code；入参非法时返回 {@code null}
     */
    public static String resolveEventCode(List<Integer> exportDataList) {
        if (exportDataList == null || exportDataList.isEmpty()) {
            return null;
        }
        if (ProductDevelopExportTypeEnum.validateCombinationMessage(exportDataList) != null) {
            return null;
        }
        if (exportDataList.size() == 1) {
            Integer flag = exportDataList.get(0);
            if (EXPORT_PRODUCT.equals(flag)) {
                return FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_PRODUCT.getCode();
            }
            if (EXPORT_TASK.equals(flag)) {
                return FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_TASK.getCode();
            }
            return null;
        }
        return FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_BOTH.getCode();
    }

    /**
     * file 侧 event code → {@code exportDataList}；未知 event 返回 {@code null}。
     * {@link FileTaskEventEnum#EXPORT_PLM_PRODUCT} 为历史遗留 code，等价于仅产品。
     */
    public static List<Integer> exportDataListFromEventCode(String eventCode) {
        FileTaskEventEnum event = FileTaskEventEnum.getByCode(eventCode);
        if (event == null) {
            return null;
        }
        switch (event) {
            case EXPORT_PLM_PRODUCT_DEV_PRODUCT:
            case EXPORT_PLM_PRODUCT:
                return Collections.singletonList(EXPORT_PRODUCT);
            case EXPORT_PLM_PRODUCT_DEV_TASK:
                return Collections.singletonList(EXPORT_TASK);
            case EXPORT_PLM_PRODUCT_DEV_BOTH:
                return Arrays.asList(EXPORT_PRODUCT, EXPORT_TASK);
            default:
                return null;
        }
    }
}

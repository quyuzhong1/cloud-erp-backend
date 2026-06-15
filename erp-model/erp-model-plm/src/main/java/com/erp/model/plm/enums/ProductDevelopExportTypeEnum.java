package com.erp.model.plm.enums;

import com.common.business.enums.FileTaskEventEnum;
import com.common.core.constant.EnumMessage;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 产品开发导出数据类型
 *
 * @author jack
 */
@Getter
public enum ProductDevelopExportTypeEnum implements EnumMessage {

    PRODUCT(0, "产品列表"),
    TASK(1, "任务列表");

    private static final Integer EXPORT_PRODUCT = PRODUCT.getCode();
    private static final Integer EXPORT_TASK = TASK.getCode();

    private final Integer code;

    private final String name;

    ProductDevelopExportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isValid(Integer code) {
        return getEnum(code) != null;
    }

    public static ProductDevelopExportTypeEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductDevelopExportTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 校验导出类型组合并返回细分错误信息（纯判断，不抛异常、不依赖异常体系）：
     * 合法返回 {@code null}；空/null、非法 code、重复元素、非法组合各返回与旧 validate 一致的细分文案。
     * 由 server 层调用方据此抛出 {@code ServiceException}，既保留错误粒度又不让 model 依赖异常体系。
     *
     * @return 错误信息；合法时为 {@code null}
     */
    public static String validateCombinationMessage(List<Integer> exportDataList) {
        if (exportDataList == null || exportDataList.isEmpty()) {
            return "导出数据类型不能为空";
        }
        Set<Integer> distinctTypes = new LinkedHashSet<>();
        for (Integer flag : exportDataList) {
            if (!isValid(flag)) {
                return "导出数据类型不合法：" + flag;
            }
            if (!distinctTypes.add(flag)) {
                return "导出数据类型存在重复：" + flag;
            }
        }
        boolean validCombination = distinctTypes.size() == 1
                || (distinctTypes.size() == 2
                    && distinctTypes.contains(EXPORT_PRODUCT)
                    && distinctTypes.contains(EXPORT_TASK));
        if (!validCombination) {
            return "导出数据类型不合法：" + exportDataList;
        }
        return null;
    }

    /**
     * {@code exportDataList}（须已通过 {@link #validateCombinationMessage}）→ file 侧 event code。
     * 与 {@link FileTaskEventEnum} 共用同一 code 源，避免 PLM 创建任务与 file Handler 路由字符串分叉。
     *
     * @return event code；入参非法时返回 {@code null}
     */
    public static String resolveEventCode(List<Integer> exportDataList) {
        if (exportDataList == null || exportDataList.isEmpty()) {
            return null;
        }
        if (validateCombinationMessage(exportDataList) != null) {
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

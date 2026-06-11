package com.erp.server.file.business.plm;

import com.common.business.enums.FileTaskEventEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.enums.ProductDevelopExportTypeEnum;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.multisheet.AbstractStreamingMultiSheetHandler;
import com.erp.server.file.core.multisheet.MultiSheetTemplateWriter;
import com.erp.server.file.entity.FileTask;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_BOTH;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_PRODUCT;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_DEV_TASK;

/**
 * 产品开发导出
 *
 * @author jack
 * @date 2025-02-14
 */
@Component
public class ExportPlmProductInfoHandler extends AbstractStreamingMultiSheetHandler<ProductSearchDTO.ExportDTO> {

    private static final Integer EXPORT_PRODUCT = ProductDevelopExportTypeEnum.PRODUCT.getCode();
    private static final Integer EXPORT_TASK = ProductDevelopExportTypeEnum.TASK.getCode();

    @Resource
    private ExportPlmFeign exportPlmFeign;

    /**
     * 解析导出参数：当上游仅切换 event 而未在 metaInfo 中携带 exportDataList 时，
     * 根据 {@link FileTask#getEvent()} 兜底推导导出类型，并在此处完成唯一一次校验，
     * 后续 {@link #getExcelPath} / {@link #sheets} 直接复用已规范化的 exportDataList。
     */
    @Override
    protected ProductSearchDTO.ExportDTO resolveExportParams(FileTask fileTask) {
        ProductSearchDTO.ExportDTO params = super.resolveExportParams(fileTask);
        if (params == null) {
            throw new ServiceException("导出数据类型不能为空");
        }
        if (CollectionUtils.isEmpty(params.getExportDataList())) {
            params.setExportDataList(deriveExportDataListByEvent(fileTask.getEvent()));
        }
        params.setExportDataList(validateExportDataList(params.getExportDataList()));
        return params;
    }

    @Override
    protected String getExcelPath(ProductSearchDTO.ExportDTO params) {
        List<Integer> exportDataList = params.getExportDataList();
        if (exportDataList.size() == 2) {
            return "excel/plm/productDevelop.xlsx";
        }
        Integer flag = exportDataList.get(0);
        if (EXPORT_PRODUCT.equals(flag)) {
            return "excel/plm/product.xlsx";
        }
        if (EXPORT_TASK.equals(flag)) {
            return "excel/plm/productTask.xlsx";
        }
        throw new ServiceException("导出数据类型不合法：" + flag);
    }

    @Override
    protected List<MultiSheetTemplateWriter.IndependentSheet<ProductSearchDTO.ExportDTO>> sheets(ProductSearchDTO.ExportDTO params) {
        List<Integer> exportDataList = params.getExportDataList();
        List<MultiSheetTemplateWriter.IndependentSheet<ProductSearchDTO.ExportDTO>> sheets = new ArrayList<>(exportDataList.size());
        if (exportDataList.contains(EXPORT_PRODUCT)) {
            sheets.add(new MultiSheetTemplateWriter.IndependentSheet<>(params, dto -> exportPlmFeign.exportProductShow(dto)));
        }
        if (exportDataList.contains(EXPORT_TASK)) {
            sheets.add(new MultiSheetTemplateWriter.IndependentSheet<>(params, dto -> exportPlmFeign.exportProjectTask(dto)));
        }
        return sheets;
    }

    /**
     * 根据 event 兜底推导导出类型，保证仅切换 event 的新事件也能正确导出。
     */
    private List<Integer> deriveExportDataListByEvent(String event) {
        if (EXPORT_PLM_PRODUCT_DEV_PRODUCT.name().equals(event)) {
            return new ArrayList<>(Arrays.asList(EXPORT_PRODUCT));
        }
        if (EXPORT_PLM_PRODUCT_DEV_TASK.name().equals(event)) {
            return new ArrayList<>(Arrays.asList(EXPORT_TASK));
        }
        if (EXPORT_PLM_PRODUCT_DEV_BOTH.name().equals(event)) {
            return new ArrayList<>(Arrays.asList(EXPORT_PRODUCT, EXPORT_TASK));
        }
        throw new ServiceException("导出数据类型不能为空");
    }

    private List<Integer> validateExportDataList(List<Integer> exportDataList) {
        if (CollectionUtils.isEmpty(exportDataList)) {
            throw new ServiceException("导出数据类型不能为空");
        }
        // 逐元素校验枚举合法性并去重，拒绝 [0, 99] 等非法值与 [0, 0] 等重复值
        Set<Integer> distinctTypes = new LinkedHashSet<>();
        for (Integer flag : exportDataList) {
            if (!ProductDevelopExportTypeEnum.isValid(flag)) {
                throw new ServiceException("导出数据类型不合法：" + flag);
            }
            if (!distinctTypes.add(flag)) {
                throw new ServiceException("导出数据类型存在重复：" + flag);
            }
        }
        // 仅允许：单一合法类型，或 PRODUCT + TASK 的组合
        boolean validCombination = distinctTypes.size() == 1
                || (distinctTypes.size() == 2
                    && distinctTypes.contains(EXPORT_PRODUCT)
                    && distinctTypes.contains(EXPORT_TASK));
        if (!validCombination) {
            throw new ServiceException("导出数据类型不合法：" + exportDataList);
        }
        return exportDataList;
    }

    @Override
    public boolean isMatch(String event) {
        return EXPORT_PLM_PRODUCT.name().equals(event)
                || EXPORT_PLM_PRODUCT_DEV_PRODUCT.name().equals(event)
                || EXPORT_PLM_PRODUCT_DEV_TASK.name().equals(event)
                || EXPORT_PLM_PRODUCT_DEV_BOTH.name().equals(event);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT;
    }
}

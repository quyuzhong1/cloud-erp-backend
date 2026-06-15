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
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT;

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
        String validateMessage = ProductDevelopExportTypeEnum.validateCombinationMessage(params.getExportDataList());
        if (validateMessage != null) {
            throw new ServiceException(validateMessage);
        }
        return params;
    }

    @Override
    protected String getExcelPath(ProductSearchDTO.ExportDTO params) {
        List<Integer> exportDataList = params.getExportDataList();
        // 双 Sheet 以元素语义（contains）判定，非旧版 size==2；非法组合（如 [0,0]）已在 resolveExportParams / PLM 建任务侧由 validateCombinationMessage 拒绝（审查勿误报为行为回退）。
        boolean exportProduct = exportDataList.contains(EXPORT_PRODUCT);
        boolean exportTask = exportDataList.contains(EXPORT_TASK);
        if (exportProduct && exportTask) {
            return "excel/plm/productDevelop.xlsx";
        }
        if (exportProduct) {
            return "excel/plm/product.xlsx";
        }
        if (exportTask) {
            return "excel/plm/productTask.xlsx";
        }
        throw new ServiceException("导出数据类型不合法：" + exportDataList);
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
        List<Integer> exportDataList = ProductDevelopExportTypeEnum.exportDataListFromEventCode(event);
        if (exportDataList == null) {
            throw new ServiceException("导出数据类型不能为空");
        }
        return new ArrayList<>(exportDataList);
    }

    @Override
    public boolean isMatch(String event) {
        return ProductDevelopExportTypeEnum.exportDataListFromEventCode(event) != null;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT;
    }
}

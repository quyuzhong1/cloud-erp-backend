package com.erp.server.file.business.plm;

import com.common.business.enums.FileTaskEventEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractStreamingMultiSheetHandler;
import com.erp.server.file.core.MultiSheetTemplateWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    private static final Integer EXPORT_PRODUCT = 0;
    private static final Integer EXPORT_TASK = 1;

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected String getExcelPath(ProductSearchDTO.ExportDTO params) {
        List<Integer> exportDataList = resolveExportDataList(params);
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
        List<Integer> exportDataList = resolveExportDataList(params);
        List<MultiSheetTemplateWriter.IndependentSheet<ProductSearchDTO.ExportDTO>> sheets = new ArrayList<>(exportDataList.size());
        if (exportDataList.contains(EXPORT_PRODUCT)) {
            sheets.add(new MultiSheetTemplateWriter.IndependentSheet<>(params, dto -> exportPlmFeign.exportProductShow(dto)));
        }
        if (exportDataList.contains(EXPORT_TASK)) {
            sheets.add(new MultiSheetTemplateWriter.IndependentSheet<>(params, dto -> exportPlmFeign.exportProjectTask(dto)));
        }
        return sheets;
    }

    private List<Integer> resolveExportDataList(ProductSearchDTO.ExportDTO params) {
        if (params == null || CollectionUtils.isEmpty(params.getExportDataList())) {
            throw new ServiceException("导出数据类型不能为空");
        }
        List<Integer> exportDataList = params.getExportDataList();
        if (exportDataList.size() == 1) {
            Integer flag = exportDataList.get(0);
            if (EXPORT_PRODUCT.equals(flag) || EXPORT_TASK.equals(flag)) {
                return exportDataList;
            }
            throw new ServiceException("导出数据类型不合法：" + flag);
        }
        if (exportDataList.size() == 2
                && exportDataList.contains(EXPORT_PRODUCT)
                && exportDataList.contains(EXPORT_TASK)) {
            return exportDataList;
        }
        throw new ServiceException("导出数据类型不合法：" + exportDataList);
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

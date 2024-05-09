package com.erp.server.file.plm;

import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskEventEnum;
import com.erp.server.file.exception.BusinessException;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProductDetailExcelDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ProductDetailExportHandlerV2 extends AbstractFileEventHandler<ProductDetailExcelDTO> {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void handle(FileTask fileTask) {

        List<ProductDetailExcelDTO> list = listSeqData(fileTask);
        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/plm/productNoSpecDetailExport.xlsx";
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        File file = null;
        try {
            file = new ExcelPrintUtils().patchExport(list, sb.toString(), excelPath);
            String s = FastDFSClientUtil.uploadFile(file, sb.toString());
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            // 删除文件
            if (file != null && Files.exists(file.toPath())) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    log.error("文件删除失败，{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.PRODUCT_DETAIL_EXPORT;
    }

    @Override
    protected int count(String metaInfo) {
        return 3000;
    }

    @Override
    protected List<ProductDetailExcelDTO> getData(String metaInfo, int limit, int offset) {
        return plmTaskFeign.getProductDetailExportData(metaInfo);
    }
}

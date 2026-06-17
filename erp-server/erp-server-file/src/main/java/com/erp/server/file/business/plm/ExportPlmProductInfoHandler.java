package com.erp.server.file.business.plm;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.dto.excel.TaskExportDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT;

/**
 * 产品开发导出
 * @date 2025-02-14
 * @author jack
 */
@Component
@Slf4j
public class ExportPlmProductInfoHandler extends AbstractPageFileEventHandler<T, ProductSearchDTO.ExportDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    private List<ProductShowDTO> getProductShow(ProductSearchDTO.ExportDTO p) {
        PagingDTO<ProductSearchDTO.ExportDTO> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(getFirstPage());
        List<ProductShowDTO> dataList = new ArrayList<>();
        boolean hasNext = true;
        int totalCount = 0;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<ProductShowDTO> data = getProductShowPageData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                dataList.addAll(data.getList());
            }
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            if (1 == getFirstPage()) {
                if (totalCount <= dto.getCurrPage() * getPageSize()) {
                    hasNext = false;
                }
            } else {
                if (totalCount <= (dto.getCurrPage() + 1) * getPageSize()) {
                    hasNext = false;
                }
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        return dataList;
    }
    private PagingVO<ProductShowDTO> getProductShowPageData(PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return exportPlmFeign.exportProductShow(dto);
    }

    private List<TaskExportDTO.ProductTaskExcelDTO> getProductTask(ProductSearchDTO.ExportDTO p) {
        PagingDTO<ProductSearchDTO.ExportDTO> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(getFirstPage());
        List<TaskExportDTO.ProductTaskExcelDTO> dataList = new ArrayList<>();
        boolean hasNext = true;
        int totalCount = 0;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<TaskExportDTO.ProductTaskExcelDTO> data = getProductTaskPageData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                dataList.addAll(data.getList());
            }
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            if (1 == getFirstPage()) {
                if (totalCount <= dto.getCurrPage() * getPageSize()) {
                    hasNext = false;
                }
            } else {
                if (totalCount <= (dto.getCurrPage() + 1) * getPageSize()) {
                    hasNext = false;
                }
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        return dataList;

    }
    private PagingVO<TaskExportDTO.ProductTaskExcelDTO> getProductTaskPageData(PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return exportPlmFeign.exportProjectTask(dto);
    }

    @Override
    public void handle(FileTask fileTask) {
        StringBuilder sb = new StringBuilder();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        ProductSearchDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductSearchDTO.ExportDTO>() {});
        /**
         * 导出数据 类型
         * 0，产品列表
         * 1. 任务列表
         */
        List<Integer> exportDataList = dto.getExportDataList();
        int size = exportDataList.size();
        Integer flag = exportDataList.get(0);

        if (size == 2) {
            List<ProductShowDTO> productShow = getProductShow(dto);
            fileTask.setCount(productShow.size());

            List<TaskExportDTO.ProductTaskExcelDTO> productTask = getProductTask(dto);

            List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
            pairList.add(new Pair(0, productShow));
            pairList.add(new Pair(1, productTask));
            String excelPath = "excel/plm/productDevelop.xlsx";

            sb.append(date);
            sb.append(name);
            sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
            try {
                byte[] bytes = new ExcelPrintUtils().sheetPatchExport(pairList, sb.toString(),excelPath);
                String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
                fileTask.setFileUrl(s);
            } catch (IOException e) {
                log.error("上传文件失败{}", e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
        }else {
            if ( 0 == flag) {
                //产品导出
                List<ProductShowDTO> productShow = getProductShow(dto);
                fileTask.setCount(productShow.size());
                String excelPath = "excel/plm/product.xlsx";
                sb.append(date);
                sb.append(name);
                sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
                try {
                    byte[] bytes = new ExcelPrintUtils().patchExport(productShow, excelPath);
                    String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
                    fileTask.setFileUrl(s);
                } catch (IOException e) {
                    log.error("上传文件失败{}", e.getMessage(), e);
                    throw new BusinessException(e.getMessage());
                }
            }else {
                //任务列表
                List<TaskExportDTO.ProductTaskExcelDTO> productTask = getProductTask(dto);
                fileTask.setCount(productTask.size());

                String excelPath = "excel/plm/productTask.xlsx";
                sb.append(date);
                sb.append(name);
                sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
                try {
                    byte[] bytes = new ExcelPrintUtils().patchExport(productTask, excelPath);
                    String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
                    fileTask.setFileUrl(s);
                } catch (IOException e) {
                    log.error("上传文件失败{}", e.getMessage(), e);
                    throw new BusinessException(e.getMessage());
                }
            }
        }
    }


    @Override
    protected PagingVO<T> getPageData(PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return null;
    }

    @Override
    protected List<T> getData(FileTask fileTask) {
        return null;
    }

    @Override
    protected String getExcelPath() {
        return "";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT;
    }
}

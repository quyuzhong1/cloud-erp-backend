package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductTaskViewDTO;
import com.erp.model.plm.dto.ProductTaskViewSearchDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_TASK_VIEW;

@Component
@Slf4j
public class ExportPlmProductTaskViewHandler extends AbstractPageFileEventHandler<ProductTaskViewDTO, ProductTaskViewSearchDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<ProductTaskViewDTO> getPageData(PagingDTO<ProductTaskViewSearchDTO> dto) {
        return exportPlmFeign.exportProductTaskView(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_TASK_VIEW;
    }

    @Override
    public String getExcelPath() {
        throw new UnsupportedOperationException("分页导出请使用 getExcelPath(P)");
    }

    @Override
    protected String getExcelPath(ProductTaskViewSearchDTO dto) {
        String excelPath = "";
        if (dto == null || Objects.isNull(dto.getType())) {
            throw new ServiceException(ApiError.PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED);
        }
        switch (dto.getType()) {
            case 1:
                excelPath = "excel/plm/taskViewPersonnel.xlsx";
                break;
            case 2:
                excelPath = "excel/plm/taskViewProduct.xlsx";
                break;
            case 3:
                excelPath = "excel/plm/taskViewPhase.xlsx";
                break;
            case 4:
                excelPath = "excel/plm/taskViewTime.xlsx";
                break;
            default:
                excelPath = "";
        }
        return excelPath;
    }
}

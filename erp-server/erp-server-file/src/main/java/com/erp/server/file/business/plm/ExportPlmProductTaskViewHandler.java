package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductTaskViewDTO;
import com.erp.model.plm.dto.ProductTaskViewSearchDTO;
import com.erp.model.plm.enums.ProductTaskViewExportTypeEnum;
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

    /**
     * 参数驱动模板处理器：运行期只走 {@link #getExcelPath(ProductTaskViewSearchDTO)}
     * （父类 {@link AbstractPageFileEventHandler#defaultPagingExportHandle} → {@code getExcelPath(P)}）。
     * 无参版本不可达，仅为兼容 {@code AbstractFileEventHandler} 的旧抽象签名而保留，禁止调用。
     */
    @Deprecated
    @Override
    public final String getExcelPath() {
        throw new UnsupportedOperationException("分页导出请使用 getExcelPath(P)");
    }

    @Override
    protected String getExcelPath(ProductTaskViewSearchDTO dto) {
        if (dto == null || Objects.isNull(dto.getType())) {
            throw new ServiceException(ApiError.PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED);
        }
        ProductTaskViewExportTypeEnum viewType = ProductTaskViewExportTypeEnum.getEnum(dto.getType());
        if (viewType == null) {
            throw new ServiceException(ApiError.PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED);
        }
        switch (viewType) {
            case PERSONNEL:
                return "excel/plm/taskViewPersonnel.xlsx";
            case PRODUCT:
                return "excel/plm/taskViewProduct.xlsx";
            case PHASE:
                return "excel/plm/taskViewPhase.xlsx";
            case IN_WAREHOUSE_TIME:
                return "excel/plm/taskViewTime.xlsx";
            default:
                throw new ServiceException(ApiError.PROJECT_TASK_VIEW_EXPORT_TYPE_REQUIRED);
        }
    }
}

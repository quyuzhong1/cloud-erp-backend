package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PROJECT_REPORT_PURCHASE_BUSINESS;

@Component
@Slf4j
public class ExportPlmProductPurchaseBusinessHandler extends AbstractPageFileEventHandler<ProjectReportFormsDTO.PagingView, ProjectReportFormsDTO.PagingParam> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<ProjectReportFormsDTO.PagingView> getData(FileTask fileTask) {
        ProjectReportFormsDTO.PagingParam dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProjectReportFormsDTO.PagingParam>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProjectReportFormsDTO.PagingView> getPageData(PagingDTO<ProjectReportFormsDTO.PagingParam> dto) {
        return exportPlmFeign.exportProductPurchaseBusiness(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PROJECT_REPORT_PURCHASE_BUSINESS;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/exportExcelProjectReportForms.xlsx";
    }
}

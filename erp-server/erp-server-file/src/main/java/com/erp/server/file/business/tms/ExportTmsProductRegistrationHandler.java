package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_PRODUCT_REGISTRATION;

@Component
@Slf4j
public class ExportTmsProductRegistrationHandler extends AbstractPageFileEventHandler<ProductRegistrationDTO.PagingVO, ProductRegistrationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/productRegistration.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_PRODUCT_REGISTRATION;
    }

    @Override
    protected List<ProductRegistrationDTO.PagingVO> getData(FileTask fileTask) {
        ProductRegistrationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductRegistrationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProductRegistrationDTO.PagingVO> getPageData(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportProductRegistration(dto);
    }
}

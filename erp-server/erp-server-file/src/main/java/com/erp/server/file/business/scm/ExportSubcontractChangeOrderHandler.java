package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUBCONTRACT_CHANGE_ORDER;

@Component
@Slf4j
public class ExportSubcontractChangeOrderHandler extends AbstractPageFileEventHandler<SubcontractChangeDTO.ListDTO, SubcontractChangeDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<SubcontractChangeDTO.ListDTO> getPageData(PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSubcontractChangeOrder(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUBCONTRACT_CHANGE_ORDER;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/subcontractChangeOrder.xlsx";
    }
}

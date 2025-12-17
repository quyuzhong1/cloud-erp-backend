package com.erp.server.file.business.scm;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_DYNAMIC_SUPPLIER_PHASE;

@Component
@Slf4j
public class ExportScmDynamicSupplierPhaseHandler extends AbstractDynamicHeadersFileEventHandler<SupplierPhaseDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        SupplierPhaseDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierPhaseDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportDynamicSupplierPhase(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_DYNAMIC_SUPPLIER_PHASE;
    }

}

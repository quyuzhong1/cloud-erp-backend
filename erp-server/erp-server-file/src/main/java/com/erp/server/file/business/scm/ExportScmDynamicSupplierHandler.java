package com.erp.server.file.business.scm;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_DYNAMIC_SUPPLIER;

@Component
@Slf4j
public class ExportScmDynamicSupplierHandler extends AbstractDynamicHeadersFileEventHandler<SupplierDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        SupplierDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportDynamicSupplier(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_DYNAMIC_SUPPLIER;
    }

}

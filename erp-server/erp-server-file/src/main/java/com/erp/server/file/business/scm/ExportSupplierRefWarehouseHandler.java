package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierRefWarehouseDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_REF_WAREHOUSE;

@Component
@Slf4j
public class ExportSupplierRefWarehouseHandler extends AbstractPageFileEventHandler<SupplierRefWarehouseDTO.ListDTO, SupplierRefWarehouseDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<SupplierRefWarehouseDTO.ListDTO> getData(FileTask fileTask) {
        SupplierRefWarehouseDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierRefWarehouseDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierRefWarehouseDTO.ListDTO> getPageData(PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportSupplierRefWarehouse(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_REF_WAREHOUSE;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierRefWarehouse.xlsx";
    }
}

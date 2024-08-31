package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CERTIFICATE;

@Component
public class ExportPlmProductCertificateHandler extends AbstractPageFileEventHandler<ProductCertificateDTO.ListDTO, ProductCertificateDTO.ExportParamDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected List<ProductCertificateDTO.ListDTO> getData(FileTask fileTask) {
        ProductCertificateDTO.ExportParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductCertificateDTO.ExportParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProductCertificateDTO.ListDTO> getPageData(PagingDTO<ProductCertificateDTO.ExportParamDTO> dto) {
        return exportPlmFeign.exportProductCertificate(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_CERTIFICATE;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/productCertificate.xlsx";
    }
}

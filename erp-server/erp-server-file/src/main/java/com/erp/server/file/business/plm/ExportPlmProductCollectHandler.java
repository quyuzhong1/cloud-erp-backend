package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_COLLECT;

@Component
@Slf4j
public class ExportPlmProductCollectHandler extends AbstractPageFileEventHandler<BomExportExcelVO, ProductSearchDTO.ExportDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<BomExportExcelVO> getData(FileTask fileTask) {
        ProductSearchDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductSearchDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<BomExportExcelVO> getPageData(PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return null;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_COLLECT;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/bom.xlsx";
    }
}

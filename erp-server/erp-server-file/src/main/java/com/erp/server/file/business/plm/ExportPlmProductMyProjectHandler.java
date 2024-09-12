package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_MY_PROJECT;

@Component
@Slf4j
public class ExportPlmProductMyProjectHandler extends AbstractPageFileEventHandler<BomExportExcelVO, SearchPagingDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<BomExportExcelVO> getData(FileTask fileTask) {
        SearchPagingDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SearchPagingDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<BomExportExcelVO> getPageData(PagingDTO<SearchPagingDTO> dto) {
        return exportPlmFeign.exportBom(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_MY_PROJECT;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/bom.xlsx";
    }
}

package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.DictHsCodeDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_DICT_HS_CODE;

/**
 * 出口申报要素导出
 * @date 2025-07-14
 * @author jack
 */
@Component
@Slf4j
public class ExportTmsDictHsCodeHandler extends AbstractPageFileEventHandler<DictHsCodeDTO.ListDTO, DictHsCodeDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<DictHsCodeDTO.ListDTO> getPageData(PagingDTO<DictHsCodeDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportDictHsCode(dto);
    }

    @Override
    protected List<DictHsCodeDTO.ListDTO> getData(FileTask fileTask) {
        DictHsCodeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DictHsCodeDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/dictHsCodeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_DICT_HS_CODE;
    }
}

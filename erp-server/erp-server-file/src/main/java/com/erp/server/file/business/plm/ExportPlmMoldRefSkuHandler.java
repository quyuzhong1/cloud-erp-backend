package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_REF_SKU;

/**
 * 模具关联SKU导出
 *
 * @author jack
 * @date 2025-10-14
 */
@Component
@Slf4j
public class ExportPlmMoldRefSkuHandler extends AbstractPageFileEventHandler<MoldRefSkuDTO.ListDTO, MoldRefSkuDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<MoldRefSkuDTO.ListDTO> getPageData(PagingDTO<MoldRefSkuDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportMoldRefSku(dto);
    }

    @Override
    protected List<MoldRefSkuDTO.ListDTO> getData(FileTask fileTask) {
        MoldRefSkuDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<MoldRefSkuDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/moldRefSku.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_MOLD_REF_SKU;
    }
}

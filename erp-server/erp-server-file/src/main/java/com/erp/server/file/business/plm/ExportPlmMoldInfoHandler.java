package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_INFO;

/**
 * 模具档案导出
 *
 * @author jack
 * @date 2025-10-11
 */
@Component
@Slf4j
public class ExportPlmMoldInfoHandler extends AbstractPageFileEventHandler<MoldInfoDTO.ListDTO, MoldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<MoldInfoDTO.ListDTO> getPageData(PagingDTO<MoldInfoDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportMoldInfo(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/plm/moldInfo.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_MOLD_INFO;
    }
}

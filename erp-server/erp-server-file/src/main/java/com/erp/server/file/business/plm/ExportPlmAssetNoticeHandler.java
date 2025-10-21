package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.AssetNoticeDTO;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ASSET_NOTICE;

/**
 * @Author: wtr
 * @Date: 2025/10/20 19:24
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportPlmAssetNoticeHandler extends AbstractPageFileEventHandler<AssetNoticeDTO.ListDTO, AssetNoticeDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<AssetNoticeDTO.ListDTO> getPageData(PagingDTO<AssetNoticeDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportAssetNotice(dto);
    }

    @Override
    protected List<AssetNoticeDTO.ListDTO> getData(FileTask fileTask) {
        AssetNoticeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetNoticeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/assetNotice.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ASSET_NOTICE;
    }
}

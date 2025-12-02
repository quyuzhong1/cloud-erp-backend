package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.AssetPurchaseChangeDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_ASSET_PURCHASE_CHANGE;

/**
 * @Author: wtr
 * @Date: 2025/10/30 12:12
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportScmAssetPurchaseChangeHandler extends AbstractPageFileEventHandler<AssetPurchaseChangeDTO.ListDTO, AssetPurchaseChangeDTO.PagingParamDTO> {

    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<AssetPurchaseChangeDTO.ListDTO> getPageData(PagingDTO<AssetPurchaseChangeDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportAssetPurchaseChange(dto);
    }

    @Override
    protected List<AssetPurchaseChangeDTO.ListDTO> getData(FileTask fileTask) {
        AssetPurchaseChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetPurchaseChangeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/scm/assetPurchaseChange.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_ASSET_PURCHASE_CHANGE;
    }
}
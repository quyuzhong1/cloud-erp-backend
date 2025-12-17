package com.erp.server.file.business.plm;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.rpc.plm.feign.ImportPlmFeign;
import com.erp.server.file.core.AbstractImportEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_SKU_IMAGES;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_SKU_STD_COST;

@Component
@Slf4j
public class ImportPlmSkuStdCostDetailHandler extends AbstractImportEventHandler<BaseDTO.ImportTypeDTO> {
    @Resource
    private ImportPlmFeign importPlmFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return IMPORT_PLM_SKU_STD_COST;
    }

    @Override
    protected void getData(FileTask fileTask) {
        BaseDTO.ImportTypeDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseDTO.ImportTypeDTO>() {
        });
        dto.setTaskId(fileTask.getId());
        importPlmFeign.skuStdCostDetail(dto);
    }
}

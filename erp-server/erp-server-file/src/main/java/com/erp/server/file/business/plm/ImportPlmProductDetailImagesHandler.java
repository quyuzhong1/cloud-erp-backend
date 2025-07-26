package com.erp.server.file.business.plm;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.rpc.plm.feign.ImportPlmFeign;
import com.erp.server.file.core.AbstractImportEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_SKU_IMAGES;

@Component
@Slf4j
public class ImportPlmProductDetailImagesHandler extends AbstractImportEventHandler<BaseDTO.ImportDTO> {
    @Resource
    private ImportPlmFeign importPlmFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return IMPORT_PLM_SKU_IMAGES;
    }

    @Override
    protected void getData(FileTask fileTask) {
        BaseDTO.ImportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseDTO.ImportDTO>() {
        });
        dto.setTaskId(fileTask.getId());
        importPlmFeign.productDetailImages(dto);
    }
}

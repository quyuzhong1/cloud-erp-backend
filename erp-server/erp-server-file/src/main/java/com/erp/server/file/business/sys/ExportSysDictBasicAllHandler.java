package com.erp.server.file.business.sys;


import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExportSysDictBasicAllHandler extends AbstractPageFileEventHandler<DictBasicAllDTO.ViewDTO, DictBasicAllDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;

    @Override
    public String getExcelPath() {
        return "excel/sys/dictBasicAll.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_DICT_BASIC_ALL;
    }


    @Override
    protected PagingVO<DictBasicAllDTO.ViewDTO> getPageData(PagingDTO<DictBasicAllDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportDictBasicAll(dto);
    }
}

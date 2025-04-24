package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_USER;

@Component
@Slf4j
public class ExportScmSupplierUserHandler extends AbstractPageFileEventHandler<SupplierUserVO, UserPagingSearchDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<SupplierUserVO> getData(FileTask fileTask) {
        UserPagingSearchDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<UserPagingSearchDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierUserVO> getPageData(PagingDTO<UserPagingSearchDTO> dto) {
        return exportScmFeign.exportSupplierUser(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_USER;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/sysUserExport.xlsx";
    }
}

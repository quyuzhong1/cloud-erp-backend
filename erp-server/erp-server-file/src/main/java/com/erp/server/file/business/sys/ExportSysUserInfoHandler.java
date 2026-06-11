package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UserManageDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_USER_INFO;

@Component
public class ExportSysUserInfoHandler extends AbstractPageFileEventHandler<UserManageDTO, SysUserInfoDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;

    @Override
    protected String getExcelPath() {
        return "excel/sys/sysUserInfo.xlsx";
    }

    @Override
    protected PagingVO<UserManageDTO> getPageData(PagingDTO<SysUserInfoDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportUserInfo(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_USER_INFO;
    }
}

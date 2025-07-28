package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.TemplateManagementEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.TemplateManagementDTO;

import java.util.List;

/**
 * <p>
 * 合同模板主表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
public interface TemplateManagementService extends SuperService<TemplateManagementEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-07-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TemplateManagementDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-07-24
    * @param dto
    * @return
    */
    Boolean update(TemplateManagementDTO.UpdateDTO dto);


    List<TemplateManagementDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<TemplateManagementDTO.ListDTO> paging(PagingDTO<TemplateManagementDTO.PagingParamDTO> dto);

    TemplateManagementDTO.ViewDTO view(String id);

    BatchResultDTO delete(String id);

    BatchResultDTO setDisabled(String id, Boolean disabledStatus);

    BatchResultDTO setDefault(String id);

    PagingVO<TemplateManagementDTO.PageSelectDTO> pagingSelect(TemplateManagementDTO.SelectDTO dto);
}

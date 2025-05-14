package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessDTO;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * <p>
 * 流程配置 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface CfgProcessService extends SuperService<CfgProcessEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(CfgProcessDTO.AddOrUpdateDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */

    /**
     * 高级查询
     * @author hcg
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    PagingVO<CfgProcessDTO.ProcessViewDTO> paging(PagingDTO<CfgProcessDTO.SearchParamDTO> dto);

    CfgProcessDTO.ProcessDTO view(String settingId);

    void delete(@NotEmpty(message = "ids不能为空") List<String> ids);

    void exportList(PagingDTO<CfgProcessDTO.SearchParamDTO> dto);

    BaseResultDTO.UpdateDTO updateDefault(CfgProcessDTO.AddOrUpdateDTO dto);

}

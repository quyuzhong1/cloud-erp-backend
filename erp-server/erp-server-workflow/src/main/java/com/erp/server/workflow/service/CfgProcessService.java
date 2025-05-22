package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;

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
    BaseResultDTO.AddDTO add(CfgProcessDTO.AddOrUpdateDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO update(CfgProcessDTO.AddOrUpdateDTO dto);

    /**
     * 高级查询
     * @author hcg
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    PagingVO<CfgProcessDTO.ProcessViewDTO> paging(PagingDTO<CfgProcessDTO.SearchParamDTO> dto);

    CfgProcessDTO.ViewDTO view(String settingId);

    void delete(@NotEmpty(message = "ids不能为空") List<String> ids);

    void exportList(CfgProcessDTO.SearchParamDTO dto);


    List<CfgProcessDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 查询流程配置
     * @author will
     * @date 2025/5/19 18:48
     * @param businessKey
     * @return CfgProcessEntity
     */
    CfgProcessEntity getByBusinessKey(String businessKey);

    void startThirdProcess(CfgProcessDTO.StartDTO dto);
}


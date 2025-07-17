package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;

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

    /**
     * 详情接口
     * @author hcg
     * @date: 2025-05-12
     * @param settingId
     * @return
     */
    CfgProcessDTO.ViewDTO view(String settingId);

    /**
     * 删除
     * @author hcg
     * @date: 2025-05-12
     * @param entity
     * @return
     */
    BatchResultDTO delete(CfgProcessRuleEntity entity);

    /**
     * 导出
     * @author hcg
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    void exportList(CfgProcessDTO.SearchParamDTO dto);

    /**
     * tab页
     * @author hcg
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    List<CfgProcessDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 查询流程配置
     * @author will
     * @date 2025/5/19 18:48
     * @param businessKey
     * @return CfgProcessEntity
     */
    CfgProcessEntity getByBusinessKey(String businessKey);

    /**
     * 测试接口，测试发起飞书审批实例
     * @author hcg
     * @date: 2025-05-12
     * @param dto
     * @return
     */
    void startThirdProcess(CfgProcessDTO.StartDTO dto);
    /**
     * erp流程下拉
     * @author will
     * @date 2025/6/27 12:27
     * @return List<ProcessSelectDTO>
     */
    List<CfgProcessDTO.ProcessSelectDTO> listProcessSelect();
}


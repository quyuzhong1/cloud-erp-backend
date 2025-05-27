package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;

import java.util.List;

/**
 * <p>
 * 流程设置值映射 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface CfgProcessValueMapService extends SuperService<CfgProcessValueMapEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(String cfgProcessId,String ruleId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(String cfgProcessId,String ruleId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> dto);

    /**
     * 根据第三方字段id以及定义code，解析form json得到选项的下拉值（包含amount）
     * @author hcg
     * @date: 2025-05-12
     * @param fieldId，approvalCode
     * @return
     */
    List<CfgProcessValueMapDTO.DropDownDTO> view(String fieldId,String approvalCode);

    /**
     * 删除
     * @author hcg
     * @date: 2025-05-12
     * @param ids
     * @return
     */
    void delete(List<String> ids);
}

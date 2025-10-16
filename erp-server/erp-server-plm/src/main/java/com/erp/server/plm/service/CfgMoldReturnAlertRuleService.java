package com.erp.server.plm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * 模具返还策略 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
public interface CfgMoldReturnAlertRuleService extends SuperService<CfgMoldReturnAlertRuleEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgMoldReturnAlertRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-15
    * @param dto
    * @return
    */
    Boolean update(CfgMoldReturnAlertRuleDTO.UpdateDTO dto);


    List<CfgMoldReturnAlertRuleDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO> paging(PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> dto);

    CfgMoldReturnAlertRuleDTO.ViewDTO view(String id);

    BatchResultDTO delete(String id);

    BatchResultDTO invalid(String id, String remark);

    BatchResultDTO disabled(String id);

    BatchResultDTO enable(String id);

    BatchResultDTO changeDisable(CfgMoldReturnAlertRuleEntity entity);

    void exportList(CfgMoldReturnAlertRuleDTO.PagingParamDTO dto, HttpServletResponse response);

    Boolean importFile(BaseDTO.ImportDTO dto);
}

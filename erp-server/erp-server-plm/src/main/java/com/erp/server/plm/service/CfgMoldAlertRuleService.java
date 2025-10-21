package com.erp.server.plm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;
import com.erp.model.plm.dto.excel.CfgMoldAlertImportExcelDTO;
import com.erp.model.plm.entity.CfgMoldAlertRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 模具预警策略 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-20
 */
public interface CfgMoldAlertRuleService extends SuperService<CfgMoldAlertRuleEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgMoldAlertRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-20
    * @param dto
    * @return
    */
    Boolean update(CfgMoldAlertRuleDTO.UpdateDTO dto);


    List<CfgMoldAlertRuleDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<CfgMoldAlertRuleDTO.ListDTO> paging(PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> dto);

    CfgMoldAlertRuleDTO.ViewDTO view(String id);

    BatchResultDTO delete(String id);

    BatchResultDTO invalid(String id, String remark);

    void exportList(CfgMoldAlertRuleDTO.PagingParamDTO dto, HttpServletResponse response);

    Boolean importFile(BaseDTO.ImportDTO dto);

    void importCfgMoldAlert(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<CfgMoldAlertImportExcelDTO> successList, List<CfgMoldAlertImportExcelDTO> errorList2, String importType);

    BatchResultDTO updateStatus(String id, @NotNull(message = "禁用状态不能为空") Boolean disabled);
}

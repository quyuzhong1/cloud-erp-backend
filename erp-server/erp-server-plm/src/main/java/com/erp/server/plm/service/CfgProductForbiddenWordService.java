package com.erp.server.plm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CfgProductForbiddenWordDTO;
import com.erp.model.plm.dto.excel.CfgProductForbiddenWordExportExcelDTO;
import com.erp.model.plm.entity.CfgProductForbiddenWordEntity;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 产品违禁词库 服务类
 * </p>
 */
public interface CfgProductForbiddenWordService extends SuperService<CfgProductForbiddenWordEntity> {

    BaseResultDTO.AddDTO add(CfgProductForbiddenWordDTO.AddDTO dto);

    Boolean update(CfgProductForbiddenWordDTO.UpdateDTO dto);

    List<CfgProductForbiddenWordDTO.TabListDTO> tabList();

    PagingVO<CfgProductForbiddenWordDTO.ListDTO> paging(PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto);

    BatchResultDTO delete(String id);

    BatchResultDTO updateStatus(String id, @NotNull(message = "禁用状态不能为空") Boolean disabled);

    Boolean exportList(CfgProductForbiddenWordDTO.PagingParamDTO dto);

    PagingVO<CfgProductForbiddenWordExportExcelDTO> exportPaging(PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto);

    List<String> listEnabledWords();

    List<String> matchEnabledWords(String productName);

    List<String> matchWords(String productName, List<String> enabledWords);

    void validateProductName(String productName);
}

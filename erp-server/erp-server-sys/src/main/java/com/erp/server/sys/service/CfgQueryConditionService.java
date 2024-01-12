package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgQueryConditionDTO;

import java.util.List;

/**
 * <p>
 * 查询条件配置表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-03
 */
public interface CfgQueryConditionService extends SuperService<CfgQueryConditionEntity> {

    Boolean add(CfgQueryConditionDTO.AddDTO dto);

    Boolean update(CfgQueryConditionDTO.UpdateDTO dto);

    List<CfgQueryConditionDTO.ViewDTO> getQueryCondition(String code);

    PagingVO<CfgQueryConditionDTO.ListDTO> paging(PagingDTO<CfgQueryConditionDTO.SearchParamDTO> searchParamDTOPagingDTO);

    PagingVO<CfgQueryConditionDTO.MenuDTO> menuPaging(PagingDTO<CfgQueryConditionDTO.MenuSearchParamDTO> menuSearchParamDTOPagingDTO);

    Boolean delete(BaseIdsDTO.IdsDTO idsDTO);

    CfgQueryConditionEntity getByCodeAndField(String code,String field);

    List<CfgQueryConditionEntity> listByCode(String code);
}

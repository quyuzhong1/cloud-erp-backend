package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.CfgDeclareEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgDeclareDTO;

/**
 * <p>
 * 申报规则表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
public interface CfgDeclareService extends SuperService<CfgDeclareEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    String add(CfgDeclareDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    Boolean update(CfgDeclareDTO.UpdateDTO dto);

    /**
     * 申报规则分页查询
     * @param dto
     * @return
     */
    PagingVO<CfgDeclareDTO.PagingViewDTO> paging(PagingDTO<CfgDeclareDTO.PagingParamDTO> dto);
}

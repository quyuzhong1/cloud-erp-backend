package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgQueryOptionDTO;

/**
 * <p>
 * 查询option配置表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
 */
public interface CfgQueryOptionService extends SuperService<CfgQueryOptionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgQueryOptionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-04
    * @param dto
    * @return
    */
    Boolean update(CfgQueryOptionDTO.UpdateDTO dto);


    void delete(CfgQueryOptionDTO.UpdateDTO dto);

    PagingVO<CfgQueryOptionDTO.ListDTO> paging(PagingDTO<CfgQueryOptionDTO.ParamDTO> dto);
}

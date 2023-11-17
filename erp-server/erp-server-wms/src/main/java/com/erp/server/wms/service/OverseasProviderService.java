package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderDTO;

import java.util.List;

/**
 * <p>
 * 海外物流商 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface OverseasProviderService extends SuperService<OverseasProviderEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasProviderDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasProviderDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 16:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasProviderDTO.ListDTO>>
     **/
    PagingVO<OverseasProviderDTO.ListDTO> paging(PagingDTO<OverseasProviderDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/11/16 16:23
     * @param id
     * @return com.erp.model.wms.dto.OverseasProviderDTO.ViewDTO
     **/
    OverseasProviderDTO.ViewDTO view(String id);

    /**
     * 服务商授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:41
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto);

    /**
     * 取消授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:44
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean cancelAuthorize(List<String> ids);
}

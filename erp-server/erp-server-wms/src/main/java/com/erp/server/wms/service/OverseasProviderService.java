package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderDTO;

/**
 * <p>
 * 海外物流商 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface OverseasProviderService extends SuperService<OverseasProviderEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasProviderDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
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
}

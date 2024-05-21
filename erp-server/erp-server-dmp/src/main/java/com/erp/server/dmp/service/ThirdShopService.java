package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdShopDTO;

/**
 * <p>
 * 第三方系统店铺表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
public interface ThirdShopService extends SuperService<ThirdShopEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-05-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdShopDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-05-17
    * @param dto
    * @return
    */
    Boolean update(ThirdShopDTO.UpdateDTO dto);


    PagingVO<ThirdShopDTO.PageDTO> paging(PagingDTO<ThirdShopDTO.PagingParamDTO> dto);

    PagingVO<ThirdShopDTO.PageSelectDTO> pagingSelect(PagingDTO<ThirdShopDTO.SelectDTO> dto);
}

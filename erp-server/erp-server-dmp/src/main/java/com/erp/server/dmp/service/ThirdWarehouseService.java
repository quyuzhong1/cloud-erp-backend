package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;

import java.util.List;

/**
 * <p>
 * 第三方系统仓库表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
public interface ThirdWarehouseService extends SuperService<ThirdWarehouseEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-05-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-05-17
    * @param dto
    * @return
    */
    Boolean update(ThirdWarehouseDTO.UpdateDTO dto);


    PagingVO<ThirdWarehouseDTO.PageDTO> paging(PagingDTO<ThirdWarehouseDTO.PagingParamDTO> dto);

    PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(PagingDTO<ThirdWarehouseDTO.SelectDTO> dto);

    ThirdWarehouseEntity getByWarehouseId(String thirdId, String sysType);

    List<ThirdWarehouseDTO.QueryMapDTO> listQueryMapping(ThirdWarehouseDTO.QueryMapParamDTO dto);
}

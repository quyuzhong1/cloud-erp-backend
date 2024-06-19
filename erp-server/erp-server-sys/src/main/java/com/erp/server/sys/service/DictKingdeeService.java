package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictKingdeeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.DictKingdeeDTO;

import java.util.List;

/**
 * <p>
 * 金蝶字典表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-06-07
 */
public interface DictKingdeeService extends SuperService<DictKingdeeEntity> {

    /**
    * 新增或更新
    * @author lrp
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean addOrUpdate(List<DictKingdeeDTO.CommonDTO> dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(DictKingdeeDTO.UpdateDTO dto);


    List<DictKingdeeDTO.ListDTO> listByParam(DictKingdeeDTO.ParamDTO param);

    List<DictKingdeeDTO.ListDTO> dropDown(String typeName);
}

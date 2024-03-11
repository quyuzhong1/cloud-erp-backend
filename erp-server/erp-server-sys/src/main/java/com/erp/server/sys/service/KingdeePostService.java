package com.erp.server.sys.service;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeePostDTO;

/**
 * <p>
 * 金蝶岗位表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeePostService extends SuperService<KingdeePostEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KingdeePostDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeePostDTO.UpdateDTO dto);


}

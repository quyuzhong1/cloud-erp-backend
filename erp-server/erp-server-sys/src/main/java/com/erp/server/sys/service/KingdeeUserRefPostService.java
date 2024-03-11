package com.erp.server.sys.service;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;

/**
 * <p>
 * 金蝶员工任岗表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeeUserRefPostService extends SuperService<KingdeeUserRefPostEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KingdeeUserRefPostDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeeUserRefPostDTO.UpdateDTO dto);


}

package com.erp.server.sys.service;
import com.erp.model.sys.dto.QuerySchemeFavoriteDTO;
import com.erp.model.sys.entity.QuerySchemeFavoriteEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-07-19
 */
public interface QuerySchemeFavoriteService extends SuperService<QuerySchemeFavoriteEntity> {


    /**
     * 修改个人方案
     * @param updateDTO
     * @return Boolean
     */
    Boolean updateById(QuerySchemeFavoriteDTO.UpdateDTO updateDTO);


    /**
     * 保存个人方案
     * @param addDTO
     * @return Boolean
     */
    Boolean add(QuerySchemeFavoriteDTO.AddDTO addDTO);

    /**
     * 查询指定用户所有保存方案
     * @param userId
     * @param modulePath
     * @return List<QuerySchemeFavoriteEntity>
     */
    List<QuerySchemeFavoriteDTO.ViewDTO> listByUserId(String userId, String modulePath);
}

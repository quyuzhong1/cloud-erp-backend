package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.QuerySchemeFavoriteDTO;
import com.erp.model.sys.entity.QuerySchemeFavoriteEntity;
import com.erp.server.sys.mapper.QuerySchemeFavoriteMapper;
import com.erp.server.sys.service.QuerySchemeFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-07-19
 */
@Slf4j
@Service
public class QuerySchemeFavoriteServiceImpl extends SuperServiceImpl<QuerySchemeFavoriteMapper, QuerySchemeFavoriteEntity> implements QuerySchemeFavoriteService {


    @Override
    public Boolean updateById(QuerySchemeFavoriteDTO.UpdateDTO updateDTO) {
        Optional<QuerySchemeFavoriteEntity> entityOpt = this.getByIdOpt(updateDTO.getId());
        if (!entityOpt.isPresent()) {
            throw new ServiceException(ApiError.SCHEME_NOT_EXIST);
        }
        String userId = UserContext.getLoginUser().getUid();
        if (StrUtil.isBlank(userId)) {
            throw new ServiceException(ApiError.ERROR_FORBIDDEN);
        }
        return this.updateById(new QuerySchemeFavoriteEntity(updateDTO,userId));
    }

    @Override
    public Boolean add(QuerySchemeFavoriteDTO.AddDTO addDTO) {
        String userId = UserContext.getLoginUser().getUid();
        if (StrUtil.isBlank(userId)) {
            throw new ServiceException(ApiError.ERROR_FORBIDDEN);
        }
        lambdaQuery().eq(QuerySchemeFavoriteEntity::getName,addDTO.getName())
                .eq(QuerySchemeFavoriteEntity::getUserId,userId)
                .eq(QuerySchemeFavoriteEntity::getModulePath, addDTO.getModulePath())
                .oneOpt().ifPresent(entity -> {
            throw new ServiceException(ApiError.SCHEME_NAME_EXIST, entity.getName());
        });
        return this.save(new QuerySchemeFavoriteEntity(addDTO,userId));
    }

    @Override
    public List<QuerySchemeFavoriteDTO.ViewDTO> listByUserId(String userId, String modulePath) {
        if (StrUtil.isBlank(userId)) {
            userId = UserContext.getLoginUser().getUid();
        }
        if (StrUtil.isBlank(userId)) {
            throw new ServiceException(ApiError.ERROR_FORBIDDEN);
        }
        List<QuerySchemeFavoriteEntity> list = lambdaQuery()
                .eq(QuerySchemeFavoriteEntity::getUserId, userId)
                .eq(QuerySchemeFavoriteEntity::getModulePath, modulePath)
                .list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanUtil.copyToList(list, QuerySchemeFavoriteDTO.ViewDTO.class);
    }
}

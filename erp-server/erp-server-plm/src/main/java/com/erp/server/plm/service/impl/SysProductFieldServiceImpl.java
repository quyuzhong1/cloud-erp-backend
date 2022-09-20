package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.model.plm.entity.SysProductFieldEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.SysProductFieldMapper;
import com.erp.server.plm.service.SysProductFieldService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname SysProductFieldServiceImpl
 * @Description TODO
 * @Date 2022-09-15 11:56
 * @Created by yl
 */
@Service
public class SysProductFieldServiceImpl extends ServiceImpl<SysProductFieldMapper, SysProductFieldEntity> implements SysProductFieldService {

    /**
     * 保存或者修改任务字段
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-15 12:12
     */
    @Override
    public Boolean saveOrUpdateField(SysProductFieldDTO dto) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String id = dto.getId();
        String name = dto.getName();
        checkFieldName(id, name);
        SysProductFieldEntity entity = new SysProductFieldEntity();
        BeanMapper.copy(dto, entity);
        List<String> contents = dto.getContents();
        if (CollectionUtils.isNotEmpty(contents)) {
            String content = String.join(",", contents);
            entity.setContent(content);
        }
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
        return this.saveOrUpdate(entity);
    }


    /**
     * 更改 状态  当更改状态的时候 对应产品就要显示对应字段
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-15 14:31
     */
    @Override
    public Boolean updateState(StateDTO dto) {
        LambdaUpdateWrapper<SysProductFieldEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysProductFieldEntity::getState, dto.getState());
        updateWrapper.eq(SysProductFieldEntity::getId, dto.getId());
        return this.update(updateWrapper);
    }

    /**
     * 分页获取设置 字段
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-15 14:41
     */
    @Override
    public PagingVO paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }


    //检查字段名是否重复
    public void checkFieldName(String id, String name) {
        List<String> fieldNames = getFieldNames(id);
        if (CollectionUtils.isNotEmpty(fieldNames) && fieldNames.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95004);
        }
    }

    //获取字段名字
    public List<String> getFieldNames(String id) {
        LambdaQueryWrapper<SysProductFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysProductFieldEntity::getName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SysProductFieldEntity::getId, id);
        }
        return this.listObjs(queryWrapper, Object::toString);
    }
}

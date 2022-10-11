package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductFieldDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.model.plm.entity.ProductFieldEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductFieldMapper;
import com.erp.server.plm.service.ProductFieldService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname SysProductFieldServiceImpl
 * @Description TODO
 * @Date 2022-09-15 11:56
 * @Created by yl
 */
@Service
public class ProductFieldServiceImpl extends ServiceImpl<ProductFieldMapper, ProductFieldEntity> implements ProductFieldService {

    /**
     * 保存或者修改任务字段
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-15 12:12
     */
    @Override
    public Boolean saveOrUpdateSysField(SysProductFieldDTO dto) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String id = dto.getId();
        String name = dto.getName();
        checkFieldName(id, name);
        ProductFieldEntity entity = new ProductFieldEntity();
        BeanMapper.copy(dto, entity);
        List<String> contents = dto.getContents();
        if (CollectionUtils.isNotEmpty(contents)) {
            String content = String.join(",", contents);
            entity.setContent(content);
        }
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
        entity.setIsSys(IsConstant.YES);
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
        LambdaUpdateWrapper<ProductFieldEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductFieldEntity::getState, dto.getState());
        updateWrapper.eq(ProductFieldEntity::getId, dto.getId());
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
    public PagingVO sysPaging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.sysPaging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }


    /**
     * 方法说明
     *
     * @return
     * @author yl
     * @date 2022-09-27 12:16
     * 保存项目字段
     */
    @Override
    public Boolean saveField(ProductFieldDTO dto) {
        String fieldId = dto.getSysFieldId();
        //检查是否已有字段
        checkField(fieldId, dto.getProductId());
        ProductFieldEntity entity = this.getById(fieldId);
        if (!Objects.isNull(entity)) {
            ProductFieldEntity productFieldEntity = new ProductFieldEntity();
            BeanMapper.copy(entity, productFieldEntity);
            productFieldEntity.setIsSys(IsConstant.NO);
            productFieldEntity.setProductId(dto.getProductId());
            productFieldEntity.setScope(IsConstant.NO);
            productFieldEntity.setIfRequired(dto.getIfRequired());
            productFieldEntity.setQuoteSysId(fieldId);
            //设置id
            productFieldEntity.setId(IdWorker.getIdStr());
            return this.save(productFieldEntity);
        }
        return false;
    }

    /**
     * 检查是否已引用该字段
     *
     * @param fieldId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-11 11:39
     */
    private void checkField(String fieldId, String productId) {
        LambdaQueryWrapper<ProductFieldEntity> queryWrapper = new LambdaQueryWrapper<ProductFieldEntity>();
        queryWrapper.eq(ProductFieldEntity::getQuoteSysId, fieldId);
        queryWrapper.eq(ProductFieldEntity::getProductId, productId);
        ProductFieldEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
           throw new ServiceException(ApiError.ERROR_95022);
        }
    }

    /**
     * 产品字段分页展示
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-27 14:05
     */
    @Override
    public PagingVO paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }

    @Override
    public List<Map<String, Object>> sysList() {
        LambdaQueryWrapper<ProductFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProductFieldEntity::getId, ProductFieldEntity::getName);
        queryWrapper.eq(ProductFieldEntity::getScope, IsConstant.NO);
        queryWrapper.eq(ProductFieldEntity::getIsSys, IsConstant.YES);
        return this.listMaps(queryWrapper);
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
        LambdaQueryWrapper<ProductFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProductFieldEntity::getName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductFieldEntity::getId, id);
        }
        return this.listObjs(queryWrapper, Object::toString);
    }
}

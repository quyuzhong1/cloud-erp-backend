package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.ApplicationCategoryDTO;
import com.erp.model.plm.entity.ApplicationCategoryEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.mapper.ApplicationCategoryMapper;
import com.erp.server.plm.service.ApplicationCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 产品应用分类 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
 */
@Slf4j
@Service
public class ApplicationCategoryServiceImpl extends SuperServiceImpl<ApplicationCategoryMapper, ApplicationCategoryEntity> implements ApplicationCategoryService {

    @Lazy
    @Resource
    private ProductInfoService productInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ApplicationCategoryDTO.AddDTO addDTO) {
        verifyData(addDTO.getName(), addDTO.getCode());
        verifyData(addDTO.getName(), addDTO.getCode());
        ApplicationCategoryEntity applicationCategoryEntity = new ApplicationCategoryEntity();
        applicationCategoryEntity.setName(addDTO.getName());
        applicationCategoryEntity.setCode(addDTO.getCode());
        boolean save = super.save(applicationCategoryEntity);
        if(!save) {
            throw new ServiceException("应用分类保存失败");
        }
        return new BaseResultDTO.AddDTO(applicationCategoryEntity.getId(), addDTO.getCode());
    }

    /**
     * 校验数据
     * @param name 名字
     * @param code code
     */
    private void verifyData(String name, String code) {
        verifyData(name, code, null);
    }

    /**
     * 校验数据
     * @param name 名字
     * @param code code
     * @param id id
     */
    private void verifyData(String name, String code, String id) {
        int nameCount = count(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getName, name)
                .ne(StringUtils.hasText(id), ApplicationCategoryEntity::getId, id));
        if (nameCount > 0) {
            throw new ServiceException(ApiError.ERROR_95245);
        }
        int codeCount = count(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getCode, code)
                .ne(StringUtils.hasText(id), ApplicationCategoryEntity::getId, id));
        if (codeCount > 0) {
            throw new ServiceException(ApiError.ERROR_95244);
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ApplicationCategoryDTO.UpdateDTO addOrUpdateDTO) {
        verifyData(addOrUpdateDTO.getName(), addOrUpdateDTO.getCode(), addOrUpdateDTO.getId());
        ApplicationCategoryEntity applicationCategoryEntity =  BeanMapperUtils.map(ApplicationCategoryEntity.class, addOrUpdateDTO);
        boolean save = super.updateById(applicationCategoryEntity);
        if(!save) {
            throw new ServiceException("应用分类保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        int count = productInfoService.count(Wrappers.<ProductInfoEntity>lambdaQuery().eq(ProductInfoEntity::getApplicationCategoryId, id));
        if (count > 0) {
            throw new ServiceException("分类下存在产品，请调整分类后删除");
        }
        removeById(id);
    }

    @Override
    public List<ApplicationCategoryDTO.ViewDTO> list(String searchKeyword) {
        return baseMapper.list(searchKeyword);
    }

    @Override
    public ApplicationCategoryEntity getByName(String applicationCategory) {
        return getOne(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getName, applicationCategory));
    }

}

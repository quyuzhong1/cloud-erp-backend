package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.business.dto.base.BasePagingSearchDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.SysPostDTO;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.server.sys.mapper.SysPostMapper;
import com.erp.server.sys.service.SysPostService;
import com.erp.server.sys.service.SysPostUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Classname SysPostServiceImpl

 * @Date 2022-07-12 16:09
 * @Created by yl
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {


    @Autowired
    private SysPostUserService sysPostUserService;

    /**
     * 保存或者修改岗位
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 16:19
     */

    @Override
    public boolean savePost(SysPostDTO dto) {
        String postName = dto.getPostName();
        SysPostEntity postEntity=getPostEntityByName(postName);
        if(!Objects.isNull(postEntity)){
            throw new ServiceException(ApiError.ERROR_9023);
        }
        SysPostEntity entity = new SysPostEntity();
        BeanMapperUtils.copy(dto, entity);
        return this.save(entity);
    }


    public SysPostEntity getPostEntityByName(String postName) {
        LambdaQueryWrapper<SysPostEntity> queryWrapper = new LambdaQueryWrapper<SysPostEntity>();
        queryWrapper.eq(SysPostEntity::getPostName,postName);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }


    /**
     * 修改 部门信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 16:26
     */

    @Override
    public boolean updatePost(SysPostDTO dto) {
        SysPostEntity entity = new SysPostEntity();
        BeanMapperUtils.copy(dto, entity);
        return this.updateById(entity);
    }


    /**
     * 分页获取部门信息
     *
     * @param dto
     * @return com.cloud.erp.common.common.vo.PagingVO
     * @author yl
     * @date 2022-07-12 16:32
     */

    @Override
    public PagingVO paging(PagingDTO<BasePagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BasePagingSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    /**
     * 批量删除岗位Id
     *
     * @param ids
     * @return boolean
     * @author yl
     * @date 2022-07-12 17:06
     */

    @Override
    @Transactional
    public boolean removePostByIds(List<String> ids) {
        boolean flag = this.removeByIds(ids);
        if (flag) {
            //删除岗位员工 关系
            sysPostUserService.removeByPostId(ids);
        }
        return flag;
    }

    /**
     * 根据关键字搜索岗位信息
     *
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.entity.SysPostEntity>
     * @author yl
     * @date 2022-07-12 17:4563
     */

    @Override
    public List<SysPostEntity> findPost(BaseSearchDTO dto) {
        LambdaQueryWrapper<SysPostEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(dto.getSearchKeyword())) {
            queryWrapper.like(SysPostEntity::getPostName, dto.getSearchKeyword());
        }
        queryWrapper.orderByDesc(SysPostEntity::getUpdateTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<SysPostEntity> listByRequisitionSetting() {
        List<String> list = new ArrayList<>();
        list.add("创建人");
        list.add("处理人");
        return this.lambdaQuery().in(SysPostEntity::getPostName,list).list();
    }
}

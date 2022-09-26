package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SaveBasicCategoryDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.server.plm.mapper.BasicCategoryMapper;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品分类表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class BasicCategoryServiceImpl extends ServiceImpl<BasicCategoryMapper, BasicCategoryEntity> implements BasicCategoryService {


    @Autowired
    private ProductInfoService productInfoService;

    /**
     * 保存 产品分类信息
     *
     * @param dto
     */
    @Override
    public void addCategory(SaveBasicCategoryDTO dto) {
        String categoryName = dto.getName();
        checkCategoryName(categoryName);
        BasicCategoryEntity entity = new BasicCategoryEntity();
        entity.setPid(dto.getPid());
        entity.setName(categoryName);
        this.save(entity);
    }

    /**
     * 修改产品分类名
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-13 14:08
     */
    @Override
    public Boolean updateCategory(UpdateBasicNameDTO dto) {
        String categoryName = dto.getName();
        checkCategoryName(categoryName);
        LambdaUpdateWrapper<BasicCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BasicCategoryEntity::getName, categoryName);
        updateWrapper.eq(BasicCategoryEntity::getId, dto.getId());
        return this.update(updateWrapper);
    }


    /**
     * 获取产品分类树结构
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.BasicCategoryEntity>
     * @author yl
     * @date 2022-09-13 14:15
     */
    @Override
    public List<BasicCategoryDTO> getTree() {
        List<BasicCategoryEntity> list = this.list();
        List<BasicCategoryDTO> allList = BeanMapper.copyList(list,BasicCategoryDTO.class);
        List<BasicCategoryDTO> treeList = allList.stream().
                filter(item -> "0".equals(item.getPid())).
                map(c -> {
                    c.setChildrenList(getChildrenList(c, allList));
                    return c;
                }).collect(Collectors.toList());

        return treeList;
    }


    /**
     * 删除分类
     *
     * @param id
     * @return boolean
     * @author yl
     * @date 2022-09-16 15:11
     */
    @Override
    public Boolean deleteById(String id) {
        checkId(id);
        return this.removeById(id);
    }


    /**
     * 检查 id 下是否存在 产品 以及分类
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-09-16 15:22
     */
    public Integer checkId(String id) {
        List<BasicCategoryEntity> childrenList = getChildren(id);
        if (CollectionUtils.isNotEmpty(childrenList) && childrenList.size() > 0) {
            throw new ServiceException(ApiError.ERROR_95005);
        }
        int count = productInfoService.countByCategoryId(id);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95006);
        }
        return count;
    }

    /**
     * 根据 分类id 获取它的子集
     *
     * @param pid
     * @return java.util.List<com.erp.model.plm.entity.BasicCategoryEntity>
     * @author yl
     * @date 2022-09-16 15:26
     */
    public List<BasicCategoryEntity> getChildren(String pid) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicCategoryEntity::getPid, pid);
        return this.list(queryWrapper);
    }


    /**
     * 获取子类数据
     *
     * @param item
     * @param allList
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     * @author yl
     * @date 2022-09-13 14:49
     */
    private List<BasicCategoryDTO> getChildrenList(BasicCategoryDTO item, List<BasicCategoryDTO> allList) {
        List<BasicCategoryDTO> collectList = allList.stream().
                filter(c -> item.getId().equals(c.getPid())).
                map(b -> {
                    b.setChildrenList(getChildrenList(b, allList));
                    return b;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? new ArrayList<>() : collectList;
    }


    /**
     * 检查分类名是否重复
     *
     * @param categoryName
     * @return void
     * @author yl
     * @date 2022-09-13 12:17
     */
    private void checkCategoryName(String categoryName) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BasicCategoryEntity::getName, categoryName);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95000);
        }
    }
}

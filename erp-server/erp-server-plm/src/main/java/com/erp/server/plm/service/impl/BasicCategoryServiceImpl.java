package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
        checkCategoryName(categoryName,null);
        checkCategoryCode(dto.getCode(),dto.getPid(),null);
        BasicCategoryEntity entity = new BasicCategoryEntity();
        entity.setPid(dto.getPid());
        entity.setName(categoryName);
        entity.setCode(dto.getCode());
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
        checkCategoryName(categoryName,dto.getId());
        BasicCategoryEntity found = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(found)) {
            throw new ServiceException(ApiError.ERROR_95072);
        }
        checkCategoryCode(dto.getCode(),found.getPid(),dto.getId());
        LambdaUpdateWrapper<BasicCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BasicCategoryEntity::getName, categoryName);
        updateWrapper.set(BasicCategoryEntity::getCode,dto.getCode());
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
        List<BasicCategoryDTO> allList = BeanMapper.copyList(list, BasicCategoryDTO.class);
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
     * 根据分类id 找出父类的id
     *
     * @param categoryId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-28 10:36
     */
    @Override
    public List<String> getPidList(String categoryId) {
        List<String> resultList = new LinkedList<>();
        BasicCategoryEntity category = this.getById(categoryId);
        if (category != null) {
            resultList.add(categoryId);
            List<BasicCategoryEntity> list = this.list();
            if (!category.getPid().equals("0")) {
                getPids(category.getPid(), resultList, list);
            }
            Collections.reverse(resultList);
        }

        return resultList;
    }

    /**
     * @description: 根据产品分类id获取最高级分类
     * @author Will
     * @date: 2022/11/22 12:22
     * @param id
     * @return String
     */
    @Override
    public void getBestEntity(String id,BasicCategoryEntity bestEntity) {
        BasicCategoryEntity entity = this.getById(id);
        if (entity.getPid().equals("0")) {
            BeanMapperUtils.copy(entity,bestEntity);
            return;
        }
         getBestEntity(entity.getPid(),bestEntity);
    }

    @Override
    public BasicCategoryDTO getCategoryByParam(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String id = params.get("id");
        String name = params.get("name");
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(BasicCategoryEntity::getId,id);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.eq(BasicCategoryEntity::getName,name);
        }
        queryWrapper.last("limit 1");
        BasicCategoryEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity)) {
            BasicCategoryDTO dto = new BasicCategoryDTO();
            BeanUtils.copyProperties(entity,dto);
            return dto;
        }
        return null;
    }

    /**
     * 获取父级id 集合
     *
     * @param id
     * @param resultList
     * @return void
     * @author yl
     * @date 2022-09-28 10:52
     */
    private void getPids(String id, List<String> resultList, List<BasicCategoryEntity> list) {
        resultList.add(id);
        BasicCategoryEntity entity = list.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if (!Objects.isNull(entity) && !entity.getPid().equals("0")) {
            getPids(entity.getPid(), resultList, list);
        }


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
    private void checkCategoryName(String categoryName,String id) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BasicCategoryEntity::getName, categoryName);
        queryWrapper.last("LIMIT 1");
        BasicCategoryEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(id)) {
            throw new ServiceException(ApiError.ERROR_95000);
        }
    }

    /**
     * @description: 分类编码信息验证
     * @author Will
     * @date: 2022/11/22 12:11
     * @param code
     * @param pid
     * @param id
     */
    private void checkCategoryCode(String code,String pid,String id) {
        if ("0".equals(pid)) {
            //一级分类必须要填分类代码
            if (StringUtils.isBlank(code)) {
                throw new ServiceException(ApiError.ERROR_95069);
            } else {
                Boolean flag = false;
                for (int i = 65;i <= 90; i++) {
                    char c = (char) (i);
                    if ( code.equals(String.valueOf(c)) ) {
                       flag = true;
                    }
                }
                if (!flag) {
                    throw new ServiceException(ApiError.ERROR_95071);
                }
                LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper();
                queryWrapper.eq(BasicCategoryEntity::getCode, code);
                queryWrapper.last("LIMIT 1");
                BasicCategoryEntity entity = this.getOne(queryWrapper);
                if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(id)) {
                    throw new ServiceException(ApiError.ERROR_95070);
                }
            }
        }
    }


    /**
     * @param categoryName：类别名称
     * @return BasicCategoryEntity
     * @Description 根据类别名称查询类别信息
     * @Author Luo_WG
     * @Date 2022/9/28 18:51
     **/
    public BasicCategoryEntity getCategoryByName(String categoryName) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(BasicCategoryEntity::getName, categoryName);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}

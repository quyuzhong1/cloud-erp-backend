package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductImgCategoryEntity;
import com.erp.server.plm.mapper.ProductImgCategoryMapper;
import com.erp.server.plm.service.ProductImgCategoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductImgCategoryDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 图片分类表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
@Slf4j
@Service
public class ProductImgCategoryServiceImpl extends SuperServiceImpl<ProductImgCategoryMapper, ProductImgCategoryEntity> implements ProductImgCategoryService {
    @Resource
    private OperateLogService operateLogService;
    
    private static final String CLASSPATH = String.valueOf(ProductImgCategoryEntity.class);

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductImgCategoryDTO.AddDTO addDTO) {
        // 检查最多支持5级分类
        if (addDTO.getLevel() != null && addDTO.getLevel() > 5) {
            throw new ServiceException(ApiError.COMMON_CATEGORY_LEVEL_EXCEED_MAX, "5");
        }
        
        // 如果有父分类，检查父分类的级别，防止在5级分类下新增子分类
        if (StrUtil.isNotBlank(addDTO.getParentId())) {
            ProductImgCategoryEntity parent = super.getById(addDTO.getParentId());
            if (parent != null && parent.getLevel() != null && parent.getLevel() >= 5) {
                throw new ServiceException(ApiError.COMMON_CATEGORY_LEVEL_EXCEED_MAX, "5");
            }
        }
        
        ProductImgCategoryEntity productImgCategoryEntity = new ProductImgCategoryEntity();
        BeanMapperUtils.copy(addDTO, productImgCategoryEntity);

        // 数据处理
        handleData(productImgCategoryEntity);

        log.info("开始新增图片分类单");
        boolean save = super.save(productImgCategoryEntity);
        if(!save) {
            throw new ServiceException(ApiError.BILL_SAVE_FAIL, "图片分类");
        }

        // 操作日志
        String content = StrUtil.format("新增了图片分类[{}]", productImgCategoryEntity.getName());
        operateLogService.addSysLogBySave(content, CLASSPATH, productImgCategoryEntity.getId(), productImgCategoryEntity.getParentId());

        return new BaseResultDTO.AddDTO(productImgCategoryEntity.getId(), productImgCategoryEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductImgCategoryDTO.UpdateDTO addOrUpdateDTO) {
        ProductImgCategoryEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类单"));
        
        // 检查是否允许编辑
        checkUpdatePermission(old);
        
        ProductImgCategoryEntity productImgCategoryEntity =  BeanMapperUtils.map(ProductImgCategoryEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(productImgCategoryEntity);
        log.info("编辑 开始修改图片分类单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productImgCategoryEntity);
        if(!save) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }

        // 记录主单操作日志
        log.info("编辑 开始记录图片分类单日志数据，id：【{}】", productImgCategoryEntity.getId());
        ProductImgCategoryDTO.ViewDTO oldDto = BeanMapperUtils.map(ProductImgCategoryDTO.ViewDTO.class, old);
        ProductImgCategoryDTO.ViewDTO newDto = BeanMapperUtils.map(ProductImgCategoryDTO.ViewDTO.class, productImgCategoryEntity);
        String msg = StrUtil.format("图片分类[{}]", productImgCategoryEntity.getName());
        operateLogService.addSysLogByUpdate(oldDto, newDto, CLASSPATH, productImgCategoryEntity.getId(), productImgCategoryEntity.getParentId(), msg);
        return Boolean.TRUE;
    }




    /**
    * 新增修改处理数据
    */
    private void handleData(ProductImgCategoryEntity productImgCategoryEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 检查分类是否允许删除
     * 所有分类、产品主图、产品图片分类、未分类不允许删除
     * 这些分类都是系统分类，通过isSystem字段判断
     */
    private void checkDeletePermission(ProductImgCategoryEntity entity) {
        if (entity == null) {
            return;
        }
        // 系统分类不允许删除
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new ServiceException(ApiError.COMMON_SYSTEM_CATEGORY_DELETE_FORBIDDEN);
        }

        // 检查是否有子分类
        LambdaQueryWrapper<ProductImgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImgCategoryEntity::getParentId, entity.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.COMMON_DELETE_CHILD_NODE_EXISTS);
        }
        //todo 删除分支需要判断是否有图片绑定在这个分类上 如果有 就禁止删除
    }

    /**
     * 检查分类是否允许编辑
     * 所有分类、产品主图、产品图片分类不允许编辑
     */
    private void checkUpdatePermission(ProductImgCategoryEntity entity) {
        if (entity == null) {
            return;
        }
        String name = entity.getName();
        // 检查是否是特殊分类：所有分类、产品主图、产品图片分类（产品营销图、京东营销图、其他产品图等）
        // 由于这些分类都是系统分类，通过isSystem字段判断更准确
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new ServiceException(ApiError.COMMON_SYSTEM_CATEGORY_UPDATE_FORBIDDEN);
        }
    }

    @Override
    public ProductImgCategoryDTO.ViewDTO view(String id) {
    ProductImgCategoryEntity productImgCategoryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类"));
    ProductImgCategoryDTO.ViewDTO data = BeanMapperUtils.map(ProductImgCategoryDTO.ViewDTO.class, productImgCategoryEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(ProductImgCategoryDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<ProductImgCategoryDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(ProductImgCategoryDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean delete(String id) {
        ProductImgCategoryEntity entity = super.getById(id);
        if (entity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类");
        }
        
        // 检查是否允许删除
        checkDeletePermission(entity);

        
        return super.removeById(id);
    }

    /**
     * 列表查询（不分页，树结构）
     */
    @Override
    public List<ProductImgCategoryDTO.TreeDTO> listTree(ProductImgCategoryDTO.ListTreeParamDTO paramDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ProductImgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (paramDTO != null && StrUtil.isNotBlank(paramDTO.getName())) {
            queryWrapper.like(ProductImgCategoryEntity::getName, paramDTO.getName());
        }
        queryWrapper.orderByAsc(ProductImgCategoryEntity::getSort);
        queryWrapper.orderByAsc(ProductImgCategoryEntity::getCreateTime);
        
        // 查询所有数据
        List<ProductImgCategoryEntity> allList = this.list(queryWrapper);
        if (CollUtil.isEmpty(allList)) {
            return new ArrayList<>();
        }
        
        // 转换为DTO
        List<ProductImgCategoryDTO.TreeDTO> allTreeList = allList.stream()
                .map(entity -> {
                    ProductImgCategoryDTO.TreeDTO treeDTO = new ProductImgCategoryDTO.TreeDTO();
                    treeDTO.setId(entity.getId());
                    treeDTO.setName(entity.getName());
                    treeDTO.setParentId(entity.getParentId());
                    treeDTO.setLevel(entity.getLevel());
                    treeDTO.setIsSystem(entity.getIsSystem());
                    treeDTO.setSort(entity.getSort());
                    treeDTO.setChildrenList(new ArrayList<>());
                    return treeDTO;
                })
                .collect(Collectors.toList());
        
        // 构建树结构（反向构造：从子节点到父节点）
        Map<String, ProductImgCategoryDTO.TreeDTO> treeMap = new HashMap<>();
        List<ProductImgCategoryDTO.TreeDTO> rootList = new ArrayList<>();
        
        // 先建立ID映射
        for (ProductImgCategoryDTO.TreeDTO treeDTO : allTreeList) {
            treeMap.put(treeDTO.getId(), treeDTO);
        }
        
        // 构建树结构
        for (ProductImgCategoryDTO.TreeDTO treeDTO : allTreeList) {
            String parentId = treeDTO.getParentId();
            if (StrUtil.isBlank(parentId) || "0".equals(parentId) || !treeMap.containsKey(parentId)) {
                // 根节点
                rootList.add(treeDTO);
            } else {
                // 子节点，添加到父节点的childrenList
                ProductImgCategoryDTO.TreeDTO parent = treeMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildrenList() == null) {
                        parent.setChildrenList(new ArrayList<>());
                    }
                    parent.getChildrenList().add(treeDTO);
                }
            }
        }
        
        return rootList;
    }
}

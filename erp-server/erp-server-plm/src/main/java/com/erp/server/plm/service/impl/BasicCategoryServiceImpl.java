package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.mapper.BasicCategoryMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeCategoryService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Autowired
    private SyncKingdeeCategoryService syncKingdeeCategoryService;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    /**
     * 保存 产品分类信息
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void addCategory(SaveBasicCategoryDTO dto) {
        String categoryName = dto.getName();
        checkCategoryName(categoryName, null);
        checkCategoryCode(dto.getCode(), dto.getPid(), null);
        BasicCategoryEntity entity = new BasicCategoryEntity();
        entity.setPid(dto.getPid());
        entity.setName(categoryName);
        entity.setCode(dto.getCode());
        this.save(entity);
        //code为空不发送金蝶
        if (StringUtils.isBlank(dto.getCode())) {
            return;
        }
        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updateCategory(UpdateBasicNameDTO dto) {
        String categoryName = dto.getName();
        checkCategoryName(categoryName, dto.getId());
        BasicCategoryEntity found = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(found)) {
            throw new ServiceException(ApiError.ERROR_95072);
        }
        checkCategoryCode(dto.getCode(), found.getPid(), dto.getId());
        BasicCategoryEntity entity = new BasicCategoryEntity();
        BeanMapperUtils.copy(found,entity);
        entity.setCode(dto.getCode());
        entity.setName(categoryName);
        this.updateById(entity);

        List<BasicCategoryEntity> resultList = new ArrayList<>();
        resultList.add(entity);
        //编辑的时候如果变动了一级编码则需要更新金蝶二级类目编码
        if ("0".equals(found.getPid()) && !StringUtils.equals(dto.getCode(),found.getCode())) {
            List<BasicCategoryEntity> list = this.lambdaQuery().eq(BasicCategoryEntity::getPid, found.getId()).list();
            if (CollectionUtils.isNotEmpty(list)) {
               resultList.addAll(list);
            }
        }
        //发送金蝶
        sendPushTask(resultList,SyncOperateEnum.OPERATE_APPROVE.getCode());
        return Boolean.TRUE;
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
        List<String> categoryIds = list.stream().map(BasicCategoryEntity::getId).collect(Collectors.toList());
        //根据分类id 获取产品信息
        List<ProductInfoEntity> productList = productInfoService.getByCategoryIds(categoryIds, IsConstant.YES);
        List<BasicCategoryDTO> allList = BeanMapper.copyList(list, BasicCategoryDTO.class);
        //获取到对应数据库的树结构
        List<BasicCategoryTreeDTO> categoryTreeList = this.getDbTree();

        List<BasicCategoryDTO> treeList = allList.stream().
                filter(item -> "0".equals(item.getPid())).
                map(c -> {
                    Long productQuantity = productList.stream().filter(p -> p.getCategoryId().equals(c.getId())).count();
                    c.setProductQuantity(productQuantity);
                    c.setChildrenList(getChildrenList(c, allList, productList, categoryTreeList));
                    return c;
                }).collect(Collectors.toList());

        return treeList;

    }


    /**
     * 查询产品开发管理 分类
     *
     * @param isArchive 是否归档
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     * @author yl
     * @date 2023-03-02 10:16
     */
    public List<BasicCategoryDTO> getCategoryTreeList(List<BasicCategoryEntity> list, boolean isFinishedProductDev, boolean isArchive) {
        List<String> categoryIds = list.stream().map(BasicCategoryEntity::getId).collect(Collectors.toList());
        List<ProductInfoEntity> productList = productInfoService.getListByCategoryIds(categoryIds, isFinishedProductDev, isArchive);
        List<BasicCategoryDTO> allList = BeanMapper.copyList(list, BasicCategoryDTO.class);
        //获取到对应数据库的树结构
        List<BasicCategoryTreeDTO> categoryTreeList = this.getDbTree();

        List<BasicCategoryDTO> treeList = allList.stream().
                filter(item -> "0".equals(item.getPid())).
                map(c -> {
                    List<String> categoryList = getChildCategory(categoryTreeList, c.getId());

                    Long productQuantity = productList.stream().filter(p -> categoryList.contains(p.getCategoryId())).map(ProductInfoEntity::getId).distinct().count();
                    c.setProductQuantity(productQuantity);
                    c.setChildrenList(getChildrenList(c, allList, productList, categoryTreeList));
                    return c;
                }).collect(Collectors.toList());
        return treeList;
    }


    public List<String> getChildCategory(List<BasicCategoryTreeDTO> categoryTreeList, String id) {
        List<String> categoryIds = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(categoryTreeList)) {
            for (BasicCategoryTreeDTO item : categoryTreeList) {
                if (item.getPath().contains(id)) {
                    categoryIds.add(item.getId());
                }
            }
        }
        return categoryIds;
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean deleteById(String id) {
        checkId(id);
        BasicCategoryEntity entity = this.getById(id);

        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_DELETE.getCode());
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
     * @param id
     * @return String
     * @description: 根据产品分类id获取最高级分类
     * @author Will
     * @date: 2022/11/22 12:22
     */
    @Override
    public void getBestEntity(String id, BasicCategoryEntity bestEntity) {
        BasicCategoryEntity entity = this.getById(id);
        if (entity.getPid().equals("0")) {
            BeanMapperUtils.copy(entity, bestEntity);
            return;
        }
        getBestEntity(entity.getPid(), bestEntity);
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
            queryWrapper.eq(BasicCategoryEntity::getId, id);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.eq(BasicCategoryEntity::getName, name);
        }
        queryWrapper.last("limit 1");
        BasicCategoryEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity)) {
            BasicCategoryDTO dto = new BasicCategoryDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }
        return null;
    }

    @Override
    public List<BasicCategoryEntity> listParentEntity(String categoryId) {
        List<BasicCategoryEntity> list = new ArrayList<>();
        setParentEntity(categoryId, list);
        return list;
    }


    /**
     * 获取列表的 分类树结构 根据产品类型
     *
     * @param type
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     * @author yl
     * @date 2023-03-02 11:09
     */
    @Override
    public List<BasicCategoryDTO> getListTree(String type) {
        List<BasicCategoryEntity> list = this.list();
        //产品
        String product = ProductConstant.product;
        //产品归档
        String productArchive = ProductConstant.PRODUCT_ARCHIVE;


        //产品归档管理分类
        if (productArchive.equals(type)) {
            return getCategoryTreeList(list, true, true);
        }
        //产品管理列表分类
        if (product.equals(type)) {
            return getSkuCategoryTreeList(list);
        }

        //产品开发管理 分类
        return getCategoryTreeList(list, true, false);

    }

    /**
     * 获取产品管理的分类列表
     *
     * @param list
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     * @author yl
     * @date 2023-03-10 13:56
     */
    private List<BasicCategoryDTO> getSkuCategoryTreeList(List<BasicCategoryEntity> list) {
        List<String> categoryIds = list.stream().map(BasicCategoryEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(categoryIds)) {
            return new ArrayList<>();
        }
        List<SkuCategoryDTO> skuList = baseMapper.getSkuByCategoryIds(categoryIds);
        List<BasicCategoryDTO> allList = BeanMapper.copyList(list, BasicCategoryDTO.class);
        //获取到对应数据库的树结构
        List<BasicCategoryTreeDTO> categoryTreeList = this.getDbTree();
        List<BasicCategoryDTO> treeList = allList.stream().
                filter(item -> "0".equals(item.getPid())).
                map(c -> {
                    List<String> categoryList = getChildCategory(categoryTreeList, c.getId());

                    Long skuQuantity = skuList.stream().filter(p -> categoryList.contains(p.getCategoryId())).map(SkuCategoryDTO::getSkuId).count();
                    c.setProductQuantity(skuQuantity);
                    c.setChildrenList(getSkuChildrenList(c, allList, skuList, categoryTreeList));
                    return c;
                }).collect(Collectors.toList());
        return treeList;
    }

    private List<BasicCategoryDTO> getSkuChildrenList(BasicCategoryDTO item, List<BasicCategoryDTO> allList, List<SkuCategoryDTO> skuList, List<BasicCategoryTreeDTO> categoryTreeList) {

        List<BasicCategoryDTO> collectList = allList.stream().
                filter(c -> item.getId().equals(c.getPid())).
                map(b -> {
                    List<String> categoryIdList = getChildCategory(categoryTreeList, b.getId());
                    Long productQuantity = skuList.stream().filter(p -> categoryIdList.contains(p.getCategoryId())).count();
                    b.setProductQuantity(productQuantity);
                    b.setChildrenList(getSkuChildrenList(b, allList, skuList, categoryTreeList));
                    return b;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? new ArrayList<>() : collectList;

    }


    /**
     * 获取数据库的分类树结构
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryTreeDTO>
     * @author yl
     * @date 2023-03-02 15:10
     */
    @Override
    public List<BasicCategoryTreeDTO> getDbTree() {
        return baseMapper.getDbTree();
    }


    /**
     * 获取到所有子
     *
     * @param categoryId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-02 15:44
     */
    @Override
    public List<String> getChildrenCategoryIds(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return new ArrayList<>();
        }
        List<BasicCategoryTreeDTO> dbTreeList = getDbTree();
        return getChildCategory(dbTreeList, categoryId);
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(BasicCategoryEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeId),BasicCategoryEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    /**
     * 获取到父级分类
     * @author yl
     * @date 2023-09-15 9:42
     * @param
     * @return java.util.List<com.erp.model.plm.entity.BasicCategoryEntity>
     */
    @Override
    public List<BasicCategoryEntity> listParentCategory() {
        return this.lambdaQuery().eq(BasicCategoryEntity::getPid,"0").orderByDesc(BasicCategoryEntity::getId).list();
    }

    /**
     * 通过子类id或名称获取到父级的分类
     */
    @Override
    public BasicCategoryDTO getParentCategoryByParam(Map<String, String> params) {
        if (null == params || params.isEmpty()) {
            return null;
        }
        String id = params.get("id");
        String name = params.get("name");
        BasicCategoryEntity entity = lambdaQuery()
                .eq(StringUtils.isNotBlank(id), BasicCategoryEntity::getId, id)
                .eq(StringUtils.isNotBlank(name), BasicCategoryEntity::getName, name)
                .last("limit 1")
                .one();
        if (null == entity){
            return null;
        }
        if (StringUtils.isBlank(entity.getPid()) || "0".equals(entity.getPid())){
            return null;
        }
        BasicCategoryEntity parentEntity = getById(entity.getPid());
        if (null == parentEntity){
            return null;
        }
        BasicCategoryDTO dto = new BasicCategoryDTO();
        BeanUtils.copyProperties(parentEntity, dto);
        return dto;

    }

    @Override
    @Cacheable(cacheNames = "cache:plm:getCategoryList",keyGenerator = "myKeyGenerator")
    public List<BasicCategoryEntity> getCategoryList() {
        return lambdaQuery().list();
    }

    @Override
    @Cacheable(cacheNames = "cache:plm:listCategoryDropDown",keyGenerator = "myKeyGenerator")
    public List<CategoryControllerDTO.CategoryDropDownDTO> listCategoryDropDown(Integer grade) {
        List<BasicCategoryEntity> list = this.lambdaQuery()
                .ne(2 == grade, BasicCategoryEntity::getPid,"0")
                .eq(1 == grade, BasicCategoryEntity::getPid,"0")
                .list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<CategoryControllerDTO.CategoryDropDownDTO> result = list.stream()
                .map(CategoryControllerDTO.CategoryDropDownDTO::new)
                .collect(Collectors.toList());
        return result;
    }
    /**
     * 获取二级分类的列表 拼接一级名称
     * @return
     */
    @Override
    public List<BasicCategoryTreeDTO> categoryGradeDown() {
        return baseMapper.categoryGradeDown();
    }

    @Override
    public List<BasicCategoryDTO> getCategoryByPid(String pid) {
        return baseMapper.getCategoryByPid(pid);
    }

    /**
     * list加入父级品类
     */
    private void setParentEntity(String pid, List<BasicCategoryEntity> list) {
        BasicCategoryEntity basicCategoryEntity = this.getById(pid);
        if (ObjectUtils.isNotEmpty(basicCategoryEntity)) {
            list.add(basicCategoryEntity);
            if (!"0".equals(basicCategoryEntity.getPid())) {
                setParentEntity(basicCategoryEntity.getPid(), list);
            }
        }
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
    private List<BasicCategoryDTO> getChildrenList(BasicCategoryDTO item, List<BasicCategoryDTO> allList, List<ProductInfoEntity> productList, List<BasicCategoryTreeDTO> categoryTreeList) {
        List<BasicCategoryDTO> collectList = allList.stream().
                filter(c -> item.getId().equals(c.getPid())).
                map(b -> {
                    List<String> categoryIdList = getChildCategory(categoryTreeList, b.getId());
                    Long productQuantity = productList.stream().filter(p -> categoryIdList.contains(p.getCategoryId())).count();
                    b.setProductQuantity(productQuantity);
                    b.setChildrenList(getChildrenList(b, allList, productList, categoryTreeList));
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
    private void checkCategoryName(String categoryName, String id) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper<BasicCategoryEntity>();
        queryWrapper.eq(BasicCategoryEntity::getName, categoryName);
        queryWrapper.last("LIMIT 1");
        BasicCategoryEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(id)) {
            throw new ServiceException(ApiError.ERROR_95000);
        }
    }

    /**
     * @param code
     * @param pid
     * @param id
     * @description: 分类编码信息验证
     * @author Will
     * @date: 2022/11/22 12:11
     */
    private void checkCategoryCode(String code, String pid, String id) {
        BasicCategoryEntity parent = this.getById(pid);
        //一二级分类必须填写代号
        if (("0".equals(pid) || (ObjectUtils.isNotEmpty(parent) && "0".equals(parent.getPid()))) && StringUtils.isBlank(code)) {
             throw new ServiceException(ApiError.ERROR_95069);
        }
        if ((!"0".equals(pid) && StringUtils.isNotBlank(code)) && (ObjectUtils.isNotEmpty(parent) && !"0".equals(parent.getPid()))) {
            //判断是否是二级分类，非一、二级分类无需添加代号
             throw new ServiceException(ApiError.ERROR_95093);
        }
        //分类必须要填分类代码，并且当前分类级别的分类代码不能重复，只有一二级存在代号
        if (StringUtils.isBlank(code)) {
            return;
        }
        //编码char数组
        char[] chars = code.toCharArray();
        //A-Z字母char数组
        char[] numbers = new char[26];
        for (int i = 0; i < numbers.length; i++){
            numbers[i] = (char)('A' + i);
        }
        Boolean isError = false;
        for (char num : chars) {
            if (!PrimitiveArrayUtil.contains(numbers,num)) {
                isError = true;
            }
        }
        if (isError) {
            throw new ServiceException(ApiError.ERROR_95071);
        }
        BasicCategoryEntity entity = lambdaQuery().eq(BasicCategoryEntity::getCode, code)
                .eq(BasicCategoryEntity::getPid, pid)
                .one();
        if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(id)) {
            throw new ServiceException(ApiError.ERROR_95070);
        }
    }


    /**
     * @param categoryName：类别名称
     * @param isMainCategory：是否是一级主类别
     * @return BasicCategoryEntity
     * @Description 根据类别名称查询类别信息
     * @Author Luo_WG
     * @Date 2022/9/28 18:51
     **/
    @Override
    public BasicCategoryEntity getCategoryByName(String categoryName, Boolean isMainCategory) {
        LambdaQueryWrapper<BasicCategoryEntity> queryWrapper = new LambdaQueryWrapper<BasicCategoryEntity>();
        queryWrapper.eq(BasicCategoryEntity::getName, categoryName);
        if (isMainCategory) {
            queryWrapper.eq(BasicCategoryEntity::getPid, "0");
        } else {
            queryWrapper.ne(BasicCategoryEntity::getPid, "0");
        }
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<BasicCategoryEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCategoryService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    /**
     * 根据分类id 找出父类的一二级名称，以/分隔
     *
     * @param categoryId
     * @return java.lang.String
     * @author jack
     * @date 2025-03-12
     */
    @Override
    public String getParentName(String categoryId) {
        List<String> pidList = getPidList(categoryId);
        if(CollUtil.isEmpty(pidList)){
            return "";
        }
        BasicCategoryEntity basicCategoryEntity = this.getById(pidList.get(0));
        if(Objects.isNull(basicCategoryEntity)){
            return "";
        }
        return basicCategoryEntity.getName();
    }

    @Override
    public List<BasicCategoryDTO.DropdownDTO> getCategoryDropdown() {
        return baseMapper.getCategoryDropdown();
    }
}

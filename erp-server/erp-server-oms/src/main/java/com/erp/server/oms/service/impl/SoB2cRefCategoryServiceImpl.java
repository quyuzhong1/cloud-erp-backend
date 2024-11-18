package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cRefCategoryDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.SoB2cRefCategoryMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.SoB2cRefCategoryService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单分类表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cRefCategoryServiceImpl extends SuperServiceImpl<SoB2cRefCategoryMapper, SoB2cRefCategoryEntity> implements SoB2cRefCategoryService {

    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Lazy
    @Resource
    private SoB2cService soB2cService;

    @Override
    public Boolean add(List<SoB2cRefCategoryDTO.AddDTO> addList, String mainId) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.TRUE;
        }
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = BeanMapperUtils.copyList(SoB2cRefCategoryEntity.class, addList);
        //数据处理
        handleCategory(soB2cRefCategoryList);

        return this.saveBatch(soB2cRefCategoryList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(List<String> categoryIdList, String mainId) {
        List<SoB2cRefCategoryEntity> categoryList = this.listByMainIds(Arrays.asList(mainId));
        //删除原有分类
        this.deleteByMainIds(Arrays.asList(mainId));
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return Boolean.TRUE;
        }
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = new ArrayList<>();
        for (String categoryId : categoryIdList) {
            SoB2cRefCategoryEntity categoryEntity = new SoB2cRefCategoryEntity();
            categoryEntity.setCategoryId(categoryId);
            categoryEntity.setSoB2cId(mainId);
            soB2cRefCategoryList.add(categoryEntity);
        }
        //数据处理
        handleCategory(soB2cRefCategoryList);
        boolean update = this.saveBatch(soB2cRefCategoryList);
        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(mainId);
        String oldValue = categoryList.stream().map(SoB2cRefCategoryEntity::getCategoryName).collect(Collectors.joining(","));
        String newValue = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryName).collect(Collectors.joining(","));
        if (!CharSequenceUtil.equals(oldValue,newValue)) {
            operateLogService.addModuleOperateLog( CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【B2C销售订单表】单据编辑了【分类信息】由[{}]变更为[{}]", UserContext.getDefaultLoginUser().getUserName(), soB2cEntity.getCode(), oldValue, newValue), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "编辑操作");
        }
        return update;
    }

    @Override
    public List<SoB2cRefCategoryEntity> listCategoryIdList(List<String> categoryIdList) {
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoB2cRefCategoryEntity::getCategoryId, categoryIdList).list();
    }

    /**
     * 根据分类id 删除
     *
     * @param categoryIds
     * @return void
     * @author yl
     * @date 2023-11-07 11:51
     */
    @Override
    public void removeByCategoryIds(List<String> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return;
        }
        this.lambdaUpdate().in(SoB2cRefCategoryEntity::getCategoryId, categoryIds).remove();
    }

    @Override
    public void deleteByMainIdAndCategoryId(String mainId, List<String> categoryIdList) {
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return ;
        }
        this.lambdaUpdate().eq(SoB2cRefCategoryEntity::getSoB2cId,mainId).in(SoB2cRefCategoryEntity::getCategoryId,categoryIdList).remove();
    }


    @Override
    public List<SoB2cRefCategoryEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cRefCategoryEntity::getSoB2cId, mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cRefCategoryEntity::getSoB2cId, mainIds).remove();
    }

    /**
     * 数据处理
     */
    private void handleCategory(List<SoB2cRefCategoryEntity> soB2cRefCategoryList) {
        if (CollectionUtils.isEmpty(soB2cRefCategoryList)) {
            return;
        }
        List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return;
        }
        List<OrderCategoryDetailEntity> orderCategoryDetailList = orderCategoryDetailService.listByIds(categoryIdList);
        if (CollectionUtils.isEmpty(orderCategoryDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_REF_CATEGORY_NOT_EXIST);
        }
        for (SoB2cRefCategoryEntity entity : soB2cRefCategoryList) {
            String categoryName = orderCategoryDetailList.stream().filter(obj -> obj.getId().equals(entity.getCategoryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            entity.setCategoryName(categoryName);
        }
    }
}

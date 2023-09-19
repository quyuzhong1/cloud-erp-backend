package com.erp.server.plm.service.impl;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.plm.entity.BasicLabelEntity;
import com.erp.model.plm.entity.BomOperateLogEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.LabelColorEnum;
import com.erp.model.plm.enums.LabelLevelEnum;
import com.erp.model.plm.vo.LabelLevelTreeVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.plm.mapper.BasicLabelMapper;
import com.erp.server.plm.mapper.ProductRefLabelMapper;
import com.erp.server.plm.service.BasicLabelService;
import com.erp.server.plm.service.BomOperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.BasicLabelDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 基础标签表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BasicLabelServiceImpl extends SuperServiceImpl<BasicLabelMapper, BasicLabelEntity> implements BasicLabelService {

    @Autowired
    private CommonService commonService;
    @Resource
    private ProductRefLabelMapper productRefLabelMapper;

    /**
     * @param dto
     * @return List<BasicLabelEntity>
     */
    @Override
    public List<BasicLabelEntity> listByCondition(BasicLabelDTO.SearchDTO dto) {
        //获取通用的全部和自己创建的私有标签
        LambdaQueryWrapper<BasicLabelEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(dto) && StringUtils.isNotBlank(dto.getSearchKeyword())) {
            queryWrapper.like(BasicLabelEntity::getName, dto.getSearchKeyword());
        }
        if (Objects.nonNull(dto) && StringUtils.isNotBlank(dto.getLevel())) {
            queryWrapper.eq(BasicLabelEntity::getLevel, dto.getLevel());
        } else {
            queryWrapper.and(labelEntityLambdaQueryWrapper -> labelEntityLambdaQueryWrapper.eq(BasicLabelEntity::getLevel, "company")
                    .or().eq(BasicLabelEntity::getLevel, "private").eq(BasicLabelEntity::getCreateUserId, commonService.getUserInfo().getUid()));
        }
        queryWrapper.select(BasicLabelEntity::getId, BasicLabelEntity::getName, BasicLabelEntity::getColor, BasicLabelEntity::getLevel);
        return this.list(queryWrapper);
    }

    @Override
    public List<LabelLevelTreeVO> listByTree() {
        List<LabelLevelTreeVO> treeVOS = new ArrayList<>(2);
        //用户当前标签列表
        List<BasicLabelEntity> list = listByCondition(null);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<BasicLabelEntity>> map = list.stream().collect(Collectors.groupingBy(BasicLabelEntity::getLevel));
            treeVOS.add(new LabelLevelTreeVO().setName(LabelLevelEnum.COMPANY.getName())
                    .setLevel(LabelLevelEnum.COMPANY.getType()).setChildren(map.get(LabelLevelEnum.COMPANY.getType())));
            treeVOS.add(new LabelLevelTreeVO().setName(LabelLevelEnum.PRIVATE.getName())
                    .setLevel(LabelLevelEnum.PRIVATE.getType()).setChildren(map.get(LabelLevelEnum.PRIVATE.getType())));
        }
        return treeVOS;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BasicLabelDTO.AddDTO addDTO) {
        BasicLabelEntity basicLabelEntity = new BasicLabelEntity();
        BeanMapperUtils.copy(addDTO, basicLabelEntity);
        // 数据处理
        handleData(basicLabelEntity);
        log.info("开始新增基础标签单");
        boolean save = super.save(basicLabelEntity);
        if (!save) {
            throw new ServiceException("基础标签单保存失败");
        }
        return basicLabelEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<BasicLabelDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.Default);
        }
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_403);
        }
        List<BasicLabelEntity> basicLabelEntities = BeanMapperUtils.copyList(BasicLabelEntity.class, list);
        //校验数据是否存在重复
        Set<String> stringSet = basicLabelEntities.stream().collect(Collectors.groupingBy(BasicLabelEntity::getName, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(stringSet)) {
            throw new ServiceException(ApiError.ERROR_EXIST_BASIC_LABEL_NAME, stringSet);
        }
        //校验标签是否数据库已存在
        Set<String> names = basicLabelEntities.stream().filter(v -> StringUtils.isBlank(v.getId())).map(BasicLabelEntity::getName).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(names)){
            List<Object> nameObjs = getByNames(names);
            if (CollectionUtils.isNotEmpty(nameObjs)) throw new ServiceException(ApiError.ERROR_EXIST_BASIC_LABEL, nameObjs.toArray());
        }

        //补充默认颜色 校验使用范围
        Set<String> labelNames = list.stream().filter(label -> {
            if (StringUtils.isBlank(label.getColor())) label.setColor(LabelColorEnum.GREY.getCode());
            return true;
        }).map(BasicLabelDTO.AddDTO::getLevel).filter(level -> StringUtils.isBlank(LabelLevelEnum.getName(level))).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(labelNames)) {
            //存在不在定义范围内的等级
            throw new ServiceException(ApiError.NOT_EXIST_BASIC_LABEL_LEVEL, labelNames);
        }
        return this.saveOrUpdateBatch(basicLabelEntities);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicLabelDTO.UpdateDTO updateDTO) {
        BasicLabelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "基础标签单"));
        BasicLabelEntity basicLabelEntity = BeanMapperUtils.map(BasicLabelEntity.class, updateDTO);

        // 数据处理
        handleData(basicLabelEntity);
        log.info("编辑 开始修改基础标签单数据，id：【{}】", old.getId());
        boolean save = updateBasicLabel(basicLabelEntity);
        if (!save) {
            throw new ServiceException("基础标签单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void removeBasicLabelById(String id) {
        Optional.ofNullable(this.getById(id)).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "基础标签单"));
        int count = productRefLabelMapper.countByLabelId(id);
        if (count > 0) throw new ServiceException("基础标签存在关联,不能删除");
        super.removeById(id);
    }

    private boolean updateBasicLabel(BasicLabelEntity basicLabelEntity) {
        LambdaUpdateWrapper<BasicLabelEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BasicLabelEntity::getName, basicLabelEntity.getName());
        updateWrapper.set(BasicLabelEntity::getColor, basicLabelEntity.getColor());
        updateWrapper.set(BasicLabelEntity::getLevel, basicLabelEntity.getLevel());
        updateWrapper.set(BasicLabelEntity::getUpdateTime, LocalDateTime.now());
        LoginUser user = commonService.getUserInfo();
        if (Objects.nonNull(user)) {
            updateWrapper.set(BasicLabelEntity::getUpdateUserId, user.getUid());
            updateWrapper.set(BasicLabelEntity::getUpdateUserName, user.getUserName());
        }
        updateWrapper.eq(BasicLabelEntity::getId, basicLabelEntity.getId());
        return this.update(updateWrapper);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(BasicLabelEntity basicLabelEntity) {
        //当前登录人
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        ValidatorUtil.isNotNull(loginUser, ApiError.ERROR_403);
        ValidatorUtil.isNotBlank(LabelLevelEnum.getName(basicLabelEntity.getLevel()), ApiError.NOT_EXIST_BASIC_LABEL_LEVEL, basicLabelEntity.getLevel());
        int count = countByLabelName(basicLabelEntity.getName(), basicLabelEntity.getId());
        if (0 != count) throw new ServiceException(ApiError.ERROR_EXIST_BASIC_LABEL, basicLabelEntity.getName());
        //颜色无值时，默认灰色
        if (StringUtils.isBlank(basicLabelEntity.getColor())) {
            basicLabelEntity.setColor(LabelColorEnum.GREY.getCode());
        }
    }

    /**
     * 根据表name 获取到 BasicLabelEntity信息
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.BasicLabelEntity>
     * @author zdy
     * @date 2023-09-16 08:01
     */
    private List<Object> getByNames(Set<String> names) {
        LambdaQueryWrapper<BasicLabelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BasicLabelEntity::getName, names);
        queryWrapper.orderByDesc(BasicLabelEntity::getCreateTime);
        queryWrapper.select(BasicLabelEntity::getName);
        return this.baseMapper.selectObjs(queryWrapper);
    }

    /**
     * 根据表name 获取到 BasicLabelEntity信息
     *
     * @param name
     * @param id
     * @return int
     * @author zdy
     * @date 2023-09-16 08:01
     */
    private int countByLabelName(String name, String id) {
        LambdaQueryWrapper<BasicLabelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicLabelEntity::getName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(BasicLabelEntity::getId, id);
        }
        return this.count(queryWrapper);
    }
}

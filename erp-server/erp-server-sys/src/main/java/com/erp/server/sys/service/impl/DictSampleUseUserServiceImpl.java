package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.SampleUseUserDTO;
import com.erp.model.sys.entity.DictSampleUseUserEntity;
import com.erp.server.sys.mapper.DictSampleUseUserMapper;
import com.erp.server.sys.service.DictSampleUseUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 示例用户 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2025-01-27
 */
@Service
public class DictSampleUseUserServiceImpl extends SuperServiceImpl<DictSampleUseUserMapper, DictSampleUseUserEntity> implements DictSampleUseUserService {

    /**
     * 保存或者修改示例用户
     *
     * @param userList
     * @return java.lang.Boolean
     * @author Lambda
     * @date 2025-01-27 16:25
     */
    @Override
    public Boolean saveOrUpdateBatchUser(ValidList<SampleUseUserDTO.AddOrUpdateDTO> userList) {
        if (CollectionUtils.isNotEmpty(userList)) {
            List<DictSampleUseUserEntity> addList = BeanMapper.copyList(userList, DictSampleUseUserEntity.class);
            return this.saveOrUpdateBatch(addList);
        }
        return true;
    }

    /**
     * 获取示例用户列表
     *
     * @param
     * @return
     * @author Lambda
     * @date 2025-01-27 16:33
     */
    @Override
    public List<SampleUseUserDTO.ViewDTO> getList() {
        LambdaQueryWrapper<DictSampleUseUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DictSampleUseUserEntity::getId, DictSampleUseUserEntity::getName, DictSampleUseUserEntity::getDisabled);
        List<DictSampleUseUserEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, SampleUseUserDTO.ViewDTO.class);
    }

    @Override
    public List<BaseIdDTO> getByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return BeanMapper.copyList(this.list(), BaseIdDTO.class);
        }
        List<DictSampleUseUserEntity> list = this.listByIds(ids);
        return BeanMapper.copyList(list, BaseIdDTO.class);
    }

    @Override
    public List<SampleUseUserDTO.ViewDTO> getListByCondition(SampleUseUserDTO.QueryDTO queryDTO) {
        LambdaQueryWrapper<DictSampleUseUserEntity> queryWrapper = buildQueryWrapper(queryDTO);
        List<DictSampleUseUserEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, SampleUseUserDTO.ViewDTO.class);
    }

    @Override
    public List<SampleUseUserDTO.ViewDTO> getListByNameList(List<String> nameList) {
        if (CollectionUtils.isEmpty(nameList)) {
            return BeanMapper.copyList(this.list(), SampleUseUserDTO.ViewDTO.class);
        }
        LambdaQueryWrapper<DictSampleUseUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DictSampleUseUserEntity::getName, nameList);
        queryWrapper.select(DictSampleUseUserEntity::getId, DictSampleUseUserEntity::getName, DictSampleUseUserEntity::getDisabled);
        List<DictSampleUseUserEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, SampleUseUserDTO.ViewDTO.class);
    }

    /**
     * 构建查询条件
     * @param queryDTO 查询参数
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<DictSampleUseUserEntity> buildQueryWrapper(SampleUseUserDTO.QueryDTO queryDTO) {
        LambdaQueryWrapper<DictSampleUseUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        // 用户名称模糊查询
        if (StringUtils.isNotBlank(queryDTO.getName())) {
            queryWrapper.like(DictSampleUseUserEntity::getName, queryDTO.getName());
        }
        
        // 排序：按创建时间倒序
        queryWrapper.orderByDesc(DictSampleUseUserEntity::getCreateTime);
        
        return queryWrapper;
    }
}

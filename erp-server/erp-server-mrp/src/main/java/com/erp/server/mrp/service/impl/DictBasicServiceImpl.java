package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.DictBasicDTO;
import com.erp.model.mrp.entity.DictBasicEntity;
import com.erp.server.mrp.mapper.DictBasicMapper;
import com.erp.server.mrp.service.DictBasicService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
    @Autowired
    private OperateLogService operateLogService;



    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictBasicDTO.UpdateDTO updateDTO) {
        DictBasicEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "字典单"));
        DictBasicEntity dictBasicEntity =  BeanMapperUtils.map(DictBasicEntity.class, updateDTO);

        // 数据处理

        log.info("编辑 开始修改字典单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dictBasicEntity);
        if(!save) {
            throw new ServiceException("字典单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录字典单日志数据，单号：【{}】", dictBasicEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictBasicEntity.getCode(), "字典单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictBasicEntity, null, dictBasicEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveOrUpdateDict(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return this.saveOrUpdateBatch(addList);
    }


    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author yl
     * @date 2023-03-17 14:16
     */
    @Override
    public List<DictBasicDTO.ViewDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class);
        return resultList;
    }


    /**
     * 根据key list 获取对应数据
     *
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     * @author yl
     * @date 2023-03-20 14:24
     */
    @Override
    public List<DictBasicEntity> getByKeyList(List<String> keyList) {
        if (CollectionUtils.isEmpty(keyList)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DictBasicEntity::getType, keyList);
        return this.list(queryWrapper);
    }


    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     * @author yl
     * @date 2023-06-28 16:25
     */
    @Override
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, type);
        queryWrapper.eq(DictBasicEntity::getCode, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }


    private List<DictBasicEntity> listByKey(String key) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        queryWrapper.orderByAsc(DictBasicEntity::getIndex);
        return this.list(queryWrapper);

    }
}

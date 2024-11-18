package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgInputConvertMappingDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.server.dmp.mapper.DmpCfgInputConvertMappingMapper;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 转换映射 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-20
 */
@Slf4j
@Service
public class DmpCfgInputConvertMappingServiceImpl extends SuperServiceImpl<DmpCfgInputConvertMappingMapper, DmpCfgInputConvertMappingEntity> implements DmpCfgInputConvertMappingService {

	@Autowired
	private RedisUtil redisUtil;
	
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputConvertMappingDTO.AddDTO addDTO) {
        DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity = new DmpCfgInputConvertMappingEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputConvertMappingEntity);

        // 数据处理
        handleData(dmpCfgInputConvertMappingEntity);

        log.info("开始新增转换映射");
        boolean save = super.save(dmpCfgInputConvertMappingEntity);
        if(!save) {
            throw new ServiceException("转换映射保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "转换映射" , dmpCfgInputConvertMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputConvertMappingEntity.getId(), dmpCfgInputConvertMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputConvertMappingDTO.UpdateDTO updateDTO) {
        DmpCfgInputConvertMappingEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "转换映射"));
        DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity =  BeanMapperUtils.map(DmpCfgInputConvertMappingEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputConvertMappingEntity);
        log.info("编辑 开始修改转换映射数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputConvertMappingEntity);
        if(!save) {
            throw new ServiceException("转换映射保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录转换映射日志数据，id：【{}】", dmpCfgInputConvertMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputConvertMappingEntity.getId(), "转换映射");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputConvertMappingEntity dmpCfgInputConvertMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }

}

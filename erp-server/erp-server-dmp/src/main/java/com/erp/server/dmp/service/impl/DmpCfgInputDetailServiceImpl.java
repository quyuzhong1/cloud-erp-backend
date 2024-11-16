package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.server.dmp.mapper.DmpCfgInputDetailMapper;
import com.erp.server.dmp.service.DmpCfgInputDetailService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 外部系统接口明细 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputDetailServiceImpl extends SuperServiceImpl<DmpCfgInputDetailMapper, DmpCfgInputDetailEntity> implements DmpCfgInputDetailService {
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDetailDTO.AddDTO addDTO) {
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity = new DmpCfgInputDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputDetailEntity);

        // 数据处理
        handleData(dmpCfgInputDetailEntity);

        log.info("开始新增外部系统接口明细");
        boolean save = super.save(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "外部系统接口明细" , dmpCfgInputDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputDetailEntity.getId(), dmpCfgInputDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDetailDTO.UpdateDTO updateDTO) {
        DmpCfgInputDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "外部系统接口明细"));
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity =  BeanMapperUtils.map(DmpCfgInputDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputDetailEntity);
        log.info("编辑 开始修改外部系统接口明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录外部系统接口明细日志数据，id：【{}】", dmpCfgInputDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputDetailEntity.getId(), "外部系统接口明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputDetailEntity dmpCfgInputDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

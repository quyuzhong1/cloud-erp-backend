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
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.server.dmp.mapper.DmpCfgOutputDetailMapper;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 推送数据配置明细 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgOutputDetailServiceImpl extends SuperServiceImpl<DmpCfgOutputDetailMapper, DmpCfgOutputDetailEntity> implements DmpCfgOutputDetailService {
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputDetailDTO.AddDTO addDTO) {
        DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = new DmpCfgOutputDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputDetailEntity);

        // 数据处理
        handleData(dmpCfgOutputDetailEntity);

        log.info("开始新增推送数据配置明细");
        boolean save = super.save(dmpCfgOutputDetailEntity);
        if(!save) {
            throw new ServiceException("推送数据配置明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送数据配置明细" , dmpCfgOutputDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputDetailEntity.getId(), dmpCfgOutputDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputDetailDTO.UpdateDTO updateDTO) {
        DmpCfgOutputDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送数据配置明细"));
        DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity =  BeanMapperUtils.map(DmpCfgOutputDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputDetailEntity);
        log.info("编辑 开始修改推送数据配置明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputDetailEntity);
        if(!save) {
            throw new ServiceException("推送数据配置明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送数据配置明细日志数据，id：【{}】", dmpCfgOutputDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputDetailEntity.getId(), "推送数据配置明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }



}

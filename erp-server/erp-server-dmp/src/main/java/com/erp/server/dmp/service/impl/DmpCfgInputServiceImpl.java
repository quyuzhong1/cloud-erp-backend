package com.erp.server.dmp.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.server.dmp.mapper.DmpCfgInputMapper;
import com.erp.server.dmp.service.DmpCfgInputService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 输入信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputServiceImpl extends SuperServiceImpl<DmpCfgInputMapper, DmpCfgInputEntity> implements DmpCfgInputService {
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDTO.AddDTO addDTO) {
        DmpCfgInputEntity dmpCfgInputEntity = new DmpCfgInputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputEntity);

        // 数据处理
        handleData(dmpCfgInputEntity);

        log.info("开始新增输入信息");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dmpCfgInputEntity.setCode(code);
        boolean save = super.save(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入信息" , dmpCfgInputEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDTO.UpdateDTO updateDTO) {
        DmpCfgInputEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输入信息"));
        DmpCfgInputEntity dmpCfgInputEntity =  BeanMapperUtils.map(DmpCfgInputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputEntity);
        log.info("编辑 开始修改输入信息数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输入信息日志数据，单号：【{}】", dmpCfgInputEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputEntity.getCode(), "输入信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputEntity dmpCfgInputEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<DmpCfgInputDTO.ListDmpCfgInputDTO> listDmpCfgInput(String id) {
        return baseMapper.listDmpCfgInput(id);
    }

    @Override
    public List<String> listBySystemIdAndTaskType(String systemId, List<String> taskTypeList) {
        return baseMapper.listBySystemIdAndTaskType(systemId, taskTypeList);
    }

    @Override
    public List<DmpCfgInputDTO.ListDmpCfgInputDTO> allDmpCfgInput() {
        List<DmpCfgInputEntity> list = lambdaQuery().select(DmpCfgInputEntity::getId, DmpCfgInputEntity::getName)
                .list();
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = BeanMapperUtils.copyList(DmpCfgInputDTO.ListDmpCfgInputDTO.class, list);
        return resultList;
    }
}

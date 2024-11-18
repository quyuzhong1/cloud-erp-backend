package com.erp.server.dmp.service.impl;


import java.util.List;
import java.util.Optional;

import com.erp.model.plm.dto.DictControllerDTO;
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
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.server.dmp.mapper.DmpBasicSystemMapper;
import com.erp.server.dmp.service.DmpBasicSystemService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 外部系统 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpBasicSystemServiceImpl extends SuperServiceImpl<DmpBasicSystemMapper, DmpBasicSystemEntity> implements DmpBasicSystemService {
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpBasicSystemDTO.AddDTO addDTO) {
        DmpBasicSystemEntity dmpBasicSystemEntity = new DmpBasicSystemEntity();
        BeanMapperUtils.copy(addDTO, dmpBasicSystemEntity);

        // 数据处理
        handleData(dmpBasicSystemEntity);

        log.info("开始新增外部系统");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dmpBasicSystemEntity.setCode(code);
        boolean save = super.save(dmpBasicSystemEntity);
        if(!save) {
            throw new ServiceException("外部系统保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "外部系统" , dmpBasicSystemEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类

        return new BaseResultDTO.AddDTO(dmpBasicSystemEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpBasicSystemDTO.UpdateDTO updateDTO) {
        DmpBasicSystemEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "外部系统"));
        DmpBasicSystemEntity dmpBasicSystemEntity =  BeanMapperUtils.map(DmpBasicSystemEntity.class, updateDTO);

        // 数据处理
        handleData(dmpBasicSystemEntity);
        log.info("编辑 开始修改外部系统数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpBasicSystemEntity);
        if(!save) {
            throw new ServiceException("外部系统保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录外部系统日志数据，单号：【{}】", dmpBasicSystemEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpBasicSystemEntity.getCode(), "外部系统");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpBasicSystemEntity dmpBasicSystemEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<DictControllerDTO.DictDropDownDTO> listDmpBasicSystem() {
        return baseMapper.listDmpBasicSystem();
    }

    @Override
    public DmpBasicSystemEntity listByCode(String code) {
        return lambdaQuery().eq(DmpBasicSystemEntity::getCode, code).last("LIMIT 1").one();
    }
}

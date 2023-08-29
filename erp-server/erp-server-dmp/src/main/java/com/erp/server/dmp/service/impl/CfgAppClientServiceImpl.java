package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.server.dmp.mapper.CfgAppClientMapper;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.CommonService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 第三方应用程序信息表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class CfgAppClientServiceImpl extends SuperServiceImpl<CfgAppClientMapper, CfgAppClientEntity> implements CfgAppClientService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgAppClientDTO.AddDTO addDTO) {
        CfgAppClientEntity cfgAppClientEntity = new CfgAppClientEntity();
        BeanMapperUtils.copy(addDTO, cfgAppClientEntity);

        // 数据处理
        handleData(cfgAppClientEntity);

        log.info("开始新增第三方应用程序信息单");
        boolean save = super.save(cfgAppClientEntity);
        if(!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "第三方应用程序信息单" , cfgAppClientEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, cfgAppClientEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return cfgAppClientEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgAppClientDTO.UpdateDTO updateDTO) {
        CfgAppClientEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方应用程序信息单"));
        CfgAppClientEntity cfgAppClientEntity =  BeanMapperUtils.map(CfgAppClientEntity.class, updateDTO);

        // 数据处理
        handleData(cfgAppClientEntity);
        log.info("编辑 开始修改第三方应用程序信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgAppClientEntity);
        if(!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方应用程序信息单日志数据，id：【{}】", cfgAppClientEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgAppClientEntity.getId(), "第三方应用程序信息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, cfgAppClientEntity, null, cfgAppClientEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgAppClientEntity cfgAppClientEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

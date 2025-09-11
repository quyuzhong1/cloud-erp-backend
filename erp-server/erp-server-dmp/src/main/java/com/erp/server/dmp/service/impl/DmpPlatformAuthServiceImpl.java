package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpPlatformAuthDTO;
import com.erp.model.dmp.entity.DmpPlatformAuthEntity;
import com.erp.server.dmp.mapper.DmpPlatformAuthMapper;
import com.erp.server.dmp.service.DmpPlatformAuthService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 平台token授权表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-08-28
 */
@Slf4j
@Service
public class DmpPlatformAuthServiceImpl extends SuperServiceImpl<DmpPlatformAuthMapper, DmpPlatformAuthEntity> implements DmpPlatformAuthService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpPlatformAuthDTO.AddDTO addDTO) {
        DmpPlatformAuthEntity dmpPlatformAuthEntity = new DmpPlatformAuthEntity();
        BeanMapperUtils.copy(addDTO, dmpPlatformAuthEntity);

        // 数据处理
        handleData(dmpPlatformAuthEntity);

        log.info("开始新增平台token授权单");
        boolean save = super.save(dmpPlatformAuthEntity);
        if(!save) {
            throw new ServiceException("平台token授权单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "平台token授权单" , dmpPlatformAuthEntity.getId());

        return new BaseResultDTO.AddDTO(dmpPlatformAuthEntity.getId(), dmpPlatformAuthEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpPlatformAuthDTO.UpdateDTO addOrUpdateDTO) {
        DmpPlatformAuthEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "平台token授权单"));
        DmpPlatformAuthEntity dmpPlatformAuthEntity =  BeanMapperUtils.map(DmpPlatformAuthEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpPlatformAuthEntity);
        log.info("编辑 开始修改平台token授权单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpPlatformAuthEntity);
        if(!save) {
            throw new ServiceException("平台token授权单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录平台token授权单日志数据，id：【{}】", dmpPlatformAuthEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpPlatformAuthEntity.getId(), "平台token授权单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpPlatformAuthEntity dmpPlatformAuthEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

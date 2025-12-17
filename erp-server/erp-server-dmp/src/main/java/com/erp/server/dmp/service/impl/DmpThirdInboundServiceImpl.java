package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.mapper.DmpThirdInboundMapper;
import com.erp.server.dmp.service.DmpThirdInboundService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpThirdInboundDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 第三方仓库存 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
 */
@Slf4j
@Service
public class DmpThirdInboundServiceImpl extends SuperServiceImpl<DmpThirdInboundMapper, DmpThirdInboundEntity> implements DmpThirdInboundService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpThirdInboundDTO.AddDTO addDTO) {
        DmpThirdInboundEntity dmpThirdInboundEntity = new DmpThirdInboundEntity();
        BeanMapperUtils.copy(addDTO, dmpThirdInboundEntity);

        // 数据处理
        handleData(dmpThirdInboundEntity);

        log.info("开始新增第三方仓库存");
        boolean save = super.save(dmpThirdInboundEntity);
        if(!save) {
            throw new ServiceException("第三方仓库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方仓库存" , dmpThirdInboundEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpThirdInboundEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpThirdInboundDTO.UpdateDTO updateDTO) {
        DmpThirdInboundEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方仓库存"));
        DmpThirdInboundEntity dmpThirdInboundEntity =  BeanMapperUtils.map(DmpThirdInboundEntity.class, updateDTO);

        // 数据处理
        handleData(dmpThirdInboundEntity);
        log.info("编辑 开始修改第三方仓库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpThirdInboundEntity);
        if(!save) {
            throw new ServiceException("第三方仓库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方仓库存日志数据，id：【{}】", dmpThirdInboundEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpThirdInboundEntity.getId(), "第三方仓库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpThirdInboundEntity, null, dmpThirdInboundEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpThirdInboundEntity dmpThirdInboundEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.ReplenishmentRefLabelDTO;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;
import com.erp.server.mrp.mapper.ReplenishmentRefLabelMapper;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.ReplenishmentRefLabelService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 补货建议标签关系表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@Service
public class ReplenishmentRefLabelServiceImpl extends SuperServiceImpl<ReplenishmentRefLabelMapper, ReplenishmentRefLabelEntity> implements ReplenishmentRefLabelService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReplenishmentRefLabelDTO.AddDTO addDTO) {
        ReplenishmentRefLabelEntity replenishmentRefLabelEntity = new ReplenishmentRefLabelEntity();
        BeanMapperUtils.copy(addDTO, replenishmentRefLabelEntity);

        // 数据处理
        handleData(replenishmentRefLabelEntity);

        log.info("开始新增补货建议标签关系单");
        boolean save = super.save(replenishmentRefLabelEntity);
        if(!save) {
            throw new ServiceException("补货建议标签关系单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "补货建议标签关系单" , replenishmentRefLabelEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, replenishmentRefLabelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(replenishmentRefLabelEntity.getId(), replenishmentRefLabelEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReplenishmentRefLabelDTO.UpdateDTO updateDTO) {
        ReplenishmentRefLabelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货建议标签关系单"));
        ReplenishmentRefLabelEntity replenishmentRefLabelEntity =  BeanMapperUtils.map(ReplenishmentRefLabelEntity.class, updateDTO);

        // 数据处理
        handleData(replenishmentRefLabelEntity);
        log.info("编辑 开始修改补货建议标签关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(replenishmentRefLabelEntity);
        if(!save) {
            throw new ServiceException("补货建议标签关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录补货建议标签关系单日志数据，id：【{}】", replenishmentRefLabelEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), replenishmentRefLabelEntity.getId(), "补货建议标签关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, replenishmentRefLabelEntity, null, replenishmentRefLabelEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReplenishmentRefLabelEntity replenishmentRefLabelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

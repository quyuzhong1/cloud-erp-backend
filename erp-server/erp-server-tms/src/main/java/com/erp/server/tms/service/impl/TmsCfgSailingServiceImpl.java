package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.erp.server.tms.mapper.TmsCfgSailingMapper;
import com.erp.server.tms.service.TmsCfgSailingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 截单开船配置 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@Service
public class TmsCfgSailingServiceImpl extends SuperServiceImpl<TmsCfgSailingMapper, TmsCfgSailingEntity> implements TmsCfgSailingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsCfgSailingDTO.AddDTO addDTO) {
        TmsCfgSailingEntity tmsCfgSailingEntity = new TmsCfgSailingEntity();
        BeanMapperUtils.copy(addDTO, tmsCfgSailingEntity);

        // 数据处理
        handleData(tmsCfgSailingEntity);

        log.info("开始新增截单开船配置");
        boolean save = super.save(tmsCfgSailingEntity);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "截单开船配置" , tmsCfgSailingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsCfgSailingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsCfgSailingEntity.getId(), tmsCfgSailingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsCfgSailingDTO.UpdateDTO updateDTO) {
        TmsCfgSailingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "截单开船配置"));
        TmsCfgSailingEntity tmsCfgSailingEntity =  BeanMapperUtils.map(TmsCfgSailingEntity.class, updateDTO);

        // 数据处理
        handleData(tmsCfgSailingEntity);
        log.info("编辑 开始修改截单开船配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsCfgSailingEntity);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录截单开船配置日志数据，id：【{}】", tmsCfgSailingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsCfgSailingEntity.getId(), "截单开船配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsCfgSailingEntity, null, tmsCfgSailingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsCfgSailingEntity tmsCfgSailingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

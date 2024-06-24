package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.erp.server.wms.mapper.CfgRuleWaveMapper;
import com.erp.server.wms.service.CfgRuleWaveService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 波次规则 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class CfgRuleWaveServiceImpl extends SuperServiceImpl<CfgRuleWaveMapper, CfgRuleWaveEntity> implements CfgRuleWaveService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO addDTO) {
        CfgRuleWaveEntity cfgRuleWaveEntity = new CfgRuleWaveEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWaveEntity);

        // 数据处理
        handleData(cfgRuleWaveEntity);

        log.info("开始新增波次规则");
        boolean save = super.save(cfgRuleWaveEntity);
        if(!save) {
            throw new ServiceException("波次规则保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "波次规则" , cfgRuleWaveEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleWaveEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleWaveEntity.getId(), cfgRuleWaveEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWaveDTO.UpdateDTO updateDTO) {
        CfgRuleWaveEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "波次规则"));
        CfgRuleWaveEntity cfgRuleWaveEntity =  BeanMapperUtils.map(CfgRuleWaveEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleWaveEntity);
        log.info("编辑 开始修改波次规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWaveEntity);
        if(!save) {
            throw new ServiceException("波次规则保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录波次规则日志数据，id：【{}】", cfgRuleWaveEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWaveEntity.getId(), "波次规则");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWaveEntity, null, cfgRuleWaveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWaveEntity cfgRuleWaveEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

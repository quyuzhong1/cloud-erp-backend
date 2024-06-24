package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.RuleTypeEnum;
import com.erp.server.wms.mapper.CfgRuleWaveMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.erp.server.wms.service.CfgRuleWaveService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private CfgRuleConditionService cfgRuleConditionService;

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

        cfgRuleConditionService.saveRuleCondition(cfgRuleWaveEntity.getId(), addDTO.getConditionList(), RuleTypeEnum.PICKING_STRATEGY.getCode());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "波次规则" , cfgRuleWaveEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), "新增操作");

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
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWaveEntity, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        CfgRuleWaveDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleWaveDTO.ListDTO> pageData = baseMapper.paging(query, params);
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次数据"));

        String disabledName = disabled ? "禁用" : "启用";
        if (disabled.equals(entity.getDisabled())) {
            throw new ServiceException(StrUtil.format("拣货车已【{}】，不支持再次【{}】",disabledName,disabledName));
        }
         lambdaUpdate().eq(CfgRuleWaveEntity::getId,id)
                .set(CfgRuleWaveEntity::getDisabled,disabled)
                .update(new CfgRuleWaveEntity());
        String msg = StrUtil.format("【{}】波次规则【{}】", disabledName,entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), id,StrUtil.format("{}操作",disabledName));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWaveEntity cfgRuleWaveEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     *  分页数据处理
     */
    private void fillList (List<CfgRuleWaveDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //波次类型
        List<DictBasicDTO.ListDTO> waveTypeList = dictBasicService.getByKey(DictBasicEnum.WAVE_TYPE.getKey());
        for (CfgRuleWaveDTO.ListDTO listDTO : list) {
            //波次类型名称
            String waveTypeName = waveTypeList.stream().filter(obj -> StrUtil.equals(obj.getValue(), listDTO.getWaveType())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            listDTO.setWaveTypeName(waveTypeName);
        }
    }
}

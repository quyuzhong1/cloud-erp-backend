package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.RuleOrderApprovalMapper;
import com.erp.server.oms.service.RuleOrderApprovalService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 订单审核规则 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleOrderApprovalServiceImpl extends SuperServiceImpl<RuleOrderApprovalMapper, RuleOrderApprovalEntity> implements RuleOrderApprovalService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleOrderApprovalDTO.AddDTO addDTO) {
        RuleOrderApprovalEntity ruleOrderApprovalEntity = new RuleOrderApprovalEntity();
        BeanMapperUtils.copy(addDTO, ruleOrderApprovalEntity);

        // 数据处理
        handleData(ruleOrderApprovalEntity);

        log.info("开始新增订单审核规则");
        boolean save = super.save(ruleOrderApprovalEntity);
        if (!save) {
            throw new ServiceException("订单审核规则保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "订单审核规则", ruleOrderApprovalEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleOrderApprovalEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleOrderApprovalEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleOrderApprovalDTO.UpdateDTO updateDTO) {
        RuleOrderApprovalEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "订单审核规则"));
        RuleOrderApprovalEntity ruleOrderApprovalEntity = BeanMapperUtils.map(RuleOrderApprovalEntity.class, updateDTO);

        // 数据处理
        handleData(ruleOrderApprovalEntity);
        log.info("编辑 开始修改订单审核规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleOrderApprovalEntity);
        if (!save) {
            throw new ServiceException("订单审核规则保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录订单审核规则日志数据，id：【{}】", ruleOrderApprovalEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleOrderApprovalEntity.getId(), "订单审核规则");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleOrderApprovalEntity, null, ruleOrderApprovalEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<RuleOrderApprovalDTO.PagingViewDTO> paging(PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto) {
        RuleOrderApprovalDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }


    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 14:15
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        RuleOrderApprovalEntity ruleOrderApproval = this.getById(dto.getId());
        if (Objects.isNull(ruleOrderApproval)) {
            throw new ServiceException("订单审核规则不存在");
        }
        Boolean disabled = ruleOrderApproval.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        ruleOrderApproval.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode(), dto.getId(), "状态变更");
        return this.updateById(ruleOrderApproval);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(RuleOrderApprovalEntity ruleOrderApprovalEntity) {
        // TODO 验证数据 & 数据赋值
    }
}

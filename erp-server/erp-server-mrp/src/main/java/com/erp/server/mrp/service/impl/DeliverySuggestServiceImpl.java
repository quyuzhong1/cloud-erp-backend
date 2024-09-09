package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.server.mrp.mapper.DeliverySuggestMapper;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 发货计划 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class DeliverySuggestServiceImpl extends SuperServiceImpl<DeliverySuggestMapper, DeliverySuggestEntity> implements DeliverySuggestService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO addDTO) {
        DeliverySuggestEntity deliverySuggestEntity = new DeliverySuggestEntity();
        BeanMapperUtils.copy(addDTO, deliverySuggestEntity);

        // 数据处理
        handleData(deliverySuggestEntity);

        log.info("开始新增发货计划");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        deliverySuggestEntity.setCode(code);
        boolean save = super.save(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货计划" , deliverySuggestEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliverySuggestEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliverySuggestEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliverySuggestDTO.UpdateDTO updateDTO) {
        DeliverySuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划"));
        DeliverySuggestEntity deliverySuggestEntity =  BeanMapperUtils.map(DeliverySuggestEntity.class, updateDTO);

        // 数据处理
        handleData(deliverySuggestEntity);
        log.info("编辑 开始修改发货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货计划日志数据，单号：【{}】", deliverySuggestEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), deliverySuggestEntity.getCode(), "发货计划");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliverySuggestEntity, null, deliverySuggestEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<DeliverySuggestDTO.ListDTO> list(DeliverySuggestDTO.ListParamDTO params) {
        return baseMapper.list(params);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliverySuggestEntity deliverySuggestEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

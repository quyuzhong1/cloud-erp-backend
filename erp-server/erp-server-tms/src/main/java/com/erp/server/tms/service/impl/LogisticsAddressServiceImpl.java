package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.server.tms.mapper.LogisticsAddressMapper;
import com.erp.server.tms.service.LogisticsAddressService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsAddressDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 物流地址表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsAddressServiceImpl extends SuperServiceImpl<LogisticsAddressMapper, LogisticsAddressEntity> implements LogisticsAddressService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsAddressDTO.AddDTO addDTO) {
        LogisticsAddressEntity logisticsAddressEntity = new LogisticsAddressEntity();
        BeanMapperUtils.copy(addDTO, logisticsAddressEntity);

        // 数据处理
        handleData(logisticsAddressEntity);
        log.info("开始新增物流地址单");
        boolean save = super.save(logisticsAddressEntity);
        if (!save) {
            throw new ServiceException("物流地址单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】", commonService.getUserInfo().getUserName(), "物流地址");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsAddressEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsAddressEntity.getId(), logisticsAddressEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsAddressDTO.UpdateDTO updateDTO) {
        LogisticsAddressEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流地址单"));
        LogisticsAddressEntity logisticsAddressEntity = BeanMapperUtils.map(LogisticsAddressEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsAddressEntity);
        log.info("编辑 开始修改物流地址单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsAddressEntity);
        if (!save) {
            throw new ServiceException("物流地址单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录物流地址单日志数据，id：【{}】", logisticsAddressEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsAddressEntity.getId(), "物流地址单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsAddressEntity, null, logisticsAddressEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public LogisticsAddressDTO.ViewDTO view(String id) {
        LogisticsAddressEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流地址"));
        LogisticsAddressDTO.ViewDTO view = new LogisticsAddressDTO.ViewDTO();
        BeanMapper.copy(entity, view);
        return view;
    }


    /**
     * 地址分页
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<LogisticsAddressDTO.PagingViewDTO> paging(PagingDTO<LogisticsAddressDTO.PagingParamDTO> dto) {
        LogisticsAddressDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData=baseMapper.paging(query, params);


        return null;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsAddressEntity logisticsAddressEntity) {
        // TODO 验证数据 & 数据赋值
    }
}

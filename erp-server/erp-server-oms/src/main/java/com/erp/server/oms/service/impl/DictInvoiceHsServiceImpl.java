package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.DictInvoiceHsDTO;
import com.erp.model.oms.entity.DictInvoiceHsEntity;
import com.erp.server.oms.mapper.DictInvoiceHsMapper;
import com.erp.server.oms.service.DictInvoiceHsService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 发票海关编码 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@Service
public class DictInvoiceHsServiceImpl extends SuperServiceImpl<DictInvoiceHsMapper, DictInvoiceHsEntity> implements DictInvoiceHsService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictInvoiceHsDTO.AddDTO addDTO) {
        DictInvoiceHsEntity dictInvoiceHsEntity = new DictInvoiceHsEntity();
        BeanMapperUtils.copy(addDTO, dictInvoiceHsEntity);

        // 数据处理
        handleData(dictInvoiceHsEntity);

        log.info("开始新增发票海关编码");
        boolean save = super.save(dictInvoiceHsEntity);
        if(!save) {
            throw new ServiceException("发票海关编码保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票海关编码" , dictInvoiceHsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictInvoiceHsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dictInvoiceHsEntity.getId(), dictInvoiceHsEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictInvoiceHsDTO.UpdateDTO addOrUpdateDTO) {
        DictInvoiceHsEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发票海关编码"));
        DictInvoiceHsEntity dictInvoiceHsEntity =  BeanMapperUtils.map(DictInvoiceHsEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dictInvoiceHsEntity);
        log.info("编辑 开始修改发票海关编码数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictInvoiceHsEntity);
        if(!save) {
            throw new ServiceException("发票海关编码保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发票海关编码日志数据，id：【{}】", dictInvoiceHsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictInvoiceHsEntity.getId(), "发票海关编码");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictInvoiceHsEntity, null, dictInvoiceHsEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DictInvoiceHsDTO.ListDTO> paging(PagingDTO<DictInvoiceHsDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DictInvoiceHsDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        return new PagingVO(pageData);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictInvoiceHsEntity dictInvoiceHsEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

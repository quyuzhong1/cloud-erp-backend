package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.erp.model.scm.enums.CfgSupplierSalesDailySalesTypeEnum;
import com.erp.model.scm.enums.CfgSupplierSalesDimensionEnum;
import com.erp.model.scm.enums.CfgSupplierSalesPermissionEnum;
import com.erp.model.scm.enums.CfgSupplierSalesSalesRatioTypeEnum;
import com.erp.server.scm.mapper.CfgSupplierSalesMapper;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.server.scm.service.ModuleOperateLogService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 销量设置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@Service
public class CfgSupplierSalesServiceImpl extends SuperServiceImpl<CfgSupplierSalesMapper, CfgSupplierSalesEntity> implements CfgSupplierSalesService {
    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSupplierSalesDTO.AddDTO addDTO) {
        CfgSupplierSalesEntity cfgSupplierSalesEntity = new CfgSupplierSalesEntity();
        BeanMapperUtils.copy(addDTO, cfgSupplierSalesEntity);

        // 数据处理
        handleData(cfgSupplierSalesEntity);

        log.info("开始新增销量设置");
        boolean save = super.save(cfgSupplierSalesEntity);
        if(!save) {
            throw new ServiceException("销量设置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量设置" , cfgSupplierSalesEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        moduleOperateLogService.addModuleOperateLog(msg, null, cfgSupplierSalesEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgSupplierSalesEntity.getId(), cfgSupplierSalesEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSupplierSalesDTO.UpdateDTO addOrUpdateDTO) {
        CfgSupplierSalesEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量设置"));
        CfgSupplierSalesEntity cfgSupplierSalesEntity =  BeanMapperUtils.map(CfgSupplierSalesEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgSupplierSalesEntity);
        log.info("编辑 开始修改销量设置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgSupplierSalesEntity);
        if(!save) {
            throw new ServiceException("销量设置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量设置日志数据，id：【{}】", cfgSupplierSalesEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgSupplierSalesEntity.getId(), "销量设置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        moduleOperateLogService.addModuleOperateLogByObj(old, cfgSupplierSalesEntity, null, cfgSupplierSalesEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgSupplierSalesEntity cfgSupplierSalesEntity) {
        // TODO 验证数据 & 数据赋值
    }


    @Override
    public PagingVO<CfgSupplierSalesDTO.ListDTO> paging(PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgSupplierSalesDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<CfgSupplierSalesDTO.ListDTO> records) {
        for (CfgSupplierSalesDTO.ListDTO record : records) {
            record.setPermissionName(CfgSupplierSalesPermissionEnum.getName(record.getPermission()));

            record.setDailySalesTypeName(CfgSupplierSalesDailySalesTypeEnum.getName(record.getDailySalesType()));

            record.setSalesRatioTypeName(CfgSupplierSalesSalesRatioTypeEnum.getName(record.getSalesRatioType()));

            record.setNoticeEnabledName(Boolean.TRUE.equals(record.getNoticeEnabled()) ? "是" : "否");

            record.setDimensionName(CfgSupplierSalesDimensionEnum.getName(record.getDimension()));
        }
    }



}

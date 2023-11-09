package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.enums.DictBasicTypeEnum;
import com.erp.model.tms.enums.LogisticsAuthStatusEnums;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnums;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.tms.mapper.LogisticsSupplierMapper;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsSupplierDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 物理商表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsSupplierServiceImpl extends SuperServiceImpl<LogisticsSupplierMapper, LogisticsSupplierEntity> implements LogisticsSupplierService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;


    @Autowired
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsSupplierDTO.AddDTO addDTO) {
        LogisticsSupplierEntity logisticsSupplierEntity = new LogisticsSupplierEntity();
        BeanMapperUtils.copy(addDTO, logisticsSupplierEntity);

        // 数据处理
        handleData(logisticsSupplierEntity);

        boolean save = super.save(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物理商保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物理商单", logisticsSupplierEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsSupplierEntity.getId(), logisticsSupplierEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsSupplierDTO.UpdateDTO updateDTO) {
        LogisticsSupplierEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物理商单"));
        LogisticsSupplierEntity logisticsSupplierEntity = BeanMapperUtils.map(LogisticsSupplierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSupplierEntity);
        log.info("编辑 开始修改物理商单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物理商单保存失败");
        }
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsSupplierEntity.getId(), "物理商单");
        operateLogService.addModuleOperateLogByObj(old, logisticsSupplierEntity, ModuleTypeEnum.LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsSupplierDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<DictBasicDTO.ViewDTO> typeList = dictBasicService.getByKey(DictBasicTypeEnum.LOGISTICS_SUPPLIER.getType());
        List<LogisticsSupplierDTO.TabListDTO> resultList = new ArrayList<>(typeList.size());
        for (DictBasicDTO.ViewDTO item : typeList) {
            LogisticsSupplierDTO.TabListDTO tab = new LogisticsSupplierDTO.TabListDTO();
            String type = item.getCode();
            tab.setType(type);
            tab.setTypeName(item.getName());
            Integer count = list.stream().filter(l -> l.getType().equals(type)).
                    map(LogisticsSupplierDTO.TabListDTO::getCount).findFirst().orElse(0);
            tab.setCount(count);
            resultList.add(tab);

        }
        return resultList;
    }

    @Override
    public PagingVO<LogisticsSupplierDTO.PagingViewDTO> paging(PagingDTO<LogisticsSupplierDTO.PagingParamDTO> dto) {
        LogisticsSupplierDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<LogisticsSupplierDTO.PagingViewDTO> list = pageData.getRecords();
        fillPagingData(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPagingData(List<LogisticsSupplierDTO.PagingViewDTO> list) {
        for (LogisticsSupplierDTO.PagingViewDTO item : list) {
            LogisticsSupplierTypeEnums type = item.getType();
            item.setTypeName(type.getName());
            Boolean disabled = item.getDisabled();
            String disabledName = Objects.isNull(disabled) && !disabled ? "启用" : "禁用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            String authStatusName = LogisticsAuthStatusEnums.getName(authStatus);
            item.setAuthStatusName(authStatusName);

        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsSupplierEntity logisticsSupplierEntity) {
        String logisticsSupplier = "物流供应商";
        String supplierId = logisticsSupplierEntity.getSupplierId();
        SupplierEntity supplier = scmTaskFeign.getSupplierById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException("供应商不存在");
        }
        //供应商分类名
        String supplierCategoryName = supplier.getCategoryName();
        if (!logisticsSupplier.equals(supplierCategoryName)) {
            throw new ServiceException("供应商分类不为物流供应商");
        }
        logisticsSupplierEntity.setSupplierName(supplier.getName());

    }
}

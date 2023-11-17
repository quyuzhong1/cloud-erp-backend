package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.LogisticsWarehouseEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnum;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.tms.mapper.LogisticsSupplierMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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


    @Autowired
    private LogisticsWarehouseService logisticsWarehouseService;

    @Autowired
    private LogisticsChannelService logisticsChannelService;

    @Autowired
    private LogisticsSaleChannelService  logisticsSaleChannelService;

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
            throw new ServiceException("物流商保存失败");
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商单"));
        LogisticsSupplierEntity logisticsSupplierEntity = BeanMapperUtils.map(LogisticsSupplierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSupplierEntity);
        log.info("编辑 开始修改物流商单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物流商单保存失败");
        }
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsSupplierEntity.getId(), "物理商单");
        operateLogService.addModuleOperateLogByObj(old, logisticsSupplierEntity, ModuleTypeEnum.LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsSupplierDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<DictBasicDTO.ViewDTO> typeList = dictBasicService.getByKey(DictBasicEnum.LOGISTICS_SUPPLIER.getType());
        List<LogisticsSupplierDTO.TabListDTO> resultList = new ArrayList<>(typeList.size());
        for (DictBasicDTO.ViewDTO item : typeList) {
            LogisticsSupplierDTO.TabListDTO tab = new LogisticsSupplierDTO.TabListDTO();
            String type = item.getCode();
            tab.setTabFlag(type);
            tab.setTabName(item.getName());
            Integer count = list.stream().filter(l -> l.getTabFlag().equals(type)).
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

    @Override
    public List<LogisticsSupplierDTO.ChannelViewDTO> listChannelView(String id) {
        LogisticsSupplierEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商"));
        List<LogisticsWarehouseEntity> logisticsWarehouseList = logisticsWarehouseService.listByLogisticsSupplierId(id);
        List<String> sourceIdList = new ArrayList<>(10);
        sourceIdList.add(id);
        List<String> logisticsWarehouseIdList = logisticsWarehouseList.stream().map(LogisticsWarehouseEntity::getId).collect(Collectors.toList());
        sourceIdList.addAll(logisticsWarehouseIdList);
        List<LogisticsChannelDTO.BaseDTO> allChannelList = logisticsChannelService.listBaseBySourceIdList(sourceIdList);
        List<LogisticsSupplierDTO.ChannelViewDTO> viewList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(logisticsWarehouseList)) {
            for (LogisticsWarehouseEntity item : logisticsWarehouseList) {
                LogisticsSupplierDTO.ChannelViewDTO channelView = new LogisticsSupplierDTO.ChannelViewDTO();
                channelView.setWarehouseId(item.getWarehouseId());
                channelView.setWarehouseName(item.getWarehouseName());
                List<LogisticsChannelDTO.BaseDTO> channelList = allChannelList.stream().filter(c -> c.getSourceId().equals(item.getId())).collect(Collectors.toList());
                channelView.setChannelList(channelList);
                viewList.add(channelView);
            }
        } else {
            LogisticsSupplierDTO.ChannelViewDTO channelView = new LogisticsSupplierDTO.ChannelViewDTO();
            channelView.setWarehouseId("");
            channelView.setWarehouseName("");
            List<LogisticsChannelDTO.BaseDTO> channelList = allChannelList.stream().filter(c -> c.getSourceId().equals(id)).collect(Collectors.toList());
            channelView.setChannelList(channelList);
            viewList.add(channelView);
        }
        return viewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        LogisticsSupplierEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商"));
        //TODO 检查订单是否引用
        this.removeById(id);
        List<LogisticsWarehouseEntity> logisticsWarehouseList = logisticsWarehouseService.listByLogisticsSupplierId(id);
        List<String> sourceIdList = new ArrayList<>(10);
        sourceIdList.add(id);
        if (CollectionUtils.isNotEmpty(logisticsWarehouseList)) {
            sourceIdList.addAll(logisticsWarehouseList.stream().map(LogisticsWarehouseEntity::getId).collect(Collectors.toList()));
            List<String> LogisticsWarehouseIdList = logisticsWarehouseList.stream().map(LogisticsWarehouseEntity::getId).collect(Collectors.toList());
            logisticsWarehouseService.removeByIds(LogisticsWarehouseIdList);
        }
        //删除渠道根据来源id
        logisticsChannelService.removeBySourceIdList(sourceIdList);
        return BatchResultDTO.success(entity.getId(), entity.getSupplierName(), OperationTypeEnum.DELETE);

    }

    /**
     * 物流商物流渠道同步
     *
     * @return
     * @parms id
     * @author yl
     * @date 2023-11-15
     */
    @Override
    public BatchResultDTO sync(String id) {
        LogisticsSupplierDTO.AuthDTO logisticsSupplier = baseMapper.getLogisticsSupplierAuthById(id);
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商单");
        }
        String authStatus = logisticsSupplier.getAuthStatus();
        String alreadyCode = LogisticsAuthStatusEnum.ALREADY.getCode();
        if (!alreadyCode.equals(authStatus)) {
            throw new ServiceException(ApiError.NOT_SYNC_BY_NOT_AUTH);
        }
        String authId = logisticsSupplier.getAuthId();
        List<LogisticsSaleChannelEntity>  saleChannelEntityList=logisticsSaleChannelService.listByAuthId(authId,Boolean.FALSE);


        return null;
    }

    @Override
    public Boolean export(LogisticsSupplierDTO.ExportDTO dto, HttpServletResponse response) {
        List<LogisticsSupplierDTO.PagingViewDTO> list = baseMapper.listExport(dto);
        fillPagingData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/logisticsSupplier.xlsx";
        String name = "物流商列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("物流商导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPagingData(List<LogisticsSupplierDTO.PagingViewDTO> list) {
        for (LogisticsSupplierDTO.PagingViewDTO item : list) {
            LogisticsSupplierTypeEnum type = item.getType();
            item.setTypeName(type.getName());
            Boolean disabled = item.getDisabled();
            String disabledName = Objects.isNull(disabled) && !disabled ? "启用" : "禁用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            String authStatusName = LogisticsAuthStatusEnum.getName(authStatus);
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

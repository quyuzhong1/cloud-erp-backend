package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.VirtualTransFlowMapper;
import com.erp.server.wms.service.VirtualTransFlowService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟库存交易流水表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualTransFlowServiceImpl extends SuperServiceImpl<VirtualTransFlowMapper, VirtualTransFlowEntity> implements VirtualTransFlowService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean add(VirtualTransFlowDTO.AddDTO addDTO, String virtualTansRuleId,InventoryModeEnum inventoryModeEnum) {
        // 记录交易流水
        VirtualTransFlowEntity virtualTransFlowEntity = new VirtualTransFlowEntity();
        BeanMapperUtils.copy(addDTO,virtualTransFlowEntity);

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        virtualTransFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        virtualTransFlowEntity.setTradeTime(LocalDateTime.now());
        virtualTransFlowEntity.setVirtualTransRuleId(StrUtils.null2EmptyWithTrim(virtualTansRuleId));
        Integer qty = addDTO.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        virtualTransFlowEntity.setQty(qty);
        boolean save = super.save(virtualTransFlowEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存流水数据保存失败"));
        return save;
    }

    @Override
    public Boolean add(VirtualTransFlowEntity param, Integer afterInventoryQty) {
        // 记录交易流水
        LoginUser loginUser = UserContext.getDefaultLoginUser();

        // 复制所有参数
        VirtualTransFlowEntity virtualTransFlow = new VirtualTransFlowEntity();
        BeanMapper.copy(param,virtualTransFlow);
        // 更改指定的参数
        virtualTransFlow.setCurInventoryQty(afterInventoryQty);
        virtualTransFlow.setTradeTime(LocalDateTime.now());
        virtualTransFlow.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "0");

        // 个别参数设置空值
        virtualTransFlow.setId(null);
        boolean save = super.save(virtualTransFlow);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存数据保存失败"));
        return save;
    }

    @Override
    public List<VirtualTransFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<VirtualTransFlowEntity> txnFlows =  lambdaQuery()
                .eq(VirtualTransFlowEntity::getSourceType, sourceType)
                .eq(VirtualTransFlowEntity::getSourceId, sourceId)
                .eq(VirtualTransFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(VirtualTransFlowEntity::getIsUnapproved, Boolean.FALSE)
                .orderByAsc(VirtualTransFlowEntity::getTradeTime)
                .orderByAsc(VirtualTransFlowEntity::getId)
                .list();

        return txnFlows;
    }

    @Override
    public Boolean updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  UserContext.getDefaultLoginUser();
        return baseMapper.updateUnapprovedById(id, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Override
    public PagingVO<VirtualTransFlowDTO.InventoryDetailDTO> detailPaging(PagingDTO<VirtualTransFlowDTO.InventoryDetailParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.InventoryDetailDTO> pageData = this.baseMapper.detailPaging(query, dto.getParams());
        // 填充名称
        fillPageDetailData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualTransFlowDTO.SearchParamDTO dto, HttpServletResponse response) {
        //总数
        Integer count = baseMapper.pagingCount(dto);
        if (count > MathUtil.EXPORT_MAX_COUNT) {
            throw new ServiceException(ApiError.ERROR_EXCEL_EXPORT_SIZE);
        }
        PagingDTO<VirtualTransFlowDTO.SearchParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<VirtualTransFlowDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        List<VirtualTransFlowDTO.ListDTO> list = (List<VirtualTransFlowDTO.ListDTO>)resultList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        fillPageData(list);
        String name = "虚拟库存流水列表信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualTransFlow.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("虚拟库存流水列表信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/6/3 17:10
     * @param list
     */
    private void fillPageData (List<VirtualTransFlowDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);


        //实体仓库
        List<String> warehouseIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //组织信息
        List<String> orgIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);


        for (VirtualTransFlowDTO.ListDTO listDTO : list) {

            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setProductName(productDetailEntity.getName());

            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            listDTO.setWarehouseName(warehouseName);

            //组织名称
            String orgName = accountingCompanyList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);

            //操作状态名称
            listDTO.setOperationModeName(EnumMessage.getNameByCode(InventoryOperationModeEnum.class,listDTO.getOperationMode()));
            //库存状态名称
            listDTO.setDictInventoryStatusName(EnumMessage.getNameByCode(InventoryStatusEnum.class,listDTO.getDictInventoryStatus()));
        }
    }


    /**
     * 虚拟库存分页查询明细数据处理
     * @author will
     * @date 2024/6/3 16:58
     * @param list
     */
    private void fillPageDetailData (List<VirtualTransFlowDTO.InventoryDetailDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //实体仓库
        List<String> warehouseIdList = list.stream().map(VirtualTransFlowDTO.InventoryDetailDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        for (VirtualTransFlowDTO.InventoryDetailDTO inventoryDetailDTO : list) {
            //来源类型名称
            inventoryDetailDTO.setSourceTypeName(SourceTypeEnum.getName(inventoryDetailDTO.getSourceType()));
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), inventoryDetailDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            inventoryDetailDTO.setWarehouseName(warehouseName);

        }
    }
}

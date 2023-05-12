package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.ExportInitStockExcelDTO;
import com.erp.model.wms.dto.excel.ImportInitStockExcelDTO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockDetailEntity;
import com.erp.model.wms.entity.InitStockEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.InitStockDetailExcelListener;
import com.erp.server.wms.mapper.InitStockMapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.InitStockDetailService;
import com.erp.server.wms.service.InitStockService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 期初库存表 服务实现类
 * </p>
 *
 * @author ZHANGCHUNLIN
 * @since 2023-05-10
 */
@Service
public class InitStockServiceImpl extends SuperServiceImpl<InitStockMapper, InitStockEntity> implements InitStockService {

    @Autowired
    private InitStockDetailService initStockDetailService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public PagingVO<InitStockDTO.ListDTO> paging(PagingDTO<InitStockDTO.SearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        // 分页查询数据
        IPage<InitStockDTO.ListDTO> pageData = this.baseMapper.page(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        filling(pageData.getRecords());
        // 明细数据主单字段只有第一条明细数据显示，其他主单数据字段置位空
        Set<String> mainIds = Sets.newHashSet();
        for(InitStockDTO.ListDTO data: pageData.getRecords()) {
            if(mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                data.setOrgName(null);
                data.setWarehouseName(null);
                continue;
            }
            mainIds.add(data.getId());
        }
        return new PagingVO(pageData);
    }

    @Override
    public InitStockDTO.ViewDTO view(String id) {
        // 查询期初库存信息
        InitStockEntity entity = this.getById(id);
        ValidatorUtil.isTrue(Objects.nonNull(entity),()->new ServiceException("未找到期初库存信息"));
        // 查询期初库存明细信息
        List<InitStockDetailEntity> entityMembers = initStockDetailService.findList(id);
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(entityMembers),()->new ServiceException("未找到期初库存明细信息"));

        // 其他字段赋值
        InitStockDTO.ViewDTO viewDTO = BeanMapperUtils.map(InitStockDTO.ViewDTO.class, entity);
        // 仓库
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(viewDTO.getWarehouseId());
        if(Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
            viewDTO.setWarehouseName(warehouseDetail.getName());
        }
        // 仓库组织
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(viewDTO.getOrgId());
        viewDTO.setOrgName(sysAccountingCompanyEntity.getCompanyName());

        List<InitStockDetailDTO.ViewDTO> members = BeanMapperUtils.copyList(InitStockDetailDTO.ViewDTO.class, entityMembers);
        // 获取SKU产品名称
        List<String> skuIds = members.stream().map(InitStockDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productMap = productDetailEntityList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity()));
        members.stream().forEach(member->member.setProductName(productMap.getOrDefault(member.getSkuId(),new ProductDetailEntity()).getName()));
        viewDTO.setDetails(members);

        return viewDTO;
    }

    @Override
    public void exportExcel(InitStockDTO.ExportSearchParamDTO param, HttpServletResponse response) {
        List<InitStockDTO.ListDTO> list = this.baseMapper.exportList(param);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        filling(list);
        List<ExportInitStockExcelDTO> resultList = BeanMapperUtils.copyList(ExportInitStockExcelDTO.class, list);
        String fileName = "期初库存数据";
        try {
            ExcelUtil.export(fileName, "期初库存数据", resultList, ExportInitStockExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public InitStockDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        // 查询所有审核通过的产品信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        InitStockDetailExcelListener listener = new InitStockDetailExcelListener(skuList);
        try {
            EasyExcel.read(excelFile.getInputStream(), ImportInitStockExcelDTO.class, listener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("excel导入错误", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<InitStockDetailDTO.AddDTO> successList = listener.getSuccessList(); // 导入成功数据
        List<ImportInitStockExcelDTO> errorList = listener.getErrorList(); // 导入失败数据
        InitStockDetailDTO.ImportDTO result = new InitStockDetailDTO.ImportDTO();
        result.setSuccessList(successList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "期初库存导入错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ImportInitStockExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    public void filling(List<InitStockDTO.ListDTO> list) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();
        // 获取SKU产品名称
        List<String> skuIds = list.stream().map(InitStockDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOs =  plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOs.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        list.stream().forEach(data->{
            // 单据状态
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // 作废状态
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(),(v)->warehouseService.detailWithCache(v));
            if(Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            // 仓库组织
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getWarehouseId(),(v)->sysUserFeign.getCompanyById(v));
            if(Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                // 产品名称
                data.setProductName(skuVO.getSkuName());
                // spu型号
                data.setSpuNo(skuVO.getSpuNo());
                // 品牌
                data.setBrandName(skuVO.getBrandName());
                // 销售状态
                data.setSaleStatus(skuVO.getSaleState());
                data.setSaleStatusName(SaleStateEnum.getNameByCode(skuVO.getSaleState()));
            }
        });
    }

}

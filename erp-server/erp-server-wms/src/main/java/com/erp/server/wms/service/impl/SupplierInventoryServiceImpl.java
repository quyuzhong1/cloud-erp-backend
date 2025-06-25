package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SupplierInventoryService;
import com.erp.server.wms.service.VirtualInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 即时库存
 * @author will
 * @date 2025/6/19 15:17
 */
@Slf4j
@Service
public class SupplierInventoryServiceImpl  implements SupplierInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualInventoryService virtualInventoryService;



    @Override
    public PagingVO<SupplierInventoryDTO.ListDTO> paging(PagingDTO<SupplierInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        List<SupplierRefUserEntity> list = FeignQuery.create(SupplierRefUserEntity.class)
                .eq(SupplierRefUserEntity::getUid, userInfo.getUid())
                .eq(SupplierRefUserEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(list)) {
            return new PagingVO();
        }
        //根据供应商查询仓库配置
        List<SupplierRefWarehouseEntity> supplierRefWarehouseList = FeignQuery.create(SupplierRefWarehouseEntity.class)
                .eq(SupplierRefWarehouseEntity::getSupplierId, list.get(0).getSupplierId())
                .eq(SupplierRefWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(supplierRefWarehouseList)) {
            return new PagingVO();
        }
        IPage<SupplierInventoryDTO.ListDTO> pageData = virtualInventoryService.supplierInventoryPaging(query,pagingParamDTO.getParams(),supplierRefWarehouseList);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        handelPaging(pageData.getRecords(),list.get(0).getSupplierId());
        return new PagingVO(pageData);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2025/6/20 14:07
     * @param list
     * @param supplierId
     * @return void
     */
    private void handelPaging(List<SupplierInventoryDTO.ListDTO> list,String supplierId) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class, supplierId);
        // 处理分页数据
        for (SupplierInventoryDTO.ListDTO entity : list) {
            entity.setSupplierName(ObjectUtil.isEmpty(supplierEntity) ? "" :supplierEntity.getName());
            entity.setImagesUrlPath(CharSequenceUtil.isBlank(entity.getImagesUrl()) ? "" : FastDFSClientUtil.publicUrl + entity.getImagesUrl());
            entity.setSaleStateName(SaleStateEnum.getNameByCode(entity.getSaleState()));
        }
    }

    @Override
    public Boolean exportExcel(SupplierInventoryDTO.PagingParamDTO params, HttpServletResponse response) {
        PagingDTO<SupplierInventoryDTO.PagingParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(params);
        pagingParamDTO.setPageSize(-1);
        PagingVO<SupplierInventoryDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        List<SupplierInventoryDTO.ListDTO> list = (List<SupplierInventoryDTO.ListDTO>) resultList.getList();
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/supplierInventory.xlsx";
        String name = "即时库存导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

}

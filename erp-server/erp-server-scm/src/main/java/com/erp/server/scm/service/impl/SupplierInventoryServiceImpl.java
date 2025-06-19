package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierInventoryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.mapper.SupplierInventoryMapper;
import com.erp.server.scm.service.SupplierInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_INVENTORY;

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
    private SupplierInventoryMapper supplierInventoryMapper;

    @Override
    public PagingVO<SupplierInventoryDTO.ListDTO> paging(PagingDTO<SupplierInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SupplierInventoryDTO.ListDTO> pageData = supplierInventoryMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }


    @Override
    public Boolean exportExcel(SupplierInventoryDTO.PagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("即时库存", EXPORT_SCM_SUPPLIER_INVENTORY.getCode(), params);
        return Boolean.TRUE;
    }

}

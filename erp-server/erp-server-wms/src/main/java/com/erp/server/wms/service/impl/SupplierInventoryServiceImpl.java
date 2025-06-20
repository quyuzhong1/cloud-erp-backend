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
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.SupplierInventoryMapper;
import com.erp.server.wms.service.SupplierInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        IPage<SupplierInventoryDTO.ListDTO> pageData = supplierInventoryMapper.paging(query,pagingParamDTO.getParams(),supplierRefWarehouseList);
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
        }
    }

    @Override
    public Boolean exportExcel(SupplierInventoryDTO.PagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("即时库存", EXPORT_SCM_SUPPLIER_INVENTORY.getCode(), params);
        return Boolean.TRUE;
    }

}

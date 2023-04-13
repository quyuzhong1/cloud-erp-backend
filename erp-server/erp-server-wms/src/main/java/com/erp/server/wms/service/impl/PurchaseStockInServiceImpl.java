package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.PurchaseStockInService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PurchaseStockInServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStockInEntity> implements PurchaseStockInService {

    @Override
    public PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseStockInDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseStockInDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(PurchaseStockInDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSupplierName(null);
                    obj.setDeliveryWarehouseName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(PurchaseStockInDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseStockInDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    @Override
    public PurchaseStockInDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(String id) {
        return null;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO dto) {
        return null;
    }
}

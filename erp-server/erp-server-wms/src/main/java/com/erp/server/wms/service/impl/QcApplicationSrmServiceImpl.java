package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.dto.QcApplicationSrmDTO;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.QcApplicationMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcApplicationService;
import com.erp.server.wms.service.QcApplicationSrmService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 质检申请单主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationSrmServiceImpl extends SuperServiceImpl<QcApplicationMapper, QcApplicationEntity> implements QcApplicationSrmService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private CommonService commonService;
    @Resource
    private QcApplicationService qcApplicationService;


    @Override
    public PagingVO<QcApplicationSrmDTO.ListDTO> srmPaging(PagingDTO<QcApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcApplicationSrmDTO.ListDTO> pageData = this.baseMapper.srmPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<QcApplicationDTO.TabListDTO> srmTabList(PermissionsDTO param) {
        QcApplicationSrmDTO.TabListParamDTO searchParam = new QcApplicationSrmDTO.TabListParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        if (ObjectUtil.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.SUPPLIER_USER_NOT_REL);
        }
        searchParam.setSupplierId(supplierEntity.getId());

        List<QcApplicationDTO.TabListDTO> list = baseMapper.srmTabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(QcApplicationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new QcApplicationDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
            }
        });
        return list;
    }

    @Override
    public Boolean exportList(QcApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        PagingDTO<QcApplicationDTO.PagingParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<QcApplicationSrmDTO.ListDTO> listDTOPagingVO = this.srmPaging(pagingParamDTO);
        if (CollUtil.isEmpty(listDTOPagingVO.getList())) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/srmQcApplicationExport.xlsx";
        String name = "质检申请单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(listDTOPagingVO.getList(), response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean generateWaitDeliveryRefQcApplication(ValidList<QcApplicationDTO.GeneratePoRefQcApplicationDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<QcApplicationDTO.GeneratePoRefQcApplicationDTO> generateList = list.getList();
        Map<String, List<QcApplicationDTO.GeneratePoRefQcApplicationDTO>> map = generateList.stream().collect(Collectors.groupingBy(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPoId));

        //查询采购订单信息
        List<String> poIdList = generateList.stream().map(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPoId).distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> poList = FeignQuery.getByIds(PurchaseOrderEntity.class, poIdList);

        //查询采购订单明细信息
        List<String> podIdList = generateList.stream().map(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPodId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> podList = FeignQuery.getByIds(PurchaseOrderDetailEntity.class, podIdList);

        //采购供应商信息
        List<PurchaseOrderSupplierEntity> poSupplierList = FeignQuery.create(PurchaseOrderSupplierEntity.class).in(PurchaseOrderSupplierEntity::getPurchaseOrderId, poIdList).list();

        for ( Map.Entry<String, List<QcApplicationDTO.GeneratePoRefQcApplicationDTO>> entry : map.entrySet()) {
            List<QcApplicationDTO.GeneratePoRefQcApplicationDTO> value = entry.getValue();

            //采购订单
            PurchaseOrderEntity purchaseOrderEntity = poList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entry.getKey())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.PO_NOT_FOUND);
            }
            if (!CharSequenceUtil.equals(purchaseOrderEntity.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.PO_APPROVED_ONLY_CAN_PUSH_QC_APPLICATION);
            }
            //采购供应商信息
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = poSupplierList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPurchaseOrderId(), purchaseOrderEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderSupplierEntity)) {
                throw new ServiceException(ApiError.PO_SUPPLIER_INFO_NOT_FOUND);
            }
            QcApplicationDTO.AddDTO addDTO = new QcApplicationDTO.AddDTO();
            addDTO.setSourceId(purchaseOrderEntity.getId());
            addDTO.setSourceCode(purchaseOrderEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.WAIT_DELIVERY.getCode());
            addDTO.setPlanQcDate(value.get(0).getPlanQcDate());
            addDTO.setWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());

            List<QcApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (QcApplicationDTO.GeneratePoRefQcApplicationDTO refDTO : value) {
                //采购订单明细
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = podList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), refDTO.getPodId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                    throw new ServiceException(ApiError.PO_DETAIL_NOT_FOUND);
                }
                QcApplicationDetailDTO.AddDTO detailDTO = new QcApplicationDetailDTO.AddDTO();
                detailDTO.setQty(refDTO.getQty());
                detailDTO.setSourceDetailId(purchaseOrderDetailEntity.getId());
                detailDTO.setSkuId(purchaseOrderDetailEntity.getSkuId());
                detailDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);
            qcApplicationService.add(addDTO);
        }
        return Boolean.TRUE;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<QcApplicationSrmDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.QC_APPLICATION.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.HTTP_UNKNOWN.getCode(), listApiResult.getMsg()));
            }
        }

        // 属性赋值
        for(QcApplicationSrmDTO.ListDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //质检类型名称
            data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));
            //质检状态名称
            data.setQcStatusName(QcTypeEnum.getByCode(data.getQcStatus()));
            //质检结果名称
            data.setQcResultName(QcResultEnum.getByCode(data.getQcResult()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,data.getApproveUserName()));
            }
        }
    }
}

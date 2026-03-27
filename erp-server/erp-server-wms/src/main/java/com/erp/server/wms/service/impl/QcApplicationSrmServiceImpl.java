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
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationSrmDTO;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.QcApplicationMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
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
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private QcApplicationDetailService qcApplicationDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private QcNoticeService qcNoticeService;
    @Resource
    private CommonService commonService;



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
        return null;
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

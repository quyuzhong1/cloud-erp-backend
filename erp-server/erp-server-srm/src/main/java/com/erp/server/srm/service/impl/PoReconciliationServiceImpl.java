package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.srm.mapper.PoReconciliationMapper;
import com.erp.server.srm.query.PoReconciliationQueryHandler;
import com.erp.server.srm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;


/**
 * <p>
 * 采购对账单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationServiceImpl extends SuperServiceImpl<PoReconciliationMapper, PoReconciliationEntity> implements PoReconciliationService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private PoReconciliationDetailService poReconciliationDetailService;

    @Autowired
    private PoReconciliationScmService poReconciliationScmService;

    @Autowired
    private PoReconciliationQueryHandler poReconciliationQueryHandler;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PoReconciliationRefDetailService poReconciliationRefDetailService;
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PoReconciliationDTO.UpdateDTO updateDTO) {
        PoReconciliationEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "采购对账单");
        }
        //添加上传附件url
        addMultipartFileUrl(updateDTO);
        //更新明细
        poReconciliationRefDetailService.srmUpdate(updateDTO.getDetailList(),updateDTO.getId());
        //更新主表对账金额
        poReconciliationScmService.updateAmount(updateDTO.getId());
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        return  poReconciliationScmService.paging(pagingParamDTO);
    }

    @Override
    public List<PoReconciliationDTO.TabListDTO> tabList(PermissionsDTO param) {
        PoReconciliationDTO.PagingParamDTO searchParam = new PoReconciliationDTO.PagingParamDTO();
        PoReconciliationEnum.TabFlagEnum[] values =  PoReconciliationEnum.TabFlagEnum.values();
        List<PoReconciliationDTO.TabListDTO> list = new ArrayList<>();
        for (PoReconciliationEnum.TabFlagEnum item : values) {
            searchParam.setPermissionSql(param.getPermissionSql());
            PoReconciliationDTO.TabListDTO resultDTO = new PoReconciliationDTO.TabListDTO();
            String tabSql = poReconciliationQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParam.setSqlMap(map);
            Integer count = this.baseMapper.tabList(searchParam);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public PoReconciliationDTO.ViewDTO viewMain(String id) {
        return poReconciliationScmService.viewMain(id);
    }


    @Override
    public void exportPoReconciliation(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationScmService.exportPoReconciliation(dto,response);
    }

    @Override
    public Integer countByStatus(String supplierId, String status) {
        return  lambdaQuery().eq(PoReconciliationEntity::getSupplierId, supplierId)
                .eq(PoReconciliationEntity::getStatus, status).count();
    }

    @Override
    public void exportList(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        poReconciliationScmService.fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliation.xlsx";
        String name = "对账单Excel导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }


    @Override
    public BatchResultDTO confirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.FIN_RECONCILIATION_NOT_FOUND);
        }
        //待供方确认
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.PO_RECONCILIATION_ONLY_PENDING_SUPPLIER_CONFIRM_ALLOWED);
        }
        log.info("开始供应商确认，id = {}",id);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, LocalDate.now())
                .set(PoReconciliationEntity::getSupplierConfirmUserId, userInfo.getUid())
                .set(PoReconciliationEntity::getSupplierConfirmUserName, userInfo.getUserName())
                .update();
        log.info("确认 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据确认 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "确认操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public BatchResultDTO cancelConfirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.FIN_RECONCILIATION_NOT_FOUND);
        }
        //待采方确认
        if (!PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.PO_RECONCILIATION_ONLY_PENDING_BUYER_CONFIRM_ALLOWED);
        }
        log.info("开始取消确认，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, null)
                .set(PoReconciliationEntity::getSupplierConfirmUserId, "")
                .set(PoReconciliationEntity::getSupplierConfirmUserName, "")
                .update();
        // 记录操作日志
        log.info("提交 开始记录对账单日志数据，id：【{}】", id);
        String msg =  CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据取消确认 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PO_RECONCILIATION.getCode(), entity.getId(), "取消确认操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_CONFIRM);
    }

    /**
     * @description: 添加文件信息
     * @author Will
     * @date: 2024/1/23 14:33
     * @param updateDTO
     */
    private void addMultipartFileUrl (PoReconciliationDTO.UpdateDTO updateDTO) {
        if (CollectionUtils.isEmpty(updateDTO.getAttachUrlList())) return;
        Class<PoReconciliationEntity> uploadClass = PoReconciliationEntity.class;
        TableName tableName = uploadClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, updateDTO.getId());
    }

    @Override
    public void uploadInvoice(PoReconciliationDTO.UploadFileDTO dto) {
        poReconciliationScmService.uploadInvoice(dto);
    }

    @Override
    public List<BatchResultDTO> downloadInvoice(List<String> ids) {
        return poReconciliationScmService.downloadInvoice(ids);
    }
}

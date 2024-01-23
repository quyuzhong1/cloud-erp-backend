package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.srm.mapper.PoReconciliationMapper;
import com.erp.server.srm.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.PoReconciliationDTO;

import java.time.LocalDate;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

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
    private CommonService commonService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private PoReconciliationDetailService poReconciliationDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO addDTO) {
        PoReconciliationEntity poReconciliationEntity = new PoReconciliationEntity();
        BeanMapperUtils.copy(addDTO, poReconciliationEntity);

        // 数据处理
        handleData(poReconciliationEntity);

        log.info("开始新增采购对账单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        poReconciliationEntity.setCode(code);
        boolean save = super.save(poReconciliationEntity);
        if(!save) {
            throw new ServiceException("采购对账单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "采购对账单" , poReconciliationEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, poReconciliationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(poReconciliationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PoReconciliationDTO.UpdateDTO updateDTO) {
        PoReconciliationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购对账单"));
        //添加上传附件url
        addMultipartFileUrl(updateDTO);
        //更新明细
        poReconciliationDetailService.update(updateDTO.getDetailList(),updateDTO.getId());
        return Boolean.TRUE;
    }

    private void addMultipartFileUrl (PoReconciliationDTO.UpdateDTO updateDTO) {
        if (CollectionUtils.isEmpty(updateDTO.getAttachUrlList()) || CollectionUtils.isEmpty(updateDTO.getAttachUrlList())) {
            return;
        }
        addMultipartFileUrl(updateDTO);
        Class<PoReconciliationEntity> uploadClass = PoReconciliationEntity.class;
        TableName tableName = uploadClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, updateDTO.getId());
    }

    @Override
    public PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PoReconciliationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PoReconciliationDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliation.xlsx";
        String name = "对账单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public BatchResultDTO confirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待供方确认
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_CONFIRM);
        }
        log.info("开始供应商确认，id = {}",id);
        LoginUser userInfo = commonService.getUserInfo();
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, LocalDate.now())
                .set(PoReconciliationEntity::getSupplierConfirmUserId, userInfo.getUid())
                .set(PoReconciliationEntity::getSupplierConfirmUserName, userInfo.getUserName())
                .update(new PoReconciliationEntity());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public BatchResultDTO cancelConfirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待采方确认
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_CANCEL_CONFIRM);
        }
        log.info("开始取消确认，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, null)
                .set(PoReconciliationEntity::getSupplierConfirmUserId, "")
                .set(PoReconciliationEntity::getSupplierConfirmUserName, "")
                .update(new PoReconciliationEntity());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_CONFIRM);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PoReconciliationEntity poReconciliationEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<PoReconciliationDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
    }
}

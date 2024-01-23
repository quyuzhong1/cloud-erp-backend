package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.srm.dto.AttachmentDTO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.server.srm.mapper.PoReconciliationMapper;
import com.erp.server.srm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class PoReconciliationScmServiceImpl extends SuperServiceImpl<PoReconciliationMapper, PoReconciliationEntity> implements PoReconciliationScmService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

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
        //待供方确认/待采方确认
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(old.getStatus())
                || PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_UPDATE);
        }
        //添加上传附件url
        addMultipartFileUrl(updateDTO);
        //更新明细
        poReconciliationDetailScmService.update(updateDTO.getDetailList(),updateDTO.getId());
        return Boolean.TRUE;
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
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_CANCEL_CONFIRM);
        }
        log.info("开始采购方确认，id = {}",id);
        LoginUser userInfo = commonService.getUserInfo();
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode())
                .set(PoReconciliationEntity::getPurchaseConfirmDate, LocalDate.now())
                .set(PoReconciliationEntity::getPurchaseConfirmUserId, userInfo.getUid())
                .set(PoReconciliationEntity::getPurchaseConfirmUserName, userInfo.getUserName())
                .update(new PoReconciliationEntity());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public BatchResultDTO cancelConfirm(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待采方确认/确认已完结
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())
                || PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_SCM_CANCEL_CONFIRM);
        }
        log.info("开始取消确认，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode())
                .set(PoReconciliationEntity::getSupplierConfirmDate, null)
                .set(PoReconciliationEntity::getSupplierConfirmUserId, "")
                .set(PoReconciliationEntity::getSupplierConfirmUserName, "")
                .set(PoReconciliationEntity::getPurchaseConfirmDate,null)
                .set(PoReconciliationEntity::getPurchaseConfirmUserId,"")
                .set(PoReconciliationEntity::getPurchaseConfirmUserName,"")
                .update(new PoReconciliationEntity());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_CONFIRM);
    }

    @Override
    public BatchResultDTO delete(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //待供方确认
        if (PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(entity.getStatus())
            || PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DELETE);
        }
        log.info("开始删除，id = {}",id);
        //删除
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO receive(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //确认待完结
        if (PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_RECEIVE);
        }
        log.info("开始单据签收，id = {}",id);
        lambdaUpdate().eq(PoReconciliationEntity::getId, id)
                .set(PoReconciliationEntity::getStatus, PoReconciliationEnum.PoReconciliationStatusEnum.RECEIVED.getCode())
                .set(PoReconciliationEntity::getReceiveDate, LocalDate.now())
                .update(new PoReconciliationEntity());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.RECEIVE);
    }

    @Override
    public PoReconciliationDTO.ViewDTO viewMain(String id) {
        PoReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        PoReconciliationDTO.ViewDTO viewDTO = BeanMapperUtils.map(PoReconciliationDTO.ViewDTO.class, entity);
        viewDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(viewDTO.getStatus()));

        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.listByBusinessIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<String> attachUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            viewDTO.setAttachUrlList(attachUrlList);
            List<String> attachNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            viewDTO.setAttachNameList(attachNameList);
        }
        return viewDTO;
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
        for (PoReconciliationDTO.ListDTO listDTO : list) {
            //业务状态
            listDTO.setStatusName(PoReconciliationEnum.PoReconciliationStatusEnum.getNameByCode(listDTO.getStatus()));
            //对账周期
            listDTO.setCycle(StrUtil.format("{}-{}",LocalDateTimeUtil.format(listDTO.getStartDate(), DateTimeFormatter.ofPattern("yy.MM.dd")),LocalDateTimeUtil.format(listDTO.getEndDate(), DateTimeFormatter.ofPattern("yy.MM.dd"))));
        }
    }

    /**
     * @description: 添加文件信息
     * @author Will
     * @date: 2024/1/23 14:33
     * @param updateDTO

     */
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
}

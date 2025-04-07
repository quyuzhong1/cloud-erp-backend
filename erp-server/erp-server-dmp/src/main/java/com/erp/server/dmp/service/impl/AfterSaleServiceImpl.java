package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.dmp.enums.AfterSaleStatusEnum;
import com.erp.server.dmp.mapper.AfterSaleMapper;
import com.erp.server.dmp.service.*;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 售后申请表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
@Slf4j
@Service
public class AfterSaleServiceImpl extends SuperServiceImpl<AfterSaleMapper, AfterSaleEntity> implements AfterSaleService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private AttachmentService attachmentService;
    @Resource
    private ThridUserInfoService thridUserInfoService;
    @Resource
    private AfterSaleDetailService afterSaleDetailService;
    @Resource
    private AfterSaleProgressService afterSaleProgressService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private WxMiniAppService wxMiniAppService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSaleDTO.AddDTO addDTO) {
        AfterSaleEntity afterSaleEntity = new AfterSaleEntity();
        BeanMapperUtils.copy(addDTO, afterSaleEntity);

        List<AfterSaleDTO.NodeDTO> nodeList = getNodeList();

        //第三方用户id为空的情况下，则新增用户
        if(StringUtils.isNotBlank(addDTO.getThridUserId())) {
            ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
            thridUserInfoEntity.setUsername(addDTO.getUsername());
            thridUserInfoEntity.setPhoneNumber(addDTO.getPhoneNumber());
            thridUserInfoEntity.setType("selfAdd");
            thridUserInfoService.save(thridUserInfoEntity);
            addDTO.setThridUserId(thridUserInfoEntity.getId());
        }
        log.info("开始新增售后申请单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHSQ);
        afterSaleEntity.setCode(code);
        boolean save = super.save(afterSaleEntity);
        if(!save) {
            throw new ServiceException("售后申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后申请单" , afterSaleEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), "新增操作");
        //新增明细
        List<AfterSaleDetailEntity> detailList = addDTO.getDetailList();
        List<String> skuIds = detailList.stream().map(AfterSaleDetailEntity::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t));
        detailList.forEach(detail -> {
            detail.setMainId(afterSaleEntity.getId());
            ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), new ProductDetailEntity());
            detail.setSkuNo(productDetail.getSkuNo());
            detail.setProdcutName(productDetail.getName());
        });
        afterSaleDetailService.saveBatch(detailList);

        //新增维修记录
        List<AfterSaleProgressEntity> progressList = new ArrayList<>();
        for (AfterSaleDTO.NodeDTO nodeDTO : nodeList) {
            AfterSaleProgressEntity afterSaleProgressEntity = new AfterSaleProgressEntity();
            afterSaleProgressEntity.setMainId(afterSaleEntity.getId());
            afterSaleProgressEntity.setIndex(nodeDTO.getIndex());
            afterSaleProgressEntity.setNode(nodeDTO.getNode());
            if(nodeDTO.getIndex().equals(1)){
                afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
            }
            progressList.add(afterSaleProgressEntity);
        }
        afterSaleProgressService.saveBatch(progressList);

        //保存附件
        List<AfterSaleDTO.AttachmentDTO> attachmentList = addDTO.getAttachmentList();
        for (AfterSaleDTO.AttachmentDTO attachmentDTO : attachmentList) {
            AttachmentEntity entity = new AttachmentEntity();
            entity.setAttachName(attachmentDTO.getAttachName());
            entity.setAttachUrl(attachmentDTO.getAttachUrl());
            entity.setType("after_sale");
            entity.setBusinessId(afterSaleEntity.getId());
            attachmentService.save(entity);
        }

        return new BaseResultDTO.AddDTO(afterSaleEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSaleDTO.UpdateDTO updateDTO) {
        AfterSaleEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "售后申请单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AfterSaleEntity afterSaleEntity =  BeanMapperUtils.map(AfterSaleEntity.class, updateDTO);

        log.info("编辑 开始修改售后申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(afterSaleEntity);
        if(!save) {
            throw new ServiceException("售后申请单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后申请单日志数据，单号：【{}】", afterSaleEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSaleEntity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLogByObj(old, afterSaleEntity, ModuleTypeEnum.AFTER_SALE.getCode(), afterSaleEntity.getId(), msg);

        //新增明细
        List<AfterSaleDetailEntity> detailList = updateDTO.getDetailList();
        List<String> skuIds = detailList.stream().map(AfterSaleDetailEntity::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t));
        detailList.forEach(detail -> {
            detail.setMainId(afterSaleEntity.getId());
            ProductDetailEntity productDetail = productDetailMap.getOrDefault(detail.getSkuId(), new ProductDetailEntity());
            detail.setSkuNo(productDetail.getSkuNo());
            detail.setProdcutName(productDetail.getName());
        });
        // 删除明细数据
        List<String> ids = detailList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        afterSaleDetailService.lambdaUpdate().eq(AfterSaleDetailEntity::getMainId, updateDTO.getId()).notIn(AfterSaleDetailEntity::getId, ids).remove();
        afterSaleDetailService.saveOrUpdateBatch(detailList);

        //保存附件
        List<AfterSaleDTO.AttachmentDTO> attachmentList = updateDTO.getAttachmentList();
        // 删除明细数据
        attachmentService.lambdaUpdate().eq(AttachmentEntity::getType, "after_sale").eq(AttachmentEntity::getBusinessId, updateDTO.getId()).remove();
        for (AfterSaleDTO.AttachmentDTO attachmentDTO : attachmentList) {
            AttachmentEntity entity = new AttachmentEntity();
            entity.setAttachName(attachmentDTO.getAttachName());
            entity.setAttachUrl(attachmentDTO.getAttachUrl());
            entity.setType("after_sale");
            entity.setBusinessId(afterSaleEntity.getId());
            attachmentService.save(entity);
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AfterSaleDTO.ListDTO> paging(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AfterSaleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AfterSaleDTO.TabListDTO> tabList(PermissionsDTO param) {
        AfterSaleDTO.PagingParamDTO searchParam = new AfterSaleDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AfterSaleDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(AfterSaleDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new AfterSaleDTO.TabListDTO(status, 0));
        }
        });
        list.add(new AfterSaleDTO.TabListDTO("all", list.stream().mapToInt(AfterSaleDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AfterSaleDTO.ExportDTO param, HttpServletResponse response) {
        List<AfterSaleDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/afterSale.xlsx";
        String name = "售后申请单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AfterSaleEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"售后申请");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改售后申请单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        log.info("提交 开始启动售后申请单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录售后申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "提交操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AfterSaleDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AfterSaleDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AfterSaleEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AfterSaleEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AfterSaleEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除售后申请单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除售后申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getCode(), "删除售后申请单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改售后申请单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AfterSaleEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"售后申请"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始修改售后申请单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "取消流程操作");

        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AfterSaleEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        return updateForApprove(entity.getId(), approveStatus.getStatus());
    }

    @Override
    public AfterSaleDTO.ViewDTO view(String id) {
        AfterSaleEntity afterSaleEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到售后申请单数据"));
        AfterSaleDTO.ViewDTO data = BeanMapperUtils.map(AfterSaleDTO.ViewDTO.class, afterSaleEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AfterSaleEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.AFTER_SALE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AfterSaleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public Boolean updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return this.lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getApproveUserId, userInfo.getUid())
            .set(AfterSaleEntity::getApproveUserName, userInfo.getUserName())
            .set(AfterSaleEntity::getApproveStatus, approveStatus)
            .set(AfterSaleEntity::getApproveTime, LocalDateTime.now())
            .update(new AfterSaleEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AfterSaleEntity::getId, id)
            .set(AfterSaleEntity::getApproveUserId, "")
            .set(AfterSaleEntity::getApproveUserName, "")
            .set(AfterSaleEntity::getApproveStatus, approveStatus)
            .set(AfterSaleEntity::getApproveTime, null)
            .update(new AfterSaleEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AfterSaleEntity::getId, id)
        .set(AfterSaleEntity::getApproveStatus, approveStatus)
        .update(new AfterSaleEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AfterSaleDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AfterSaleDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //单据状态
            data.setStatusName(AfterSaleStatusEnum.getName(data.getStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AfterSaleEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }


    /**
     * 获取节点配置信息
     */
    private List<AfterSaleDTO.NodeDTO>  getNodeList() {
        String value = cfgSettingService.getValue(SettingEnum.AFTER_SALSE_NODE);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("售后维修节点配置不存在");
        }
        return JSONUtil.toList(value, AfterSaleDTO.NodeDTO.class);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AfterSaleEntity afterSaleEntity) {



    }


//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public BatchResultDTO changeStatus(String id) {
//        AfterSaleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后申请单数据"));
//        // 待提交或审核不通过并且未作废允许作废
//        if ((!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())  || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
//            throw new ServiceException("只有审核通过的单据才能进行状态变更");
//        }
//        Integer count = afterSaleProgressService.lambdaQuery().eq(AfterSaleProgressEntity::getMainId, id).in(AfterSaleProgressEntity::getNode,
//                AfterSaleStatusEnum.FINISHED.getCode(), AfterSaleStatusEnum.TERMINATED.getCode()).count();
//        if(count > 0){
//            throw new ServiceException("未完成未终止的单据才能进行状态变更");
//        }
//
//        List<AfterSaleProgressEntity> progressList = afterSaleProgressService.getByMainIds(Collections.singletonList(id));
//        List<AfterSaleProgressEntity> oldList = progressList.stream().filter(t -> Objects.nonNull(t.getNodeTime())).collect(Collectors.toList());
//        oldList.sort(Comparator.comparingInt(AfterSaleProgressEntity::getIndex).reversed());
//        String oldNode = oldList.get(0).getNode();
//
//        List<AfterSaleProgressEntity> newList = progressList.stream().filter(t -> Objects.isNull(t.getNodeTime())).collect(Collectors.toList());
//        // 根据 index 进行排序
//        newList.sort(Comparator.comparingInt(AfterSaleProgressEntity::getIndex));
//        // 变更状态
//        AfterSaleProgressEntity afterSaleProgressEntity = newList.get(0);
//        afterSaleProgressEntity.setNodeTime(LocalDateTime.now());
//        afterSaleProgressService.updateById(afterSaleProgressEntity);
//        String newNode = afterSaleProgressEntity.getNode();
//
//        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据状态变更操作：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后申请单","从"+oldNode+"变更到"+newNode);
//        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE.getCode(), entity.getId(), "状态变更操作");
//        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
//    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> changeStatus(AfterSaleDTO.IdsDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>();
        List<AfterSaleEntity> entityList = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(entityList)) {
            throw new ServiceException("未找到售后申请单数据");
        }
        for (AfterSaleEntity entity : entityList) {
            BatchResultDTO batchResultDTO;
            // 待提交或审核不通过并且未作废允许作废
            if ((!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus()) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus()))){
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有审核通过未作废的单据才能进行状态变更");
            }else if(AfterSaleStatusEnum.TO_BE_SHIPPED.getCode().equals(entity.getStatus())){
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有未完成的单据才能进行状态变更");
            }else {
                batchResultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
                //更新状态
                entity.setStatus(dto.getNode());
                updateById(entity);

                afterSaleProgressService.updateStatus(entity.getId(), dto.getNode(),dto.getRemark());

            }
            resultList.add(batchResultDTO);
        }
        return resultList;
    }




    /**
     * 寄修进度
     */
    @Override
    public List<AfterSaleProgressDTO.RepairRecordListDTO> getRepairProgress(AfterSaleDTO.ProgressDTO dto) {
        return Collections.emptyList();
    }
    /**
     * 寄修历史
     */
    @Override
    public List<AfterSaleProgressDTO.RepairHistoryListDTO> getRepairHistory(AfterSaleDTO.ProgressDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public String getAccessToken() {
        return wxMiniAppService.getAccessToken();
    }

    @Override
    public WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode){
        return wxMiniAppService.jsCode2SessionInfo(jsCode);
    }


}

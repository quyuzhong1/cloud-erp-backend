package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.RequisitionTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.OverseasDeliveryPlanConverter;
import com.erp.server.wms.mapper.OverseasDeliveryPlanMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货计划 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasDeliveryPlanServiceImpl extends SuperServiceImpl<OverseasDeliveryPlanMapper, OverseasDeliveryPlanEntity> implements OverseasDeliveryPlanService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private OverseasDeliveryPlanDetailService overseasDeliveryPlanDetailService;
    @Autowired
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private FbaDeliveryService fbaDeliveryService;
    @Autowired
    private RequisitionApplicationService requisitionApplicationService;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasDeliveryPlanDTO.AddDTO addDTO) {
        OverseasDeliveryPlanEntity overseasDeliveryPlanEntity = new OverseasDeliveryPlanEntity();
        BeanMapperUtils.copy(addDTO, overseasDeliveryPlanEntity);

        // 数据处理
        handleData(overseasDeliveryPlanEntity);

        log.info("开始新增发货计划");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHJH);
        overseasDeliveryPlanEntity.setCode(code);
        boolean save = super.save(overseasDeliveryPlanEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "发货计划" , overseasDeliveryPlanEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), overseasDeliveryPlanEntity.getId(), "新增操作");
        // 新增明细
        overseasDeliveryPlanDetailService.add(addDTO, overseasDeliveryPlanEntity.getId());
        return new BaseResultDTO.AddDTO(overseasDeliveryPlanEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasDeliveryPlanDTO.UpdateDTO updateDTO) {
        OverseasDeliveryPlanEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        OverseasDeliveryPlanEntity overseasDeliveryPlanEntity =  BeanMapperUtils.map(OverseasDeliveryPlanEntity.class, updateDTO);

        // 数据处理
        handleData(overseasDeliveryPlanEntity);
        log.info("编辑 开始修改发货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(overseasDeliveryPlanEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        // 修改明细数据
        overseasDeliveryPlanDetailService.update(updateDTO, overseasDeliveryPlanEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录发货计划日志数据，单号：【{}】", overseasDeliveryPlanEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasDeliveryPlanEntity.getCode(), "发货计划");
        operateLogService.addModuleOperateLogByObj(old, overseasDeliveryPlanEntity, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), overseasDeliveryPlanEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<OverseasDeliveryPlanDTO.ListDTO> paging(PagingDTO<OverseasDeliveryPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<OverseasDeliveryPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<OverseasDeliveryPlanDTO.TabListDTO> tabList(PermissionsDTO param) {
        OverseasDeliveryPlanDTO.PagingParamDTO searchParam = new OverseasDeliveryPlanDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<OverseasDeliveryPlanDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(OverseasDeliveryPlanDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new OverseasDeliveryPlanDTO.TabListDTO(status, 0));
        }
        });
        list.add(new OverseasDeliveryPlanDTO.TabListDTO("all", list.stream().mapToInt(OverseasDeliveryPlanDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(OverseasDeliveryPlanDTO.ExportDTO param, HttpServletResponse response) {
        List<OverseasDeliveryPlanDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/overseasDeliveryPlan.xlsx";
        String name = "发货计划导出";
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
        OverseasDeliveryPlanEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到发货计划数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改发货计划状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动发货计划流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录发货计划日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(OverseasDeliveryPlanDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(OverseasDeliveryPlanDTO.UpdateDTO dto) {
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
        OverseasDeliveryPlanEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(OverseasDeliveryPlanEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());
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
        OverseasDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 检查是否有下推单据
        List<FbaDeliveryEntity> deliveryEntities = fbaDeliveryService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(deliveryEntities)) {
            throw new ServiceException(ApiError.EXIST_FBA_DELIVERY_DETAIL_NOT_DISAPPROVE);
        }
        List<RequisitionApplicationEntity> requisitionApplicationEntities = requisitionApplicationService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(requisitionApplicationEntities)) {
            throw new ServiceException(ApiError.EXIST_REQUISITION_APPLICATION_NOT_DISAPPROVE);
        }

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(OverseasDeliveryPlanEntity entity) {
        // 已审核支持反审核
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // 下游单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        OverseasDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除明细数据
        overseasDeliveryPlanDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除发货计划主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货计划日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getCode(), "删除发货计划数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        OverseasDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改发货计划状态数据，id：【{}】", id);
        lambdaUpdate().eq(OverseasDeliveryPlanEntity::getId, id)
            .set(OverseasDeliveryPlanEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(OverseasDeliveryPlanEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        OverseasDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划数据"));
        // 只有审核中的单据允许撤销
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货计划状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, OverseasDeliveryPlanEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public OverseasDeliveryPlanDTO.ViewDTO view(String id) {
        OverseasDeliveryPlanEntity overseasDeliveryPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货计划数据"));
        OverseasDeliveryPlanDTO.ViewDTO data = BeanMapperUtils.map(OverseasDeliveryPlanDTO.ViewDTO.class, overseasDeliveryPlanEntity);

        //发货计划详情
        List<OverseasDeliveryPlanDetailEntity> detailEntityList = overseasDeliveryPlanDetailService.listByMainIds(Arrays.asList(id));

        // 数据填充处理
        fillOne(data, detailEntityList);
        return data;
    }

    /**
    * 启动流程
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(OverseasDeliveryPlanEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(OverseasDeliveryPlanDTO.ViewDTO data, List<OverseasDeliveryPlanDetailEntity> detailEntityList) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //获取sku信息
        List<String> skuIdList = detailEntityList.stream().map(OverseasDeliveryPlanDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取库存sku信息
        List<String> skuNoList = detailEntityList.stream().map(OverseasDeliveryPlanDetailEntity::getSkuNo).collect(Collectors.toList());
        List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNoViews = omsListingInfoFeign.listStockSkuNoByProductSkuNo(skuNoList);

        //设置状态中文名称
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

        //明细信息
        List<OverseasDeliveryPlanDetailDTO.ViewDTO> viewDTOS = BeanMapper.copyList(detailEntityList, OverseasDeliveryPlanDetailDTO.ViewDTO.class);
        for (OverseasDeliveryPlanDetailDTO.ViewDTO viewDTO : viewDTOS) {

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setProductName(skuVO.getSkuNo());
                viewDTO.setImageUrl(skuVO.getSkuImagesUrl());
            }

            //获取库存sku
            SkuMappingDTO.listStockSkuNoByProductSkuNoView listStockSkuNoByProductSkuNoView = listStockSkuNoByProductSkuNoViews.stream().filter(req -> req.getProductSkuId().equals(viewDTO.getSkuId())).distinct().findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(listStockSkuNoByProductSkuNoView)) {
                viewDTO.setStockSku(listStockSkuNoByProductSkuNoView.getWarehouseSkuNo());
                viewDTO.setStockSkuName(listStockSkuNoByProductSkuNoView.getWarehouseProductName());
            }

        }
        data.setDetailList(viewDTOS);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().eq(OverseasDeliveryPlanEntity::getId, id)
            .set(OverseasDeliveryPlanEntity::getApproveUserId, userInfo.getUid())
            .set(OverseasDeliveryPlanEntity::getApproveUserName, userInfo.getUserName())
            .set(OverseasDeliveryPlanEntity::getApproveStatus, approveStatus)
            .set(OverseasDeliveryPlanEntity::getApproveTime, LocalDateTime.now())
            .update(new OverseasDeliveryPlanEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(OverseasDeliveryPlanEntity::getId, id)
            .set(OverseasDeliveryPlanEntity::getApproveUserId, "")
            .set(OverseasDeliveryPlanEntity::getApproveUserName, "")
            .set(OverseasDeliveryPlanEntity::getApproveStatus, approveStatus)
            .set(OverseasDeliveryPlanEntity::getApproveTime, null)
            .update(new OverseasDeliveryPlanEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(OverseasDeliveryPlanEntity::getId, id)
        .set(OverseasDeliveryPlanEntity::getApproveStatus, approveStatus)
        .update(new OverseasDeliveryPlanEntity());
    }

    @Override
    public List<FbaDeliveryDTO.DeliverRecordView> listDeliverRecord(String id) {
        List<FbaDeliveryDTO.DeliverRecordView> deliverRecordViews = fbaDeliveryService.listDeliveryRecordBySourceIds(Arrays.asList(id));
        return deliverRecordViews;
    }

    @Override
    public List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> ids) {
        List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list = baseMapper.generateRequisitionApplicationView(ids);
        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        for (OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO viewDTO : list) {
            //海外发货计划下推要货单要货类型默认是：海外仓
            viewDTO.setType(RequisitionTypeEnum.OVERSEAS_WAREHOUSE.getCode());
            viewDTO.setTypeName(RequisitionTypeEnum.OVERSEAS_WAREHOUSE.getName());

            //来源类型
            viewDTO.setSourceType(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
                viewDTO.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }
        }
        return list;
    }

    @Override
    public Boolean generateRequisitionApplicationSave(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list) {
        return generateRequisitionApplication(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateRequisitionApplicationSaveAndSubmit(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list) {
        return generateRequisitionApplication(list, Boolean.TRUE);
    }

    private Boolean generateRequisitionApplication(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list, Boolean isSubmit) {
        //一个发货计划单，生成一个要货申请单
        Map<String, List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> map = list.stream().collect(Collectors.groupingBy(OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO::getSourceId));

        //根据仓库id查询仓库信息
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getRequisitionWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(requisitionWarehouseIds);

        for (Map.Entry<String, List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> entry : map.entrySet()) {
            List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> value = entry.getValue();
            //映射主表信息
            RequisitionApplicationDTO.AddDTO addDTO = OverseasDeliveryPlanConverter.INSTANCE.DeliveryPlanGRA(value.get(MathUtil.ZERO));

            //映射详情信息
            List<RequisitionApplicationDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO viewDTO : value) {
                //要货仓库中文
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(viewDTO.getRequisitionWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                addDTO.setRequisitionWarehouseName(updateDTO.getName());

                RequisitionApplicationDetailDTO.AddDTO detailAddDto = OverseasDeliveryPlanConverter.INSTANCE.DeliveryPlanDetailGRA(viewDTO);

                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);

            BaseResultDTO.AddDTO add = requisitionApplicationService.add(addDTO);
            if (isSubmit) {
                requisitionApplicationService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids) {
        List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list = baseMapper.generateDeliverView(ids);
        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        for (OverseasDeliveryPlanDTO.GenerateDeliverViewDTO viewDTO : list) {

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }
        }
        return list;
    }

    @Override
    public Boolean generateDeliverSave(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateDeliverSaveAndSubmit(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.TRUE);
    }

    private Boolean generateDeliver(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list, Boolean isSubmit) {
        //一个发货计划单，生成一个要发货单
        Map<String, List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>> map = list.stream().collect(Collectors.groupingBy(OverseasDeliveryPlanDTO.GenerateDeliverViewDTO::getSourceId));
        List<String> ids = new ArrayList<>();

        //查询仓库信息
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getDeliveryWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(requisitionWarehouseIds);

        //查询子件信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //获取sku信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        for (Map.Entry<String, List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>> entry : map.entrySet()) {
            List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> value = entry.getValue();
            //映射主表信息
            FbaDeliveryDTO.AddDTO addDTO = OverseasDeliveryPlanConverter.INSTANCE.generateDeliverFDD(value.get(MathUtil.ZERO));

            //备货类型
            addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode());
            //来源类型
            addDTO.setSourceType(SourceTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode());

            //映射详情信息
            List<FbaDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (OverseasDeliveryPlanDTO.GenerateDeliverViewDTO viewDTO : value) {
                //发货仓库中文
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(viewDTO.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                addDTO.setDeliveryWarehouseName(updateDTO.getName());
                addDTO.setInventoryOrgId(updateDTO.getOrgId());

                FbaDeliveryDetailDTO.AddDTO detailAddDto = OverseasDeliveryPlanConverter.INSTANCE.generateDeliverDetailFDD(viewDTO);

                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    detailAddDto.setIsCombination(Boolean.TRUE);
                } else {
                    detailAddDto.setIsCombination(Boolean.FALSE);
                }

                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(viewDTO.getSkuId())).distinct().findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(skuVO)) {
                    detailAddDto.setSkuNo(skuVO.getSkuNo());
                    detailAddDto.setNetWeight(skuVO.getNetWeight());
                    //拆分产品尺寸
                    String productSize = skuVO.getProductSize();
                    splitProductSize(detailAddDto, productSize);
                }

                //暂无仓位
                detailAddDto.setWarehouseLocation("");


                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);

            BaseResultDTO.AddDTO add = fbaDeliveryService.add(addDTO);
            if (isSubmit) {
                fbaDeliveryService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 拆分产品尺寸长宽高存入数据集
     *
     * @param detailAdd   数据集
     * @param productSize 需要拆分的尺寸
     * @return void
     * @Author Luo_WG
     * @Date 2023/11/2 17:28
     **/
    private void splitProductSize(FbaDeliveryDetailDTO.AddDTO detailAdd, String productSize) {
        if (StringUtils.isNotBlank(productSize)) {
            String[] productSizes = productSize.split("X");
            //长
            if (productSizes.length > 0) {
                if (StringUtils.isNotBlank(productSizes[0])) {
                    detailAdd.setProductSizeLength(new BigDecimal(productSizes[0]));
                } else {
                    detailAdd.setProductSizeLength(new BigDecimal(BigInteger.ZERO));
                }
            }
            //宽
            if (productSizes.length > 1) {
                if (StringUtils.isNotBlank(productSizes[1])) {
                    detailAdd.setProductSizeWidth(new BigDecimal(productSizes[1]));
                } else {
                    detailAdd.setProductSizeWidth(new BigDecimal(BigInteger.ZERO));
                }
            }
            //高
            if (productSizes.length > 2) {
                if (StringUtils.isNotBlank(productSizes[2])) {
                    detailAdd.setProductSizeHeight(new BigDecimal(productSizes[2]));
                } else {
                    detailAdd.setProductSizeHeight(new BigDecimal(BigInteger.ZERO));
                }
            }
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasDeliveryPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        //根据单据id查询审核流程
        List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(ids);

        // 属性赋值
        for(OverseasDeliveryPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDeliveryStatusName(DeliveryStatusEnum.getName(data.getDeliveryStatus()));

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(data.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                data.setIsCombination(Boolean.TRUE);
            } else {
                data.setIsCombination(Boolean.FALSE);
            }
            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(data.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                data.setSkuNo(skuVO.getSkuNo());
                data.setProductName(skuVO.getSkuName());
            }
            //待审核人
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(data.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String waitApproveUserName = StringUtils.join(curApproveName, ",");
            data.setWaitApproveUserName(waitApproveUserName);
        }
    }
    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(OverseasDeliveryPlanEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasDeliveryPlanEntity overseasDeliveryPlanEntity) {
        //根据仓库id查询和第三方仓绑定关系，并设置国家字段值
        OverseasProviderWarehouseEntity warehouseEntity = overseasProviderWarehouseService.getByWarehouseId(overseasDeliveryPlanEntity.getToWarehouseId());
        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
            overseasDeliveryPlanEntity.setCountry(warehouseEntity.getCountry());
            overseasDeliveryPlanEntity.setCountryName(warehouseEntity.getCountryName());
        }

        //设置仓库中文名
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(overseasDeliveryPlanEntity.getToWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(overseasDeliveryPlanEntity.getToWarehouseId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(updateDTO)) {
            overseasDeliveryPlanEntity.setToWarehouseName(updateDTO.getName());
        }

    }

}

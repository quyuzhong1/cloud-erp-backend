package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleAdjustmentInfoEntity;
import com.erp.server.wms.mapper.SampleAdjustmentInfoMapper;
import com.erp.server.wms.service.SampleAdjustmentInfoService;
import com.erp.server.wms.service.SampleAdjustmentDetailService;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SampleAdjustmentDetailEntity;
import com.erp.model.wms.dto.SampleAdjustmentDetailDTO;
import com.erp.model.wms.enums.SampleAdjustmentTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.wms.service.WmsAttachmentService;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.commons.collections4.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.math3.util.Pair;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleAdjustmentInfoDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品调整单 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
@Slf4j
@Service
public class SampleAdjustmentInfoServiceImpl extends SuperServiceImpl<SampleAdjustmentInfoMapper, SampleAdjustmentInfoEntity> implements SampleAdjustmentInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SampleAdjustmentDetailService sampleAdjustmentDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private WmsAttachmentService attachmentService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleAdjustmentInfoDTO.AddDTO addDTO) {
        SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity = new SampleAdjustmentInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleAdjustmentInfoEntity);

        // 数据处理
        handleData(sampleAdjustmentInfoEntity);

        log.info("开始新增样品调整单");

        // 校验明细不能为空
        if (CollUtil.isEmpty(addDTO.getDetailList())) {
            throw new ServiceException("样品调整单明细不能为空");
        }

        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPTZ);
        sampleAdjustmentInfoEntity.setCode(code);
        boolean save = super.save(sampleAdjustmentInfoEntity);
        if(!save) {
            throw new ServiceException("样品调整单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品调整单" , sampleAdjustmentInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), "新增操作");
        
        // 处理明细数据
        List<SampleAdjustmentDetailDTO.AddDTO> detailList = addDTO.getDetailList();

        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(SampleAdjustmentDetailDTO.AddDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(com.common.core.enums.ApiError.ERROR_REPEAT_SKU);
        }

        List<SampleAdjustmentDetailEntity> sampleAdjustmentDetailEntities = BeanMapperUtils.copyList(SampleAdjustmentDetailEntity.class, detailList);
        List<String> skuIds = sampleAdjustmentDetailEntities.stream().map(SampleAdjustmentDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        
        for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : sampleAdjustmentDetailEntities) {
            sampleAdjustmentDetailEntity.setMainId(sampleAdjustmentInfoEntity.getId());

            // 计算差异数量 = 实际数量 - 台账数量
            Integer differenceQty = sampleAdjustmentDetailEntity.getActualQty() - sampleAdjustmentDetailEntity.getLedgerQty();
            sampleAdjustmentDetailEntity.setDifferenceQty(differenceQty);

            SkuVO skuVO = skuMap.getOrDefault(sampleAdjustmentDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleAdjustmentDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleAdjustmentDetailEntity.setProductName(skuVO.getSkuName());
            }
        }

        sampleAdjustmentDetailService.saveBatch(sampleAdjustmentDetailEntities);
        
        //附件
        addAttachment(addDTO, sampleAdjustmentInfoEntity);

        return new BaseResultDTO.AddDTO(sampleAdjustmentInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleAdjustmentInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品调整单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 校验明细不能为空
        if (CollUtil.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("样品调整单明细不能为空");
        }
        SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity =  BeanMapperUtils.map(SampleAdjustmentInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleAdjustmentInfoEntity);
        log.info("编辑 开始修改样品调整单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleAdjustmentInfoEntity);
        if(!save) {
            throw new ServiceException("样品调整单保存失败");
        }
        
        // 记录主单操作日志
        log.info("编辑 开始记录样品调整单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "样品调整单");
        operateLogService.addModuleOperateLogByObj(old, sampleAdjustmentInfoEntity, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), msg);

        //明细
        updateDetail(addOrUpdateDTO, sampleAdjustmentInfoEntity);

        //附件
        updateAttachment(addOrUpdateDTO, old);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SampleAdjustmentInfoDTO.ListDTO> paging(PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleAdjustmentInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleAdjustmentInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleAdjustmentInfoDTO.PagingParamDTO searchParam = new SampleAdjustmentInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleAdjustmentInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleAdjustmentInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new SampleAdjustmentInfoDTO.TabListDTO(status, "", 0));
            }
        });

        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        // 修改为按照 ApproveStatusEnum 枚举声明顺序排序
        list.sort(Comparator.comparingInt(tabDto -> {
            ApproveStatusEnum statusEnum = ApproveStatusEnum.getByStatus(tabDto.getTabFlag());
            return statusEnum != null ? statusEnum.ordinal() : Integer.MAX_VALUE;
        }));
        return list;
    }

    @Override
    public void exportList(SampleAdjustmentInfoDTO.ExportDTO param, HttpServletResponse response) {
        List<SampleAdjustmentInfoDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/sampleAdjustmentInfo.xlsx";
        String name = "样品调整单导出";
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
        SampleAdjustmentInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品调整单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品调整单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动样品调整单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品调整单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleAdjustmentInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleAdjustmentInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleAdjustmentInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleAdjustmentInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleAdjustmentInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除样品调整单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品调整单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除样品调整单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品调整单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleAdjustmentInfoEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单", remark);
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品调整单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleAdjustmentInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public SampleAdjustmentInfoDTO.ViewDTO view(String id) {
        SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品调整单数据"));
        SampleAdjustmentInfoDTO.ViewDTO data = BeanMapperUtils.map(SampleAdjustmentInfoDTO.ViewDTO.class, sampleAdjustmentInfoEntity);
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

    public void startProcess(SampleAdjustmentInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleAdjustmentInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleAdjustmentInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
            .set(SampleAdjustmentInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleAdjustmentInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getApproveUserId, "")
            .set(SampleAdjustmentInfoEntity::getApproveUserName, "")
            .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
            .set(SampleAdjustmentInfoEntity::getApproveTime, null)
            .update(new SampleAdjustmentInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
        .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleAdjustmentInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleAdjustmentInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(SampleAdjustmentInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setAdjustmentTypeName(SampleAdjustmentTypeEnum.getName(data.getAdjustmentType()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleAdjustmentInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleAdjustmentInfoEntity 样品调整单实体
     */
    private void addAttachment(SampleAdjustmentInfoDTO.AddDTO addDTO, SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleAdjustmentInfoEntity> credentialClass = SampleAdjustmentInfoEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleAdjustmentInfoEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
    }

    /**
     * 更新附件信息
     * @param addOrUpdateDTO 包含附件URL和名称列表的更新数据传输对象
     * @param old 原样品调整单实体，用于获取业务ID
     */
    private void updateAttachment(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO, SampleAdjustmentInfoEntity old) {
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<WmsAttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessIds(Arrays.asList(old.getId()));
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<WmsAttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(add)){
                Class<SampleAdjustmentInfoEntity> credentialClass = SampleAdjustmentInfoEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
                String type = tableName.value();
                List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!add.contains(attachmentUrlList.get(i))){
                        continue;
                    }
                    WmsAttachmentEntity addAttachment = new WmsAttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(old.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

                if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                    attachmentService.saveBatch(batchAttachmentList);
                }
            }
        }
    }

    /**
     * 更新样品调整单的明细数据
     * <p>
     * 该方法根据传入的更新DTO对象，对样品调整单明细进行增删改操作，并记录相应的操作日志。
     * 具体包括：
     * - 删除旧明细中存在但新数据中不存在的记录；
     * - 新增新数据中ID为空的明细记录；
     * - 更新新数据中ID不为空的明细记录；
     * 同时为上述操作添加对应的操作日志。
     *
     * @param addOrUpdateDTO        包含待更新明细数据的DTO对象
     * @param sampleAdjustmentInfoEntity 主表实体对象
     */
    private void updateDetail(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO, SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
        List<SampleAdjustmentDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();

        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(SampleAdjustmentDetailDTO.UpdateDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(com.common.core.enums.ApiError.ERROR_REPEAT_SKU);
        }

        List<SampleAdjustmentDetailEntity> oldList = sampleAdjustmentDetailService.listByMainId(sampleAdjustmentInfoEntity.getId());

        List<SampleAdjustmentDetailEntity> sampleAdjustmentDetailEntities = BeanMapperUtils.copyList(SampleAdjustmentDetailEntity.class, detailList);
        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuIds = sampleAdjustmentDetailEntities.stream().map(SampleAdjustmentDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : sampleAdjustmentDetailEntities) {
            sampleAdjustmentDetailEntity.setMainId(sampleAdjustmentInfoEntity.getId());

            // 计算差异数量 = 实际数量 - 台账数量
            Integer differenceQty = sampleAdjustmentDetailEntity.getActualQty() - sampleAdjustmentDetailEntity.getLedgerQty();
            sampleAdjustmentDetailEntity.setDifferenceQty(differenceQty);

            SkuVO skuVO = skuMap.getOrDefault(sampleAdjustmentDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleAdjustmentDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleAdjustmentDetailEntity.setProductName(skuVO.getSkuName());
            }
        }

        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream().map(SampleAdjustmentDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 处理删除的数据
            List<SampleAdjustmentDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                sampleAdjustmentDetailService.removeByIds(remove.stream().map(SampleAdjustmentDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), removePairList, "编辑操作");
            }
        }
        //处理需要新增的数据
        List<SampleAdjustmentDetailEntity> addList = sampleAdjustmentDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            sampleAdjustmentDetailService.saveBatch(addList);

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), addPairList, "编辑操作");
        }
        //处理需要更新的数据
        List<SampleAdjustmentDetailEntity> updateList = sampleAdjustmentDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            sampleAdjustmentDetailService.updateBatchById(updateList);
            //添加日志
            for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : updateList) {
                SampleAdjustmentDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), sampleAdjustmentDetailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, sampleAdjustmentDetailEntity, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), String.format("编辑SKU【%s】",oldDetail.getSkuNo()));
                }
            }
        }
    }
}

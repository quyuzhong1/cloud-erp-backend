package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.OverseasDeliveryPlanEntity;
import com.erp.server.wms.mapper.OverseasDeliveryPlanMapper;
import com.erp.server.wms.service.OverseasDeliveryPlanService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
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
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        overseasDeliveryPlanEntity.setCode(code);
        boolean save = super.save(overseasDeliveryPlanEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "发货计划" , overseasDeliveryPlanEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasDeliveryPlanEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

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
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货计划日志数据，单号：【{}】", overseasDeliveryPlanEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasDeliveryPlanEntity.getCode(), "发货计划");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasDeliveryPlanEntity, null, overseasDeliveryPlanEntity.getId(), msg);
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
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
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
    private void approveProcess(OverseasDeliveryPlanEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        OverseasDeliveryPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货计划单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(OverseasDeliveryPlanEntity entity) {
        // 已审核支持反审核
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
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
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除发货计划主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货计划日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除发货计划数据");
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
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "作废操作");
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
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货计划状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "发货计划");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
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
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public OverseasDeliveryPlanDTO.ViewDTO view(String id) {
        OverseasDeliveryPlanEntity overseasDeliveryPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到发货计划数据"));
        OverseasDeliveryPlanDTO.ViewDTO data = BeanMapperUtils.map(OverseasDeliveryPlanDTO.ViewDTO.class, overseasDeliveryPlanEntity);
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

    public void startProcess(OverseasDeliveryPlanEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(OverseasDeliveryPlanDTO.ViewDTO data) {
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
    public List<OverseasDeliveryPlanDTO.DeliverRecordDTO> listDeliverRecord(String id) {
        return null;
    }

    @Override
    public List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> ids) {
        return null;
    }

    @Override
    public Boolean generateRequisitionApplicationSave(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list) {
        return null;
    }

    @Override
    public List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids) {
        return null;
    }

    @Override
    public Boolean generateDeliverSave(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list) {
        return null;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasDeliveryPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(OverseasDeliveryPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
//            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
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
        // TODO 验证数据 & 数据赋值
    }

}

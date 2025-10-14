package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.SysLogService;
import io.seata.spring.annotation.GlobalTransactional;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.erp.server.plm.mapper.MoldRefSkuMapper;
import com.erp.server.plm.service.MoldRefSkuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_INFO;

/**
 * <p>
 * 模具关联sku 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-14
 */
@Slf4j
@Service
public class MoldRefSkuServiceImpl extends SuperServiceImpl<MoldRefSkuMapper, MoldRefSkuEntity> implements MoldRefSkuService {
    @Resource
    private SysLogService sysLogService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<MoldRefSkuDTO.ListDTO> paging(PagingDTO<MoldRefSkuDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<MoldRefSkuDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<MoldRefSkuDTO.TabListDTO> tabList(PermissionsDTO param) {
        MoldRefSkuDTO.PagingParamDTO searchParam = new MoldRefSkuDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<MoldRefSkuDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();

        List<MoldRefSkuDTO.TabListDTO> result = new ArrayList<>();
        result.add(new MoldRefSkuDTO.TabListDTO("all","全部", 0));
        for (String status : statusList) {
            MoldRefSkuDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldRefSkuDTO.TabListDTO(status, "", 0));
            tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public void exportList(MoldRefSkuDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具档案导出", EXPORT_PLM_MOLD_INFO.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        MoldRefSkuEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到模具关联sku数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改模具关联sku状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动模具关联sku流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录模具关联sku日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        //operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        MoldRefSkuEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        //operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(MoldRefSkuEntity entity, ApproveOneDTO dto) {
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
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        //operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(MoldRefSkuEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除模具关联sku主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除模具关联sku日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        //operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除模具关联sku数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        MoldRefSkuEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具关联sku数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改模具关联sku状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模具关联sku");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        //operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
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
    public Boolean approveEnd(ApproveOneDTO dto, MoldRefSkuEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        return null;
    }

    @Override
    public MoldRefSkuDTO.ViewDTO view(String id) {
        MoldRefSkuEntity moldRefSkuEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到模具关联sku数据"));
        MoldRefSkuDTO.ViewDTO data = BeanMapperUtils.map(MoldRefSkuDTO.ViewDTO.class, moldRefSkuEntity);
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

    public void startProcess(MoldRefSkuEntity entity) {
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
    private void fillOne(MoldRefSkuDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
            .set(MoldRefSkuEntity::getApproveUserId, userInfo.getUid())
            .set(MoldRefSkuEntity::getApproveUserName, userInfo.getUserName())
            .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
            .set(MoldRefSkuEntity::getApproveTime, LocalDateTime.now())
            .update(new MoldRefSkuEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
            .set(MoldRefSkuEntity::getApproveUserId, "")
            .set(MoldRefSkuEntity::getApproveUserName, "")
            .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
            .set(MoldRefSkuEntity::getApproveTime, null)
            .update(new MoldRefSkuEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(MoldRefSkuEntity::getId, id)
        .set(MoldRefSkuEntity::getApproveStatus, approveStatus)
        .update(new MoldRefSkuEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<MoldRefSkuDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.MOLD_INFO.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        // 属性赋值
        for(MoldRefSkuDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(MoldRefSkuEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(MoldRefSkuEntity moldRefSkuEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcNoticeDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.QcNoticeDetailEntity;
import com.erp.model.wms.entity.QcNoticeEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.PutawayStatusEnum;
import com.erp.model.wms.enums.QcNoticeStatusEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.QcNoticeMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.common.business.enums.ApproveStatusEnum;
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
 * 质检通知单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
@Slf4j
@Service
public class QcNoticeServiceImpl extends SuperServiceImpl<QcNoticeMapper, QcNoticeEntity> implements QcNoticeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private QcNoticeDetailService qcNoticeDetailService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WmsAttachmentService attachmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(QcNoticeDTO.AddDTO addDTO) {
        QcNoticeEntity qcNoticeEntity = new QcNoticeEntity();
        BeanMapperUtils.copy(addDTO, qcNoticeEntity);
        log.info("开始新增质检通知单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZJTZ);
        qcNoticeEntity.setCode(code);
        boolean save = super.save(qcNoticeEntity);
        if (!save) {
            throw new ServiceException("质检通知单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "质检通知单", qcNoticeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeEntity.getId(), "新增操作");

        qcNoticeDetailService.add(addDTO, qcNoticeEntity.getId());
        return new BaseResultDTO.AddDTO(qcNoticeEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(QcNoticeDTO.UpdateDTO updateDTO) {
        QcNoticeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "质检通知单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        QcNoticeEntity qcNoticeEntity = BeanMapperUtils.map(QcNoticeEntity.class, updateDTO);

        log.info("编辑 开始修改质检通知单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(qcNoticeEntity);
        if (!save) {
            throw new ServiceException("质检通知单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录质检通知单日志数据，单号：【{}】", qcNoticeEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), qcNoticeEntity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLogByObj(old, qcNoticeEntity, ModuleTypeEnum.QC_NOTICE.getCode(), qcNoticeEntity.getId(), msg);

        qcNoticeDetailService.update(updateDTO, qcNoticeEntity.getId());

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<QcNoticeDTO.ListDTO> paging(PagingDTO<QcNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<QcNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        QcNoticeDTO.PagingParamDTO searchParam = new QcNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<QcNoticeDTO.TabListDTO> list = new ArrayList<>();
        List<QcNoticeDTO.TabListDTO> tabList = baseMapper.tabList(searchParam);
        Map<String, QcNoticeDTO.TabListDTO> tabMap = tabList.stream().collect(Collectors.toMap(QcNoticeDTO.TabListDTO::getTabFlag, t ->t));
        if(tabMap.containsKey(ApproveStatusEnum.WAIT_SUBMIT.getCode())){
            QcNoticeDTO.TabListDTO tabListDTO = tabMap.get(ApproveStatusEnum.WAIT_SUBMIT.getCode());
            tabListDTO.setTabFlagName(ApproveStatusEnum.WAIT_SUBMIT.getName());
            list.add(tabListDTO);
        }else{
            list.add(new QcNoticeDTO.TabListDTO(ApproveStatusEnum.WAIT_SUBMIT.getCode(),ApproveStatusEnum.WAIT_SUBMIT.getName(), 0));
        }

        if(tabMap.containsKey(ApproveStatusEnum.APPROVE_ING.getCode())){
            QcNoticeDTO.TabListDTO tabListDTO = tabMap.get(ApproveStatusEnum.APPROVE_ING.getCode());
            tabListDTO.setTabFlagName(ApproveStatusEnum.APPROVE_ING.getName());
            list.add(tabListDTO);
        }else{
            list.add(new QcNoticeDTO.TabListDTO(ApproveStatusEnum.APPROVE_ING.getCode(),ApproveStatusEnum.APPROVE_ING.getName(), 0));
        }

        List<QcNoticeDTO.TabListDTO> tabQcStatusList = baseMapper.tabQcStatusList(searchParam);
        Map<String, QcNoticeDTO.TabListDTO> tabQcStatusMap = tabQcStatusList.stream().collect(Collectors.toMap(QcNoticeDTO.TabListDTO::getTabFlag, t -> t));
        List<String> codeList = QcNoticeStatusEnum.getCodeList();
        for (String code : codeList) {
            if(tabQcStatusMap.containsKey(code)){
                QcNoticeDTO.TabListDTO tabListDTO = tabQcStatusMap.get(code);
                tabListDTO.setTabFlagName(QcNoticeStatusEnum.getByCode(code).getName());
                list.add(tabListDTO);
            }else {
                list.add(new QcNoticeDTO.TabListDTO(code,QcNoticeStatusEnum.getByCode(code).getName(), 0));
            }
        }
        list.add(new QcNoticeDTO.TabListDTO("all","全部", list.stream().mapToInt(QcNoticeDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(QcNoticeDTO.ExportDTO param, HttpServletResponse response) {
        List<QcNoticeDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/qcNotice.xlsx";
        String name = "质检通知单导出";
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
        QcNoticeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到质检通知单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改质检通知单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动质检通知单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录质检通知单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(QcNoticeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(QcNoticeDTO.UpdateDTO dto) {
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
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        QcNoticeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(QcNoticeEntity entity, ApproveOneDTO dto) {
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(QcNoticeEntity entity) {
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
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除质检通知单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除质检通知单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除质检通知单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        QcNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改质检通知单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检通知单");
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
    public Boolean approveEnd(ApproveOneDTO dto, QcNoticeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public QcNoticeDTO.ViewDTO view(String id) {
        QcNoticeEntity qcNoticeEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检通知单数据"));
        QcNoticeDTO.ViewDTO data = BeanMapperUtils.map(QcNoticeDTO.ViewDTO.class, qcNoticeEntity);

        List<QcNoticeDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(QcNoticeDetailDTO.ViewDTO.class, qcNoticeDetailService.listByMainIds(Arrays.asList(id)));
        data.setDetailList(detailList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(QcNoticeEntity entity) {
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

    private void fillOne(QcNoticeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<String> warehouseIdList = new ArrayList<>();
        warehouseIdList.add(data.getQcWarehouseId());
        warehouseIdList.add(data.getPutawayWarehouseId());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIdList);
        Map<String, String> warehouseMap = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //质检仓库
        data.setQcWarehouseName(warehouseMap.get(data.getQcWarehouseId()));

        //上架仓库
        data.setPutawayWarehouseName(warehouseMap.get(data.getPutawayWarehouseId()));

        //质检类型
        data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));

        //单据状态
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

        if(CollUtil.isNotEmpty(data.getDetailList())){
            List<QcNoticeDetailDTO.ViewDTO> detailList = data.getDetailList();
            List<String> ids = detailList.stream().map(QcNoticeDetailDTO.ViewDTO::getId).collect(Collectors.toList());
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> listMap = attachmentList.stream().collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId));

            for (QcNoticeDetailDTO.ViewDTO dto : detailList) {
                List<WmsAttachmentDTO.UpdateDTO> updateDTOS = listMap.get(dto.getId());
                List<String> imageUrls = updateDTOS.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
                List<String> imageNames = updateDTOS.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
                dto.setAttachNameList(imageNames);
                dto.setAttachUrlList(imageUrls);
            }
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveUserId, userInfo.getUid())
                .set(QcNoticeEntity::getApproveUserName, userInfo.getUserName())
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .set(QcNoticeEntity::getApproveTime, LocalDateTime.now())
                .update(new QcNoticeEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveUserId, "")
                .set(QcNoticeEntity::getApproveUserName, "")
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .set(QcNoticeEntity::getApproveTime, null)
                .update(new QcNoticeEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(QcNoticeEntity::getId, id)
                .set(QcNoticeEntity::getApproveStatus, approveStatus)
                .update(new QcNoticeEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<QcNoticeDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        List<String> qcWarehouseId = list.stream().map(QcNoticeDTO.ListDTO::getQcWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> putawayWarehouseId = list.stream().map(QcNoticeDTO.ListDTO::getPutawayWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        qcWarehouseId.addAll(putawayWarehouseId);

        List<String> warehouseIdList = qcWarehouseId.stream().distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIdList);
        Map<String, String> warehouseMap = warehouseEntities.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        // 属性赋值
        for (QcNoticeDTO.ListDTO data : list) {
            //质检仓库
            data.setQcWarehouseName(warehouseMap.get(data.getQcWarehouseId()));

            //上架仓库
            data.setPutawayWarehouseName(warehouseMap.get(data.getPutawayWarehouseId()));

            //质检类型
            data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));

            //单据质检状态
            data.setQcStatusName(QcNoticeStatusEnum.getByCode(data.getQcStatus()).getName());

            //单据状态
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

            //sku 质检状态
            data.setQcDetailStatusName(QcNoticeStatusEnum.getByCode(data.getQcDetailStatus()).getName());

            //上架状态 待上架:wait  部分上架：part  已上架：finish
            data.setPutawayStatusName(PutawayStatusEnum.getByCode(data.getPutawayStatus()).getName());

        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(QcNoticeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(QcNoticeEntity qcNoticeEntity) {
    }
}

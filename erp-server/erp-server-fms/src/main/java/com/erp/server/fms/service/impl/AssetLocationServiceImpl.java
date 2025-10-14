package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
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
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.erp.model.fms.dto.excel.AssetLocationExcelDTO;
import com.erp.model.fms.entity.AssetLocationEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.listener.AssetLocationExcelListener;
import com.erp.server.fms.mapper.AssetLocationMapper;
import com.erp.server.fms.service.AssetLocationService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_FMS_ASSET_LOCATION;
/**
 * <p>
 * 资产位置表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetLocationServiceImpl extends SuperServiceImpl<AssetLocationMapper, AssetLocationEntity> implements AssetLocationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetLocationDTO.AddDTO addDTO) {
        AssetLocationEntity assetLocationEntity = new AssetLocationEntity();
        BeanMapperUtils.copy(addDTO, assetLocationEntity);

        // 数据处理
        handleData(assetLocationEntity);

        log.info("开始新增资产位置单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZCWZ);
        assetLocationEntity.setCode(code);
        boolean save = super.save(assetLocationEntity);
        if(!save) {
            throw new ServiceException("资产位置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产位置单" , assetLocationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), assetLocationEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetLocationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetLocationDTO.UpdateDTO addOrUpdateDTO) {
        AssetLocationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产位置单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetLocationEntity assetLocationEntity =  BeanMapperUtils.map(AssetLocationEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetLocationEntity);
        log.info("编辑 开始修改资产位置单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetLocationEntity);
        if(!save) {
            throw new ServiceException("资产位置单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录资产位置单日志数据，单号：【{}】", assetLocationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetLocationEntity.getCode(), "资产位置单");
        operateLogService.addModuleOperateLogByObj(old, assetLocationEntity, ModuleTypeEnum.ASSET_LOCATION.getCode(), assetLocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetLocationDTO.ListDTO> paging(PagingDTO<AssetLocationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetLocationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetLocationDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetLocationDTO.PagingParamDTO searchParam = new AssetLocationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetLocationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        });
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetLocationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetLocationDTO.TabListDTO newTab = new AssetLocationDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
                list.add(newTab);
            }
        });
        
        // 按照指定顺序排序：待提交、审核中、已审核、不通过
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approve", "reject");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(AssetLocationDTO.TabListDTO::getCount).sum();
        AssetLocationDTO.TabListDTO allTab = new AssetLocationDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }

    @Override
    public Boolean exportList(AssetLocationDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("资产位置导出", FileTaskEventEnum.EXPORT_FMS_ASSET_LOCATION.getCode(), param);
        return true;
    }

    @Override
    public PagingVO<AssetLocationDTO.ListDTO> getAssetLocationPageData(PagingDTO<AssetLocationDTO.ExportDTO> dto) {
        Page<AssetLocationDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<AssetLocationDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetLocationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产位置单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产位置单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动资产位置单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产位置单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetLocationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetLocationDTO.UpdateDTO dto) {
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
        AssetLocationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetLocationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_LOCATION.getCode());
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
        AssetLocationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产位置单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetLocationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetLocationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产位置单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        // 删除主单数据
        log.info("删除 开始删除资产位置单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除资产位置单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getCode(), "删除资产位置单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetLocationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产位置单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产位置单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetLocationEntity::getId, id)
            .set(AssetLocationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetLocationEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetLocationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产位置单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改资产位置单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产位置单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_LOCATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_LOCATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetLocationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        return Boolean.TRUE;
    }

    @Override
    public AssetLocationDTO.ViewDTO view(String id) {
        AssetLocationEntity assetLocationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到资产位置单数据"));
        AssetLocationDTO.ViewDTO data = BeanMapperUtils.map(AssetLocationDTO.ViewDTO.class, assetLocationEntity);
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

    public void startProcess(AssetLocationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_LOCATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetLocationDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(AssetLocationEntity::getId, id)
            .set(AssetLocationEntity::getApproveUserId, userInfo.getUid())
            .set(AssetLocationEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetLocationEntity::getApproveStatus, approveStatus)
            .set(AssetLocationEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetLocationEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetLocationEntity::getId, id)
            .set(AssetLocationEntity::getApproveUserId, "")
            .set(AssetLocationEntity::getApproveUserName, "")
            .set(AssetLocationEntity::getApproveStatus, approveStatus)
            .set(AssetLocationEntity::getApproveTime, null)
            .update(new AssetLocationEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetLocationEntity::getId, id)
        .set(AssetLocationEntity::getApproveStatus, approveStatus)
        .update(new AssetLocationEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetLocationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AssetLocationDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDisabledStatusName(data.getDisabled() ? "是" : "否");
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetLocationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetLocationEntity assetLocationEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
    * 批量启用/禁用
    * @author wuht
    * @date: 2025-10-13
    * @param dto
    * @return Boolean
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<AssetLocationEntity> list = this.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "资产位置");
        }

        Boolean disabled = dto.getDisabled();
        // 检查所有数据的当前状态是否与目标状态不同
        long count = list.stream().filter(d -> !d.getDisabled().equals(disabled)).count();
        if (count != list.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }

        // 更新禁用状态
        list.forEach(d -> d.setDisabled(disabled));
        
        // 添加日志
        List<Pair<String, String>> pairList = list.stream()
                .map(obj -> new Pair<>(obj.getId(), obj.getCode()))
                .collect(Collectors.toList());
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "禁用", disabled ? "禁用" : "启用");
        String finalContent = "[%s]," + content;
        operateLogService.batchAddModuleOperateLog(finalContent, ModuleTypeEnum.ASSET_LOCATION.getCode(), pairList, "状态变更");

        return this.updateBatchById(list);
    }

    /**
     * 下载模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 下载资产位置导入模板
        String path = "classpath:excel/assetLocationTemplate.xlsx";
        String excelName = "资产位置导入模板.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
            log.info("开始下载资产位置导入模板");
        } catch (Exception e) {
            log.error("资产位置导入模板下载失败", e);
            throw new ServiceException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 异步导入
     */
    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("资产位置导入", IMPORT_FMS_ASSET_LOCATION.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 导入资产位置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetLocation(BaseDTO.ImportDTO dto) {
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream()
                .filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(findUserDTO)) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        AssetLocationExcelListener excelListenerUtil = new AssetLocationExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetLocationExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetLocationExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "资产位置错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetLocationExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    /**
     * 处理导入成功的数据
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<AssetLocationExcelDTO> successList, List<AssetLocationExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        if (StringUtils.isBlank(importType)) {
            //给个默认值
            importType = ImportTypeEnum.ADD.getCode();
        }

        // 处理每条数据
        for (AssetLocationExcelDTO excelDTO : successList) {
            try {
                if (ImportTypeEnum.ADD.getCode().equals(importType)) {
                    AssetLocationDTO.AddDTO addDTO = new AssetLocationDTO.AddDTO();
                    addDTO.setDescription(excelDTO.getDescription());
                    addDTO.setAddress(excelDTO.getAddress());
                    addDTO.setDetailedAddress(excelDTO.getDetailedAddress());
                    this.add(addDTO);
                }
            } catch (Exception e) {
                log.error("导入资产位置失败，行号：{}，错误：{}", excelDTO.getRowNum(), e.getMessage(), e);
                excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                errorList2.add(excelDTO);
            }
        }
    }
}

package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.dto.AssetNoticeDetailDTO;
import com.erp.model.plm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.AssetApproveStatusEnum;
import com.erp.model.plm.enums.AssetPurchaseOrderTypeEnum;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.AssetNoticeExcelListener;
import com.erp.server.plm.mapper.AssetNoticeDetailMapper;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.plm.mapper.AssetNoticeMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.AssetNoticeDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ASSET_NOTICE;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetNoticeServiceImpl extends SuperServiceImpl<AssetNoticeMapper, AssetNoticeEntity> implements AssetNoticeService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;

    @Autowired
    private MoldInfoService moldInfoService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProductPurchaseService productPurchaseService;

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetNoticeDetailMapper assetNoticeDetailMapper;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetNoticeDTO.AddDTO addDTO) {
        AssetNoticeEntity assetNoticeEntity = new AssetNoticeEntity();
        BeanMapperUtils.copy(addDTO, assetNoticeEntity);

        // 数据处理
        handleData(assetNoticeEntity);

        log.info("开始新增资产通知单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL);
        assetNoticeEntity.setCode(code);
        boolean save = super.save(assetNoticeEntity);
        if(!save) {
            throw new ServiceException("资产通知单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产通知单" , assetNoticeEntity.getCode());
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), assetNoticeEntity.getId(), "新增操作");
        // 新增明细
        assetNoticeDetailService.add(addDTO.getAssetNoticeDetailDTO(),assetNoticeEntity.getId());
        return new BaseResultDTO.AddDTO(assetNoticeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetNoticeDTO.UpdateDTO addOrUpdateDTO) {
        AssetNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetNoticeEntity assetNoticeEntity =  BeanMapperUtils.map(AssetNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetNoticeEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetNoticeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        assetNoticeDetailService.update(addOrUpdateDTO.getAssetNoticeDetailDTO(),assetNoticeEntity.getId());
        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，单号：【{}】", assetNoticeEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetNoticeEntity.getCode(), "资产通知单");
        operateLogService.addSysLogByUpdate(old, assetNoticeEntity, null, assetNoticeEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetNoticeDTO.ListDTO> paging(PagingDTO<AssetNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetNoticeDTO.PagingParamDTO searchParam = new AssetNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AssetNoticeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(AssetNoticeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        for (AssetNoticeDTO.TabListDTO tabListDTO : list) {
            tabListDTO.setTabFlagName(ApproveStatusEnum.getName(tabListDTO.getTabFlag()));
        }
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new AssetNoticeDTO.TabListDTO(status, AssetApproveStatusEnum.getName(status), 0));
        }
        });
        list.add(new AssetNoticeDTO.TabListDTO("all", AssetApproveStatusEnum.ALL.getName() ,list.stream().mapToInt(AssetNoticeDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AssetNoticeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("资产通知单导出", EXPORT_PLM_ASSET_NOTICE.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetNoticeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), id, "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetNoticeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetNoticeDTO.UpdateDTO dto) {
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
        AssetNoticeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单", approveType.getName(), dto.getComment());
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetNoticeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_NOTICE.getCode());
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
        BatchResultDTO batchResultDTO = new BatchResultDTO();
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据
        List<AssetNoticeDetailEntity> assetNoticeDetailEntityList = assetNoticeDetailService.list(new LambdaQueryWrapper<AssetNoticeDetailEntity>().eq(AssetNoticeDetailEntity::getMainId, id));
        long createCount = assetNoticeDetailEntityList.stream().filter(obj -> !CreatePoTypeEnum.NOT_GENERATED.getStatus().equals(obj.getCreatePoType())).count();
        if (createCount > 0) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
        }
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetNoticeEntity entity) {
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
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交并且未作废数据支持删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        assetNoticeDetailService.removeById(id,null);
        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单");
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_NOTICE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetNoticeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public AssetNoticeDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        //查询所有审核通过的模具
        LambdaQueryWrapper<MoldInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MoldInfoEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getCode())
                .eq(MoldInfoEntity::getIsDeleted,Boolean.FALSE)
                .eq(MoldInfoEntity::getInvalidStatus,Boolean.FALSE);
        List<MoldInfoEntity> moldList = moldInfoService.list(lambdaQueryWrapper);
        //查询所有启用核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        AssetNoticeExcelListener excelListenerUtil = new AssetNoticeExcelListener(moldList,userList,deptList,companyList);

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), AssetNoticeImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<AssetNoticeImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        AssetNoticeDetailDTO.ImportDTO importDTO = new AssetNoticeDetailDTO.ImportDTO();
        //导入数据处理
        List<AssetNoticeDetailDTO.MoldImportDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<AssetNoticeImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "资产通知单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetNoticeImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    @Transactional
    public BatchResultDTO invalid(AssetNoticeEntity entity, String remark) {
        super.getByIdOpt(entity.getId()).orElseThrow(() -> new ServiceException("未找到资产通知单"));

        // 待提交或审核不通过并且未作废允许作废
        if(!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())){
            throw new ServiceException(ApiError.ERROR_98012);
        }
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        log.info("作废 开始修改资产通知单状态数据，id：【{}】", entity.getId());
        lambdaUpdate().eq(AssetNoticeEntity::getId, entity.getId())
                .set(AssetNoticeEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(AssetNoticeEntity::getInvalidReason, remark)
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产通知单", remark);
        operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public AssetNoticeDTO.ViewDTO view(String id) {
        AssetNoticeEntity assetNoticeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        AssetNoticeDTO.ViewDTO data = BeanMapperUtils.map(AssetNoticeDTO.ViewDTO.class, assetNoticeEntity);
        // 数据填充处理
        fillOne(data);
        LambdaQueryWrapper<AssetNoticeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetNoticeDetailEntity::getMainId,id);
        List<AssetNoticeDetailEntity> detailList = assetNoticeDetailService.list(queryWrapper);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_95298);
        }
        List<AssetNoticeDetailDTO.ViewDTO> dtoList = BeanMapperUtils.copyList(AssetNoticeDetailDTO.ViewDTO.class, detailList);
        fillViewList(dtoList);
        data.setAssetNoticeDetailDTOList(dtoList);
        return data;
    }

    public void fillViewList(List<AssetNoticeDetailDTO.ViewDTO> dtoList){

        for (AssetNoticeDetailDTO.ViewDTO detailDTO : dtoList) {
            List<AssetNoticeDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOS = assetNoticeDetailMapper.searchMoldRefSkuByAssetId(detailDTO.getAssetId());
            detailDTO.setAssetDetailRefSkuDTOList(assetDetailRefSkuDTOS);
            detailDTO.setTagName(MoldInfoTagEnum.getName(detailDTO.getTag()));
        }


    }
    /**
    * 启动流程
    * @param entity
    * @return void
    * @Date
    **/

    public void startProcess(AssetNoticeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.MOULD_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetNoticeDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(AssetNoticeEntity::getId, id)
            .set(AssetNoticeEntity::getApproveUserId, userInfo.getUid())
            .set(AssetNoticeEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetNoticeEntity::getApproveStatus, approveStatus)
            .set(AssetNoticeEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetNoticeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetNoticeEntity::getId, id)
            .set(AssetNoticeEntity::getApproveUserId, "")
            .set(AssetNoticeEntity::getApproveUserName, "")
            .set(AssetNoticeEntity::getApproveStatus, approveStatus)
            .set(AssetNoticeEntity::getApproveTime, null)
            .update(new AssetNoticeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetNoticeEntity::getId, id)
        .set(AssetNoticeEntity::getApproveStatus, approveStatus)
        .update(new AssetNoticeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetNoticeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AssetNoticeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setCreatePoTypeName(CreatePoTypeEnum.getName(data.getCreatePoType()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetNoticeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetNoticeEntity assetNoticeEntity) {
        //状态默认待提交
        assetNoticeEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        //采购员
        if (StringUtils.isNotBlank(assetNoticeEntity.getApplyUserId())) {
            FindUserDTO purchaseUser = sysUserFeign.getUserByUserId(assetNoticeEntity.getApplyUserId());
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(purchaseUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            assetNoticeEntity.setApplyUserName(purchaseUser.getUserName());
            // 部门
            assetNoticeEntity.setApplyDeptId(purchaseUser.getDepartmentId());
            assetNoticeEntity.setApplyDeptName(purchaseUser.getDepartmentName());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportSuccessList(List<AssetNoticeDetailDTO.MoldImportDTO> successList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 按 serialNumber 分组
        Map<String, List<AssetNoticeDetailDTO.MoldImportDTO>> groupedBySerialNumber = successList.stream()
                .collect(Collectors.groupingBy(AssetNoticeDetailDTO.MoldImportDTO::getSerialNumber));

        try {
            for (Map.Entry<String, List<AssetNoticeDetailDTO.MoldImportDTO>> entry : groupedBySerialNumber.entrySet()) {
                String serialNumber = entry.getKey();
                List<AssetNoticeDetailDTO.MoldImportDTO> moldImportDTOList = entry.getValue();

                if (CollectionUtils.isEmpty(moldImportDTOList)) {
                    continue;
                }

                // 取第一个元素作为主表数据
                AssetNoticeDetailDTO.MoldImportDTO firstMoldImportDTO = moldImportDTOList.get(0);
                AssetNoticeEntity entity = new AssetNoticeEntity();
                entity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPL));
                entity.setApplyDate(firstMoldImportDTO.getApplyDate());
                entity.setApplyUserId(firstMoldImportDTO.getApplyUserId());
                entity.setApplyUserName(firstMoldImportDTO.getApplyUserName());
                entity.setApplyDeptId(firstMoldImportDTO.getApplyDeptId());
                entity.setApplyDeptName(firstMoldImportDTO.getApplyDeptName());
                entity.setInvalidStatus(Boolean.FALSE);
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);

                // 保存主表
                boolean save = super.save(entity);
                if (!save) {
                    throw new ServiceException("资产通知单单头导入保存失败");
                }

                // 处理明细数据
                List<AssetNoticeDetailEntity> assetNoticeDetailEntities = new ArrayList<>();
                for (AssetNoticeDetailDTO.MoldImportDTO moldImportDTO : moldImportDTOList) {
                    List<AssetNoticeDetailDTO.MoldDetailImportDTO> moldDetailImportDTOList = moldImportDTO.getMoldDetailImportDTOList();
                    for (AssetNoticeDetailDTO.MoldDetailImportDTO moldDetailImportDTO : moldDetailImportDTOList) {
                        AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
                        BeanMapperUtils.copy(moldDetailImportDTO, assetNoticeDetailEntity);
                        assetNoticeDetailEntity.setMainId(entity.getId()); // 关联主表ID
                        assetNoticeDetailEntity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
                        assetNoticeDetailEntities.add(assetNoticeDetailEntity);
                    }
                }

                // 批量保存明细
                boolean saveDetail = assetNoticeDetailService.saveBatch(assetNoticeDetailEntities);
                if (!saveDetail) {
                    throw new ServiceException("资产通知单明细导入保存失败");
                }

                // 记录操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】",
                        UserContext.getDefaultLoginUser().getUserName(),
                        "资产通知单",
                        entity.getCode());
                operateLogService.addSysLogBySave(msg, ModuleTypeEnum.ASSET_NOTICE.getCode(), entity.getId(), "新增操作");
            }
        } catch (Exception e) {
            throw new ServiceException("资产通知单导入保存失败", e);
        }
    }
}

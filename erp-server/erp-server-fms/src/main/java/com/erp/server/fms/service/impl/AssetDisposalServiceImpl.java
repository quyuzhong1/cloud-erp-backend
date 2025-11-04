package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.fms.dto.AssetDisposalDetailDTO;
import com.erp.model.fms.dto.AssetDisposalPhysicalDetailDTO;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.erp.model.fms.dto.excel.AssetDisposalImportExcelDTO;
import com.erp.model.fms.entity.*;
import com.erp.model.fms.enums.AssetDisposalDetailInvoiceTypeEnum;
import com.erp.model.fms.enums.AssetDisposalDisposalMethodEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.fms.listener.AssetDisposalExcelListener;
import com.erp.server.fms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.fms.mapper.AssetDisposalMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.fms.dto.AssetDisposalDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_DISPOSAL;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_FMS_ASSET_DISPOSAL;

/**
 * <p>
 * 资产处置单主表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-29
 */
@Slf4j
@Service
public class AssetDisposalServiceImpl extends SuperServiceImpl<AssetDisposalMapper, AssetDisposalEntity> implements AssetDisposalService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private AssetDisposalDetailService assetDisposalDetailService;
    @Resource
    private AssetDisposalPhysicalDetailService assetDisposalPhysicalDetailService;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private AssetLocationService assetLocationService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private AssetCardService assetCardService;
    @Resource
    private AssetCardDetailService assetCardDetailService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetDisposalDTO.AddDTO addDTO) {
        AssetDisposalEntity assetDisposalEntity = new AssetDisposalEntity();
        BeanMapperUtils.copy(addDTO, assetDisposalEntity);

        // 数据处理
        handleData(assetDisposalEntity);

        log.info("开始新增资产处置单主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_PRODIS);
        assetDisposalEntity.setCode(code);
        boolean save = super.save(assetDisposalEntity);
        if (!save) {
            throw new ServiceException("资产处置单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产处置单主单", assetDisposalEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), assetDisposalEntity.getId(), "新增操作");

        String mainId = assetDisposalEntity.getId();
        List<AssetDisposalDetailDTO.UpdateDTO> assetDisposalDetailDTOList = addDTO.getAssetDisposalDetailDTOList();
        for (AssetDisposalDetailDTO.UpdateDTO dto : assetDisposalDetailDTOList) {
            AssetDisposalDetailEntity detailEntity = new AssetDisposalDetailEntity();
            BeanMapper.copy(dto, detailEntity);
            detailEntity.setMainId(mainId);
            assetDisposalDetailService.save(detailEntity);

            String assetDisposalDetailId = detailEntity.getId();

            List<AssetDisposalPhysicalDetailDTO.UpdateDTO> assetDisposalPhysicalDetailDTOList = dto.getAssetDisposalPhysicalDetailDTOList();
            assetDisposalPhysicalDetailDTOList.forEach(e -> {
                e.setMainId(mainId);
                e.setAssetDisposalDetailId(assetDisposalDetailId);
            });
            List<AssetDisposalPhysicalDetailEntity> assetDisposalPhysicalDetailEntities = BeanMapper.copyList(assetDisposalPhysicalDetailDTOList, AssetDisposalPhysicalDetailEntity.class);
            assetDisposalPhysicalDetailService.saveBatch(assetDisposalPhysicalDetailEntities);
        }
        return new BaseResultDTO.AddDTO(assetDisposalEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetDisposalDTO.UpdateDTO addOrUpdateDTO) {
        AssetDisposalEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "资产处置单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetDisposalEntity assetDisposalEntity = BeanMapperUtils.map(AssetDisposalEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetDisposalEntity);
        log.info("编辑 开始修改资产处置单主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetDisposalEntity);
        if (!save) {
            throw new ServiceException("资产处置单主单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录资产处置单主单日志数据，单号：【{}】", assetDisposalEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetDisposalEntity.getCode(), "资产处置单主单");
        operateLogService.addModuleOperateLogByObj(old, assetDisposalEntity, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), assetDisposalEntity.getId(), msg);

        //更新明细
        updateDetail(addOrUpdateDTO, assetDisposalEntity);


        return Boolean.TRUE;
    }

    /**
     * 更新资产处置明细信息（包括新增、修改、删除操作）
     * @param addOrUpdateDTO 资产处置更新传输对象，包含主表及明细数据
     * @param assetDisposalEntity 当前资产处置主表实体对象
     */
    private void updateDetail(AssetDisposalDTO.UpdateDTO addOrUpdateDTO, AssetDisposalEntity assetDisposalEntity) {
        String mainId = assetDisposalEntity.getId();

        //查询旧数据
        List<AssetDisposalDetailEntity> oldDetailList = assetDisposalDetailService.lambdaQuery().eq(AssetDisposalDetailEntity::getMainId, assetDisposalEntity.getId()).list();

        List<AssetDisposalDetailDTO.UpdateDTO> assetDisposalDetailDTOList = addOrUpdateDTO.getAssetDisposalDetailDTOList();

        if(CollUtil.isNotEmpty(oldDetailList)){
            List<String> detailIds = assetDisposalDetailDTOList.stream().map(AssetDisposalDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 处理删除的数据
            List<AssetDisposalDetailEntity> remove = oldDetailList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                List<String> removeIds = remove.stream().map(AssetDisposalDetailEntity::getId).collect(Collectors.toList());

                assetDisposalDetailService.removeByIds(removeIds);

                assetDisposalPhysicalDetailService.lambdaUpdate()
                        .in(AssetDisposalPhysicalDetailEntity::getAssetDisposalDetailId, removeIds)
                        .set(AssetDisposalPhysicalDetailEntity::getIsDeleted,Boolean.TRUE)
                        .update();

                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(mainId, obj.getSourceCode())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除资产卡片【%s】", ModuleTypeEnum.ASSET_DISPOSAL.getCode(), removePairList, "编辑操作");
            }
        }

        //处理需要新增的数据
        List<AssetDisposalDetailDTO.UpdateDTO> addList = assetDisposalDetailDTOList.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            for (AssetDisposalDetailDTO.UpdateDTO dto : addList) {
                AssetDisposalDetailEntity detailEntity = new AssetDisposalDetailEntity();
                BeanMapper.copy(dto, detailEntity);
                detailEntity.setMainId(mainId);
                assetDisposalDetailService.save(detailEntity);

                String assetDisposalDetailId = detailEntity.getId();

                List<AssetDisposalPhysicalDetailDTO.UpdateDTO> assetDisposalPhysicalDetailDTOList = dto.getAssetDisposalPhysicalDetailDTOList();
                assetDisposalPhysicalDetailDTOList.forEach(e -> {
                    e.setMainId(mainId);
                    e.setAssetDisposalDetailId(assetDisposalDetailId);
                });
                List<AssetDisposalPhysicalDetailEntity> assetDisposalPhysicalDetailEntities = BeanMapper.copyList(assetDisposalPhysicalDetailDTOList, AssetDisposalPhysicalDetailEntity.class);
                assetDisposalPhysicalDetailService.saveOrUpdateBatch(assetDisposalPhysicalDetailEntities);
            }

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSourceCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加资产卡片【%s】", ModuleTypeEnum.ASSET_DISPOSAL.getCode(), addPairList, "编辑操作");
        }

        //处理需要更新的数据
        List<AssetDisposalDetailDTO.UpdateDTO> updateList = assetDisposalDetailDTOList.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            List<String> assetDisposalDetailIds = updateList.stream().map(AssetDisposalDetailDTO.UpdateDTO::getId).collect(Collectors.toList());

            //查询旧数据
            List<AssetDisposalPhysicalDetailEntity> oldPhysicalDetailList = assetDisposalPhysicalDetailService.lambdaQuery()
                    .eq(AssetDisposalPhysicalDetailEntity::getMainId, assetDisposalEntity.getId())
                    .in(AssetDisposalPhysicalDetailEntity::getAssetDisposalDetailId, assetDisposalDetailIds)
                    .list();

            Map<String, List<AssetDisposalPhysicalDetailEntity>> map = oldPhysicalDetailList.stream().collect(Collectors.groupingBy(AssetDisposalPhysicalDetailEntity::getAssetDisposalDetailId));

            for (AssetDisposalDetailDTO.UpdateDTO dto : updateList) {
                AssetDisposalDetailEntity detailEntity = new AssetDisposalDetailEntity();
                BeanMapper.copy(dto, detailEntity);
                detailEntity.setMainId(mainId);
                assetDisposalDetailService.updateById(detailEntity);

                String assetDisposalDetailId = detailEntity.getId();

                List<AssetDisposalPhysicalDetailDTO.UpdateDTO> assetDisposalPhysicalDetailDTOList = dto.getAssetDisposalPhysicalDetailDTOList();
                assetDisposalPhysicalDetailDTOList.forEach(e -> {
                    e.setMainId(mainId);
                    e.setAssetDisposalDetailId(assetDisposalDetailId);
                });

                List<AssetDisposalPhysicalDetailEntity> oldPhysicalDetailById = map.getOrDefault(assetDisposalDetailId,null);
                if(Objects.nonNull(oldPhysicalDetailById) && CollUtil.isNotEmpty(oldPhysicalDetailById)){

                    List<String> detailIds = assetDisposalPhysicalDetailDTOList.stream().map(AssetDisposalPhysicalDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    // 处理删除的数据
                    List<AssetDisposalPhysicalDetailEntity> remove = oldPhysicalDetailById.stream()
                            .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                            .collect(Collectors.toList());
                    if(CollUtil.isNotEmpty(remove)){
                        List<String> removeIds = remove.stream().map(AssetDisposalPhysicalDetailEntity::getId).collect(Collectors.toList());
                        assetDisposalPhysicalDetailService.removeByIds(removeIds);
                    }
                }

                List<AssetDisposalPhysicalDetailEntity> assetDisposalPhysicalDetailEntities = BeanMapper.copyList(assetDisposalPhysicalDetailDTOList, AssetDisposalPhysicalDetailEntity.class);
                assetDisposalPhysicalDetailService.saveOrUpdateBatch(assetDisposalPhysicalDetailEntities);

                //添加日志
                AssetDisposalDetailEntity oldDetail = oldDetailList.stream().filter(e -> Objects.equals(e.getId(),assetDisposalDetailId)).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, detailEntity, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), mainId, "编辑资产卡片【%s】",oldDetail.getSourceCode());
                }
            }
        }
    }


    @Override
    public PagingVO<AssetDisposalDTO.ListDTO> paging(PagingDTO<AssetDisposalDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetDisposalDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetDisposalDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetDisposalDTO.PagingParamDTO searchParam = new AssetDisposalDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AssetDisposalDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<AssetDisposalDTO.TabListDTO> result = new ArrayList<>();
        result.add(new AssetDisposalDTO.TabListDTO("all", "全部", 0));
        for (String status : statusList) {
            AssetDisposalDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new AssetDisposalDTO.TabListDTO(status, "", 0));
            tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
            result.add(tabListDTO);
        }
        return result;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<AssetDisposalDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
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
        for (AssetDisposalDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDisposalMethodName(AssetDisposalDisposalMethodEnum.getName(data.getDisposalMethod()));
            data.setDisposalCurrencyName(CurrencyEnum.getNameByCode(data.getDisposalCurrency()));
            data.setInvoiceTypeName(AssetDisposalDetailInvoiceTypeEnum.getName(data.getInvoiceType()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }

    @Override
    public void exportList(AssetDisposalDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("资产处置单导出", EXPORT_FMS_ASSET_DISPOSAL.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetDisposalEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产处置单主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产处置单主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动资产处置单主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产处置单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetDisposalDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetDisposalDTO.UpdateDTO dto) {
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
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetDisposalEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(AssetDisposalEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_DISPOSAL.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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
        AssetDisposalEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产处置单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetDisposalEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetDisposalEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产处置单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }

        assetDisposalDetailService.lambdaUpdate().set(AssetDisposalDetailEntity::getIsDeleted, true)
                .eq(AssetDisposalDetailEntity::getMainId, id)
                .update();

        assetDisposalPhysicalDetailService.lambdaUpdate().set(AssetDisposalPhysicalDetailEntity::getIsDeleted, true)
                .eq(AssetDisposalPhysicalDetailEntity::getMainId, id)
                .update();

        // 删除主单数据
        log.info("删除 开始删除资产处置单主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除资产处置单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getCode(), "删除资产处置单主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetDisposalEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产处置单主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if (!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产处置单主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetDisposalEntity::getId, id)
                .set(AssetDisposalEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(AssetDisposalEntity::getInvalidRemark, remark)
                .set(AssetDisposalEntity::getInvalidTime, LocalDateTime.now())
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetDisposalEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产处置单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改资产处置单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产处置单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_DISPOSAL.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_DISPOSAL.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetDisposalEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }


    @Override
    public AssetDisposalDTO.ViewDTO view(String id) {
        AssetDisposalEntity assetDisposalEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产处置单主单数据"));
        AssetDisposalDTO.ViewDTO data = BeanMapperUtils.map(AssetDisposalDTO.ViewDTO.class, assetDisposalEntity);

        // 数据填充处理
        fillOne(data);

        //查询明细数据
        List<AssetDisposalDetailDTO.ViewDTO> detial = assetDisposalDetailService.listByMainId(id);
        data.setAssetDisposalDetailDTOList(detial);
        return data;
    }

    private void fillOne(AssetDisposalDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setDisposalMethodName(AssetDisposalDisposalMethodEnum.getName(data.getDisposalMethod()));
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(AssetDisposalEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_DISPOSAL.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }


    @Override
    public Map<String, Object> getVariablesMap(AssetDisposalEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.ASSET_DISPOSAL.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
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
        this.lambdaUpdate().eq(AssetDisposalEntity::getId, id)
                .set(AssetDisposalEntity::getApproveUserId, userInfo.getUid())
                .set(AssetDisposalEntity::getApproveUserName, userInfo.getUserName())
                .set(AssetDisposalEntity::getApproveStatus, approveStatus)
                .set(AssetDisposalEntity::getApproveTime, LocalDateTime.now())
                .update(new AssetDisposalEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetDisposalEntity::getId, id)
                .set(AssetDisposalEntity::getApproveUserId, "")
                .set(AssetDisposalEntity::getApproveUserName, "")
                .set(AssetDisposalEntity::getApproveStatus, approveStatus)
                .set(AssetDisposalEntity::getApproveTime, null)
                .update(new AssetDisposalEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetDisposalEntity::getId, id)
                .set(AssetDisposalEntity::getApproveStatus, approveStatus)
                .update(new AssetDisposalEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(AssetDisposalEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(AssetDisposalEntity assetDisposalEntity) {
        // TODO 验证数据 & 数据赋值
    }


    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("资产处置单导入", IMPORT_FMS_ASSET_DISPOSAL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetDisposal(BaseDTO.ImportDTO dto) {
        //处置方式
        AssetDisposalDisposalMethodEnum[] values = AssetDisposalDisposalMethodEnum.values();
        //组织
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        Map<String, String> companyMap = companyList.stream().collect(Collectors.toMap(BaseIdDTO::getName, BaseIdDTO::getId, (o1, o2) -> o1));
        //币种
        List<DictCurrencyEntity> dictCurrencyEntities = sysUserFeign.currencyList();
        //资产位置
        List<AssetLocationDTO.DropDownDTO> assetLocationEntities = assetLocationService.dropDownList("");
        Map<String, String> assetLocationMap = assetLocationEntities.stream().collect(Collectors.toMap(AssetLocationDTO.DropDownDTO::getAddress, AssetLocationDTO.DropDownDTO::getId, (o1, o2) -> o1));

        //资产卡片
        List<AssetCardEntity> assetCardEntities = assetCardService.lambdaQuery().eq(AssetCardEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getCode()).list();
        Map<String, String> assetCardMap = assetCardEntities.stream().collect(Collectors.toMap(AssetCardEntity::getAssetCode, AssetCardEntity::getId, (o1, o2) -> o1));

        List<AssetCardDetailEntity> assetCardDetailEntities = assetCardDetailService.list();
        Map<String, List<AssetCardDetailEntity>> assetCardDetailMap = assetCardDetailEntities.stream().collect(Collectors.groupingBy(AssetCardDetailEntity::getMainId));


        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        AssetDisposalExcelListener excelListenerUtil = new AssetDisposalExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),companyMap,dictCurrencyEntities,assetLocationMap,assetCardMap,assetCardDetailMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetDisposalImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetDisposalImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "资产处置单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetDisposalImportExcelDTO.class);
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

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<AssetDisposalImportExcelDTO> successList, List<String> errorNoList, List<AssetDisposalImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<AssetDisposalImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        AssetDisposalServiceImpl bean = ApplicationContextUtils.getBean(AssetDisposalServiceImpl.class);
        //按序号分组
        Map<String, List<AssetDisposalImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(AssetDisposalImportExcelDTO::getNo));
        for (Map.Entry<String, List<AssetDisposalImportExcelDTO>> entry : collect.entrySet()) {
            List<AssetDisposalImportExcelDTO> value = entry.getValue();
            AssetDisposalImportExcelDTO importMainDTO = value.get(0);
            AssetDisposalDTO.AddDTO addDTO = new AssetDisposalDTO.AddDTO();
            BeanMapper.copy(importMainDTO,addDTO);

            //资产明细
            List<AssetDisposalDetailDTO.UpdateDTO> assetDisposalDetailDTOList = BeanMapper.copyList(value, AssetDisposalDetailDTO.UpdateDTO.class);

            //value再按卡片编码进行分组
            Map<String, List<AssetDisposalImportExcelDTO>> detailCollect = value.stream().collect(Collectors.groupingBy(AssetDisposalImportExcelDTO::getSourceCode));

            //遍历资产明细
            for (AssetDisposalDetailDTO.UpdateDTO disposalImportExcelDTO : assetDisposalDetailDTOList) {
                disposalImportExcelDTO.setAssetDisposalPhysicalDetailDTOList(BeanMapper.copyList(detailCollect.get(disposalImportExcelDTO.getSourceCode()), AssetDisposalPhysicalDetailDTO.UpdateDTO.class));
            }

            addDTO.setAssetDisposalDetailDTOList(assetDisposalDetailDTOList);

            //执行新增
            bean.add(addDTO);
        }
    }
}

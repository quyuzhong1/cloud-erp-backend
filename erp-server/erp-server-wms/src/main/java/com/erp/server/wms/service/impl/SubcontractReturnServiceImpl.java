package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.ApproveType;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SubcontractReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractReturnMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SUBCONTRACT_RETURN;

/**
 * <p>
 * 委外退料单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@Service
public class SubcontractReturnServiceImpl extends SuperServiceImpl<SubcontractReturnMapper, SubcontractReturnEntity> implements SubcontractReturnService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SubcontractReturnDetailService subcontractReturnDetailService;
    @Resource
    private SubcontractIssueDetailService subcontractIssueDetailService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractReturnDTO.AddDTO addDTO) {
        SubcontractReturnEntity subcontractReturnEntity = new SubcontractReturnEntity();
        BeanMapperUtils.copy(addDTO, subcontractReturnEntity);


        //来源为空时（界面新增），验证供应商是否一致
        if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(addDTO.getSourceType()) ||  CharSequenceUtil.isBlank(addDTO.getSourceType())) {
            List<SubcontractReturnDetailDTO.AddDTO> detailList = addDTO.getDetailList();
            List<String> sourceDetailIdList = detailList.stream().map(SubcontractReturnDetailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
            checkSupplier(subcontractReturnEntity,sourceDetailIdList);
        }

        // 数据处理
        handleData(subcontractReturnEntity);

        log.info("开始新增委外退料单");
        // 生成单号
        if(CharSequenceUtil.isBlank(subcontractReturnEntity.getCode())){
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TLD);
            subcontractReturnEntity.setCode(code);
        }
        boolean save = super.save(subcontractReturnEntity);
        if(!save) {
            throw new ServiceException("委外退料单保存失败");
        }
        //新增明细
        subcontractReturnDetailService.add(addDTO.getDetailList(),subcontractReturnEntity.getId());
        // 操作日志
        String msg = null;
        if (CharSequenceUtil.equals(SourceTypeEnum.SUBCONTRACT_ORDER.getCode(),subcontractReturnEntity.getSourceType())){
            msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料单" , subcontractReturnEntity.getCode());
        }else if (CharSequenceUtil.equals(SourceTypeEnum.PO_RETURN.getCode(),subcontractReturnEntity.getSourceType())){
            //TODO 增加子件和成品退货单记录
            msg = CharSequenceUtil.format("用户【{}】从成品采购退货单【{}】下推生成了委外退料单【{}】", UserContext.getDefaultLoginUser().getUserName(), subcontractReturnEntity.getSourceCode(), subcontractReturnEntity.getCode());
        }
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), subcontractReturnEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(subcontractReturnEntity.getId(), subcontractReturnEntity.getCode());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractReturnDTO.UpdateDTO updateDTO) {
        SubcontractReturnEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "委外退料单");
        }
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        if (SourceTypeEnum.SELF_ADD.getCode().equals(old.getType()) && CharSequenceUtil.isNotBlank(old.getSourceCode())){
            throw new ServiceException(CharSequenceUtil.format("由采购退货单【{}】自动生成的委外退料单【{}】不支持编辑", old.getSourceCode(), old.getCode()));
        }
        SubcontractReturnEntity subcontractReturnEntity =  BeanMapperUtils.map(SubcontractReturnEntity.class, updateDTO);

        //来源为委外时，验证供应商是否一致
        if (CharSequenceUtil.equals(SourceTypeEnum.SUBCONTRACT_ORDER.getCode(),old.getSourceType()) ||  CharSequenceUtil.isBlank(old.getSourceType())) {
            List<SubcontractReturnDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
            List<String> sourceDetailIdList = detailList.stream().map(SubcontractReturnDetailDTO.UpdateDTO::getSourceDetailId).collect(Collectors.toList());
            checkSupplier(subcontractReturnEntity,sourceDetailIdList);
        }
        subcontractReturnEntity.setSourceType(old.getSourceType());
        // 数据处理
        handleData(subcontractReturnEntity);
        log.info("编辑 开始修改委外退料单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractReturnEntity);
        if(!save) {
            throw new ServiceException("委外退料单保存失败");
        }
        //修改明细
        subcontractReturnDetailService.update(updateDTO.getDetailList(),subcontractReturnEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录委外退料单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "委外退料单");
        operateLogService.addModuleOperateLogByObj(old, subcontractReturnEntity, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), subcontractReturnEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SubcontractReturnDTO.ListDTO> paging(PagingDTO<SubcontractReturnDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractReturnDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractReturnDTO.TabListDTO> tabList(PermissionsDTO param) {
        SubcontractReturnDTO.PagingParamDTO searchParam = new SubcontractReturnDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SubcontractReturnDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<SubcontractReturnDTO.TabListDTO> tabListDTOList = new ArrayList<>();
        tabListDTOList.add(new SubcontractReturnDTO.TabListDTO(ApproveStatusEnum.WAIT_SUBMIT.getStatus(),ApproveStatusEnum.WAIT_SUBMIT.getTableName(),getCountByStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus(),list)));
        tabListDTOList.add(new SubcontractReturnDTO.TabListDTO(ApproveStatusEnum.APPROVE_ING.getStatus(),ApproveStatusEnum.APPROVE_ING.getTableName(),getCountByStatus(ApproveStatusEnum.APPROVE_ING.getStatus(),list)));
        tabListDTOList.add(new SubcontractReturnDTO.TabListDTO(ApproveStatusEnum.APPROVE.getStatus(),ApproveStatusEnum.APPROVE.getTableName(),getCountByStatus(ApproveStatusEnum.APPROVE.getStatus(),list)));
        tabListDTOList.add(new SubcontractReturnDTO.TabListDTO(ApproveStatusEnum.REJECT.getStatus(),ApproveStatusEnum.REJECT.getTableName(),getCountByStatus(ApproveStatusEnum.REJECT.getStatus(),list)));
        // 计算合计数量
        return tabListDTOList;
    }

    private Integer getCountByStatus(String status, List<SubcontractReturnDTO.TabListDTO> list) {
        if (CollUtil.isEmpty(list) || CharSequenceUtil.isBlank(status)){
            return MathUtil.ZERO;
        }
        SubcontractReturnDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)){
            return Objects.nonNull(tabListDTO.getCount())? tabListDTO.getCount() :MathUtil.ZERO;
        }else {
            return MathUtil.ZERO;
        }
    }

    @Override
    public void exportList(SubcontractReturnDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("委外退料单导出", EXPORT_WMS_SUBCONTRACT_RETURN.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SubcontractReturnEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到委外退料单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改委外退料单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        //启动流程（如果需要的话）
//        log.info("提交 开始启动委外退料单流程，id=：【{}】", entity.getId());
//        if (isProcess){
//            startProcess(entity);
//        }
        // 记录操作日志
        log.info("提交 开始记录委外退料单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SubcontractReturnDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractReturnDTO.UpdateDTO dto) {
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
        SubcontractReturnEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SubcontractReturnEntity entity, ApproveOneDTO dto) {
//        LoginUser userInfo = UserContext.getDefaultLoginUser();
//        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
//        approveDTO.setBusinessId(entity.getId());
//        approveDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_RETURN.getCode());
//        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
//        approveDTO.setComment(dto.getComment());
//        approveDTO.setUserId(userInfo.getUid());
//        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
//        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
//        Integer code = approveResult.getCode();
//        if (200 != code) {
//            throw new ServiceException(ApiError.ERROR_94006);
//        }
//        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
//        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
//        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        // 审核完成自动退料扣库存
        inventoryTransCoreService.unApprove(new InventoryUnApproveDTO(InventorySourceTypeEnum.RETURN_MATERIAL, entity.getId()));
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SubcontractReturnEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除委外退料单主单数据，id：【{}】", id);
        super.removeById(id);
        //删除明细数据（如果有明细数据的话）
        subcontractReturnDetailService.deleteByMainId(id);
        // 删除日志数据
        log.info("删除 开始删除委外退料单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getCode(), "删除委外退料单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 待提交或审核不通过
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改委外退料单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SubcontractReturnEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SubcontractReturnEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到委外退料单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        log.info("撤销 开始修改委外退料单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委外退料单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), entity.getId(), "取消流程操作");
//        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
//        revokeDTO.setBusinessId(entity.getId());
//        revokeDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_RETURN.getCode());
//        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
//        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SubcontractReturnEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        if (dto.getType().equals(ApproveType.PASS)) {
            // 审核完成自动退料扣库存
            autoOutStockInventory(entity);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SubcontractReturnDTO.SubcontractDetailListDTO> listSubcontractDetail(SubcontractReturnDTO.DetailPagingParamDTO dto) {
        List<SubcontractReturnDTO.SubcontractDetailListDTO> resultList = new ArrayList<>();
        //委外订单id
        String sourceId = dto.getSourceId();
        List<SubcontractOrderEntity> subcontractOrderList = scmTaskFeign.listSubcontractOrderByIds(Collections.singletonList(sourceId));
        if (CollUtil.isEmpty(subcontractOrderList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        //委外明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(sourceId));
        if (CollUtil.isEmpty(subcontractOrderDetailList)) {
            throw  new ServiceException(ApiError.ERROR_98070);
        }
        //委外父级SKU明细
        List<SubcontractOrderDetailEntity> parentList = subcontractOrderDetailList.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getParentId()) && (CollUtil.isEmpty(dto.getSkuNoList()) ? Boolean.TRUE : dto.getSkuNoList().contains(obj.getSkuNo()))).collect(Collectors.toList());
        if (CollUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }

        //bom信息
        List<String> parentSkuIdList = parentList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIdList);

        //委外子级SKU明细
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98072);
        }
        //已审核退料数量
        List<String> subcontractOrderDetailIdList = childList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        List<SubcontractIssueDetailEntity> hasIssueDetailList = subcontractIssueDetailService.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);
        //已审核退料数量
//        List<String> subcontractOrderDetailIdList = childList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        List<SubcontractReturnDetailEntity> hasReturnDetailList = subcontractReturnDetailService.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);

        //sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuCategoryByIds(parentSkuIdList);

        //即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSubDetailSkuInventoryList(childList);

        for (SubcontractOrderDetailEntity parentDetailEntity : parentList) {
            SubcontractReturnDTO.SubcontractDetailListDTO detailListDTO = new SubcontractReturnDTO.SubcontractDetailListDTO();
            detailListDTO.setSkuId(parentDetailEntity.getSkuId());
            detailListDTO.setSkuNo(parentDetailEntity.getSkuNo());
            detailListDTO.setSourceId(subcontractOrderList.get(0).getId());
            detailListDTO.setSourceDetailId(parentDetailEntity.getId());
            detailListDTO.setSubcontractOrderId(subcontractOrderList.get(0).getId());
            detailListDTO.setParentSourceDetailId(parentDetailEntity.getId());
            detailListDTO.setSubcontractOrderDetailId(parentDetailEntity.getId());
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuId().equals(parentDetailEntity.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95010);
            }
            detailListDTO.setCategoryName(skuVO.getCategoryName());
            detailListDTO.setBrandName(skuVO.getBrandName());
            detailListDTO.setProductName(skuVO.getSkuName());
            detailListDTO.setSpuNo(skuVO.getSpuNo());
            detailListDTO.setStatusName(ProductDetailStatusEnum.getName(skuVO.getStatus()));
            detailListDTO.setImagesUrl(skuVO.getSkuImagesUrl());
            detailListDTO.setSupplierId(parentDetailEntity.getSupplierId());
            detailListDTO.setSupplierName(parentDetailEntity.getSupplierName());
            List<SubcontractOrderDetailEntity> childEntityList = childList.stream().filter(obj -> obj.getParentId().equals(parentDetailEntity.getId())).collect(Collectors.toList());
            List<SubcontractReturnDetailDTO.ListSourceDetailDTO> detailList = new ArrayList<>();
            for (SubcontractOrderDetailEntity  childDetailEntity : childEntityList) {
                SubcontractReturnDetailDTO.ListSourceDetailDTO detailDTO = new SubcontractReturnDetailDTO.ListSourceDetailDTO();
                detailDTO.setSourceId(subcontractOrderList.get(0).getId());
                detailDTO.setSubcontractOrderId(subcontractOrderList.get(0).getId());
                detailDTO.setSourceDetailId(childDetailEntity.getId());
                detailDTO.setSubcontractOrderDetailId(childDetailEntity.getId());
                detailDTO.setParentSourceDetailId(parentDetailEntity.getId());
                detailDTO.setParentSkuId(parentDetailEntity.getSkuId());
                detailDTO.setParentSkuNo(parentDetailEntity.getSkuNo());
                detailDTO.setSkuId(childDetailEntity.getSkuId());
                detailDTO.setSkuNo(childDetailEntity.getSkuNo());
                detailDTO.setWarehouseId(childDetailEntity.getWarehouseId());
                detailDTO.setWarehouseName(childDetailEntity.getWarehouseName());
                detailDTO.setReceiveQty(childDetailEntity.getDeliveryQty());
                detailDTO.setWarehouseLocation(childDetailEntity.getWarehouseLocation());
                WarehouseLocationEntity warehouseLocation = warehouseLocationService.findByWarehouseIdAndCode(childDetailEntity.getWarehouseId(), childDetailEntity.getWarehouseLocation());
                if (Objects.nonNull(warehouseLocation)){
                    detailDTO.setWarehouseLocationName(warehouseLocation.getName());
                }
                //bom信息
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(parentDetailEntity.getSkuId()) && obj.getSkuId().equals(childDetailEntity.getSkuId()))
                        .findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                detailDTO.setProductName(bomChildrenSkuDTO.getSkuName());
                detailDTO.setBomVersion(bomChildrenSkuDTO.getBomVersion());
                detailDTO.setQuantity(bomChildrenSkuDTO.getQuantity());

                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(detailDTO.getSkuId())
                                && s.getWarehouseId().equals(detailDTO.getWarehouseId())
                                && (CharSequenceUtil.isBlank(childDetailEntity.getWarehouseLocation()) ? Boolean.TRUE : childDetailEntity.getWarehouseLocation().equals(s.getWarehouseLocationId())))
                        .mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
                detailDTO.setCurInventoryQty(curInventoryQty);

                //已退料数量
                Integer hasIssueQty = hasIssueDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(detailDTO.getSubcontractOrderDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                        .map(SubcontractIssueDetailEntity::getIssueQty).reduce(MathUtil.ZERO, Integer::sum);
                detailDTO.setHasIssueQty(hasIssueQty);
                //已退料数量  hasReturnDetailList
                Integer hasReturnQty = hasReturnDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(detailDTO.getSubcontractOrderDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                        .map(SubcontractReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                detailDTO.setHasReturnQty(hasReturnQty);
                detailDTO.setMaxReturnQty(hasIssueQty - hasReturnQty);
                detailList.add(detailDTO);
            }
            detailListDTO.setDetailList(detailList);
            resultList.add(detailListDTO);
        }
        return resultList;
    }

    @Override
    public List<SubcontractReturnEntity> listBySourceIds(List<String> sourceIds) {
        if (CollUtil.isEmpty(sourceIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(SubcontractReturnEntity::getSourceId,sourceIds).list();
    }

    @Override
    public SubcontractReturnDTO.ViewDTO view(String id) {
        SubcontractReturnEntity subcontractReturnEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外退料单数据"));
        SubcontractReturnDTO.ViewDTO data = BeanMapperUtils.map(SubcontractReturnDTO.ViewDTO.class, subcontractReturnEntity);
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

    public void startProcess(SubcontractReturnEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SUBCONTRACT_RETURN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SubcontractReturnDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //状态名称
        data.setApproveStatusName(data.getApproveStatus().getName());
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

        //退料类型
        data.setTypeName(SubcontractReturnTypeEnum.NORMAL.getName());

        //委外退料明细
        List<SubcontractReturnDetailEntity> subcontractReturnDetailList = subcontractReturnDetailService.listByMainIds(Collections.singletonList(data.getId()));
        if (CollUtil.isEmpty(subcontractReturnDetailList)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_DETAIL_NOT_EXIST);
        }

        //产品信息
        List<String> skuIdList = subcontractReturnDetailList.stream().map(SubcontractReturnDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = subcontractReturnDetailList.stream()
                .map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        //即时库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSubReturnSkuInventoryList(subcontractReturnDetailList);

        //已审核退料数量
        List<String> subDetailIdList = subcontractReturnDetailList.stream().map(SubcontractReturnDetailEntity::getSubcontractOrderDetailId).collect(Collectors.toList());
        List<SubcontractReturnDetailEntity> hasReturnDetailList = subcontractReturnDetailService.listBySubcontractOrderDetailIdList(subDetailIdList);
        //已审核发料数量
        List<SubcontractIssueDetailEntity> hasIssueDetailList = subcontractIssueDetailService.listBySubcontractOrderDetailIdList(subDetailIdList);

        //委外明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByIds(subDetailIdList);

        List<SubcontractReturnDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(SubcontractReturnDetailDTO.ViewDTO.class, subcontractReturnDetailList);
        for (SubcontractReturnDetailDTO.ViewDTO viewDTO : detailList) {
            //仓位名称
            String warehouseLocationName = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(viewDTO.getWarehouseId()) && obj.getCode().equals(viewDTO.getWarehouseLocation()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setWarehouseLocationName(warehouseLocationName);
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewDTO.setProductName(skuVO.getSkuName());
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(viewDTO.getSkuId())
                            && s.getWarehouseId().equals(viewDTO.getWarehouseId())
                            && (CharSequenceUtil.isBlank(viewDTO.getWarehouseLocation()) ? Boolean.TRUE : viewDTO.getWarehouseLocation().equals(s.getWarehouseLocationId())))
                    .mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
            viewDTO.setCurInventoryQty(curInventoryQty);
            //已退料数量
            Integer hasReturnQty = hasReturnDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(viewDTO.getSubcontractOrderDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                    .map(SubcontractReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            viewDTO.setHasReturnQty(hasReturnQty);
            //已发料数量
            Integer hasIssueQty = hasIssueDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(viewDTO.getSubcontractOrderDetailId()) && ApproveStatusEnum.APPROVE.getCode().equals(obj.getApproveStatus()))
                    .map(SubcontractIssueDetailEntity::getIssueQty).reduce(MathUtil.ZERO, Integer::sum);
            viewDTO.setHasIssueQty(hasIssueQty);
            //最大可退数量
            viewDTO.setMaxReturnQty(hasIssueQty - hasReturnQty);
            //委外明细父级来源id
            String parentSourceDetailId = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(viewDTO.getSubcontractOrderDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getParentId())).orElse("");
            viewDTO.setParentSourceDetailId(parentSourceDetailId);
        }
        data.setDetailList(detailList);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractReturnEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
            .set(SubcontractReturnEntity::getApproveTime, LocalDateTime.now())
            .update(new SubcontractReturnEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
            .set(SubcontractReturnEntity::getApproveUserId, "")
            .set(SubcontractReturnEntity::getApproveUserName, "")
            .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
            .set(SubcontractReturnEntity::getApproveTime, null)
            .update(new SubcontractReturnEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SubcontractReturnEntity::getId, id)
        .set(SubcontractReturnEntity::getApproveStatus, approveStatus)
        .update(new SubcontractReturnEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractReturnDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(SubcontractReturnDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = list.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(paramList);

        // 属性赋值
        for(SubcontractReturnDTO.ListDTO data : list) {
            //状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            //仓位信息
            String warehouseLocationName = warehouseLocationList.stream().filter(obj -> obj.getWarehouseId().equals(data.getWarehouseId()) && obj.getCode().equals(data.getWarehouseLocation()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setWarehouseLocationName(warehouseLocationName);
            //产品信息
            String productName = skuVOList.stream().filter(obj -> obj.getSkuId().equals(data.getSkuId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            data.setProductName(productName);
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SubcontractReturnEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractReturnEntity subcontractReturnEntity) {
        if (CharSequenceUtil.isBlank(subcontractReturnEntity.getSourceType())) {
            //页面新增时默认来源类型
            subcontractReturnEntity.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
        }
        //默认手动新增
        if (CharSequenceUtil.isBlank(subcontractReturnEntity.getType())){
            subcontractReturnEntity.setType(SourceTypeEnum.SELF_ADD.getCode());
        }
        //委外订单
        List<SubcontractOrderEntity> subcontractOrderList = scmTaskFeign.listSubcontractOrderByIds(Collections.singletonList(subcontractReturnEntity.getSubcontractOrderId()));
        if (CollUtil.isEmpty(subcontractOrderList)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        subcontractReturnEntity.setSubcontractOrderCode(subcontractOrderList.get(0).getCode());
        //来源单号为委外订单时
        if (CharSequenceUtil.equals(subcontractReturnEntity.getSourceType(),SourceTypeEnum.PO_RETURN.getCode())) {
            if (CharSequenceUtil.isNotBlank(subcontractReturnEntity.getSourceId()) && CharSequenceUtil.isBlank(subcontractReturnEntity.getSourceCode())){
                PoReturnEntity poReturnEntity = poReturnService.getById(subcontractReturnEntity.getSourceId());
                if (Objects.nonNull(poReturnEntity)){
                    subcontractReturnEntity.setSourceCode(poReturnEntity.getCode());
                }
            }
        }
        //供应商
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(subcontractReturnEntity.getSupplierId());
        if (ObjectUtil.isNotEmpty(supplierEntity)) {
            subcontractReturnEntity.setSupplierName(supplierEntity.getName());
        }
    }

    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSubDetailSkuInventoryList(List<SubcontractOrderDetailEntity> childList) {
        //skuId集合
        List<String> skuIdList = childList.stream().map(SubcontractOrderDetailEntity::getSkuId)
                .distinct().collect(Collectors.toList());
        //仓库Id集合
        List<String> warehouseIdList = childList.stream().map(SubcontractOrderDetailEntity::getWarehouseId)
                .distinct().collect(Collectors.toList());
        //仓位集合
        List<String> warehouseLocationList = childList.stream().map(SubcontractOrderDetailEntity::getWarehouseLocation)
                .distinct().collect(Collectors.toList());
        return listSkuInventoryTotalList(skuIdList,warehouseIdList,warehouseLocationList);
    }
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryTotalList(List<String> skuIdList,List<String> warehouseIdList
            ,List<String> warehouseLocationList) {
        InventoryQtyDTO.SkuInventoryParamDTO paramDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        paramDTO.setWarehouseLocationIdList(warehouseLocationList);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryService.listSkuInventory(paramDTO);
        return skuInventoryTotalList;
    }
    /**
     * @description: 检测供应商是否一致
     * @author zdy
     * @date: 2024/1/27 14:43
     * @param subcontractReturnEntity
     */
    private void checkSupplier (SubcontractReturnEntity subcontractReturnEntity, List<String> sourceDetailIdList) {
        //委外订单下全部明细
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByMainIds(Collections.singletonList(subcontractReturnEntity.getSourceId()));
        //父级id集合
        List<String> parentIdList = subcontractOrderDetailList.stream().filter(obj -> sourceDetailIdList.contains(obj.getId())).map(SubcontractOrderDetailEntity::getParentId).collect(Collectors.toList());
        //所有父级数据
        List<SubcontractOrderDetailEntity> parentDetailList = subcontractOrderDetailList.stream().filter(obj -> parentIdList.contains(obj.getId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(parentDetailList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }
        //录入单据父级SKU供应商需要一致
        long count = parentDetailList.stream().map(SubcontractOrderDetailEntity::getSupplierId).distinct().count();
        if (count > 1) {
            String supplierNames = parentDetailList.stream().map(SubcontractOrderDetailEntity::getSupplierName).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_SUPPLIER_DIFF,supplierNames);
        }
        subcontractReturnEntity.setSupplierId(parentDetailList.get(0).getSupplierId());
        subcontractReturnEntity.setSupplierName(parentDetailList.get(0).getSupplierName());
    }

    /**
     * @description: 自动扣库存
     * @author Will
     * @date: 2024/1/29 16:10
     * @param entity
     */
    private void autoOutStockInventory (SubcontractReturnEntity entity) {
        //委外退料明细
        List<SubcontractReturnDetailEntity> subcontractReturnDetailList = subcontractReturnDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isEmpty(subcontractReturnDetailList)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_DETAIL_NOT_EXIST);
        }
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (SubcontractReturnDetailEntity detailEntity : subcontractReturnDetailList) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.RETURN_MATERIAL);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(LocalDate.now());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getReturnQty());
            inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //生成退料入库单，需要按比例出库（父级SKU入库数量/父级SKU采购数量）（现没有领料出库单据，则直接调用领料库存变化逻辑）
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SUBCONTRACT_RETURN_IN.getCode());
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }
    /**
     * @description: 查询详情获取即时库存
     * @author Will
     * @date: 2024/1/9 16:46
     * @param subcontractReturnDetailList
     * @return List<SkuInventoryTotalDTO>
     */
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSubReturnSkuInventoryList(List<SubcontractReturnDetailEntity> subcontractReturnDetailList) {
        //skuId集合
        List<String> skuIdList = subcontractReturnDetailList.stream().map(SubcontractReturnDetailEntity::getSkuId).distinct()
                .collect(Collectors.toList());
        //仓库Id集合
        List<String> warehouseIdList = subcontractReturnDetailList.stream().map(SubcontractReturnDetailEntity::getWarehouseId)
                .distinct().collect(Collectors.toList());
        //仓位集合
        List<String> warehouseLocationList = subcontractReturnDetailList.stream().map(SubcontractReturnDetailEntity::getWarehouseLocation)
                .distinct().collect(Collectors.toList());

        return listSkuInventoryTotalList(skuIdList,warehouseIdList,warehouseLocationList);
    }
}

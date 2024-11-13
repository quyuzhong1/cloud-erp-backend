package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoDeliveryNoticeChangeTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeMapper;
import com.erp.server.wms.service.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_DELIVERY_NOTICE_CHANGE;

/**
 * <p>
 * 发货通知变更单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
@Slf4j
@Service
public class SoDeliveryNoticeChangeServiceImpl extends SuperServiceImpl<SoDeliveryNoticeChangeMapper, SoDeliveryNoticeChangeEntity> implements SoDeliveryNoticeChangeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private SoDeliveryNoticeChangeDetailService detailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDTO.ViewDTO addDTO) {
        List<String> sourceDetailIds = addDTO.getViewDetailList().stream().map(SoDeliveryNoticeChangeDTO.ViewDetail::getSourceDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.checkExist(sourceDetailIds,addDTO.getNoticeId(), addDTO.getId());
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = this.buildEntity(addDTO);
        // 数据处理
        handleData(soDeliveryNoticeChangeEntity);
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHBG);
        soDeliveryNoticeChangeEntity.setCode(code);
        boolean save = super.save(soDeliveryNoticeChangeEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单保存失败");
        }
        detailService.add(addDTO,soDeliveryNoticeChangeEntity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货通知变更单" , soDeliveryNoticeChangeEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), soDeliveryNoticeChangeEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(soDeliveryNoticeChangeEntity.getId(), code);
    }

    private SoDeliveryNoticeChangeEntity buildEntity(SoDeliveryNoticeChangeDTO.ViewDTO addDTO) {
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = new SoDeliveryNoticeChangeEntity();
        soDeliveryNoticeChangeEntity.setSourceCode(addDTO.getNoticeCode());
        soDeliveryNoticeChangeEntity.setSourceId(addDTO.getNoticeId());
        soDeliveryNoticeChangeEntity.setSoCode(addDTO.getSoCode());
        soDeliveryNoticeChangeEntity.setSoId(addDTO.getSoId());
        soDeliveryNoticeChangeEntity.setCustomerId(addDTO.getCustomerId());
        soDeliveryNoticeChangeEntity.setCustomerName(addDTO.getCustomerName());
        soDeliveryNoticeChangeEntity.setChangeReason(addDTO.getChangeReason());
        return soDeliveryNoticeChangeEntity;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoDeliveryNoticeChangeDTO.ViewDTO updateDTO) {
        SoDeliveryNoticeChangeEntity old = super.getById(updateDTO.getId());
        List<String> sourceDetailIds = updateDTO.getViewDetailList().stream().map(SoDeliveryNoticeChangeDTO.ViewDetail::getSourceDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        this.checkExist(sourceDetailIds,old.getSourceId(), updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货通知变更单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity =  BeanMapperUtils.map(SoDeliveryNoticeChangeEntity.class, old);
        soDeliveryNoticeChangeEntity.setChangeReason(updateDTO.getChangeReason());

        log.info("编辑 开始修改发货通知变更单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soDeliveryNoticeChangeEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单保存失败");
        }
        detailService.update(updateDTO,soDeliveryNoticeChangeEntity);

        // 记录主单操作日志
        log.info("编辑 开始记录发货通知变更单日志数据，单号：【{}】", soDeliveryNoticeChangeEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soDeliveryNoticeChangeEntity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeChangeEntity, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), soDeliveryNoticeChangeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> paging(PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoDeliveryNoticeChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoDeliveryNoticeChangeDTO.PagingParamDTO searchParam = new SoDeliveryNoticeChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoDeliveryNoticeChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SoDeliveryNoticeChangeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SoDeliveryNoticeChangeDTO.TabListDTO(status,"", 0));
        }
        });
        list.forEach(v->{
            if(v.getTabFlag().equals(ApproveStatusEnum.WAIT_SUBMIT.getCode())){
                v.setTabFlagName("待提交");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.APPROVE_ING.getCode())){
                v.setTabFlagName("待审核");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.APPROVE.getCode())){
                v.setTabFlagName("审核通过");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.REJECT.getCode())){
                v.setTabFlagName("审核不通过");
            }
        });
        list.add(new SoDeliveryNoticeChangeDTO.TabListDTO("","全部", list.stream().mapToInt(SoDeliveryNoticeChangeDTO.TabListDTO::getCount).sum()));
        // 定义排序顺序
        Map<String, Integer> orderMap = new HashMap<>();
        orderMap.put(ApproveStatusEnum.WAIT_SUBMIT.getCode(), 0);
        orderMap.put(ApproveStatusEnum.APPROVE_ING.getCode(), 1);
        orderMap.put(ApproveStatusEnum.APPROVE.getCode(), 2);
        orderMap.put(ApproveStatusEnum.REJECT.getCode(), 3);

        // 排序
        list.sort((o1, o2) -> {
            Integer order1 = orderMap.getOrDefault(o1.getTabFlag(), 4); // 4 表示“全部”
            Integer order2 = orderMap.getOrDefault(o2.getTabFlag(), 4);
            return order1.compareTo(order2);
        });
        return list;
    }

    @Override
    public void exportList(SoDeliveryNoticeChangeDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("发货通知变更单导出", EXPORT_WMS_SO_DELIVERY_NOTICE_CHANGE.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SoDeliveryNoticeChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到发货通知变更单数据");
        }
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(entity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        if(!(ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                || ApproveStatusEnum.APPROVE_ING.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                || ApproveStatusEnum.REJECT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus()))){
            throw new ServiceException("【发货通知单】{} 状态只有待提交、待审核,审核不通过时允许提交",soDeliveryNoticeEntity.getCode());
        }
        validateSubmit(entity);
        SoDeliveryNoticeChangeDTO.ViewDTO viewDTO = this.view(new SoDeliveryNoticeChangeDTO.ViewIdDTO(id,new ArrayList<>(),"edit"));
        detailService.checkData(viewDTO.getViewDetailList());
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SoDeliveryNoticeChangeDTO.ViewDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SoDeliveryNoticeChangeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SoDeliveryNoticeChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(entity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        if(!(ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                || ApproveStatusEnum.APPROVE_ING.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                || ApproveStatusEnum.REJECT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus()))){
            throw new ServiceException("【发货通知单】{} 状态只有待提交、待审核,审核不通过时允许审核",soDeliveryNoticeEntity.getCode());
        }
        SoDeliveryNoticeChangeDTO.ViewDTO viewDTO = this.view(new SoDeliveryNoticeChangeDTO.ViewIdDTO(entity.getId(),new ArrayList<>(),"edit"));
        detailService.checkData(viewDTO.getViewDetailList());
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单", approveType.getName(), dto.getComment());

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SoDeliveryNoticeChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE_CHANGE.getCode());
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException("只有待提交或审核不通过数据支持作废");
        }
        detailService.removeByMainId(entity.getId());
        // 删除主单数据
        super.removeById(id);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getCode(), "删除发货通知变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getId(), "取消流程操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoDeliveryNoticeChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(entity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        if(!(soDeliveryNoticeEntity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || soDeliveryNoticeEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus()))) {
            throw new ServiceException("【发货通知单】{} 状态只有待提交、待审核时允许变更",soDeliveryNoticeEntity.getCode());
        }

        //查询发货通知单明细数据
        List<SoDeliveryNoticeDetailEntity> oldDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainId(soDeliveryNoticeEntity.getId());
        if (CollectionUtils.isEmpty(oldDeliveryNoticeDetailList)) {
            throw new ServiceException("未找到发货通知单明细数据");
        }

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        List<SoDeliveryNoticeChangeDetailEntity> detailList = detailService.listByMainId(entity.getId());
        changeNotice(entity,detailList);
        //虚拟库存变更
        virtualInventoryChange(entity,detailList,oldDeliveryNoticeDetailList);
        return Boolean.TRUE;
    }

    private void changeNotice(SoDeliveryNoticeChangeEntity entity, List<SoDeliveryNoticeChangeDetailEntity> detailList) {
        if(CollectionUtils.isEmpty(detailList)){
            return;
        }

        SoDeliveryNoticeEntity soDeliveryNotice = soDeliveryNoticeService.getByIdOpt(entity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailByMainId(soDeliveryNotice.getId());
        List<SoDeliveryNoticeChangeDetailEntity> sourceDetailList = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> addList = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> updateList = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> deleteList = new ArrayList<>();
        for (SoDeliveryNoticeChangeDetailEntity detail : detailList) {
            if(SoDeliveryNoticeChangeTypeEnum.ADD.getCode().equals(detail.getChangeType())){
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = BeanUtil.copyProperties(detail,SoDeliveryNoticeDetailEntity.class);
                soDeliveryNoticeDetailEntity.setMainId(soDeliveryNotice.getId());
                soDeliveryNoticeDetailEntity.setSourceDetailId(detail.getSoDetailId());
                soDeliveryNoticeDetailEntity.setDeliveryQty(detail.getNewQty());
                soDeliveryNoticeDetailEntity.setLastPickingQty(detail.getNewQty());
                soDeliveryNoticeDetailEntity.setId(IdWorker.getIdStr());
                addList.add(soDeliveryNoticeDetailEntity);
                //发货通知变更单明细
                detail.setSourceDetailId(soDeliveryNoticeDetailEntity.getId());
                sourceDetailList.add(detail);
            }else if (SoDeliveryNoticeChangeTypeEnum.UPDATE.getCode().equals(detail.getChangeType())){
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = soDeliveryNoticeDetailList.stream().filter(v -> v.getId().equals(detail.getSourceDetailId())).findFirst().orElseThrow(()->new ServiceException("{}未找到发货通知单明细数据",detail.getSkuNo()));
                soDeliveryNoticeDetailEntity.setChangeBeforeSkuNo(soDeliveryNoticeDetailEntity.getSkuNo());
                soDeliveryNoticeDetailEntity.setSkuId(detail.getSkuId());
                soDeliveryNoticeDetailEntity.setSkuNo(detail.getSkuNo());
                soDeliveryNoticeDetailEntity.setChangeBeforeQty(soDeliveryNoticeDetailEntity.getDeliveryQty());
                soDeliveryNoticeDetailEntity.setDeliveryQty(detail.getNewQty());
                soDeliveryNoticeDetailEntity.setLastPickingQty(detail.getNewQty());
                updateList.add(soDeliveryNoticeDetailEntity);
            }else if (SoDeliveryNoticeChangeTypeEnum.DELETE.getCode().equals(detail.getChangeType())){
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = soDeliveryNoticeDetailList.stream().filter(v -> v.getId().equals(detail.getSourceDetailId())).findFirst().orElseThrow(()->new ServiceException("{}未找到发货通知单明细数据",detail.getSkuNo()));
                deleteList.add(soDeliveryNoticeDetailEntity);
            }
        }
        soDeliveryNoticeService.updateByNoticeChange(addList,updateList,deleteList);

        //新增的数据回填销售通知变更单明细
        if(CollectionUtils.isNotEmpty(sourceDetailList)) {
            detailService.updateBatchById(sourceDetailList);
        }
    }

    @Override
    public PagingVO<SoDeliveryNoticeChangeDTO.ProductDTO> addProductPaging(PagingDTO<SoDeliveryNoticeChangeDTO.ProductAddDTO> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeChangeDTO.ProductDTO> pageData = this.baseMapper.productPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        List<String> skuIds = pageData.getRecords().stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        // 数据处理
        for (SoDeliveryNoticeChangeDTO.ProductDTO record : pageData.getRecords()) {
            record.setMaxCanChangeQty(record.getSaleQty() - record.getAllNoticeQty() + record.getCurrentNoticeQty());
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(record.getSkuId())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
        }
        List<String> skuNos = pagingParamDTO.getParams().getSkuNoList();
        if(CollUtil.isNotEmpty(skuNos)){
            List<SoDeliveryNoticeChangeDTO.ProductDTO> productDTOS = new ArrayList<>();
            for (String skuNo : skuNos) {
                SoDeliveryNoticeChangeDTO.ProductDTO productDTO = pageData.getRecords().stream().filter(v->v.getSkuNo().equals(skuNo)).findFirst().orElse(new SoDeliveryNoticeChangeDTO.ProductDTO());
                productDTOS.add(productDTO);
            }
            pageData.setRecords(productDTOS);
        }
        return new PagingVO(pageData);
    }

    @Override
    public BatchResultDTO invalid(String id) {
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有待提交数据允许作废
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), entity.getApproveStatus()) && !Objects.equals(ApproveStatusEnum.REJECT.getStatus(), entity.getApproveStatus())) {
            throw new ServiceException("只有待提交或审核不通过数据支持作废");
        }
        if(entity.getInvalidStatus()){
            throw new ServiceException("该数据已作废");
        }
        // 删除主单数据
        entity.setInvalidStatus(true);
        super.updateById(entity);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getCode(), "作废发货通知变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public List<SoDeliveryNoticeChangeDTO.ProductDTO> addProductPaste(SoDeliveryNoticeChangeDTO.ProductAddDTO dto) {
        Page query = new Page(1, Integer.MAX_VALUE);
        IPage<SoDeliveryNoticeChangeDTO.ProductDTO> pageData = this.baseMapper.productPaging(query, dto);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new ArrayList<>();
        }
        List<String> skuIds = pageData.getRecords().stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        // 数据处理
        for (SoDeliveryNoticeChangeDTO.ProductDTO record : pageData.getRecords()) {
            record.setMaxCanChangeQty(record.getSaleQty() - record.getAllNoticeQty() + record.getCurrentNoticeQty());
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(record.getSkuId())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
        }
        List<String> skuNos = dto.getSkuNoList();
        if(CollUtil.isNotEmpty(skuNos)){
            List<SoDeliveryNoticeChangeDTO.ProductDTO> productDTOS = new ArrayList<>();
            for (String skuNo : skuNos) {
                SoDeliveryNoticeChangeDTO.ProductDTO productDTO = pageData.getRecords().stream().filter(v->v.getSkuNo().equals(skuNo)).findFirst().orElse(new SoDeliveryNoticeChangeDTO.ProductDTO());
                productDTOS.add(productDTO);
            }
            pageData.setRecords(productDTOS);
        }
        return pageData.getRecords();
    }

    @Override
    public List<SoDeliveryNoticeChangeEntity> listNoApproveByNoticeId(String noticeId) {
        if(StringUtils.isBlank(noticeId)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().eq(SoDeliveryNoticeChangeEntity::getSourceId, noticeId).ne(SoDeliveryNoticeChangeEntity::getSourceId, noticeId).ne(SoDeliveryNoticeChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()).eq(SoDeliveryNoticeChangeEntity::getInvalidStatus,false).list();
    }

    @Override
    public SoDeliveryNoticeChangeDTO.ViewDTO view(SoDeliveryNoticeChangeDTO.ViewIdDTO viewIdDTO) {
        String type = viewIdDTO.getType();
        String id = viewIdDTO.getId();
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = new SoDeliveryNoticeChangeEntity();
        SoDeliveryNoticeEntity soDeliveryNoticeEntity;
        if("pushDown".equals(type)){
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
            if(!(ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                    || ApproveStatusEnum.APPROVE_ING.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                    || ApproveStatusEnum.REJECT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus()))){
                throw new ServiceException("只有待提交、待审核,审核不通过状态允许下推");
            }
        }else{
            soDeliveryNoticeChangeEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(soDeliveryNoticeChangeEntity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        }

        SoDeliveryNoticeDTO.View noticeView = soDeliveryNoticeService.view(soDeliveryNoticeEntity.getId());
        SoDeliveryNoticeChangeDTO.ViewDTO viewDTO = BeanUtil.toBean(noticeView,SoDeliveryNoticeChangeDTO.ViewDTO.class);
        viewDTO.setId(soDeliveryNoticeChangeEntity.getId());
        viewDTO.setNoticeId(soDeliveryNoticeEntity.getId());
        viewDTO.setCode(soDeliveryNoticeChangeEntity.getCode());
        viewDTO.setNoticeCode(soDeliveryNoticeEntity.getCode());
        viewDTO.setApproveStatus(soDeliveryNoticeChangeEntity.getApproveStatus());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(soDeliveryNoticeChangeEntity.getApproveStatus()));
        viewDTO.setSoId(soDeliveryNoticeEntity.getSourceId());
        viewDTO.setSoCode(soDeliveryNoticeEntity.getSourceCode());
        viewDTO.setChangeReason(soDeliveryNoticeChangeEntity.getChangeReason());
        if(StringUtil.isNotBlank(soDeliveryNoticeChangeEntity.getId())){
            List<SoDeliveryNoticeChangeDTO.ViewDetail> detailList = baseMapper.listViewDetailList(soDeliveryNoticeChangeEntity.getId());
            for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : detailList) {
                viewDetail.setChangeTypeName(SoDeliveryNoticeChangeTypeEnum.getName(viewDetail.getChangeType()));
                viewDetail.setMaxCanChangeQty(viewDetail.getSaleQty() - viewDetail.getAllNoticeQty() + viewDetail.getCurrentNoticeQty());
            }
            viewDTO.setViewDetailList(detailList);
        }
        if(CollectionUtils.isNotEmpty(viewIdDTO.getDetailIds())){
            Page query = new Page(1, Integer.MAX_VALUE);
            SoDeliveryNoticeChangeDTO.ProductAddDTO params = new SoDeliveryNoticeChangeDTO.ProductAddDTO();
            Map<String,String> map = new HashMap<>();
            map.put("default","1=1");
            params.setSqlMap(map);
            params.setDetailIds(viewIdDTO.getDetailIds());
            params.setSoId(soDeliveryNoticeEntity.getSourceId());
            params.setNoticeId(soDeliveryNoticeEntity.getId());
            IPage<SoDeliveryNoticeChangeDTO.ProductDTO> productPaging = this.baseMapper.productPaging(query, params);
            List<SoDeliveryNoticeChangeDTO.ViewDetail> detailList = BeanUtil.copyToList(productPaging.getRecords(),SoDeliveryNoticeChangeDTO.ViewDetail.class);
            List<String> skuNoList = detailList.stream().map(SoDeliveryNoticeChangeDTO.ViewDetail::getSkuNo).collect(Collectors.toList());
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);
            for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : detailList) {
                SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuNo().equals(viewDetail.getSkuNo())).findFirst().orElse(new SkuVO());
                viewDetail.setChangeType(SoDeliveryNoticeChangeTypeEnum.UPDATE.getCode());
                viewDetail.setChangeTypeName(SoDeliveryNoticeChangeTypeEnum.UPDATE.getName());
                viewDetail.setProductName(skuVO.getSkuName());
                viewDetail.setMaxCanChangeQty(viewDetail.getSaleQty() - viewDetail.getAllNoticeQty() + viewDetail.getCurrentNoticeQty());
            }
            viewDTO.setViewDetailList(detailList);
        }
        return viewDTO;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SoDeliveryNoticeChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE_CHANGE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SoDeliveryNoticeChangeDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(SoDeliveryNoticeChangeEntity::getId, id)
            .set(SoDeliveryNoticeChangeEntity::getApproveUserId, userInfo.getUid())
            .set(SoDeliveryNoticeChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(SoDeliveryNoticeChangeEntity::getApproveStatus, approveStatus)
            .set(SoDeliveryNoticeChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new SoDeliveryNoticeChangeEntity());
     }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoDeliveryNoticeChangeEntity::getId, id)
        .set(SoDeliveryNoticeChangeEntity::getApproveStatus, approveStatus)
        .update(new SoDeliveryNoticeChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SoDeliveryNoticeChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        List<String> soDetailIds = list.stream().map(v->v.getSoDetailId()).collect(Collectors.toList());
        List<String> sourceDetails = list.stream().map(v->v.getSourceDetailId()).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntityList = FeignQuery.getByIds(SoDetailEntity.class,soDetailIds);
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIds);
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(sourceDetails);
        List<String> ids = list.stream().map(SoDeliveryNoticeChangeDTO.ListDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_DELIVERY_NOTICE_CHANGE.getCode(), obj));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.Default.code, listApiResult.getMsg()));
            }
        }
        // 属性赋值
        for(SoDeliveryNoticeChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(v->v.getId().equals(data.getSoDetailId())).findFirst().orElse(new SoDetailEntity());
            data.setSaleQty(soDetailEntity.getQty());
            List<SoDeliveryNoticeDetailEntity> currentNoticeDetailList = soDeliveryNoticeDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(data.getSoDetailId())).collect(Collectors.toList());
            data.setAllNoticeQty(currentNoticeDetailList.stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(0, Integer::sum));
            data.setChangeTypeName(SoDeliveryNoticeChangeTypeEnum.getName(data.getChangeType()));
            List<PickingDetailEntity> currentPickingDetailList = pickingDetailEntityList.stream().filter(v->v.getSourceDetailId().equals(data.getSourceDetailId())).collect(Collectors.toList());
            data.setPickedQty(currentPickingDetailList.stream().map(PickingDetailEntity::getQty).reduce(0, Integer::sum));
            //最新待审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if(StringUtils.isNotBlank(curApprove)){
                    data.setApproveUserName(curApprove);
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SoDeliveryNoticeChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 更新虚拟仓库存
     * @author will
     * @date 2024/10/30 11:31
     * @param entity
     * @param detailList
     */
    private void virtualInventoryChange (SoDeliveryNoticeChangeEntity entity,List<SoDeliveryNoticeChangeDetailEntity> detailList,List<SoDeliveryNoticeDetailEntity> oldDeliveryNoticeDetailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        //发货通知单
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(entity.getSourceId());
        if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST);
        }
        //发货通知单明细
        List<String> soDetailIdList = detailList.stream().map(SoDeliveryNoticeChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeDetailService.listByIds(soDetailIdList);
        if (CollectionUtils.isEmpty(soDeliveryNoticeDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_DETAIL_NOT_EXIST);
        }
        //销售订单
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soDeliveryNoticeEntity.getSourceId());
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        //无虚拟仓不扣库存
        if (StrUtil.isBlank(soInfoEntity.getVirtualWarehouseId())) {
            return;
        }
        //销售订单明细
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> soParamList = new ArrayList<>();

        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> subParamList = new ArrayList<>();

        //发货通知单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> addNoticeParamList = new ArrayList<>();
        //发货通知单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> subNoticeParamList = new ArrayList<>();

        //销售订单冻结数量更新
        List<SoDetailDTO.UpdateFrozenQtyDTO> updateList = new ArrayList<>();
        /**
         * 1. 发货通知变更单审核通过后，发货通知单明细更新，此时虚拟仓库存跟随变动；
         *    三者需保持同步，要么全部成功、要么全部失败。
         * 2. 虚拟仓库存变更：
         *   - 多退少补：
         *     - 同 <拣货单数量修改--发货单审核后--虚拟仓多退少补> 实现一致。
         *     - 差异数量 = | 原虚拟仓冻结数量 - 变更后发货通知单数量 |
         *   - 类型 = 新增、类型 = 修改（增加），追加冻结：
         *     - 若此时B2B销售订单-冻结数量 ＞ 差异数量
         *       1. 从B2B销售订单上，扣减冻结+增加可用（取差异数量）；
         *       2. 到发货通知单上，增加可用+增加冻结（取差异数量）；
         *       3. 更新B2B销售订单的冻结数量 = 原冻结数量 - 差异数量；
         *     - 若此时B2B销售订单-冻结数量 ≤ 差异数量
         *       1. 从B2B销售订单上，扣减冻结+增加可用（取冻结数量）
         *       2. 到发货通知单上，扣减可用+增加冻结（取差异数量）；
         *       3. 更新B2B销售订单的冻结数量 = 0；
         *   - 类型 = 删除、类型 = 修改（减少），释放冻结：
         *     - 发货通知单上，扣减冻结、增加可用（差异数量）；
         */

        for (SoDeliveryNoticeChangeDetailEntity changeDetailEntity : detailList) {

            //销售通知单明细
            SoDeliveryNoticeDetailEntity detailEntity = soDeliveryNoticeDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), changeDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            //删除类型的明细需要从旧数据中查关联
            if (StrUtil.equals(SoDeliveryNoticeChangeTypeEnum.DELETE.getCode(),changeDetailEntity.getChangeType())) {
                detailEntity = oldDeliveryNoticeDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), changeDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            }

            if (ObjectUtil.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_DETAIL_NOT_EXIST);
            }
            //销售订单明细
            SoDeliveryNoticeDetailEntity finalDetailEntity = detailEntity;
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), finalDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }
            //删除类型时新数量设置为0
            Integer newQty = StrUtil.equals(SoDeliveryNoticeChangeTypeEnum.DELETE.getCode(),changeDetailEntity.getChangeType())
                    ? MathUtil.ZERO : changeDetailEntity.getNewQty();

            //差异数量
            Integer diffQty = newQty -  changeDetailEntity.getOriginQty();
            //销售订单追加冻结
            handleSoParam(soInfoEntity, soDetailEntity,soParamList,diffQty);
            //销售订单扣减冻结
            handleSubSoParam(soInfoEntity, soDetailEntity,subParamList,diffQty);
            //发货通知单参数
            handleSoDeliveryNoticeAddParam(soDeliveryNoticeEntity, detailEntity,addNoticeParamList,subNoticeParamList,diffQty);

            //更新冻结库存参数
            if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                SoDetailDTO.UpdateFrozenQtyDTO updateFrozenQtyDTO = new SoDetailDTO.UpdateFrozenQtyDTO();
                updateFrozenQtyDTO.setDetailId(soDetailEntity.getId());
                boolean isExceed = soDetailEntity.getFrozenQty() > diffQty;
                if (isExceed) {
                    updateFrozenQtyDTO.setFrozenQty(soDetailEntity.getFrozenQty() - diffQty);
                } else {
                    updateFrozenQtyDTO.setFrozenQty(MathUtil.ZERO);
                }
                updateList.add(updateFrozenQtyDTO);
            }
        }
        //销售订单扣减库存
        if (CollectionUtils.isNotEmpty(soParamList)) {
            //添加冻结，扣减可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(soParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //销售订单减冻结
        if (CollectionUtils.isNotEmpty(subParamList)) {
            //减冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(subParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_SUBTRACT_FREEZE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //发货通知单添加冻结
        if (CollectionUtils.isNotEmpty(addNoticeParamList)) {
            //添加冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(addNoticeParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_ADD.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //发货通知单减少冻结
        if (CollectionUtils.isNotEmpty(subNoticeParamList)) {
            //添加冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(subNoticeParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_APPROVE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //更新销售订单冻结数量
        soInfoFeign.updateFrozenQty(updateList);
    }

    /**
     * 销售订单追加冻结
     * @author will
     * @date 2024/10/30 11:22
     * @param soInfoEntity
     * @param soDetailEntity
     * @param soParamList
     * @param diffQty
     */
    private void handleSoParam(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity,List<VirtualInventoryStockDTO.OutInStockDTO> soParamList,Integer diffQty) {
        //差异数量大于冻结数量则需要添加冻结
        if (MathUtil.ZERO >= diffQty || soDetailEntity.getFrozenQty() >= diffQty) {
            return;
        }
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setQty(diffQty - soDetailEntity.getFrozenQty());
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        //库存数量为0不添加
        if (MathUtil.compareTo(outInStockDTO.getQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        soParamList.add(outInStockDTO);
    }

    /**
     * 销售订单扣减冻结
     * @author will
     * @date 2024/10/30 11:25
     * @param soInfoEntity
     * @param soDetailEntity
     * @param paramList
     * @param diffQty
     */
    private void handleSubSoParam(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity,List<VirtualInventoryStockDTO.OutInStockDTO> paramList,Integer diffQty) {
        if (MathUtil.ZERO >= diffQty) {
            return;
        }
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setQty(diffQty);
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        //库存数量为0不添加
        if (MathUtil.compareTo(outInStockDTO.getQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        paramList.add(outInStockDTO);
    }

    /**
     * 销售通知单添加冻结
     * @author will
     * @date 2024/10/30 11:28
     * @param soDeliveryNoticeEntity
     * @param detailEntity
     * @param addNoticeParamList
     * @param subNoticeParamList
     * @param diffQty
     */
    private void handleSoDeliveryNoticeAddParam(SoDeliveryNoticeEntity soDeliveryNoticeEntity,SoDeliveryNoticeDetailEntity detailEntity,
                                                List<VirtualInventoryStockDTO.OutInStockDTO> addNoticeParamList,List<VirtualInventoryStockDTO.OutInStockDTO> subNoticeParamList,Integer diffQty) {

        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
        outInStockDTO.setSourceId(soDeliveryNoticeEntity.getId());
        outInStockDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
        outInStockDTO.setSourceDetailId(detailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(detailEntity.getSkuId());
        outInStockDTO.setSkuNo(detailEntity.getSkuNo());
        outInStockDTO.setQty(Math.abs(diffQty));
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        //库存数量为0不添加
        if (MathUtil.compareTo(outInStockDTO.getQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return;
        }
        if (diffQty > MathUtil.ZERO) {
            addNoticeParamList.add(outInStockDTO);
        } else {
            subNoticeParamList.add(outInStockDTO);
        }
    }

    private void checkExist(List<String> sourceDetailIds, String noticeId, String id){
        if(CollectionUtils.isEmpty(sourceDetailIds) || StringUtils.isBlank(noticeId)){
            return;
        }
        List<SoDeliveryNoticeChangeEntity> exist = this.lambdaQuery().ne(StringUtils.isNotBlank(id),SoDeliveryNoticeChangeEntity::getId,id).eq(SoDeliveryNoticeChangeEntity::getSourceId, noticeId).ne(SoDeliveryNoticeChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()).eq(SoDeliveryNoticeChangeEntity::getInvalidStatus,false).list();
        if(CollectionUtils.isEmpty(exist)){
            return;
        }
        List<String> mainIds = exist.stream().map(v->v.getId()).collect(Collectors.toList());
        List<SoDeliveryNoticeChangeDetailEntity> allExistDetailList = detailService.lambdaQuery().in(SoDeliveryNoticeChangeDetailEntity::getMainId,mainIds).in(SoDeliveryNoticeChangeDetailEntity::getSourceDetailId,sourceDetailIds).list();
        if(CollectionUtils.isNotEmpty(allExistDetailList)){
            throw new ServiceException("发货通知单存在未审核且未作废变更单，请勿重复提交");
        }
    }
}

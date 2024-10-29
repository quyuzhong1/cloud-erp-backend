package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.enums.SoDeliveryNoticeChangeTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDTO.ViewDTO addDTO) {
        SoDeliveryNoticeChangeEntity exist = this.lambdaQuery().eq(SoDeliveryNoticeChangeEntity::getSourceId, addDTO.getNoticeId()).ne(SoDeliveryNoticeChangeEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode()).eq(SoDeliveryNoticeChangeEntity::getInvalidStatus,false).last("limit 1").one();
        if(Objects.nonNull(exist)){
            throw new ServiceException("发货通知单存在未审核且未作废变更单，请勿重复提交");
        }
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
        operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeChangeEntity, null, soDeliveryNoticeChangeEntity.getId(), msg);
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
                v.setTabFlagName("待我审核");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.APPROVE.getCode())){
                v.setTabFlagName("审核通过");
            }
            if(v.getTabFlag().equals(ApproveStatusEnum.REJECT.getCode())){
                v.setTabFlagName("审核不通过");
            }
        });
        list.add(new SoDeliveryNoticeChangeDTO.TabListDTO("","全部", list.stream().mapToInt(SoDeliveryNoticeChangeDTO.TabListDTO::getCount).sum()));
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
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SoDeliveryNoticeChangeDTO.ViewDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SoDeliveryNoticeChangeDTO.UpdateDTO dto) {
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
        SoDeliveryNoticeChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
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
        approveDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_NOTICE_CHANGE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoDeliveryNoticeChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        //TODO 审核通过修改发货通知单数据
        return Boolean.TRUE;
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
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "作废发货通知变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public SoDeliveryNoticeChangeDTO.ViewDTO view(SoDeliveryNoticeChangeDTO.ViewIdDTO viewIdDTO) {
        String type = viewIdDTO.getType();
        String id = viewIdDTO.getId();
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = new SoDeliveryNoticeChangeEntity();
        SoDeliveryNoticeEntity soDeliveryNoticeEntity;
        if("pushDown".equals(type)){
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        }else{
            soDeliveryNoticeChangeEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(soDeliveryNoticeChangeEntity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        }
        if(!(ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())
                || ApproveStatusEnum.APPROVE_ING.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus()))){
            throw new ServiceException("只有待提交、待审核状态允许下推");
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
        startDTO.setBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
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
            data.setPickedQty(currentPickingDetailList.stream().map(PickingDetailEntity::getPickedQty).reduce(0, Integer::sum));
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
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.model.wms.enums.RequisitionChangeTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.RequisitionApplicationChangeMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 要货申请变更单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Slf4j
@Service
public class RequisitionApplicationChangeServiceImpl extends SuperServiceImpl<RequisitionApplicationChangeMapper, RequisitionApplicationChangeEntity> implements RequisitionApplicationChangeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OmsListingInfoFeign listingInfoFeign;

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private RequisitionApplicationChangeDetailService detailService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationChangeDTO.AddDTO addDTO) {
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity = new RequisitionApplicationChangeEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationChangeEntity);

        // 数据处理
        handleData(requisitionApplicationChangeEntity);

        log.info("开始新增要货申请变更单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        requisitionApplicationChangeEntity.setCode(code);
        boolean save = super.save(requisitionApplicationChangeEntity);
        if(!save) {
            throw new ServiceException("要货申请变更单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "要货申请变更单" , requisitionApplicationChangeEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, requisitionApplicationChangeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(requisitionApplicationChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationChangeDTO.UpdateDTO updateDTO) {
        RequisitionApplicationChangeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请变更单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity =  BeanMapperUtils.map(RequisitionApplicationChangeEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationChangeEntity);
        log.info("编辑 开始修改要货申请变更单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(requisitionApplicationChangeEntity);
        if(!save) {
            throw new ServiceException("要货申请变更单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录要货申请变更单日志数据，单号：【{}】", requisitionApplicationChangeEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), requisitionApplicationChangeEntity.getCode(), "要货申请变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationChangeEntity, null, requisitionApplicationChangeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<RequisitionApplicationChangeDTO.ListDTO> paging(PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RequisitionApplicationChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<RequisitionApplicationChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        RequisitionApplicationChangeDTO.PagingParamDTO searchParam = new RequisitionApplicationChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RequisitionApplicationChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RequisitionApplicationChangeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new RequisitionApplicationChangeDTO.TabListDTO(status, 0));
        }
        });
        list.add(new RequisitionApplicationChangeDTO.TabListDTO("all", list.stream().mapToInt(RequisitionApplicationChangeDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(RequisitionApplicationChangeDTO.ExportDTO param, HttpServletResponse response) {
        List<RequisitionApplicationChangeDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/requisitionApplicationChange.xlsx";
        String name = "要货申请变更单导出";
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
        RequisitionApplicationChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到要货申请变更单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改要货申请变更单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动要货申请变更单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录要货申请变更单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(RequisitionApplicationChangeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(RequisitionApplicationChangeDTO.UpdateDTO dto) {
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
        RequisitionApplicationChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单", approveType.getName(), dto.getComment());
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
    private void approveProcess(RequisitionApplicationChangeEntity entity, ApproveOneDTO dto) {
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
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(RequisitionApplicationChangeEntity entity) {
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
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除要货申请变更单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除要货申请变更单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除要货申请变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        RequisitionApplicationChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改要货申请变更单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请变更单");
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
    public Boolean approveEnd(ApproveOneDTO dto, RequisitionApplicationChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<RequisitionApplicationChangeDTO.ProductDTO> addProductPaging(PagingDTO<RequisitionApplicationChangeDTO.ProductAddDTO> dto) {
        RequisitionApplicationChangeDTO.ProductAddDTO param = dto.getParams();
        RequisitionApplicationEntity requisitionApplicationEntity =requisitionApplicationService.getByIdOpt(param.getRequisitionId()).orElseThrow(() -> new ServiceException("未找到要货申请数据"));
        PagingDTO<ListingInfoDTO.PagingParamDTO> paramDTO = new PagingDTO<>();
        BeanUtil.copyProperties(dto,paramDTO);
        ListingInfoDTO.PagingParamDTO listingParamDTO = new ListingInfoDTO.PagingParamDTO();
        listingParamDTO.setAdvanceQueryDTOList(new ArrayList<>());
        listingParamDTO.setSqlMap(param.getSqlMap());
        if (RequisitionApplicationTypeEnum.FBA.getCode().equals(requisitionApplicationEntity.getType())) {
            listingParamDTO.setShopId(requisitionApplicationEntity.getChannelId());
        } else {
            listingParamDTO.setWarehouseId(requisitionApplicationEntity.getChannelId());
        }
        paramDTO.setParams(listingParamDTO);
        //调用listing接口获取商品信息
        PagingVO<ListingInfoDTO.PageDTO> pagingVO = listingInfoFeign.paging(paramDTO);
        PagingVO<RequisitionApplicationChangeDTO.ProductDTO> result = new PagingVO<>();
        BeanUtil.copyProperties(pagingVO,result);
        if(CollUtil.isEmpty(pagingVO.getList())){
            return result;
        }
        List<ListingInfoDTO.PageDTO> listingList = pagingVO.getList();
        List<RequisitionApplicationChangeDTO.ProductDTO> productList = new ArrayList<>();
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(requisitionApplicationEntity.getId()));
        List<String> skuNos = listingList.stream().map(ListingInfoDTO.PageDTO::getSkuNo).collect(Collectors.toList());
        List<ProductBomInfoDTO.SkuBomVersion> skuBomVersionList = plmTaskFeign.listBomVersionBySkuNos(skuNos);
        for (ListingInfoDTO.PageDTO pageDTO : listingList) {
            RequisitionApplicationChangeDTO.ProductDTO productDTO = new RequisitionApplicationChangeDTO.ProductDTO();
            productDTO.setPlatformSku(pageDTO.getPlatformSku());
            productDTO.setPlatformSkuName(pageDTO.getPlatformSkuName());
            productDTO.setSkuId(pageDTO.getSkuId());
            productDTO.setSkuNo(pageDTO.getSkuNo());
            productDTO.setProductName(pageDTO.getProductName());
            productDTO.setFnSku(pageDTO.getFnSku());
            productDTO.setAsin(pageDTO.getAsin());
            ProductBomInfoDTO.SkuBomVersion skuBomVersion = skuBomVersionList.stream().filter(v->v.getSkuNo().equals(pageDTO.getSkuNo())).findFirst().orElse(null);
            if(Objects.nonNull(skuBomVersion)){
                productDTO.setBomVersion(skuBomVersion.getBomVersionList().stream().max(String::compareTo).orElse(""));
            }
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = requisitionApplicationDetailEntityList.stream().filter(v->v.getPlatformSku().equals(pageDTO.getPlatformSku())).findFirst().orElse(null);
            //如果能关联到要货申明细，设置为修改类型
            if(Objects.nonNull(requisitionApplicationDetailEntity)){
                productDTO.setRequisitionDetailId(requisitionApplicationDetailEntity.getId());
                productDTO.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                productDTO.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
                productDTO.setChangeTypeName(RequisitionChangeTypeEnum.UPDATE.getName());
                productDTO.setOriginRequisitionQty(requisitionApplicationDetailEntity.getRequisitionQty());
            }else{
                productDTO.setChangeType(RequisitionChangeTypeEnum.ADD.getCode());
                productDTO.setChangeTypeName(RequisitionChangeTypeEnum.ADD.getName());
            }
            productList.add(productDTO);
        }
        List<String> patchPlatformSkuNos = param.getPlatformSkuNoList();
        if(CollUtil.isNotEmpty(patchPlatformSkuNos)){
            List<RequisitionApplicationChangeDTO.ProductDTO> productDTOS = new ArrayList<>();
            for (String patchPlatformSkuNo : patchPlatformSkuNos) {
                RequisitionApplicationChangeDTO.ProductDTO productDTO = productList.stream().filter(v->v.getPlatformSku().equals(patchPlatformSkuNo)).findFirst().orElse(new RequisitionApplicationChangeDTO.ProductDTO());
                productDTOS.add(productDTO);
            }
            result.setList(productDTOS);
        }else{
            result.setList(productList);
        }
        return result;
    }

    @Override
    public RequisitionApplicationChangeDTO.ViewDTO view(RequisitionApplicationChangeDTO.ViewIdDTO viewIdDTO) {
        String type = viewIdDTO.getType();
        String id = viewIdDTO.getId();
        RequisitionApplicationChangeEntity requisitionApplicationChangeEntity = new RequisitionApplicationChangeEntity();
        RequisitionApplicationEntity requisitionApplicationEntity;
        if("pushDown".equals(type)){
            requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请数据"));
            if(requisitionApplicationEntity.getStatus().equals(RequisitionApplicationStatusEnum.HANDLE.getStatus())){
                throw new ServiceException("要货申请已完成，不允许下推变更单");
            }
            List<PickingListsDTO.SourceView> pickingList = pickingListsService.listBySourceIds(Collections.singletonList(requisitionApplicationEntity.getId()));
            if(CollUtil.isEmpty(pickingList)){
                throw new ServiceException("未生成拣货单的要货申请不允许下推变更单");
            }
        }else{
            requisitionApplicationChangeEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
            requisitionApplicationEntity = requisitionApplicationService.getByIdOpt(requisitionApplicationChangeEntity.getSourceId()).orElseThrow(() -> new ServiceException("未找到要货申请变更单数据"));
        }
        //校验是否存在未处理
        if(CollectionUtils.isNotEmpty(viewIdDTO.getDetailIds())){
            List<RequisitionApplicationChangeDTO.ExistDTO> existDTOList = detailService.checkExist(viewIdDTO.getDetailIds());
            if(CollectionUtils.isNotEmpty(existDTOList)){
                String existCode = existDTOList.get(0).getCode();
                List<String> existSkuNos = existDTOList.stream().map(RequisitionApplicationChangeDTO.ExistDTO::getSkuNo).collect(Collectors.toList());
                throw new ServiceException("存在处理中的要货申请变更单【{}】，sku【{}】，请等待审核完成后操作",existCode,existSkuNos);
            }
        }
        RequisitionApplicationChangeDTO.ViewDTO result = new RequisitionApplicationChangeDTO.ViewDTO();
        buildView(result, requisitionApplicationChangeEntity, type, requisitionApplicationEntity);
        //处理明细
        List<RequisitionApplicationChangeDTO.ViewDetailDTO> details = new ArrayList<>();
        buildViewDetail(viewIdDTO, details, requisitionApplicationChangeEntity);

        result.setDetails(details);

        return result;
    }

    private void buildViewDetail(RequisitionApplicationChangeDTO.ViewIdDTO viewIdDTO, List<RequisitionApplicationChangeDTO.ViewDetailDTO> details, RequisitionApplicationChangeEntity requisitionApplicationChangeEntity) {
        if(CollectionUtils.isNotEmpty(viewIdDTO.getDetailIds())){
            List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntityList = requisitionApplicationDetailService.listByIds(viewIdDTO.getDetailIds());
            //查询产品信息
            List<String> skuIdList = requisitionApplicationDetailEntityList.stream().map(RequisitionApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
            for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : requisitionApplicationDetailEntityList) {
                RequisitionApplicationChangeDTO.ViewDetailDTO viewDetailDTO = new RequisitionApplicationChangeDTO.ViewDetailDTO();
                viewDetailDTO.setRequisitionDetailId(requisitionApplicationDetailEntity.getId());
                viewDetailDTO.setPlatformSku(requisitionApplicationDetailEntity.getPlatformSku());
                viewDetailDTO.setPlatformSkuName(requisitionApplicationDetailEntity.getPlatformSkuName());
                viewDetailDTO.setSkuId(requisitionApplicationDetailEntity.getSkuId());
                viewDetailDTO.setSkuNo(requisitionApplicationDetailEntity.getSkuNo());
                viewDetailDTO.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                viewDetailDTO.setAsin(requisitionApplicationDetailEntity.getPlatformSpu());
                viewDetailDTO.setChangeType(RequisitionChangeTypeEnum.UPDATE.getCode());
                viewDetailDTO.setChangeTypeName(RequisitionChangeTypeEnum.UPDATE.getName());
                viewDetailDTO.setOriginRequisitionQty(requisitionApplicationDetailEntity.getRequisitionQty());
                viewDetailDTO.setBomVersion(requisitionApplicationDetailEntity.getBomVersion());
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                viewDetailDTO.setProductName(skuVO.getSkuName());

                details.add(viewDetailDTO);
            }
        }
        List<RequisitionApplicationChangeDetailEntity> detailEntityList = detailService.listByMains(Collections.singletonList(requisitionApplicationChangeEntity.getId()));
        //查询产品信息
        List<String> skuIdList = detailEntityList.stream().map(RequisitionApplicationChangeDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        for (RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity : detailEntityList) {
            RequisitionApplicationChangeDTO.ViewDetailDTO viewDetailDTO = new RequisitionApplicationChangeDTO.ViewDetailDTO();
            viewDetailDTO.setDetailId(requisitionApplicationChangeDetailEntity.getId());
            viewDetailDTO.setRequisitionDetailId(requisitionApplicationChangeDetailEntity.getSourceDetailId());
            viewDetailDTO.setPlatformSku(requisitionApplicationChangeDetailEntity.getPlatformSkuNo());
            viewDetailDTO.setPlatformSkuName(requisitionApplicationChangeDetailEntity.getPlatformSkuName());
            viewDetailDTO.setSkuId(requisitionApplicationChangeDetailEntity.getSkuId());
            viewDetailDTO.setSkuNo(requisitionApplicationChangeDetailEntity.getSkuNo());
            viewDetailDTO.setFnSku(requisitionApplicationChangeDetailEntity.getFnSku());
            viewDetailDTO.setAsin(requisitionApplicationChangeDetailEntity.getPlatformSpu());
            viewDetailDTO.setChangeType(requisitionApplicationChangeDetailEntity.getChangeType());
            viewDetailDTO.setChangeTypeName(RequisitionChangeTypeEnum.getName(requisitionApplicationChangeDetailEntity.getChangeType()));
            viewDetailDTO.setOriginRequisitionQty(requisitionApplicationChangeDetailEntity.getOriginQty());
            viewDetailDTO.setNewRequisitionQty(requisitionApplicationChangeDetailEntity.getNewQty());
            viewDetailDTO.setRemark(requisitionApplicationChangeDetailEntity.getRemark());
            viewDetailDTO.setBomVersion(requisitionApplicationChangeDetailEntity.getBomVersion());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationChangeDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            viewDetailDTO.setProductName(skuVO.getSkuName());
            details.add(viewDetailDTO);
        }
    }

    private void buildView(RequisitionApplicationChangeDTO.ViewDTO result, RequisitionApplicationChangeEntity requisitionApplicationChangeEntity, String type, RequisitionApplicationEntity requisitionApplicationEntity) {
        result.setId(requisitionApplicationChangeEntity.getId());
        result.setCode(requisitionApplicationChangeEntity.getCode());
        if("pushDown".equals(type)){
            result.setSourceId(requisitionApplicationEntity.getId());
            result.setSourceCode(requisitionApplicationEntity.getCode());
            result.setBusinessId(requisitionApplicationEntity.getId());
            result.setBusinessCode(requisitionApplicationEntity.getCode());
            result.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            result.setApproveStatusName(ApproveStatusEnum.WAIT_SUBMIT.getName());
        }else{
            result.setSourceId(requisitionApplicationChangeEntity.getSourceId());
            result.setSourceCode(requisitionApplicationChangeEntity.getSourceCode());
            result.setBusinessId(requisitionApplicationChangeEntity.getBusinessId());
            result.setBusinessCode(requisitionApplicationChangeEntity.getBusinessCode());
            result.setApproveStatus(requisitionApplicationChangeEntity.getApproveStatus());
            result.setApproveStatusName(ApproveStatusEnum.getName(requisitionApplicationChangeEntity.getApproveStatus()));
        }
        if(SourceTypeEnum.DELIVERY_PLAN.getCode().equals(requisitionApplicationEntity.getSourceType())){
            result.setDeliveryPlanCode(requisitionApplicationEntity.getSourceCode());
        }
        result.setBillType(requisitionApplicationEntity.getType());
        result.setBillTypeName(RequisitionApplicationTypeEnum.getName(requisitionApplicationEntity.getType()));
        result.setChannelId(requisitionApplicationEntity.getChannelId());
        result.setChannelName(requisitionApplicationEntity.getChannelName());
        result.setRequisitionWarehouseId(requisitionApplicationEntity.getRequisitionWarehouseId());
        result.setRequisitionWarehouseName(requisitionApplicationEntity.getRequisitionWarehouseName());
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(RequisitionApplicationChangeEntity entity) {
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
    private void fillOne(RequisitionApplicationChangeDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
            .set(RequisitionApplicationChangeEntity::getApproveUserId, userInfo.getUid())
            .set(RequisitionApplicationChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
            .set(RequisitionApplicationChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new RequisitionApplicationChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
            .set(RequisitionApplicationChangeEntity::getApproveUserId, "")
            .set(RequisitionApplicationChangeEntity::getApproveUserName, "")
            .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
            .set(RequisitionApplicationChangeEntity::getApproveTime, null)
            .update(new RequisitionApplicationChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(RequisitionApplicationChangeEntity::getId, id)
        .set(RequisitionApplicationChangeEntity::getApproveStatus, approveStatus)
        .update(new RequisitionApplicationChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<RequisitionApplicationChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(RequisitionApplicationChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(RequisitionApplicationChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationChangeEntity requisitionApplicationChangeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
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
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.MachineTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.TransferApplicationMapper;
import com.erp.server.wms.query.TransferApplicationQueryHandler;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_APPLICATION;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferApplicationServiceImpl extends SuperServiceImpl<TransferApplicationMapper, TransferApplicationEntity> implements TransferApplicationService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferApplicationDetailService transferApplicationDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private TransferApplicationQueryHandler transferApplicationQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> pagingDTO) {
        TransferApplicationDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<TransferApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<TransferApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {

        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<TransferApplicationDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            TransferApplicationDTO.SearchParamDTO searchParamDTO = new TransferApplicationDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            TransferApplicationDTO.ListStatusCountDTO resultDTO = new TransferApplicationDTO.ListStatusCountDTO();
            String tabSql = transferApplicationQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.equals(PageListTypeEnum.TO_BE_APPROVE) ? "待我审核" : item.getName());
            list.add(resultDTO);
        }
        return list;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String add(TransferApplicationDTO.AddDTO dto) {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);

        //验证调出入仓库是否相同
        if (dto.getInWarehouseId().equals(dto.getOutWarehouseId())) {
            throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,"")));
        }

        log.info("调拨申请单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.DBSQ, BusinessNoTypeEnum.CODE_DBSQ.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DBSQ);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个调拨申请单【%s】", code), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "新增操作");
            //新增明细
            transferApplicationDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(TransferApplicationDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        TransferApplicationEntity entity = this.getById(id);
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        //提交
        BatchResultDTO submit = this.submit(entity);
        if (!submit.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_1042,"调拨申请单");
        }
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TransferApplicationDTO.UpdateDTO dto) {

        TransferApplicationEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        //验证调出入仓库是否相同
        if (dto.getInWarehouseId().equals(dto.getOutWarehouseId())) {
            throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,old.getCode())));
        }

        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        List<TransferApplicationDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);

        log.info("调拨申请单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        transferApplicationDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        TransferApplicationEntity entity = this.getById(dto.getId());
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        //提交
        BatchResultDTO submit = this.submit(entity);
        return submit.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(TransferApplicationEntity entity) {
        //待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //验证调出入仓库是否相同
        if (entity.getInWarehouseId().equals(entity.getOutWarehouseId())) {
            throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,entity.getCode())));
        }

        log.info("调拨申请单提交，id=【{}】", entity.getId());

        //提交流程
        startProcess(entity);
        //更新审核状态
        updateApproveStatus(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        operateLogService.addModuleOperateLog("提交了一个调拨申请单【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public TransferApplicationDTO.ViewDTO view(String id) {
        TransferApplicationDTO.ViewDTO viewDTO = new TransferApplicationDTO.ViewDTO();
        //主表信息
        TransferApplicationEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        List<TransferApplicationDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferApplicationDetailDTO.ViewDTO.class, detailList);

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isNotEmpty(transferDirectionList)) {
            String name = transferDirectionList.stream().filter(obj -> obj.getValue().equals(viewDTO.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse(null);
            viewDTO.setTransferDirectionName(name);
        }

        //产品信息
        List<String> skuIds = detailList.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class,skuIds);

        //产品销售信息
        List<ProductSaleEntity> productSaleEntityList = FeignQuery.create(ProductSaleEntity.class).in(ProductSaleEntity::getSkuId,skuIds).list();

        //组织
        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(Collections.singletonList(viewDTO.getOutOrgId()));
        param.setSkuIdList(skuIds);
        param.setWarehouseIdList(Collections.singletonList(viewDTO.getOutWarehouseId()));
        //库存信息
        List<InventoryEntity> inventoryInfoList = inventoryService.listInventoryByParam(param);

        for (TransferApplicationDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            String productName = skuList.stream().filter(e -> e.getId().equals(viewDetailDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            viewDetailDTO.setProductName(productName);

            //商品状态
            String saleStateName = productSaleEntityList.stream().filter(e -> CharSequenceUtil.equals(e.getSkuId(), viewDetailDTO.getSkuId())).findFirst().map(e -> SaleStateEnum.getNameByCode(e.getSaleState())).orElse("");
            viewDetailDTO.setSaleStateName(saleStateName);

            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryInfoList.stream().filter(obj -> obj.getSkuId().equals(viewDetailDTO.getSkuId()) && InventoryStatusEnum.USABLE.getCode().equals(obj.getDictInventoryStatus()))
                    .map(InventoryEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails) {

        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交并且未作废允许删除
//        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<TransferApplicationEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        for (TransferApplicationEntity entity : list) {
            if (!entity.getApproveStatus().equals(waitSubmitStatus)
                    ||entity.getInvalidStatus()){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(TransferApplicationEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        log.info("调拨申请删除，ids=【{}】", JSONUtil.toJsonStr(removeIdList));
        //删除明细数据
        transferApplicationDetailService.removeByMainIds(removeIdList);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的调拨申请单", UserContext.getDefaultLoginUser().getUserName(), removeList.stream().map(TransferApplicationEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "删除操作");
        //删除主表数据
        boolean result = this.removeByIds(removeIdList);
        if (!result){
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }

        // 返回成功结果
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("调拨申请删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        transferApplicationDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的调拨申请单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(TransferApplicationEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "删除操作");
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deleteEntity(TransferApplicationEntity entity) {
        //待提交并且未作废允许删除
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("调拨申请删除，id=【{}】", entity.getId());
        List<String> ids = Collections.singletonList(entity.getId());
        //删除明细数据
        transferApplicationDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的调拨申请单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode());
        List<Pair<String, String>> pairList = Collections.singletonList(new Pair<>(entity.getId(), entity.getCode()));
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.QC_ORDER.getCode(), pairList, "删除操作");
        //删除主表数据
        boolean result = this.removeByIds(ids);
        if (result) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败");
        }
    }

    @Override
    public Map<String, TransferApplicationEntity> mapByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<TransferApplicationEntity> list = this.listByIds(ids);
        return list.stream().collect(Collectors.toMap(TransferApplicationEntity::getId, Function.identity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("调拨申请单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(TransferApplicationEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个调拨申请单【%s】，作废原因：".concat(reason), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(TransferApplicationEntity entity, String type, String comment, Boolean isNeedProcess){
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        log.info("调拨申请单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());

        //调用审核流程
        approveProcess(entity, type, comment, isNeedProcess);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个调拨申请单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(TransferApplicationEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (Objects.isNull(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus;
        if (type.equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
        }
        log.info("调拨申请单审核【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());

        Boolean result = this.updateApproveStatusForApprove(Collections.singletonList(entity.getId()), approveStatus.getStatus());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (type.equals(ApproveType.PASS)) {
            //审核通过后生成拣货明细
//            generatePickingDetail(list);

            //获取需要自动生成加工单的数据
            /* List<TransferApplicationDetailEntity> transferApplicationDetailEntities = transferApplicationDetailService.listByMainIds(ids);
            List<String> infoIds = transferApplicationDetailEntities.stream().filter(req -> req.getIsAutoMachine().equals(Boolean.TRUE)).map(TransferApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(infoIds)) {
                List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfoList = viewGenerateMachineInfo(infoIds, Boolean.TRUE, MathUtil.ZERO);
                saveGenerateMachineInfo(viewGenerateMachineInfoList);
            }*/
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void singleApprove(SingleApproveParamDTO singleApproveParamDTO) {
        String id = singleApproveParamDTO.getId();
        //根据id查询
        TransferApplicationEntity entity = this.getById(id);
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = singleApproveParamDTO.getType();
        log.info("调拨申请单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("调拨申请单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(Collections.singletonList(id), ApproveStatusEnum.APPROVE.getStatus());
            //审核通过后生成拣货明细
            generatePickingDetail(Collections.singletonList(entity));

            //获取需要自动生成加工单的数据
            /*List<TransferApplicationDetailEntity> transferApplicationDetailEntities = transferApplicationDetailService.listByMainIds(Collections.singletonList(id));
            List<String> infoIds = transferApplicationDetailEntities.stream().filter(req -> req.getIsAutoMachine().equals(Boolean.TRUE)).map(TransferApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfoList = viewGenerateMachineInfo(infoIds, Boolean.TRUE, singleApproveParamDTO.getQty());
            saveGenerateMachineInfo(viewGenerateMachineInfoList);*/
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("调拨申请单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
            //中止当前审核流程
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(id), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = Collections.singletonList(entity).stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个调拨申请单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(CharSequenceUtil.isNotBlank(singleApproveParamDTO.getComment()) ? String.format(",意见：%s", singleApproveParamDTO.getComment()) : ""), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "审核操作");
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(TransferApplicationEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }

        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            throw  new ServiceException(ApiError.ERROR_99045);
        }
        List<TransferOutEntity> transferOutList = transferOutService.listBySourceIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isNotEmpty(transferOutList)) {
            throw  new ServiceException(ApiError.ERROR_99046);
        }

        /*  List<MachineInfoEntity> machineInfoEntityList = machineInfoService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(machineInfoEntityList)) {
            throw  new ServiceException(ApiError.ERROR_99046);
        }*/
        log.info("调拨申请单反审核，id=【{}】", entity.getId());

        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
//        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_APPLY,ids);
//        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
//        //删除拣货明细
//        pickingDetailService.deleteBySourceId(ids);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个调拨申请单【%s】", entity.getCode()), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("调拨申请单撤销流程，id=【{}】", ids);

        //撤销现有流程
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });


        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("调拨申请单【%s】取消流程", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("调拨申请单导出", EXPORT_WMS_TRANSFER_APPLICATION.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferInfo(List<String> detailIdList) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = viewGenerateData(detailIdList);
        return list;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferOut(List<String> detailIdList) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = viewGenerateData(detailIdList);
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferInfo(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        List<TransferApplicationDTO.GenerateTransferInfoDTO> list = validList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //生成下推单据
        generateTransferData(list,MathUtil.ZERO);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferOut(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        List<TransferApplicationDTO.GenerateTransferInfoDTO> list = validList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //生成下推单据
        generateTransferData(list,MathUtil.ONE);
        return Boolean.TRUE;
    }

    /**
     * @description: 调拨申请单下推保存
     * @author Will
     * @date: 2023/5/30 16:15
     * @param list
     * @param type 0、直接调拨。1、分步式调出
     */
    private void generateTransferData(List<TransferApplicationDTO.GenerateTransferInfoDTO> list,Integer type) {
        //调拨申请单主表信息
        List<String> sourceIds = list.stream().map(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceId).distinct().collect(Collectors.toList());
        List<TransferApplicationEntity> transferApplicationList = this.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(transferApplicationList)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        long count = transferApplicationList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99064);
        }

        List<String> sourceDetailIds = list.stream().map(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceDetailId).distinct().collect(Collectors.toList());

        //拣货明细信息
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByIds(sourceDetailIds);

        //直接调拨明细
        List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listSourceDetailIds(sourceDetailIds);
        //分步式调出明细
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listSourceDetailIds(sourceDetailIds);

        //根据来源id分组生成下推直接调拨单
        Map<String, List<TransferApplicationDTO.GenerateTransferInfoDTO>> map = list.stream().collect(Collectors.groupingBy(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceId));

        for (Map.Entry<String, List<TransferApplicationDTO.GenerateTransferInfoDTO>> entry : map.entrySet()) {
            List<TransferApplicationDTO.GenerateTransferInfoDTO> value = entry.getValue();
            TransferApplicationDTO.GenerateTransferInfoDTO transferInfoDTO = value.get(0);

            //调拨方向
            TransferApplicationEntity entity = transferApplicationList.stream().filter(obj -> obj.getId().equals(transferInfoDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_99043);
            }
            //直接调拨单
            if (MathUtil.ZERO.equals(type)) {
                TransferInfoDTO.AddDTO addInfoDTO = new TransferInfoDTO.AddDTO();
                BeanMapperUtils.copy(transferInfoDTO,addInfoDTO);
                addInfoDTO.setTransferDirection(entity.getTransferDirection());
                addInfoDTO.setOutOrgId(entity.getOutOrgId());
                addInfoDTO.setInOrgId(entity.getInOrgId());
                addInfoDTO.setRemark(null);
                List<TransferInfoDetailDTO.AddDTO> addDetailList = new ArrayList<>();
                for (TransferApplicationDTO.GenerateTransferInfoDTO dto : value) {
                    //验证明细是否已经被调拨
                    checkGenerateTransfer(transferInfoDetailList,transferOutDetailList,detailList,dto);
                    TransferInfoDetailDTO.AddDTO addDetailDTO = new TransferInfoDetailDTO.AddDTO();
                    BeanMapperUtils.copy(dto,addDetailDTO);
                    addDetailDTO.setOutWarehouseId(entity.getOutWarehouseId());
                    addDetailList.add(addDetailDTO);
                }
                addInfoDTO.setDetailList(addDetailList);
                //新增直接调拨单
                transferInfoService.add(addInfoDTO);
            }

            //分步式调出单
            if (MathUtil.ONE.equals(type)) {
                //分步式调出单
                TransferOutDTO.AddDTO addOutDTO = new TransferOutDTO.AddDTO();
                BeanMapperUtils.copy(transferInfoDTO,addOutDTO);
                addOutDTO.setTransferDirection(entity.getTransferDirection());
                addOutDTO.setOutWarehouseId(entity.getOutWarehouseId());
                addOutDTO.setRemark(null);
                List<TransferOutDetailDTO.AddDTO> addDetailList = new ArrayList<>();
                for (TransferApplicationDTO.GenerateTransferInfoDTO dto : value) {
                    //验证明细是否已经被调拨
                    checkGenerateTransfer(transferInfoDetailList,transferOutDetailList,detailList,dto);
                    TransferOutDetailDTO.AddDTO addDetailDTO = BeanMapperUtils.map(TransferOutDetailDTO.AddDTO.class,dto);
                    addDetailList.add(addDetailDTO);
                }
                addOutDTO.setDetailList(addDetailList);
                //新增直接调拨单
                transferOutService.add(addOutDTO);
            }
        }

    }

    /**
     * @description: 验证是否被调拨
     * @author Will
     * @date: 2023/5/18 10:10
     * @param transferInfoDetailList
     * @param transferOutDetailList
     * @param dto
     */
    private void checkGenerateTransfer (List<TransferInfoDetailEntity> transferInfoDetailList,List<TransferOutDetailEntity> transferOutDetailList,List<TransferApplicationDetailEntity> detailList,TransferApplicationDTO.GenerateTransferInfoDTO dto) {
        //来源明细id
        String sourceDetailId = dto.getSourceDetailId();
        //sku编码
        String skuNo = dto.getSkuNo();

        //拣货数量
        Integer pickingQty = detailList.stream().filter(obj -> obj.getId().equals(sourceDetailId)).map(TransferApplicationDetailEntity::getQty).findFirst().orElse(MathUtil.ZERO);

        //直接调拨数量
       Integer transferInfoQty = MathUtil.ZERO;

       //分步式调出数量
        Integer transferOutQty = MathUtil.ZERO;

        //直接调拨
        if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
            //已调拨数量
            transferInfoQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (pickingQty.intValue() == transferInfoQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_99051.code, String.format(ApiError.ERROR_99051.msg,dto.getSourceCode(), skuNo));
            }
        }
        //分步式调出
        if (CollectionUtils.isNotEmpty(transferOutDetailList)) {
            //已调拨数量
            transferOutQty = transferOutDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(TransferOutDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (pickingQty.intValue() == transferOutQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_99055.code, String.format(ApiError.ERROR_99055.msg,dto.getSourceCode(), skuNo));
            }
        }
        //调拨数量校验（直接调拨数量+分步式调出数量+本次调拨数量 不能大于 拣货数量）
        if (transferInfoQty.intValue() + transferOutQty.intValue() + dto.getQty().intValue() > pickingQty.intValue()) {
            throw new ServiceException(ApiError.ERROR_99050.code, String.format(ApiError.ERROR_99050.msg,dto.getSourceCode(), skuNo, pickingQty.intValue() - transferOutQty.intValue() -transferInfoQty.intValue()));

        }
    }

    @Override
    public List<PickingDetailDTO.ListDTO> listPickingDetail(PickingDetailDTO.SearchParamDTO dto) {
        return pickingDetailService.listPickingDetailBySourceId(dto);
    }

    /**
     * 下推加工单-列表查询
     * @Author Luo_WG
     * @Date 2023/6/29 17:50
     * @param ids
     * @param isAutoMachine 是否需要自动生成的
     * @param qty 审核通过填写的批准加工数量
     * @return java.util.List<com.erp.model.wms.dto.TransferApplicationDTO.ViewGenerateMachineInfo>
     **/
    @Override
    public List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfo(List<String> ids, Boolean isAutoMachine, Integer qty) {
        List<TransferApplicationDTO.ViewGenerateMachineInfo> list = baseMapper.viewGenerateMachineInfo(ids, isAutoMachine);
        List<TransferApplicationDTO.ViewGenerateMachineInfo> sonSkuDateList = new ArrayList<>();

        //产品信息
        List<String> skuIds = list.stream().map(TransferApplicationDTO.ViewGenerateMachineInfo::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        //根据sku查询拥有的子sku
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        String combinationType = BomTypeEnum.COMBINATION.getType();
        bomChildrenSkuDTOS = bomChildrenSkuDTOS.stream().filter(b -> combinationType.equals(b.getType())).collect(Collectors.toList());
        for (TransferApplicationDTO.ViewGenerateMachineInfo viewGenerateMachineInfo : list) {
            //如果是自动生成的需要设置批准数量为加工数量再下推
            if (isAutoMachine) {
                //如果批准数量是0表示是批量审核，设置调拨数量为加工数量，单个审核使用批准数量
                if (!MathUtil.ZERO.equals(qty)) {
                    viewGenerateMachineInfo.setMachineQty(qty);
                }
            }
            //标记是否是原始手动添加的sku，用来区分自动生成的子sku
            viewGenerateMachineInfo.setIsBody(Boolean.TRUE);
            viewGenerateMachineInfo.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
            //获取sku名称
            String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewGenerateMachineInfo.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            viewGenerateMachineInfo.setProductName(productName);
            //获取sku的子sku
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewGenerateMachineInfo.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                //设置主sku标识
                viewGenerateMachineInfo.setIsCombination(Boolean.TRUE);
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    //初始化子sku信息在列表显示
                    TransferApplicationDTO.ViewGenerateMachineInfo info = new TransferApplicationDTO.ViewGenerateMachineInfo();
                    //获取sku名称
                    String sonProductName = skuList.stream().filter(e -> e.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                    info.setSourceId(viewGenerateMachineInfo.getSourceId());
                    info.setSourceCode(viewGenerateMachineInfo.getSourceCode());
                    info.setSkuId(bomChildrenSkuDTO.getSkuId());
                    info.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    info.setProductName(sonProductName);
                    info.setWarehouseId(viewGenerateMachineInfo.getWarehouseId());
                    info.setWarehouseName(viewGenerateMachineInfo.getWarehouseName());
                    info.setMachineQty(viewGenerateMachineInfo.getMachineQty() * bomChildrenSkuDTO.getQuantity());
                    info.setIsBody(Boolean.FALSE);
                    sonSkuDateList.add(info);
                }
            }
        }
        list.sort(Comparator.comparing(TransferApplicationDTO.ViewGenerateMachineInfo::getSourceCode, Comparator.reverseOrder()));
        return list;
    }

    /**
     * 下推加工单-保存
     * @Author Luo_WG
     * @Date 2023/6/29 17:49
     * @param list
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveGenerateMachineInfo(List<TransferApplicationDTO.ViewGenerateMachineInfo> list) {
        //获取到保存的主单据个数
        List<String> sourceIdList = list.stream().map(TransferApplicationDTO.ViewGenerateMachineInfo::getSourceId).distinct().collect(Collectors.toList());
        for (String sourceId : sourceIdList) {
            MachineInfoDTO.AddDTO addDTO = new MachineInfoDTO.AddDTO();
            List<MachineDetailDTO.AddDTO> MachineDetailDtoList = new ArrayList<>();
            addDTO.setBillDate(LocalDate.now());
            addDTO.setType(MachineTypeEnum.ORDINARY.getCode());
            addDTO.setSourceId(sourceId);
            addDTO.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
            String sourceCode = list.stream().filter(v->v.getSourceId().equals(sourceId)).findFirst().orElseThrow(() -> new ServiceException("未找到对应信息")).getSourceCode();
            addDTO.setSourceCode(sourceCode);
            List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfoList = list.stream().filter(req -> req.getSourceId().equals(sourceId)).collect(Collectors.toList());
            List<MachineSubComponentsDTO.AddDTO> componentsDTOList = new ArrayList<>();
            for (TransferApplicationDTO.ViewGenerateMachineInfo viewGenerateMachineInfo : viewGenerateMachineInfoList) {
                MachineDetailDTO.AddDTO dto = new MachineDetailDTO.AddDTO();
                //表示是手动添加的sku
                if (viewGenerateMachineInfo.getIsBody()) {
                    addDTO.setWorkType(viewGenerateMachineInfo.getWorkType());
                    addDTO.setWarehouseId(viewGenerateMachineInfo.getWarehouseId());
                    addDTO.setType(viewGenerateMachineInfo.getWorkType());
                    dto.setSkuId(viewGenerateMachineInfo.getSkuId());
                    dto.setSkuNo(viewGenerateMachineInfo.getSkuNo());
                    dto.setQty(viewGenerateMachineInfo.getMachineQty());
                    dto.setRemark(viewGenerateMachineInfo.getRemark());
                    dto.setWarehouseLocation(viewGenerateMachineInfo.getWarehouseLocation());
                    //查询是否包含了子sku
                    List<TransferApplicationDTO.ViewGenerateMachineInfo> infoList = list.stream().filter(req -> viewGenerateMachineInfo.getSourceId().equals(sourceId)
                            && req.getIsCombination().equals(Boolean.TRUE)
                            && req.getIsBody().equals(Boolean.TRUE)).collect(Collectors.toList());
                    for (TransferApplicationDTO.ViewGenerateMachineInfo info : infoList) {
                        MachineSubComponentsDTO.AddDTO componentsDTO = new MachineSubComponentsDTO.AddDTO();
                        componentsDTO.setIsChild(Boolean.TRUE);
                        componentsDTO.setQty(info.getMachineQty());
                        componentsDTO.setSkuId(info.getSkuId());
                        componentsDTO.setSkuNo(info.getSkuNo());
                        componentsDTO.setRemark(info.getRemark());
                        componentsDTO.setWarehouseId(info.getWarehouseId());
                        componentsDTO.setWarehouseLocation(info.getWarehouseLocation());
                        componentsDTOList.add(componentsDTO);
                    }
                    dto.setSubComponentsList(componentsDTOList);
                    MachineDetailDtoList.add(dto);
                }
            }
            addDTO.setDetailList(MachineDetailDtoList);
            machineInfoService.add(addDTO);
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> exportTransferApplication(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<TransferApplicationDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandleData(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<TransferApplicationEntity> listByCodes(List<String> list) {
        return this.list(new QueryWrapper<TransferApplicationEntity>().lambda().in(TransferApplicationEntity::getCode, list).eq(TransferApplicationEntity::getIsDeleted, Boolean.FALSE));
    }

    @Override
    public void updateApproveStatus(TransferApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        String approveStatus = updateApprovalStatusDTO.getApproveStatus();
        TransferApplicationEntity transferApplicationEntity = updateApprovalStatusDTO.getTransferApplicationEntity();
        lambdaUpdate().in(TransferApplicationEntity::getId, Collections.singletonList(transferApplicationEntity.getId()))
                .set(TransferApplicationEntity::getApproveUserId, transferApplicationEntity.getApproveUserId())
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * @description: 下推数据查询
     * @author Will
     * @date: 2023/5/12 9:12
     * @param detailIdList
     * @return List<ViewGenerateTransferInfoDTO>
     */
    private List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>  viewGenerateData(List<String> detailIdList) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = baseMapper.viewGenerateTransferInfo(detailIdList);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> sourceDetailIds = list.stream().map(TransferApplicationDTO.ViewGenerateTransferInfoDTO::getSourceDetailId).collect(Collectors.toList());

        List<String> skuIds = list.stream().map(TransferApplicationDTO.ViewGenerateTransferInfoDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());

        //直接调拨明细
        List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listSourceDetailIds(sourceDetailIds);
        //分步式调出明细
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listSourceDetailIds(sourceDetailIds);


        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> resultList = new ArrayList<>();
        for (TransferApplicationDTO.ViewGenerateTransferInfoDTO dto : list ) {

            //直接调拨数量
            Integer transferInfoQty = MathUtil.ZERO;
            //分步式调出数量
            Integer transferOutQty = MathUtil.ZERO;

            //直接调拨
            if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
                //已调拨数量
                transferInfoQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //分步式调出
            if (CollectionUtils.isNotEmpty(transferOutDetailList)) {
                //已调拨数量
                transferOutQty = transferOutDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(TransferOutDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //完成调拨不显示
            if (transferInfoQty.intValue() + transferOutQty.intValue() >=  dto.getQty().intValue()) {
                continue;
            }

            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(dto.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                dto.setProductName(productName);
            }
            //调拨方向名称
            if (CollectionUtils.isNotEmpty(transferDirectionList)) {
                String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(dto.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
                dto.setTransferDirectionName(transferDirectionName);
            }

            dto.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * @description: 生成拣货明细
     * @author Will
     * @date: 2023/5/12 10:45
     * @param list
     */
    private void generatePickingDetail (List<TransferApplicationEntity> list) {
        //生成拣货明细
        List<String> ids = list.stream().map(TransferApplicationEntity::getId).collect(Collectors.toList());
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        //拣货明细集合
        List<PickingDetailDTO.CommonDTO> addList = new ArrayList<>();

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        for (TransferApplicationEntity entity :list) {
            List<TransferApplicationDetailEntity> detailEntities = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }
            for (TransferApplicationDetailEntity detailEntity : detailEntities) {
                //查询可用库存生成拣货明细
                PickingDetailDTO.InventoryParamDTO dto = new PickingDetailDTO.InventoryParamDTO(entity.getOutOrgId(),entity.getOutOrgName(),entity.getOutWarehouseId(),
                        entity.getOutWarehouseName(),detailEntity.getSkuId(),detailEntity.getSkuNo(),detailEntity.getQty());

                List<InventoryEntity> inventoryList = Lists.newArrayList();
                if(ignoreInventorySkuIds.contains(detailEntity.getSkuId())) {
                    InventoryEntity inventoryEntity = new InventoryEntity();
                    inventoryEntity.setWarehouseId(dto.getWarehouseId());
                    inventoryEntity.setOrgId(dto.getOrgId());
                    inventoryEntity.setWarehouseLocation("");
                    inventoryEntity.setQty(detailEntity.getQty());
                    inventoryEntity.setSkuId(detailEntity.getSkuId());
                    inventoryEntity.setSkuNo(detailEntity.getSkuNo());
                    inventoryEntity.setDictInventoryStatus(InventoryStatusEnum.USABLE.getCode());
                    inventoryList.add(inventoryEntity);
                } else {
                    inventoryList = inventoryService.listPickingDetailInventory(dto);
                }

                List<PickingDetailDTO.CommonDTO> pickingDetailList = BeanMapperUtils.copyList(PickingDetailDTO.CommonDTO.class, inventoryList);

                List<InOutStockDTO>  inOutStockList = new ArrayList<>();
                for ( PickingDetailDTO.CommonDTO addDTO : pickingDetailList) {
                    addDTO.setSourceId(entity.getId());
                    addDTO.setSourceCode(entity.getCode());
                    addDTO.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
                    addDTO.setSourceDetailId(detailEntity.getId());
                    addDTO.setUnit(detailEntity.getUnit());
                    addDTO.setWarehouseName(entity.getOutWarehouseName());
                    addDTO.setOrgName(entity.getOutOrgName());

                    //调拨操作请求实体
                    InOutStockDTO inOutStockDTO = new InOutStockDTO();
                    inOutStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_APPLY);
                    inOutStockDTO.setSourceId(entity.getId());
                    inOutStockDTO.setSourceCode(entity.getCode());
                    inOutStockDTO.setSourceDetailId(detailEntity.getId());
                    inOutStockDTO.setBillDate(entity.getBillDate());
                    inOutStockDTO.setSkuId(addDTO.getSkuId());
                    inOutStockDTO.setSkuNo(addDTO.getSkuNo());
                    inOutStockDTO.setQty(addDTO.getQty());
                    inOutStockDTO.setWarehouseId(entity.getOutWarehouseId());
                    inOutStockDTO.setWarehouseLocation(addDTO.getWarehouseLocation());
                    inOutStockList.add(inOutStockDTO);
                }
                addList.addAll(pickingDetailList);

                //减少可用库存，添加冻结库存
                InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
                inventoryInOutStockDTO.setParamList(inOutStockList);
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.TRANSFER_APPLY.getCode());
                //更新库存
                inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
            }
        }
        //添加拣货明细数据
        pickingDetailService.add(addList);
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<TransferApplicationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(TransferApplicationDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class,ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //产品销售信息
        List<ProductSaleEntity> productSaleEntityList = FeignQuery.create(ProductSaleEntity.class).in(ProductSaleEntity::getSkuId,ids).list();

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isEmpty(transferDirectionList)) {
            throw new ServiceException(ApiError.ERROR_99049);
        }
        List<String> skuIds = records.stream().map(TransferApplicationDTO.ListDTO::getSkuNo).collect(Collectors.toList());
        //根据sku查询拥有的子sku
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        records.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.TRANSFER_APPLICATION.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
            }
        }

        for (TransferApplicationDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (CharSequenceUtil.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);
            //商品状态
            String saleStateName = productSaleEntityList.stream().filter(e -> CharSequenceUtil.equals(e.getSkuId(), obj.getSkuId())).findFirst().map(e -> SaleStateEnum.getNameByCode(e.getSaleState())).orElse("");
            obj.setSaleStateName(saleStateName);
            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(obj.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            if (CharSequenceUtil.isBlank(transferDirectionName)) {
                throw new ServiceException(ApiError.ERROR_99049);
            }
            obj.setTransferDirectionName(transferDirectionName);

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            //获取sku的子sku
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(obj.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                //设置组合品sku标识
                obj.setIsCombination(Boolean.TRUE);
            }
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && CharSequenceUtil.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
               obj.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,obj.getApproveUserName()));
            }
        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String inWarehouseId, String outWarehouseId, String applyUserId, TransferApplicationEntity entity) {

        //申请人
        if (CharSequenceUtil.isNotBlank(applyUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setApplyUserName(userDTO.getUserName());
            }
        }
        //仓库信息
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(inWarehouseId,outWarehouseId));

        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调入仓库
        WarehouseEntity inWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(inWarehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调出仓库
        WarehouseEntity outWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(outWarehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(inWarehouse.getOrgId(), outWarehouse.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        entity.setInWarehouseName(inWarehouse.getName());
        entity.setInOrgId(inWarehouse.getOrgId());
        //调入组织名称
        String inOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(inWarehouse.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInOrgName(inOrgName);
        entity.setOutOrgId(outWarehouse.getOrgId());
        entity.setOutWarehouseName(outWarehouse.getName());
        //调出组织名称
        String outOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(outWarehouse.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setOutOrgName(outOrgName);
    }

    /**
     * 根据ids查询数据
     */
    private List<TransferApplicationEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<TransferApplicationEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private Boolean updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

       return this.lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveUserId, userInfo.getUid())
                .set(TransferApplicationEntity::getApproveUserName, userInfo.getUserName())
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .set(TransferApplicationEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .set(TransferApplicationEntity::getApproveUserId, "")
                .set(TransferApplicationEntity::getApproveUserName, "")
                .set(TransferApplicationEntity::getApproveTime, null)
                .update();
    }



    /**
     * @description: 启动审核流程
     * @author Will
     * @date: 2023/8/2 14:51
     * @param entity
     */
    private void startProcess(TransferApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(entity.getApplyUserId());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @description: 结束深审核
     * @author Will
     * @date: 2023/8/2 14:58
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     */
    private void approveProcess(TransferApplicationEntity entity, String type, String comment, Boolean isNeedProcess) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(type));
        approveDTO.setComment(comment);
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        if (ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()){
            approveEnd(entity, type, comment, isNeedProcess);
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(TransferApplicationEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        //总计数量
        Integer qtyTotal = detailList.stream().map(TransferApplicationDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
        variablesMap.put("qtyTotal", qtyTotal);
        //SKU
        String skuNo = detailList.stream().map(TransferApplicationDetailEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        return variablesMap;
    }
}

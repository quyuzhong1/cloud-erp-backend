package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.TransferDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInfoService;
import com.erp.server.wms.mapper.TransferInfoMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferInfoServiceImpl extends SuperServiceImpl<TransferInfoMapper, TransferInfoEntity> implements TransferInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SyncKingdeeTransferInfoService syncKingdeeTransferInfoService;



    @Override
    public PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<TransferInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<TransferInfoDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setTransferDirection(null);
                obj.setTransferDirectionName(null);
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setApproveUserName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<TransferInfoDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            TransferInfoDTO.SearchParamDTO searchParamDTO = new TransferInfoDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            TransferInfoDTO.ListStatusCountDTO resultDTO = new TransferInfoDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setSearchType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(TransferInfoDTO.AddDTO dto) {
        TransferInfoEntity entity = new TransferInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getWarehouseKeeperId(), entity);
        log.info("直接调拨单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.ZJDB, BusinessNoTypeEnum.CODE_ZJDB.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个直接调拨单【%s】", code), ModuleTypeEnum.TRANSFER_INFO.getCode(), entity.getId(), "新增操作");
            //新增明细
            transferInfoDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(TransferInfoDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TransferInfoDTO.UpdateDTO dto) {

        TransferInfoEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        TransferInfoEntity entity = new TransferInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        List<TransferInfoDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getWarehouseKeeperId(), entity);

        log.info("直接调拨单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.TRANSFER_INFO.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        transferInfoDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //验证调出入仓库是否相同
        for (TransferInfoEntity entity : list) {
            if (entity.getInWarehouseId().equals(entity.getOutWarehouseId())) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,entity.getCode())));
            }
        }

        log.info("直接调拨单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个直接调拨单【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public TransferInfoDTO.ViewDTO view(String id) {
        TransferInfoDTO.ViewDTO viewDTO = new TransferInfoDTO.ViewDTO();
        //主表信息
        TransferInfoEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        List<TransferInfoDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.ViewDTO.class, detailList);

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isNotEmpty(transferDirectionList)) {
            String name = transferDirectionList.stream().filter(obj -> obj.getValue().equals(viewDTO.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse(null);
            viewDTO.setTransferDirectionName(name);
        }

        //产品信息
        List<String> skuIds = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        for (TransferInfoDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDetailDTO.setProductName(productName);
            }
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getOutWarehouseId(), viewDetailDTO.getSkuId());
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("直接调拨单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        transferInfoDetailService.removeByMainIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("直接调拨单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(TransferInfoEntity::getId, ids)
                .set(TransferInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(TransferInfoEntity::getInvalidRemark, reason)
                .update();

        //发送金蝶
        list.forEach(obj -> syncKingdeeTransferInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_INVALID.getCode()));

        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个直接调拨单【%s】，作废原因：".concat(reason), ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("直接调拨单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("直接调拨单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
            //更新库存
            updateInventoryTransCore(list);
            //发送金蝶
            list.forEach(obj -> syncKingdeeTransferInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("直接调拨单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个直接调拨单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("直接调拨单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_INFO,ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //发送金蝶
        list.forEach(obj -> syncKingdeeTransferInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));

        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个直接调拨单【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("直接调拨单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("直接调拨单【%s】取消流程", ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<TransferInfoDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandleData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/transferInfo.xlsx";
        String name = "直接调拨单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<TransferInfoEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery()
                .in(TransferInfoEntity::getSourceId,sourceIds)
                .eq(TransferInfoEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId,String operate) {
        return  this.lambdaUpdate()
                .eq(TransferInfoEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),TransferInfoEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),TransferInfoEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId),TransferInfoEntity::getSyncKingdeeId,syncKingdeeId)
                .set(StringUtils.isNotBlank(operate),TransferInfoEntity::getSyncOperate,operate)
                .update();
    }

    /**
     * @description:更新库存
     * @author Will
     * @date: 2023/5/15 15:19
     * @param list
     */
    private void updateInventoryTransCore (List<TransferInfoEntity> list) {
        List<String> ids = list.stream().map(TransferInfoEntity::getId).collect(Collectors.toList());
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        List<TransferDTO>  addTransferList = new ArrayList<>();
        List<TransferDTO>  pushTransferList = new ArrayList<>();

        for (TransferInfoDetailEntity detailEntity : detailList) {

            TransferInfoEntity transferInfoEntity = list.stream().filter(obj -> obj.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(transferInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_99047);
            }

            //调拨操作请求实体
            TransferDTO transferDTO = new TransferDTO();
            transferDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
            transferDTO.setSourceId(transferInfoEntity.getId());
            transferDTO.setSourceCode(transferInfoEntity.getCode());
            transferDTO.setSourceDetailId(detailEntity.getId());
            transferDTO.setBillDate(transferInfoEntity.getBillDate());
            transferDTO.setCurWarehouseId(transferInfoEntity.getOutWarehouseId());
            transferDTO.setCurWarehouseLocation(detailEntity.getOutWarehouseLocation());
            transferDTO.setTargetWarehouseId(transferInfoEntity.getInWarehouseId());
            transferDTO.setTargetWarehouseLocation(detailEntity.getInWarehouseLocation());
            transferDTO.setSkuId(detailEntity.getSkuId());
            transferDTO.setSkuNo(detailEntity.getSkuNo());
            transferDTO.setQty(detailEntity.getQty());
            if (SourceTypeEnum.SELF_ADD.getCode().equals(transferInfoEntity.getSourceType())) {
                addTransferList.add(transferDTO);
            } else {
                pushTransferList.add(transferDTO);
            }
        }
        //手动新增数据更新库存
        if (CollectionUtils.isNotEmpty(addTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setMembers(addTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DIRECT_ALLOCATE.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //下推数据更新库存
        if (CollectionUtils.isNotEmpty(pushTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setMembers(pushTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<TransferInfoDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(TransferInfoDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isEmpty(transferDirectionList)) {
            throw new ServiceException(ApiError.ERROR_99049);
        }

        for (TransferInfoDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(obj.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            if (StringUtils.isBlank(transferDirectionName)) {
                throw new ServiceException(ApiError.ERROR_99049);
            }
            obj.setTransferDirectionName(transferDirectionName);

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

        }
    }
    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String inWarehouseId, String outWarehouseId, String warehouseKeeperId, TransferInfoEntity entity) {

        //申请人
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setWarehouseKeeperName(userDTO.getUserName());
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

        //调拨类型
        if (inOrgName.equals(outOrgName))  {
            entity.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            entity.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<TransferInfoEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<TransferInfoEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(TransferInfoEntity::getId, ids)
                .set(TransferInfoEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(TransferInfoEntity::getId, ids)
                .set(TransferInfoEntity::getApproveUserId, userInfo.getUid())
                .set(TransferInfoEntity::getApproveUserName, userInfo.getUserName())
                .set(TransferInfoEntity::getApproveStatus, approveStatus)
                .set(TransferInfoEntity::getApproveTime, LocalDateTime.now())
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), TransferInfoEntity::getSyncKingdeeStatus, SyncKingdeeStatusEnum.TO_BE_SYNC.getCode())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(TransferInfoEntity::getId, ids)
                .set(TransferInfoEntity::getApproveStatus, approveStatus)
                .set(TransferInfoEntity::getApproveUserId, "")
                .set(TransferInfoEntity::getApproveUserName, "")
                .set(TransferInfoEntity::getApproveTime, null)
                .update();
    }
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeTransferInfoService;
import com.erp.server.wms.mabang.SyncMabangTransferService;
import com.erp.server.wms.mapper.TransferInfoDetailMapper;
import com.erp.server.wms.mapper.TransferInfoMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
@RefreshScope
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
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SyncKingdeeTransferInfoService syncKingdeeTransferInfoService;

    @Autowired
    private SyncMabangTransferService syncMabangTransferService;

    @Value("${transfer-sync-to-mb: true}")
    private Boolean transferSyncToMb;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;

    @Resource
    private TransferInfoDetailMapper transferInfoDetailMapper;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

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
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<TransferInfoDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            TransferInfoDTO.SearchParamDTO searchParamDTO = new TransferInfoDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            searchParamDTO.setInvalidStatus(Boolean.FALSE);
            TransferInfoDTO.ListStatusCountDTO resultDTO = new TransferInfoDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PageListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(TransferInfoDTO.AddDTO dto) {
        List<TransferInfoDetailDTO.AddDTO> detailList = dto.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            log.warn("调拨单的明细条数为0 dto={}", JSONUtil.toJsonStr(dto));
            throw new ServiceException(ApiError.ERROR_DETAIL_IS_ZERO, "调拨单");
        }
        List<TransferInfoDetailDTO.AddDTO> addDetailList = detailList.stream().filter(item -> item.getQty() > 0).collect(Collectors.toList());
        if(CollUtil.isEmpty(addDetailList)){
            log.warn("调拨单大于0的明细条数为0 dto={}", JSONUtil.toJsonStr(dto));
            throw new ServiceException(ApiError.ERROR_DETAIL_IS_ZERO, "调拨单数量大于0");
        }
        dto.setDetailList(addDetailList);
        TransferInfoEntity entity = new TransferInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(entity);
        log.info("直接调拨单新增");
        if (StringUtils.isBlank(dto.getCode())) {

            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZJDB);

            entity.setCode(code);
        }
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个直接调拨单【%s】", entity.getCode()), ModuleTypeEnum.TRANSFER_INFO.getCode(), entity.getId(), "新增操作");
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String addAndApprove(TransferInfoDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        //审核
        TransferInfoEntity entity = this.getById(id);
        if (Objects.nonNull(entity)){
            this.approve(entity,ApproveType.PASS,"", null , Boolean.TRUE);
        }
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
        //马帮直接调拨单不允许修改 TODO
        if (ThirdPartySystemEnum.ENUM_MB.getCode().equals(dto.getCode())) {
            throw new ServiceException(ApiError.ERROR_TRANSFER_MB_UPDATE);
        }

        TransferInfoEntity entity = new TransferInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        List<TransferInfoDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(entity);

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
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }

        //验证调出入仓库是否相同
        for (TransferInfoDetailEntity detailEntity : detailList) {
            String code = list.stream().filter(obj -> obj.getId().equals(detailEntity.getMainId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
            if (detailEntity.getInWarehouseId().equals(detailEntity.getOutWarehouseId())) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,code)));
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
        viewDTO.setTypeName(TransferTypeEnum.getNameByCode(entity.getType()));
        //产品信息
        List<String> skuIds = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        //查询仓位信息
        List<String> warehouseIdList = detailList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).distinct().collect(Collectors.toList());
        List<String> warehouseLocationCodeList = detailList.stream().map(r->StrUtils.null2EmptyWithTrim(r.getOutWarehouseLocation())).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdsAndCodeList(warehouseIdList,warehouseLocationCodeList);

        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIds);
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationCodeList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //可用数量
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
        for (TransferInfoDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).findFirst().orElse(new SkuVO());
                viewDetailDTO.setProductName(skuVO.getSkuName());
                viewDetailDTO.setSpuNo(skuVO.getSpuNo());
                viewDetailDTO.setVariantProperty(skuVO.getVariantProperty());
            }
            //根据组织、仓库、sku查询可用库存
            /*
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDetailDTO.getOutWarehouseId(), viewDetailDTO.getSkuId(),viewDetailDTO.getOutWarehouseLocation());
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
             */
            //即时库存
            Integer curInventoryQty = skuInventoryList.stream().filter(r ->Objects.equals(r.getSkuId(), viewDetailDTO.getSkuId())
                    && Objects.equals(r.getWarehouseId(), viewDetailDTO.getOutWarehouseId())
                    && Objects.equals(r.getWarehouseLocationId(), StrUtils.null2EmptyWithTrim(viewDetailDTO.getOutWarehouseLocation()))).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            viewDetailDTO.setCurInventoryQty(curInventoryQty);

            // 取仓位名称
            String outWarehouseLocationName = warehouseLocationList.stream().filter(r -> Objects.equals(r.getWarehouseId(), viewDetailDTO.getOutWarehouseId())
                        && Objects.equals(StrUtils.null2EmptyWithTrim(r.getCode()), StrUtils.null2EmptyWithTrim(viewDetailDTO.getOutWarehouseLocation()))).map(o->StrUtils.null2EmptyWithTrim(o.getName())).findFirst().orElse("");
            viewDetailDTO.setOutWarehouseLocationName(outWarehouseLocationName);

            String inWarehouseLocationName = warehouseLocationList.stream().filter(r -> Objects.equals(r.getWarehouseId(), viewDetailDTO.getInWarehouseId())
                    && Objects.equals(StrUtils.null2EmptyWithTrim(r.getCode()), StrUtils.null2EmptyWithTrim(viewDetailDTO.getInWarehouseLocation()))).map(o->StrUtils.null2EmptyWithTrim(o.getName())).findFirst().orElse("");
            viewDetailDTO.setInWarehouseLocationName(inWarehouseLocationName);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        String codes = list.stream().filter(obj -> ThirdPartySystemEnum.ENUM_MB.getCode().equals(obj.getCode())).map(TransferInfoEntity::getCode).collect(Collectors.joining(","));
        //马帮直接调拨单不允许删除 TODO
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_TRANSFER_MB_UPDATE,codes);
        }

        log.info("直接调拨单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        transferInfoDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的直接调拨单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(TransferInfoEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "删除操作");
        //删除发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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


        //作废发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个直接调拨单【%s】，作废原因：".concat(reason), ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public BatchResultDTO approve(TransferInfoEntity entity, String type, String comment, Boolean isNeedProcess, Boolean isSyncKingDee){
       //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        List<TransferInfoEntity> list = Arrays.asList(entity);
        log.info("直接调拨单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("直接调拨单【{}】审核通过，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(entity.getId(), ApproveStatusEnum.APPROVE.getStatus());
            //直接调拨单明细
            List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_99048);
            }
            //更新库存
            updateInventoryTransCore(Collections.singletonList(entity),detailList);
            //更新虚拟库存
            updateVirtualInventoryTransCore(list,detailList);
            if (isSyncKingDee) {
                //审核发送金蝶
                sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
                //同时发送旺店通
                list.forEach(item -> syncApproveInfoToWdt(item, SyncOperateEnum.OPERATE_APPROVE));
            }
            //发送马帮（非马帮平台的才需要推送）
            // TODO 正式上线时需注释掉
            log.warn("直接调拨单同步马帮开关：【{}】", transferSyncToMb);
            if(Objects.equals(transferSyncToMb, Boolean.TRUE)) {
                // 直接调拨单发送马帮出入库
                log.warn("审核直接调拨单【{}】第三方平台类型：【{}】", entity.getCode(), entity.getThirdPartySystem());
                if(!Objects.equals(entity.getThirdPartySystem(), ThirdPartySystemEnum.ENUM_MB.getCode())) {
                    log.warn("审核直接调拨单【{}】是非马帮平台的，需要同步到马帮平台出入库，直接调拨单参数：{}", entity.getCode(), JSONObject.toJSONString(entity));
                    syncMabangTransferService.syncDataToMabang(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
                }
            }
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("直接调拨单【{}】审核不通过，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(entity.getId(), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个直接调拨单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.TRANSFER_INFO.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 扣减虚拟库存
     * @author will
     * @date 2024/7/25 19:04
     * @param list
     */
    @Override
    public void updateVirtualInventoryTransCore (List<TransferInfoEntity> list,List<TransferInfoDetailEntity> detailList) {

        List<TransferInfoEntity> pushList = list.stream().filter(obj -> StrUtil.equals(SourceTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode(), obj.getSourceType())
                        || StrUtil.equals(SourceTypeEnum.REQUISITION_APPLICATION_FINISH.getCode(), obj.getSourceType()))
                .distinct().collect(Collectors.toList());
        //要货申请来源扣减冻结库存
        updateRequistionApplicationInventory(pushList,detailList);

        //头程发货单下推直接调拨单库存调整
        List<TransferInfoEntity> pushFirstMileDeliveryList = list.stream().filter(obj -> StrUtil.equals(SourceTypeEnum.FIRST_MILE_DELIVERY_TO_ULANZI.getCode(), obj.getSourceType())
                        || StrUtil.equals(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode(), obj.getSourceType()))
                .distinct().collect(Collectors.toList());
        updateFirstMileDeliveryInventory(pushFirstMileDeliveryList,detailList);

        //来源发货通知单
        List<TransferInfoEntity> transferInfoList = list.stream().filter(obj -> StrUtil.equals(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(), obj.getSourceType()))
                .distinct().collect(Collectors.toList());
        updateSoDeliveryNoticeInventory(transferInfoList,detailList);
    }

    /**
     * 发货通知单来源调拨出冻结
     * @author will
     * @date 2024/8/9 14:11
     * @param transferInfoList
     * @param detailList
     */
    private void updateSoDeliveryNoticeInventory (List<TransferInfoEntity> transferInfoList,List<TransferInfoDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(transferInfoList)) {
            return;
        }

        List<String> idList = transferInfoList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
        List<TransferInfoDetailEntity> pushDetailList = detailList.stream().filter(obj -> idList.contains(obj.getMainId())).collect(Collectors.toList());

        //发货通知单
        List<String> noticeIdList = transferInfoList.stream().map(TransferInfoEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoDeliveryNoticeEntity> soDeliveryNoticeList = soDeliveryNoticeService.listByIds(noticeIdList);
        //出冻结库存
        List<VirtualInventoryStockDTO.OutInStockDTO> outList = new ArrayList<>();

        for (TransferInfoDetailEntity transferInfoDetailEntity : pushDetailList) {
            //直接调拨单
            TransferInfoEntity transferInfoEntity = transferInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferInfoDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferInfoEntity)) {
                throw new ServiceException("直接调拨单未找到");
            }
            //发货通知单明细
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferInfoEntity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
                throw new ServiceException(StrUtil.format("直接调拨单【{}】未找到发货通知单",transferInfoEntity.getCode(),transferInfoDetailEntity.getSkuNo()));
            }
            if (StrUtil.isBlank(soDeliveryNoticeEntity.getVirtualWarehouseId())) {
                continue;
            }
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(transferInfoEntity.getId());
            outInStockDTO.setSourceCode(transferInfoEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
            outInStockDTO.setSourceDetailId(transferInfoDetailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(transferInfoDetailEntity.getSkuId());
            outInStockDTO.setSkuNo(transferInfoDetailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(transferInfoDetailEntity.getOutWarehouseId());
            outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
            outInStockDTO.setQty(transferInfoDetailEntity.getQty());
            outList.add(outInStockDTO);
        }
        if (CollectionUtils.isNotEmpty(outList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
            stockParamDTO.setParamList(outList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
    }


    /**
     * 头程发货单下推直接调拨单库存调整
     * @author will
     * @date 2024/7/25 22:23
     * @param pushFirstMileDeliveryList
     * @param detailList
     */
    private void updateFirstMileDeliveryInventory (List<TransferInfoEntity> pushFirstMileDeliveryList,List<TransferInfoDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(pushFirstMileDeliveryList)) {
            return;
        }

        List<String> idList = pushFirstMileDeliveryList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
        List<TransferInfoDetailEntity> pushDetailList = detailList.stream().filter(obj -> idList.contains(obj.getMainId())).collect(Collectors.toList());


        //头程发货单明细
        List<String> sourceDetailIdList = pushDetailList.stream().map(TransferInfoDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailService.listByIds(sourceDetailIdList);

        //要货申请明细
        List<String> applicationDetailIdList = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailList = requisitionApplicationDetailService.listByIds(applicationDetailIdList);
        if (CollectionUtils.isEmpty(requisitionApplicationDetailList)) {
            throw new ServiceException("未找到直接调拨单对应的要货申请明细");
        }
        //要货申请主表数据
        List<String> mainIdList = requisitionApplicationDetailList.stream().map(RequisitionApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationEntity> requisitionApplicationList = requisitionApplicationService.listByIds(mainIdList);

        //出冻结库存
        List<VirtualInventoryStockDTO.OutInStockDTO> outList = new ArrayList<>();

        for (TransferInfoDetailEntity transferInfoDetailEntity : pushDetailList) {
            //直接调拨单
            TransferInfoEntity transferInfoEntity = pushFirstMileDeliveryList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferInfoDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferInfoEntity)) {
                throw new ServiceException("直接调拨单未找到");
            }

            FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = firstMileDeliveryDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferInfoDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(firstMileDeliveryDetailEntity)) {
                throw new ServiceException("未找到头程发货单明细");
            }
            RequisitionApplicationDetailEntity applicationDetailEntity = requisitionApplicationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), firstMileDeliveryDetailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(applicationDetailEntity)) {
                throw new ServiceException("未找到要货申请明细");
            }
            RequisitionApplicationEntity applicationEntity = requisitionApplicationList.stream().filter(obj -> StrUtil.equals(obj.getId(), applicationDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(applicationEntity)) {
                throw new ServiceException("未找到直接调拨单对应的要货申请信息");
            }
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(transferInfoEntity.getId());
            outInStockDTO.setSourceCode(transferInfoEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
            outInStockDTO.setSourceDetailId(transferInfoDetailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(applicationDetailEntity.getSkuId());
            outInStockDTO.setSkuNo(applicationDetailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
            if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
            outInStockDTO.setQty(transferInfoDetailEntity.getQty());
            outList.add(outInStockDTO);
        }
        if (CollectionUtils.isNotEmpty(outList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
            stockParamDTO.setParamList(outList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
    }


    /**
     * 要货申请下推数据库存调整
     * @author will
     * @date 2024/7/25 22:01
     * @param pushList
     * @param detailList
     */
    private void updateRequistionApplicationInventory (List<TransferInfoEntity> pushList,List<TransferInfoDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(pushList)) {
            return;
        }
        List<String> transferIdList = pushList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
        List<TransferInfoDetailEntity> pushDetailList = detailList.stream().filter(obj -> transferIdList.contains(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pushDetailList)) {
            return;
        }
        //要货申请数据
        List<String> sourceDetailIdList = pushDetailList.stream().map(TransferInfoDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailList = requisitionApplicationDetailService.listByIds(sourceDetailIdList);

        //要货申请下推直接调拨单出冻结
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();

        for (TransferInfoDetailEntity detailEntity : pushDetailList) {

            TransferInfoEntity transferInfoEntity = pushList.stream().filter(obj -> obj.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(transferInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_99047);
            }
            //要货申请明细数据
            RequisitionApplicationDetailEntity applicationDetailEntity = requisitionApplicationDetailList.stream()
                    .filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(applicationDetailEntity)) {
                throw new ServiceException("要货申请不能为空");
            }
            //虚拟仓库存随调出仓出
            if (!StrUtil.equals(applicationDetailEntity.getFromWarehouseId(),detailEntity.getOutWarehouseId())) {
                continue;
            }
            //调拨操作请求实体
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
            outInStockDTO.setSourceId(transferInfoEntity.getId());
            outInStockDTO.setSourceCode(transferInfoEntity.getCode());
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setQty(detailEntity.getQty());
            outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
            if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
            paramList.add(outInStockDTO);
        }
        if (CollectionUtils.isEmpty(paramList)) {
            return;
        }
        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setParamList(paramList);
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
        virtualInventoryTransCoreService.approve(dto);
    }

    /**
     * 直接调拨单审核通过时将数据同步给旺店通
     *
     * @param entity 直接调拨单主数据
     * @date: 2024-05-17
     * @author: tanmujin
     */
    private void syncApproveInfoToWdt(TransferInfoEntity entity, SyncOperateEnum syncOperateEnum) {
        //查询直接调拨单明细数据
        List<TransferInfoDetailEntity> transferDetailList = transferInfoDetailService.listByMainId(entity.getId());
        HashSet<String> warehouseIdSet = new HashSet<>();
        for (TransferInfoDetailEntity detail : transferDetailList) {
            warehouseIdSet.add(detail.getInWarehouseId());
            warehouseIdSet.add(detail.getOutWarehouseId());
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //每个调出仓转换为一个其他出库单
        Map<String, List<TransferInfoDetailEntity>> outWarehouseCollect = transferDetailList.stream().collect(Collectors.groupingBy(item -> item.getOutWarehouseId()));
        for (Map.Entry<String, List<TransferInfoDetailEntity>> entry : outWarehouseCollect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
            for (TransferInfoDetailEntity detailEntity : entry.getValue()) {
                CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                outGoods.setSpecNo(detailEntity.getSkuNo());
                outGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
                outGoods.setPositionNo(StringUtils.isNotBlank(detailEntity.getOutWarehouseLocation()) ? detailEntity.getOutWarehouseLocation() : "");
                outGoods.setWarehouseId(warehouseId);
                outGoodsList.add(outGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
        }
        //每个调入仓转换为一个其他入库单
        Map<String, List<TransferInfoDetailEntity>> inWarehouseCollect = transferDetailList.stream().collect(Collectors.groupingBy(item -> item.getInWarehouseId()));
        for (Map.Entry<String, List<TransferInfoDetailEntity>> entry : inWarehouseCollect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
            for (TransferInfoDetailEntity detailEntity : entry.getValue()) {
                CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                inGoods.setSpecNo(detailEntity.getSkuNo());
                inGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
                inGoods.setPositionNo(StringUtils.isNotBlank(detailEntity.getInWarehouseLocation()) ? detailEntity.getInWarehouseLocation() : "");
                inGoods.setWarehouseId(warehouseId);
                inGoodsList.add(inGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
        }
    }

    /**
     * 反审核的调拨单转换成调出仓的其他入库单、调入仓的其他出库单同步至旺店通
     * @param entity 直接调拨单主数据
     * @date: 2024-05-17
     * @author: tanmujin
     */
    private void syncDisApproveInfoToWdt(TransferInfoEntity entity, SyncOperateEnum syncOperateEnum) {
        //查询直接调拨单明细数据
        List<TransferInfoDetailEntity> transferDetailList = transferInfoDetailService.listByMainId(entity.getId());
        if(transferDetailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }

        HashSet<String> warehouseIdSet = new HashSet<>();
        for (TransferInfoDetailEntity detail : transferDetailList) {
            warehouseIdSet.add(detail.getInWarehouseId());
            warehouseIdSet.add(detail.getOutWarehouseId());
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //每个调入仓转换为一个其他出库单
        Map<String, List<TransferInfoDetailEntity>> inWarehouseCollect = transferDetailList.stream().collect(Collectors.groupingBy(item -> item.getInWarehouseId()));
        for (Map.Entry<String, List<TransferInfoDetailEntity>> entry : inWarehouseCollect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
            for (TransferInfoDetailEntity detailEntity : entry.getValue()) {
                CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                outGoods.setSpecNo(detailEntity.getSkuNo());
                outGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
                outGoods.setPositionNo(StringUtils.isNotBlank(detailEntity.getInWarehouseLocation()) ? detailEntity.getInWarehouseLocation() : "");
                outGoods.setWarehouseId(warehouseId);
                outGoodsList.add(outGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
        }

        //每个调出仓转换为一个其他入库单
        Map<String, List<TransferInfoDetailEntity>> outWarehouseCollect = transferDetailList.stream().collect(Collectors.groupingBy(item -> item.getOutWarehouseId()));
        for (Map.Entry<String, List<TransferInfoDetailEntity>> entry : outWarehouseCollect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
            for (TransferInfoDetailEntity detailEntity : entry.getValue()) {
                CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                inGoods.setSpecNo(detailEntity.getSkuNo());
                inGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
                inGoods.setPositionNo(StringUtils.isNotBlank(detailEntity.getOutWarehouseLocation()) ? detailEntity.getOutWarehouseLocation() : "");
                inGoods.setWarehouseId(warehouseId);
                inGoodsList.add(inGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public BatchResultDTO disApprove(TransferInfoEntity entity, Boolean isPushKingDee,Boolean isManual) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        List<TransferInfoEntity> list = Arrays.asList(entity);
        log.info("直接调拨单反审核，id=【{}】", entity.getId());
        //发货单来源和发货通知单来源的直接调拨单不允许手动反审核
        if (isManual) {
            String codeList = list.stream().filter(obj -> StrUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_B2C_DELIVERY.getCode())
                            || StrUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_DELIVERY_NOTICE.getCode()))
                    .map(TransferInfoEntity::getCode).collect(Collectors.joining(","));
            if (StrUtil.isNotBlank(codeList)) {
                throw new ServiceException(StrUtil.format("直接调拨单【{}】不支持手动反审核",codeList));
            }
        }

        log.info("直接调拨单反审核，id=【{}】", entity.getId());

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO(InventorySourceTypeEnum.TRANSFER_INFO,entity.getId());
        inventoryTransCoreService.unApprove(inventoryUnApproveDTO);

        //虚拟仓回退库存
        virtualInventoryTransCoreService.unApprove(inventoryUnApproveDTO);
        if(isPushKingDee){
            //反审核发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        //发送旺店通
        list.forEach(obj -> syncDisApproveInfoToWdt(obj, SyncOperateEnum.OPERATE_DISAPPROVE));

        //发送马帮（非马帮平台的才需要推送）
        // TODO 正式上线时需注释掉
        log.warn("直接调拨单同步马帮开关：【{}】", transferSyncToMb);
        if(Objects.equals(transferSyncToMb, Boolean.TRUE)) {
            // 直接调拨单发送马帮出入库
            log.warn("反审核直接调拨单【{}】第三方平台类型：【{}】", entity.getCode(), entity.getThirdPartySystem());
            if(!Objects.equals(entity.getThirdPartySystem(), ThirdPartySystemEnum.ENUM_MB.getCode())) {
                log.warn("反审核直接调拨单【{}】是非马帮平台的，需要同步到马帮平台出入库，直接调拨单参数：{}", entity.getCode(), JSONObject.toJSONString(entity));
                syncMabangTransferService.syncDataToMabang(entity, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
            }
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个直接调拨单【%s】", entity.getCode()), ModuleTypeEnum.TRANSFER_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(TransferInfoEntity::getId,id)
                .ne(TransferInfoEntity::getThirdPartySystem,ThirdPartySystemEnum.ENUM_MB.getCode())
                .set(StringUtils.isNotBlank(syncKingdeeId),TransferInfoEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    @Override
    public TransferInfoDTO.ViewDTO viewTransferInfoByCode(String code) {

        TransferInfoDTO.ViewDTO viewDTO = new TransferInfoDTO.ViewDTO();
        //主表信息
        TransferInfoEntity entity = this.getTransferInfoByCode(code);
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        List<TransferInfoDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.ViewDTO.class, detailList);
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    public String checkSkuInventory(TransferInfoDTO.CommonDTO dto, List<TransferInfoDetailDTO.AddDTO> detailList) {
        StringBuffer errmsg = new StringBuffer("");

        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()): Lists.newArrayList();

        Map<String, List<TransferInfoDetailDTO.AddDTO>> multiTransferMap = detailList.stream().collect(
                Collectors.groupingBy(r -> r.getOutWarehouseId() + "-" + r.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(r.getOutWarehouseLocation()), Collectors.toList()));
        //仓位信息
        List<String> warehouseIdList = detailList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).distinct().collect(Collectors.toList());
        List<String> warehouseLocationCodeList = detailList.stream().map(r->StrUtils.null2EmptyWithTrim(r.getOutWarehouseLocation())).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdsAndCodeList(warehouseIdList,warehouseLocationCodeList);

        multiTransferMap.forEach((key, multiList)->{
            String warehouseId = multiList.get(0).getOutWarehouseId();
            String skuId = multiList.get(0).getSkuId();
            String warehouseLocation = StrUtils.null2EmptyWithTrim(multiList.get(0).getOutWarehouseLocation());
            String skuNo = multiList.get(0).getSkuNo();
            // 合计调拨数量
            int sumQty = multiList.stream().mapToInt(TransferInfoDetailDTO.AddDTO::getQty).sum();
            log.warn("sku id【{}】库位【{}】 合计调出数量【{}】",  warehouseId, skuId, warehouseLocation, sumQty);
            List<InventoryQtyDTO.SkuInventoryTotalDTO> inventoryList = inventoryService.listSkuInventory(Lists.newArrayList(skuId), warehouseId,
                    warehouseLocation, InventoryStatusEnum.USABLE.getCode());
            //即时库存
            Integer curInventoryQty = inventoryList.stream().filter(r -> Objects.equals(r.getSkuId(), skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);

            log.warn("仓库id【{}】sku id【{}】库位【{}】 合计调出数量【{}】实时库存数量", warehouseId, skuId, warehouseLocation,
                    sumQty, curInventoryQty);
            Boolean isScarce = curInventoryQty < sumQty;
            if(isScarce && !ignoreInventorySkuIds.contains(skuId)) {
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(warehouseId, (v) -> warehouseService.detailWithCache(v));
                String warehouseName = Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId()) ? warehouseDetail.getName() : "";
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationList.stream().filter(req -> req.getWarehouseId().equals(warehouseId) && req.getCode().equals(warehouseLocation)).findFirst().orElse(new WarehouseLocationEntity());

                String msg = StrUtil.format("仓库【{}】仓位【{}】SKU【{}】【缺货：{}个】", warehouseName, warehouseLocationEntity.getName(), skuNo, (sumQty - curInventoryQty));
                errmsg.append(msg).append("</br>");
            }
        });
        return errmsg.toString();
    }

    /**
     * @description: 根据编码查询
     * @author Will
     * @date: 2023/6/28 18:52
     * @param code
     * @return TransferInfoEntity
     */
    private TransferInfoEntity getTransferInfoByCode (String code) {
        return lambdaQuery().eq(TransferInfoEntity::getCode, code)
                .eq(TransferInfoEntity::getInvalidStatus, Boolean.FALSE)
                .one();
    }

    /**
     * @description:更新库存
     * @author Will
     * @date: 2023/5/15 15:19
     * @param list
     */
    private void updateInventoryTransCore (List<TransferInfoEntity> list, List<TransferInfoDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(detailList)) {
            return;
        }

        List<TransferDTO>  addTransferList = new ArrayList<>();
        List<TransferDTO>  pushTransferList = new ArrayList<>();
        List<TransferDTO>  deliveryTransferList = new ArrayList<>();
        List<TransferDTO>  deliveryNoticeTransferList = new ArrayList<>();
        List<TransferDTO>  soInfoTransferList = new ArrayList<>();
        List<TransferDTO>  requisitionTransferList = new ArrayList<>();
        List<TransferDTO>  firstMileTransferList = new ArrayList<>();
        List<TransferDTO>  TransferToUlanziList = new ArrayList<>();
        List<TransferDTO>  TransferFromUlanziList = new ArrayList<>();

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
            transferDTO.setCurWarehouseId(detailEntity.getOutWarehouseId());
            transferDTO.setCurWarehouseLocation(detailEntity.getOutWarehouseLocation());
            transferDTO.setTargetWarehouseId(detailEntity.getInWarehouseId());
            transferDTO.setTargetWarehouseLocation(detailEntity.getInWarehouseLocation());
            transferDTO.setSkuId(detailEntity.getSkuId());
            transferDTO.setSkuNo(detailEntity.getSkuNo());
            transferDTO.setQty(detailEntity.getQty());
            if (SourceTypeEnum.TRANSFER_APPLICATION.getCode().equals(transferInfoEntity.getSourceType())) {
                pushTransferList.add(transferDTO);
            } else if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(transferInfoEntity.getSourceType())){
                deliveryTransferList.add(transferDTO);
            } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(transferInfoEntity.getSourceType()) ){
                deliveryNoticeTransferList.add(transferDTO);
            } else if (SourceTypeEnum.SO_INFO.getCode().equals(transferInfoEntity.getSourceType())){
                soInfoTransferList.add(transferDTO);
            }else if (SourceTypeEnum.FIRST_MILE_DELIVERY_TO_ULANZI.getCode().equals(transferInfoEntity.getSourceType())){
                TransferToUlanziList.add(transferDTO);
            } else if (SourceTypeEnum.FIRST_MILE_DELIVERY_FROM_ULANZI.getCode().equals(transferInfoEntity.getSourceType())){
                TransferFromUlanziList.add(transferDTO);
            } else if (SourceTypeEnum.REQUISITION_APPLICATION_FINISH.getCode().equals(transferInfoEntity.getSourceType())) {
                requisitionTransferList.add(transferDTO);
            } else if (SourceTypeEnum.FIRST_MILE_DELIVERY.getCode().equals(transferInfoEntity.getSourceType())) {
                firstMileTransferList.add(transferDTO);
            }else {
                addTransferList.add(transferDTO);
            }
        }
        //手动新增数据更新库存
        if (CollectionUtils.isNotEmpty(addTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(addTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DIRECT_ALLOCATE.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //调拨申请下推数据更新库存
        if (CollectionUtils.isNotEmpty(pushTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(pushTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //B2C发货单下推数据更新库存
        if (CollectionUtils.isNotEmpty(deliveryTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(deliveryTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DELIVERY_PUSH_TRANSFER.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //B2B发货通知单下推数据更新库存
        if (CollectionUtils.isNotEmpty(deliveryNoticeTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(deliveryNoticeTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DELIVERY_NOTICE_PUSH_TRANSFER.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        if (CollectionUtils.isNotEmpty(soInfoTransferList)) {
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(soInfoTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.SO_INFO_PUSH_TRANSFER.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //头程发货单生成调拨单（发货仓-优蓝子中转仓）
        if(CollectionUtils.isNotEmpty(TransferToUlanziList)){
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(TransferToUlanziList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DELIVERY_PUSH_TRANSFER_TO_ULANZI.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        //头程发货单生成调拨单（优蓝子中转仓-目的仓在途仓）
        if(CollectionUtils.isNotEmpty(TransferFromUlanziList)){
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(TransferFromUlanziList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.DELIVERY_PUSH_TRANSFER_FROM_ULANZI.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        if(CollectionUtils.isNotEmpty(requisitionTransferList)){
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(requisitionTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.REQUISITION_PUSH_TRANSFER.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
        }
        if(CollectionUtils.isNotEmpty(firstMileTransferList)){
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(firstMileTransferList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.FIRST_MILE_PUSH_TRANSFER.getCode());
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


        //查询仓位信息
        List<String> warehouseIdList = records.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).distinct().collect(Collectors.toList());
        List<String> warehouseLocationCodeList = records.stream().flatMap(obj -> Stream.of(obj.getInWarehouseLocation(), obj.getOutWarehouseLocation())).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdsAndCodeList(warehouseIdList,warehouseLocationCodeList);

        for (TransferInfoDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);
            //来源类型名称
            obj.setSourceTypeName(SourceTypeEnum.getName(obj.getSourceType()));

            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(obj.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            if (StringUtils.isBlank(transferDirectionName)) {
                throw new ServiceException(ApiError.ERROR_99049);
            }
            obj.setTransferDirectionName(transferDirectionName);

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

            //仓位名称
            String inWarehouseLocationName = warehouseLocationList.stream().filter(req -> req.getWarehouseId().equals(obj.getInWarehouseId()) && req.getCode().equals(obj.getInWarehouseLocation()))
                    .map(WarehouseLocationEntity::getName).findFirst().orElse("");
            obj.setInWarehouseLocationName(inWarehouseLocationName);
            String outWarehouseLocationName = warehouseLocationList.stream().filter(req -> req.getWarehouseId().equals(obj.getOutWarehouseId()) && req.getCode().equals(obj.getOutWarehouseLocation()))
                    .map(WarehouseLocationEntity::getName).findFirst().orElse("");
            obj.setOutWarehouseLocationName(outWarehouseLocationName);
        }
    }
    /**
     * 处理数据id
     */
    private void doOpHandleDataId( TransferInfoEntity entity) {

        //申请人
        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getInOrgId(),entity.getOutOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }

        //调入组织名称
        String inOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInOrgName(inOrgName);

        //调出组织名称
        String outOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getOutOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
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
    private void updateApproveStatusForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().eq(TransferInfoEntity::getId, id)
                .set(TransferInfoEntity::getApproveUserId, userInfo.getUid())
                .set(TransferInfoEntity::getApproveUserName, userInfo.getUserName())
                .set(TransferInfoEntity::getApproveStatus, approveStatus)
                .set(TransferInfoEntity::getApproveTime, LocalDateTime.now())
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

    @Override
    public PagingVO<TransferInfoDTO.PdaListDTO> pdaPaging(PagingDTO<TransferInfoDTO.PdaSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        TransferInfoDTO.PdaSearchParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<TransferInfoDTO.PdaListDTO> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<TransferInfoDTO.PdaListDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<TransferInfoDetailEntity> transferInfoDetailEntities = transferInfoDetailService.listByMainIds(ids);
        for (TransferInfoDTO.PdaListDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<TransferInfoDetailEntity> detailEntities = transferInfoDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<TransferInfoDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, TransferInfoDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferInfoDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<TransferInfoDTO.PdaListStatusCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            TransferInfoDTO.SearchParamDTO pagingParamDTO = new TransferInfoDTO.SearchParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            TransferInfoDTO.PdaListStatusCountDTO resultDTO = new TransferInfoDTO.PdaListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @Transactional
    public String generateFromOverseasInbound(OverseasWarehouseInboundEntity mainEntity, List<OverseasWarehouseInboundDetailEntity> detailList, List<OverseasWarehouseInboundReceivedEntity> receivedEntityList, String remark,Boolean isToOnwayWarehouse) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(mainEntity.getToWarehouseId(), mainEntity.getDeliveryWarehouseId()));
        //目的仓
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream()
                .filter(req -> req.getId().equals(mainEntity.getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (org.apache.commons.lang3.StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }

        WarehouseEntity destWarehouseEntity = warehouseService.getById(destWarehouse.getId());
        //查询在途仓
        WarehouseEntity onWayWarehouseEntity = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        WarehouseEntity toWarehouseEntity = isToOnwayWarehouse?onWayWarehouseEntity:destWarehouseEntity;
        WarehouseEntity fromWarehouseEntity = isToOnwayWarehouse?destWarehouseEntity:onWayWarehouseEntity;

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：海外仓入库单
        addDTO.setSourceType(SourceTypeEnum.OVERSEAS_INBOUND.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(mainEntity.getReceiveTime().toLocalDate());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(toWarehouseEntity.getOrgId());
        //调出组织
        addDTO.setOutOrgId(fromWarehouseEntity.getOrgId());
        //调拨类型
        if (destWarehouseEntity.getOrgId().equals(onWayWarehouseEntity.getOrgId())) {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceCode(mainEntity.getCode());
        addDTO.setRemark(remark);

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        Map<String,OverseasWarehouseInboundDetailEntity> detailEntityMap = detailList.stream().collect(Collectors.toMap(OverseasWarehouseInboundDetailEntity::getId, Function.identity()));
        //根据签收记录封装明细
        for(OverseasWarehouseInboundReceivedEntity receivedEntity : receivedEntityList){
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            OverseasWarehouseInboundDetailEntity detailEntity = detailEntityMap.get(receivedEntity.getDetailId());
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(Math.abs(receivedEntity.getReceiveQty()));
            detailAddDto.setOutWarehouseId(fromWarehouseEntity.getId());
            detailAddDto.setOutWarehouseLocation("");
            detailAddDto.setInWarehouseId(toWarehouseEntity.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(receivedEntity.getId());
            detailAddDto.setRemark(mainEntity.getCode());
            detailAddDtoList.add(detailAddDto);
        }

        addDTO.setDetailList(detailAddDtoList);
        return this.addAndApprove(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requisitionApplicationCancelProcess(String code, String sourceType) {
        List<TransferInfoEntity> list = lambdaQuery()
                .eq(TransferInfoEntity::getSourceCode, code)
                .eq(TransferInfoEntity::getSourceType, sourceType)
                .list();

        //反审核，删除调拨单
        for (TransferInfoEntity entity : list) {

            //如果是已审核，反审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
                try {
                    this.disApprove(entity, Boolean.TRUE, Boolean.TRUE);
                } catch (Exception e) {
                    throw new ServiceException(ApiError.TRANSFER_INFO_ERROR_NOT_CANCEL_PROCESS, entity.getCode());
                }
            }

            //如果是审核中，撤销
            if (ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
                try {
                    this.cancelProcess(Arrays.asList(entity.getId()));
                } catch (Exception e) {
                    throw new ServiceException(ApiError.TRANSFER_INFO_CANCEL_PROCESS_ERROR, entity.getCode());
                }
            }

            //删除
            this.requisitionApplicationDelete(Arrays.asList(entity.getId()));
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean requisitionApplicationDelete(List<String> ids) {
        //根据ids查询
        List<TransferInfoEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())
                && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        String codes = list.stream().filter(obj -> ThirdPartySystemEnum.ENUM_MB.getCode().equals(obj.getCode())).map(TransferInfoEntity::getCode).collect(Collectors.joining(","));
        //马帮直接调拨单不允许删除 TODO
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_TRANSFER_MB_UPDATE,codes);
        }

        log.info("直接调拨单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        transferInfoDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的直接调拨单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(TransferInfoEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_INFO.getCode(), pairList, "删除操作");
        //删除发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkHistoryAndDel(String sourceCode, String sourceType, LocalDate billDate) {
        List<TransferInfoEntity> list = this.lambdaQuery()
                .eq(TransferInfoEntity::getSourceCode, sourceCode)
                .eq(TransferInfoEntity::getSourceType, sourceType)
                .eq(TransferInfoEntity::getBillDate, billDate)
                .eq(TransferInfoEntity::getInvalidStatus, false)
                .list();
        if (CollectionUtils.isEmpty(list)){
            return true;
        }
        List<TransferInfoEntity> approveList = list.stream()
                .filter(e -> ApproveStatusEnum.APPROVE.getStatus().equalsIgnoreCase(e.getApproveStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(approveList)){
            approveList.forEach(transferInfoEntity -> {
                // 反审核
                disApprove(transferInfoEntity, true, Boolean.TRUE);
            });
        }

        List<TransferInfoEntity> unSubmitList = list.stream()
                .filter(e -> ApproveStatusEnum.APPROVE_ING.getStatus().equalsIgnoreCase(e.getApproveStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unSubmitList)){
            List<String> unSubmitIds = unSubmitList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            cancelProcess(unSubmitIds);
        }
        List<String> delIds = list.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        // 删除
        delete(delIds);
        return true;
    }

    @Override
    public List<TransferInfoEntity> listByBatchNoList(List<String> batchNoList) {
        if (CollectionUtils.isEmpty(batchNoList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TransferInfoEntity::getBatchNo,batchNoList).list();
    }

    @Override
    public List<TransferInfoEntity> listBySourceId(String sourceId) {
        return lambdaQuery().eq(TransferInfoEntity::getSourceId,sourceId).list();
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<TransferInfoEntity> list,String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeTransferInfoService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    /**
     * 生成中间表数据
     */
    private DmpPushWdtDTO.AddDTO generateWdtInterim(TransferInfoEntity entity, String operateCode, String warehouseId, String outCode, String thirdWarehouseCode, List<? extends CommonCreateBillGoodsReq> outGoods, SourceTypeEnum sourceTypeEnum) {
        DmpPushWdtDTO.AddDTO pushWdtDTO = new DmpPushWdtDTO.AddDTO();
        pushWdtDTO.setSourceId(entity.getId());
        pushWdtDTO.setSourceCode(entity.getCode());
        pushWdtDTO.setThirdCode(outCode);
        pushWdtDTO.setWarehouseId(warehouseId);
        pushWdtDTO.setThirdWarehouseCode(thirdWarehouseCode);
        pushWdtDTO.setThirdType(sourceTypeEnum.getCode());
        pushWdtDTO.setOperateType(operateCode);
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(outGoods, DmpPushWdtDetailDTO.class);
        pushWdtDTO.setDetailDTOList(detailDTOList);
        return pushWdtDTO;
    }
}

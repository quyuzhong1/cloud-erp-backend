package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.dto.SubcontractChangeDetailDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSubcontractChangeService;
import com.erp.server.scm.mapper.SubcontractChangeMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUBCONTRACT_CHANGE_ORDER;

/**
 * <p>
 * 委外变更单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractChangeServiceImpl extends SuperServiceImpl<SubcontractChangeMapper, SubcontractChangeEntity> implements SubcontractChangeService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ModuleOperateLogService operateLogService;


    @Autowired
    private SubcontractChangeDetailService subcontractChangeDetailService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Autowired
    private SubcontractOrderDetailService subcontractOrderDetailService;


    @Autowired
    private SyncKingdeeSubcontractChangeService syncKingdeeSubcontractChangeService;

    @Autowired
    private SubcontractOrderService subcontractOrderService;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;


    @Override
    public PagingVO<SubcontractChangeDTO.ListDTO> paging(PagingDTO<SubcontractChangeDTO.PagingParamDTO> pagingParamDTO) {
        SubcontractChangeDTO.PagingParamDTO params = pagingParamDTO.getParams();
        params.setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<SubcontractChangeDTO.TabListDTO> list = new ArrayList<>();
        for (PageListTypeEnum item: values) {
            SubcontractChangeDTO.PagingParamDTO searchParamDTO = new SubcontractChangeDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(param.getPermissionSql());
            SubcontractChangeDTO.TabListDTO resultDTO = new SubcontractChangeDTO.TabListDTO();
            //搜索类型
            searchParamDTO.setSearchType(item.getCode());
            //列表Tab查询状态处理
            Boolean isFlag = doOpHandleTableParam(searchParamDTO);
            Integer count = MathUtil.ZERO;
            if (isFlag) {
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setSearchType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    private Boolean doOpHandleTableParam (SubcontractChangeDTO.PagingParamDTO params) {
        List<String> approveStatusList = new ArrayList<>(1);
        //待我审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        //已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (PageListTypeEnum.REJECT.getCode().equals(params.getSearchType())) {
            approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        if (CollectionUtils.isNotEmpty(approveStatusList)) {
            params.setApproveStatusList(approveStatusList);
        }
        return Boolean.TRUE;
    }

    @Override
    public void exportList(SubcontractChangeDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("委外变更单导出", EXPORT_SCM_SUBCONTRACT_CHANGE_ORDER.getCode(), param);
    }

    @Override
    public void invalid(List<String> ids, String remark) {
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外变更单数据");
        }
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("委外变更单作废，ids=【{}】", JSONUtil.toJsonStr(ids));
        //更新订单作废状态
        updateInvalidStatus(ids, remark);

        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个委外变更单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), pairList, "作废操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SubcontractChangeDTO.AddDTO addDTO) {
        SubcontractChangeEntity subcontractChangeEntity = new SubcontractChangeEntity();
        BeanMapperUtils.copy(addDTO, subcontractChangeEntity);

        // 数据处理
        handleData(subcontractChangeEntity);

        log.info("开始新增委外变更单");
        // 生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.SUBCH, BusinessNoTypeEnum.CODE_SUBCH.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SUBCH);
        subcontractChangeEntity.setCode(code);
        boolean save = super.save(subcontractChangeEntity);
        if(!save) {
           throw new ServiceException("委外变更单保存失败");
        }
        //新增明细
        subcontractChangeDetailService.add(addDTO.getDetailList(),subcontractChangeEntity.getId());

        // 操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个委外变更单【%s】", code), ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), subcontractChangeEntity.getId(), "新增操作");

        return subcontractChangeEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SubcontractChangeDTO.UpdateDTO updateDTO) {
        SubcontractChangeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到委外变更单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        SubcontractChangeEntity subcontractChangeEntity =  BeanMapperUtils.map(SubcontractChangeEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractChangeEntity);

        log.info("编辑 开始修改委外变更单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractChangeEntity);
        if(!save) {
           throw new ServiceException("委外变更单保存失败");
        }

        //修改明细
        subcontractChangeDetailService.update(updateDTO.getDetailList(),subcontractChangeEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录委外变更单日志数据，单号：【{}】", subcontractChangeEntity.getCode());
        operateLogService.addModuleOperateLogByObj(old, subcontractChangeEntity, ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), subcontractChangeEntity.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<SubcontractChangeEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到委外变更单数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }

       // 更新单据审核状态
       log.info("提交 开始修改委外变更单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // TODO 启动流程（如果需要的话）

       // 记录操作日志
       log.info("提交 开始记录委外变更单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       operateLogService.batchAddModuleOperateLog("提交了一个委外变更单【%s】", ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(SubcontractChangeDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractChangeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(Arrays.asList(dto.getId()));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
           throw new ServiceException("审核不通过请填写审核意见");
        }
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外变更单数据");
        }
        // 审核中的数据允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 新审核状态
        ApproveStatusEnum approveStatus = Objects.equals(ApproveTypeEnum.PASS, approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT;
        if(Objects.equals(ApproveTypeEnum.PASS, approveType)) {
           // TODO 审核通过流程处理

            //审核通过更新委外订单
            handleSubcontractOrder(ids,list);

            //审核通过发送金蝶(防止数据先删除导致查不到，需要先发送金蝶)
            List<DmpPushTaskEntity> resultList = new ArrayList<>();
            list.forEach(obj -> {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeSubcontractChangeService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
                resultList.add(pushTaskEntity);
            });
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(resultList);
                }
            });

        } else if (Objects.equals(ApproveTypeEnum.REJECT, approveType)) {
           // TODO 终止审批流程
        }

        // 更新审核信息
        updateForApprove(ids, approveStatus.getStatus());

        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个委外变更单", approveType.getName()).concat("【%s】").concat(StrUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), pairList, "审核操作");
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SubcontractChangeEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SubcontractChangeEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public List<SubcontractChangeEntity> listBySourceIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return  Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SubcontractChangeEntity::getSourceId,ids)
                .eq(SubcontractChangeEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @Override
    public PagingVO<SubcontractChangeDTO.ListDTO> exportSubcontractChangeOrder(PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto) {
        Page<SubcontractChangeDTO.ListDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    /**
     * @description: 审核通过更新委外订单
     * @author Will
     * @date: 2023/6/20 10:08
     * @param ids
     * @param list
     */
    private void handleSubcontractOrder(List<String> ids,List<SubcontractChangeEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<SubcontractChangeDetailEntity> detailEntityList = subcontractChangeDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_98085);
        }
        //新增
        List<SubcontractChangeDetailEntity> addList = detailEntityList.stream().filter(obj -> OptChangeTypeEnum.ADD.getCode().equals(obj.getOptType())).collect(Collectors.toList());
        //修改
        List<SubcontractChangeDetailEntity> updateList = detailEntityList.stream().filter(obj -> OptChangeTypeEnum.UPDATE.getCode().equals(obj.getOptType())).collect(Collectors.toList());
        //删除
        List<SubcontractChangeDetailEntity> deleteList = detailEntityList.stream().filter(obj -> OptChangeTypeEnum.DELETE.getCode().equals(obj.getOptType())).collect(Collectors.toList());
        //删除
        if (CollectionUtils.isNotEmpty(deleteList)) {
            List<String> sourceDetailIds = deleteList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSourceDetailId())).map(SubcontractChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sourceDetailIds)) {
                List<PurchaseOrderDetailEntity> podList = purchaseOrderDetailService.listBySourceDetailIds(sourceDetailIds);
                if (CollectionUtils.isNotEmpty(podList)) {
                    String codes = podList.stream().map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
                    log.error("SKU【{}】已存在下推单据，不支持删除变更",codes);
                    throw new ServiceException(new ApiResult(ApiError.ERROR_98086.code, StrUtil.format(ApiError.ERROR_98086.msg,codes)));
                }
                //删除委外订单明细
                subcontractOrderDetailService.removeByIds(sourceDetailIds);
            }
        }

        //修改
        if (CollectionUtils.isNotEmpty(updateList)) {
            //收货数量和入库数量校验
            checkGenerateUpdate(updateList);
            //更新委外订单数据
            generateUpdate(updateList,list);
        }
        //新增
        if (CollectionUtils.isNotEmpty(addList)) {
            generateAdd(addList,list);
        }
    }
    /**
     * @description: 数量验证
     * @author Will
     * @date: 2023/6/20 10:06
     * @param updateList
     */
    private void checkGenerateUpdate (List<SubcontractChangeDetailEntity> updateList) {
        List<String> sourceDetailIds = updateList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSourceDetailId())).map(SubcontractChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return;
        }
        List<PurchaseOrderDetailEntity> podList = purchaseOrderDetailService.listBySourceDetailIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(podList)) {
            return;
        }

        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(subcontractOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        List<String> podIds = podList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //收货单
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        //入库单
        List<PoInstockDetailEntity> poInstockDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        for (SubcontractChangeDetailEntity updateEntity : updateList) {

            SubcontractOrderDetailEntity detailEntity = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(updateEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98070);
            }
            //采购数量
            Integer purchaseQty = podList.stream().filter(obj -> obj.getSourceDetailId().equals(updateEntity.getSourceDetailId())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            if (purchaseQty > updateEntity.getQty()) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98087.code,StrUtil.format(ApiError.ERROR_98087.msg,updateEntity.getSkuNo(),updateEntity.getQty(),purchaseQty)));
            }
            //仓库和供应商未改变则直接跳过
            if (updateEntity.getWarehouseId().equals(detailEntity.getWarehouseId()) && updateEntity.getSupplierId().equals(detailEntity.getSupplierId())) {
                continue;
            }
            List<String> podIdList = podList.stream().filter(obj -> obj.getSourceDetailId().equals(updateEntity.getSourceDetailId())).map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
            //收货数量
            long receiveCount = receiveDetailList.stream().filter(obj -> podIdList.contains(obj.getPurchaseOrderDetailId())).count();
            if (receiveCount > 0) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98094.code,StrUtil.format(ApiError.ERROR_98094.msg,updateEntity.getSkuNo())));
            }

            //入库数量
            long instockCount = poInstockDetailList.stream().filter(obj -> podIdList.contains(obj.getPurchaseOrderDetailId())).count();
            if (instockCount > 0) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98095.code,StrUtil.format(ApiError.ERROR_98095.msg,updateEntity.getSkuNo())));
            }
        }
    }
    /**
     * @description: 新增生成数据
     * @author Will
     * @date: 2023/6/20 10:07
     * @param addList
     * @param list
     */
    private void generateAdd (List<SubcontractChangeDetailEntity> addList,List<SubcontractChangeEntity> list) {
        //明细父级sku
        List<SubcontractChangeDetailEntity> parentList = addList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98082);
        }
        //新增(根据变更单分组)
        Map<String, List<SubcontractChangeDetailEntity>> map = parentList.stream().collect(Collectors.groupingBy(SubcontractChangeDetailEntity::getMainId));
        for (Map.Entry<String, List<SubcontractChangeDetailEntity>> entry : map.entrySet()) {
            String mainId = entry.getKey();
            List<SubcontractChangeDetailEntity> value = entry.getValue();
            SubcontractChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(mainId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            List<SubcontractOrderDetailDTO.UpdateDTO> detailList = new ArrayList<>();
            List<Pair<String,String>> pairList = new ArrayList<>();
            for (SubcontractChangeDetailEntity addEntity : value) {
                SubcontractOrderDetailDTO.UpdateDTO addDTO = BeanMapperUtils.map(SubcontractOrderDetailDTO.UpdateDTO.class,addEntity);
                addDTO.setId(IdWorker.getIdStr());
                addDTO.setSourceDetailId(null);
                addEntity.setSourceDetailId(addDTO.getId());

                pairList.add(new Pair<>(addEntity.getId(),addDTO.getId()));
                //子集SKU
                List<SubcontractChangeDetailEntity> childList = addList.stream().filter(obj -> obj.getParentId().equals(addEntity.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {
                    throw new ServiceException(ApiError.ERROR_98083);
                }
                List<SubcontractOrderDetailDTO.UpdateDTO> childDTOList = new ArrayList<>();
                for (SubcontractChangeDetailEntity childEntity : childList) {
                    SubcontractOrderDetailDTO.UpdateDTO childDTO = BeanMapperUtils.map(SubcontractOrderDetailDTO.UpdateDTO.class,childEntity);
                    childDTO.setId(IdWorker.getIdStr());
                    childDTO.setSourceDetailId(null);
                    childEntity.setSourceDetailId(childDTO.getId());
                    pairList.add(new Pair<>(childEntity.getId(),childDTO.getId()));
                    childDTOList.add(childDTO);
                }
                addDTO.setChildList(childDTOList);
                detailList.add(addDTO);
            }
            //委外订单添加明细
            subcontractOrderDetailService.addByChange(detailList,entity.getSourceId());
            //更新委外变更单来源明细id
            subcontractChangeDetailService.updateSourceDetailId(pairList);
        }
        //自动下推
        autoPushdownDetail(addList);
    }
    /**
     * @description: 修改更新数据
     * @author Will
     * @date: 2023/6/20 10:07
     * @param updateList
     * @param list
     */
    private void generateUpdate (List<SubcontractChangeDetailEntity> updateList,List<SubcontractChangeEntity> list) {
        //明细父级sku
        List<SubcontractChangeDetailEntity> parentList = updateList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98082);
        }
        //新增(根据变更单分组)
        Map<String, List<SubcontractChangeDetailEntity>> map = parentList.stream().collect(Collectors.groupingBy(SubcontractChangeDetailEntity::getMainId));
        for (Map.Entry<String, List<SubcontractChangeDetailEntity>> entry : map.entrySet()) {
            String mainId = entry.getKey();
            List<SubcontractChangeDetailEntity> value = entry.getValue();
            SubcontractChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(mainId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            List<SubcontractOrderDetailDTO.UpdateDTO> detailList = new ArrayList<>();
            for (SubcontractChangeDetailEntity updateEntity : value) {
                SubcontractOrderDetailDTO.UpdateDTO updateDTO = BeanMapperUtils.map(SubcontractOrderDetailDTO.UpdateDTO.class,updateEntity);
                updateDTO.setId(updateEntity.getSourceDetailId());
                updateDTO.setSourceDetailId(null);
                //子集SKU
                List<SubcontractChangeDetailEntity> childList = updateList.stream().filter(obj -> obj.getParentId().equals(updateEntity.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {
                    throw new ServiceException(ApiError.ERROR_98083);
                }
                List<SubcontractOrderDetailDTO.UpdateDTO> childDTOList = new ArrayList<>();
                for (SubcontractChangeDetailEntity childEntity : childList) {
                    SubcontractOrderDetailDTO.UpdateDTO childDTO = BeanMapperUtils.map(SubcontractOrderDetailDTO.UpdateDTO.class,childEntity);
                    childDTO.setId(childDTO.getSourceDetailId());
                    childDTO.setSourceDetailId(null);
                    childDTOList.add(childDTO);
                }
                updateDTO.setChildList(childDTOList);
                detailList.add(updateDTO);
            }
            subcontractOrderDetailService.updateByChange(detailList,entity.getSourceId());
        }
        //自动下推
        autoPushdownDetail(updateList);
    }
    /**
     * @description: 自动下推
     * @author Will
     * @date: 2023/6/20 11:46
     * @param list
     */
    private void autoPushdownDetail(List<SubcontractChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> mainIds = list.stream().map(SubcontractChangeDetailEntity::getMainId).collect(Collectors.toList());
        List<SubcontractChangeEntity> subcontractChangeList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(subcontractChangeList)) {
            throw new ServiceException(ApiError.ERROR_98084);
        }
        List<String> sourceDetailIds = list.stream().map(SubcontractChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> poList = purchaseOrderDetailService.listBySourceDetailIds(sourceDetailIds);

        List<SubcontractOrderDTO.GeneratePoDTO> resultLust = new ArrayList<>();
        for (SubcontractChangeDetailEntity detailEntity : list) {
            if (!detailEntity.getIsGeneratePo()) {
                continue;
            }
            SubcontractChangeEntity subcontractChangeEntity = subcontractChangeList.stream().filter(obj -> obj.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(subcontractChangeEntity)) {
                throw new ServiceException(ApiError.ERROR_98084);
            }
            //已下推采购订单数量
            Integer qty = poList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId())).map(PurchaseOrderDetailEntity::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
           //如果已下推数量等于变更后数量则无需下推采购订单
            if (MathUtil.compareTo(detailEntity.getQty(),qty) == MathUtil.ZERO) {
                continue;
            }
            SubcontractOrderDTO.GeneratePoDTO generatePoDTO = new SubcontractOrderDTO.GeneratePoDTO();
            generatePoDTO.setSourceDetailId(detailEntity.getSourceDetailId());
            generatePoDTO.setSourceId(subcontractChangeEntity.getSourceId());
            generatePoDTO.setSourceCode(subcontractChangeEntity.getSourceCode());
            generatePoDTO.setSourceType(SourceTypeEnum.SUBCONTRACT_ORDER.getCode());
            generatePoDTO.setSupplierId(detailEntity.getSupplierId());
            generatePoDTO.setQty(detailEntity.getQty() - qty);
            generatePoDTO.setPlanDeliveryDate(detailEntity.getPlanDeliveryDate());
            generatePoDTO.setPaymentCondition(detailEntity.getPaymentCondition());
            resultLust.add(generatePoDTO);
        }
        if (CollectionUtils.isNotEmpty(resultLust)) {
            ValidList<SubcontractOrderDTO.GeneratePoDTO> validList = new ValidList<>();
            validList.setList(resultLust);
            subcontractOrderService.generatePo(validList,Boolean.TRUE);
        }

    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外变更单数据");
        }
        // 已审核支持反审核
        long count = list.stream().filter(obj -> !Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 检查是否有下推单据（如果支持下推的话）

        // 更新审核信息
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个委外变更单【%s】", ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), pairList, "反审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
       List<SubcontractChangeEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到委外变更单数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }
       // 删除日志数据
       log.info("删除 开始删除委外变更单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       //删除明细数据
       subcontractChangeDetailService.removeByMainIds(ids);

       // 删除主单数据
       log.info("删除 开始删除委外变更单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));

        log.info("撤销 开始修改委外变更单状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("委外变更单【%s】取消流程", ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), pairList, "取消流程操作");
    }

    @Override
    public SubcontractChangeDTO.ViewDTO view(String id) {
        SubcontractChangeEntity subcontractChangeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外变更单数据"));
        SubcontractChangeDTO.ViewDTO data = BeanMapperUtils.map(SubcontractChangeDTO.ViewDTO.class, subcontractChangeEntity);
        //查询明细
        List<SubcontractChangeDetailEntity> subcontractChangeDetailList = subcontractChangeDetailService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(subcontractChangeDetailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }
        //产品信息
        List<String> skuIds = subcontractChangeDetailList.stream().map(SubcontractChangeDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //供应商信息
        List<String> supplierIds = subcontractChangeDetailList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(SubcontractChangeDetailEntity::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(supplierIds)) {
            supplierList = supplierService.listByIds(supplierIds);
        }

        //明细父级sku
        List<SubcontractChangeDetailEntity> parentList = subcontractChangeDetailList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98082);
        }

        List<String> parentSkuIds = parentList.stream().map(SubcontractChangeDetailEntity::getSkuId).collect(Collectors.toList());
        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(parentSkuIds);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }


        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

        List<SubcontractChangeDetailDTO.ViewDTO> parentDTOList = BeanMapperUtils.copyList(SubcontractChangeDetailDTO.ViewDTO.class, parentList);

        List<SubcontractChangeDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (SubcontractChangeDetailDTO.ViewDTO viewDTO : parentDTOList) {
            //产品名称
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId()))
                    .findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse(null);
            viewDTO.setProductName(productName);
            //供应商名称
            String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(viewDTO.getSupplierId()))
                    .findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse(null);
            viewDTO.setSupplierName(supplierName);
            //变更类型
            viewDTO.setOptTypeName(OptChangeTypeEnum.getName(viewDTO.getOptType()));


            //子集SKU
            List<SubcontractChangeDetailEntity> childList = subcontractChangeDetailList.stream().filter(obj -> obj.getParentId().equals(viewDTO.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childList)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            List<SubcontractChangeDetailDTO.ChildDTO> childDTOList = BeanMapperUtils.copyList(SubcontractChangeDetailDTO.ChildDTO.class, childList);
            for (SubcontractChangeDetailDTO.ChildDTO childViewDTO : childDTOList) {

                //bom信息
                BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(viewDTO.getSkuId()) && obj.getSkuId().equals(childViewDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                    throw new ServiceException(ApiError.ERROR_95163);
                }
                childViewDTO.setQuantity(bomChildrenSkuDTO.getQuantity());

                //产品名称
                String childProductName = skuList.stream().filter(obj -> obj.getSkuId().equals(childViewDTO.getSkuId()))
                        .findFirst()
                        .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse(null);
                childViewDTO.setProductName(childProductName);
                //供应商名称
                String childSupplierName = supplierList.stream().filter(obj -> obj.getId().equals(childViewDTO.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse(null);
                childViewDTO.setSupplierName(childSupplierName);
                //变更类型
                childViewDTO.setOptTypeName(OptChangeTypeEnum.getName(childViewDTO.getOptType()));
            }
            viewDTO.setChildList(childDTOList);
            detailList.add(viewDTO);
        }
        data.setDetailList(detailList);
        return data;
    }

    /**
     * @param ids
     * @param reason
     * @description: 更新作废状态
     * @author Will
     */
    private void updateInvalidStatus(List<String> ids, String reason) {
        //更新
        lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
                .set(SubcontractChangeEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SubcontractChangeEntity::getInvalidTime, LocalDateTime.now())
                .set(SubcontractChangeEntity::getInvalidRemark, reason)
                .update();
    }

    /**
    * 审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    private void updateForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
            .set(SubcontractChangeEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractChangeEntity::getApproveStatus, approveStatus)
            .set(SubcontractChangeEntity::getApproveTime, LocalDateTime.now())
            .update();
     }

    /**
    * 更新审核状态
    */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
        .set(SubcontractChangeEntity::getApproveStatus, approveStatus)
        .set(SubcontractChangeEntity::getApproveUserId, "")
        .set(SubcontractChangeEntity::getApproveUserName, "")
        .set(SubcontractChangeEntity::getApproveTime, null)
        .update();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //产品信息
        List<String> skuIds = list.stream().map(SubcontractChangeDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        // 属性赋值
        for(SubcontractChangeDTO.ListDTO data : list) {

            //sku信息
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(data.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(skuVO)) {
                    data.setProductName(skuVO.getSkuName());
                }
            }
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setOptTypeName(OptChangeTypeEnum.getName(data.getOptType()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractChangeEntity entity) {

        SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
        if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        entity.setPurchaseOrgId(subcontractOrderEntity.getPurchaseOrgId());
        entity.setPurchaseOrgName(subcontractOrderEntity.getPurchaseOrgName());

        //人员信息
        if (StringUtils.isNotBlank(entity.getChangerId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getChangerId());
            entity.setChangerName(findUserDTO.getUserName());
        }

        //部门信息
        if (StringUtils.isNotBlank(entity.getDeptId())) {
            SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(entity.getDeptId());
            entity.setDeptName(sysDepartmentDTO.getName());
        }
    }

}

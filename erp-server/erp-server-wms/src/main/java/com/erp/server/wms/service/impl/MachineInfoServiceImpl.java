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
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.kingdee.SyncKingdeeMachineInfoService;
import com.erp.server.wms.mapper.MachineInfoMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Maps;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class MachineInfoServiceImpl extends SuperServiceImpl<MachineInfoMapper, MachineInfoEntity> implements MachineInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SyncKingdeeMachineInfoService syncKingdeeMachineInfoService;


    @Override
    public PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<MachineInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<MachineInfoDTO.ListDTO> records = pageData.getRecords();
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
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setWorkType(null);
                obj.setWorkTypeName(null);
                obj.setApproveUserName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<MachineInfoDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            MachineInfoDTO.SearchParamDTO searchParamDTO = new MachineInfoDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            MachineInfoDTO.ListStatusCountDTO resultDTO = new MachineInfoDTO.ListStatusCountDTO();
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
    public String add(MachineInfoDTO.AddDTO dto) {
        MachineInfoEntity entity = new MachineInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(), entity);
        log.info("加工单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.ZZCX, BusinessNoTypeEnum.CODE_ZZCX.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个加工单【%s】", code), ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "新增操作");
            //新增明细
            machineDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(MachineInfoDTO.AddDTO dto) {
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
    public Boolean update(MachineInfoDTO.UpdateDTO dto) {

        MachineInfoEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        MachineInfoEntity entity = new MachineInfoEntity();
        BeanMapperUtils.copy(dto, entity);
        List<MachineDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(), entity);

        log.info("加工单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.MACHINE_INFO.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        machineDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("加工单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个加工单【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public MachineInfoDTO.ViewDTO view(String id) {
        MachineInfoDTO.ViewDTO viewDTO = new MachineInfoDTO.ViewDTO();
        //主表信息
        MachineInfoEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<MachineDetailEntity> detailList = machineDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        List<MachineDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(MachineDetailDTO.ViewDTO.class, detailList);

        //产品信息
        List<String> skuIds = detailList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, List<MachineSubComponentsDTO.ViewDTO>> bomSubMap = Maps.newHashMap();
        skuIds.stream().forEach(skuId-> bomSubMap.put(skuId, viewBomSubComponents(skuId)));
        for (MachineDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDetailDTO.setProductName(productName);
            }
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getWarehouseId(), viewDetailDTO.getSkuId());
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
            //明细子件
            List<MachineSubComponentsDTO.ViewDTO> subComponentsList = this.viewSubComponents(viewDetailDTO.getId());
            List<MachineSubComponentsDTO.ViewDTO> subList = bomSubMap.get(viewDetailDTO.getSkuId());
            Map<String,MachineSubComponentsDTO.ViewDTO> subMap = subList.stream().collect(Collectors.toMap(MachineSubComponentsDTO.ViewDTO::getSkuId, Function.identity()));
            subComponentsList.stream().forEach(sub-> {
                MachineSubComponentsDTO.ViewDTO subView = subMap.get(sub.getSkuId());
                sub.setItemQty(subView.getQty());
            });
            viewDetailDTO.setSubComponentsList(subComponentsList);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    public List<MachineSubComponentsDTO.ViewDTO> viewBomSubComponents(String skuId) {
        List<MachineSubComponentsDTO.ViewDTO> resultList = new ArrayList<>();
        //查询BOM中SKU子集
        List<BomChildrenSkuDTO> childrenList = plmTaskFeign.listBomChildBySkuIds(Arrays.asList(skuId));
        if (CollectionUtils.isEmpty(childrenList)) {
            return resultList;
        }
        for (BomChildrenSkuDTO bomChildrenSkuDTO : childrenList) {
            MachineSubComponentsDTO.ViewDTO viewDTO = new MachineSubComponentsDTO.ViewDTO();
            viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            viewDTO.setProductName(bomChildrenSkuDTO.getSkuName());
            viewDTO.setUnit(bomChildrenSkuDTO.getUnitName());
            viewDTO.setQty(bomChildrenSkuDTO.getQuantity());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String operate) {
        return  this.lambdaUpdate()
                .eq(MachineInfoEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),MachineInfoEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),MachineInfoEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId),MachineInfoEntity::getSyncKingdeeId,syncKingdeeId)
                .set(StringUtils.isNotBlank(operate),MachineInfoEntity::getSyncOperate,operate)
                .update();
    }

    @Override
    public List<MachineSubComponentsDTO.ViewDTO> viewSubComponents(String detailId) {
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailId(detailId);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            return Collections.EMPTY_LIST;
        }
        List<MachineSubComponentsDTO.ViewDTO> resultList = BeanMapperUtils.copyList(MachineSubComponentsDTO.ViewDTO.class, machineSubComponentsList);

        List<String> skuIds = resultList.stream().map(MachineSubComponentsDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //仓库信息
        List<String> warehouseIds = machineSubComponentsList.stream().map(MachineSubComponentsEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        for (MachineSubComponentsDTO.ViewDTO viewDTO : resultList) {
            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewDTO.setSkuNo(skuVO.getSkuNo());
            viewDTO.setProductName(skuVO.getSkuName());
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getWarehouseId(), viewDTO.getSkuId(), viewDTO.getWarehouseLocation());
            viewDTO.setCurInventoryQty(curInventoryQty);
        }

        return  resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("加工单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除子件明细
        machineSubComponentsService.removeByMainIds(ids);
        //删除明细数据
        machineDetailService.removeByMainIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("加工单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(MachineInfoEntity::getInvalidRemark, reason)
                .update();
        //金蝶推送
        list.forEach(obj -> syncKingdeeMachineInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_INVALID.getCode()));
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个加工单【%s】，作废原因：".concat(reason), ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("加工单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("加工单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
            //更新库存
            updateInventoryTransCore(list);
            //金蝶推送
            list.forEach(obj -> syncKingdeeMachineInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("加工单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个加工单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("加工单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.MACHINE_INFO,ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
        //金蝶推送
        list.forEach(obj -> syncKingdeeMachineInfoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个加工单【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<MachineInfoEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("加工单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("加工单【%s】取消流程", ModuleTypeEnum.MACHINE_INFO.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<MachineInfoDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandleData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/machineInfo.xlsx";
        String name = "加工单导出";
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


    /**
     * @description: 更新加工单库存
     * @author Will
     * @date: 2023/5/18 12:11
     * @param list
     */
    private void updateInventoryTransCore (List<MachineInfoEntity> list) {
        List<String> ids = list.stream().map(MachineInfoEntity::getId).collect(Collectors.toList());
        //加工明细
        List<MachineDetailEntity> detailList = machineDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        //加工子件明细
        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> machineSubComponentsList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollectionUtils.isEmpty(machineSubComponentsList)) {
            throw new ServiceException(ApiError.ERROR_99056);
        }
        for (MachineInfoEntity entity : list) {
            //加工明细
            List<MachineDetailEntity> resultDetails = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(resultDetails)) {
                throw new ServiceException(ApiError.ERROR_99053);
            }
            //父级SKU库存更新
            updateInventoryForMachineDetail(entity,resultDetails);

            //子件SKU库存更新
            updateInventoryForMachineSubComponents(entity,resultDetails,machineSubComponentsList);
        }
    }

    /**
     * @description: 父级SKU库存更新
     * @author Will
     * @date: 2023/5/18 12:10
     * @param entity
     * @param resultDetails
     */
    private void updateInventoryForMachineDetail (MachineInfoEntity entity ,List<MachineDetailEntity> resultDetails) {
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (MachineDetailEntity detailEntity : resultDetails) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.MACHINE_INFO);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getQty());
            inOutStockDTO.setWarehouseId(entity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //组装父SKU增加库存，拆卸父SKU减少库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setMembers(inOutStockList);
        if (WorkTypeEnum.ASSEMBLE.getCode().equals(entity.getWorkType())) {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_PARENT.getCode());
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.DISASSEMBLE_IN_PARENT.getCode());
        }
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * @description: 子件SKU库存更新
     * @author Will
     * @date: 2023/5/18 12:10
     * @param entity
     * @param resultDetails
     * @param machineSubComponentsList
     */
    private void updateInventoryForMachineSubComponents (MachineInfoEntity entity ,List<MachineDetailEntity> resultDetails,List<MachineSubComponentsEntity> machineSubComponentsList) {

        //子件SKU库存更新
        List<String> resultDetailIds = resultDetails.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> resultMachineSubComponents = machineSubComponentsList.stream().filter(obj -> resultDetailIds.contains(obj.getDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resultMachineSubComponents)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        //父级SKU库存更新
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (MachineSubComponentsEntity detailEntity : resultMachineSubComponents) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.MACHINE_INFO);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getQty());
            inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //组装父SKU增加库存，拆卸父SKU减少库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setMembers(inOutStockList);
        if (WorkTypeEnum.ASSEMBLE.getCode().equals(entity.getWorkType())) {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.ASSEMBLE_IN_CHILDD.getCode());
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.DISASSEMBLE_IN_CHILD.getCode());
        }
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<MachineInfoDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(MachineInfoDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (MachineInfoDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            obj.setWorkTypeName(WorkTypeEnum.getByCode(obj.getWorkType()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String warehouseId, String receiveOrgId, String warehouseKeeperId,String receiverId, MachineInfoEntity entity) {

        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId,receiverId));
        if (CollectionUtils.isNotEmpty(userList)) {
            //仓管员
            String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setWarehouseKeeperName(warehouseKeeperName);
            //领料员
            String receiverName = userList.stream().filter(obj -> obj.getUserId().equals(receiverId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setReceiverName(receiverName);
        }
        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if  (ObjectUtils.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        entity.setWarehouseName(warehouseEntity.getName());
        //库存组织
        String inventoryOrgId = warehouseEntity.getOrgId();
        entity.setInventoryOrgId(inventoryOrgId);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(inventoryOrgId, receiveOrgId));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //库存组织名称
        String inventoryOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(inventoryOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInventoryOrgName(inventoryOrgName);
        //收料组织名称
        String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(receiveOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setReceiveOrgName(receiveOrgName);
    }

    /**
     * 根据ids查询数据
     */
    private List<MachineInfoEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<MachineInfoEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveUserId, userInfo.getUid())
                .set(MachineInfoEntity::getApproveUserName, userInfo.getUserName())
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .set(MachineInfoEntity::getApproveTime, LocalDateTime.now())
                .set(ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus), MachineInfoEntity::getSyncKingdeeStatus, SyncKingdeeStatusEnum.TO_BE_SYNC.getCode())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(MachineInfoEntity::getId, ids)
                .set(MachineInfoEntity::getApproveStatus, approveStatus)
                .set(MachineInfoEntity::getApproveUserId, "")
                .set(MachineInfoEntity::getApproveUserName, "")
                .set(MachineInfoEntity::getApproveTime, null)
                .update();
    }

}

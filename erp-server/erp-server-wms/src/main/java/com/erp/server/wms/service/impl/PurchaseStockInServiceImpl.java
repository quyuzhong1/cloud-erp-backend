package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.PurchaseStockExportExcelDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购入库单 服务实现类
 *
 * @author will
 * @since 2023-04-10
 */
@Slf4j
@Service
public class PurchaseStockInServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStockInEntity> implements PurchaseStockInService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ProductOrderFeign productOrderFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Override
    public PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseStockInDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseStockInDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(PurchaseStockInDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSupplierName(null);
                    obj.setDeliveryWarehouseName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<PurchaseStockInDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item: values) {
            PurchaseStockInDTO.SearchParamDTO searchParamDTO = new PurchaseStockInDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            PurchaseStockInDTO.ListStatusCountDTO resultDTO = new PurchaseStockInDTO.ListStatusCountDTO();
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
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(PurchaseStockInDTO.AddDTO dto) {
        PurchaseStockInEntity entity = new PurchaseStockInEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(),dto.getStockInUserId(),dto.getDeliveryWarehouseId(),entity);
        log.info("采购入库单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGRK, BusinessNoTypeEnum.CODE_CGRK.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购入库单【%s】",code), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseStockInDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseStockInDTO.UpdateDTO dto) {
        PurchaseStockInEntity entity = new PurchaseStockInEntity();
        BeanMapperUtils.copy(dto,entity);
        List<PurchaseStockInDetailDTO.UpdateDTO> details = dto.getDetails();
        //校验明细是否有重复sku
        checkUpdateDetailsRepeatSku(details);
        //处理数据id
        doOpHandleDataId(dto.getStockInDeptId(),dto.getStockInUserId(),dto.getDeliveryWarehouseId(),entity);

        log.info("采购入库单修改，id=【{}】", dto.getId());

        //添加日志
        PurchaseStockInEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old,entity,ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseStockInDetailService.update(details,entity.getId());
        return Boolean.TRUE;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseStockInDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("采购入库单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购入库单【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"提交操作");
        return Boolean.TRUE;
    }

    @Override
    public PurchaseStockInDTO.ViewDTO view(String id) {
        PurchaseStockInDTO.ViewDTO dto = new PurchaseStockInDTO.ViewDTO();

        //主表信息
        PurchaseStockInEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        BeanMapperUtils.copy(entity,dto);

        //明细信息
        List<PurchaseStockInDetailEntity> entityDetails = purchaseStockInDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98002);
        }
        List<PurchaseStockInDetailDTO.ViewDTO> details = BeanMapperUtils.copyList(PurchaseStockInDetailDTO.ViewDTO.class, entityDetails);
        dto.setDetails(details);

        //查询采购供应商信息
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = productOrderFeign.getOrderSupplierByOrderId(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        PurchaseStockInDTO.SupplierDTO supplierDTO = new PurchaseStockInDTO.SupplierDTO();
        supplierDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
        supplierDTO.setSupplierContactId(purchaseOrderSupplierEntity.getSupplierContactId());

        //查询供应商信息
        SupplierEntity supplierEntity = productOrderFeign.getSupplierById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isNotEmpty(supplierEntity)) {
            supplierDTO.setSupplierAddress(supplierEntity.getCompanyAddress());
        }
        dto.setSupplierDTO(supplierDTO);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("采购入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        purchaseStockInDetailService.removeByMainIds(ids);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return  this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("采购入库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(PurchaseStockInEntity::getId,ids)
                .set(PurchaseStockInEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(PurchaseStockInEntity::getInvalidTime, LocalDateTime.now())
                .set(PurchaseStockInEntity::getInvalidRemark,reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个采购入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = baseApproveParamDTO.getType();

        log.info("采购入库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids,ApproveStatusEnum.APPROVE.getStatus());
        }else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids,ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购入库单",ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //审核中和已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        log.info("采购入库单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个采购入库单【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseStockInEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购入库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购入库单【%s】取消流程", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<PurchaseStockExportExcelDTO> resultList = baseMapper.listExportExcel(dto);
        String fileName = "采购入库单数据";
        try {
            ExcelUtil.export(fileName, "采购入库单数据", resultList, PurchaseStockExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids) {
        List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> list = baseMapper.viewGeneratePurchaseReturnOrder(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> skuIds = list.stream().map(PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            return list;
        }
        for (PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO dto : list) {
            //来源类型
            dto.setSourceType(SourceTypeEnum.PURCHASE_RETURN_ORDER.getType());
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
            dto.setProductName(productName);
        }
        return list;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO> list = dto.getList();
        for (PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO generateDto : list) {
            //退货人

        }

        return null;
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids,String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(PurchaseStockInEntity::getId,ids)
                .set(PurchaseStockInEntity::getApproveUserId,userInfo.getUid())
                .set(PurchaseStockInEntity::getApproveUserName,userInfo.getUserName())
                .set(PurchaseStockInEntity::getApproveStatus,approveStatus)
                .set(PurchaseStockInEntity::getApproveTime,LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids,String approveStatus) {

        this.lambdaUpdate().in(PurchaseStockInEntity::getId,ids)
                .set(PurchaseStockInEntity::getApproveStatus,approveStatus)
                .set(PurchaseStockInEntity::getApproveUserId,"")
                .set(PurchaseStockInEntity::getApproveUserName,"")
                .set(PurchaseStockInEntity::getApproveTime,null)
                .update();
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseStockInEntity::getId,ids)
                .set(PurchaseStockInEntity::getApproveStatus,approveStatus)
                .update();
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseStockInEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseStockInEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        return list;
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String stockInDeptId, String stockInUserId, String deliveryWarehouseId, PurchaseStockInEntity entity) {

        //入库员
        if (StringUtils.isNotBlank(stockInUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(stockInUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setStockInUserName(userDTO.getUserName());
        }
        //入库部门
        if (StringUtils.isNotBlank(stockInDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(stockInDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setStockInDeptName(depart.getName());
        }
        //仓库
        if (StringUtils.isNotBlank(deliveryWarehouseId)) {
            //仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(deliveryWarehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
        }
    }


    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseStockInDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseStockInDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseStockInDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseStockInDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<PurchaseStockInDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }
    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<PurchaseStockInDetailDTO.UpdateDTO> list) {
        Map<String, List<PurchaseStockInDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseStockInDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseStockInDetailDTO.UpdateDTO>> entry: map.entrySet()) {
            List<PurchaseStockInDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
        }
    }


}

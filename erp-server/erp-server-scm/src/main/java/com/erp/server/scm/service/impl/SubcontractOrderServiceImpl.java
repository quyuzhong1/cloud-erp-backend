package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SubcontractOrderMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Sets;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 委外订单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderServiceImpl extends SuperServiceImpl<SubcontractOrderMapper, SubcontractOrderEntity> implements SubcontractOrderService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ModuleOperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;


    @Override
    public PagingVO<SubcontractOrderDTO.ListDTO> paging(PagingDTO<SubcontractOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        // 同一主单多行明细只有第一行显示主单字段，其他行赋空
        hideData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SubcontractOrderDTO.TabListDTO> tabList(PermissionsDTO param) {
        PurchaseListTypeEnum[] values = PurchaseListTypeEnum.values();
        List<SubcontractOrderDTO.TabListDTO> list = new ArrayList<>();
        for (PurchaseListTypeEnum item : values) {
            SubcontractOrderDTO.PagingParamDTO searchParamDTO = new SubcontractOrderDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(param.getPermissionSql());
            SubcontractOrderDTO.TabListDTO resultDTO = new SubcontractOrderDTO.TabListDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.TO_BE_CREATE.getCode().equals(item.getCode())) {
                searchParamDTO.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.NON_ARRIVAL.getCode(), ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.CREATED.getCode().equals(item.getCode())) {
                searchParamDTO.setArrivalStatusList(Arrays.asList(ArrivalStatusEnum.ARRIVED.getCode()));
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseListTypeEnum.REJECT.getCode().equals(item.getCode())) {
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
    public void exportList(SubcontractOrderDTO.ExportDTO param, HttpServletResponse response) {
        List<SubcontractOrderDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/subcontractOrder.xlsx";
        String name = "委外订单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<SubcontractOrderDTO.PurchaseOrderListDTO> listPurchaseOrderByDetailId(String detailId) {
        List<SubcontractOrderDTO.PurchaseOrderListDTO> resultList = new ArrayList<>();
        List<String> detailIds = new ArrayList<>();
        detailIds.add(detailId);
        List<SubcontractOrderDetailEntity> childList = subcontractOrderDetailService.listByParentId(detailId);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_98072);
        }
        childList.forEach(obj -> detailIds.add(obj.getId()));
        List<PurchaseOrderDTO.ListDTO> list = purchaseOrderService.listBySourceDetailIds(detailIds);
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //列表数据处理
        purchaseOrderService.doOpHandlePurchaseOrder(list);
        for (PurchaseOrderDTO.ListDTO listDTO : list) {
            SubcontractOrderDTO.PurchaseOrderListDTO purchaseOrderListDTO = new SubcontractOrderDTO.PurchaseOrderListDTO();
            if (detailId.equals(listDTO.getSourceDetailId())) {
                purchaseOrderListDTO.setIsParent(Boolean.TRUE);
            }
            purchaseOrderListDTO.setPurchaseOrderDTO(listDTO);
            resultList.add(purchaseOrderListDTO);
        }
        return resultList;
    }

    @Override
    public Boolean finishDelivery(List<String> ids, String remark) {
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }
        long count = detailList.stream().filter(obj -> !ArrivalStatusEnum.PARTIAL_ARRIVAL.getCode().equals(obj.getArrivalStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98035);
        }
        //更新明细中的交货状态
        subcontractOrderDetailService.updateArrivalStatusByIds(ArrivalStatusEnum.ARRIVED.getCode(), ids);
        //操作日志
        List<Pair<String, String>> pairList = detailList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("SKU【%s】结束交货", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "结束交货操作");
        return Boolean.TRUE;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SubcontractOrderDTO.AddDTO addDTO) {
        SubcontractOrderEntity subcontractOrderEntity = new SubcontractOrderEntity();
        BeanMapperUtils.copy(addDTO, subcontractOrderEntity);

        // 数据处理
        handleData(subcontractOrderEntity);

        log.info("开始新增委外订单");
        // 生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.SUB, BusinessNoTypeEnum.CODE_SUB.getCode()));
        subcontractOrderEntity.setCode(code);
        boolean save = super.save(subcontractOrderEntity);
        if(!save) {
           throw new ServiceException("委外订单保存失败");
        }
        //新增明细
        subcontractOrderDetailService.add(addDTO.getDetailList(),subcontractOrderEntity.getId());

        // 操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个委外订单【%s】", code), ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), subcontractOrderEntity.getId(), "新增操作");
        return subcontractOrderEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SubcontractOrderDTO.UpdateDTO updateDTO) {
        SubcontractOrderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到委外订单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        SubcontractOrderEntity subcontractOrderEntity =  BeanMapperUtils.map(SubcontractOrderEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractOrderEntity);

        log.info("编辑 开始修改委外订单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractOrderEntity);
        if(!save) {
           throw new ServiceException("委外订单保存失败");
        }

        //新增明细
        subcontractOrderDetailService.update(updateDTO.getDetailList(),subcontractOrderEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录委外订单日志数据，单号：【{}】", subcontractOrderEntity.getCode());
        operateLogService.addModuleOperateLogByObj(old, subcontractOrderEntity, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), subcontractOrderEntity.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<SubcontractOrderEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到委外订单数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }

       // 更新单据审核状态
       log.info("提交 开始修改委外订单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // TODO 启动流程（如果需要的话）

       // 记录操作日志
       log.info("提交 开始记录委外订单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       operateLogService.batchAddModuleOperateLog("提交了一个委外订单【%s】", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(SubcontractOrderDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractOrderDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(Arrays.asList(dto.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
           throw new ServiceException("审核不通过请填写审核意见");
        }
        List<SubcontractOrderEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外订单数据");
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
        } else if (Objects.equals(ApproveTypeEnum.REJECT, approveType)) {
           // TODO 终止审批流程
        }

        // 更新审核信息
        updateForApprove(ids, approveStatus.getStatus());

        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个委外订单", approveType.getName()).concat("【%s】").concat(StrUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "审核操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        List<SubcontractOrderEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外订单数据");
        }
        // 已审核支持反审核
        long count = list.stream().filter(obj -> !Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 检查是否有下推单据（如果支持下推的话）

        // 更新审核信息
        updateForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个委外订单【%s】", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "反审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
       List<SubcontractOrderEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到委外订单数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }
       // 删除日志数据
       log.info("删除 开始删除委外订单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       // 删除明细数据（如果有明细数据的话）
       subcontractOrderDetailService.removeByMainIds(ids);


       // 删除主单数据
       log.info("删除 开始删除委外订单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<SubcontractOrderEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外订单数据");
        }
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));

        log.info("撤销 开始修改委外订单状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("委外订单【%s】取消流程", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), pairList, "取消流程操作");
    }

    @Override
    public SubcontractOrderDTO.ViewDTO view(String id) {
        SubcontractOrderEntity subcontractOrderEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外订单数据"));
        SubcontractOrderDTO.ViewDTO data = BeanMapperUtils.map(SubcontractOrderDTO.ViewDTO.class, subcontractOrderEntity);
        //委外订单明细数据
        List<SubcontractOrderDetailEntity> detailList = subcontractOrderDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98070);
        }

        //产品信息
        List<String> skuIds = detailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        log.info("查询产品信息，skuId集合：【{}】", JSONUtil.toJsonStr(skuIds));
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //供应商信息
        List<String> supplierIds = detailList.stream().map(SubcontractOrderDetailEntity::getSupplierId).collect(Collectors.toList());
        log.info("查询供应商信息，supplierId集合：【{}】", JSONUtil.toJsonStr(supplierIds));
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        //明细父级sku
        List<SubcontractOrderDetailEntity> parentList = detailList.stream().filter(obj -> StringUtils.isBlank(obj.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentList)) {
            throw new ServiceException(ApiError.ERROR_98071);
        }
        List<SubcontractOrderDetailDTO.ViewDTO> parentDTOList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.ViewDTO.class, parentList);
        for (SubcontractOrderDetailDTO.ViewDTO viewDTO : parentDTOList) {
            //产品名称
            String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuName())).orElse("");
            viewDTO.setProductName(productName);
            //供应商名称
            String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(viewDTO.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
            viewDTO.setSupplierName(supplierName);


            //子集SKU
            List<SubcontractOrderDetailEntity> childList = detailList.stream().filter(obj -> obj.getParentId().equals(viewDTO.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childList)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            List<SubcontractOrderDetailDTO.ViewDTO> childDTOList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.ViewDTO.class, childList);
            for (SubcontractOrderDetailDTO.ViewDTO childViewDTO : childDTOList) {
                //产品名称
                String childProductName = skuList.stream().filter(obj -> obj.getSkuId().equals(childViewDTO.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuName())).orElse("");
                childViewDTO.setProductName(childProductName);
                //供应商名称
                String childSupplierName = supplierList.stream().filter(obj -> obj.getId().equals(viewDTO.getSupplierId())).findFirst().flatMap(e -> Optional.ofNullable(e.getName())).orElse("");
                childViewDTO.setSupplierName(childSupplierName);
            }

            viewDTO.setChildList(childDTOList);
        }
        data.setDetailList(parentDTOList);
        return data;
    }

    @Override
    public List<SubcontractOrderDTO.ViewGeneratePoDTO> viewGeneratePo(List<String> ids) {


        return null;
    }

    @Override
    public void generatePo(ValidList<SubcontractOrderDTO.GeneratePoDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
    }

    @Override
    public List<SubcontractOrderDTO.ViewAddDetailDTO> viewAddDetail(SubcontractOrderDTO.ViewAddDetailParamDTO dto) {


        return null;
    }

    /**
    * 审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    public void updateForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
            .set(SubcontractOrderEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractOrderEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractOrderEntity::getApproveStatus, approveStatus)
            .set(SubcontractOrderEntity::getApproveTime, LocalDateTime.now())
            .update();
     }

    /**
    * 反审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
            .set(SubcontractOrderEntity::getApproveUserId, "")
            .set(SubcontractOrderEntity::getApproveUserName, "")
            .set(SubcontractOrderEntity::getApproveStatus, approveStatus)
            .set(SubcontractOrderEntity::getApproveTime, null)
            .update();
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(SubcontractOrderEntity::getId, ids)
        .set(SubcontractOrderEntity::getApproveStatus, approveStatus)
        .update();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractOrderDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        List<String> skuIds = list.stream().map(SubcontractOrderDTO.ListDTO::getSkuId).collect(Collectors.toList());

        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);

        // 属性赋值
        for(SubcontractOrderDTO.ListDTO data : list) {
            //sku信息
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(data.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(skuVO)) {
                    data.setProductName(skuVO.getSkuName());
                }
            }
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setArrivalStatusName(ArrivalStatusEnum.getNameByCode(data.getArrivalStatus()));
        }
    }

    /**
    * 分页查询同一主单多行明细只有第一行显示主单字段，其他行赋空
    */
    private void hideData(List<SubcontractOrderDTO.ListDTO> list) {
        Set<String> mainIds = Sets.newHashSet();
        // 同一个主单的其他行明细，只显示第一行的主单字段
        for(SubcontractOrderDTO.ListDTO data : list) {
            if (mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                data.setApproveUserName(null);
                data.setCreateUserName(null);
                data.setCreateTime(null);
                // TODO 其他需要赋空值字段
                continue;
            }
            mainIds.add(data.getId());
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractOrderEntity entity) {
        //核算公司信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(), entity.getReceiveOrgId(), entity.getSubcontractOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //人员信息
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaserId());
        entity.setPurchaserName(findUserDTO.getUserName());

        //部门信息
        SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(entity.getDeptId());
        entity.setDeptName(sysDepartmentDTO.getName());

        //采购组织名称
        String purchaseOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setPurchaseOrgName(purchaseOrgName);

        //收料组织名称
        String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setReceiveOrgName(receiveOrgName);

        //委外组织名称
        String subcontractOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrgId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        entity.setSubcontractOrgName(subcontractOrgName);
    }

}

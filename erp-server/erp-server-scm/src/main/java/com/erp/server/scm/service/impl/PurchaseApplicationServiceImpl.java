package com.erp.server.scm.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.PurchaseApplicationExportExcelDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseApplicationListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.listener.PurchaseApplicationExcelListener;
import com.erp.server.scm.mapper.PurchaseApplicationMapper;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购申请表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Slf4j
@Service
public class PurchaseApplicationServiceImpl extends SuperServiceImpl<PurchaseApplicationMapper, PurchaseApplicationEntity> implements PurchaseApplicationService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Override
    public PagingVO<PurchaseApplicationDTO.ListDTO> paging(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<PurchaseApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(PurchaseApplicationDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setIsFirstMassProduct(null);
                    obj.setCreateUserName(null);
                    return;
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setCreatePoTypeName(CreatePoTypeEnum.getName(obj.getCreatePoType()));
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<ListStatusCountDTO.PurchaseApplicationCountDTO> listCount() {
        PurchaseApplicationDTO.SearchParamDTO dto = new PurchaseApplicationDTO.SearchParamDTO();
        PurchaseApplicationListTypeEnum[] values = PurchaseApplicationListTypeEnum.values();
        List<ListStatusCountDTO.PurchaseApplicationCountDTO> list = new ArrayList<>();
        for (PurchaseApplicationListTypeEnum item: values) {
            ListStatusCountDTO.PurchaseApplicationCountDTO resultDTO = new ListStatusCountDTO.PurchaseApplicationCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseApplicationListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseApplicationListTypeEnum.TO_BE_CREATE.getCode().equals(item.getCode())) {
                dto.setCreatePoTypeList(Arrays.asList(CreatePoTypeEnum.NOT_GENERATED.getStatus(),CreatePoTypeEnum.PARTIAL_GENERATED.getStatus()));
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseApplicationListTypeEnum.CREATED.getCode().equals(item.getCode())) {
                dto.setCreatePoTypeList(Arrays.asList(CreatePoTypeEnum.ALL_GENERATED.getStatus()));
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            if (PurchaseApplicationListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                dto.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(dto);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO :count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
       return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(PurchaseApplicationDTO.AddDTO dto) {
        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);
        log.info("采购申请单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PL, BusinessNoTypeEnum.CODE_PL.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个采购申请单【%s】",code), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"新增操作");
            //新增明细
            purchaseApplicationDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseApplicationDTO.UpdateDTO dto) {
        PurchaseApplicationEntity entity = new PurchaseApplicationEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkUpdateDetailsRepeatSku(dto.getDetails(),dto.getId());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),entity);

        log.info("采购申请单修改，id=【{}】", dto.getId());
        PurchaseApplicationDTO.UpdateDTO old = new PurchaseApplicationDTO.UpdateDTO();
        PurchaseApplicationDTO.ViewDTO view = this.view(dto.getId());
        BeanMapperUtils.copy(view,old);
        //操作日志
        moduleOperateLogService.addModuleOperateLogByObj(old,dto,ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        purchaseApplicationDetailService.update(dto.getDetails(),entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = baseApproveParamDTO.getType();

        log.info("采购申请单【{}】，ids=【{}】",ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO

            //更新单据状态(后面有流程了可删)
            updateApproveStatusForApprove(ids,ApproveStatusEnum.APPROVE.getStatus());
        }
        //审核不通过
        if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids,ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个采购申请单",ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //审核中和已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("采购申请单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));
        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个采购申请单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"反审核操作");
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> ids) {
        List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> resultList = new ArrayList<>();
        //主表数据
        List<PurchaseApplicationEntity> purchaseApplicationList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseApplicationList)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        //可以生成采购订单的明细
        List<PurchaseApplicationDetailEntity> list = purchaseApplicationDetailService.listCreatePurchaseOrderDetail(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98015);
        }

        List<String> detailIds = list.stream().map(PurchaseApplicationDetailEntity::getId).collect(Collectors.toList());
        //查询关联信息
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.listByPurchaseApplicationDetailIds(detailIds);
        List<String> strList = new ArrayList<>();
        for (PurchaseApplicationDetailEntity entity :list) {
            PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO dto = new PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO();
            BeanMapperUtils.copy(entity,dto);
            dto.setId(entity.getPurchaseApplicationId());
            dto.setPurchaseApplicationDetailId(entity.getId());
            //查询主表数据
            PurchaseApplicationEntity purchaseApplicationEntity = purchaseApplicationList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseApplicationId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseApplicationEntity)) {
                throw new ServiceException(ApiError.ERROR_98016);
            }
            dto.setCode(purchaseApplicationEntity.getCode());
            Integer purchaseQty = MathUtil.ZERO;
            //查询已采购数量
            if (CollectionUtils.isNotEmpty(refList)) {
                purchaseQty = refList.stream().filter(obj -> entity.getId().equals(obj.getPurchaseApplicationDetailId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(0, Integer::sum);
            }
            dto.setPurchasedQty(purchaseQty);

            //清空第一条明细后其他明细中的单号
            boolean contains = strList.contains(entity.getPurchaseApplicationId());
            if (contains) {
                dto.setCode(null);
            } else {
                strList.add(entity.getPurchaseApplicationId());
            }
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generatePurchaseOrder(PurchaseApplicationDTO.ListGeneratePurchaseOrderDTO dto) {
        List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> list = dto.getList();
        //主表数据
        PurchaseApplicationEntity entity = this.getById(list.get(0).getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }

        List<String> detailIds = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getPurchaseApplicationDetailId).distinct().collect(Collectors.toList());
        //查询关联信息
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.listByPurchaseApplicationDetailIds(detailIds);

        //明细数据
        List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByIds(detailIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        for (PurchaseApplicationDetailEntity detail : detailList) {
            //已采购数量
            Integer purchaseQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(refList)) {
                 purchaseQty = refList.stream().filter(obj -> obj.getPurchaseApplicationDetailId().equals(detail.getId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //本次采购数量
            Integer thisPurchaseQty = list.stream().filter(obj -> obj.getPurchaseApplicationDetailId().equals(detail.getId())).map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            
            //申请数量
            Integer applyQty = detail.getApplyQty();
            if (thisPurchaseQty > (applyQty - purchaseQty)) {
                throw new ServiceException(new ApiResult(1,String.format("【%s】采购数量不能大于%s",detail.getSkuNo(),applyQty - purchaseQty)));
            } else if (thisPurchaseQty == (applyQty - purchaseQty)) {
                detail.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            } else {
                detail.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            }
        }

        //sku信息
        List<String> skuIds = list.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //采购订单新增数据
        List<PurchaseOrderDTO.AddDTO> resultList = new ArrayList<>();

        //主表数据按供应商和采购组织分组
        Map<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSupplierId().concat("|").concat(obj.getPurchaseOrgId())));
        for (Map.Entry<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> entry : map.entrySet()) {
            List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> value = entry.getValue();
            //采购订单主表数据
            PurchaseOrderDTO.AddDTO addDTO = new PurchaseOrderDTO.AddDTO();
            addDTO.setPurchaseUserId(entity.getApproveUserId());
            addDTO.setPurchaseDeptId(entity.getApplyDeptId());
            addDTO.setPurchaseOrgId(value.get(0).getPurchaseOrgId());
            addDTO.setPurchaseDate(LocalDate.now());

            List<PurchaseOrderDetailDTO.AddDTO> details = new ArrayList<>();
            //明细数据按skuId、仓库、收料组织、交期分组
            Map<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> detailMap = value.stream().collect(Collectors.groupingBy(obj ->obj.getSkuId().concat("|").concat(obj.getReceiveOrgId()).concat(obj.getDestWarehouseId()).concat("|").concat(String.valueOf(obj.getPlanDeliveryDate()))));
            for (Map.Entry<String, List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO>> detailEntry : detailMap.entrySet()) {

                List<PurchaseApplicationDTO.GeneratePurchaseOrderDTO> detailValue = detailEntry.getValue();
                //采购订单明细数据
                PurchaseOrderDetailDTO.AddDTO addDetailDTO = new PurchaseOrderDetailDTO.AddDTO();
                //采购申请对应明细信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailValue.get(0).getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                addDetailDTO.setSkuId(skuVO.getSkuId());
                addDetailDTO.setSkuNo(skuVO.getSkuNo());
                addDetailDTO.setProductName(skuVO.getSkuName());
                addDetailDTO.setDeclareModel(skuVO.getDeclareModel());
                addDetailDTO.setDeclareName(skuVO.getDeclareName());
                addDetailDTO.setReceiveOrgId(detailValue.get(0).getReceiveOrgId());
                addDetailDTO.setDeliveryWarehouseId(detailValue.get(0).getDestWarehouseId());
                addDetailDTO.setPlanDeliveryDate(detailValue.get(0).getPlanDeliveryDate());
                addDetailDTO.setTaxPrice(detailValue.get(0).getTaxPrice());
                //采购数量
                Integer purchaseQty = detailValue.stream().map(PurchaseApplicationDTO.GeneratePurchaseOrderDTO::getPurchaseQty).reduce(0, Integer::sum);
                addDetailDTO.setPurchaseQty(purchaseQty);
                //采购金额
                BigDecimal purchaseAmount = detailValue.stream().map(obj -> MathUtil.multiply(obj.getTaxPrice(), obj.getPurchaseQty())).reduce(BigDecimal.ZERO, BigDecimal::add);
                addDetailDTO.setPurchaseAmount(purchaseAmount);
                //是否加急，明细存在加急则设置加急
                long count = detailValue.stream().filter(obj -> obj.getIsGift()).count();
                if (count > 0) {
                    addDetailDTO.setIsGift(Boolean.TRUE);
                }
                //采购报价单取税率 TODO
                addDetailDTO.setTaxRate(BigDecimal.ZERO);

                addDetailDTO.setPurchaseApplicationId(detailValue.get(0).getId());
                addDetailDTO.setPurchaseApplicationDetailId(detailValue.get(0).getPurchaseApplicationDetailId());
                details.add(addDetailDTO);
            }
            addDTO.setDetails(details);
            resultList.add(addDTO);
        }
        //新增采购订单
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList.forEach(obj -> purchaseOrderService.add(obj));
        }
        //更新申请明细生成状态
        purchaseApplicationDetailService.saveOrUpdateBatch(detailList);
        return Boolean.TRUE;
    }

    @Override
    public PurchaseApplicationDetailDTO.ImportDTO importFile(MultipartFile excelFile,List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        //查询所有审核通过并启用的仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        //查询所有启用核算公司
        List<BaseIdDTO> companyList = sysUserFeign.listAccountingCompany();
        PurchaseApplicationExcelListener excelListenerUtil = new PurchaseApplicationExcelListener(skuList,warehouseList,skuIds,companyList);

        try {
            EasyExcel.read(excelFile.getInputStream(), PurchaseApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<PurchaseApplicationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        PurchaseApplicationDetailDTO.ImportDTO importDTO = new PurchaseApplicationDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchaseApplicationDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<PurchaseApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购申请错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchaseApplicationImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public Boolean exportExcel(PurchaseApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<PurchaseApplicationExportExcelDTO> exportExcelList = baseMapper.listExportExcel(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/purchaseApplicationExport.xlsx";
        String name = "采购申请单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportExcelList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        log.info("采购申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        purchaseApplicationDetailService.removeByPurchaseApplicationIds(ids);
        //删除主表数据
        return  this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);
        //待提交并且未作废允许提交
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        log.info("采购申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));
        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个采购申请单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"提交操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(PurchaseApplicationDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    @Override
    public PurchaseApplicationDTO.ViewDTO view(String id) {
        PurchaseApplicationDTO.ViewDTO dto = new PurchaseApplicationDTO.ViewDTO();

        //主表信息
        PurchaseApplicationEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        BeanMapperUtils.copy(entity,dto);

        //明细信息
        List<PurchaseApplicationDetailEntity> entityDetails = purchaseApplicationDetailService.listByPurchaseApplicationId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(PurchaseApplicationDetailDTO.UpdateDTO.class, entityDetails);
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<PurchaseApplicationEntity> list = getList(ids);

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("采购申请单撤销流程，ids=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("采购申请单【%s】取消流程", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        return  purchasePriceDetailService.getTaxPrice(dto);
    }


    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(PurchaseApplicationEntity::getId,ids)
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids,String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(PurchaseApplicationEntity::getId,ids)
                .set(PurchaseApplicationEntity::getApproveUserId,userInfo.getUid())
                .set(PurchaseApplicationEntity::getApproveUserName,userInfo.getUserName())
                .set(PurchaseApplicationEntity::getApproveStatus,approveStatus)
                .set(PurchaseApplicationEntity::getApproveTime,LocalDateTime.now())
                .update();
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String applyUserId,String applyDeptId,PurchaseApplicationEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO applyUser = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(applyUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setApplyUserName(applyUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(applyDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(applyDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setApplyDeptName(depart.getName());
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<PurchaseApplicationEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<PurchaseApplicationEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        return list;
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseApplicationDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseApplicationDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseApplicationDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseApplicationDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<PurchaseApplicationDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<PurchaseApplicationDetailDTO.UpdateDTO> list,String purchaseApplicationId) {
        Map<String, List<PurchaseApplicationDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseApplicationDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseApplicationDetailDTO.UpdateDTO>> entry: map.entrySet()) {
            List<PurchaseApplicationDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
            PurchaseApplicationDetailEntity entity = purchaseApplicationDetailService.getByPurchaseApplicationIdAndSkuId(purchaseApplicationId, entry.getKey());
            if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(value.get(0).getId())) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】已存在")));
            }
        }
    }

}

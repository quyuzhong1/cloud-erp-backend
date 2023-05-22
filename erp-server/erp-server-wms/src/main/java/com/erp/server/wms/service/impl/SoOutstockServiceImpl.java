package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Resource
    private CommonService commonService;

    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().in(SoOutstockEntity::getSourceId, ids).list();
    }


    /**
     * 添加销售出库单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-19 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoOutstockDTO.AddDTO dto) {
        //TODO 对应检查数量
        String id = IdWorker.getIdStr();
        //来源类型
        String sourceType = dto.getSourceType();

        String sourceId = dto.getSourceId();

        //检查出库数量
        soOutstockDetailService.checkOutQty(sourceId,sourceType,dto.getDetailList());


        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setId(id);
        List<SoOutstockDetiailDTO.AddDTO> detailList = dto.getDetailList();
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSCK, BusinessNoTypeEnum.CODE_XSCK.getCode()));
        soOutstock.setCode(code);
        //发货组织
        String deliveryOrgId = dto.getDeliveryOrgId();
        //仓库id
        String warehouseId = dto.getWarehouseId();
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                soOutstock.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(deliveryOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(deliveryOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soOutstock.setDeliveryOrgName(salesOrgName);
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {
            soOutstockDetailService.add(id, detailList);
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }

    /**
     * 批量提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:34
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoOutstockEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        String userName = commonService.getUserInfo().getUserName();
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(rejectStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), userName);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");
        }
        return result;
    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:42
     */
    @Override
    public Boolean addAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 销售出库单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     * @author yl
     * @date 2023-05-19 10:45
     */
    @Override
    public SoOutstockDTO.ViewDTO view(String id) {
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        SoOutstockDTO.ViewDTO result = new SoOutstockDTO.ViewDTO();
        BeanMapper.copy(soOutstock, result);
        String soId = soOutstock.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
        if (soInfo != null) {
            result.setCustomerName(soInfo.getCustomerName());
            result.setSoCode(soInfo.getCode());
            result.setReceiveAddress(soInfo.getReceiveAddress());
            result.setReceiverName(soInfo.getReceiverName());
            result.setDeliveryModeName(soInfo.getDeliveryModeName());
            result.setSalesOrgName(soInfo.getSalesOrgName());
            result.setRequireDate(soInfo.getRequireDate());
            result.setTelNumber(soInfo.getTelNumber());
            result.setTypeName(soInfo.getOrderTypeName());
            result.setSellerName(soInfo.getSellerName());
        }
        List<SoOutstockDetiailDTO.ViewDTO> detailList = soOutstockDetailService.listByMainId(id, soOutstock.getWarehouseId());
        result.setDetailList(detailList);
        return result;
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 11:42
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        String userName = commonService.getUserInfo().getUserName();
        //TODO 需要做什么 释放冻结 销售订单的发货状态
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus), userName);
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus), userName);
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:10
     */
    @Override
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        String userName = commonService.getUserInfo().getUserName();
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveIngStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), userName);
        //反审核 TODO 需要做什么
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");

        }
        return result;
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:13
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售出库单【%s】取消流程", ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "取消流程操作");
        return result;

    }


    /**
     * 删除销售出库单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:16
     */
    @Override
    public Boolean delete(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "删除");
            //删除明细
            soOutstockDetailService.removeByMainIdList(ids);

        }
        return result;
    }


    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 14:17
     */
    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(waitSubmitStatus);
        statusList.add(rejectStatus);
        long invalidCount = list.stream().filter(d -> !d.getInvalidStatus()).count();
        if (invalidCount != list.size()) {
            throw new ServiceException(ApiError.ERROR_98061);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        lambdaUpdate().in(SoOutstockEntity::getId, ids).
                set(SoOutstockEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售出库单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "作废");
        return Boolean.TRUE;

    }


    /**
     * 获取tab
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.TabListDTO>
     * @author yl
     * @date 2023-05-19 14:23
     */
    @Override
    public List<SoOutstockDTO.TabListDTO> tabList() {
        List<SoOutstockDTO.TabListDTO> resultList = new ArrayList<>();
        //全部
        List<SoOutstockEntity> list = this.list();
        SoOutstockDTO.TabListDTO all = new SoOutstockDTO.TabListDTO();
        all.setCount(list.size());
        all.setSearchType(SearchType.ALL);
        resultList.add(all);

        //待审核
        ApproveStatusEnum ing = ApproveStatusEnum.getByStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        SoOutstockDTO.TabListDTO waitApprove = new SoOutstockDTO.TabListDTO();
        waitApprove.setCount((int) list.stream().filter(l -> ing.equals(l.getApproveStatus())).count());
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        ApproveStatusEnum approveStatus = ApproveStatusEnum.getByStatus(ApproveStatusEnum.APPROVE.getStatus());
        SoOutstockDTO.TabListDTO approve = new SoOutstockDTO.TabListDTO();
        approve.setCount((int) list.stream().filter(l -> approveStatus.equals(l.getApproveStatus())).count());
        approve.setSearchType(ApproveStatusEnum.APPROVE.getStatus());
        resultList.add(approve);

        //审核不通过
        ApproveStatusEnum rejectStatus = ApproveStatusEnum.getByStatus(ApproveStatusEnum.REJECT.getStatus());
        SoOutstockDTO.TabListDTO reject = new SoOutstockDTO.TabListDTO();
        reject.setCount((int) list.stream().filter(l -> rejectStatus.equals(l.getApproveStatus())).count());
        reject.setSearchType(ApproveStatusEnum.REJECT.getStatus());
        resultList.add(reject);
        return resultList;


    }

    /**
     * 分页列表
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-22 8:56
     */
    @Override
    public PagingVO<SoOutstockDTO.PagingViewDTO> paging(PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        SoOutstockDTO.PagingParamDTO params = dto.getParams();
        params.setParam(dto.getParam());
        String searchType = params.getSearchType();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<SoOutstockDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> soIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoDTO.CustomerDTO> soCustomerList = soInfoFeign.listSoCustomer(soIdList);
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> flagList = new ArrayList<>();

        for (SoOutstockDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            SoInfoDTO.CustomerDTO soInfo = soCustomerList.stream().filter(s -> s.getId().equals(soId)).findFirst().orElse(new SoInfoDTO.CustomerDTO());
            item.setTypeName(soInfo.getOrderTypeName());
            item.setSalesOrgName(soInfo.getSalesOrgName());
            item.setCustomerName(soInfo.getCustomerName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
            if (contains) {
                item.setId("");
                item.setCode("");
                item.setSoCode("");
                item.setSourceCode("");
                item.setTypeName("");
                item.setApproveStatusName("");
                item.setCustomerName("");
                item.setDeliveryOrgName("");
                item.setSalesOrgName("");
                item.setPlanDeliveryDate(null);
                item.setPackDate(null);
                item.setActualDeliveryDate(null);
                item.setCreateUserName("");
                item.setCreateTime(null);
            }
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出销售出库单
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-22 11:41
     */
    @Override
    public Boolean exportExcel(SoOutstockDTO.ExportDTO dto, HttpServletResponse response) {
        String searchType = dto.getSearchType();
        List<String> approveList = listBySearchType(searchType);

        //获取导出数据
        List<SoOutstockDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }

        List<String> soIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoDTO.CustomerDTO> soCustomerList = soInfoFeign.listSoCustomer(soIdList);
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoOutstockDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            SoInfoDTO.CustomerDTO soInfo = soCustomerList.stream().filter(s -> s.getId().equals(soId)).findFirst().orElse(new SoInfoDTO.CustomerDTO());
            item.setTypeName(soInfo.getOrderTypeName());
            item.setSalesOrgName(soInfo.getSalesOrgName());
            item.setCustomerName(soInfo.getCustomerName());
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soOutstock.xlsx";
        String name = "销售订单出库列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售订单出库导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private List<String> listBySearchType(String searchType) {
        List<String> approveList = new ArrayList<>();
        //待审核
        if (SearchType.WAIT_APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        //已审核
        if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE.getStatus());
        }

        //审核不通过
        if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
            approveList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        return approveList;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return
     */
    private Boolean updateApproveStatus(List<SoOutstockEntity> list, ApproveStatusEnum statusEnum, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoOutstockEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return true;
    }

}

package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoChangeMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单变更 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoChangeServiceImpl extends SuperServiceImpl<SoChangeMapper, SoChangeEntity> implements SoChangeService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-18 11:54
     */
    @Override
    public String add(SoChangeDTO.AddDTO dto) {
        //销售订单
        String soId = dto.getSoId();
        //检查能否变更
        checkIsChange(soId);
        String id = IdWorker.getIdStr();
        SoChangeEntity soChange = new SoChangeEntity();
        BeanMapper.copy(dto, soChange);
        String useId = dto.getUseId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isEmpty(useId)) {
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(useId);
            if (userInfo != null) {
                useName = userInfo.getUserName();
            }
        }
        if (StringUtils.isNotBlank(deptId)) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(deptId);
            if (dept != null) {
                deptName = dept.getName();
            }
        }
        soChange.setDeptName(deptName);
        soChange.setUserName(useName);
        soChange.setId(id);
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSBG, BusinessNoTypeEnum.CODE_XSBG.getCode()));
        soChange.setCode(code);
        Boolean addResult = this.save(soChange);
        if (addResult) {
            //添加日志
            soChangeDetailService.addDetailList(id, dto.getDetailList());
            String content = String.format("新增了一个{%s}-销售变更单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_CHANGE.getCode(), id, "新增操作");
            return id;
        }
        return "";
    }


    /**
     * 检查能否变更
     * 单据状态为已审核并且不在“变更中”才可变更
     *
     * @param soId
     * @return void
     * @author yl
     * @date 2023-05-24 16:10
     */
    private void checkIsChange(String soId) {
        SoInfoEntity soInfo = soInfoService.getById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        String approveStatus = soInfo.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        if (!approve.equals(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_92033);
        }


    }


    /**
     * 根据 so id 获取对应数据
     *
     * @return
     */
    public List<SoChangeEntity> listBySoId(String soId, String id) {
        LambdaQueryWrapper<SoChangeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoChangeEntity::getSoId, soId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SoChangeEntity::getId, id);
        }
        return this.list(queryWrapper);
    }


    /**
     * 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-24 14:45
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoChangeEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-24 14:53
     */
    @Override
    public Boolean addAndSubmit(SoChangeDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 获取tab 列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.TabListDTO>
     * @author yl
     * @date 2023-05-24 14:56
     */
    @Override
    public List<SoChangeDTO.TabListDTO> tabList() {
        List<SoChangeDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<SoChangeDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount();
        int allCount = approveCountList.stream().mapToInt(SoChangeDTO.ApproveCountDTO::getCount).sum();
        SoChangeDTO.TabListDTO all = new SoChangeDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);

        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoChangeDTO.TabListDTO waitApprove = new SoChangeDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        SoChangeDTO.TabListDTO approve = new SoChangeDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoChangeDTO.TabListDTO reject = new SoChangeDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoChangeDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-24 16:44
     */
    @Override
    public PagingVO<SoChangeDTO.PagingViewDTO> paging(PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        SoChangeDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String searchType = params.getSearchType();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //根据搜索类型获取到审核状态
        List<String> approveList = listBySearchType(searchType);
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<SoChangeDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //客户id
        List<String> customerIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<String> flagList = new ArrayList<>();
        for (SoChangeDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            BillTypeEnum orderType = item.getOrderType();
            item.setOrderTypeName(orderType.getName());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String customerName = customerList.stream().filter(c -> c.getId().equals(item.getCustomerId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCustomerName(customerName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (sku != null) {
                item.setProductName(sku.getSkuName());
                item.setUnit(sku.getUnitName());
            }
            if (contains) {
                item.setId("");
                item.setCode("");
                item.setSoCode("");
                item.setOrderType(null);
                item.setOrderTypeName("");
                item.setApproveStatusName("");
                item.setCustomerName("");
                item.setInvalidStatusName("");
                item.setCreateUserName("");
                item.setCreateTime(null);
            }
            flagList.add(item.getId());
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-24 17:47
     */
    @Override
    public Boolean exportExcel(SoChangeDTO.ExportDTO dto, HttpServletResponse response) {
        String searchType = dto.getSearchType();
        List<String> approveList = listBySearchType(searchType);
        List<SoChangeDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }

        List<String> skuIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //客户id
        List<String> customerIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        for (SoChangeDTO.PagingViewDTO item : list) {
            BillTypeEnum orderType = item.getOrderType();
            item.setOrderTypeName(orderType.getName());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String customerName = customerList.stream().filter(c -> c.getId().equals(item.getCustomerId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCustomerName(customerName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (sku != null) {
                item.setProductName(sku.getSkuName());
                item.setUnit(sku.getUnitName());
            }
            BigDecimal amount = item.getAmount();
            String currencySymbol = item.getCurrencySymbol();
            item.setAmountStr(currencySymbol + amount);

            BigDecimal oldAmount = item.getOldAmount();
            String oldCurrencySymbol = item.getOldCurrencySymbol();
            item.setOldAmountStr(oldCurrencySymbol + oldAmount);
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SoChange.xlsx";
        String name = "销售变更单列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售变更单列表导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;


    }


    /**
     * 详情
     *
     * @param id
     * @return com.erp.model.oms.dto.SoChangeDTO.ViewDTO
     * @author yl
     * @date 2023-05-24 18:04
     */
    @Override
    public SoChangeDTO.ViewDTO view(String id) {
        SoChangeEntity soChange = this.getById(id);
        if(Objects.isNull(soChange)){
            throw new ServiceException(ApiError.ERROR_92034);
        }
        return null;
    }

    private List<String> listBySearchType(String searchType) {
        List<String> approveList = new ArrayList<>(3);
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

    private Boolean updateApproveStatus(List<SoChangeEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoChangeEntity item : list) {
                item.setApproveStatus(statusEnum);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }
}

package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private CommonService commonService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-18 11:54
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoChangeDTO.AddDTO dto) {
        //销售订单
        String soId = dto.getSoId();
        //检查能否变更
        checkIsChange(soId);
        //检查对应详情的变更类型
        soChangeDetailService.checkChange(dto.getDetailList());
        String id = IdWorker.getIdStr();
        SoChangeEntity soChange = new SoChangeEntity();
        BeanMapper.copy(dto, soChange);
        String userId = dto.getUserId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isEmpty(userId)) {
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(userId);
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
     * 更改销售变更单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-25 11:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSoChange(SoChangeDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoChangeEntity soChange = this.getById(id);
        if (Objects.isNull(soChange)) {
            throw new ServiceException(ApiError.ERROR_92034);
        }
        List<SoChangeDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //检查对应详情的变更类型
        soChangeDetailService.checkChange(BeanMapper.copyList(detailList, SoChangeDetailDTO.AddDTO.class));
        String code = soChange.getCode();
        //旧的
        SoChangeEntity old = new SoChangeEntity();
        BeanMapper.copy(soChange, old);
        BeanMapper.copy(dto, soChange);
        soChange.setCode(code);
        String userId = dto.getUserId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isEmpty(userId)) {
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(userId);
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
        Boolean updateResult = this.updateById(soChange);
        if(updateResult){
            operateLogService.addModuleOperateLogByObj(old, soChange, ModuleTypeEnum.SO_CHANGE.getCode(), id, "", "");
            soChangeDetailService.updateDetailList(id, dto.getDetailList());
            return id;
        }
        return "";
    }


    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-25 12:23
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    public Boolean updateAndSubmit(SoChangeDTO.UpdateDTO dto) {
        String id = this.updateSoChange(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }




    /**
     * 检查能否变更
     * 单据状态为已审核并且不在“变更中”才可变更
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
    @Transactional(rollbackFor = Exception.class)
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
        if (Objects.isNull(soChange)) {
            throw new ServiceException(ApiError.ERROR_92034);
        }
        SoChangeDTO.ViewDTO view = new SoChangeDTO.ViewDTO();
        BeanMapper.copy(soChange, view);
        String soId = soChange.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        view.setAddressTypeName(soInfo.getAddressTypeName());
        ApproveStatusEnum approveStatus = soChange.getApproveStatus();
        view.setApproveStatusName(approveStatus.getName());
        view.setCurrency(soInfo.getCurrency());
        view.setCustomerId(soInfo.getCustomerId());
        view.setCustomerName(soInfo.getCustomerName());
        view.setIsTax(soInfo.getIsTax());
        view.setDeliveryModeName(soInfo.getDeliveryModeName());
        view.setOrderTypeName(soInfo.getOrderTypeName());
        view.setOrderType(soInfo.getOrderType());
        view.setReceiveAddress(soInfo.getReceiveAddress());
        view.setReceiverName(soInfo.getReceiverName());
        view.setSoCode(soInfo.getCode());
        view.setSoId(soInfo.getId());
        view.setTelNumber(soInfo.getTelNumber());
        //根据主表id 获取详情
        List<SoChangeDetailDTO.ViewDTO> detailList = soChangeDetailService.listDetailByMainId(id);
        view.setDetailList(detailList);
        return view;
    }



    /**
     * 根据销售订单id 获取到对应详情
     * @author yl
     * @date 2023-05-25 14:04
     * @param soId
     * @return com.erp.model.oms.dto.SoChangeDTO.ViewDTO
     */
    @Override
    public SoChangeDTO.ViewDTO getViewBySoId(String soId) {
        SoChangeDTO.ViewDTO view = new SoChangeDTO.ViewDTO();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        view.setAddressTypeName(soInfo.getAddressTypeName());
        ApproveStatusEnum approveStatus = ApproveStatusEnum.WAIT_SUBMIT;
        view.setApproveStatusName(approveStatus.getName());
        view.setApproveStatus(approveStatus);
        view.setCurrency(soInfo.getCurrency());
        view.setCustomerId(soInfo.getCustomerId());
        view.setCustomerName(soInfo.getCustomerName());
        view.setIsTax(soInfo.getIsTax());
        view.setDeliveryModeName(soInfo.getDeliveryModeName());
        view.setOrderTypeName(soInfo.getOrderTypeName());
        view.setOrderType(soInfo.getOrderType());
        view.setReceiveAddress(soInfo.getReceiveAddress());
        view.setReceiverName(soInfo.getReceiverName());
        view.setSoCode(soInfo.getCode());
        view.setSoId(soInfo.getId());
        view.setTelNumber(soInfo.getTelNumber());
        //根据主表id 获取详情
        List<SoChangeDetailDTO.ViewDTO> detailList = soChangeDetailService.listDetailBySoId(soId);
        view.setDetailList(detailList);
        return view;
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 10:46
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoChangeEntity> list = this.listByIds(ids);
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
        LoginUser user = commonService.getUserInfo();
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            result = this.updateApproveInfo(list, ApproveStatusEnum.APPROVE, user.getUid(), user.getUserName());
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            result = this.updateApproveInfo(list, ApproveStatusEnum.REJECT, user.getUid(), user.getUserName());
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_CHANGE.getCode(), pairList, "状态变更");
        }
        return result;
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 11:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoChangeEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_92034);
        }
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.WAIT_SUBMIT);
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售变更单【%s】取消流程", ModuleTypeEnum.SO_CHANGE.getCode(), pairList, "取消流程操作");
        return result;
    }


    /**
     * 删除销售变更单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 11:05
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        List<SoChangeEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_92034);
        }
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
            String content = "删除销售变更单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_CHANGE.getCode(), pairList, "删除");
            //删除明细
            soChangeDetailService.removeByMainIdList(ids);

        }
        return result;
    }


    /**
     * 作废单据
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 11:14
     */
    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<SoChangeEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_92034);
        }
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
        lambdaUpdate().in(SoChangeEntity::getId, ids).
                set(SoChangeEntity::getInvalidStatus, Boolean.TRUE).
                set(SoChangeEntity::getInvalidRemark, remark).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售变更单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_CHANGE.getCode(), pairList, "作废");
        return Boolean.TRUE;
    }


    /**
     * 更改审核信息
     *
     * @param list
     * @param approveStatus
     * @param approveUserId
     * @param approveUserName
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 10:52
     */
    private Boolean updateApproveInfo(List<SoChangeEntity> list, ApproveStatusEnum approveStatus, String approveUserId, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoChangeEntity item : list) {
                item.setApproveStatus(approveStatus);
                item.setApproveUserId(approveUserId);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
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

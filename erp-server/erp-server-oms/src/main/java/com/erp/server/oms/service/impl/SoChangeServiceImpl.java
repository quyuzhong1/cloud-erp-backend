package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
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
import com.common.core.utils.*;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.CustomerAddressTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoChangeService;
import com.erp.server.oms.mapper.SoChangeMapper;
import com.erp.server.oms.query.SoChangeQueryHandler;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_CHANGE;

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
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SyncKingdeeSoChangeService syncKingdeeSoChangeService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private SoChangeQueryHandler soChangeQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


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
        List<SoChangeDetailDTO.UpdateDTO> checkList= BeanMapper.copyList(dto.getDetailList(),SoChangeDetailDTO.UpdateDTO.class);
        soChangeDetailService.checkChange(checkList);
        String id = IdWorker.getIdStr();
        SoChangeEntity soChange = new SoChangeEntity();
        BeanMapper.copy(dto, soChange);
        String userId = dto.getUserId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isNotBlank(userId)) {
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

        // 判断是否可以修改地址和联系人信息
        checkChangeCustomerInfo(soId, dto.getReceiveAddressId(), dto.getAddressType(), dto.getReceiverName(), dto.getTelNumber());
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSBG, BusinessNoTypeEnum.CODE_XSBG.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSBG);
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
        soChangeDetailService.checkChange(detailList);
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
        if (StringUtils.isNotBlank(userId)) {
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


        // 判断是否可以修改地址和联系人信息
        checkChangeCustomerInfo(soChange.getSoId(), dto.getReceiveAddressId(), dto.getAddressType(), dto.getReceiverName(), dto.getTelNumber());

        Boolean updateResult = this.updateById(soChange);
        if (updateResult) {
            operateLogService.addModuleOperateLogByObj(old, soChange, ModuleTypeEnum.SO_CHANGE.getCode(), id, "", "");
            soChangeDetailService.updateDetailList(id, dto.getDetailList());
            return id;
        }
        return "";
    }


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 12:23
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
     * 验证销售变更单
     *
     * @param soId
     * @param receiveAddressId
     * @param addressType
     * @param receiverName
     * @param telNumber
     */
    private void checkChangeCustomerInfo(String soId, String receiveAddressId, String addressType, String receiverName, String telNumber) {
        // 判断是否可以修改地址和联系人信息
        Boolean isPushed = soInfoService.checkSoPushDeliveryNotice(soId);
        if (Objects.equals(isPushed, Boolean.TRUE)) {
            // 已下推发货通知单不允许修改收货地址和联系人信息
            SoInfoEntity soInfoEntity = soInfoService.getById(soId);
            ValidatorUtil.isTrue(Objects.nonNull(soInfoEntity), () -> new ServiceException(ApiError.ERROR_92016));
            Boolean isChange = !Objects.equals(soInfoEntity.getReceiveAddressId(), receiveAddressId)
                    || !Objects.equals(StrUtils.null2EmptyWithTrim(soInfoEntity.getAddressType()), StrUtils.null2EmptyWithTrim(addressType))
                    || !Objects.equals(StrUtils.null2EmptyWithTrim(soInfoEntity.getReceiverName()), StrUtils.null2EmptyWithTrim(receiverName))
                    || !Objects.equals(StrUtils.null2EmptyWithTrim(soInfoEntity.getTelNumber()), StrUtils.null2EmptyWithTrim(telNumber));

            ValidatorUtil.isTrue(Objects.equals(isChange, Boolean.FALSE), () -> new ServiceException("销售订单已下推发货通知单，不允许修改地址"));
        }
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoChangeEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }
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

        //提交流程
        startProcess(list);

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
    @GlobalTransactional(rollbackFor = Exception.class)
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
    public List<SoChangeDTO.TabListDTO> tabList(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<SoChangeDTO.TabListDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            if (PageListTypeEnum.WAIT_SUBMIT.equals(item)) {
                continue;
            }
            SoChangeDTO.PagingParamDTO pagingParamDTO = new SoChangeDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            SoChangeDTO.TabListDTO resultDTO = new SoChangeDTO.TabListDTO();
            String tabSql = soChangeQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            pagingParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(pagingParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.equals(PageListTypeEnum.TO_BE_APPROVE) ? "待我审核" : item.getName());
            list.add(resultDTO);
        }
        return list;
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        IPage pageData = baseMapper.paging(query, params);
        List<SoChangeDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        List<String> ids = list.stream().map(SoChangeDTO.PagingViewDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_CHANGE.getCode(), obj));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(new ApiResult(ApiError.Default.code,listApiResult.getMsg()));
        }

        //客户id
        List<String> customerIdList = list.stream().map(SoChangeDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        //客户
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        // 国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();

        for (SoChangeDTO.PagingViewDTO item : list) {
            BillTypeEnum orderType = item.getOrderType();
            item.setOrderTypeName(orderType.getName());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            //客户id
            String customerId = item.getCustomerId();
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            CustomerInfoEntity customerInfo = customerList.stream().filter(c -> c.getId().
                    equals(item.getCustomerId())).findFirst().orElse(null);
            if (Objects.nonNull(customerInfo)) {
                //国家id
                String countryId = customerInfo.getCountryId();
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(countryId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                item.setCountryName(countryName);
                item.setCustomerName(customerInfo.getName());
            } else {
                item.setCustomerName("");
                item.setCountryName("");
            }

            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (sku != null) {
                item.setProductName(sku.getSkuName());
                item.setUnit(sku.getUnitName());
            }
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
    public Boolean exportExcel(SoChangeDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售变更单列表",EXPORT_OMS_SO_CHANGE.getCode(), dto);
        return true;
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
        view.setCurrencySymbol(soInfo.getCurrencySymbol());
        view.setCustomerId(soInfo.getCustomerId());
        view.setCustomerName(soInfo.getCustomerName());
        view.setIsTax(soInfo.getIsTax());
        view.setDeliveryModeName(soInfo.getDeliveryModeName());
        view.setOrderTypeName(soInfo.getOrderTypeName());
        view.setOrderType(soInfo.getOrderType());
        view.setReceiveAddressId(soChange.getReceiveAddressId());
        if (StrUtils.isNotEmpty(view.getReceiveAddressId())) {
            CustomerAddressEntity addressEntity = customerAddressService.getById(view.getReceiveAddressId());
            if (Objects.nonNull(addressEntity)) {
                view.setReceiveAddress(addressEntity.getAddress());
            }
        } else {
            view.setReceiveAddress(soInfo.getReceiveAddress());
        }
        view.setReceiverName(soChange.getReceiverName());
        view.setSoCode(soInfo.getCode());
        view.setSoId(soInfo.getId());
        view.setTelNumber(soChange.getTelNumber());
        view.setAddressType(soChange.getAddressType());
        if (StrUtils.isNotEmpty(view.getAddressType())) {
            view.setAddressTypeName(CustomerAddressTypeEnum.getName(view.getAddressType()));
        }
        view.setSalesOrgName(soInfo.getSalesOrgName());
        view.setSellerId(soInfo.getSellerId());
        view.setSellerName(soInfo.getSellerName());
        //根据主表id 获取详情
        List<SoChangeDetailDTO.ViewDTO> detailList = soChangeDetailService.listDetailByMainId(id);
        view.setDetailList(detailList);
        return view;
    }


    /**
     * 根据销售订单详情 获取到对应详情
     *
     * @param soDetailIds
     * @return com.erp.model.oms.dto.SoChangeDTO.ViewDTO
     * @author yl
     * @date 2023-05-25 14:04
     */
    @Override
    public SoChangeDTO.ViewDTO getViewBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<SoDetailEntity> soDetailList = soDetailService.listByIds(soDetailIds);
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<String> soIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if (soIdList.size() > 1) {
            throw new ServiceException(ApiError.ERROR_92039);
        }
        String soId = soDetailList.get(0).getMainId();
        SoChangeDTO.ViewDTO view = new SoChangeDTO.ViewDTO();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        String soInfoApproveStatus = soInfo.getApproveStatus().getStatus();
        String soApproveStatus = ApproveStatusEnum.APPROVE.getStatus();
        if (!soInfoApproveStatus.equals(soApproveStatus)) {
            throw new ServiceException(ApiError.ERROR_92033);
        }
        view.setAddressTypeName(soInfo.getAddressTypeName());
        ApproveStatusEnum approveStatus = ApproveStatusEnum.WAIT_SUBMIT;
        view.setApproveStatusName(approveStatus.getName());
        view.setApproveStatus(approveStatus);
        view.setCurrency(soInfo.getCurrency());
        view.setCurrencySymbol(soInfo.getCurrencySymbol());
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
        List<SoChangeDetailDTO.ViewDTO> detailList = soChangeDetailService.listDetailBySoId(soId, soDetailIds, true);
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 销售订单 关联的销售变更单
     *
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.SoRefDTO>
     * @author yl
     * @date 2023-05-25 16:06
     */
    @Override
    public List<SoChangeDTO.SoRefDTO> listSoRefSoChangeBySoId(String soId) {
        SoInfoDTO.CustomerDTO soCustomer = soInfoService.getSoCustomer(soId);
        List<SoChangeDTO.SoRefDTO> resultList = baseMapper.listSoRefSoChangeBySoId(soId);
        List<String> skuIdList = resultList.stream().map(SoChangeDTO.SoRefDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SoChangeDTO.SoRefDTO item : resultList) {
            String skuId = item.getSkuId();
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            item.setOrderType(soCustomer.getOrderType());
            item.setOrderTypeName(soCustomer.getOrderTypeName());
            item.setCustomerId(soCustomer.getCustomerId());
            item.setCustomerName(soCustomer.getCustomerName());
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String productName = "";
            String unit = "";
            if (sku != null) {
                productName = sku.getSkuName();
                unit = sku.getUnitName();
            }
            item.setProductName(productName);
            item.setUnit(unit);
            BigDecimal amount = item.getAmount();
            String currencySymbol = item.getCurrencySymbol();
            item.setAmountStr(currencySymbol + amount);

            BigDecimal oldAmount = item.getOldAmount();
            String oldCurrencySymbol = item.getOldCurrencySymbol();
            item.setOldAmountStr(oldCurrencySymbol + oldAmount);
        }

        return resultList;
    }

    /**
     * 根据选择销售订单id 获取到对应的sku 信息
     *
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-26 9:26
     */
    @Override
    public List<SoChangeDetailDTO.SoDetailViewDTO> listSoSkuBySoId(String soId) {
        //根据主表id 获取详情
        List<SoChangeDetailDTO.SoDetailViewDTO> detailList = soChangeDetailService.listSelectDetailBySoId(soId, Collections.emptyList(), Boolean.FALSE);
        return detailList;
    }


    /**
     * 检测能否变更根据so 详情id
     *
     * @param soDetailIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-26 14:42
     */
    @Override
    public List<String> checkBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<SoDetailEntity> soDetailList = soDetailService.listByIds(soDetailIds);
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<String> soIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if (soIdList.size() > 1) {
            throw new ServiceException(ApiError.ERROR_92039);
        }
        String soId = soDetailList.get(0).getMainId();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        String soInfoApproveStatus = soInfo.getApproveStatus().getStatus();
        String soApproveStatus = ApproveStatusEnum.APPROVE.getStatus();
        if (!soInfoApproveStatus.equals(soApproveStatus)) {
            throw new ServiceException(ApiError.ERROR_92033);
        }
        return soDetailIds;
    }

    /**
     * 根据销售订单详情 获取到变更的
     *
     * @param soIds
     * @return void
     * @author yl
     * @date 2023-05-30 17:24
     */
    @Override
    public List<SoChangeEntity> listBySoIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoChangeEntity::getSoId, soIds).list();
    }


    /**
     * 更改销售订单金蝶推送的状态
     *
     * @param id
     * @param syncKingdeeId
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {

        if (StringUtils.isEmpty(id)) {
            return Boolean.TRUE;
        }
        SoChangeEntity soChange = this.getById(id);
        if (Objects.nonNull(soChange)) {
            Boolean existAdd = soChangeDetailService.existAdd(id);
            //如果有添加新的sku 销售订单需要重新推送
            if (existAdd) {
                String soId = soChange.getSoId();
                soDetailService.updateDetailKingdeeId(soId);
            }
        }
        return this.lambdaUpdate()
                .eq(SoChangeEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SoChangeEntity::getSyncKingdeeId, syncKingdeeId)
                .update();

    }

    @Override
    public PagingVO<SoChangeDTO.PagingViewDTO> exportSoChange(PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        Page<SoChangeDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        List<String> skuIdList = page.getRecords().stream().map(SoChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //客户id
        List<String> customerIdList = page.getRecords().stream().map(SoChangeDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<String> ids = page.getRecords().stream().map(SoChangeDTO.PagingViewDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_CHANGE.getCode(), obj)));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(new ApiResult<>(ApiError.Default.code,listApiResult.getMsg()));
        }
        for (SoChangeDTO.PagingViewDTO item : page.getRecords()) {
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
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }
            BigDecimal amount = item.getAmount();
            String currencySymbol = item.getCurrencySymbol();
            item.setAmountStr(currencySymbol + amount);

            BigDecimal oldAmount = item.getOldAmount();
            String oldCurrencySymbol = item.getOldCurrencySymbol();
            item.setOldAmountStr(oldCurrencySymbol + oldAmount);
        }
        return new PagingVO<>(page);
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(BaseApproveParamDTO dto, SoChangeEntity entity) {
        List<SoChangeEntity> list = Arrays.asList(entity);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        //调用审核流程
        approveProcess(list, dto);

        //添加日志
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售变更", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO_CHANGE.getCode(), pairList, "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    /**
     * @param dto
     * @param list
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<SoChangeEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        LoginUser user = UserContext.getDefaultLoginUser();
        ApproveStatusEnum approveStatus;
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
        }
        Boolean result = this.updateApproveInfo(list, approveStatus, user.getUid(), user.getUserName());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<DmpPushTaskEntity> pushTaskList = new ArrayList<>();
        if (dto.getType().equals(ApproveType.PASS)) {
            //销售变更单校验
            List<String> idList = list.stream().map(SoChangeEntity::getId).collect(Collectors.toList());
            List<SoChangeDetailEntity> soChangeDetailList = soChangeDetailService.listByMainIdList(idList);
            if (CollectionUtils.isEmpty(soChangeDetailList)) {
                throw new ServiceException(ApiError.ERROR_92036);
            }
            List<SoChangeDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(SoChangeDetailDTO.UpdateDTO.class, soChangeDetailList);
            soChangeDetailService.checkChange(updateList);

            //更新销售表数据
            soChangeDetailService.handleDb(list);
            //审核通过发送金蝶
            list.forEach(obj -> {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeSoChangeService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
                pushTaskList.add(pushTaskEntity);
            });
        }
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(pushTaskList);
            }
        });
        return Boolean.TRUE;
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

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_CHANGE.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

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
    @Transactional(rollbackFor = Exception.class)
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
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<SoChangeEntity> list) {
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        //销售订单id 集合
        List<String> soIdList = list.stream().map(SoChangeEntity::getSoId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = CollectionUtils.isNotEmpty(soIdList) ? soInfoService.listByIds(soIdList) : Collections.emptyList();
        list.forEach(obj -> {
            String sellerId = soInfoList.stream().filter(s -> s.getId().equals(obj.getSoId())).
                    map(SoInfoEntity::getSellerId).findFirst().orElse("");
            if (StringUtils.isNotBlank(sellerId)) {
                ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
                startDTO.setBusinessId(obj.getId());
                startDTO.setBusinessCode(obj.getCode());
                startDTO.setBusinessKey(SourceTypeEnum.SO_CHANGE.getCode());
                startDTO.setBusinessName(obj.getCode());
                startDTO.setUserId(sellerId);
                startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
                resultList.add(startDTO);
            }

        });
        if (CollectionUtils.isNotEmpty(resultList)) {
            ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
            if (!listApiResult.isSuccess()) {
                throw new ServiceException(listApiResult.getMsg());
            }
        }
    }

    /**
     * @param list
     * @param dto
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     */
    private void approveProcess(List<SoChangeEntity> list, BaseApproveParamDTO dto) {
        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.SO_CHANGE.getCode());
            approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
            approveDTO.setComment(dto.getComment());
            approveDTO.setUserId(userInfo.getUid());
            approveDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(approveDTO);
        });
        ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> listApiResult = workflowFeign.batchApproveProcess(resultList);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<ProcessManagementDTO.ApproveResultDTO> data = listApiResult.getData();
        List<String> updateIdList = data.stream()
                .filter(obj -> ObjectUtils.isEmpty(obj.getIsExistProcess()) || !obj.getIsExistProcess())
                .map(ProcessManagementDTO.ApproveResultDTO::getBusinessId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(updateIdList)) {
            //无需走流程的数据则直接更新状态
            List<SoChangeEntity> updateList = list.stream().filter(obj -> updateIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, updateList);
        }
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

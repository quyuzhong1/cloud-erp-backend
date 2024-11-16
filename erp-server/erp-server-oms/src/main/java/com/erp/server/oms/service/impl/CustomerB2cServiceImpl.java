package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AddressTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.mapper.CustomerB2cMapper;
import com.erp.server.oms.service.*;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class CustomerB2cServiceImpl extends SuperServiceImpl<CustomerB2cMapper, CustomerB2cEntity> implements CustomerB2cService {


    @Resource
    private CustomerB2cContactService customerB2cContactService;

    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    @Resource
    private OmsAttachmentService omsAttachmentService;


    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private SoInfoService soInfoService;


    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private DocNoGenHelper docNoGenHelper;



    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;

    /**
     * 获取到分组的id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:10
     */
//    @Override
//    public List<String> listGroup() {
//        List<CustomerB2cEntity> list = this.list();
//        return list.stream().map(CustomerB2cEntity::getGroupId).distinct().collect(Collectors.toList());
//    }


    /**
     * 添加客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-12 10:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(CustomerB2CDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //客户联系人
        List<CustomerContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        customerB2cContactService.checkIsDefault(contactList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.AddDTO> addressList = dto.getAddressList();
        customerB2cAddressService.checkIsDefault(addressList);

        //检查默认发票 银行账号
//        List<InvoiceDTO.AddDTO> invoiceList = dto.getInvoiceList();
//        customerB2cInvoiceService.checkIsDefault(invoiceList);


        //id
        String id = IdWorker.getIdStr();
        CustomerB2cEntity addEntity = new CustomerB2cEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //国家id
        String countryId = dto.getCountryId();
        List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(countryId));
        String areaId = globalAreaList.stream().filter(d -> d.getCountryId().equals(countryId)).findFirst().map(DictGlobalAreaDTO.InfoDTO::getId).orElse("");
        addEntity.setAreaId(areaId);
        //分组id
//        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        addEntity.setPayCode(payCode);
        //获取客户分组信息
//        List<CustomerB2cGroupEntity> customerGroupList = customerB2cGroupService.listById(groupId);
//        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        addEntity.setGroupName(groupName);
        //生成单号
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUSTC);
        addEntity.setCode(code);
        //销售员
//        String sellerId = dto.getSellerId();
//        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(sellerId);
//        addEntity.setSellerName(findUserDTO.getUserName());
//        //对应组织
//        String innerOrgId = dto.getInnerOrgId();
//
//        //使用组织
//        String useOrgId = dto.getUseOrgId();
//        //组织列表
//        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

//        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        addEntity.setInnerOrgName(innerOrgName);
//
//        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        addEntity.setUseOrgName(useOrgName);
        //保存客户
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            Class<CustomerB2cEntity> customerClass = CustomerB2cEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);
            //添加日志
            String content = String.format("新增了一个{%s}-客户-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), id, "新增操作");

            //批量保存地址信息
            customerB2cAddressService.saveBatchAddress(id, addressList);

            //批量保存联系人信息
            customerB2cContactService.saveBatchContact(id, contactList);

            //批量保存发票信息
//            customerB2cInvoiceService.saveBatchInvoice(id, invoiceList);


            return id;

        }

        return "";
    }


    /**
     * 提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-12 16:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<CustomerB2cEntity> list = this.listByIds(ids);

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
        // 启动流程
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), "");
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2C.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.CUSTOMER_B2C.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }

    /**
     * 获取tab list
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.TabListDTO>
     * @author yl
     * @date 2023-05-12 17:01
     */
    @Override
    public List<CustomerB2CDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<CustomerB2CDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<CustomerB2CDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(CustomerB2CDTO.ApproveCountDTO::getCount).sum();
        CustomerB2CDTO.TabListDTO all = new CustomerB2CDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);
        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        CustomerB2CDTO.TabListDTO waitApprove = new CustomerB2CDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        CustomerB2CDTO.TabListDTO approve = new CustomerB2CDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        CustomerB2CDTO.TabListDTO reject = new CustomerB2CDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;
    }

    /**
     * 分页信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerB2CDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-12 17:21
     */
    @Override
    public PagingVO<CustomerB2CDTO.PagingViewDTO> paging(PagingDTO<CustomerB2CDTO.PagingParamDTO> dto) {
        CustomerB2CDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        String searchType = params.getSearchType();
        List<String> approveList = new ArrayList<>();
        //待审核
        if (OmsConstant.WAIT_APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        //已审核
        if (OmsConstant.APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE.getStatus());
        }

        //审核不通过
        if (OmsConstant.REJECT.equals(searchType)) {
            approveList.add(ApproveStatusEnum.REJECT.getStatus());
        }

        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<CustomerB2CDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> groupIdList = list.stream().map(CustomerB2CDTO.PagingViewDTO::getGroupId).collect(Collectors.toList());
//        List<CustomerB2cGroupEntity> groupList = CollectionUtils.isNotEmpty(groupIdList) ? customerB2cGroupService.listByIds(groupIdList) : Collections.emptyList();
        List<String> ids = list.stream().map(CustomerB2CDTO.PagingViewDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_B2C.getCode(), obj));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
            }
        }

        for (CustomerB2CDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
//            String groupId = item.getGroupId();
//            String groupName = groupList.stream().filter(g -> g.getId().equals(groupId)).findFirst().
//                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//            item.setGroupName(groupName);
            //最新审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }
        }

        return new PagingVO<>(pageData);
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 9:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(CustomerB2CDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        if (result) {
            return id;
        }
        return "";

    }


    /**
     * 客户详情
     *
     * @param id
     * @return com.erp.model.oms.dto.CustomerB2CDTO.ViewDTO
     * @author yl
     * @date 2023-05-15 9:24
     */
    @Override
    public CustomerB2CDTO.ViewDTO view(String id) {
        CustomerB2CDTO.ViewDTO view = new CustomerB2CDTO.ViewDTO();
        CustomerB2cEntity customer = this.getById(id);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        BeanMapper.copy(customer, view);
        String areaId = customer.getAreaId();
        String payCode = customer.getPayCode();
        view.setPayCodeList(StringUtils.isNotBlank(payCode) ? Arrays.asList(payCode.split(",")) : Collections.emptyList());
        String areaName = "";
        String subregionName = "";
        if (StringUtils.isNotBlank(areaId)) {
            DictGlobalAreaEntity globalArea = sysUserFeign.getGlobalAreaById(areaId);
            if (Objects.nonNull(globalArea)) {
                areaName = globalArea.getRegionName();
                subregionName = globalArea.getSubregionName();
            }
        }
        view.setAreaName(areaName);
        view.setSubregionName(subregionName);
        view.setApproveStatusName(customer.getApproveStatus().getName());
        List<OmsAttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessIds(Arrays.asList(id));
        List<String> attachmentUrlList = attachmentList.stream().
                map(OmsAttachmentDTO.UpdateDTO::getAttachUrl).
                collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().
                map(OmsAttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        view.setAttachUrlList(attachmentUrlList);
        view.setAttachNameList(attachmentNameList);
        //联系人信息
        List<CustomerContactDTO.ViewDTO> contactList = customerB2cContactService.listByMainId(id);
        view.setContactList(contactList);


        //地址信息
        List<CustomerAddressDTO.ViewDTO> addressList = customerB2cAddressService.listByMainId(id);
        view.setAddressList(addressList);

        //发票信息
//        List<InvoiceDTO.ViewDTO> invoiceList = customerB2cInvoiceService.listByMainId(id);
//        view.setInvoiceList(invoiceList);

        //销售员信息
//        List<SellerDTO.ViewDTO> sellerList = customerB2cSellerService.listByMainId(id);
//        view.setSellerList(sellerList);

        return view;
    }


    /**
     * 修改客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 10:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateCustomer(CustomerB2CDTO.UpdateDTO dto) {
        String id = dto.getId();
        CustomerB2cEntity customer = this.getById(id);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }

        //检查名称
        checkName(id, dto.getName());
        //客户联系人
        List<CustomerContactDTO.ViewDTO> contactList = dto.getContactList();
        List<CustomerContactDTO.AddDTO> contactAddList = BeanMapper.copyList(contactList, CustomerContactDTO.AddDTO.class);
        //检查联系人默认是否多个
        customerB2cContactService.checkIsDefault(contactAddList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.ViewDTO> addressList = dto.getAddressList();
        List<CustomerAddressDTO.AddDTO> addressAddList = BeanMapper.copyList(addressList, CustomerAddressDTO.AddDTO.class);
        customerB2cAddressService.checkIsDefault(addressAddList);

        //检查默认发票 银行账号
//        List<InvoiceDTO.ViewDTO> invoiceList = dto.getInvoiceList();
//        List<InvoiceDTO.AddDTO> invoiceAddList = BeanMapper.copyList(invoiceList, InvoiceDTO.AddDTO.class);
//        customerB2cInvoiceService.checkIsDefault(invoiceAddList);

        String code = customer.getCode();

        //旧的
        CustomerB2cEntity old = new CustomerB2cEntity();

        BeanMapper.copy(customer, old);
        //旧销售员
        String oldSellerId = old.getSellerId();
        BeanMapper.copy(dto, customer);

        //分组id
//        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        customer.setPayCode(payCode);
        //国家id
        String countryId = dto.getCountryId();
        List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(countryId));

        String areaId = globalAreaList.stream().filter(d -> d.getCountryId().equals(countryId)).findFirst().map(DictGlobalAreaDTO.InfoDTO::getId).orElse("");
        customer.setAreaId(areaId);
        customer.setCode(code);
        //获取客户分组信息
//        List<CustomerB2cGroupEntity> customerGroupList = customerB2cGroupService.listById(groupId);
//        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        customer.setGroupName(groupName);

        //销售员
//        String sellerId = dto.getSellerId();
//        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(sellerId);
//        customer.setSellerName(findUserDTO.getUserName());

        //对应组织
//        String innerOrgId = dto.getInnerOrgId();

        //使用组织
//        String useOrgId = dto.getUseOrgId();
        //组织列表
//        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

//        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        customer.setInnerOrgName(innerOrgName);

//        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
//                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
//        customer.setUseOrgName(useOrgName);
        Boolean updateResult = this.updateById(customer);
        if (updateResult) {

            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, customer, ModuleTypeEnum.CUSTOMER_B2C.getCode(), id, "", "");

            Class<CustomerB2cEntity> customerClass = CustomerB2cEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //修改附件
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);

            //批量修改联系人信息
            customerB2cContactService.updateBatchContact(id, dto.getContactList());

            //批量修改地址信息
            customerB2cAddressService.updateBatchAddress(id, dto.getAddressList());

            //批量修改发票信息
//            customerB2cInvoiceService.updateBatchInvoice(id, dto.getInvoiceList());

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
     * @date 2023-05-15 14:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(CustomerB2CDTO.UpdateDTO dto) {
        String id = this.updateCustomer(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(BaseApproveParamDTO dto,CustomerB2cEntity entity) {
        List<CustomerB2cEntity> list = Arrays.asList(entity);

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
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个客户信息", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.CUSTOMER_B2C.getCode(), pairList, "审核操作");

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
    public Boolean approveEnd(BaseApproveParamDTO dto, List<CustomerB2cEntity> list) {
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
        Boolean result = this.updateApproveStatus(list, approveStatus, user.getUserName());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
//        if (dto.getType().equals(ApproveType.PASS)) {
//            String sourceType=SourceTypeEnum.SHOP.getCode();
//            //审核通过发送金蝶
//            list=list.stream().filter(l->sourceType.equals(l.getSourceType())).collect(Collectors.toList());
//            list.forEach(obj -> syncKingdeeCustomerB2cService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
//            //批量保存销售员信息
//            customerB2cSellerService.batchSellerHistory(list);
//
//        }
        return Boolean.TRUE;
    }


    /**
     * 反审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:25
     */
    @Override
    public BatchResultDTO disApprove(CustomerB2cEntity entity) {
        List<CustomerB2cEntity> list = Arrays.asList(entity);

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveStatus);

        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        //反审核
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.CUSTOMER_B2C.getCode(), rejectPairList, "状态变更");
            //审核通过发送金蝶
//            list.forEach(obj -> syncKingdeeCustomerB2cService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    /**
     * 删除客户
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        List<CustomerB2cEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //占用状态
        long occupyCount = list.stream().filter(s -> s.getOccupyStatus()).count();
        if (occupyCount > 0) {
            throw new ServiceException(ApiError.ERROR_92018);
        }


        //删除客户
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除客户[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2C.getCode(), pairList, "删除");
        }
        return result;
    }


    /**
     * 导出 客户列表
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:53
     */
    @Override
    public Boolean exportExcel(CustomerB2CDTO.ExportDTO dto, HttpServletResponse response) {
        String searchType = dto.getSearchType();
        List<String> approveList = new ArrayList<>();
        //待审核
        if (OmsConstant.WAIT_APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        //已审核
        if (OmsConstant.APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE.getStatus());
        }

        //审核不通过
        if (OmsConstant.REJECT.equals(searchType)) {
            approveList.add(ApproveStatusEnum.REJECT.getStatus());
        }

        List<CustomerB2CDTO.PagingViewDTO> list = baseMapper.listExport(dto, approveList);
        for (CustomerB2CDTO.PagingViewDTO item : list) {
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "停用" : "启用";
            item.setDisabledName(disabledName);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/CustomerExport.xlsx";
        String name = "客户列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("客户列表导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;


    }

    @Override
    public List<CustomerB2CDTO.InfoDTO> listCustomer() {
        LambdaQueryWrapper<CustomerB2cEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerB2cEntity::getId,
                CustomerB2cEntity::getCode,
                CustomerB2cEntity::getName,
                CustomerB2cEntity::getDisabled);
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(approve);
        queryWrapper.eq(CustomerB2cEntity::getApproveStatus, approveStatusEnum);
        queryWrapper.orderByDesc(CustomerB2cEntity::getDisabled);
        List<CustomerB2cEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, CustomerB2CDTO.InfoDTO.class);
    }


    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<CustomerB2cEntity> customerList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(customerList)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }

        Boolean disabled = dto.getDisabled();
        long count = customerList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != customerList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        if (disabled) {
            //客户是否有使用
            Boolean isUseCustomer = soInfoService.getIsUseCustomer(ids);
            if (isUseCustomer) {
                throw new ServiceException(ApiError.ERROR_92044);
            }
        }
        customerList.forEach(d -> d.setDisabled(disabled));
        //添加日志
        List<Pair<String, String>> pairList = customerList.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getName())).collect(Collectors.toList());
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        String finalContent = "[%s]," + content;
        operateLogService.batchAddModuleOperateLog(finalContent, ModuleTypeEnum.CUSTOMER_B2C.getCode(), pairList, "状态变更");

//        customerList.forEach(req -> {
//            //发送金蝶
//            if (dto.getDisabled()) {
//                syncKingdeeCustomerB2cService.syncDataToKingdee(req, SyncOperateEnum.OPERATE_DISABLE.getCode());
//            } else {
//                syncKingdeeCustomerB2cService.syncDataToKingdee(req, SyncOperateEnum.OPERATE_ENABLE.getCode());
//            }
//        });

        return this.updateBatchById(customerList);


    }


    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<CustomerB2cEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_INFO.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("客户【%s】取消流程", ModuleTypeEnum.CUSTOMER_B2C.getCode(), pairList, "取消流程操作");
        return result;
    }


    /**
     * 获取启用的列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 16:05
     */
    @Override
    public List<CustomerB2CDTO.InfoDTO> listEnable(String permissionSql) {
        LambdaQueryWrapper<CustomerB2cEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerB2cEntity::getId,
                CustomerB2cEntity::getCode,
                CustomerB2cEntity::getName,
                CustomerB2cEntity::getApproveStatus,
                CustomerB2cEntity::getDisabled);
        queryWrapper.eq(CustomerB2cEntity::getDisabled, Boolean.FALSE);
        if (StringUtils.isNotBlank(permissionSql)) {
            queryWrapper.last(permissionSql);
        }
        List<CustomerB2cEntity> list = this.list(queryWrapper);
        List<CustomerB2CDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerB2CDTO.InfoDTO.class);
        List<ApproveStatusEnum> statusList = new ArrayList<>(1);
        statusList.add(ApproveStatusEnum.APPROVE);
        resultList = resultList.stream().sorted(Comparator.comparing(CustomerB2CDTO.InfoDTO::getDisabled)).collect(Collectors.toList());
        return resultList;
    }


    /**
     * 获取客户的默认联系人
     *
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerB2CDTO.BaseDTO
     * @author yl
     * @date 2023-05-15 16:15
     */
    @Override
    public CustomerB2CDTO.BaseDTO getBase(String customerId) {
        CustomerB2CDTO.BaseDTO base = new CustomerB2CDTO.BaseDTO();
        CustomerB2cEntity customer = this.getById(customerId);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        base.setId(customer.getId());
        base.setCode(customer.getCode());
        String currency = customer.getCurrency();
        //币别
        base.setCurrency(currency);
        base.setCountryId(customer.getCountryId());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(currency));

        // 国家
        DictCountryEntity dictCountryEntity = sysUserFeign.getCountryById(base.getCountryId());
        if (ObjectUtils.isNotEmpty(dictCountryEntity)) {
            base.setCountryName(dictCountryEntity.getNameCn());
        }
        base.setSellerId(customer.getSellerId());
        base.setSellerName(customer.getSellerName());
//        base.setUseOrgId(customer.getUseOrgId());
//        base.setUseOrgName(customer.getUseOrgName());
        String currencySymbol = "";
        if (CollectionUtils.isNotEmpty(currencyList)) {
            currencySymbol = currencyList.get(0).getSymbol();
        }
        base.setCurrencySymbol(currencySymbol);
        List<CustomerAddressDTO.ViewDTO> addressList = customerB2cAddressService.listByMainId(customerId);
        CustomerAddressDTO.ViewDTO address = addressList.stream().filter(c -> c.getIsDefault()).findFirst().orElse(null);
        if (address != null) {
            base.setAddress(address.getAddress());
            base.setAddressId(address.getId());
            base.setAddressType(address.getType());
            base.setPerson(address.getPerson());
            base.setTelNumber(address.getTelNumber());
        }
        String receiptConditionId =customer.getConditionDict();
        if (StrUtils.isNotEmpty(receiptConditionId)) {
            base.setReceiveCondition(receiptConditionId);
            KingdeeReceiptConditionEntity receiptCondition = kingdeeReceiptConditionService.getById(receiptConditionId);
            base.setReceiveConditionName(receiptCondition != null ? receiptCondition.getName() : "");
        }
        return base;
    }


    /**
     * 引用客户
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean quoteCustomer(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            this.lambdaUpdate().in(CustomerB2cEntity::getId, ids).
                    set(CustomerB2cEntity::getOccupyStatus, Boolean.TRUE).update();
        }
        return Boolean.TRUE;
    }

    private Boolean updateApproveStatus(List<CustomerB2cEntity> list, ApproveStatusEnum statusEnum, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (CustomerB2cEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return true;
    }


    /**
     * 检查名称
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-05-12 15:08
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<CustomerB2cEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(CustomerB2cEntity::getId, id);
        }
        queryWrapper.eq(CustomerB2cEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_1028);
        }
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    /**
     * 处理平台类型历史数据
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-28 15:53
     */
    @Override
    public Boolean processData() {
        List<CustomerB2cEntity> list = this.list();
        String type = DictBasicTypeEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        for (CustomerB2cEntity item : list) {
            String platformType = item.getPlatformType();
            String platformTypeName = PlatformDictEnum.getByCode(platformType).getName();
            String newPlatformType = dictList.stream().filter(d -> d.getName().equals(platformTypeName)).
                    findFirst().map(DictBasicDTO.ViewDTO::getValue).orElse("");
            item.setPlatformType(newPlatformType);
        }
        return this.updateBatchById(list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCustomer(MultipartFile file) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = wb.getSheetAt(1);
        // 读取数据集
        int rows = sheet.getPhysicalNumberOfRows();

        // 组织
        List<BaseIdDTO> accountingCompanyList = sysUserFeign.listAccountingCompany();
        Map<String, List<BaseIdDTO>> accountCompanyNameMap = accountingCompanyList.stream().collect(Collectors.groupingBy(BaseIdDTO::getName));
        // 客户分组
//        List<CustomerB2cGroupEntity> customerGroupEntityList = customerB2cGroupService.list();
//        Map<String, List<CustomerB2cGroupEntity>> customerGroupNameMap = customerGroupEntityList.stream().filter(r -> StrUtils.isNotEmpty(r.getName())).collect(Collectors.groupingBy(CustomerB2cGroupEntity::getName));
//        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String, List<DictCountryDTO.ListDTO>> countryNameMap = countryList.stream().collect(Collectors.groupingBy(DictCountryDTO.ListDTO::getNameCn));
        // 平台类型
        List<DictBasicDTO.ViewDTO> platFormList = dictBasicService.getByKey(DictBasicTypeEnum.PLATFORM.getType());
        Map<String, DictBasicDTO.ViewDTO> platformNameMap = platFormList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, Function.identity()));
        // 客户类别
        List<DictBasicDTO.ViewDTO> customerCategoryList = dictBasicService.getByKey("customerCompanyCategory");
        Map<String, DictBasicDTO.ViewDTO> customerCategoryNameMap = customerCategoryList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, Function.identity()));
        // 结算方式
        List<DictBasicDTO.ViewDTO> settleModeList = dictBasicService.getByKey("settleMode");
        Map<String, DictBasicDTO.ViewDTO> settleModeNameMap = settleModeList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getName, Function.identity()));
        // 币别
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        Map<String, DictCurrencyEntity> currencyNameMap = currencyList.stream().collect(Collectors.toMap(DictCurrencyEntity::getName, Function.identity()));

        // 部门
        List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
        Map<String, List<SysUserDeptDTO>> deptNameMap = userDeptList.stream().filter(r -> StrUtils.isNotEmpty(r.getDeptName())).collect(Collectors.groupingBy(SysUserDeptDTO::getDeptName));
        // 人员
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, List<FindUserDTO>> userNameMap = userList.stream().collect(Collectors.groupingBy(FindUserDTO::getUserName));

        wb.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        // 一个客户会存在多行数据
        Map<String, CustomerB2cEntity> customerBaseMap = Maps.newHashMap();
        for (int i = 2; i < rows; i++) {
            int noticeRow = i + 1;
            XSSFRow row = sheet.getRow(i);

            // 客户编码
            String code = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));

            LambdaQueryWrapper<CustomerB2cEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CustomerB2cEntity::getCode, code);
            queryWrapper.last("LIMIT 1");
            CustomerB2cEntity checkCustomerB2cEntity = this.baseMapper.selectOne(queryWrapper);
            if (Objects.nonNull(checkCustomerB2cEntity)) {
                log.info("已经存在客户编码【{}】，本次不导入", code);
                continue;
            }

            CustomerB2cEntity customerInfoEntity = new CustomerB2cEntity();

            // 基本信息
            customerInfoEntity.setCode(code);
            // 使用组织
//            String useOrgName = ExcelUtil.convertCellValueToString(row.getCell(2));
//            customerInfoEntity.setUseOrgName(useOrgName);
            // 使用组织id需根据名称获取
//            if (!accountCompanyNameMap.containsKey(useOrgName)) {
//                throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到组织【{}】", noticeRow, useOrgName));
//            }
            // 名称不会重复
//            if (Objects.nonNull(accountCompanyNameMap.get(useOrgName))) {
//                customerInfoEntity.setUseOrgId(accountCompanyNameMap.get(useOrgName).get(0).getId());
//            }

            // 客户分组（单独的表需提前维护customer_group），分组id需要根据名称获取
            String groupName = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(3)));
            // 客户分组id需根据客户分组名称获取
//            if (StrUtils.isNotEmpty(groupName) && customerGroupNameMap.containsKey(groupName)) {
//                customerInfoEntity.setGroupName(groupName);
//                customerInfoEntity.setGroupId(customerGroupNameMap.get(groupName).get(0).getId());
//            }

            // 国家
            String countryName = ExcelUtil.convertCellValueToString(row.getCell(4));
            if (!countryNameMap.containsKey(countryName)) {
                throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到国家【{}】", noticeRow, countryName));
            }
            // 国家id需根据国家名称获取
            customerInfoEntity.setCountryId("");
            if (countryNameMap.containsKey(countryName)) {
                customerInfoEntity.setCountryId(countryNameMap.get(countryName).get(0).getId());
            }
            String countryId = customerInfoEntity.getCountryId();
            List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(countryId));
            String areaId = globalAreaList.stream().filter(d -> d.getCountryId().equals(countryId)).findFirst().map(DictGlobalAreaDTO.InfoDTO::getId).orElse("");
            // 区域id需根据国家id获取
            customerInfoEntity.setAreaId(areaId);
            // 省份
            String provinceName = ExcelUtil.convertCellValueToString(row.getCell(6));
            // 省份id需要根据名称获取
            DictCityDTO.ListDTO provinceDTO = null;
//            if (StrUtils.isNotEmpty(provinceName)) {
//                List<DictCityDTO.ListDTO> provinceList = sysUserFeign.getProvincesByCountryCode(customerInfoEntity.getCountryId());
//                if (CollUtil.isNotEmpty(provinceList)) {
//                    provinceDTO = provinceList.stream().filter(r -> Objects.equals(r.getName(), provinceName)).findFirst().orElse(null);
//                    if (Objects.nonNull(provinceDTO)) {
//                        customerInfoEntity.setProvinceId(provinceDTO.getId());
//                    }
//                }
//            }

            // 城市
//            DictCityDTO.ListDTO cityDTO = null;
//            String cityName = ExcelUtil.convertCellValueToString(row.getCell(7));
//            if (StrUtils.isNotEmpty(cityName) && Objects.nonNull(provinceDTO)) {
//                List<DictCityDTO.ListDTO> cityList = provinceDTO.getChildrenList();
//                if (CollUtil.isNotEmpty(cityList)) {
//                    cityDTO = cityList.stream().filter(r -> Objects.equals(r.getName(), cityName)).findFirst().orElse(null);
//                }
//                if (Objects.nonNull(cityDTO)) {
//                    customerInfoEntity.setCityId(cityDTO.getId());
//                }
//            }

            // 客户名称
            String name = ExcelUtil.convertCellValueToString(row.getCell(8));
            customerInfoEntity.setName(name);
            // 客户简称
            String shortName = ExcelUtil.convertCellValueToString(row.getCell(9));
            customerInfoEntity.setShortName(shortName);
            // 平台类型
            String platformTypeName = ExcelUtil.convertCellValueToString(row.getCell(10));
            if (!platformNameMap.containsKey(platformTypeName)) {
                throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到平台类型【{}】", noticeRow, platformTypeName));
            }
            customerInfoEntity.setPlatformType(platformNameMap.get(platformTypeName).getValue());
            // 公司类别
            String companyCategoryName = ExcelUtil.convertCellValueToString(row.getCell(11));
            if (StrUtils.isNotEmpty(companyCategoryName) && customerCategoryNameMap.containsKey(companyCategoryName)) {
                customerInfoEntity.setCompanyCategoryDict(customerCategoryNameMap.get(companyCategoryName).getValue());
            }

            // 商务信息
            // 结算方（也是启用的客户）
            customerInfoEntity.setSettleCode("");
            // 付款方（也是启用的客户）
            customerInfoEntity.setPayCode("");
            // 结算方式
            String settleDictName = ExcelUtil.convertCellValueToString(row.getCell(14));
            if (StrUtils.isNotEmpty(settleDictName) && settleModeNameMap.containsKey(settleDictName)) {
                customerInfoEntity.setSettleDict(settleModeNameMap.get(settleDictName).getValue());
            }
            // 结算币别
            String currencyName = ExcelUtil.convertCellValueToString(row.getCell(15));
            if (StrUtils.isEmpty(currencyName) || !currencyNameMap.containsKey(currencyName)) {
                throw new ServiceException( CharSequenceUtil.format("第【{}】行币别为空或未找到结算币别【{}】", noticeRow, currencyName));
            }
            customerInfoEntity.setCurrency(currencyNameMap.get(currencyName).getId());
            // 收款条件
            String conditionDictName = ExcelUtil.convertCellValueToString(row.getCell(16));


            // 无附件

            // 备注
            String remark = ExcelUtil.convertCellValueToString(row.getCell(18));
            customerInfoEntity.setRemark(remark);
            // 审核状态
            customerInfoEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            customerInfoEntity.setApproveUserName("admin");
            customerInfoEntity.setCreateTime(LocalDateTime.now());
            customerInfoEntity.setUpdateTime(LocalDateTime.now());
            customerInfoEntity.setCreateUserId("");
            customerInfoEntity.setCreateUserName("admin");
            customerInfoEntity.setUpdateUserId("");
            customerInfoEntity.setUpdateUserName("admin");

            if (!customerBaseMap.containsKey(code)) {
                super.save(customerInfoEntity);
                customerBaseMap.put(code, customerInfoEntity);
            } else {
                customerInfoEntity = customerBaseMap.get(code);
            }

            // 客户联系人
            this.importCustomerContact(customerInfoEntity.getId(), row);

            // 客户地址
            this.importCustomerAddress(customerInfoEntity.getId(), row);

            // 客户发票
//            this.importCustomerInvoice(customerInfoEntity.getId(), row);

            // 客户销售员信息
//            this.importCustomerSale(customerInfoEntity.getId(), row, noticeRow, deptNameMap, userNameMap);

            // 需要拉取客户的金蝶id
        }

    }

//    @Override
//    public List<CustomerB2cEntity> listByKingdeeIdList(List<String> kingdeeCustomerIds) {
//        if (CollectionUtils.isEmpty(kingdeeCustomerIds)) {
//            return Collections.emptyList();
//        }
//        return this.lambdaQuery().in(CustomerB2cEntity::getSyncKingdeeId, kingdeeCustomerIds).list();
//    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public void importCustomerKingdee(MultipartFile file) throws IOException {
//        XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
//        XSSFSheet sheet = wb.getSheetAt(0);
//        // 读取数据集
//        int rows = sheet.getPhysicalNumberOfRows();
//
//        for (int i = 2; i < rows; i++) {
//            XSSFRow row = sheet.getRow(i);
//
//            // 客户名称
//            String customerName = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(6)));
//            // 金蝶id
//            String kingdeeId = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));
//            LambdaQueryWrapper<CustomerB2cEntity> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(CustomerB2cEntity::getName, customerName);
//            queryWrapper.last("LIMIT 1");
//            CustomerB2cEntity customerInfoEntity = super.getOne(queryWrapper);
//            if (Objects.isNull(customerInfoEntity)) {
//                log.info("未找到客户【{}】", customerName);
//                continue;
//            }
//            if (StrUtils.isNotEmpty(customerInfoEntity.getSyncKingdeeId())) {
//                log.info("客户【{}】已经存在金蝶id，不处理", customerName);
//                continue;
//            }
//            lambdaUpdate().set(CustomerB2cEntity::getSyncKingdeeId, kingdeeId).set(CustomerB2cEntity::getSyncKingdeeTime, LocalDateTime.now())
//                    .set(CustomerB2cEntity::getSyncOperate, SyncOperateEnum.OPERATE_APPROVE.getCode())
//                    .set(CustomerB2cEntity::getSyncKingdeeStatus, SyncStatusEnum.SUCCESS_SYNC.getCode())
//                    .eq(CustomerB2cEntity::getId, customerInfoEntity.getId())
//                    .update();
//        }
//
//    }

    @Override
    public List<CustomerB2cEntity> listByCountryIdList(List<String> countryIdList) {
        if (CollectionUtils.isEmpty(countryIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CustomerB2cEntity> list = lambdaQuery()
                .in(CustomerB2cEntity::getCountryId, countryIdList)
                .list();
        return list;
    }

    @Override
    public SoB2cDTO.ViewReceiveDataDTO viewReceiveData(String id) {
        //客户信息
        CustomerB2cEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_B2C_CUSTOMER_NOT_EXIST);
        }
        SoB2cDTO.ViewReceiveDataDTO viewReceiveDataDTO = new SoB2cDTO.ViewReceiveDataDTO();
        viewReceiveDataDTO.setCustomerId(id);
        viewReceiveDataDTO.setName(entity.getName());

//        if (StringUtils.isNotBlank(entity.getCityId())) {
//            DictCityEntity dictCityEntity = sysUserFeign.getCityById(entity.getCityId());
//            if (ObjectUtils.isNotEmpty(dictCityEntity)) {
//                viewReceiveDataDTO.setCityName(dictCityEntity.getName());
//            }
//        }
        viewReceiveDataDTO.setCountryId(entity.getCountryId());
        if (StringUtils.isNotBlank(entity.getCountryId())) {
            DictCountryEntity dictCountryEntity = sysUserFeign.getCountryById(entity.getCountryId());
            if (ObjectUtils.isNotEmpty(dictCountryEntity)) {
                viewReceiveDataDTO.setCountryName(dictCountryEntity.getNameCn());
            }
        }
        //客户地址
        List<CustomerAddressDTO.ViewDTO> addressList = customerB2cAddressService.listByMainId(id);
        if (CollectionUtils.isNotEmpty(addressList)) {
            CustomerAddressDTO.ViewDTO viewDTO = addressList.stream().filter(obj -> obj.getIsDefault() && !obj.getDisabled()).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(viewDTO)) {
                viewReceiveDataDTO.setReceiverName(viewDTO.getPerson());
                viewReceiveDataDTO.setReceiverTelNumber(viewDTO.getTelNumber());
                viewReceiveDataDTO.setFirstAddress(viewDTO.getAddress());
                viewReceiveDataDTO.setSecondAddress(viewDTO.getAddress());
                viewReceiveDataDTO.setZipCode(viewDTO.getZipCode());
            }
        }
        //联系人
        List<CustomerContactDTO.ViewDTO> contactList = customerB2cContactService.listByMainId(id);
        if (CollectionUtils.isNotEmpty(contactList)) {
            CustomerContactDTO.ViewDTO viewDTO = contactList.stream().filter(obj -> obj.getIsDefault() && !obj.getDisabled()).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(viewDTO)) {
                viewReceiveDataDTO.setEmail(viewDTO.getEmail());
                viewReceiveDataDTO.setTelNumber(viewDTO.getTelNumber());
            }
        }

        return viewReceiveDataDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerB2cEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity, String dictCountryCode, List<DictCountryEntity> countryList) {
        // 当前国家
        DictCountryEntity dictCountryEntity = countryList.stream().findFirst().orElse(null);
        CustomerB2cEntity entity = this.getBySourceId(mainEntity.getId());
        if (null == entity){
            CustomerB2cEntity customerB2cEntity = new CustomerB2cEntity();
            //生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUSTC);
            customerB2cEntity.setCode(code);
            customerB2cEntity.setSourceId(mainEntity.getId());
            customerB2cEntity.setSourceType(SourceTypeEnum.SO_B2C.getCode());
            customerB2cEntity.setName(dto.getReceiver().getName());
            customerB2cEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            customerB2cEntity.setPlatformType(dto.getDictPlatform());
            customerB2cEntity.setCountryId(dictCountryCode);
            customerB2cEntity.setConditionDict("onlineStorePayment");
            customerB2cEntity.setCurrency(dto.getCurrency());
            if (null != dictCountryEntity){
                customerB2cEntity.setAreaId(dictCountryEntity.getSubregionCode());
            }
            customerB2cEntity.setDisabled(false);
            if (!save(customerB2cEntity)){
                throw new ServiceException("[CustomerB2cEntity] 保存失败");
            }
            return customerB2cEntity;
        } else {
            if (StringUtils.isBlank(entity.getPlatformType())){
                entity.setPlatformType(dto.getDictPlatform());
            }
            if (StringUtils.isBlank(entity.getCode())){
                //生成单号
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUSTC);
                entity.setCode(code);
            }
            if (StringUtils.isBlank(entity.getCountryId())){
                entity.setCountryId(dictCountryCode);
            }
            if (StringUtils.isBlank(entity.getAreaId()) && null != dictCountryEntity){
                entity.setAreaId(dictCountryEntity.getSubregionCode());
            }
            if (StringUtils.isBlank(entity.getConditionDict())){
                entity.setConditionDict("onlineStorePayment");
            }
            if (StringUtils.isBlank(entity.getCurrency())){
                entity.setCurrency(dto.getCurrency());
            }
            if (StringUtils.isBlank(entity.getName())){
                entity.setName(dto.getReceiver().getName());
            }
            entity.setApproveStatus(ApproveStatusEnum.APPROVE);
            entity.setDisabled(false);
            updateById(entity);
//            if (!updateById(entity)){
//                throw new ServiceException("[CustomerB2cEntity] 更新失败");
//            }
            return entity;
        }
    }

    @Override
    public CustomerB2cEntity getBySourceId(String sourceId) {
        return  lambdaQuery().eq(CustomerB2cEntity::getSourceId, sourceId).last("LIMIT 1").one();
    }

    @Override
    public CustomerB2cEntity findByPlatformAndName(String dictPlatform, String name, String sourceType) {
        return lambdaQuery()
                .eq(CustomerB2cEntity::getPlatformType, dictPlatform)
                .eq(CustomerB2cEntity::getName, name)
                .eq(CustomerB2cEntity::getSourceType, sourceType)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public CustomerB2CDTO.DropPagingDTO<CustomerB2CDTO.DropListDTO> customerDropDown(PagingDTO<CustomerB2CDTO.DropSearchDTO> pagingDTO) {
        Page<CustomerB2CDTO.DropListDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CustomerB2CDTO.DropListDTO> pageData=  baseMapper.customerDropDown(query, pagingDTO.getParams());
        buildCustomerDTO(pageData.getRecords());
        CustomerB2CDTO.DropPagingDTO<CustomerB2CDTO.DropListDTO> result = new CustomerB2CDTO.DropPagingDTO<>(pageData);
        if (CollectionUtils.isNotEmpty(pageData.getRecords()))  {
            long count = pageData.getRecords().stream().filter(obj -> CharSequenceUtil.equals(obj.getCustomerName(), pagingDTO.getParams().getCustomerName())).count();
            if (count > 0) {
                result.setIsExist(Boolean.TRUE);
            }
        }
        return result;
    }

    @Override
    public CustomerB2cEntity getByIdOrName(String keyWord) {
        CustomerB2cEntity customerB2cEntity = this.getById(keyWord);
        if (ObjectUtil.isNotEmpty(customerB2cEntity)) {
            return customerB2cEntity;
        }
        CustomerB2cEntity b2cEntity = this.getByName(keyWord);
        return b2cEntity;
    }
    /**
     * 远程搜索
     */
    @Override
    public PagingVO<CustomerB2CDTO.InfoDTO> pagingSelect(PagingDTO<CustomerB2CDTO.SelectDTO> searchDTO) {
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        CustomerB2CDTO.SelectDTO params = searchDTO.getParams();
        IPage<CustomerB2CDTO.InfoDTO> pagResult = baseMapper.pagingSelect(query, params);
        return new PagingVO<>(pagResult);
    }
    /**
     * @description: 根据名称查询
     * @author Will
     * @date: 2024/5/28 9:28
     * @param name
     * @return CustomerB2cEntity
     */
    private CustomerB2cEntity getByName (String name) {
        CustomerB2cEntity customerB2cEntity = lambdaQuery().eq(CustomerB2cEntity::getName, name)
                .last("limit 1")
                .one();
        return customerB2cEntity;
    }

    /**
     * 赋值
     * @param records
     */
    private void buildCustomerDTO(List<CustomerB2CDTO.DropListDTO> records) {
        if(CollectionUtils.isEmpty(records)){
            return;
        }
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        records.forEach(dropListDTO -> {
            if (StringUtils.isNotEmpty(dropListDTO.getCountryId()) && CollectionUtils.isNotEmpty(countryList)){
                DictCountryDTO.ListDTO listDTO = countryList.stream().filter(e -> dropListDTO.getCountryId().equals(e.getId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(listDTO)){
                    dropListDTO.setCountryName(listDTO.getNameCn());
                }
            }
        });

    }

    /**
     * 客户联系人信息
     *
     * @param customerId
     * @param row
     */
    public void importCustomerContact(String customerId, XSSFRow row) {
        // 联系人信息
        // 联系人名称
        String person = ExcelUtil.convertCellValueToString(row.getCell(19));
        if (StrUtils.isEmpty(person)) {
            return;
        }

        CustomerB2cContactEntity customerB2cContactEntity = new CustomerB2cContactEntity();
        customerB2cContactEntity.setMainId(customerId);


        customerB2cContactEntity.setPerson(person);
        // 职务
        String position = ExcelUtil.convertCellValueToString(row.getCell(20));
        customerB2cContactEntity.setPosition(position);
        // 电话
        String telNumber = ExcelUtil.convertCellValueToString(row.getCell(21));
        customerB2cContactEntity.setTelNumber(telNumber);
        // 邮箱
        String email = ExcelUtil.convertCellValueToString(row.getCell(22));
        customerB2cContactEntity.setEmail(email);
        // 默认联系人
        String isDefaultName = ExcelUtil.convertCellValueToString(row.getCell(23));
        customerB2cContactEntity.setIsDefault(Boolean.FALSE);
        if (Objects.equals(isDefaultName, "是") || Objects.equals(isDefaultName, "默认")) {
            customerB2cContactEntity.setIsDefault(Boolean.TRUE);
        }

        // 是否启用
        String isDisableName = ExcelUtil.convertCellValueToString(row.getCell(24));
        customerB2cContactEntity.setDisabled(Boolean.FALSE);
        if (Objects.equals(isDisableName, "是") || Objects.equals(isDisableName, "禁用")) {
            customerB2cContactEntity.setDisabled(Boolean.TRUE);
        }

        // 备注
        String contactRemark = ExcelUtil.convertCellValueToString(row.getCell(25));
        customerB2cContactEntity.setRemark(contactRemark);

        customerB2cContactService.save(customerB2cContactEntity);
    }

    /**
     * 客户地址信息
     *
     * @param customerId
     * @param row
     */
    public void importCustomerAddress(String customerId, XSSFRow row) {
        // 地址信息
        // 详细地址
        String address = ExcelUtil.convertCellValueToString(row.getCell(26));
        // 联系人
        String addressPerson = ExcelUtil.convertCellValueToString(row.getCell(27));
        if (StrUtils.isEmpty(address) && StrUtils.isEmpty(addressPerson)) {
            return;
        }

        CustomerB2cAddressEntity customerB2cAddressEntity = new CustomerB2cAddressEntity();
        customerB2cAddressEntity.setMainId(customerId);

        customerB2cAddressEntity.setAddress(address);

        customerB2cAddressEntity.setPerson(addressPerson);
        // 地址类型
        String addressTypeName = ExcelUtil.convertCellValueToString(row.getCell(28));
        if (StrUtils.isNotEmpty(addressTypeName)) {
            customerB2cAddressEntity.setType(AddressTypeEnum.getCodeByName(addressTypeName));
        }
        // 电话
        String addressTelNumber = ExcelUtil.convertCellValueToString(row.getCell(29));
        customerB2cAddressEntity.setTelNumber(addressTelNumber);
        // 邮箱
        String addressEmail = ExcelUtil.convertCellValueToString(row.getCell(30));
        customerB2cAddressEntity.setEmail(addressEmail);
        // 默认地址
        String isDefaultAddress = ExcelUtil.convertCellValueToString(row.getCell(31));
        customerB2cAddressEntity.setIsDefault(Boolean.FALSE);
        if (Objects.equals(isDefaultAddress, "是") || Objects.equals(isDefaultAddress, "默认")) {
            customerB2cAddressEntity.setIsDefault(Boolean.TRUE);
        }
        // 是否启用
        String isDisableAddress = ExcelUtil.convertCellValueToString(row.getCell(32));
        customerB2cAddressEntity.setDisabled(Boolean.FALSE);
        if (Objects.equals(isDisableAddress, "是") || Objects.equals(isDisableAddress, "禁用")) {
            customerB2cAddressEntity.setDisabled(Boolean.TRUE);
        }

        // 备注
        String addressRemark = ExcelUtil.convertCellValueToString(row.getCell(33));
        customerB2cAddressEntity.setRemark(addressRemark);
        customerB2cAddressEntity.setCreateTime(LocalDateTime.now());
        customerB2cAddressEntity.setUpdateTime(LocalDateTime.now());
        customerB2cAddressEntity.setCreateUserId("");
        customerB2cAddressEntity.setCreateUserName("");
        customerB2cAddressEntity.setUpdateUserId("");
        customerB2cAddressEntity.setUpdateUserName("");
        customerB2cAddressService.save(customerB2cAddressEntity);

    }

    /**
     * 客户发票信息
     *
     * @param mainId
     * @param row
     */
//    public void importCustomerInvoice(String mainId, XSSFRow row) {
//        // 发票信息
//        // 发票抬头
//        String invoiceHead = ExcelUtil.convertCellValueToString(row.getCell(34));
//        if (StrUtils.isEmpty(invoiceHead)) {
//            return;
//        }
//        CustomerB2cInvoiceEntity customerB2cInvoiceEntity = new CustomerB2cInvoiceEntity();
//        customerB2cInvoiceEntity.setMainId(mainId);
//        customerB2cInvoiceEntity.setHead(invoiceHead);
//        // 发票类型
//        String invoiceTypeName = ExcelUtil.convertCellValueToString(row.getCell(35));
//        customerB2cInvoiceEntity.setType(invoiceTypeName);
//        // 开户银行
//        String bankName = ExcelUtil.convertCellValueToString(row.getCell(36));
//        customerB2cInvoiceEntity.setBankName(bankName);
//        // 银行账号
//        String bankAccount = ExcelUtil.convertCellValueToString(row.getCell(37));
//        customerB2cInvoiceEntity.setBankAccount(bankAccount);
//        // 是否默认银行
//        String isDefaultBank = ExcelUtil.convertCellValueToString(row.getCell(38));
//        customerB2cInvoiceEntity.setIsDefault(Boolean.FALSE);
//        if (Objects.equals(isDefaultBank, "是") || Objects.equals(isDefaultBank, "默认")) {
//            customerB2cInvoiceEntity.setIsDefault(Boolean.TRUE);
//        }
//
//        // 备注
//        String invoiceRemark = ExcelUtil.convertCellValueToString(row.getCell(39));
//        customerB2cInvoiceEntity.setRemark(invoiceRemark);
//        customerB2cInvoiceEntity.setCreateTime(LocalDateTime.now());
//        customerB2cInvoiceEntity.setUpdateTime(LocalDateTime.now());
//        customerB2cInvoiceEntity.setCreateUserId("");
//        customerB2cInvoiceEntity.setCreateUserName("");
//        customerB2cInvoiceEntity.setUpdateUserId("");
//        customerB2cInvoiceEntity.setUpdateUserName("");
//        customerB2cInvoiceService.save(customerB2cInvoiceEntity);
//    }

    /**
     * 新增客户销售员信息
     *
     * @param mainId
     * @param row
     * @param noticeRow
     * @param deptNameMap
     * @param userNameMap
     */
//    public void importCustomerSale(String mainId, XSSFRow row,
//                                   Integer noticeRow,
//                                   Map<String, List<SysUserDeptDTO>> deptNameMap,
//                                   Map<String, List<FindUserDTO>> userNameMap) {
//        // 销售部门
//        String deptName = ExcelUtil.convertCellValueToString(row.getCell(40));
//        if (StrUtils.isEmpty(deptName)) {
//            return;
//        }
//        if (StrUtils.isNotEmpty(deptName) && !deptNameMap.containsKey(deptName)) {
//            throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到销售部门【{}】", noticeRow, deptName));
//        }
//        // 销售员信息
//        CustomerB2cSellerEntity customerB2cSellerEntity = new CustomerB2cSellerEntity();
//        customerB2cSellerEntity.setMainId(mainId);
//        customerB2cSellerEntity.setDeptId("");
//        if (Objects.nonNull(deptNameMap.get(deptName))) {
//            customerB2cSellerEntity.setDeptId(deptNameMap.get(deptName).get(0).getDeptId());
//        }
//        // 销售员
//        String sellerName = ExcelUtil.convertCellValueToString(row.getCell(41));
//        if (StrUtils.isNotEmpty(sellerName) && !userNameMap.containsKey(sellerName)) {
//            throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到销售员【{}】", noticeRow, sellerName));
//        }
//        customerB2cSellerEntity.setSellerName(sellerName);
//        // 需转换成销售员id
//        customerB2cSellerEntity.setSellerId("");
//        if (userNameMap.containsKey(sellerName)) {
//            customerB2cSellerEntity.setSellerId(userNameMap.get(sellerName).get(0).getUserId());
//        }
//        // 开始日期
//        String startDateStr = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(42)));
//        if (StrUtils.isNotEmpty(startDateStr) && startDateStr.length() == 10) {
//            if (startDateStr.contains("-")) {
//                LocalDate startDate = LocalDate.parse(startDateStr);
//                customerB2cSellerEntity.setStartDate(startDate);
//            } else if (startDateStr.contains("/")) {
//                LocalDate endDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//                customerB2cSellerEntity.setStartDate(endDate);
//            }
//        }
//        // 结束日期
//        String endDateStr = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(43)));
//        if (StrUtils.isNotEmpty(endDateStr) && endDateStr.length() == 10) {
//            if (endDateStr.contains("-")) {
//                LocalDate endDate = LocalDate.parse(endDateStr);
//                customerB2cSellerEntity.setEndDate(endDate);
//            } else if (endDateStr.contains("/")) {
//                LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//                customerB2cSellerEntity.setEndDate(endDate);
//            }
//        }
//        // 备注
//        String sellerRemark = ExcelUtil.convertCellValueToString(row.getCell(44));
//        customerB2cSellerEntity.setRemark(sellerRemark);
//        customerB2cSellerEntity.setCreateTime(LocalDateTime.now());
//        customerB2cSellerEntity.setUpdateTime(LocalDateTime.now());
//        customerB2cSellerEntity.setCreateUserId("");
//        customerB2cSellerEntity.setCreateUserName("");
//        customerB2cSellerEntity.setUpdateUserId("");
//        customerB2cSellerEntity.setUpdateUserName("");
//        customerB2cSellerService.save(customerB2cSellerEntity);
//
//    }

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<CustomerB2cEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_B2C.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @param list
     * @param dto
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     */
    private void approveProcess(List<CustomerB2cEntity> list, BaseApproveParamDTO dto) {
        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_B2C.getCode());
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
            List<CustomerB2cEntity> updateList = list.stream().filter(obj -> updateIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, updateList);
        }
    }
}

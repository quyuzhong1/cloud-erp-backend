package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.CustomerDTO.CustomerBatchUpdateDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.AddressTypeEnum;
import com.erp.model.oms.enums.CustomerAddressTypeEnum;
import com.erp.model.oms.enums.CustomerInfoBusinessModeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.vo.CustomerInfoVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.*;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.dht.DhtService;
import com.erp.server.oms.dht.SyncDhtService;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerService;
import com.erp.server.oms.mapper.CustomerInfoMapper;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER;

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
public class CustomerInfoServiceImpl extends SuperServiceImpl<CustomerInfoMapper, CustomerInfoEntity> implements CustomerInfoService {

    @Resource
    private CommonService commonService;

    @Resource
    private SyncDhtService syncDhtService;
    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private CustomerInvoiceService customerInvoiceService;

    @Resource
    private CustomerSellerService customerSellerService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private DhtService dhtService;

    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private UserInfoFeign userInfoFeign;

    @Resource
    private SyncKingdeeCustomerService syncKingdeeCustomerService;


    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private SysPartitionFeign sysPartitionFeign;

    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;
    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 获取到分组的id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:10
     */
    @Override
    public List<String> listGroup() {
        List<CustomerInfoEntity> list = this.list();
        return list.stream().map(CustomerInfoEntity::getGroupId).distinct().collect(Collectors.toList());
    }


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
    public String add(CustomerDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //客户联系人
        List<CustomerContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        customerContactService.checkIsDefault(contactList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.AddDTO> addressList = dto.getAddressList();
        customerAddressService.checkIsDefault(addressList);

        //检查默认发票 银行账号
        List<InvoiceDTO.AddDTO> invoiceList = dto.getInvoiceList();
        customerInvoiceService.checkIsDefault(invoiceList);


        //id
        String id = IdWorker.getIdStr();
        CustomerInfoEntity addEntity = new CustomerInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //国家id
        String countryId = dto.getCountryId();
        List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(countryId));
        String areaId = globalAreaList.stream().filter(d -> d.getCountryId().equals(countryId)).findFirst().map(DictGlobalAreaDTO.InfoDTO::getId).orElse("");
        addEntity.setAreaId(areaId);
        //分组id
        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        addEntity.setPayCode(payCode);
        //获取客户分组信息
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setGroupName(groupName);
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CUST, BusinessNoTypeEnum.CODE_CUST.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUST);
        addEntity.setCode(code);
        //销售员
        String sellerId = dto.getSellerId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(sellerId);
        addEntity.setSellerName(findUserDTO.getUserName());
        //对应组织
        String innerOrgId = dto.getInnerOrgId();

        //使用组织
        String useOrgId = dto.getUseOrgId();
        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setInnerOrgName(innerOrgName);

        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setUseOrgName(useOrgName);
        //保存客户
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            Class<CustomerInfoEntity> customerClass = CustomerInfoEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            omsAttachmentService.batchSaveOrUpdate(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);
            //添加日志
            String content = String.format("新增了一个{%s}-客户-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), id, "新增操作");

            //批量保存地址信息
            customerAddressService.saveBatchAddress(id, addressList);

            //批量保存联系人信息
            customerContactService.saveBatchContact(id, contactList);

            //批量保存发票信息
            customerInvoiceService.saveBatchInvoice(id, invoiceList);


            return id;

        }

        return "";
    }
  /**
     * 添加客户信息
     *
     * @param dto
     * @return java.lang.String
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerInfoEntity addOrGetCustom(CustomerDTO.AddDTO dto) {
        //检查名称
        CustomerInfoEntity customerInfoEntity = getByName(null, dto.getName());
        if (Objects.nonNull(customerInfoEntity)){
            return customerInfoEntity;
        }

        //客户联系人
        List<CustomerContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        customerContactService.checkIsDefault(contactList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.AddDTO> addressList = dto.getAddressList();
        customerAddressService.checkIsDefault(addressList);

        //检查默认发票 银行账号
        List<InvoiceDTO.AddDTO> invoiceList = dto.getInvoiceList();
        customerInvoiceService.checkIsDefault(invoiceList);


        //id
        String id = IdWorker.getIdStr();
        CustomerInfoEntity addEntity = new CustomerInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //国家id
        String countryId = dto.getCountryId();
        List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(countryId));
        String areaId = globalAreaList.stream().filter(d -> d.getCountryId().equals(countryId)).findFirst().map(DictGlobalAreaDTO.InfoDTO::getId).orElse("");
        addEntity.setAreaId(areaId);
        //分组id
        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        addEntity.setPayCode(payCode);
        //获取客户分组信息
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setGroupName(groupName);
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CUST, BusinessNoTypeEnum.CODE_CUST.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUST);
        addEntity.setCode(code);
        //销售员
        String sellerId = dto.getSellerId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(sellerId);
        addEntity.setSellerName(findUserDTO.getUserName());
        //对应组织
        String innerOrgId = dto.getInnerOrgId();

        //使用组织
        String useOrgId = dto.getUseOrgId();
        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setInnerOrgName(innerOrgName);

        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setUseOrgName(useOrgName);
        //保存客户
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            Class<CustomerInfoEntity> customerClass = CustomerInfoEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            omsAttachmentService.batchSaveOrUpdate(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);
            //添加日志
            String content = String.format("新增了一个{%s}-客户-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), id, "新增操作");

            //批量保存地址信息
            customerAddressService.saveBatchAddress(id, addressList);

            //批量保存联系人信息
            customerContactService.saveBatchContact(id, contactList);

            //批量保存发票信息
            customerInvoiceService.saveBatchInvoice(id, invoiceList);


            return addEntity;

        }

        return null;
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
        List<CustomerInfoEntity> list = this.listByIds(ids);
        
        List<CustomerInfoEntity> hasPartitionList = list.stream().filter(l -> StringUtils.isNotBlank(l.getPartitionId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(hasPartitionList)) {
        	Map<String, Map<String, Object>> cacheMap = new HashMap<>();
            Map<String, String> dictPartitionIdCodeMap = FeignQuery.getByIds(DictPartitionEntity.class, 
            		hasPartitionList.stream().map(CustomerInfoEntity::getPartitionId).filter(Objects::nonNull).collect(Collectors.toList()))
            		  .stream().collect(Collectors.toMap(DictPartitionEntity::getId, DictPartitionEntity::getCode));
            String errorDepartmentCodeJoin = hasPartitionList.stream().filter(l -> queryAndCacheOmsDictBasic(cacheMap, dictPartitionIdCodeMap.get(l.getPartitionId()), l.getPlatformType()) == null)
            		.map(CustomerInfoEntity::getCode).collect(Collectors.joining("、"));
            if(StringUtils.isNotBlank(errorDepartmentCodeJoin)) {
            	throw new ServiceException(errorDepartmentCodeJoin + "军区未关联部门，请联系实施配置");
            }
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
        // 启动流程
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), "");
        if (result) {
            //添加日志
            String content = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }
    
    private SysDepartmentEntity queryAndCacheOmsDictBasic(Map<String, Map<String, Object>> cacheMap, String partitionCode, String dictPlatform) {
        if (StringUtils.isBlank(partitionCode) || StringUtils.isBlank(dictPlatform)){
            return null;
        }
        Map<String, Object> dictBasicMap = cacheMap.getOrDefault("omsDictBasic", new HashMap<>());
        List<DictBasicEntity> sdyPartitionDeptList = new ArrayList<>();
        List<DictBasicEntity> sdyPlatformDeptList = new ArrayList<>();
        List<SysDepartmentEntity> deptList = new LinkedList<>();

        Object level1ListObj = dictBasicMap.get(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType());
        Object level2ListObj = dictBasicMap.get(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType());
        Object deptListObj = dictBasicMap.get("deptList");
        if (null == level2ListObj || null == level1ListObj || null == deptListObj) {
            List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                    .in(DictBasicEntity::getType, Arrays.asList(
                            DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                            DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                    ))
                    .list();
            if (CollectionUtils.isNotEmpty(dictBasicEntityList)) {
                Map<String, List<DictBasicEntity>> groupMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
                sdyPartitionDeptList = groupMap.get(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType());
                sdyPlatformDeptList = groupMap.get(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType());
                dictBasicMap.putAll(groupMap);
            }
            // 部门信息
            deptList = sysUserFeign.getDeptEntityList();
            if (CollectionUtils.isNotEmpty(deptList)){
                dictBasicMap.put("deptList", deptList);
            }
            cacheMap.put("omsDictBasic", dictBasicMap);
        } else {
            sdyPartitionDeptList = (List<DictBasicEntity>) level1ListObj;
            sdyPlatformDeptList = (List<DictBasicEntity>) level2ListObj;
            deptList = (List<SysDepartmentEntity>) deptListObj;
        }

        // 军区一级部门映射
        DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(partitionCode)).findFirst().orElse(null);
        // 销售平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(dictPlatform)).collect(Collectors.toList());
        if (null != sdyPartitionDeptEntity && !CollectionUtils.isEmpty(sdyPlatformDeptEntityList)) {
            List<String> deptLevel2Ids = sdyPlatformDeptEntityList.stream().map(DictBasicEntity::getValue).distinct().collect(Collectors.toList());
            return deptList.stream().filter(e -> e.getPath().contains(sdyPartitionDeptEntity.getValue())
                                    && deptLevel2Ids.contains(e.getId())
                    )
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 获取tab list
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.TabListDTO>
     * @author yl
     * @date 2023-05-12 17:01
     */
    @Override
    public List<CustomerDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<CustomerDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<CustomerDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(CustomerDTO.ApproveCountDTO::getCount).sum();
        CustomerDTO.TabListDTO all = new CustomerDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);
        //待审核
        CustomerDTO.TabListDTO waitApprove = new CustomerDTO.TabListDTO();
        //需要审核的业务ids
        List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.CUSTOMER_INFO.getCode());
        int waitApproveCount = 0;
        if(CollectionUtils.isNotEmpty(businessIds)){
            List<CustomerInfoEntity> customerInfoEntities = this.listByIds(businessIds);
            customerInfoEntities = customerInfoEntities.stream().filter(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)).collect(Collectors.toList());
            waitApproveCount = customerInfoEntities.size();
        }
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        CustomerDTO.TabListDTO approve = new CustomerDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        CustomerDTO.TabListDTO reject = new CustomerDTO.TabListDTO();
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
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-12 17:21
     */
    @Override
    public PagingVO<CustomerDTO.PagingViewDTO> paging(PagingDTO<CustomerDTO.PagingParamDTO> dto) {
        CustomerDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<CustomerDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> groupIdList = list.stream().map(CustomerDTO.PagingViewDTO::getGroupId).collect(Collectors.toList());
        List<CustomerGroupEntity> groupList = CollectionUtils.isNotEmpty(groupIdList) ? customerGroupService.listByIds(groupIdList) : Collections.emptyList();
        List<String> ids = list.stream().map(CustomerDTO.PagingViewDTO::getId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_INFO.getCode(), obj));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);

        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String,String> countryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(countryList)){
            countryMap = countryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        }
        //销售部门id
        List<String> salesDeptIdList = list.stream().map(CustomerDTO.PagingViewDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(salesDeptIdList);
        for (CustomerDTO.PagingViewDTO item : list) {
            String deptName = departmentList.stream().filter(d -> d.getId().equals(item.getSalesDeptId())).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(deptName);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String groupId = item.getGroupId();
            String groupName = groupList.stream().filter(g -> g.getId().equals(groupId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGroupName(groupName);
            //最新审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,item.getApproveUserName()));
            }
            //平台类型名称
            String platformTypeName = dictList.stream().filter(obj -> obj.getValue().equals(item.getPlatformType())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPlatformTypeName(platformTypeName);
            //国家
            item.setCountryName(countryMap.get(item.getCountryId()));
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
    public String addAndSubmit(CustomerDTO.AddDTO dto) {
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
     * @return com.erp.model.oms.dto.CustomerDTO.ViewDTO
     * @author yl
     * @date 2023-05-15 9:24
     */
    @Override
    public CustomerDTO.ViewDTO view(String id) {
        CustomerDTO.ViewDTO view = new CustomerDTO.ViewDTO();
        CustomerInfoEntity customer = this.getById(id);
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
        String partitionId = customer.getPartitionId();
		if(StringUtils.isNotBlank(partitionId)){
            DictPartitionEntity dictPartitionEntity = FeignQuery.getById(DictPartitionEntity.class, partitionId);
            if(dictPartitionEntity != null){
                view.setPartitionName(dictPartitionEntity.getName());
                view.setPartitionCode(dictPartitionEntity.getCode());
            }
        }
        view.setAreaName(areaName);
        view.setSubregionName(subregionName);
        if(CharSequenceUtil.isNotBlank(view.getSellerId())){
            SysUserInfoEntity user = userInfoFeign.info(view.getSellerId());
            view.setSellerName(Objects.nonNull(user) ? user.getRealName() : CharSequenceUtil.EMPTY);
        }
        if (CharSequenceUtil.isNotBlank(customer.getSalesDeptId())){
            List<SysDepartmentEntity> departmentEntityList = sysUserFeign.getDeptByIds(Collections.singletonList(customer.getSalesDeptId()));
            view.setSalesDeptName(CollUtil.isNotEmpty(departmentEntityList) ? departmentEntityList.get(0).getName() : CharSequenceUtil.EMPTY);
        }
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
        List<CustomerContactDTO.ViewDTO> contactList = customerContactService.listByMainId(id);
        view.setContactList(contactList);


        //地址信息
        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(id);
        view.setAddressList(addressList);

        //发票信息
        List<InvoiceDTO.ViewDTO> invoiceList = customerInvoiceService.listByMainId(id);
        view.setInvoiceList(invoiceList);

        //销售员信息
        List<SellerDTO.ViewDTO> sellerList = customerSellerService.listByMainId(id);
        view.setSellerList(sellerList);

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
    public String updateCustomer(CustomerDTO.UpdateDTO dto) {
        String id = dto.getId();
        CustomerInfoEntity customer = this.getById(id);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }

        //检查名称
        checkName(id, dto.getName());
        //客户联系人
        List<CustomerContactDTO.ViewDTO> contactList = dto.getContactList();
        List<CustomerContactDTO.AddDTO> contactAddList = BeanMapper.copyList(contactList, CustomerContactDTO.AddDTO.class);
        //检查联系人默认是否多个
        customerContactService.checkIsDefault(contactAddList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.ViewDTO> addressList = dto.getAddressList();
        List<CustomerAddressDTO.AddDTO> addressAddList = BeanMapper.copyList(addressList, CustomerAddressDTO.AddDTO.class);
        customerAddressService.checkIsDefault(addressAddList);

        //检查默认发票 银行账号
        List<InvoiceDTO.ViewDTO> invoiceList = dto.getInvoiceList();
        List<InvoiceDTO.AddDTO> invoiceAddList = BeanMapper.copyList(invoiceList, InvoiceDTO.AddDTO.class);
        customerInvoiceService.checkIsDefault(invoiceAddList);

        String code = customer.getCode();

        if(!customer.getCountryId().equals(dto.getCountryId()) &&
                (customer.getPlatformType().equals(PlatformDictEnum.AMAZON.getCode()) ||customer.getPlatformType().equals(PlatformDictEnum.SHOPEE.getCode()) )){
            throw new ServiceException("B2B客户平台归属为shopee和亚马逊时，国家字段不允许修改");
        }
        //旧的
        CustomerInfoEntity old = new CustomerInfoEntity();

        BeanMapper.copy(customer, old);
        //旧销售员
        String oldSellerId = old.getSellerId();
        BeanMapper.copy(dto, customer);

        //分组id
        String groupId = dto.getGroupId();
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
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setGroupName(groupName);

        //销售员
        String sellerId = dto.getSellerId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(sellerId);
        customer.setSellerName(findUserDTO.getUserName());

        //对应组织
        String innerOrgId = dto.getInnerOrgId();

        //使用组织
        String useOrgId = dto.getUseOrgId();
        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setInnerOrgName(innerOrgName);

        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setUseOrgName(useOrgName);
        Boolean updateResult = this.updateById(customer);
        
        shopInfoService.lambdaUpdate()
	        .eq(ShopInfoEntity::getCustomerId, id)
//	        .set(ShopInfoEntity::getDictCountryCode, customer.getCountryId())
	        .set(ShopInfoEntity::getDictAreaCode, FeignQuery.getById(DictCountryEntity.class, customer.getCountryId()).getRegionCode())
	        .set(ShopInfoEntity::getSettlementCurrency, customer.getCurrency())
	        .set(ShopInfoEntity::getTradeCurrency, customer.getTradeCurrency())
	        .set(ShopInfoEntity::getSalesOrgId, customer.getUseOrgId())
	        .set(ShopInfoEntity::getSalesOrgName, customer.getUseOrgName())
	        .set(ShopInfoEntity::getChargeId, customer.getSellerId())
	        .set(ShopInfoEntity::getChargeName, customer.getSellerName())
	        .update();
        
        if (updateResult) {

            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, customer, ModuleTypeEnum.CUSTOMER.getCode(), id, "", "");

            Class<CustomerInfoEntity> customerClass = CustomerInfoEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //修改附件
            omsAttachmentService.batchSaveOrUpdate(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);

            //批量修改联系人信息
            List<DmpPushTaskEntity> dmpPushTaskList= customerContactService.updateBatchContact(id, dto.getContactList());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(dmpPushTaskList);
                }
            });

            //批量修改地址信息
            customerAddressService.updateBatchAddress(id, dto.getAddressList());

            //批量修改发票信息
            customerInvoiceService.updateBatchInvoice(id, dto.getInvoiceList());

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
    public Boolean updateAndSubmit(CustomerDTO.UpdateDTO dto) {
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
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(BaseApproveParamDTO dto, CustomerInfoEntity entity) {
        List<CustomerInfoEntity> list = Arrays.asList(entity);

        if(!ApproveStatusEnum.APPROVE_ING.equals(entity.getApproveStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        approveProcess(list, dto);

        //添加日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个客户信息", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.CUSTOMER.getCode(), pairList, "审核操作");
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<CustomerInfoEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        LoginUser user = UserContext.getDefaultLoginUser();
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result = this.updateApproveStatus(list, approveStatus, user.getUserName());
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (dto.getType().equals(ApproveType.PASS)) {
            //批量保存销售员信息
            customerSellerService.batchSellerHistory(list, LocalDate.now());
            //发送金蝶
            sendPushTask(list,SyncOperateEnum.OPERATE_APPROVE.getCode());
            //发送订货通
            sendDhtPushTask(list, SyncOperateEnum.OPERATE_APPROVE.getCode());
            List<String> countryIdList = list.stream().map(CustomerInfoEntity::getCountryId).collect(Collectors.toList());
            List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
            list.forEach(customer->{
                DictCountryEntity dictCountryEntity = countryList.stream().filter(d -> d.getId().equals(customer.getCountryId())).findFirst().orElse(null);
                if(Objects.nonNull(dictCountryEntity)){
                    shopInfoService.lambdaUpdate()
                            .eq(ShopInfoEntity::getCustomerId, customer.getId())
                            .set(ShopInfoEntity::getDictCountryCode, customer.getCountryId())
                            .set(ShopInfoEntity::getCountryName, dictCountryEntity.getNameCn())
                            .update();
                }

            });

        }

        return Boolean.TRUE;
    }

    private void sendDhtPushTask(List<CustomerInfoEntity> list, String code) {
        //查询地址
        List<String> ids = list.stream().map(CustomerInfoEntity::getId).collect(Collectors.toList());
        List<CustomerAddressEntity> customerAddressEntities = customerAddressService.listAllByMainIds(ids);
        for (CustomerInfoEntity customerInfo : list) {
            syncDhtService.createSyncCustomerTaskToDht(customerInfo,code);
        }
        for (CustomerAddressEntity customerAddressEntity : customerAddressEntities) {
            String newOperate = customerAddressEntity.getIsDeleted() ? SyncOperateEnum.OPERATE_DELETE.getCode() : code;
            syncDhtService.createSyncCustomerAddressTaskToDht(customerAddressEntity, newOperate);
        }
    }


    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(CustomerInfoEntity entity) {
        List<CustomerInfoEntity> list = Arrays.asList(entity);

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveStatus);

        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        //反审核
        if (result) {
            //添加日志
            String ingContent = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");

            //发送金蝶
            sendPushTask(list,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
            //发送订货通
            sendDhtPushTask(list, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public  List<BatchResultDTO>  deleteByIds(List<String> ids) {
        List<CustomerInfoEntity> list = this.listByIds(ids);
//        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
//        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
//        //占用状态
//        long occupyCount = list.stream().filter(s -> s.getOccupyStatus()).count();
//        if (occupyCount > 0) {
//            throw new ServiceException(ApiError.ERROR_92018);
//        }
        List<CustomerInfoEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (CustomerInfoEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            if (entity.getOccupyStatus()){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_92018.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(CustomerInfoEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }

        //删除客户
        Boolean result = this.removeByIds(removeIdList);
        if (result) {
            //添加日志
            String content = "删除客户[%s]";
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "删除");
            //发送金蝶
            sendPushTask(removeList,SyncOperateEnum.OPERATE_DELETE.getCode());
        }else {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        return resultDTOList;
    }


    /**
     * 导出 客户列表
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:53
     */
    @Override
    public Boolean exportExcel(CustomerDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("客户列表", EXPORT_OMS_CUSTOMER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<CustomerDTO.InfoDTO> listCustomer() {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerInfoEntity::getId,
                CustomerInfoEntity::getCode,
                CustomerInfoEntity::getName,
                CustomerInfoEntity::getDisabled);
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(approve);
        queryWrapper.eq(CustomerInfoEntity::getApproveStatus, approveStatusEnum);
        queryWrapper.orderByAsc(CustomerInfoEntity::getDisabled);
        List<CustomerInfoEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean updateStatus(CustomerBatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<CustomerInfoEntity> customerList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(customerList)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }

        Boolean disabled = dto.getDisabled();
        LocalDateTime enableTime = dto.getEnableTime();
		if(!disabled && enableTime == null) {
        	throw new ServiceException("修改状态为启用，启用时间必填");
        }
        long count = customerList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != customerList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        customerList.forEach(d -> {
        	d.setDisabled(disabled);
        	if(disabled) {
        		d.setDownTime(LocalDateTime.now());
        	}else {
        		d.setEnableTime(enableTime);
        	}
        	shopInfoService.lambdaUpdate()
	        	.eq(ShopInfoEntity::getCustomerId, d.getId())
	        	.set(ShopInfoEntity::getDisabled, d.getDisabled())
	        	.set(ShopInfoEntity::getEnableTime, d.getEnableTime())
	        	.set(ShopInfoEntity::getDownTime, d.getDownTime())
	        	.update();
        });
        
        //添加日志
        List<Pair<String, String>> pairList = customerList.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getName())).collect(Collectors.toList());
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        String finalContent = "[%s]," + content;
        operateLogService.batchAddModuleOperateLog(finalContent, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");

        boolean update = this.updateBatchById(customerList);

        //发送金蝶
        String operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        if (dto.getDisabled()) {
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        // 发送金蝶
        sendPushTask(customerList,operate);
        // 发送订货通
        sendDhtPushTask(customerList, operate);
        return  update;
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
        List<CustomerInfoEntity> list = this.listByIds(ids);
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
        operateLogService.batchAddModuleOperateLog("客户【%s】取消流程", ModuleTypeEnum.CUSTOMER.getCode(), pairList, "取消流程操作");
        return result;
    }


    /**
     * 获取启用的列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 16:05
     */
    @Override
    public List<CustomerDTO.InfoDTO> listEnable(String permissionSql) {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerInfoEntity::getId,
                CustomerInfoEntity::getCode,
                CustomerInfoEntity::getName,
                CustomerInfoEntity::getShortName,
                CustomerInfoEntity::getApproveStatus,
                CustomerInfoEntity::getDisabled,
                CustomerInfoEntity::getCurrency);
        if (StringUtils.isNotBlank(permissionSql)) {
            queryWrapper.last(permissionSql + " ORDER BY create_time DESC");
        } else {
            queryWrapper.last(" ORDER BY create_time DESC");
        }
        List<CustomerInfoEntity> list = this.list(queryWrapper);
        List<CustomerDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
        List<ApproveStatusEnum> statusList = new ArrayList<>(1);
        statusList.add(ApproveStatusEnum.APPROVE);
        for (CustomerDTO.InfoDTO item : resultList) {
            if (!statusList.contains(item.getApproveStatus())) {
                item.setDisabled(true);
            }
        }
        resultList = resultList.stream().sorted(Comparator.comparing(CustomerDTO.InfoDTO::getDisabled)).collect(Collectors.toList());
        return resultList;
    }


    /**
     * 获取客户的默认联系人
     *
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerDTO.BaseDTO
     * @author yl
     * @date 2023-05-15 16:15
     */
    @Override
    public CustomerDTO.BaseDTO getBase(String customerId) {
        CustomerDTO.BaseDTO base = new CustomerDTO.BaseDTO();
        CustomerInfoEntity customer = this.getById(customerId);
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
        base.setUseOrgId(customer.getUseOrgId());
        base.setUseOrgName(customer.getUseOrgName());
        String currencySymbol = "";
        if (CollectionUtils.isNotEmpty(currencyList)) {
            currencySymbol = currencyList.get(0).getSymbol();
        }
        base.setCurrencySymbol(currencySymbol);
        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(customerId);
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
        if(StringUtils.isNotBlank(customer.getSalesDeptId())){
            List<SysDepartmentEntity> departmentEntityList = sysUserFeign.getDeptByIds(Collections.singletonList(customer.getSalesDeptId()));
            base.setSalesDeptId(customer.getSalesDeptId());
            base.setSalesDeptName(CollUtil.isNotEmpty(departmentEntityList) ? departmentEntityList.get(0).getName() : CharSequenceUtil.EMPTY);
        }
        return base;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(CustomerInfoEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), CustomerInfoEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
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
            this.lambdaUpdate().in(CustomerInfoEntity::getId, ids).
                    set(CustomerInfoEntity::getOccupyStatus, Boolean.TRUE).update();
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CustomerInfoVO> listCustomerByGroup() {
        return baseMapper.listCustomerByGroup();
    }

    @Override
    @Cacheable(cacheNames = "cache:oms:listCustomerByProperty", keyGenerator = "myKeyGenerator")
    public List<CustomerInfoVO> listCustomerByProperty() {
        return baseMapper.listCustomerByProperty();
    }

    private Boolean updateApproveStatus(List<CustomerInfoEntity> list, ApproveStatusEnum statusEnum, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (CustomerInfoEntity item : list) {
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
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(CustomerInfoEntity::getId, id);
        }
        queryWrapper.eq(CustomerInfoEntity::getName, name);
        queryWrapper.last( SqlConstants.LIMIT_1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_1028);
        }
    }
    /**
     * 检查名称
     *
     * @param id
     * @param name
     * @return void
     */
    private CustomerInfoEntity getByName(String id, String name) {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(CustomerInfoEntity::getId, id);
        }
        queryWrapper.eq(CustomerInfoEntity::getName, name);
        queryWrapper.last( SqlConstants.LIMIT_1);
        CustomerInfoEntity one = this.getOne(queryWrapper);
        if (Objects.nonNull(one)) {
            return one;
        }
        return null;
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
        List<CustomerInfoEntity> list = this.list();
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        for (CustomerInfoEntity item : list) {
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
        List<CustomerGroupEntity> customerGroupEntityList = customerGroupService.list();
        Map<String, List<CustomerGroupEntity>> customerGroupNameMap = customerGroupEntityList.stream().filter(r -> StrUtils.isNotEmpty(r.getName())).collect(Collectors.groupingBy(CustomerGroupEntity::getName));
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String, List<DictCountryDTO.ListDTO>> countryNameMap = countryList.stream().collect(Collectors.groupingBy(DictCountryDTO.ListDTO::getNameCn));
        // 平台类型
        List<DictBasicDTO.ViewDTO> platFormList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
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
        // 收款条件
        List<KingdeeReceiptConditionEntity> receiptConditionList = kingdeeReceiptConditionService.list();
        // 部门
        List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
        Map<String, List<SysUserDeptDTO>> deptNameMap = userDeptList.stream().filter(r -> StrUtils.isNotEmpty(r.getDeptName())).collect(Collectors.groupingBy(SysUserDeptDTO::getDeptName));
        // 人员
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, List<FindUserDTO>> userNameMap = userList.stream().collect(Collectors.groupingBy(FindUserDTO::getUserName));

        wb.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        // 一个客户会存在多行数据
        Map<String, CustomerInfoEntity> customerBaseMap = Maps.newHashMap();
        for (int i = 2; i < rows; i++) {
            int noticeRow = i + 1;
            XSSFRow row = sheet.getRow(i);

            // 客户编码
            String code = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));

            LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CustomerInfoEntity::getCode, code);
            queryWrapper.last( SqlConstants.LIMIT_1);
            CustomerInfoEntity checkCustomerInfoEntity = this.baseMapper.selectOne(queryWrapper);
            if (Objects.nonNull(checkCustomerInfoEntity)) {
                log.info("已经存在客户编码【{}】，本次不导入", code);
                continue;
            }

            CustomerInfoEntity customerInfoEntity = new CustomerInfoEntity();

            // 基本信息
            customerInfoEntity.setCode(code);
            // 使用组织
            String useOrgName = ExcelUtil.convertCellValueToString(row.getCell(2));
            customerInfoEntity.setUseOrgName(useOrgName);
            // 使用组织id需根据名称获取
            if (!accountCompanyNameMap.containsKey(useOrgName)) {
                throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到组织【{}】", noticeRow, useOrgName));
            }
            // 名称不会重复
            if (Objects.nonNull(accountCompanyNameMap.get(useOrgName))) {
                customerInfoEntity.setUseOrgId(accountCompanyNameMap.get(useOrgName).get(0).getId());
            }

            // 客户分组（单独的表需提前维护customer_group），分组id需要根据名称获取
            String groupName = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(3)));
            // 客户分组id需根据客户分组名称获取
            if (StrUtils.isNotEmpty(groupName) && customerGroupNameMap.containsKey(groupName)) {
                customerInfoEntity.setGroupName(groupName);
                customerInfoEntity.setGroupId(customerGroupNameMap.get(groupName).get(0).getId());
            }

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
            if (StrUtils.isNotEmpty(provinceName)) {
                List<DictCityDTO.ListDTO> provinceList = sysUserFeign.getProvincesByCountryCode(customerInfoEntity.getCountryId());
                if (CollUtil.isNotEmpty(provinceList)) {
                    provinceDTO = provinceList.stream().filter(r -> Objects.equals(r.getName(), provinceName)).findFirst().orElse(null);
                    if (Objects.nonNull(provinceDTO)) {
                        customerInfoEntity.setProvinceId(provinceDTO.getId());
                    }
                }
            }

            // 城市
            DictCityDTO.ListDTO cityDTO = null;
            String cityName = ExcelUtil.convertCellValueToString(row.getCell(7));
            if (StrUtils.isNotEmpty(cityName) && Objects.nonNull(provinceDTO)) {
                List<DictCityDTO.ListDTO> cityList = provinceDTO.getChildrenList();
                if (CollUtil.isNotEmpty(cityList)) {
                    cityDTO = cityList.stream().filter(r -> Objects.equals(r.getName(), cityName)).findFirst().orElse(null);
                }
                if (Objects.nonNull(cityDTO)) {
                    customerInfoEntity.setCityId(cityDTO.getId());
                }
            }

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
            String conditionDictId=receiptConditionList.stream().filter(c->c.getName().equals(conditionDictName)).map(c->c.getId()).findFirst().orElse("");
            if (StrUtils.isEmpty(conditionDictName) ||StringUtils.isEmpty(conditionDictId)) {
                throw new ServiceException( CharSequenceUtil.format("第【{}】行收款条件为空或未找到收款条件【{}】", noticeRow, conditionDictName));
            }
            customerInfoEntity.setConditionDict(conditionDictId);

            // 无附件

            // 备注
            String remark = ExcelUtil.convertCellValueToString(row.getCell(18));
            customerInfoEntity.setRemark(remark);
            // 审核状态
            customerInfoEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            customerInfoEntity.setApproveUserName(ApiError.ADMIN.msg);
            customerInfoEntity.setCreateTime(LocalDateTime.now());
            customerInfoEntity.setUpdateTime(LocalDateTime.now());
            customerInfoEntity.setCreateUserId("");
            customerInfoEntity.setCreateUserName(ApiError.ADMIN.msg);
            customerInfoEntity.setUpdateUserId("");
            customerInfoEntity.setUpdateUserName(ApiError.ADMIN.msg);

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
            this.importCustomerInvoice(customerInfoEntity.getId(), row);

            // 客户销售员信息
            this.importCustomerSale(customerInfoEntity.getId(), row, noticeRow, deptNameMap, userNameMap);

            // 需要拉取客户的金蝶id
        }

    }

    @Override
    public List<CustomerInfoEntity> listByKingdeeIdList(List<String> kingdeeCustomerIds) {
        if (CollectionUtils.isEmpty(kingdeeCustomerIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(CustomerInfoEntity::getSyncKingdeeId, kingdeeCustomerIds).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCustomerKingdee(MultipartFile file) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = wb.getSheetAt(0);
        // 读取数据集
        int rows = sheet.getPhysicalNumberOfRows();

        for (int i = 2; i < rows; i++) {
            XSSFRow row = sheet.getRow(i);

            // 客户名称
            String customerName = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(6)));
            // 金蝶id
            String kingdeeId = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));
            LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CustomerInfoEntity::getName, customerName);
            queryWrapper.last( SqlConstants.LIMIT_1);
            CustomerInfoEntity customerInfoEntity = super.getOne(queryWrapper);
            if (Objects.isNull(customerInfoEntity)) {
                log.info("未找到客户【{}】", customerName);
                continue;
            }
            if (StrUtils.isNotEmpty(customerInfoEntity.getSyncKingdeeId())) {
                log.info("客户【{}】已经存在金蝶id，不处理", customerName);
                continue;
            }
            lambdaUpdate().set(CustomerInfoEntity::getSyncKingdeeId, kingdeeId)
                    .eq(CustomerInfoEntity::getId, customerInfoEntity.getId())
                    .update();
        }

    }

    @Override
    public List<CustomerInfoEntity> listByCountryIdList(List<String> countryIdList) {
        if (CollectionUtils.isEmpty(countryIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CustomerInfoEntity> list = lambdaQuery()
                .in(CustomerInfoEntity::getCountryId, countryIdList)
                .list();
        return list;
    }

    @Override
    public CustomerInfoEntity getCustomerByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerInfoEntity::getName, name);
        queryWrapper.eq(CustomerInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus());
        queryWrapper.last( SqlConstants.LIMIT_1);
        return this.getOne(queryWrapper);
    }

    @Override
    public CustomerInfoEntity getByName(String customerName) {

        return this.lambdaQuery().eq(CustomerInfoEntity::getName, customerName).
                eq(CustomerInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE).last( SqlConstants.LIMIT_1).one();
    }

    @Override
    public List<CustomerInfoEntity> listByNameList(List<String> customerNameList) {
        if (CollectionUtils.isEmpty(customerNameList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(CustomerInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE).
                in(CustomerInfoEntity::getName, customerNameList).list();
    }

    @Override
    public List<CustomerInfoEntity> listByName(String name) {
        if (StringUtils.isBlank(name)) {
            return this.list();
        }
        return this.lambdaQuery().like(CustomerInfoEntity::getName, name).
                orderByAsc(CustomerInfoEntity::getDisabled).list();
    }

    @Override
    public List<CustomerDTO.ReceiveInfoDTO> listReceiveByName(String name) {
        List<CustomerInfoEntity> customerList = this.listByName(name);
        List<CustomerDTO.ReceiveInfoDTO> resultList = new ArrayList<>(customerList.size());
        List<CustomerAddressEntity> addressList = customerAddressService.listByCustomerName(name);
        for (CustomerInfoEntity item : customerList) {
            CustomerDTO.ReceiveInfoDTO info = new CustomerDTO.ReceiveInfoDTO();
            info.setName(item.getName());
            info.setId(item.getId());
            info.setCode(item.getCode());
            info.setDisabled(item.getDisabled());
            CustomerAddressEntity address = addressList.stream().filter(a -> a.getMainId().equals(item.getId())).findFirst().orElse(null);
            if (Objects.nonNull(address)) {
                info.setReceiverName(address.getPerson());
                info.setTelNumber(address.getTelNumber());
                info.setReceiveAddress(address.getAddress());
            }
            resultList.add(info);
        }
        return resultList;
    }

    @Override
    public List<CustomerDTO.ReceiveInfoDTO> listDTOByNameList(List<String> customerNameList) {
        if (CollectionUtils.isEmpty(customerNameList)){
            return Collections.emptyList();
        }
        List<CustomerInfoEntity> list = listByNameList(customerNameList);
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<String> mainIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, List<CustomerAddressEntity>> addressMap = customerAddressService.listByMainIdList(mainIds)
                .stream()
                .collect(Collectors.groupingBy(CustomerAddressEntity::getMainId));

       return list.stream().map(item ->{
            CustomerDTO.ReceiveInfoDTO info = new CustomerDTO.ReceiveInfoDTO();
            info.setName(item.getName());
            info.setId(item.getId());
            info.setCode(item.getCode());
            info.setDisabled(item.getDisabled());

            CustomerAddressEntity address = null;
           List<CustomerAddressEntity> addressList = addressMap.get(info.getId());
           if (CollectionUtils.isNotEmpty(addressList)){
               CustomerAddressEntity defaultAddressEntity = addressList.stream()
                       .filter(CustomerAddressEntity::getIsDefault)
                       .findFirst()
                       .orElse(null);
               if (null != defaultAddressEntity){
                   // 默认地址
                    address = defaultAddressEntity;
               } else {
                   // 最新地址
                   address = addressList.stream()
                           .max(Comparator.comparing(CustomerAddressEntity::getCreateUserName))
                           .orElse(null);
               }
           }
           if (null != address) {
                info.setReceiverName(address.getPerson());
                info.setTelNumber(address.getTelNumber());
                info.setReceiveAddress(address.getAddress());
            } else {
                info.setReceiverName("");
                info.setTelNumber("");
                info.setReceiveAddress("");
            }
            return info;
            }).collect(Collectors.toList());
    }

    @Override
    public CustomerInfoEntity getCustomerById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return this.getById(id);
    }

    @Override
    public CustomerInfoEntity getCustomerByCode(String code) {
        return this.lambdaQuery().eq(CustomerInfoEntity::getCode, code).last("limit 1").one();
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

        CustomerContactEntity customerContactEntity = new CustomerContactEntity();
        customerContactEntity.setMainId(customerId);


        customerContactEntity.setPerson(person);
        // 职务
        String position = ExcelUtil.convertCellValueToString(row.getCell(20));
        customerContactEntity.setPosition(position);
        // 电话
        String telNumber = ExcelUtil.convertCellValueToString(row.getCell(21));
        customerContactEntity.setTelNumber(telNumber);
        // 邮箱
        String email = ExcelUtil.convertCellValueToString(row.getCell(22));
        customerContactEntity.setEmail(email);
        // 默认联系人
        String isDefaultName = ExcelUtil.convertCellValueToString(row.getCell(23));
        customerContactEntity.setIsDefault(Boolean.FALSE);
        if (Objects.equals(isDefaultName, "是") || Objects.equals(isDefaultName, "默认")) {
            customerContactEntity.setIsDefault(Boolean.TRUE);
        }

        // 是否启用
        String isDisableName = ExcelUtil.convertCellValueToString(row.getCell(24));
        customerContactEntity.setDisabled(Boolean.FALSE);
        if (Objects.equals(isDisableName, "是") || Objects.equals(isDisableName, "禁用")) {
            customerContactEntity.setDisabled(Boolean.TRUE);
        }

        // 备注
        String contactRemark = ExcelUtil.convertCellValueToString(row.getCell(25));
        customerContactEntity.setRemark(contactRemark);

        customerContactService.save(customerContactEntity);
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

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setMainId(customerId);

        customerAddressEntity.setAddress(address);

        customerAddressEntity.setPerson(addressPerson);
        // 地址类型
        String addressTypeName = ExcelUtil.convertCellValueToString(row.getCell(28));
        if (StrUtils.isNotEmpty(addressTypeName)) {
            customerAddressEntity.setType(AddressTypeEnum.getCodeByName(addressTypeName));
        }
        // 电话
        String addressTelNumber = ExcelUtil.convertCellValueToString(row.getCell(29));
        customerAddressEntity.setTelNumber(addressTelNumber);
        // 邮箱
        String addressEmail = ExcelUtil.convertCellValueToString(row.getCell(30));
        customerAddressEntity.setEmail(addressEmail);
        // 默认地址
        String isDefaultAddress = ExcelUtil.convertCellValueToString(row.getCell(31));
        customerAddressEntity.setIsDefault(Boolean.FALSE);
        if (Objects.equals(isDefaultAddress, "是") || Objects.equals(isDefaultAddress, "默认")) {
            customerAddressEntity.setIsDefault(Boolean.TRUE);
        }
        // 是否启用
        String isDisableAddress = ExcelUtil.convertCellValueToString(row.getCell(32));
        customerAddressEntity.setDisabled(Boolean.FALSE);
        if (Objects.equals(isDisableAddress, "是") || Objects.equals(isDisableAddress, "禁用")) {
            customerAddressEntity.setDisabled(Boolean.TRUE);
        }

        // 备注
        String addressRemark = ExcelUtil.convertCellValueToString(row.getCell(33));
        customerAddressEntity.setRemark(addressRemark);
        customerAddressEntity.setCreateTime(LocalDateTime.now());
        customerAddressEntity.setUpdateTime(LocalDateTime.now());
        customerAddressEntity.setCreateUserId("");
        customerAddressEntity.setCreateUserName("");
        customerAddressEntity.setUpdateUserId("");
        customerAddressEntity.setUpdateUserName("");
        customerAddressService.save(customerAddressEntity);

    }

    /**
     * 客户发票信息
     *
     * @param mainId
     * @param row
     */
    public void importCustomerInvoice(String mainId, XSSFRow row) {
        // 发票信息
        // 发票抬头
        String invoiceHead = ExcelUtil.convertCellValueToString(row.getCell(34));
        if (StrUtils.isEmpty(invoiceHead)) {
            return;
        }
        CustomerInvoiceEntity customerInvoiceEntity = new CustomerInvoiceEntity();
        customerInvoiceEntity.setMainId(mainId);
        customerInvoiceEntity.setHead(invoiceHead);
        // 发票类型
        String invoiceTypeName = ExcelUtil.convertCellValueToString(row.getCell(35));
        customerInvoiceEntity.setType(invoiceTypeName);
        // 开户银行
        String bankName = ExcelUtil.convertCellValueToString(row.getCell(36));
        customerInvoiceEntity.setBankName(bankName);
        // 银行账号
        String bankAccount = ExcelUtil.convertCellValueToString(row.getCell(37));
        customerInvoiceEntity.setBankAccount(bankAccount);
        // 是否默认银行
        String isDefaultBank = ExcelUtil.convertCellValueToString(row.getCell(38));
        customerInvoiceEntity.setIsDefault(Boolean.FALSE);
        if (Objects.equals(isDefaultBank, "是") || Objects.equals(isDefaultBank, "默认")) {
            customerInvoiceEntity.setIsDefault(Boolean.TRUE);
        }

        // 备注
        String invoiceRemark = ExcelUtil.convertCellValueToString(row.getCell(39));
        customerInvoiceEntity.setRemark(invoiceRemark);
        customerInvoiceEntity.setCreateTime(LocalDateTime.now());
        customerInvoiceEntity.setUpdateTime(LocalDateTime.now());
        customerInvoiceEntity.setCreateUserId("");
        customerInvoiceEntity.setCreateUserName("");
        customerInvoiceEntity.setUpdateUserId("");
        customerInvoiceEntity.setUpdateUserName("");
        customerInvoiceService.save(customerInvoiceEntity);
    }

    /**
     * 新增客户销售员信息
     *
     * @param mainId
     * @param row
     * @param noticeRow
     * @param deptNameMap
     * @param userNameMap
     */
    public void importCustomerSale(String mainId, XSSFRow row,
                                   Integer noticeRow,
                                   Map<String, List<SysUserDeptDTO>> deptNameMap,
                                   Map<String, List<FindUserDTO>> userNameMap) {
        // 销售部门
        String deptName = ExcelUtil.convertCellValueToString(row.getCell(40));
        if (StrUtils.isEmpty(deptName)) {
            return;
        }
        if (StrUtils.isNotEmpty(deptName) && !deptNameMap.containsKey(deptName)) {
            throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到销售部门【{}】", noticeRow, deptName));
        }
        // 销售员信息
        CustomerSellerEntity customerSellerEntity = new CustomerSellerEntity();
        customerSellerEntity.setMainId(mainId);
        customerSellerEntity.setDeptId("");
        if (Objects.nonNull(deptNameMap.get(deptName))) {
            customerSellerEntity.setDeptId(deptNameMap.get(deptName).get(0).getDeptId());
        }
        // 销售员
        String sellerName = ExcelUtil.convertCellValueToString(row.getCell(41));
        if (StrUtils.isNotEmpty(sellerName) && !userNameMap.containsKey(sellerName)) {
            throw new ServiceException( CharSequenceUtil.format("第【{}】行未找到销售员【{}】", noticeRow, sellerName));
        }
        customerSellerEntity.setSellerName(sellerName);
        // 需转换成销售员id
        customerSellerEntity.setSellerId("");
        if (userNameMap.containsKey(sellerName)) {
            customerSellerEntity.setSellerId(userNameMap.get(sellerName).get(0).getUserId());
        }
        // 开始日期
        String startDateStr = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(42)));
        if (StrUtils.isNotEmpty(startDateStr) && startDateStr.length() == 10) {
            if (startDateStr.contains("-")) {
                LocalDate startDate = LocalDate.parse(startDateStr);
                customerSellerEntity.setStartDate(startDate);
            } else if (startDateStr.contains("/")) {
                LocalDate endDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                customerSellerEntity.setStartDate(endDate);
            }
        }
        // 结束日期
        String endDateStr = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(43)));
        if (StrUtils.isNotEmpty(endDateStr) && endDateStr.length() == 10) {
            if (endDateStr.contains("-")) {
                LocalDate endDate = LocalDate.parse(endDateStr);
                customerSellerEntity.setEndDate(endDate);
            } else if (endDateStr.contains("/")) {
                LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                customerSellerEntity.setEndDate(endDate);
            }
        }
        // 备注
        String sellerRemark = ExcelUtil.convertCellValueToString(row.getCell(44));
        customerSellerEntity.setRemark(sellerRemark);
        customerSellerEntity.setCreateTime(LocalDateTime.now());
        customerSellerEntity.setUpdateTime(LocalDateTime.now());
        customerSellerEntity.setCreateUserId("");
        customerSellerEntity.setCreateUserName("");
        customerSellerEntity.setUpdateUserId("");
        customerSellerEntity.setUpdateUserName("");
        customerSellerService.save(customerSellerEntity);

    }

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<CustomerInfoEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_INFO.getCode());
            startDTO.setBusinessName(obj.getCode());
            //客户审核的时候流程发起人修改为销售员，如果没有销售员再使用当前登录人
            if (StringUtils.isNotBlank(obj.getSellerId())) {
                startDTO.setUserId(obj.getSellerId());
            } else {
                startDTO.setUserId(userInfo.getUid());
            }
            startDTO.setVariablesMap(getVariablesMap(obj));
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
    private void approveProcess(List<CustomerInfoEntity> list, BaseApproveParamDTO dto) {
        //无需流程则直接更新状态
        if (ObjectUtil.isNotEmpty(dto.getIsNeedProcess()) && !dto.getIsNeedProcess()) {
            approveEnd(dto, list);
            return;
        }

        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_INFO.getCode());
            approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
            approveDTO.setComment(dto.getComment());
            approveDTO.setUserId(userInfo.getUid());
            approveDTO.setVariablesMap(getVariablesMap(obj));
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
            List<CustomerInfoEntity> updateList = list.stream().filter(obj -> updateIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, updateList);
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(CustomerInfoEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<CfgCountryPartitionEntity> cfgCountryPartitionEntity = FeignQuery.create(CfgCountryPartitionEntity.class).eq(CfgCountryPartitionEntity::getCountry,entity.getCountryId()).list();
        if(CollectionUtils.isNotEmpty(cfgCountryPartitionEntity)){
            variablesMap.put("partitionName", cfgCountryPartitionEntity.get(0).getPartitionName());
        }
        return variablesMap;
    }

    @Override
    public List<CustomerDTO.SellerUserDeptDTO> listSellerUserDepByCodes(List<String> codeList) {
        if (CollUtil.isEmpty(codeList)) {
            return Collections.emptyList();
        }
        List<CustomerDTO.SellerUserDeptDTO> resultList = new ArrayList<>();
        List<CustomerInfoEntity> list = lambdaQuery().in(CustomerInfoEntity::getCode, codeList).list();

        //获取负责人id
        List<String> sellerIdList = list.stream().map(req -> req.getSellerId()).distinct().collect(Collectors.toList());
        //查询负责人部门
        List<SysDepartmentUserNumberDTO> sysDepartmentUserNumberDTOS = sysUserFeign.listDeptUserByUserIdList(sellerIdList);

        for (CustomerInfoEntity entity : list) {
            CustomerDTO.SellerUserDeptDTO sellerUserDeptDTO = new CustomerDTO.SellerUserDeptDTO();
            sellerUserDeptDTO.setCountryId(entity.getCountryId());
            //客户编码
            sellerUserDeptDTO.setCode(entity.getCode());
            //负责人id
            sellerUserDeptDTO.setSellerId(entity.getSellerId());
            //负责人名称
            sellerUserDeptDTO.setSellerName(entity.getSellerName());
            //查询负责人部门
            SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysDepartmentUserNumberDTOS.stream().filter(req -> entity.getSellerId().equals(req.getUserId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysDepartmentUserNumberDTO)) {
                //负责人部门id
                sellerUserDeptDTO.setDeptId(sysDepartmentUserNumberDTO.getDepartmentId());
                //负责人部门名称
                sellerUserDeptDTO.setDeptName(sysDepartmentUserNumberDTO.getDepartmentName());
            }
            resultList.add(sellerUserDeptDTO);
        }
        return resultList;
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<CustomerInfoEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            List<DmpPushTaskEntity> pushTaskEntityList = syncKingdeeCustomerService.syncDataToKingdee(obj, operate);
            syncKingdeeCustomerService.syncDataToSdy(obj, operate);
            resultList.addAll(pushTaskEntityList);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    @Override
    public PagingVO<CustomerDTO.PageSelectDTO> pagingSelect(PagingDTO<CustomerDTO.SelectDTO> dto) {
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CustomerDTO.PageSelectDTO> pageData = this.baseMapper.pageSelect(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO<>(pageData);

    }

    @Override
    public List<cn.hutool.core.lang.Pair<Integer,List<?>>> exportCustomerPairList(PagingDTO<CustomerDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        List<cn.hutool.core.lang.Pair<Integer,List<?>>> pairList = new ArrayList<>();
        Page<CustomerDTO.PagingExportDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        List<CustomerDTO.PagingExportDTO> records = page.getRecords();
        if(CollUtil.isEmpty(records)) {
            return pairList;
        }
        List<String> customerIdList = records.stream().map(CustomerDTO.PagingExportDTO::getId).collect(Collectors.toList());

        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);

        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        customerIdList.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_INFO.getCode(), obj)));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String,String> countryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(countryList)){
            countryMap = countryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        }
        //省/城市
        List<String> cityIds = records.stream().map(CustomerDTO.PagingExportDTO::getCityId).collect(Collectors.toList());
        List<String> provinceIds = records.stream().map(CustomerDTO.PagingExportDTO::getProvinceId).collect(Collectors.toList());
        cityIds.addAll(provinceIds);
        List<DictCityEntity> cityList = sysUserFeign.listCityByIds(cityIds);
        Map<String,String> cityMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(cityList)){
            cityMap = cityList.stream().collect(Collectors.toMap(DictCityEntity::getId, DictCityEntity::getName));
        }

        //区域
        List<String> areaIds = records.stream().map(CustomerDTO.PagingExportDTO::getAreaId).collect(Collectors.toList());
        List<DictGlobalAreaEntity> areaList = sysUserFeign.listGlobalAreaByIds(areaIds);
        Map<String,String> areaMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(areaList)){
            areaMap = areaList.stream().collect(Collectors.toMap(DictGlobalAreaEntity::getId, DictGlobalAreaEntity::getRegionName));
        }

        //公司类别
        List<DictBasicDTO.ViewDTO> customerCategoryList = dictBasicService.getByKey("customerCompanyCategory");
        Map<String,String> customerCategoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(customerCategoryList)){
            customerCategoryMap = customerCategoryList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getId, DictBasicDTO.ViewDTO::getName));
        }

        for (CustomerDTO.PagingExportDTO item : records) {
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "停用" : "启用";
            item.setDisabledName(disabledName);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            //平台类型名称
            String platformTypeName = dictList.stream().filter(obj -> obj.getValue().equals(item.getPlatformType())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPlatformTypeName(platformTypeName);
            //最新审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,item.getApproveUserName()));
            }
            //国家
            item.setCountryName(countryMap.get(item.getCountryId()));
            //城市
            item.setCityName(cityMap.get(item.getCityId()));
            //省
            item.setProvinceName(cityMap.get(item.getProvinceId()));
            //区域
            item.setAreaName(areaMap.get(item.getAreaId()));
            //平台类型
            item.setBusinessModeName(CustomerInfoBusinessModeEnum.getName(item.getBusinessMode()));
            //公司类别
            item.setCompanyCategoryDictName(customerCategoryMap.get(item.getCompanyCategoryDict()));
        }



        //发票信息
        List<InvoiceDTO.ViewDTO> invoiceList = customerInvoiceService.listByMainIds(customerIdList);
        //发票
        for (InvoiceDTO.ViewDTO view : invoiceList) {
            view.setIsDefaultName(view.getIsDefault() ? "是" : "否");
            view.setTypeName(InvoiceTypeEnum.getName(view.getType()));
        }

        //联系人信息
        //地址信息
        List<CustomerDTO.PagingAddressContactExportDTO> pagingAddressContactDTOS = baseMapper.listAddressContactExport(customerIdList);
        //联系人地址
        for (CustomerDTO.PagingAddressContactExportDTO view : pagingAddressContactDTOS) {
            view.setPersonDisabledName(view.getPersonDisabled() ? "停用" : "启用");
            view.setPersonIsDefaultName(view.getPersonIsDefault() ? "是" : "否");
            view.setAddressDisabledName(view.getAddressDisabled() ? "停用" : "启用");
            view.setAddressIsDefaultName(view.getAddressIsDefault() ? "是" : "否");
        }

        cn.hutool.core.lang.Pair customerInfo = new cn.hutool.core.lang.Pair(0,records);
        cn.hutool.core.lang.Pair invoice = new cn.hutool.core.lang.Pair(1,invoiceList);
        cn.hutool.core.lang.Pair addressContact = new cn.hutool.core.lang.Pair(2,pagingAddressContactDTOS);
        pairList.add(customerInfo);
        pairList.add(invoice);
        pairList.add(addressContact);
        return pairList;
    }

    @Override
    public PagingVO<CustomerDTO.PagingExportDTO> exportCustomer(PagingDTO<CustomerDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<CustomerDTO.PagingExportDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        List<CustomerDTO.PagingExportDTO> records = page.getRecords();
        if(CollUtil.isEmpty(records)) {
            return new PagingVO<>(page);
        }
        List<String> customerIdList = records.stream().map(CustomerDTO.PagingExportDTO::getId).collect(Collectors.toList());

        //发票信息
        List<InvoiceDTO.ViewDTO> invoiceList = customerInvoiceService.listByMainIds(customerIdList);
        Map<String, List<InvoiceDTO.ViewDTO>> invoiceMap = invoiceList.stream().collect(Collectors.groupingBy(InvoiceDTO.ViewDTO::getMainId));

        //联系人信息
        //地址信息
        List<CustomerDTO.PagingAddressContactExportDTO> pagingAddressContactDTOS = baseMapper.listAddressContactExport(customerIdList);
        Map<String, List<CustomerDTO.PagingAddressContactExportDTO>> addressContactMap = pagingAddressContactDTOS.stream().collect(Collectors.groupingBy(CustomerDTO.PagingAddressContactExportDTO::getId));

        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);

        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        customerIdList.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_INFO.getCode(), obj)));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String,String> countryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(countryList)){
            countryMap = countryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        }
        //省/城市
        List<String> cityIds = records.stream().map(CustomerDTO.PagingExportDTO::getCityId).collect(Collectors.toList());
        List<String> provinceIds = records.stream().map(CustomerDTO.PagingExportDTO::getProvinceId).collect(Collectors.toList());
        cityIds.addAll(provinceIds);
        List<DictCityEntity> cityList = sysUserFeign.listCityByIds(cityIds);
        Map<String,String> cityMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(cityList)){
            cityMap = cityList.stream().collect(Collectors.toMap(DictCityEntity::getId, DictCityEntity::getName));
        }

        //区域
        List<String> areaIds = records.stream().map(CustomerDTO.PagingExportDTO::getAreaId).collect(Collectors.toList());
        List<DictGlobalAreaEntity> areaList = sysUserFeign.listGlobalAreaByIds(areaIds);
        Map<String,String> areaMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(areaList)){
            areaMap = areaList.stream().collect(Collectors.toMap(DictGlobalAreaEntity::getId, DictGlobalAreaEntity::getRegionName));
        }

        //公司类别
        List<DictBasicDTO.ViewDTO> customerCategoryList = dictBasicService.getByKey("customerCompanyCategory");
        Map<String,String> customerCategoryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(customerCategoryList)){
            customerCategoryMap = customerCategoryList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));
        }
        //销售部门id
        List<String> salesDeptIdList = records.stream().map(CustomerDTO.PagingExportDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(salesDeptIdList);

        for (CustomerDTO.PagingExportDTO item : records) {
            String deptName = departmentList.stream().filter(d -> d.getId().equals(item.getSalesDeptId())).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(deptName);
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "停用" : "启用";
            item.setDisabledName(disabledName);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            //平台类型名称
            String platformTypeName = dictList.stream().filter(obj -> obj.getValue().equals(item.getPlatformType())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPlatformTypeName(platformTypeName);
            //最新审核人
            if (listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,item.getApproveUserName()));
            }
            //国家
            item.setCountryName(countryMap.get(item.getCountryId()));
            //城市
            item.setCityName(cityMap.get(item.getCityId()));
            //省
            item.setProvinceName(cityMap.get(item.getProvinceId()));
            //区域
            item.setAreaName(areaMap.get(item.getAreaId()));
            //平台类型
            item.setBusinessModeName(CustomerInfoBusinessModeEnum.getName(item.getBusinessMode()));
            //公司类别
            item.setCompanyCategoryDictName(customerCategoryMap.get(item.getCompanyCategoryDict()));

            //发票
            List<InvoiceDTO.ViewDTO> invoiceViews = invoiceMap.get(item.getId());
            if(CollUtil.isNotEmpty(invoiceViews)){
                for (InvoiceDTO.ViewDTO view : invoiceViews) {
                    view.setIsDefaultName(view.getIsDefault() ? "是" : "否");
                    view.setTypeName(InvoiceTypeEnum.getName(view.getType()));
                    view.setCode(item.getCode());
                    view.setName(item.getName());
                }
                item.setInvoiceList(invoiceViews);
            }
            //联系人地址
            List<CustomerDTO.PagingAddressContactExportDTO> addressContactViews = addressContactMap.get(item.getId());
            if(CollUtil.isNotEmpty(addressContactViews)){
                for (CustomerDTO.PagingAddressContactExportDTO view : addressContactViews) {
                    view.setTypeName(CustomerAddressTypeEnum.getName(view.getType()));
                    view.setPersonDisabledName(null == view.getPersonDisabled() || view.getPersonDisabled()? "停用" : "启用");
                    view.setPersonIsDefaultName(null == view.getPersonIsDefault() || view.getPersonIsDefault() ? "是" : "否");
                    view.setAddressDisabledName(null == view.getAddressDisabled() || view.getAddressDisabled()? "停用" : "启用");
                    view.setAddressIsDefaultName(null == view.getAddressIsDefault() || view.getAddressIsDefault() ? "是" : "否");
                }
                item.setAddressContactList(addressContactViews);
            }
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<CustomerDTO.InfoDTO> listSimpleName(CustomerDTO.PageSelectDTO dto) {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerInfoEntity::getId,
                CustomerInfoEntity::getCode,
                CustomerInfoEntity::getName,
                CustomerInfoEntity::getShortName,
                CustomerInfoEntity::getApproveStatus,
                CustomerInfoEntity::getDisabled);
        queryWrapper.last(" ORDER BY create_time DESC");
        if(Objects.nonNull(dto) && StringUtils.isNotBlank(dto.getName())){
            queryWrapper.like(CustomerInfoEntity::getName, dto.getName()).or().like(CustomerInfoEntity::getShortName, dto.getName());
        }
        List<CustomerInfoEntity> list = this.list(queryWrapper);
        List<CustomerDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
        List<ApproveStatusEnum> statusList = new ArrayList<>(1);
        statusList.add(ApproveStatusEnum.APPROVE);
        for (CustomerDTO.InfoDTO item : resultList) {
            if (!statusList.contains(item.getApproveStatus())) {
                item.setDisabled(true);
            }
        }
        resultList = resultList.stream().sorted(Comparator.comparing(CustomerDTO.InfoDTO::getDisabled)).collect(Collectors.toList());
        return resultList;
    }

    @Override
    public void initHistoryCustomerDeptId() {
        List<CustomerInfoEntity> list = this.list();
        if (CollUtil.isEmpty(list)){
            return;
        }
        Map<String, List<CustomerInfoEntity>> orgMap = list.stream().collect(Collectors.groupingBy(CustomerInfoEntity::getUseOrgId));
        for (String orgId : orgMap.keySet()){
            if (CharSequenceUtil.isBlank(orgId)){
                continue;
            }
            List<CustomerInfoEntity> customerInfoEntityList = orgMap.get(orgId);
            if (CollUtil.isEmpty(customerInfoEntityList)){
                continue;
            }
            KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
            dto.setOrgId(orgId);
            dto.setType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> listApiResult = kingdeeFeign.listKingdeeUser(dto);
            List<UserInfoDTO.BusinessOperationUserDTO> data = listApiResult.getData();
            if (CollUtil.isEmpty(data)){
                continue;
            }
            customerInfoEntityList.forEach(customerInfoEntity -> {
                List<UserInfoDTO.BusinessOperationUserDTO> collect = data.stream().filter(e -> Objects.equals(customerInfoEntity.getSellerId(), e.getUserId())).collect(Collectors.toList());
                List<String> deptIds = new ArrayList<>();
                if(CollUtil.isNotEmpty(collect)){
                    deptIds = collect.stream().map(UserInfoDTO.BusinessOperationUserDTO::getDepartmentId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
                }
                if (1 == deptIds.size() && CharSequenceUtil.isNotBlank(deptIds.get(0))){
                    this.lambdaUpdate().eq(CustomerInfoEntity::getId,customerInfoEntity.getId()).set(CustomerInfoEntity::getSalesDeptId, deptIds.get(0)).update();
                }
            });

        }

    }

    @Override
    public VirtualWarehouseDTO.VwDTO getVirtualWarehouseByCustomerId(CustomerDTO.VirtualDTO dto) {
        CustomerInfoEntity customerInfoEntity = this.getById(dto.getCustomerId());
        if (null == customerInfoEntity){
            return new VirtualWarehouseDTO.VwDTO();
        }
        if(StringUtils.isNotBlank(customerInfoEntity.getCountryId()) ){
            String country = customerInfoEntity.getCountryId();
            String partitionId = sysPartitionFeign.getPartitionByCountry(country);
            VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
            platformDTO.setDictPlatform(customerInfoEntity.getPlatformType());
            platformDTO.setWarehouseIdList(Arrays.asList(dto.getWarehouseId()));
            platformDTO.setRelationId("");
            platformDTO.setPartitionId(partitionId);
            List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);
            if (CollectionUtils.isEmpty(virtualWarehouseList)) {
                return new VirtualWarehouseDTO.VwDTO();
            }
            VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = virtualWarehouseList.get(0);
            List<VirtualWarehouseEntity> virtualWarehouseEntities = wmsVirtualWarehouseFeign.listByIds(Arrays.asList(virtualWarehouseRelationEntity.getVirtualWarehouseId()));
            if(CollectionUtils.isNotEmpty(virtualWarehouseEntities)){
                VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntities.get(0);
                return new VirtualWarehouseDTO.VwDTO(virtualWarehouseEntity.getId(),virtualWarehouseEntity.getDisabled(),virtualWarehouseEntity.getCode(),virtualWarehouseEntity.getName());
            }
        }
        return new VirtualWarehouseDTO.VwDTO();
    }

    @Override
    public List<CustomerInfoEntity> listByCodes(List<String> list) {
         return this.list(new QueryWrapper<CustomerInfoEntity>().lambda().in(CustomerInfoEntity::getCode, list).eq(CustomerInfoEntity::getIsDeleted, Boolean.FALSE));
    }

    @Override
    public void updateApproveStatus(CustomerInfoEntity entity) {
        this.updateById(entity);
    }

    @Override
    public CustomerDTO.ThirdCustomerAccountDTO getThirdCustomerAccount(BaseIdDTO dto) {
        CustomerInfoEntity entity = this.getById(dto.getId());
        return dhtService.queryCustomerAccountByCustomerCode(entity);
    }

    @Override
    public Boolean isSyncDht(String customerId) {
        if(StringUtils.isBlank(customerId)){
            return false;
        }
        CfgSettingEntity cfgSetting = cfgSettingService.getSettingByKey(CfgSettingEnum.DHT_CUSTOMER_WHITELIST.getCode());
        if(cfgSetting != null && StringUtils.isNotBlank(cfgSetting.getValue())){
            List<String> whitelist = Arrays.asList(cfgSetting.getValue().split(","));
            if(whitelist.contains(customerId)){
                return true;
            }
        }
        return false;
    }
}

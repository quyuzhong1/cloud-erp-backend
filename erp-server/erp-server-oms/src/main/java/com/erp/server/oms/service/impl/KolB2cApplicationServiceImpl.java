package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.dto.KolB2cApplicationAddressDTO;
import com.erp.model.oms.dto.KolB2cApplicationDetailDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationAddressImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationDetailImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationImportExcelDTO;
import com.erp.model.oms.dto.excel.KolPartnerInfoImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgKolOptionTypeEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationDeliveryStatusEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationOrderStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.oms.listener.KolB2cApplicationAddressExcelListener;
import com.erp.server.oms.listener.KolB2cApplicationDetailExcelListener;
import com.erp.server.oms.listener.KolB2cApplicationExcelListener;
import com.erp.server.oms.listener.KolPartnerInfoExcelListener;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.oms.mapper.KolB2cApplicationMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * B2C寄样申请单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolB2cApplicationServiceImpl extends SuperServiceImpl<KolB2cApplicationMapper, KolB2cApplicationEntity> implements KolB2cApplicationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private CfgKolOptionService cfgKolOptionService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private KolB2cApplicationDetailService kolB2cApplicationDetailService;
    @Resource
    private KolB2cApplicationAddressService kolB2cApplicationAddressService;
    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;
    @Resource
    private KolSubB2cApplicationDetailService kolSubB2cApplicationDetailService;
    @Resource
    private CommonService commonService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private KolPartnerInfoService kolPartnerInfoService;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolB2cApplicationDTO.AddDTO addDTO) {
        handleAddData(addDTO);

        KolB2cApplicationEntity kolB2cApplicationEntity = new KolB2cApplicationEntity();
        BeanMapperUtils.copy(addDTO, kolB2cApplicationEntity);

        log.info("开始新增B2C寄样申请单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KOLC);
        kolB2cApplicationEntity.setCode(code);
        boolean save = super.save(kolB2cApplicationEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C寄样申请单" , kolB2cApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), kolB2cApplicationEntity.getId(), "新增操作");
        String id = kolB2cApplicationEntity.getId();
        List<KolB2cApplicationDetailEntity> kolB2cApplicationDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), KolB2cApplicationDetailEntity.class);
        kolB2cApplicationDetailEntities.forEach(e -> e.setMainId(id));
        kolB2cApplicationDetailService.saveBatch(kolB2cApplicationDetailEntities);

        List<KolB2cApplicationAddressEntity> kolB2cApplicationAddressEntities = BeanMapper.copyList(addDTO.getAddressList(), KolB2cApplicationAddressEntity.class);
        kolB2cApplicationAddressEntities.forEach(e -> e.setMainId(id));
        kolB2cApplicationAddressService.saveBatch(kolB2cApplicationAddressEntities);

        return new BaseResultDTO.AddDTO(kolB2cApplicationEntity.getId(), code);
    }

    private void handleAddData(KolB2cApplicationDTO.AddDTO addDTO) {
        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(addDTO.getShopId());
        if(Objects.isNull(shopInfoEntity)){
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "店铺");
        }else{
            addDTO.setShopName(shopInfoEntity.getName());
        }

        //仓库
        if(StringUtils.isNotBlank(addDTO.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Arrays.asList(addDTO.getWarehouseId()));
            if(CollUtil.isEmpty(updateDTOS)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "仓库");
            }else {
                addDTO.setWarehouseName(updateDTOS.get(0).getName());
            }
        }

        //物流渠道
        if(StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
            LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(addDTO.getLogisticsChannelId());
            if(Objects.isNull(logisticsChannelEntity)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "物流渠道");
            }else {
                addDTO.setLogisticsChannelName(logisticsChannelEntity.getName());
            }
        }
        //申请人
        String applyUserId = addDTO.getApplyUserId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(applyUserId);
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "申请人");
        }else {
            addDTO.setApplyUserName(findUserDTO.getUserName());

            //申请部门
            String applyDeptId = addDTO.getApplyDeptId();
            if(StringUtils.isNotBlank(applyDeptId)) {
                if(Objects.equals(applyDeptId,findUserDTO.getDepartmentId())){
                    addDTO.setApplyDeptName(findUserDTO.getDepartmentName());
                }else {
                    List<SysDepartmentEntity> depts = sysUserFeign.getDeptByIds(Arrays.asList(applyDeptId));
                    if (CollUtil.isEmpty(depts)) {
                        throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "申请部门");
                    } else {
                        addDTO.setLogisticsChannelName(depts.get(0).getName());
                    }
                }
            }
        }

        List<KolB2cApplicationDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_1041,"B2C寄样申请单");
        }

        List<KolB2cApplicationAddressDTO.AddDTO> addressList = addDTO.getAddressList();
        if(CollUtil.isEmpty(addressList)){
            throw new ServiceException(ApiError.ERROR_1041,"B2C寄样申请单地址");
        }

        List<String> skuIds = detailList.stream().map(KolB2cApplicationDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        List<String> partnerIds = detailList.stream().map(KolB2cApplicationDetailDTO.AddDTO::getPartnerId).distinct().collect(Collectors.toList());
        List<KolPartnerInfoEntity> partnerList = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, partnerIds).eq(KolPartnerInfoEntity::getDisabled, false).list();
        Map<String, String> partnerMap = partnerList.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, KolPartnerInfoEntity::getNickname));

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        int index = 1;
        for (KolB2cApplicationDetailDTO.AddDTO detail : detailList) {
            String nickName = partnerMap.get(detail.getPartnerId());
            if(StringUtils.isBlank(nickName)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "第"+index+"行达人");
            }

            SkuVO skuVO = skuMap.get(detail.getSkuId());
            if(Objects.isNull(skuVO)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "第"+index+"行SKU");
            }

            detail.setSkuNo(skuVO.getSkuNo());

            detail.setBrandId(skuVO.getBrandId());

            detail.setBrandName(skuVO.getBrandName());

            List<String> projectTagList = detail.getProjectTagList();
            if(CollUtil.isNotEmpty(projectTagList)){
                String projectTag = projectTagList.stream().collect(Collectors.joining(","));
                detail.setProjectTag(projectTag);

                String projectTagName = projectTagList.stream().filter(StringUtils::isNotBlank).map(map::get).filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
                detail.setProjectTagName(projectTagName);
            }

            index+=1;
        }

        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId));

        for (KolB2cApplicationAddressDTO.AddDTO address : addressList) {
            address.setCountryName(dictCountryMap.get(address.getCountryName()));
        }
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolB2cApplicationDTO.UpdateDTO addOrUpdateDTO) {
        KolB2cApplicationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C寄样申请单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        handleUpdateData(addOrUpdateDTO);

        KolB2cApplicationEntity kolB2cApplicationEntity =  BeanMapperUtils.map(KolB2cApplicationEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改B2C寄样申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kolB2cApplicationEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录B2C寄样申请单日志数据，单号：【{}】", kolB2cApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolB2cApplicationEntity.getCode(), "B2C寄样申请单");
        operateLogService.addModuleOperateLogByObj(old, kolB2cApplicationEntity, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), kolB2cApplicationEntity.getId(), msg);

        String id = kolB2cApplicationEntity.getId();
        List<KolB2cApplicationDetailEntity> kolB2cApplicationDetailEntities = BeanMapper.copyList(addOrUpdateDTO.getDetailList(), KolB2cApplicationDetailEntity.class);
        kolB2cApplicationDetailEntities.forEach(e -> e.setMainId(id));
        List<KolB2cApplicationDetailEntity> oldKolB2cApplicationDetailEntities = kolB2cApplicationDetailService.lambdaQuery().eq(KolB2cApplicationDetailEntity::getMainId, id).list();
        commonService.updateDetail(id,ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(),kolB2cApplicationDetailService,  kolB2cApplicationDetailEntities, oldKolB2cApplicationDetailEntities,"skuNo");

        List<KolB2cApplicationAddressEntity> kolB2cApplicationAddressEntities = BeanMapper.copyList(addOrUpdateDTO.getAddressList(), KolB2cApplicationAddressEntity.class);
        kolB2cApplicationAddressEntities.forEach(e -> e.setMainId(id));
        List<KolB2cApplicationAddressEntity> oldKolB2cApplicationAddressEntities = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getMainId, id).list();
        commonService.updateDetail(id,ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(),kolB2cApplicationAddressService,  kolB2cApplicationAddressEntities, oldKolB2cApplicationAddressEntities,"nickname");
        return Boolean.TRUE;
    }

    private void handleUpdateData(KolB2cApplicationDTO.UpdateDTO addDTO) {
        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(addDTO.getShopId());
        if(Objects.isNull(shopInfoEntity)){
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "店铺");
        }else{
            addDTO.setShopName(shopInfoEntity.getName());
        }

        //仓库
        if(StringUtils.isNotBlank(addDTO.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Arrays.asList(addDTO.getWarehouseId()));
            if(CollUtil.isEmpty(updateDTOS)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "仓库");
            }else {
                addDTO.setWarehouseName(updateDTOS.get(0).getName());
            }
        }

        //物流渠道
        if(StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
            LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(addDTO.getLogisticsChannelId());
            if(Objects.isNull(logisticsChannelEntity)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "物流渠道");
            }else {
                addDTO.setLogisticsChannelName(logisticsChannelEntity.getName());
            }
        }
        //申请人
        String applyUserId = addDTO.getApplyUserId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(applyUserId);
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "申请人");
        }else {
            addDTO.setApplyUserName(findUserDTO.getUserName());

            //申请部门
            String applyDeptId = addDTO.getApplyDeptId();
            if(StringUtils.isNotBlank(applyDeptId)) {
                if(Objects.equals(applyDeptId,findUserDTO.getDepartmentId())){
                    addDTO.setApplyDeptName(findUserDTO.getDepartmentName());
                }else {
                    List<SysDepartmentEntity> depts = sysUserFeign.getDeptByIds(Arrays.asList(applyDeptId));
                    if (CollUtil.isEmpty(depts)) {
                        throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "申请部门");
                    } else {
                        addDTO.setLogisticsChannelName(depts.get(0).getName());
                    }
                }
            }
        }

        List<KolB2cApplicationDetailDTO.UpdateDTO> detailList = addDTO.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_1041,"B2C寄样申请单");
        }

        List<KolB2cApplicationAddressDTO.UpdateDTO> addressList = addDTO.getAddressList();
        if(CollUtil.isEmpty(addressList)){
            throw new ServiceException(ApiError.ERROR_1041,"B2C寄样申请单地址");
        }

        List<String> skuIds = detailList.stream().map(KolB2cApplicationDetailDTO.UpdateDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        List<String> partnerIds = detailList.stream().map(KolB2cApplicationDetailDTO.UpdateDTO::getPartnerId).distinct().collect(Collectors.toList());
        List<KolPartnerInfoEntity> partnerList = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, partnerIds).eq(KolPartnerInfoEntity::getDisabled, false).list();
        Map<String, String> partnerMap = partnerList.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, KolPartnerInfoEntity::getNickname));

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        int index = 1;
        for (KolB2cApplicationDetailDTO.UpdateDTO detail : detailList) {
            String nickName = partnerMap.get(detail.getPartnerId());
            if(StringUtils.isBlank(nickName)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "第"+index+"行达人");
            }

            SkuVO skuVO = skuMap.get(detail.getSkuId());
            if(Objects.isNull(skuVO)){
                throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "第"+index+"行SKU");
            }

            detail.setSkuNo(skuVO.getSkuNo());

            detail.setBrandId(skuVO.getBrandId());

            detail.setBrandName(skuVO.getBrandName());

            List<String> projectTagList = detail.getProjectTagList();
            if(CollUtil.isNotEmpty(projectTagList)){
                String projectTag = projectTagList.stream().collect(Collectors.joining(","));
                detail.setProjectTag(projectTag);

                String projectTagName = projectTagList.stream().filter(StringUtils::isNotBlank).map(map::get).filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
                detail.setProjectTagName(projectTagName);
            }

            index+=1;
        }

        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId));

        for (KolB2cApplicationAddressDTO.UpdateDTO address : addressList) {
            address.setCountryName(dictCountryMap.get(address.getCountryName()));
        }
    }


    @Override
    public PagingVO<KolB2cApplicationDTO.ListDTO> paging(PagingDTO<KolB2cApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolB2cApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<KolB2cApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolB2cApplicationDTO.PagingParamDTO searchParam = new KolB2cApplicationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        //待我审核
        //根据单据id查询审核流程
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            searchParam.setIds(ids);
        }else {
            searchParam.setIds(Arrays.asList("-1"));
        }
        List<KolB2cApplicationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        list.add(new KolB2cApplicationDTO.TabListDTO("all","全部",0));
        return list;
    }

    @Override
    public void exportList(KolB2cApplicationDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("B2C寄样申请单导出", EXPORT_OMS_KOL_B2C_APPLICATION.getCode(), param);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        KolB2cApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2C寄样申请单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2C寄样申请单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2C寄样申请单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录B2C寄样申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(KolB2cApplicationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(KolB2cApplicationDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        KolB2cApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(KolB2cApplicationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(KolB2cApplicationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException("只有待提交数据支持删除");
        }

        //删除明细
        kolB2cApplicationDetailService.lambdaUpdate()
                .set(KolB2cApplicationDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(KolB2cApplicationDetailEntity::getMainId, id)
                .update();

        kolB2cApplicationAddressService.lambdaUpdate()
                .set(KolB2cApplicationAddressEntity::getIsDeleted, Boolean.TRUE)
                .eq(KolB2cApplicationAddressEntity::getMainId, id)
                .update();

        List<KolSubB2cApplicationEntity> subList = kolSubB2cApplicationService.lambdaQuery().eq(KolSubB2cApplicationEntity::getSourceId, id).list();
        if(CollUtil.isNotEmpty(subList)){
            kolSubB2cApplicationService.lambdaUpdate()
                    .set(KolSubB2cApplicationEntity::getIsDeleted, Boolean.TRUE)
                    .eq(KolSubB2cApplicationEntity::getSourceId, id)
                    .update();

            kolSubB2cApplicationDetailService.lambdaUpdate()
                    .set(KolSubB2cApplicationDetailEntity::getIsDeleted, Boolean.TRUE)
                    .eq(KolSubB2cApplicationDetailEntity::getMainId, id)
                    .update();
        }
        // 删除主单数据
        log.info("删除 开始删除B2C寄样申请单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除B2C寄样申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getCode(), "删除B2C寄样申请单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        // 只有待提交、审核不通过数据允许作废
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改B2C寄样申请单状态数据，id：【{}】", id);
        lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
            .set(KolB2cApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(KolB2cApplicationEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改B2C寄样申请单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, KolB2cApplicationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        return Boolean.TRUE;
    }


    @Override
    public KolB2cApplicationDTO.ViewDTO view(String id) {
        KolB2cApplicationEntity kolB2cApplicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2C寄样申请单数据"));
        KolB2cApplicationDTO.ViewDTO data = BeanMapperUtils.map(KolB2cApplicationDTO.ViewDTO.class, kolB2cApplicationEntity);


        List<KolB2cApplicationDetailEntity> detailList = kolB2cApplicationDetailService.lambdaQuery().eq(KolB2cApplicationDetailEntity::getMainId, id).list();
        List<KolB2cApplicationDetailDTO.UpdateDTO> viewDetailList = BeanMapper.copyList(detailList, KolB2cApplicationDetailDTO.UpdateDTO.class);
        data.setDetailList(viewDetailList);

        List<KolB2cApplicationAddressEntity> addressList = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getMainId, id).list();
        List<KolB2cApplicationAddressDTO.UpdateDTO> addressViewList = BeanMapper.copyList(addressList, KolB2cApplicationAddressDTO.UpdateDTO.class);
        data.setAddressList(addressViewList);

        // 数据填充处理
        fillOne(data);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(KolB2cApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 根据业务实体获取变量映射表
     * @param entity 业务实体对象，用于转换为变量映射
     * @return 变量映射表，包含业务相关的配置变量
     */
    private Map<String,Object> getVariablesMap(KolB2cApplicationEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.KOL_B2C_APPLICATION.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);

        List<KolB2cApplicationDetailEntity> detailList = kolB2cApplicationDetailService.lambdaQuery().eq(KolB2cApplicationDetailEntity::getMainId, entity.getId()).list();
        map.put("detailList", detailList);
        List<KolB2cApplicationAddressEntity> addressList = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getMainId, entity.getId()).list();
        map.put("addressList", addressList);
        return map;
    }

    private void fillOne(KolB2cApplicationDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        List<DictCurrencyEntity> dictCurrencyEntities = sysUserFeign.currencyList();
        Map<String, String> currencyMap = dictCurrencyEntities.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getName));

        List<KolB2cApplicationDetailDTO.UpdateDTO> detailList = data.getDetailList();
        List<String> skuIds = detailList.stream().map(KolB2cApplicationDetailDTO.UpdateDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));

        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        data.setSampleTypeName(map.get(data.getSampleType()));
        data.setIsInternationalName(getIsInternationalName(data.getIsInternational()));
        data.setCurrencyName(currencyMap.get(data.getCurrency()));

        // 属性赋值
        for(KolB2cApplicationDetailDTO.UpdateDTO detailData : detailList) {
            detailData.setProductName(skuMap.get(detailData.getSkuId()));

            if(StringUtils.isNotBlank(detailData.getProjectTag())){
                String projectTagName = Arrays.stream(detailData.getProjectTag().split(",")).map(skuMap::get).collect(Collectors.joining(","));
                detailData.setProjectTagName(projectTagName);

                detailData.setProjectTagList(Arrays.asList(detailData.getProjectTag().split(",")));
            }
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
            .set(KolB2cApplicationEntity::getApproveUserId, userInfo.getUid())
            .set(KolB2cApplicationEntity::getApproveUserName, userInfo.getUserName())
            .set(KolB2cApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2cApplicationEntity::getApproveTime, LocalDateTime.now())
            .update(new KolB2cApplicationEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
            .set(KolB2cApplicationEntity::getApproveUserId, "")
            .set(KolB2cApplicationEntity::getApproveUserName, "")
            .set(KolB2cApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2cApplicationEntity::getApproveTime, null)
            .update(new KolB2cApplicationEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
        .set(KolB2cApplicationEntity::getApproveStatus, approveStatus)
        .update(new KolB2cApplicationEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<KolB2cApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        List<DictCurrencyEntity> dictCurrencyEntities = sysUserFeign.currencyList();
        Map<String, String> currencyMap = dictCurrencyEntities.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getName));

        List<String> skuIds = list.stream().map(KolB2cApplicationDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));

        // 属性赋值
        for(KolB2cApplicationDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setSampleTypeName(map.get(data.getSampleType()));
            data.setIsInternationalName(getIsInternationalName(data.getIsInternational()));
            data.setCurrencyName(currencyMap.get(data.getCurrency()));
            data.setOrderStatusName(KolSubB2cApplicationOrderStatusEnum.getName(data.getOrderStatus()));
            data.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.getName(data.getDeliveryStatus()));
            data.setProductName(skuMap.get(data.getSkuId()));

            if(StringUtils.isNotBlank(data.getProjectTag())){
                String projectTagName = Arrays.stream(data.getProjectTag().split(",")).map(skuMap::get).collect(Collectors.joining(","));
                data.setProjectTagName(projectTagName);
            }
        }
    }

    private String getIsInternationalName(Boolean isInternational) {
        return Boolean.TRUE.equals(isInternational) ? "国外" : "国内";
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(KolB2cApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }
    @Override
    public KolB2cApplicationDTO.DetailViewDTO detailView(List<String> detailIdList) {
        return null;
    }

    @Override
    public Boolean generateReturnPiece(List<KolB2cApplicationDTO.DetailViewDTO> list) {
        return null;
    }



    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入B2C寄样申请", IMPORT_OMS_KOL_B2C_APPLICATION.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void importKolB2cApplication(BaseDTO.ImportDTO dto) {
        //设置操作人
        if(StringUtils.isNotBlank(dto.getUserId())){
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getUserId());
            if(Objects.nonNull(findUserDTO)){
                LoginUser user = new LoginUser();
                user.setUid(findUserDTO.getUserId());
                user.setUserName(findUserDTO.getUserName());
                user.setRealName(findUserDTO.getRealName());
                user.setUserAccount(findUserDTO.getMobile());
                user.setMobile(findUserDTO.getMobile());
                UserContext.setLoginUser(user);
            }
        }
        //店铺
        List<ShopInfoEntity> shopInfoEntities = shopInfoService.listAuth(null);
        Map<String, String> shopMap = shopInfoEntities.stream().collect(Collectors.toMap(ShopInfoEntity::getName, ShopInfoEntity::getId));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, FindUserDTO> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserName, Function.identity()));
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        Map<String, String> deptMap = deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getName, SysDepartmentDTO::getId));
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouserList = wmsTaskFeign.listApproveWarehouse();
        Map<String, String> warehouserMap = warehouserList.stream().collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getName, WarehouseDTO.UpdateDTO::getId));
        //物流渠道
        List<BaseDropDownDTO.DisabledDTO> logisticsList = logisticsFeign.listAll();
        Map<String, String> logisticsMap = logisticsList.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode));
        //sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity()));
        //达人
        List<KolPartnerInfoEntity> partnerList = kolPartnerInfoService.lambdaQuery().eq(KolPartnerInfoEntity::getDisabled, false).list();
        Map<String, String> partnerMap = partnerList.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getNickname, KolPartnerInfoEntity::getId));
        //字典
        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> cfgKolOptionMap = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getName, CfgKolOptionEntity::getId));
        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId));
        //币种
        List<DictCurrencyEntity> dictCurrencyEntities = sysUserFeign.currencyList();
        Map<String, String> currencyMap = dictCurrencyEntities.stream().collect(Collectors.toMap(DictCurrencyEntity::getName, DictCurrencyEntity::getId));

        KolB2cApplicationExcelListener excelListenerUtil = new KolB2cApplicationExcelListener(dto.getTaskId(),
                dto.getImportType(),
                dto.getImportCount(),
                cfgKolOptionMap,
                shopMap,
                userMap,
                deptMap,
                warehouserMap,
                logisticsMap,
                currencyMap
        );

        KolB2cApplicationDetailExcelListener detailExcelListenerUtil = new KolB2cApplicationDetailExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(), skuMap,partnerMap,cfgKolOptionMap);

        KolB2cApplicationAddressExcelListener addressListenerUtil = new KolB2cApplicationAddressExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),partnerMap,dictCountryMap);

        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());

            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationDetailImportExcelDTO.class, detailExcelListenerUtil).sheet(1).doRead();
            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationAddressImportExcelDTO.class, addressListenerUtil).sheet(2).doRead();

            List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList = detailExcelListenerUtil.getSuccessList();
            List<KolB2cApplicationDetailImportExcelDTO> detailErrorList = detailExcelListenerUtil.getErrorList();

            List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList = addressListenerUtil.getSuccessList();
            List<KolB2cApplicationAddressImportExcelDTO> addressErrorList = addressListenerUtil.getErrorList();

            List<KolB2cApplicationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            List<KolB2cApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();

            List<String> errorNoList = errorList.stream().map(KolB2cApplicationImportExcelDTO::getNo).distinct().collect(Collectors.toList());

            List<KolB2cApplicationDetailImportExcelDTO> error1 = detailSuccessList.stream().filter(e -> errorNoList.contains(e.getNo())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(error1)){
                detailErrorList.addAll(error1);

                detailSuccessList = detailSuccessList.stream().filter(e -> !errorNoList.contains(e.getNo())).collect(Collectors.toList());
            }

            List<KolB2cApplicationAddressImportExcelDTO> error2 = addressSuccessList.stream().filter(e -> errorNoList.contains(e.getNo())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(error2)){
                addressErrorList.addAll(error2);

                addressSuccessList = addressSuccessList.stream().filter(e -> !errorNoList.contains(e.getNo())).collect(Collectors.toList());
            }

            KolB2cApplicationService kolB2cApplicationService = SpringUtil.getBean(KolB2cApplicationService.class);
            kolB2cApplicationService.handleImportSuccessList(successList, errorList, detailSuccessList, addressSuccessList,dto.getImportType());

        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<KolB2cApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "B2C寄样申请错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, KolB2cApplicationImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public List<KolB2cApplicationImportExcelDTO> handleImportSuccessList(List<KolB2cApplicationImportExcelDTO> successList,
                                        List<KolB2cApplicationImportExcelDTO> errorList,
                                        List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList,
                                        List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList ,
                                        String importType) {

        if(CollUtil.isNotEmpty(successList)){

            KolB2cApplicationService kolB2cApplicationService = SpringUtil.getBean(KolB2cApplicationService.class);

            Map<String, List<KolB2cApplicationDetailImportExcelDTO>> detailGroup = detailSuccessList.stream().collect(Collectors.groupingBy(KolB2cApplicationDetailImportExcelDTO::getNo));

            Map<String, List<KolB2cApplicationAddressImportExcelDTO>> addressGroup = addressSuccessList.stream().collect(Collectors.groupingBy(KolB2cApplicationAddressImportExcelDTO::getNo));

            for (KolB2cApplicationImportExcelDTO kolB2cApplicationImportExcelDTO : successList) {
                String no = kolB2cApplicationImportExcelDTO.getNo();

                List<KolB2cApplicationDetailImportExcelDTO> kolB2cApplicationDetailImportExcelDTOS = detailGroup.get(no);
                if(CollUtil.isEmpty(kolB2cApplicationDetailImportExcelDTOS)){
                    kolB2cApplicationImportExcelDTO.setErrorMsg("1、产品明细异常；");
                    errorList.add(kolB2cApplicationImportExcelDTO);
                    continue;
                }

                List<KolB2cApplicationAddressImportExcelDTO> kolB2cApplicationAddressImportExcelDTOS = addressGroup.get(no);
                if(CollUtil.isEmpty(kolB2cApplicationAddressImportExcelDTOS)){
                    kolB2cApplicationImportExcelDTO.setErrorMsg("1、地址明细异常；");
                    errorList.add(kolB2cApplicationImportExcelDTO);
                    continue;
                }

                KolB2cApplicationDTO.AddDTO addDTO = new KolB2cApplicationDTO.AddDTO();
                BeanMapper.copy(kolB2cApplicationImportExcelDTO,addDTO);

                List<KolB2cApplicationDetailDTO.AddDTO> detailList = BeanMapper.copyList(kolB2cApplicationDetailImportExcelDTOS, KolB2cApplicationDetailDTO.AddDTO.class);
                addDTO.setDetailList(detailList);

                List<KolB2cApplicationAddressDTO.AddDTO> addressList = BeanMapper.copyList(kolB2cApplicationAddressImportExcelDTOS, KolB2cApplicationAddressDTO.AddDTO.class);
                addDTO.setAddressList(addressList);

                kolB2cApplicationService.add(addDTO);
            }
        }

        return errorList;
    }
}

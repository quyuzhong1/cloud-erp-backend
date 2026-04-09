package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.dto.MultiErrorExcelData;
import com.common.core.enums.ApiError;
import com.common.core.enums.DictCityTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.KolB2cApplicationAddressImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationDetailImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.oms.listener.KolB2cApplicationAddressExcelListener;
import com.erp.server.oms.listener.KolB2cApplicationDetailExcelListener;
import com.erp.server.oms.listener.KolB2cApplicationExcelListener;
import com.erp.server.oms.mapper.KolB2cApplicationMapper;
import com.erp.server.oms.rocketmq.sync.wangdian.SyncWangDianSoB2cService;
import com.erp.server.oms.service.address.AddressParseService;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_B2C_APPLICATION;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_OMS_KOL_B2C_APPLICATION;

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
    @Resource
    private KolFeedbackService kolFeedbackService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SyncWangDianSoB2cService syncWangDianSoB2cService;
    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SoB2cRuleService soB2cRuleService;
    @Resource
    private AddressParseService addressParseService;

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
        kolB2cApplicationEntity.setBillStatus(KolB2cApplicationDocumentStatusEnum.WAIT.getCode());
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

        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        //省市区
        List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
        Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
        Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1, o2)->o1));
        Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
        Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));

        List<KolB2cApplicationAddressEntity> kolB2cApplicationAddressEntities = BeanMapper.copyList(addDTO.getAddressList(), KolB2cApplicationAddressEntity.class);
        for (KolB2cApplicationAddressEntity entity : kolB2cApplicationAddressEntities) {
            entity.setMainId(id);
            checkAndSetAddress(entity, dictCountryMap, provinceMap, cityMap, districtMap);
        }
        kolB2cApplicationAddressService.saveBatch(kolB2cApplicationAddressEntities);

        return new BaseResultDTO.AddDTO(kolB2cApplicationEntity.getId(), code);
    }


    //校验并设置国家省市区
    private static void checkAndSetAddress(KolB2cApplicationAddressEntity KolB2cApplicationAddressEntity, Map<String, String> dictCountryMap, Map<String, String> provinceMap, Map<String, String> cityMap, Map<String, String> districtMap) {
        //国家
        String countryName = dictCountryMap.getOrDefault(KolB2cApplicationAddressEntity.getCountryId(), "");
        if(StringUtils.isNotBlank(countryName)){
            KolB2cApplicationAddressEntity.setCountryName(countryName);
        }else {
            throw new ServiceException("国家名称不存在");
        }
        if(KolB2cApplicationAddressEntity.getCountryId().equals(DictValueEnum.CN.getCode())){
            //省
            if(StringUtils.isBlank(KolB2cApplicationAddressEntity.getProvinceId())){
                throw new ServiceException("省不能为空");
            }
            String province = provinceMap.getOrDefault(KolB2cApplicationAddressEntity.getProvinceId(), "");
            if(StringUtils.isNotBlank(province)){
                KolB2cApplicationAddressEntity.setProvince(province);
            }else {
                throw new ServiceException("省不存在");
            }
            //市
            if(StringUtils.isBlank(KolB2cApplicationAddressEntity.getCityId())){
                throw new ServiceException("市不能为空");
            }
            String city = cityMap.getOrDefault(KolB2cApplicationAddressEntity.getCityId(), "");
            if(StringUtils.isNotBlank(city)){
                KolB2cApplicationAddressEntity.setCity(city);
            }else {
                throw new ServiceException("市不存在");
            }
            //区域
            if(StringUtils.isNotBlank(KolB2cApplicationAddressEntity.getDistrictId())){
                String district = districtMap.getOrDefault(KolB2cApplicationAddressEntity.getDistrictId(), "");
                if(StringUtils.isNotBlank(district)){
                    KolB2cApplicationAddressEntity.setDistrict(district);
                }else {
                    throw new ServiceException("区域不存在");
                }
            }else {
                throw new ServiceException("国家为中国大陆则区域不能为空");
            }
        }else{
            //省
            if(StringUtils.isBlank(KolB2cApplicationAddressEntity.getProvince())){
                throw new ServiceException("省不能为空");
            }
            //市
            if(StringUtils.isBlank(KolB2cApplicationAddressEntity.getCity())){
                throw new ServiceException("市不能为空");
            }
        }
    }

    private void handleAddData(KolB2cApplicationDTO.AddDTO addDTO) {
        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(addDTO.getShopId());
        if(Objects.isNull(shopInfoEntity)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "店铺");
        }else{
            addDTO.setShopName(shopInfoEntity.getName());
        }

        //仓库
        if(StringUtils.isNotBlank(addDTO.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Arrays.asList(addDTO.getWarehouseId()));
            if(CollUtil.isEmpty(updateDTOS)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓库");
            }else {
                addDTO.setWarehouseName(updateDTOS.get(0).getName());
            }
        }

        //物流渠道
        if(StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
            LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(addDTO.getLogisticsChannelId());
            if(Objects.isNull(logisticsChannelEntity)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "物流渠道");
            }else {
                addDTO.setLogisticsChannelName(logisticsChannelEntity.getName());
            }
        }
        //申请人
        String applyUserId = addDTO.getApplyUserId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(applyUserId);
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "申请人");
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
                        throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "申请部门");
                    } else {
                        addDTO.setApplyDeptName(depts.get(0).getName());
                    }
                }
            }
        }

        List<KolB2cApplicationDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED,"B2C寄样申请单");
        }

        List<KolB2cApplicationAddressDTO.AddDTO> addressList = addDTO.getAddressList();
        if(CollUtil.isEmpty(addressList)){
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED,"B2C寄样申请单地址");
        }
        // 校验 partnerId 是否存在重复
        Set<String> partnerIdSet = new HashSet<>();
        for (KolB2cApplicationAddressDTO.AddDTO address : addressList) {
            if (!partnerIdSet.add(address.getPartnerId())) {
                throw new ServiceException("存在重复的达人地址：" + address.getNickname());
            }
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
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "第"+index+"行达人");
            }
            detail.setNickname(nickName);
            SkuVO skuVO = skuMap.get(detail.getSkuId());
            if(Objects.isNull(skuVO)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "第"+index+"行SKU");
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
            address.setNickname(partnerMap.get(address.getPartnerId()));
            address.setCountryName(dictCountryMap.get(address.getCountryId()));
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2C寄样申请单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        if(old.getInvalidStatus()){
            throw new ServiceException(ApiError.BILL_VOID_EDIT_FORBIDDEN);
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


        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        //省市区
        List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
        Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
        Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1, o2)->o1));
        Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
        Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));

        List<KolB2cApplicationAddressEntity> kolB2cApplicationAddressEntities = BeanMapper.copyList(addOrUpdateDTO.getAddressList(), KolB2cApplicationAddressEntity.class);
        for (KolB2cApplicationAddressEntity entity : kolB2cApplicationAddressEntities) {
            entity.setMainId(id);
            checkAndSetAddress(entity, dictCountryMap, provinceMap, cityMap, districtMap);

        }
        List<KolB2cApplicationAddressEntity> oldKolB2cApplicationAddressEntities = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getMainId, id).list();
        commonService.updateDetail(id,ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(),kolB2cApplicationAddressService,  kolB2cApplicationAddressEntities, oldKolB2cApplicationAddressEntities,"nickname");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateDetailRemark(String id, String detailId, String remark) {
        KolB2cApplicationEntity entity = super.getById(id);
        entity = Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2C寄样申请单"));

        KolB2cApplicationDetailEntity detailEntity = kolB2cApplicationDetailService.lambdaQuery()
                .eq(KolB2cApplicationDetailEntity::getId, detailId)
                .eq(KolB2cApplicationDetailEntity::getMainId, id)
                .one();
        detailEntity = Optional.ofNullable(detailEntity).orElseThrow(() -> new ServiceException("B2C寄样申请明细不存在"));

        String newRemark = StrUtil.nullToEmpty(remark);
        if (Objects.equals(detailEntity.getRemark(), newRemark)) {
            return Boolean.TRUE;
        }

        KolB2cApplicationDetailEntity oldDetail = BeanMapperUtils.map(KolB2cApplicationDetailEntity.class, detailEntity);
        detailEntity.setRemark(newRemark);
        boolean update = kolB2cApplicationDetailService.updateById(detailEntity);
        if (!update) {
            throw new ServiceException("B2C寄样申请明细备注更新失败");
        }

        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的明细【{}】备注，由[{}]变更为[{}]",
                UserContext.getDefaultLoginUser().getUserName(),
                entity.getCode(),
                StrUtil.blankToDefault(detailEntity.getSkuNo(), detailId),
                formatOperateLogValue(oldDetail.getRemark()),
                formatOperateLogValue(newRemark));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), id, "编辑信息");
        return Boolean.TRUE;
    }

    private String formatOperateLogValue(String value) {
        return StringUtils.isBlank(value) ? "空值" : value;
    }

    private void handleUpdateData(KolB2cApplicationDTO.UpdateDTO addDTO) {
        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(addDTO.getShopId());
        if(Objects.isNull(shopInfoEntity)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "店铺");
        }else{
            addDTO.setShopName(shopInfoEntity.getName());
        }

        //仓库
        if(StringUtils.isNotBlank(addDTO.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Arrays.asList(addDTO.getWarehouseId()));
            if(CollUtil.isEmpty(updateDTOS)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓库");
            }else {
                addDTO.setWarehouseName(updateDTOS.get(0).getName());
            }
        }

        //物流渠道
        if(StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
            LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(addDTO.getLogisticsChannelId());
            if(Objects.isNull(logisticsChannelEntity)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "物流渠道");
            }else {
                addDTO.setLogisticsChannelName(logisticsChannelEntity.getName());
            }
        }
        //申请人
        String applyUserId = addDTO.getApplyUserId();
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(applyUserId);
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "申请人");
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
                        throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "申请部门");
                    } else {
                        addDTO.setLogisticsChannelName(depts.get(0).getName());
                    }
                }
            }
        }

        List<KolB2cApplicationDetailDTO.UpdateDTO> detailList = addDTO.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED,"B2C寄样申请单");
        }

        List<KolB2cApplicationAddressDTO.UpdateDTO> addressList = addDTO.getAddressList();
        if(CollUtil.isEmpty(addressList)){
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED,"B2C寄样申请单地址");
        }
        // 校验 partnerId 是否存在重复
        Set<String> partnerIdSet = new HashSet<>();
        for (KolB2cApplicationAddressDTO.UpdateDTO address : addressList) {
            if (!partnerIdSet.add(address.getPartnerId())) {
                throw new ServiceException("存在重复的达人地址：" + address.getNickname());
            }
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
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "第"+index+"行达人");
            }
            detail.setNickname(nickName);
            SkuVO skuVO = skuMap.get(detail.getSkuId());
            if(Objects.isNull(skuVO)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "第"+index+"行SKU");
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
            address.setNickname(partnerMap.get(address.getPartnerId()));
            address.setCountryName(dictCountryMap.get(address.getCountryId()));
        }
    }


    @Override
    public PagingVO<KolB2cApplicationDTO.ListDTO> paging(PagingDTO<KolB2cApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        query.setOptimizeCountSql(false);
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
        list.add(0,new KolB2cApplicationDTO.TabListDTO("all","全部",0));
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
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        KolB2cApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2C寄样申请单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
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
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
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
        throw new ServiceException(ApiError.SAMPLE_B2C_DISAPPROVE_FORBIDDEN);
    }

    private Boolean validateDisApprove(KolB2cApplicationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus())) {
            throw new ServiceException("只有待提交数据支持删除");
        }
        if (entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.BILL_DELETE_ALLOWED_STATUS_ONLY);
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
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT.getCode(), entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.BILL_VOID_ALLOWED_STATUS_ONLY);
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

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String id) {
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        String billStatus = KolB2cApplicationDocumentStatusEnum.normalize(entity.getBillStatus());
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode())) {
            throw new ServiceException(ApiError.SAMPLE_B2C_CANCEL_APPROVE_REQUIRED);
        }
        if (Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELED.getCode())) {
            throw new ServiceException(ApiError.SAMPLE_B2C_CANCEL_ALREADY);
        }
        if (!Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CREATED.getCode())
                && !Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getCode())
                && !Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELING.getCode())) {
            throw new ServiceException(ApiError.SAMPLE_B2C_CANCEL_STATUS_INVALID);
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ObjectUtil.isEmpty(userInfo)) {
            userInfo = UserContext.getNonLoginUser();
        }
        if (ObjectUtil.isEmpty(userInfo)) {
            userInfo = new LoginUser();
            userInfo.setUid("0");
            userInfo.setUserName("system");
        }
        LocalDateTime now = LocalDateTime.now();
        try {
            if (Boolean.TRUE.equals(entity.getIsInternational())) {
                cancelInternationalOrder(entity);
                updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCELED.getCode(), "", now, userInfo);
                markApplicationInvalidOnCancel(entity.getId(), "B2C寄样单取消自动作废");
                String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据取消成功，并自动作废", userInfo.getUserName(), entity.getCode(), "B2C寄样申请单");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "取消操作");
            } else {
                boolean retryFromCanceling = Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELING.getCode());
                updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCELING.getCode(), "", now, userInfo);
                boolean hasSyncTask = cancelDomesticOrder(entity);
                String msg;
                if (hasSyncTask) {
                    msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据{}，状态更新为【{}】",
                            userInfo.getUserName(), entity.getCode(), "B2C寄样申请单",
                            retryFromCanceling ? "重新发起取消" : "发起取消",
                            KolB2cApplicationDocumentStatusEnum.CANCELING.getName());
                } else {
                    updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCELED.getCode(), "", now, userInfo);
                    msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据取消成功", userInfo.getUserName(), entity.getCode(), "B2C寄样申请单");
                }
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "取消操作");
            }
            return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL);
        } catch (Exception e) {
            String reason = StringUtils.substring(StrUtil.blankToDefault(e.getMessage(), "取消失败"), 0, 500);
            updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getCode(), reason, now, userInfo);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshCancelStatusBySubOrder(String mainId, String failReason) {
        if (StringUtils.isBlank(mainId)) {
            return;
        }
        KolB2cApplicationEntity entity = super.getByIdOpt(mainId).orElse(null);
        if (ObjectUtil.isEmpty(entity)) {
            log.warn("刷新B2C寄样申请取消状态失败，主单不存在: mainId={}", mainId);
            return;
        }
        String billStatus = KolB2cApplicationDocumentStatusEnum.normalize(entity.getBillStatus());
        if (!Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELING.getCode())
                && !Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getCode())) {
            return;
        }
        List<KolSubB2cApplicationEntity> subList = kolSubB2cApplicationService.lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getSourceId, mainId)
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(subList)) {
            return;
        }
        boolean allCanceled = subList.stream()
                .allMatch(sub -> Objects.equals(KolSubB2cApplicationOrderStatusEnum.NOT.getCode(), sub.getOrderStatus()));
        LoginUser userInfo = buildCancelCallbackUser(entity);
        LocalDateTime now = LocalDateTime.now();
        if (allCanceled) {
            if (!Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELED.getCode())) {
                updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCELED.getCode(), "", now, userInfo);
                String msg = StrUtil.format("旺店通回传取消成功，单号为【{}】的【{}】单据状态更新为【{}】",
                        entity.getCode(), "B2C寄样申请单", KolB2cApplicationDocumentStatusEnum.CANCELED.getName());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "取消回调");
            }
            return;
        }
        if (Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELING.getCode())) {
            String reason = StringUtils.substring(StrUtil.blankToDefault(failReason, "旺店通取消未成功"), 0, 500);
            updateBillCancelStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getCode(), reason, now, userInfo);
            String msg = StrUtil.format("旺店通回传取消失败，单号为【{}】的【{}】单据状态更新为【{}】, 原因：【{}】",
                    entity.getCode(), "B2C寄样申请单", KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getName(), reason);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), entity.getId(), "取消回调");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDomesticCancelPushSuccess(KolB2cApplicationCancelCallbackDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return;
        }
        KolSubB2cApplicationEntity subEntity = getKolSubB2cApplicationByCallback(dto);
        if (ObjectUtil.isEmpty(subEntity)) {
            log.warn("处理中台B2C取消成功回调失败，拆分单不存在: subOrderId={}, subOrderCode={}, syncTaskId={}",
                    dto.getSubOrderId(), dto.getSubOrderCode(), dto.getSyncTaskId());
            return;
        }
        KolB2cApplicationEntity mainEntity = super.getByIdOpt(subEntity.getSourceId()).orElse(null);
        if (ObjectUtil.isEmpty(mainEntity)) {
            log.warn("处理中台B2C取消成功回调失败，主单不存在: subOrderId={}, subOrderCode={}, mainId={}, syncTaskId={}",
                    dto.getSubOrderId(), dto.getSubOrderCode(), subEntity.getSourceId(), dto.getSyncTaskId());
            return;
        }
        boolean subNeedUpdate = !Objects.equals(KolSubB2cApplicationOrderStatusEnum.NOT.getCode(), subEntity.getOrderStatus());
        String billStatus = KolB2cApplicationDocumentStatusEnum.normalize(mainEntity.getBillStatus());
        if (!subNeedUpdate && Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELED.getCode())) {
            return;
        }
        if (subNeedUpdate) {
            KolSubB2cApplicationEntity updateSubEntity = new KolSubB2cApplicationEntity();
            updateSubEntity.setId(subEntity.getId());
            updateSubEntity.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.NOT.getCode());
            kolSubB2cApplicationService.updateById(updateSubEntity);
        }
        List<KolSubB2cApplicationEntity> subList = kolSubB2cApplicationService.lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getSourceId, mainEntity.getId())
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .list();
        boolean allCanceled = CollUtil.isNotEmpty(subList) && subList.stream()
                .allMatch(sub -> Objects.equals(KolSubB2cApplicationOrderStatusEnum.NOT.getCode(), sub.getOrderStatus()));
        boolean mainNeedUpdate = allCanceled
                && !Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELED.getCode());
        if (mainNeedUpdate) {
            LoginUser userInfo = buildCancelCallbackUser(mainEntity);
            updateBillCancelStatus(mainEntity.getId(), KolB2cApplicationDocumentStatusEnum.CANCELED.getCode(),
                    "", LocalDateTime.now(), userInfo);
        }
        String callbackMsg = StringUtils.substring(StrUtil.blankToDefault(dto.getResponseMsg(), ""), 0, 200);
        String msg;
        if (allCanceled) {
            msg = StrUtil.format("中台取消推送成功回调，拆分单【{}】状态更新为【{}】，主单【{}】状态更新为【{}】{}",
                    subEntity.getCode(),
                    KolSubB2cApplicationOrderStatusEnum.NOT.getName(),
                    mainEntity.getCode(),
                    KolB2cApplicationDocumentStatusEnum.CANCELED.getName(),
                    StringUtils.isNotBlank(callbackMsg) ? StrUtil.format("，回调结果：【{}】", callbackMsg) : "");
        } else {
            long canceledCount = Optional.ofNullable(subList).orElse(Collections.emptyList()).stream()
                    .filter(sub -> Objects.equals(KolSubB2cApplicationOrderStatusEnum.NOT.getCode(), sub.getOrderStatus()))
                    .count();
            msg = StrUtil.format("中台取消推送成功回调，拆分单【{}】状态更新为【{}】，主单【{}】保持【{}】，待其余拆分单回调完成（{}/{}）{}",
                    subEntity.getCode(),
                    KolSubB2cApplicationOrderStatusEnum.NOT.getName(),
                    mainEntity.getCode(),
                    KolB2cApplicationDocumentStatusEnum.getName(billStatus),
                    canceledCount,
                    CollUtil.size(subList),
                    StringUtils.isNotBlank(callbackMsg) ? StrUtil.format("，回调结果：【{}】", callbackMsg) : "");
        }
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), mainEntity.getId(), "取消回调");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDomesticCancelPushFail(KolB2cApplicationCancelCallbackDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return;
        }
        KolSubB2cApplicationEntity subEntity = getKolSubB2cApplicationByCallback(dto);
        if (ObjectUtil.isEmpty(subEntity)) {
            log.warn("处理中台B2C取消失败回调失败，拆分单不存在: subOrderId={}, subOrderCode={}, syncTaskId={}",
                    dto.getSubOrderId(), dto.getSubOrderCode(), dto.getSyncTaskId());
            return;
        }
        KolB2cApplicationEntity mainEntity = super.getByIdOpt(subEntity.getSourceId()).orElse(null);
        if (ObjectUtil.isEmpty(mainEntity)) {
            log.warn("处理中台B2C取消失败回调失败，主单不存在: subOrderId={}, subOrderCode={}, mainId={}, syncTaskId={}",
                    dto.getSubOrderId(), dto.getSubOrderCode(), subEntity.getSourceId(), dto.getSyncTaskId());
            return;
        }
        String billStatus = KolB2cApplicationDocumentStatusEnum.normalize(mainEntity.getBillStatus());
        if (Objects.equals(billStatus, KolB2cApplicationDocumentStatusEnum.CANCELED.getCode())) {
            return;
        }
        String failReason = StringUtils.substring(StrUtil.blankToDefault(dto.getResponseMsg(), "中台取消推送失败"), 0, 500);
        LoginUser userInfo = buildCancelCallbackUser(mainEntity);
        updateBillCancelStatus(mainEntity.getId(), KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getCode(),
                failReason, LocalDateTime.now(), userInfo);
        String msg = StrUtil.format("中台取消推送失败回调，拆分单【{}】推送失败，主单【{}】状态更新为【{}】，原因：【{}】",
                subEntity.getCode(),
                mainEntity.getCode(),
                KolB2cApplicationDocumentStatusEnum.CANCEL_FAIL.getName(),
                failReason);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), mainEntity.getId(), "取消回调");
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        KolB2cApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C寄样申请单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
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
        revokeDTO.setSourcePlatform(dto.getSourcePlatform());
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
        // 只有审核通过才下推
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            List<KolB2cApplicationDetailEntity> list = kolB2cApplicationDetailService.lambdaQuery().eq(KolB2cApplicationDetailEntity::getMainId, entity.getId()).list();
            //企业达人信息
            List<String> partnerIds = list.stream().map(KolB2cApplicationDetailEntity::getPartnerId).distinct().collect(Collectors.toList());
            List<KolPartnerInfoEntity> kolPartnerInfoEntities = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, partnerIds).eq(KolPartnerInfoEntity::getDisabled,false).list();
            Map<String, KolPartnerInfoEntity> partnerMap = kolPartnerInfoEntities.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, v -> v));

            //B2C寄样申请单地址信息
            List<KolB2cApplicationAddressEntity> addressList = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getMainId, entity.getId()).list();
            Map<String, KolB2cApplicationAddressEntity> partnerAddressMap = addressList.stream().collect(Collectors.toMap(KolB2cApplicationAddressEntity::getPartnerId, v -> v));

            //根据业务类型生成 国外=B2C订单  国内=旺店通销售订单
            if(entity.getIsInternational()){
                try {
                    UserContext.setIsUserSystem(true);
                    //
                    List<KolSubB2cApplicationDTO.PushDTO> pushDTOS = kolSubB2cApplicationService.generateSplitOrder(entity, list);
                    //下推B2C订单
                    Map<String, String> b2cCodeMap = pushSoB2c(entity,pushDTOS, partnerMap, partnerAddressMap);
                }finally {
                    UserContext.clearIsUserSystem();
                }

            }else {
                //旺店通
                List<KolSubB2cApplicationDTO.PushDTO> pushDTOS = kolSubB2cApplicationService.generateSplitOrder(entity, list);
                // 获取所有推送订单的所有明细 SKU ID 列表
                List<String> skuNos = pushDTOS.stream()
                        .flatMap(e -> e.getDetailList().stream())
                        .map(KolSubB2cApplicationDetailEntity::getSkuNo)
                        .distinct()
                        .collect(Collectors.toList());

                List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNos);
                Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
                for (KolSubB2cApplicationDTO.PushDTO pushDTO : pushDTOS) {
                    syncWangDianSoB2cService.syncDataToWangDian(pushDTO, skuMap);
                }
            }
            updateBillStatus(entity.getId(), KolB2cApplicationDocumentStatusEnum.CREATED.getCode());
        }
        return Boolean.TRUE;
    }

    /**
     * 审核通过生成B2C订单则自动回写kol-b2c拆分单的状态
     * @author jack
     * @date: 2025-12-04
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKolSubStatus(String kolId) {
        List<KolSubB2cApplicationEntity> kolSubB2cApplicationEntities = kolSubB2cApplicationService.lambdaQuery().eq(KolSubB2cApplicationEntity::getSourceId, kolId).list();
        if(CollUtil.isNotEmpty(kolSubB2cApplicationEntities)){
            List<String> sourceIds = kolSubB2cApplicationEntities.stream().map(KolSubB2cApplicationEntity::getId).collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntities = soB2cService.lambdaQuery().eq(SoB2cEntity::getSourceType, SourceTypeEnum.KOL_B2C_APPLICATION.getCode())
                    .in(SoB2cEntity::getSourceId, sourceIds).list();

            for (KolSubB2cApplicationEntity entity : kolSubB2cApplicationEntities) {
                SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(e -> e.getSourceId().equals(entity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(soB2cEntity)){
                    entity.setPlatformSoCode(soB2cEntity.getCode());
                    entity.setPlatformOrderCode(soB2cEntity.getCode());
                    entity.setOrderStatus(soB2cEntity.getApproveStatus().getStatus());
                    if(StringUtils.isNotBlank(soB2cEntity.getBillStatus())&&soB2cEntity.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())){
                        entity.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.SHIPPED.getCode());
                    }else {
                        entity.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.WAITSHIPPED.getCode());
                    }
                    entity.setTrackNo(soB2cEntity.getShippingOrderNo());
                }
            }
            kolSubB2cApplicationService.updateBatchById(kolSubB2cApplicationEntities);
        }
    }

    /**
     * 审核通过自动下推B2C订单
     * @author jack
     * @date: 2025-12-04
     * @return
     */
    private Map<String,String> pushSoB2c(KolB2cApplicationEntity kolB2cApplicationEntity, List<KolSubB2cApplicationDTO.PushDTO> pushDTOS, Map<String, KolPartnerInfoEntity> partnerMap, Map<String, KolB2cApplicationAddressEntity> partnerAddressMap) {
        Map<String,String> resultMap = new HashMap<>();

        String orderCategoryId ="";
        List<OrderCategoryDetailEntity> orderCategoryEntityList = orderCategoryDetailService.lambdaQuery().eq(OrderCategoryDetailEntity::getName, "网红财务审核").list();
        if(CollUtil.isNotEmpty(orderCategoryEntityList)){
            orderCategoryId = orderCategoryEntityList.get(0).getId();
        }

        for (KolSubB2cApplicationDTO.PushDTO pushDTO : pushDTOS) {
            KolSubB2cApplicationEntity entity = pushDTO.getEntity();
            List<KolSubB2cApplicationDetailEntity> detailList = pushDTO.getDetailList();

            //------------根据拆分单生成B2C------------
            //B2C
            SoB2cDTO.AddDTO b2cDto = new SoB2cDTO.AddDTO();
            //销售平台 -- 其他平台
            b2cDto.setDictPlatform(PlatformDictEnum.OTHER_PLATFORM.getCode());
            //店铺
            b2cDto.setShopId(kolB2cApplicationEntity.getShopId());
            //订单金额
            b2cDto.setAmount(BigDecimal.ZERO);
            //币别
            b2cDto.setCurrency(kolB2cApplicationEntity.getCurrency());
            //订单付款时间
            b2cDto.setPayTime(LocalDateTime.now());
            //订单分类
            if(StringUtils.isNotBlank(orderCategoryId)){
                b2cDto.setCategoryIdList(Arrays.asList(orderCategoryId));
            }
            //单据子类型 -- 红人样品
            b2cDto.setTransactionSubType(OrderSubTypeEnum.INFLUENCER_SAMPLE.getCode());
            //物流信息
            SoB2cLogisticsDTO.AddDTO logisticsDTO = new SoB2cLogisticsDTO.AddDTO();
            //物流渠道
            logisticsDTO.setLogisticsChannelId(kolB2cApplicationEntity.getLogisticsChannelId());
            b2cDto.setLogisticsDTO(logisticsDTO);

            //达人信息
            KolPartnerInfoEntity kolPartnerInfoEntity = partnerMap.get(entity.getPartnerId());
            //B2C寄样申请地址
            KolB2cApplicationAddressEntity kolB2cApplicationAddressEntity = partnerAddressMap.get(entity.getPartnerId());
            //买家信息
            SoB2cReceiverDTO.AddDTO receiverDTO = new SoB2cReceiverDTO.AddDTO();
            receiverDTO.setCustomerId(entity.getNickname());
            receiverDTO.setEmail(kolPartnerInfoEntity.getEmail());
            receiverDTO.setTelNumber(kolPartnerInfoEntity.getPhone());
            receiverDTO.setCountry(kolB2cApplicationAddressEntity.getCountryId());
            receiverDTO.setProvinceName(kolB2cApplicationAddressEntity.getProvince());
            receiverDTO.setCityName(kolB2cApplicationAddressEntity.getCity());
            receiverDTO.setDistrictName(kolB2cApplicationAddressEntity.getDistrict());
            receiverDTO.setFirstAddress(kolB2cApplicationAddressEntity.getDetailAddress());
            receiverDTO.setReceiverName(kolB2cApplicationAddressEntity.getReceiverName());
            receiverDTO.setReceiverTelNumber(kolB2cApplicationAddressEntity.getReceiverPhone());
            receiverDTO.setPostCode(kolB2cApplicationAddressEntity.getZipCode());
            receiverDTO.setReceiverTaxNo(kolB2cApplicationAddressEntity.getReceiverTaxNo());
            b2cDto.setReceiverDTO(receiverDTO);

            //------------根据生成拆分单明细，以及生成B2C明细------------
            List<SoB2cDetailDTO.AddDTO> soB2cDetailList = new ArrayList<>(detailList.size());
            for (KolSubB2cApplicationDetailEntity detailEntity : detailList) {
                SoB2cDetailDTO.AddDTO detailAddDto = new SoB2cDetailDTO.AddDTO();
                detailAddDto.setSourceDetailId(detailEntity.getId());
                detailAddDto.setSkuId(detailEntity.getSkuId());
                detailAddDto.setQty(detailEntity.getApplyQty());
                detailAddDto.setWarehouseId(kolB2cApplicationEntity.getWarehouseId());
                detailAddDto.setPrice(BigDecimal.ZERO);
                soB2cDetailList.add(detailAddDto);
            }

            //下推B2C
            //来源
            b2cDto.setSourceId(entity.getId());
            b2cDto.setSourceCode(entity.getCode());
            b2cDto.setSourceType(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
            b2cDto.setDetailList(soB2cDetailList);
            String b2cCode = processOrderCreation(b2cDto);
            resultMap.put(entity.getCode(),b2cCode);
        }
        return resultMap;
    }


    private String processOrderCreation(SoB2cDTO.AddDTO dto) {
        // 速卖通手工订单首次添加税后金额=订单金额(其他平台=0)
        dto.checkAndSetAfterTaxAmount();
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(dto.getShopId());
        if(Objects.nonNull(shopInfoEntity) && shopInfoEntity.getDisabled()){
            throw new ServiceException("店铺已禁用，无法新增订单");
        }

        // 通过代理调用，确保 add() 方法上的独立事务生效
        SoB2cEntity add = soB2cService.add(dto, null);
        String id = add.getId();
        //检查是否备案并修改状态
        soB2cService.checkProductRegistrationAndUpdate(id, "");

        //速卖通平台仓订单不走任何规则
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(add.getDictPlatform()) && add.hasPlatformWarehouseOrder()) {
            return add.getCode();
        }

        // 通过代理调用，确保 orderRule() 方法上的独立事务生效
        SoB2cDTO.RuleResultDTO orderRuleResult = soB2cService.orderRule(id);
        //匹配成功
        Boolean ruleMatch = orderRuleResult.getIsRuleMatch();
        Boolean isPass = orderRuleResult.getIsPass();

        if (ruleMatch && isPass) {
            // 通过代理调用，确保 warehouseRule() 方法上的独立事务生效
            SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRuleNotRequiresNew(orderRuleResult.getId(), orderRuleResult.getSoB2cDetailList(), orderRuleResult.getMap());
            Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
            if (warehouseRuleMatch) {
                // 通过代理调用，确保 logisticsRule() 方法上的独立事务生效
                SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRuleNotRequiresNew(id, new HashMap<>(), false);
                Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                Boolean autoGetTrackNotOfRangeDelivery = logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery();
                Boolean isRuleMatch = logisticsRuleResult.getIsRuleMatch();
                //表示成功
                if(isRuleMatch){
                    //检查是否备案并修改状态
                    soB2cService.checkProductRegistrationAndUpdate(id, "");
                    //申报信息规则
                    soB2cService.declareRule(id, new HashMap<>(), Boolean.FALSE, false);
                }
                SoB2cEntity entity = soB2cService.getById(id);
                Boolean isOutOfRangeDelivery = entity.getIsOutOfRangeDelivery();
                if ((Objects.nonNull(autoGetTrackNo) && Boolean.TRUE.equals(autoGetTrackNo))
                        || (Boolean.FALSE.equals(isOutOfRangeDelivery) && Objects.nonNull(autoGetTrackNotOfRangeDelivery) && Boolean.TRUE.equals(autoGetTrackNotOfRangeDelivery))) {
                    soB2cRuleService.handleAutoSubmitDelivery(id, logisticsRuleResult.getName());
                }
            }
        }
        //自动计算预估运费到订单的预估运费字段（异步）
        soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(id));
        return add.getCode();
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
        List<String> skuIds = detailList.stream()
                .map(KolB2cApplicationDetailDTO.UpdateDTO::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuList = CollUtil.isEmpty(skuIds) ? Collections.emptyList() : plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));

        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setBillStatus(KolB2cApplicationDocumentStatusEnum.normalize(data.getBillStatus()));
        data.setBillStatusName(KolB2cApplicationDocumentStatusEnum.getName(data.getBillStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        String sampleTypeName = map.get(data.getSampleType());
        if(StringUtils.isNotBlank(sampleTypeName)){
            data.setSampleTypeName(sampleTypeName);
        }else{
            data.setSampleTypeName(data.getSampleType());
        }
        data.setIsInternationalName(getIsInternationalName(data.getIsInternational()));
        data.setCurrencyName(currencyMap.get(data.getCurrency()));

        // 属性赋值
        for(KolB2cApplicationDetailDTO.UpdateDTO detailData : detailList) {
            SkuVO skuVO = skuMap.get(detailData.getSkuId());
            if (ObjectUtil.isNotEmpty(skuVO)) {
                detailData.setProductName(skuVO.getSkuName());
                detailData.setSpuNo(skuVO.getSpuNo());
            }

            if(StringUtils.isNotBlank(detailData.getProjectTag())){
                String projectTagName = Arrays.stream(detailData.getProjectTag().split(",")).map(map::get).collect(Collectors.joining(","));
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

    private boolean cancelDomesticOrder(KolB2cApplicationEntity entity) {
        List<KolSubB2cApplicationEntity> subList = kolSubB2cApplicationService.lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getSourceId, entity.getId())
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(subList)) {
            return false;
        }
        List<KolSubB2cApplicationDTO.PushDTO> pushDTOS = kolSubB2cApplicationService.listPushByIds(subList.stream().map(KolSubB2cApplicationEntity::getId).collect(Collectors.toList()));
        if (CollUtil.isEmpty(pushDTOS)) {
            return false;
        }
        for (KolSubB2cApplicationDTO.PushDTO pushDTO : pushDTOS) {
            syncWangDianSoB2cService.syncCancelDataToWangDian(pushDTO, null);
        }
        return true;
    }

    private void cancelInternationalOrder(KolB2cApplicationEntity entity) {
        List<KolSubB2cApplicationEntity> subList = kolSubB2cApplicationService.lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getSourceId, entity.getId())
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(subList)) {
            return;
        }
        List<String> subIds = subList.stream().map(KolSubB2cApplicationEntity::getId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cService.lambdaQuery()
                .eq(SoB2cEntity::getSourceType, SourceTypeEnum.KOL_B2C_APPLICATION.getCode())
                .in(SoB2cEntity::getSourceId, subIds)
                .eq(SoB2cEntity::getIsDeleted, false)
                .eq(SoB2cEntity::getInvalidStatus, false)
                .list();
        for (SoB2cEntity soB2cEntity : soB2cList) {
            soB2cService.invalid(soB2cEntity.getId(), "B2C寄样单取消自动作废", SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        }
    }

    private void updateBillCancelStatus(String id, String billStatus, String failReason, LocalDateTime now, LoginUser userInfo) {
        lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
                .set(KolB2cApplicationEntity::getBillStatus, billStatus)
                .set(KolB2cApplicationEntity::getCancelFailReason, StrUtil.blankToDefault(failReason, ""))
                .set(KolB2cApplicationEntity::getCancelTime, now)
                .set(KolB2cApplicationEntity::getCancelUserId, userInfo.getUid())
                .set(KolB2cApplicationEntity::getCancelUserName, userInfo.getUserName())
                .update(new KolB2cApplicationEntity());
    }

    private void markApplicationInvalidOnCancel(String id, String invalidRemark) {
        lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
                .set(KolB2cApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(KolB2cApplicationEntity::getInvalidRemark, StrUtil.blankToDefault(invalidRemark, ""))
                .update(new KolB2cApplicationEntity());
    }

    private void updateBillStatus(String id, String billStatus) {
        lambdaUpdate().eq(KolB2cApplicationEntity::getId, id)
                .set(KolB2cApplicationEntity::getBillStatus, billStatus)
                .update(new KolB2cApplicationEntity());
    }

    private LoginUser buildCancelCallbackUser(KolB2cApplicationEntity entity) {
        LoginUser userInfo = new LoginUser();
        userInfo.setUid(StrUtil.blankToDefault(entity.getCancelUserId(), "0"));
        userInfo.setUserName(StrUtil.blankToDefault(entity.getCancelUserName(), "system"));
        return userInfo;
    }

    private KolSubB2cApplicationEntity getKolSubB2cApplicationByCallback(KolB2cApplicationCancelCallbackDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return null;
        }
        if (StringUtils.isNotBlank(dto.getSubOrderId())) {
            KolSubB2cApplicationEntity subEntity = kolSubB2cApplicationService.getByIdOpt(dto.getSubOrderId()).orElse(null);
            if (ObjectUtil.isNotEmpty(subEntity)) {
                return subEntity;
            }
        }
        if (StringUtils.isBlank(dto.getSubOrderCode())) {
            return null;
        }
        return kolSubB2cApplicationService.lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getCode, dto.getSubOrderCode())
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .last("limit 1")
                .one();
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<KolB2cApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.KOL_B2C_APPLICATION.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.HTTP_UNKNOWN.getCode(), listApiResult.getMsg()));
            }
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
            data.setBillStatus(KolB2cApplicationDocumentStatusEnum.normalize(data.getBillStatus()));
            data.setBillStatusName(KolB2cApplicationDocumentStatusEnum.getName(data.getBillStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            String sampleTypeName = map.get(data.getSampleType());
            if(StringUtils.isNotBlank(sampleTypeName)){
                data.setSampleTypeName(sampleTypeName);
            }else{
                data.setSampleTypeName(data.getSampleType());
            }
            data.setIsInternationalName(getIsInternationalName(data.getIsInternational()));
            data.setCurrencyName(currencyMap.get(data.getCurrency()));
            data.setOrderStatusName(KolSubB2cApplicationOrderStatusEnum.getName(data.getOrderStatus()));
            data.setDeliveryStatusName(KolSubB2cApplicationDeliveryStatusEnum.getName(data.getDeliveryStatus()));
            data.setProductName(skuMap.get(data.getSkuId()));

            if(StringUtils.isNotBlank(data.getProjectTag())){
                String projectTagName = Arrays.stream(data.getProjectTag().split(",")).map(map::get).collect(Collectors.joining(","));
                data.setProjectTagName(projectTagName);
            }

            //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                List<ProcessManagementDTO.CurApproveInfoDTO> curApproveList = listApiResult.getData().stream()
                        .filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(curApproveList)) {
                    String curApproveName = curApproveList.stream()
                            .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                            .collect(Collectors.joining(","));
                    if (StringUtils.isNotBlank(curApproveName)) {
                        data.setApproveUserName(curApproveName);
                    }
                }
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
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }
    @Override
    public List<KolB2cApplicationDTO.DetailViewDTO> detailView(List<String> detailIdList) {
        if(CollUtil.isEmpty(detailIdList)){
            return Collections.emptyList();
        }
        List<KolB2cApplicationDTO.DetailViewDTO> list = this.baseMapper.detailView(detailIdList);
        int count = list.stream().filter(e -> !e.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList()).size();
        if (count > 0) {
            throw new ServiceException(ApiError.SAMPLE_B2C_APPROVED_REQUIRED);
        }
        List<String> skuIds = list.stream().map(KolB2cApplicationDTO.DetailViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntities = plmTaskFeign.listByIds(skuIds);
        Map<String, String> map = productDetailEntities.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
        list.forEach(e -> e.setProductName(map.get(e.getSkuId())));
        return list;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean generateReturnPiece(List<KolB2cApplicationDTO.DetailViewDTO> list) {
        if(CollUtil.isEmpty(list)){
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED,"回片登记下推");
        }
        for (KolB2cApplicationDTO.DetailViewDTO detailViewDTO : list) {
            KolFeedbackDTO.AddDTO addDTO = new KolFeedbackDTO.AddDTO();
            BeanMapper.copy(detailViewDTO, addDTO);
            addDTO.setSourceId(detailViewDTO.getId());
            addDTO.setSourceDetailId(detailViewDTO.getDetailId());
            addDTO.setSourceCode(detailViewDTO.getCode());
            addDTO.setSourceType(SourceTypeEnum.KOL_B2C_APPLICATION.getCode());
            addDTO.setQty(detailViewDTO.getApplyQty());
            kolFeedbackService.add(addDTO);
        }
        return true;
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
        Map<String, String> shopMap = shopInfoEntities.stream().collect(Collectors.toMap(ShopInfoEntity::getName, ShopInfoEntity::getId, (o1, o2) -> o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, FindUserDTO> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserName, Function.identity(), (o1, o2) -> o1));
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        Map<String, String> deptMap = deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getName, SysDepartmentDTO::getId, (o1, o2) -> o1));
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouserList = wmsTaskFeign.listApproveWarehouse();
        Map<String, String> warehouserMap = warehouserList.stream().collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getName, WarehouseDTO.UpdateDTO::getId, (o1, o2) -> o1));
        //物流渠道
        List<BaseDropDownDTO.DisabledDTO> logisticsList = logisticsFeign.listAll();
        Map<String, String> logisticsMap = logisticsList.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE))
                .collect(Collectors.toMap(BaseDropDownDTO.DisabledDTO::getValue, BaseDropDownDTO.DisabledDTO::getCode, (o1, o2) -> o1));
        //sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity(), (o1, o2) -> o1));
        //达人
        List<KolPartnerInfoEntity> partnerList = kolPartnerInfoService.lambdaQuery().eq(KolPartnerInfoEntity::getDisabled, false).list();
        Map<String, String> partnerMap = partnerList.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getNickname, KolPartnerInfoEntity::getId, (o1, o2) -> o1));
        //字典
        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> cfgKolOptionMap = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getName, CfgKolOptionEntity::getId, (o1, o2) -> o1));
        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId, (o1, o2) -> o1));
        //省市区
        List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
        Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
        Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));
        Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));
        Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));

        //币种
        List<DictCurrencyEntity> dictCurrencyEntities = sysUserFeign.currencyList();
        Map<String, String> currencyMap = dictCurrencyEntities.stream().collect(Collectors.toMap(DictCurrencyEntity::getName, DictCurrencyEntity::getId, (o1, o2) -> o1));

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

        KolB2cApplicationAddressExcelListener addressListenerUtil = new KolB2cApplicationAddressExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),partnerMap,dictCountryMap,provinceMap,cityMap,districtMap);

        List<MultiErrorExcelData> errList = new ArrayList<>();
        List<KolB2cApplicationDetailImportExcelDTO> detailErrorList = new ArrayList<>();
        List<KolB2cApplicationAddressImportExcelDTO> addressErrorList = new ArrayList<>();
        List<KolB2cApplicationImportExcelDTO> errorList = new ArrayList<>();
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());

            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationDetailImportExcelDTO.class, detailExcelListenerUtil).sheet(1).doRead();
            EasyExcel.read(new ByteArrayInputStream(bytes), KolB2cApplicationAddressImportExcelDTO.class, addressListenerUtil).sheet(2).doRead();

            List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList = detailExcelListenerUtil.getSuccessList();
            detailErrorList = detailExcelListenerUtil.getErrorList();

            List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList = addressListenerUtil.getSuccessList();
            addressErrorList = addressListenerUtil.getErrorList();

            List<KolB2cApplicationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            errorList = excelListenerUtil.getErrorList();

            List<String> errorNoList = errorList.stream().map(KolB2cApplicationImportExcelDTO::getNo).distinct().collect(Collectors.toList());
            Set<String> allMainNoSet = Stream.concat(successList.stream(), errorList.stream())
                    .map(KolB2cApplicationImportExcelDTO::getNo)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            List<KolB2cApplicationDetailImportExcelDTO> error1 = detailSuccessList.stream().filter(e -> errorNoList.contains(e.getNo())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(error1)){
                error1.forEach(e -> e.setErrorMsg(appendImportError(e.getErrorMsg(), "1、主表数据异常；")));
                detailErrorList.addAll(error1);

                detailSuccessList = detailSuccessList.stream().filter(e -> !errorNoList.contains(e.getNo())).collect(Collectors.toList());
            }

            List<KolB2cApplicationAddressImportExcelDTO> error2 = addressSuccessList.stream().filter(e -> errorNoList.contains(e.getNo())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(error2)){
                error2.forEach(e -> e.setErrorMsg(appendImportError(e.getErrorMsg(), "1、主表数据异常；")));
                addressErrorList.addAll(error2);

                addressSuccessList = addressSuccessList.stream().filter(e -> !errorNoList.contains(e.getNo())).collect(Collectors.toList());
            }

            // 明细/地址中的序号在sheet1不存在，按错误处理，避免“仅导入地址明细也提示成功”
            List<KolB2cApplicationDetailImportExcelDTO> detailNoNotExistList = detailSuccessList.stream()
                    .filter(e -> !allMainNoSet.contains(e.getNo()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(detailNoNotExistList)) {
                detailNoNotExistList.forEach(e -> e.setErrorMsg(appendImportError(e.getErrorMsg(), "1、序号在sheet1中不存在；")));
                detailErrorList.addAll(detailNoNotExistList);
                detailSuccessList = detailSuccessList.stream().filter(e -> allMainNoSet.contains(e.getNo())).collect(Collectors.toList());
            }

            List<KolB2cApplicationAddressImportExcelDTO> addressNoNotExistList = addressSuccessList.stream()
                    .filter(e -> !allMainNoSet.contains(e.getNo()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(addressNoNotExistList)) {
                addressNoNotExistList.forEach(e -> e.setErrorMsg(appendImportError(e.getErrorMsg(), "1、序号在sheet1中不存在；")));
                addressErrorList.addAll(addressNoNotExistList);
                addressSuccessList = addressSuccessList.stream().filter(e -> allMainNoSet.contains(e.getNo())).collect(Collectors.toList());
            }

            KolB2cApplicationService kolB2cApplicationService = SpringUtil.getBean(KolB2cApplicationService.class);
            kolB2cApplicationService.handleImportSuccessList(successList, errorList, detailSuccessList, addressSuccessList,dto.getImportType());

            MultiErrorExcelData sheet2 = new MultiErrorExcelData();
            MultiErrorExcelData sheet3 = new MultiErrorExcelData();

            sheet2.setSheetName("sheet2");
            sheet2.setSheetNo(1);
            sheet2.setClazz(KolB2cApplicationDetailImportExcelDTO.class);
            sheet2.setDataResult(detailErrorList);

            sheet3.setSheetName("sheet3");
            sheet3.setSheetNo(2);
            sheet3.setClazz(KolB2cApplicationAddressImportExcelDTO.class);
            sheet3.setDataResult(addressErrorList);


            errList.add(sheet2);
            errList.add(sheet3);

        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        MultiErrorExcelData sheet1 = new MultiErrorExcelData();
        sheet1.setSheetName("sheet1");
        sheet1.setSheetNo(0);
        sheet1.setClazz(KolB2cApplicationImportExcelDTO.class);
        sheet1.setDataResult(errorList);
        errList.add(0,sheet1);
        String url = "";
        int totalErrorCount = errorList.size() + detailErrorList.size() + addressErrorList.size();
        if (totalErrorCount > 0) {
            String fileName = "B2C寄样申请错误信息.xlsx";
//            File file = ExcelUtil.exportFile(fileName, "error", errorList, KolB2cApplicationImportExcelDTO.class);
            File file = ExcelUtil.generateTemplateFile(fileName,errList);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + totalErrorCount + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public AddressParseDTO.ParseResultDTO addressParse(AddressParseDTO.ParseRequestDTO dto) {
        return addressParseService.parse(dto);
    }

    private String appendImportError(String sourceErrorMsg, String appendErrorMsg) {
        if (StringUtils.isBlank(sourceErrorMsg)) {
            return appendErrorMsg;
        }
        return sourceErrorMsg + appendErrorMsg;
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

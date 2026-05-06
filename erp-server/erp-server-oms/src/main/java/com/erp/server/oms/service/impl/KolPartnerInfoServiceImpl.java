package com.erp.server.oms.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import cn.hutool.core.util.StrUtil;
import com.common.business.vo.LoginUser;
import com.common.core.enums.DictCityTypeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.KolPartnerInfoImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgKolOptionTypeEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.KolPartnerInfoExcelListener;
import com.erp.server.oms.service.address.AddressParseService;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.server.oms.mapper.KolPartnerInfoMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 企业达人库 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
@Slf4j
@Service
public class KolPartnerInfoServiceImpl extends SuperServiceImpl<KolPartnerInfoMapper, KolPartnerInfoEntity> implements KolPartnerInfoService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgKolOptionService cfgKolOptionService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private KolAddressInfoService kolAddressInfoService;
    @Resource
    private KolCooperationPlatformService kolCooperationPlatformService;
    @Resource
    private DictLanguageService dictLanguageService;
    @Resource
    private CommonService commonService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private KolB2cApplicationAddressService  kolB2cApplicationAddressService;
    @Resource
    private KolFeedbackService  kolFeedbackService;
    @Resource
    private AddressParseService addressParseService;
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolPartnerInfoDTO.AddDTO addDTO) {
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(addDTO.getChargeId());
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "负责人");
        }else {
            addDTO.setChargeName(findUserDTO.getUserName());
        }

        if(StringUtils.isNotBlank(addDTO.getDeptId())){
            List<SysDepartmentEntity> depts = sysUserFeign.getDeptByIds(Arrays.asList(addDTO.getDeptId()));
            if (CollUtil.isEmpty(depts)) {
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "负责部门");
            } else {
                addDTO.setDeptName(depts.get(0).getName());
            }
        }


        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        DictCountryDTO.ListDTO listDTO = dictCountryList.stream().filter(e -> e.getId().equals(addDTO.getCountryId())).findFirst().orElse(null);
        if(Objects.isNull(listDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "国家");
        }
        addDTO.setCountryName(listDTO.getNameCn());

        KolPartnerInfoEntity one = lambdaQuery().eq(KolPartnerInfoEntity::getNickname, addDTO.getNickname()).eq(KolPartnerInfoEntity::getIsDeleted, false).one();
        if(Objects.nonNull(one)){
            throw new ServiceException("达人昵称已存在");
        }

        KolPartnerInfoEntity kolPartnerInfoEntity = new KolPartnerInfoEntity();
        BeanMapperUtils.copy(addDTO, kolPartnerInfoEntity);

        // 数据处理
        if(CollUtil.isNotEmpty(addDTO.getTypeList())){
            String type = String.join(",", addDTO.getTypeList());
            kolPartnerInfoEntity.setType(type);
        }
        if(CollUtil.isNotEmpty(addDTO.getCooperationTypeList())){
            String cooperationType = String.join(",", addDTO.getCooperationTypeList());
            kolPartnerInfoEntity.setCooperationType(cooperationType);
        }

        log.info("开始新增企业达人库");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DR);
        kolPartnerInfoEntity.setCode(code);
        boolean save = super.save(kolPartnerInfoEntity);
        if(!save) {
            throw new ServiceException("企业达人库保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "企业达人库" , kolPartnerInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolPartnerInfoEntity.getId(), "新增操作");

        String id = kolPartnerInfoEntity.getId();

        List<KolAddressInfoDTO.AddDTO> kolAddressInfoDTOList = addDTO.getKolAddressInfoDTOList();
        if(CollUtil.isNotEmpty(kolAddressInfoDTOList)){
            long count = kolAddressInfoDTOList.stream().filter(e -> e.getIsDefault().equals(true)).count();
            if(count > 1){
                throw new ServiceException(ApiError.SAMPLE_PARTNER_MULTIPLE_DEFAULT_ADDRESS_FORBIDDEN);
            }
            //国家
            Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
            //省市区
            List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
            Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
            Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
            Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
            Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));

            List<KolAddressInfoEntity> list = BeanMapperUtils.copyList(KolAddressInfoEntity.class, kolAddressInfoDTOList);
            for (KolAddressInfoEntity kolAddressInfoEntity : list) {
                kolAddressInfoEntity.setMainId(id);
                checkAndSetAddress(kolAddressInfoEntity, dictCountryMap, provinceMap, cityMap, districtMap);
            }
            kolAddressInfoService.saveBatch(list);
        }

        List<KolCooperationPlatformDTO.AddDTO> kolCooperationPlatformDTOList = addDTO.getKolCooperationPlatformDTOList();
        if(CollUtil.isNotEmpty(kolCooperationPlatformDTOList)){
            List<KolCooperationPlatformEntity> list = BeanMapperUtils.copyList(KolCooperationPlatformEntity.class, kolCooperationPlatformDTOList);
            list.forEach(e -> e.setMainId(id));
            kolCooperationPlatformService.saveBatch(list);
        }

        //附件
        // 保存附件
        TableName tableName = KolPartnerInfoEntity.class.getDeclaredAnnotation(TableName.class);
        omsAttachmentService.batchSaveOrUpdate(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), tableName.value(), id);

        return new BaseResultDTO.AddDTO(kolPartnerInfoEntity.getId(), code);
    }

    //校验并设置国家省市区
    private static void checkAndSetAddress(KolAddressInfoEntity kolAddressInfoEntity, Map<String, String> dictCountryMap, Map<String, String> provinceMap, Map<String, String> cityMap, Map<String, String> districtMap) {
        //国家
        String countryName = dictCountryMap.getOrDefault(kolAddressInfoEntity.getCountryId(), "");
        if(StringUtils.isNotBlank(countryName)){
            kolAddressInfoEntity.setCountryName(countryName);
        }else {
            throw new ServiceException("国家名称不存在");
        }
        if(kolAddressInfoEntity.getCountryId().equals(DictValueEnum.CN.getCode())){
            //省
            if(StringUtils.isBlank(kolAddressInfoEntity.getProvinceId())){
                throw new ServiceException("省不能为空");
            }
            String province = provinceMap.getOrDefault(kolAddressInfoEntity.getProvinceId(), "");
            if(StringUtils.isNotBlank(province)){
                kolAddressInfoEntity.setProvince(province);
            }else {
                throw new ServiceException("省不存在");
            }
            //市
            if(StringUtils.isBlank(kolAddressInfoEntity.getCityId())){
                throw new ServiceException("市不能为空");
            }
            String city = cityMap.getOrDefault(kolAddressInfoEntity.getCityId(), "");
            if(StringUtils.isNotBlank(city)){
                kolAddressInfoEntity.setCity(city);
            }else {
                throw new ServiceException("市不存在");
            }
            //区域
            if(StringUtils.isNotBlank(kolAddressInfoEntity.getDistrictId())){
                String district = districtMap.getOrDefault(kolAddressInfoEntity.getDistrictId(), "");
                if(StringUtils.isNotBlank(district)){
                    kolAddressInfoEntity.setDistrict(district);
                }else {
                    throw new ServiceException("区域不存在");
                }
            }else {
                throw new ServiceException("国家为中国大陆则区域不能为空");
            }
        }else{
            //省
            if(StringUtils.isBlank(kolAddressInfoEntity.getProvince())){
                throw new ServiceException("省不能为空");
            }
            //市
            if(StringUtils.isBlank(kolAddressInfoEntity.getCity())){
                throw new ServiceException("市不能为空");
            }
        }
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolPartnerInfoDTO.UpdateDTO addOrUpdateDTO) {
        KolPartnerInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "企业达人库"));

        // 数据处理
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(addOrUpdateDTO.getChargeId());
        if(ObjectUtil.isNull(findUserDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "负责人");
        }else {
            addOrUpdateDTO.setChargeName(findUserDTO.getUserName());
        }

        if(StringUtils.isNotBlank(addOrUpdateDTO.getDeptId())){
            List<SysDepartmentEntity> depts = sysUserFeign.getDeptByIds(Arrays.asList(addOrUpdateDTO.getDeptId()));
            if (CollUtil.isEmpty(depts)) {
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "负责部门");
            } else {
                addOrUpdateDTO.setDeptName(depts.get(0).getName());
            }
        }

        //国家
        List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
        DictCountryDTO.ListDTO listDTO = dictCountryList.stream().filter(e -> e.getId().equals(addOrUpdateDTO.getCountryId())).findFirst().orElse(null);
        if(Objects.isNull(listDTO)){
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "国家");
        }
        addOrUpdateDTO.setCountryName(listDTO.getNameCn());

        if(CollUtil.isNotEmpty(addOrUpdateDTO.getTypeList())){
            String type = String.join(",", addOrUpdateDTO.getTypeList());
            addOrUpdateDTO.setType(type);
        }
        if(CollUtil.isNotEmpty(addOrUpdateDTO.getCooperationTypeList())){
            String cooperationType = String.join(",", addOrUpdateDTO.getCooperationTypeList());
            addOrUpdateDTO.setCooperationType(cooperationType);
        }

        KolPartnerInfoEntity one = lambdaQuery().eq(KolPartnerInfoEntity::getNickname, addOrUpdateDTO.getNickname()).ne(KolPartnerInfoEntity::getId, addOrUpdateDTO.getId()).one();
        if(Objects.nonNull(one)){
            throw new ServiceException("达人昵称已存在");
        }

        KolPartnerInfoEntity kolPartnerInfoEntity =  BeanMapperUtils.map(KolPartnerInfoEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改企业达人库数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kolPartnerInfoEntity);
        if(!save) {
            throw new ServiceException("企业达人库保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录企业达人库日志数据，单号：【{}】", kolPartnerInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolPartnerInfoEntity.getCode(), "企业达人库");
        operateLogService.addModuleOperateLogByObj(old, kolPartnerInfoEntity, ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolPartnerInfoEntity.getId(), msg);

        List<KolCooperationPlatformEntity> oldKolCooperationPlatformEntities = kolCooperationPlatformService.lambdaQuery().eq(KolCooperationPlatformEntity::getMainId, kolPartnerInfoEntity.getId()).list();
        List<KolCooperationPlatformDTO.UpdateDTO> kolCooperationPlatformDTOList = addOrUpdateDTO.getKolCooperationPlatformDTOList();
        if(CollUtil.isEmpty(kolCooperationPlatformDTOList)){
            if (CollUtil.isNotEmpty(oldKolCooperationPlatformEntities)){
                kolCooperationPlatformService.lambdaUpdate().set(KolCooperationPlatformEntity::getIsDeleted, true).eq(KolCooperationPlatformEntity::getMainId, kolPartnerInfoEntity.getId()).update();
                // 操作日志
                String detailMsg = StrUtil.format("用户【{}】删除所有合作平台信息", UserContext.getDefaultLoginUser().getUserName());
                operateLogService.addModuleOperateLog(detailMsg, ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolPartnerInfoEntity.getId(), "编辑信息");
            }
        }else{
            List<KolCooperationPlatformEntity> kolCooperationPlatformEntities = BeanMapperUtils.copyList(KolCooperationPlatformEntity.class, kolCooperationPlatformDTOList);
            kolCooperationPlatformEntities.forEach(e -> e.setMainId(kolPartnerInfoEntity.getId()));
            commonService.updateDetail(kolPartnerInfoEntity.getId(),ModuleTypeEnum.KOL_PARTNER_INFO.getCode(),kolCooperationPlatformService,  kolCooperationPlatformEntities, oldKolCooperationPlatformEntities,"platformName");
        }

        List<KolAddressInfoEntity> oldKolAddressInfoEntities = kolAddressInfoService.lambdaQuery().eq(KolAddressInfoEntity::getMainId, kolPartnerInfoEntity.getId()).list();
        List<KolAddressInfoDTO.UpdateDTO> kolAddressInfoDTOList = addOrUpdateDTO.getKolAddressInfoDTOList();

        if(CollUtil.isEmpty(kolAddressInfoDTOList)){
            if (CollUtil.isNotEmpty(oldKolAddressInfoEntities)){
                kolAddressInfoService.lambdaUpdate().set(KolAddressInfoEntity::getIsDeleted, true).eq(KolAddressInfoEntity::getMainId, kolPartnerInfoEntity.getId()).update();
                // 操作日志
                String detailMsg = StrUtil.format("用户【{}】删除所有地址信息", UserContext.getDefaultLoginUser().getUserName());
                operateLogService.addModuleOperateLog(detailMsg, ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolPartnerInfoEntity.getId(), "编辑信息");
            }
        }else {
            long count = kolAddressInfoDTOList.stream().filter(e -> e.getIsDefault().equals(true)).count();
            if(count > 1){
                throw new ServiceException(ApiError.SAMPLE_PARTNER_MULTIPLE_DEFAULT_ADDRESS_FORBIDDEN);
            }
            //国家
            Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
            //省市区
            List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
            Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
            Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
            Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));
            Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getId,DictCityEntity::getName,(o1,o2)->o1));

            List<KolAddressInfoEntity> list = BeanMapperUtils.copyList(KolAddressInfoEntity.class, kolAddressInfoDTOList);
            for (KolAddressInfoEntity kolAddressInfoEntity : list) {
                kolAddressInfoEntity.setMainId(kolPartnerInfoEntity.getId());
                checkAndSetAddress(kolAddressInfoEntity, dictCountryMap, provinceMap, cityMap, districtMap);
            }
            commonService.updateDetail(kolPartnerInfoEntity.getId(), ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolAddressInfoService, list, oldKolAddressInfoEntities, "contactPerson");
        }

        // 保存附件
        TableName tableName = KolPartnerInfoEntity.class.getDeclaredAnnotation(TableName.class);
        omsAttachmentService.batchSaveOrUpdate(addOrUpdateDTO.getAttachUrlList(), addOrUpdateDTO.getAttachNameList(), tableName.value(), kolPartnerInfoEntity.getId());

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolPartnerInfoDTO.ListDTO> paging(PagingDTO<KolPartnerInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolPartnerInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<KolPartnerInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolPartnerInfoDTO.PagingParamDTO searchParam = new KolPartnerInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<KolPartnerInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        list.add(0,(new KolPartnerInfoDTO.TabListDTO("all","全部",0)));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(KolPartnerInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("企业达人库导出", EXPORT_OMS_KOL_PARTNER_INFO.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        KolPartnerInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到企业达人库数据"));
        //校验KOL-B2C是否被引用
        Integer count = kolB2cApplicationAddressService.lambdaQuery().eq(KolB2cApplicationAddressEntity::getPartnerId, id).count();
        //校验KOL-回片登记是否被引用
        Integer count1 = kolFeedbackService.lambdaQuery().eq(KolFeedbackEntity::getPartnerId, id).count();
        if(count > 0 || count1 > 0){
            throw new ServiceException(ApiError.SAMPLE_PARTNER_IN_USE);
        }
        // 删除主单数据
        log.info("删除 开始删除企业达人库主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除企业达人库日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "企业达人库");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除企业达人库数据");

        kolCooperationPlatformService.lambdaUpdate().eq(KolCooperationPlatformEntity::getMainId, id).set(KolCooperationPlatformEntity::getIsDeleted, true).update();

        kolAddressInfoService.lambdaUpdate().eq(KolAddressInfoEntity::getMainId, id).set(KolAddressInfoEntity::getIsDeleted, true).update();

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disabled(String id, Boolean disabled) {
        KolPartnerInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到企业达人库数据"));
        if(!entity.getDisabled().equals(disabled)){

            entity.setDisabled(disabled);

            updateById(entity);

            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "企业达人库",disabled ? "禁用" : "启用");
            operateLogService.addModuleOperateLog(msg, null, entity.getCode(), disabled ? "禁用" : "启用" + "企业达人库数据");
        }

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入企业达人库", IMPORT_OMS_KOL_PARTNER_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void importKolPartnerInfo(BaseDTO.ImportDTO dto) {
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

        KolPartnerInfoExcelListener excelListenerUtil = new KolPartnerInfoExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), KolPartnerInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<KolPartnerInfoImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "企业达人库错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, KolPartnerInfoImportExcelDTO.class);
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

    @Override
    public void handleImportSuccessList(List<KolPartnerInfoImportExcelDTO> successList, List<String> errorNoList, List<KolPartnerInfoImportExcelDTO> errorList, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNickname()) && !errorNoList.contains(e.getNickname())).collect(Collectors.toList());

            //全部返回到错误列表
            List<KolPartnerInfoImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNickname()) || errorNoList.contains(e.getNickname())).collect(Collectors.toList());
            errorList.addAll(collect);
        }

        if(CollUtil.isNotEmpty(successList)){
            // 获取数据字典
            List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.COOPERATION_TYPE.getCode(), CfgKolOptionTypeEnum.PARTNER_TYPE.getCode())).list();
            Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getName, CfgKolOptionEntity::getId));
            // 获取语言字典
            List<DictLanguageEntity> dictLanguageEntities = dictLanguageService.list();
            Map<String, String> languageMap = dictLanguageEntities.stream().collect(Collectors.toMap(DictLanguageEntity::getId, DictLanguageEntity::getNameZh));
            //部门
            List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
            Map<String, String> deptMap = deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getName, SysDepartmentDTO::getId,(o1,o2)->o1));
            //用户
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserName, FindUserDTO::getUserId,(o1,o2)->o1));
            //国家
            List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
            Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId));
            //省市区
            List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
            Map<String, List<DictCityEntity>> dictCityGroup = dictCityEntities.stream().filter(e -> e.getDisabled().equals(Boolean.FALSE)).collect(Collectors.groupingBy(DictCityEntity::getType));
            Map<String,String> provinceMap = dictCityGroup.get(DictCityTypeEnum.PROVINCE.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));
            Map<String,String> cityMap = dictCityGroup.get(DictCityTypeEnum.CITY.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));
            Map<String,String> districtMap = dictCityGroup.get(DictCityTypeEnum.DISTRICT.getCode()).stream().collect(Collectors.toMap(DictCityEntity::getName,DictCityEntity::getId,(o1,o2)->o1));

            List<String> nicknameList = successList.stream()
                    .map(KolPartnerInfoImportExcelDTO::getNickname)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            //企业达人库旧数据
            Map<String, String> oldMap = new HashMap<>();
            if (CollUtil.isNotEmpty(nicknameList)) {
                List<KolPartnerInfoEntity> oldList = lambdaQuery()
                        .in(KolPartnerInfoEntity::getNickname, nicknameList)
                        .eq(KolPartnerInfoEntity::getIsDeleted, false)
                        .list();
                oldMap = oldList.stream()
                        .collect(Collectors.toMap(KolPartnerInfoEntity::getNickname, KolPartnerInfoEntity::getId, (o1, o2) -> o1));
            }

            //按昵称分组
            Map<String, List<KolPartnerInfoImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(KolPartnerInfoImportExcelDTO::getNickname));

            List<KolPartnerInfoDTO.AddDTO> addList = new ArrayList<>();
            // 用于记录当前批次中已处理的昵称，避免同一批次内重复
            Set<String> processedNicknameSet = new HashSet<>();

            for (Map.Entry<String, List<KolPartnerInfoImportExcelDTO>> entry : collect.entrySet()) {
                List<String> errorMsgList = new ArrayList<>();

                String nickname = entry.getKey();
                // 如果昵称为空，跳过处理
                if(StringUtils.isBlank(nickname)){
                    continue;
                }
                // 检查数据库中是否已存在
                if(oldMap.containsKey(nickname)){
                    errorMsgList.add("达人昵称已存在");
                } else if(processedNicknameSet.contains(nickname)){
                    // 检查当前批次内是否重复（理论上不会发生，因为已经分组）
                    errorMsgList.add("达人昵称已存在");
                } else {
                    // 记录当前批次中已处理的昵称
                    processedNicknameSet.add(nickname);
                }

                List<KolPartnerInfoImportExcelDTO> list = entry.getValue();
                KolPartnerInfoImportExcelDTO mainInfo = list.get(0);

                //达人类型
                String typeName = mainInfo.getTypeName();
                if(StringUtils.isNotBlank(typeName)){
                    String[] split = typeName.split(",");
                    for (String e : Arrays.asList(split)) {
                        if(!map.containsKey(e)){
                            errorMsgList.add("达人类型不存在");
                            break;
                        }
                    }

                    String type = Arrays.stream(typeName.split(","))
                            .map(map::get)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(","));
                    mainInfo.setType(type);
                }

                //合作类型
                String cooperationTypeName = mainInfo.getCooperationTypeName();
                if(StringUtils.isNotBlank(cooperationTypeName)){
                    String[] split = cooperationTypeName.split(",");
                    for (String e : Arrays.asList(split)) {
                        if(!map.containsKey(e)){
                            errorMsgList.add("合作类型不存在");
                            break;
                        }
                    }

                    String cooperationType = Arrays.stream(split)
                            .map(map::get)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(","));
                    mainInfo.setCooperationType(cooperationType);
                }

                //国家
                String countryId = dictCountryMap.getOrDefault(mainInfo.getCountryName(), "");
                if(StringUtils.isNotBlank(countryId)){
                    mainInfo.setCountryId(countryId);
                }else {
                    errorMsgList.add("国家名称不存在");
                }
                //语言
                String language = languageMap.getOrDefault(mainInfo.getLanguageName(), "");
                if(StringUtils.isNotBlank(language)){
                    mainInfo.setLanguage(language);
                }
                //负责人
                String chargeId = userMap.getOrDefault(mainInfo.getChargeName(), "");
                if(StringUtils.isNotBlank(chargeId)){
                    mainInfo.setChargeId(chargeId);
                }else {
                    errorMsgList.add("负责人不存在");
                }
                //部门
                String deptName = mainInfo.getDeptName();
                if(StringUtils.isNotBlank(deptName)){
                    String deptId = deptMap.getOrDefault(deptName, "");
                    if(StringUtils.isNotBlank(deptId)){
                        mainInfo.setDeptId(deptId);
                    }else {
                        errorMsgList.add("部门不存在");
                    }
                }

                KolPartnerInfoDTO.AddDTO addDTO = new KolPartnerInfoDTO.AddDTO();
                BeanMapperUtils.copy(mainInfo,addDTO);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                    mainInfo.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                    errorList.add(mainInfo);
                    continue;
                }

                List<KolAddressInfoDTO.AddDTO> kolAddressInfoDTOList = new ArrayList<>();
                List<KolCooperationPlatformDTO.AddDTO> kolCooperationPlatformDTOList = new ArrayList<>();

                Boolean isAdd = true;
                Boolean isFirstAddress = true;
                for (KolPartnerInfoImportExcelDTO item : list) {
                    List<String> msgList = new ArrayList<>();

                    //合作平台
                    KolCooperationPlatformDTO.AddDTO kolCooperationPlatformDTO = new KolCooperationPlatformDTO.AddDTO();
                    BeanMapperUtils.copy(item,kolCooperationPlatformDTO);
                    kolCooperationPlatformDTO.setRemark(item.getPlatformRemark());
                    kolCooperationPlatformDTOList.add(kolCooperationPlatformDTO);

                    if(isFirstAddress && StringUtils.isNotBlank(item.getAddressCountryName())){
                        isFirstAddress = false;
                        //地址信息
                        KolAddressInfoDTO.AddDTO kolAddressInfoDTO = new KolAddressInfoDTO.AddDTO();

                        //国家
                        String addressCountryId = dictCountryMap.getOrDefault(item.getAddressCountryName(), "");
                        if(StringUtils.isNotBlank(addressCountryId)){
                            kolAddressInfoDTO.setCountryId(addressCountryId);
                            kolAddressInfoDTO.setCountryName(item.getAddressCountryName());
                        }else {
                            msgList.add("地址信息--国家名称未找到");
                        }

                        // 判断是否为中国大陆或中国，只有在这种情况下才需要校验省、城市、区域
                        boolean isChina = StringUtils.isNotBlank(addressCountryId) &&
                                (addressCountryId.equals(DictValueEnum.CN.getCode()) ||
                                        DictValueEnum.CN.getName().equals(item.getAddressCountryName()));

                        if(isChina){
                            //省
                            String provinceId = provinceMap.getOrDefault(item.getProvince(), "");
                            if(StringUtils.isNotBlank(provinceId)){
                                kolAddressInfoDTO.setProvinceId(provinceId);
                            }else {
                                msgList.add("地址信息--省未找到");
                            }
                            //市
                            String cityId = cityMap.getOrDefault(item.getCity(), "");
                            if(StringUtils.isNotBlank(cityId)){
                                kolAddressInfoDTO.setCityId(cityId);
                            }else {
                                msgList.add("地址信息--市未找到");
                            }
                            //区域
                            if(StringUtils.isBlank(item.getDistrict())) {
                                msgList.add("国家为中国大陆则区域不能为空");
                            }else{
                                String districtId = districtMap.getOrDefault(item.getDistrict(), "");
                                if(StringUtils.isNotBlank(districtId)){
                                    kolAddressInfoDTO.setDistrictId(districtId);
                                }else {
                                    msgList.add("地址信息--区域未找到");
                                }
                            }
                        }else {
                            // 非中国大陆或中国，不校验省、城市、区域，但可以设置值
                            if(StringUtils.isNotBlank(item.getProvince())){
                                String provinceId = provinceMap.getOrDefault(item.getProvince(), "");
                                if(StringUtils.isNotBlank(provinceId)){
                                    kolAddressInfoDTO.setProvinceId(provinceId);
                                }
                            }
                            if(StringUtils.isNotBlank(item.getCity())){
                                String cityId = cityMap.getOrDefault(item.getCity(), "");
                                if(StringUtils.isNotBlank(cityId)){
                                    kolAddressInfoDTO.setCityId(cityId);
                                }
                            }
                            if(StringUtils.isNotBlank(item.getDistrict())){
                                String districtId = districtMap.getOrDefault(item.getDistrict(), "");
                                if(StringUtils.isNotBlank(districtId)){
                                    kolAddressInfoDTO.setDistrictId(districtId);
                                }
                            }
                        }

                            kolAddressInfoDTO.setProvince(item.getProvince());
                            kolAddressInfoDTO.setCity(item.getCity());
                            kolAddressInfoDTO.setDistrict(item.getDistrict());
                            kolAddressInfoDTO.setDetailAddress(item.getDetailAddress());
                            kolAddressInfoDTO.setContactPerson(item.getContactPerson());
                            kolAddressInfoDTO.setPhone(item.getContactPersonPhone());
                            kolAddressInfoDTO.setZipCode(item.getZipCode());
                            kolAddressInfoDTO.setReceiverTaxNo(item.getReceiverTaxNo());

                        String isDefaultName = item.getIsDefaultName();
                        if(StringUtils.isBlank(isDefaultName) || "是".equals(isDefaultName)){
                            kolAddressInfoDTO.setIsDefault(Boolean.TRUE);
                        }else{
                            kolAddressInfoDTO.setIsDefault(Boolean.FALSE);
                        }

                        String disabledName = item.getDisabledName();
                        if(StringUtils.isBlank(disabledName) || "启用".equals(disabledName)){
                            kolAddressInfoDTO.setDisabled(Boolean.FALSE);
                        }else{
                            kolAddressInfoDTO.setDisabled(Boolean.TRUE);
                        }
                        kolAddressInfoDTO.setRemark(item.getAddressRemark());
                        kolAddressInfoDTOList.add(kolAddressInfoDTO);
                    }

                    if (CollectionUtils.isNotEmpty(msgList) || CollectionUtils.isNotEmpty(errorMsgList)) {
                        isAdd = Boolean.FALSE;
                        List<String> itemErrorList = Stream.concat(errorMsgList.stream(), msgList.stream()).distinct().collect(Collectors.toList());
                        item.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                        errorList.add(item);
                    }
                }

                // 内层循环结束后，仅当本批次无错误时再 set 并加入 addList，避免同一 addDTO 被重复添加
                if (isAdd) {
                    addDTO.setKolAddressInfoDTOList(kolAddressInfoDTOList);
                    addDTO.setKolCooperationPlatformDTOList(kolCooperationPlatformDTOList);
                    addList.add(addDTO);
                }
            }

            // 所有 entry 处理完毕后，再统一落库，避免在循环内重复插入
            if (CollUtil.isNotEmpty(addList)) {
                KolPartnerInfoService kolPartnerInfoService = SpringUtil.getBean(KolPartnerInfoService.class);
                for (KolPartnerInfoDTO.AddDTO dto : addList) {
                    kolPartnerInfoService.add(dto);
                }
            }
        }
    }


    @Override
    public KolPartnerInfoDTO.ViewDTO view(String id) {
        KolPartnerInfoEntity kolPartnerInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到企业达人库数据"));
        KolPartnerInfoDTO.ViewDTO data = BeanMapperUtils.map(KolPartnerInfoDTO.ViewDTO.class, kolPartnerInfoEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    private void fillOne(KolPartnerInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.COOPERATION_TYPE.getCode(), CfgKolOptionTypeEnum.PARTNER_TYPE.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        List<DictLanguageEntity> dictLanguageEntities = dictLanguageService.list();
        Map<String, String> languageMap = dictLanguageEntities.stream().collect(Collectors.toMap(DictLanguageEntity::getId, DictLanguageEntity::getNameZh));

        String cooperationTypeName = Arrays.stream(data.getCooperationType().split(","))
                .map(map::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
        data.setCooperationTypeName(cooperationTypeName);

        String typeName = Arrays.stream(data.getType().split(","))
                .map(map::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
        data.setTypeName(typeName);

        data.setLanguageName(languageMap.get(data.getLanguage()));

        List<KolCooperationPlatformEntity> kolCooperationPlatformEntities = kolCooperationPlatformService.lambdaQuery().eq(KolCooperationPlatformEntity::getMainId, data.getId()).orderByDesc(KolCooperationPlatformEntity::getCreateTime).list();
        data.setKolCooperationPlatformDTOList(kolCooperationPlatformEntities);

        List<KolAddressInfoEntity> kolAddressInfoEntities = kolAddressInfoService.lambdaQuery().eq(KolAddressInfoEntity::getMainId, data.getId()).orderByDesc(KolAddressInfoEntity::getCreateTime).list();
        data.setKolAddressInfoDTOList(kolAddressInfoEntities);

        //附件
        List<AttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessId(data.getId());
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        data.setAttachUrlList(attachmentUrlList);
        data.setAttachNameList(attachmentNameList);
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<KolPartnerInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.COOPERATION_TYPE.getCode(), CfgKolOptionTypeEnum.PARTNER_TYPE.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));

        List<DictLanguageEntity> dictLanguageEntities = dictLanguageService.list();
        Map<String, String> languageMap = dictLanguageEntities.stream().collect(Collectors.toMap(DictLanguageEntity::getId, DictLanguageEntity::getNameZh));


        // 属性赋值
        for(KolPartnerInfoDTO.ListDTO data : list) {

            String cooperationTypeName = Arrays.stream(data.getCooperationType().split(","))
                    .map(map::get)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
            data.setCooperationTypeName(cooperationTypeName);

            String typeName = Arrays.stream(data.getType().split(","))
                    .map(map::get)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
            data.setTypeName(typeName);

            data.setLanguageName(languageMap.get(data.getLanguage()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(KolPartnerInfoEntity entity) {
    }

    @Override
    public List<KolPartnerInfoDTO.DropDownDTO> dropDown(KolPartnerInfoDTO.SelectDTO dto) {
        return this.baseMapper.dropDown(dto.getSearchKeyword());
    }

    @Override
    public List<KolPartnerInfoDTO.PartnerAddressDTO> partnerAddressList(KolPartnerInfoDTO.AddressSelectDTO dto) {
        return this.baseMapper.partnerAddressList(dto);
    }

    @Override
    public AddressParseDTO.ParseResultDTO addressParse(AddressParseDTO.ParseRequestDTO dto) {
        return addressParseService.parse(dto);
    }

    @Override
    public List<AddressParseDTO.BatchParseResultDTO> batchAddressParse(List<AddressParseDTO.BatchParseRequestDTO> dtoList) {
        List<AddressParseDTO.BatchParseResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        dtoList.forEach(dto -> {
            AddressParseDTO.ParseRequestDTO requestDTO = new AddressParseDTO.ParseRequestDTO();
            requestDTO.setFullAddress(dto.getFullAddress());
            AddressParseDTO.ParseResultDTO resultDTO = addressParseService.parse(requestDTO);
            AddressParseDTO.BatchParseResultDTO batchParseResultDTO = new AddressParseDTO.BatchParseResultDTO();
            BeanMapperUtils.copy(resultDTO, batchParseResultDTO);
            batchParseResultDTO.setId(dto.getId());
            batchParseResultDTO.setDetailAddress(resultDTO.getDistrict() + resultDTO.getDetailAddress());
            resultDTOS.add(batchParseResultDTO);
        });
        return resultDTOS;
    }

}

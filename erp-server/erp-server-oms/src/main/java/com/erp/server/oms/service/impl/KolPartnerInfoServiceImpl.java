package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import cn.hutool.core.util.StrUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.oms.dto.ExhibitionOrderImportExcelDTO;
import com.erp.model.oms.dto.KolAddressInfoDTO;
import com.erp.model.oms.dto.KolCooperationPlatformDTO;
import com.erp.model.oms.dto.excel.KolPartnerInfoImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgKolOptionTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.ExhibitionOrderExcelListener;
import com.erp.server.oms.listener.KolPartnerInfoExcelListener;
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
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolPartnerInfoDTO.AddDTO addDTO) {
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
            List<KolAddressInfoEntity> list = BeanMapperUtils.copyList(KolAddressInfoEntity.class, kolAddressInfoDTOList);
            list.forEach(e -> e.setMainId(id));
            kolAddressInfoService.saveBatch(list);
        }

        List<KolCooperationPlatformDTO.AddDTO> kolCooperationPlatformDTOList = addDTO.getKolCooperationPlatformDTOList();
        if(CollUtil.isNotEmpty(kolCooperationPlatformDTOList)){
            List<KolCooperationPlatformEntity> list = BeanMapperUtils.copyList(KolCooperationPlatformEntity.class, kolCooperationPlatformDTOList);
            list.forEach(e -> e.setMainId(id));
            kolCooperationPlatformService.saveBatch(list);
        }
        return new BaseResultDTO.AddDTO(kolPartnerInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolPartnerInfoDTO.UpdateDTO addOrUpdateDTO) {
        KolPartnerInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "企业达人库"));

        KolPartnerInfoEntity kolPartnerInfoEntity =  BeanMapperUtils.map(KolPartnerInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        // 数据处理
        if(CollUtil.isNotEmpty(addOrUpdateDTO.getTypeList())){
            String type = String.join(",", addOrUpdateDTO.getTypeList());
            kolPartnerInfoEntity.setType(type);
        }
        if(CollUtil.isNotEmpty(addOrUpdateDTO.getCooperationTypeList())){
            String cooperationType = String.join(",", addOrUpdateDTO.getCooperationTypeList());
            kolPartnerInfoEntity.setCooperationType(cooperationType);
        }

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
            List<KolAddressInfoEntity> kolAddressInfoEntities = BeanMapperUtils.copyList(KolAddressInfoEntity.class, kolAddressInfoDTOList);
            kolAddressInfoEntities.forEach(e -> e.setMainId(kolPartnerInfoEntity.getId()));
            commonService.updateDetail(kolPartnerInfoEntity.getId(), ModuleTypeEnum.KOL_PARTNER_INFO.getCode(), kolAddressInfoService, kolAddressInfoEntities, oldKolAddressInfoEntities, "contactPerson");
        }
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
            throw new ServiceException(ApiError.ERROR_1016);
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
            List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getId, Arrays.asList(CfgKolOptionTypeEnum.COOPERATION_TYPE.getCode(), CfgKolOptionTypeEnum.PARTNER_TYPE.getCode())).list();
            Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));
            // 获取语言字典
            List<DictLanguageEntity> dictLanguageEntities = dictLanguageService.list();
            Map<String, String> languageMap = dictLanguageEntities.stream().collect(Collectors.toMap(DictLanguageEntity::getId, DictLanguageEntity::getNameZh));
            //部门
            List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
            Map<String, String> deptMap = deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getName, SysDepartmentDTO::getId));
            //用户
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserName, FindUserDTO::getUserId));
            //国家
            List<DictCountryDTO.ListDTO> dictCountryList = sysUserFeign.countryList();
            Map<String, String> dictCountryMap = dictCountryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getNameCn, DictCountryDTO.ListDTO::getId));

            List<String> nicknameList = successList.stream().map(KolPartnerInfoImportExcelDTO::getNickname).distinct().collect(Collectors.toList());
            //企业达人库旧数
            List<KolPartnerInfoEntity> oldList = lambdaQuery().in(KolPartnerInfoEntity::getNickname, nicknameList).list();
            Map<String, String> oldMap = oldList.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getNickname, KolPartnerInfoEntity::getId, (o1, o2) -> o1));

            //按昵称分组
            Map<String, List<KolPartnerInfoImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(KolPartnerInfoImportExcelDTO::getNickname));

            List<KolPartnerInfoDTO.AddDTO> addList = new ArrayList<>();

            for (Map.Entry<String, List<KolPartnerInfoImportExcelDTO>> entry : collect.entrySet()) {
                List<String> errorMsgList = new ArrayList<>();

                String nickname = entry.getKey();
                if(oldMap.containsKey(nickname)){
                    errorMsgList.add("达人昵称已存在");
                }else {
                    oldMap.put(nickname, "1");
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
                }else {
                    errorMsgList.add("语言不存在");
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
                if(CollUtil.isEmpty(list)){
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                        mainInfo.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                        errorList.add(mainInfo);
                    }else{
                        addList.add(addDTO);
                    }
                }else {
                    List<KolAddressInfoDTO.AddDTO> kolAddressInfoDTOList = new ArrayList<>();
                    List<KolCooperationPlatformDTO.AddDTO> kolCooperationPlatformDTOList = new ArrayList<>();

                    Boolean isDefault = false;
                    Boolean isAdd = true;
                    for (KolPartnerInfoImportExcelDTO item : list) {
                        List<String> msgList = new ArrayList<>();

                        //合作平台
                        KolCooperationPlatformDTO.AddDTO kolCooperationPlatformDTO = new KolCooperationPlatformDTO.AddDTO();
                        BeanMapperUtils.copy(item,kolCooperationPlatformDTO);
                        kolCooperationPlatformDTO.setRemark(item.getPlatformRemark());
                        kolCooperationPlatformDTOList.add(kolCooperationPlatformDTO);

                        if(!isDefault && StringUtils.isNotBlank(item.getAddressCountryName())){
                            isDefault = true;

                            //地址信息
                            KolAddressInfoDTO.AddDTO kolAddressInfoDTO = new KolAddressInfoDTO.AddDTO();

                            //国家
                            String addressCountryId = dictCountryMap.getOrDefault(item.getAddressCountryName(), "");
                            if(StringUtils.isNotBlank(addressCountryId)){
                                kolAddressInfoDTO.setCountryId(addressCountryId);
                                kolAddressInfoDTO.setCountryName(item.getAddressCountryName());
                            }else {
                                msgList.add("地址信息--国家名称不存在");
                            }

                            kolAddressInfoDTO.setProvince(item.getProvince());
                            kolAddressInfoDTO.setCity(item.getCity());
                            kolAddressInfoDTO.setDistrict(item.getDistrict());
                            kolAddressInfoDTO.setDetailAddress(item.getDetailAddress());
                            kolAddressInfoDTO.setContactPerson(item.getContactPerson());
                            kolAddressInfoDTO.setPhone(item.getContactPersonPhone());
                            kolAddressInfoDTO.setZipCode(item.getZipCode());

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
                            List<String> itemErrorList = Stream.concat(errorMsgList.stream(),msgList.stream()).distinct().collect(Collectors.toList());
                            item.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                            errorList.add(item);
                        }
                    }
                    if(isAdd){
                        addDTO.setKolAddressInfoDTOList(kolAddressInfoDTOList);
                        addDTO.setKolCooperationPlatformDTOList(kolCooperationPlatformDTOList);
                        addList.add(addDTO);
                    }
                }

                if(CollUtil.isNotEmpty(addList)){
                    for (KolPartnerInfoDTO.AddDTO dto : addList) {
                        this.add(dto);
                    }
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
        if(Objects.isNull(dto)){
            return Collections.emptyList();
        }
        //构建查询条件：根据搜索关键字模糊查询code字段，并过滤掉无效、已删除和未审批通过的数据
        LambdaQueryWrapper<KolPartnerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(dto.getSearchKeyword())){
            queryWrapper.like(KolPartnerInfoEntity::getNickname, dto.getSearchKeyword());
        }
        queryWrapper.eq(KolPartnerInfoEntity::getIsDeleted, false);
        queryWrapper.orderByDesc(KolPartnerInfoEntity::getCreateTime);
        queryWrapper.orderByDesc(KolPartnerInfoEntity::getDisabled);
        List<KolPartnerInfoEntity> list = this.list(queryWrapper);
        return BeanMapperUtils.copyList(KolPartnerInfoDTO.DropDownDTO.class, list);
    }

    @Override
    public List<KolPartnerInfoDTO.PartnerAddressDTO> partnerAddressList(KolPartnerInfoDTO.AddressSelectDTO dto) {
        return this.baseMapper.partnerAddressList(dto);
    }

}

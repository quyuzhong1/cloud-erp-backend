package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperService;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.KolAddressInfoDTO;
import com.erp.model.oms.dto.KolCooperationPlatformDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_PARTNER_INFO;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CfgKolOptionService cfgKolOptionService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private KolAddressInfoService kolAddressInfoService;
    @Autowired
    private KolCooperationPlatformService kolCooperationPlatformService;
    @Autowired
    private DictLanguageService dictLanguageService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolPartnerInfoDTO.AddDTO addDTO) {
        KolPartnerInfoEntity kolPartnerInfoEntity = new KolPartnerInfoEntity();
        BeanMapperUtils.copy(addDTO, kolPartnerInfoEntity);

        // 数据处理
        String type = String.join(",", addDTO.getTypeList());
        String cooperationType = String.join(",", addDTO.getCooperationTypeList());
        kolPartnerInfoEntity.setType(type);
        kolPartnerInfoEntity.setCooperationType(cooperationType);

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
        String type = String.join(",", addOrUpdateDTO.getTypeList());
        String cooperationType = String.join(",", addOrUpdateDTO.getCooperationTypeList());
        kolPartnerInfoEntity.setType(type);
        kolPartnerInfoEntity.setCooperationType(cooperationType);

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
        return null;
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

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getId, Arrays.asList("cooperationType", "partnerType")).list();
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

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getId, Arrays.asList("cooperationType", "partnerType")).list();
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

}

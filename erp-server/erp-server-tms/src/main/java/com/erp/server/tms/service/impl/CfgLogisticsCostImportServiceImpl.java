package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.KolB2cApplicationDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.server.tms.mapper.CfgLogisticsCostImportMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 费用项配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportMapper, CfgLogisticsCostImportEntity> implements CfgLogisticsCostImportService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;
    @Resource
    private CfgLogisticsCostImportFieldService cfgLogisticsCostImportFieldService;
    @Resource
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsCostImportDTO.AddDTO dto) {
        //校验是否已存在（配置生成单据+平台+识别名称+费用来源+sheet 为唯一）
        Integer count = lambdaQuery()
                .eq(CfgLogisticsCostImportEntity::getBussinessType, dto.getBussinessType())
                .eq(CfgLogisticsCostImportEntity::getDictPlatform, dto.getDictPlatform())
                .eq(CfgLogisticsCostImportEntity::getName, dto.getName())
                .eq(CfgLogisticsCostImportEntity::getSheetName, dto.getSheetName())
                .eq(CfgLogisticsCostImportEntity::getCostType, dto.getCostType())
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.COMMON_HAS_EXIST, "费用配置");
        }


        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = new CfgLogisticsCostImportEntity();
        cfgLogisticsCostImportEntity.setImportType(String.join(",", dto.getImportTypeList()));
        BeanMapperUtils.copy(dto, cfgLogisticsCostImportEntity);
        log.info("开始新增费用项配置");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FYPZ);
        cfgLogisticsCostImportEntity.setCode(code);
        boolean save = super.save(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "费用项配置" , cfgLogisticsCostImportEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), cfgLogisticsCostImportEntity.getId(), "新增操作");

        String id = cfgLogisticsCostImportEntity.getId();
        //处理明细
        List<CfgLogisticsCostImportDetailDTO.AddDTO> detailList = dto.getDetailList();
        List<String> targetFieldIds = detailList.stream().map(CfgLogisticsCostImportDetailDTO.AddDTO::getTargetFieldId).collect(Collectors.toList());
        List<CfgLogisticsCostImportFieldEntity> cfgLogisticsCostImportFieldEntities = cfgLogisticsCostImportFieldService.listByIds(targetFieldIds);
        Map<String, CfgLogisticsCostImportFieldEntity> fieldMap = cfgLogisticsCostImportFieldEntities.stream().collect(Collectors.toMap(CfgLogisticsCostImportFieldEntity::getId, cfgLogisticsCostImportFieldEntity -> cfgLogisticsCostImportFieldEntity,(o1,o2)->o1));
        for (CfgLogisticsCostImportDetailDTO.AddDTO addDTO : detailList) {
            CfgLogisticsCostImportFieldEntity fieldEntity = fieldMap.get(addDTO.getTargetFieldId());
            if(Objects.nonNull(fieldEntity)){
                addDTO.setTargetField(fieldEntity.getField());
                addDTO.setTargetFieldName(fieldEntity.getFieldName());
                addDTO.setTargetFieldType(fieldEntity.getFieldType());
                addDTO.setMainId(id);
            }
        }
        List<CfgLogisticsCostImportDetailEntity> detailEntityList = BeanMapper.copyList(detailList, CfgLogisticsCostImportDetailEntity.class);





        cfgLogisticsCostImportDetailService.saveBatch(detailEntityList);

        return new BaseResultDTO.AddDTO(cfgLogisticsCostImportEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "dto.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsCostImportDTO.UpdateDTO dto) {
        CfgLogisticsCostImportEntity old = super.getById(dto.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置"));
        dto.setImportType(String.join(",", dto.getImportTypeList()));
        CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity =  BeanMapperUtils.map(CfgLogisticsCostImportEntity.class, dto);
        log.info("编辑 开始修改费用项配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgLogisticsCostImportEntity);
        if(!save) {
            throw new ServiceException("费用项配置保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录费用项配置日志数据，单号：【{}】", cfgLogisticsCostImportEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgLogisticsCostImportEntity.getCode(), "费用项配置");
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsCostImportEntity, ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(), cfgLogisticsCostImportEntity.getId(), msg);

        String id = dto.getId();
        List<CfgLogisticsCostImportDetailEntity> detailList = BeanMapper.copyList(dto.getDetailList(), CfgLogisticsCostImportDetailEntity.class);
        detailList.forEach(e -> e.setMainId(id));

        List<CfgLogisticsCostImportDetailEntity> oldDetailList = cfgLogisticsCostImportDetailService.lambdaQuery().eq(CfgLogisticsCostImportDetailEntity::getMainId, id).list();
        commonService.updateDetail(id,ModuleTypeEnum.CFG_LOGISTICS_COST_IMPORT.getCode(),cfgLogisticsCostImportDetailService, detailList, oldDetailList,Arrays.asList("sourceField","sourceDetailField"));
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgLogisticsCostImportDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgLogisticsCostImportDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgLogisticsCostImportDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgLogisticsCostImportDTO.PagingParamDTO searchParam = new CfgLogisticsCostImportDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgLogisticsCostImportDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<CfgLogisticsCostImportDTO.TabListDTO> result = new ArrayList<>();
        result.add(new CfgLogisticsCostImportDTO.TabListDTO("all","全部",0));
        CfgLogisticsCostImportDTO.TabListDTO tTabListDTO = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(new CfgLogisticsCostImportDTO.TabListDTO("t", "", 0));
        tTabListDTO.setTabFlagName("启用");
        CfgLogisticsCostImportDTO.TabListDTO fTabListDTO = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(new CfgLogisticsCostImportDTO.TabListDTO("f", "", 0));
        fTabListDTO.setTabFlagName("停用");
        result.add(tTabListDTO);
        result.add(fTabListDTO);
        return result;
    }


    @Override
    public CfgLogisticsCostImportDTO.ViewDTO view(String id) {
    CfgLogisticsCostImportEntity cfgLogisticsCostImportEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用项配置数据"));
    CfgLogisticsCostImportDTO.ViewDTO data = BeanMapperUtils.map(CfgLogisticsCostImportDTO.ViewDTO.class, cfgLogisticsCostImportEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgLogisticsCostImportDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgLogisticsCostImportDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgLogisticsCostImportDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public void updateDisabled(CfgLogisticsCostImportDTO.UpdateDisabledDTO dto) {

    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        return null;
    }
}

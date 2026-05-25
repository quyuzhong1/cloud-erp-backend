package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.util.CfgLogisticsCostImportEtlRuleHelper;
import com.erp.server.tms.mapper.CfgLogisticsCostImportDetailMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 费用项配置字段配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportDetailServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportDetailMapper, CfgLogisticsCostImportDetailEntity> implements CfgLogisticsCostImportDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Lazy
    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsCostImportDetailDTO.UpdateDTO addDTO) {
        validateMainDetailList(addDTO, false);
        CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity = new CfgLogisticsCostImportDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgLogisticsCostImportDetailEntity);

        // 数据处理
        handleData(cfgLogisticsCostImportDetailEntity);

        log.info("开始新增费用项配置字段配置");
        boolean save = super.save(cfgLogisticsCostImportDetailEntity);
        if(!save) {
            throw new ServiceException("费用项配置字段配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "费用项配置字段配置" , cfgLogisticsCostImportDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgLogisticsCostImportDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgLogisticsCostImportDetailEntity.getId(), cfgLogisticsCostImportDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsCostImportDetailDTO.UpdateDTO addOrUpdateDTO) {
        CfgLogisticsCostImportDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "费用项配置字段配置"));
        if (StrUtil.isBlank(addOrUpdateDTO.getMainId())) {
            addOrUpdateDTO.setMainId(old.getMainId());
        }
        validateMainDetailList(addOrUpdateDTO, true);
        CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity =  BeanMapperUtils.map(CfgLogisticsCostImportDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgLogisticsCostImportDetailEntity);
        log.info("编辑 开始修改费用项配置字段配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgLogisticsCostImportDetailEntity);
        if(!save) {
            throw new ServiceException("费用项配置字段配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录费用项配置字段配置日志数据，id：【{}】", cfgLogisticsCostImportDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgLogisticsCostImportDetailEntity.getId(), "费用项配置字段配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsCostImportDetailEntity, null, cfgLogisticsCostImportDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CfgLogisticsCostImportDetailDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgLogisticsCostImportDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CfgLogisticsCostImportDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgLogisticsCostImportDetailDTO.PagingParamDTO searchParam = new CfgLogisticsCostImportDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgLogisticsCostImportDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(CfgLogisticsCostImportDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new CfgLogisticsCostImportDetailDTO.TabListDTO(status, 0));
        }
        });
        list.add(new CfgLogisticsCostImportDetailDTO.TabListDTO("all", list.stream().mapToInt(CfgLogisticsCostImportDetailDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(CfgLogisticsCostImportDetailDTO.ExportDTO param, HttpServletResponse response) {

    }

    @Override
    public List<CfgLogisticsCostImportDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return  Collections.emptyList();
        }
        return lambdaQuery().in(CfgLogisticsCostImportDetailEntity::getMainId,mainIdList).list();
    }

    /**
     * 校验同一主单下的完整明细配置。
     *
     * @param currentDetail 当前明细
     * @param update 是否修改
     * @return 无
     * @throws ServiceException 主单或明细配置不合法时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void validateMainDetailList(CfgLogisticsCostImportDetailDTO.UpdateDTO currentDetail, boolean update) {
        if (StrUtil.isBlank(currentDetail.getMainId())) {
            throw new ServiceException("主单id不能为空");
        }
        CfgLogisticsCostImportEntity main = cfgLogisticsCostImportService.getById(currentDetail.getMainId());
        if (Objects.isNull(main)) {
            throw new ServiceException("未找到物流费用导入配置主单");
        }
        List<CfgLogisticsCostImportDetailDTO.UpdateDTO> detailList = lambdaQuery()
                .eq(CfgLogisticsCostImportDetailEntity::getMainId, currentDetail.getMainId())
                .list()
                .stream()
                .filter(detail -> !update || !Objects.equals(detail.getId(), currentDetail.getId()))
                .map(detail -> BeanMapperUtils.map(CfgLogisticsCostImportDetailDTO.UpdateDTO.class, detail))
                .collect(Collectors.toList());
        detailList.add(currentDetail);
        cfgLogisticsCostImportService.validateDetailList(main.getBusinessType(), detailList);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity) {
        cfgLogisticsCostImportDetailEntity.setIsAbsoluteValue(false);
        List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList = cfgLogisticsCostImportDetailEntity.getEtlRuleList();
        if (CollUtil.isEmpty(ruleList)) {
            cfgLogisticsCostImportDetailEntity.setEtlRuleList(Collections.emptyList());
            cfgLogisticsCostImportDetailEntity.setEtlRuleListStorage(CfgLogisticsCostImportEtlRuleHelper.EMPTY_ETL_RULE_LIST);
            return;
        }
        int ruleIndex = 1;
        for (CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO : ruleList) {
            CfgLogisticsCostImportEtlRuleHelper.validateEtlRule(ruleDTO, 0, ruleIndex++);
        }
        CfgLogisticsCostImportEtlRuleHelper.normalizeIndexes(ruleList);
        cfgLogisticsCostImportDetailEntity.setEtlRuleListStorage(CfgLogisticsCostImportEtlRuleHelper.buildStorage(ruleList));
    }

    @Override
    public CfgLogisticsCostImportDetailDTO.ViewDTO view(String id) {
    CfgLogisticsCostImportDetailEntity cfgLogisticsCostImportDetailEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用项配置字段配置数据"));
    CfgLogisticsCostImportDetailDTO.ViewDTO data = BeanMapperUtils.map(CfgLogisticsCostImportDetailDTO.ViewDTO.class, cfgLogisticsCostImportDetailEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(CfgLogisticsCostImportDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
        data.setEtlRuleList(CfgLogisticsCostImportEtlRuleHelper.parseStorage(data.getEtlRuleListStorage()));
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<CfgLogisticsCostImportDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(CfgLogisticsCostImportDetailDTO.ListDTO data : list) {
            data.setEtlRuleList(CfgLogisticsCostImportEtlRuleHelper.parseStorage(data.getEtlRuleListStorage()));
        }
   }
}

package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.ShippingTemplateTypeEnum;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.server.tms.mapper.ShippingTemplateMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 运费模板 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateServiceImpl extends SuperServiceImpl<ShippingTemplateMapper, ShippingTemplateEntity> implements ShippingTemplateService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Resource
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;

    @Resource
    private ShippingTemplateRuleService shippingTemplateRuleService;

    @Resource
    private ShippingTemplateOtherCostService shippingTemplateOtherCostService;

    @Resource
    private ShippingRegionCityService shippingRegionCityService;

    @Resource
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @Resource
    private DictBasicService dictBasicService;


    @Override
    public List<ShippingTemplateDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<ShippingTemplateDTO.TabListDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        return dbList;
    }

    @Override
    public PagingVO<ShippingTemplateDTO.ListDTO> paging(PagingDTO<ShippingTemplateDTO.PagingParamDTO> pagingDTO) {
        ShippingTemplateDTO.PagingParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ShippingTemplateDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        //清空明细数据
        List<ShippingTemplateDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        doOpHandleShippingTemplate(records);
        return new PagingVO(pageData);
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateDTO.AddDTO addDTO) {
        ShippingTemplateEntity shippingTemplateEntity = new ShippingTemplateEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateEntity);

        // 数据处理
        handleData(shippingTemplateEntity);

        log.info("开始新增运费模板");
        boolean save = super.save(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }

        //新增运费规则
        shippingTemplateRuleService.add(addDTO.getDetailList(),shippingTemplateEntity.getId());
        //新增其他费用
        shippingTemplateOtherCostService.add(addDTO.getOtherCostList(),shippingTemplateEntity.getId());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "运费模板" , shippingTemplateEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), shippingTemplateEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(shippingTemplateEntity.getId(), shippingTemplateEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateDTO.UpdateDTO updateDTO) {
        ShippingTemplateEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateEntity shippingTemplateEntity =  BeanMapperUtils.map(ShippingTemplateEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateEntity);
        log.info("编辑 开始修改运费模板数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }
        //修改运费规则
        shippingTemplateRuleService.update(updateDTO.getDetailList(),shippingTemplateEntity.getId());
        //修改其他费用
        shippingTemplateOtherCostService.update(updateDTO.getOtherCostList(),shippingTemplateEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录运费模板日志数据，id：【{}】", shippingTemplateEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateEntity.getId(), "运费模板");

        operateLogService.addModuleOperateLogByObj(old, shippingTemplateEntity, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), shippingTemplateEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public BigDecimal trialCalculation(ShippingTemplateDTO.TrialCalculationParamDTO dto) {
        return null;
    }

    @Override
    public ShippingTemplateDTO.ViewDTO view(String id) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateDTO.ViewDTO viewDTO = BeanMapperUtils.map(ShippingTemplateDTO.ViewDTO.class, entity);

        //运费规则
        List<ShippingTemplateRuleEntity> ruleList = shippingTemplateRuleService.listByMainId(id);
        List<ShippingTemplateRuleDTO.ViewDTO> detailList = BeanMapperUtils.copyList(ShippingTemplateRuleDTO.ViewDTO.class, ruleList);
        //判断是否分区类型
        if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(entity.getType())) {
            handleRegionCity(detailList);
        }
        viewDTO.setDetailList(detailList);

        //其他费用
        List<ShippingTemplateOtherCostEntity> costList = shippingTemplateOtherCostService.listByMainId(id);
        List<ShippingTemplateOtherCostDTO.ViewDTO> otherCostList = BeanMapperUtils.copyList(ShippingTemplateOtherCostDTO.ViewDTO.class, costList);
        handleOtherCost(otherCostList);
        viewDTO.setOtherCostList(otherCostList);
        return viewDTO;
    }



    @Override
    public Boolean updateChannel(ShippingTemplateDTO.ChannelParamDTO dto) {
        return null;
    }

    @Override
    public BatchResultDTO updateStatus(String id,Boolean disabled) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));

        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(refList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_DISABLED);
        }

        lambdaUpdate().eq(ShippingTemplateEntity::getId, id)
                .set(ShippingTemplateEntity::getDisabled, disabled)
                .update();

        // 启用/停用日志数据
        log.info("启用/停用 开始启用/停用运费模板单日志数据，id集合：【{}】", JSONObject.toJSONString(id));
        String msg = StrUtil.format("用户【{}】运费模板【{}】的【{}】单据{}操作 ", commonService.getUserInfo().getUserName(), entity.getName(), "运费模板",disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), entity.getName(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DISABLED);
    }

    @Override
    public BatchResultDTO delete(String id) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));

        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(refList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_DELETE);
        }

        //

        // 删除主单数据
        removeById(id);
        // 删除日志数据
        log.info("删除 开始删除运费模板单日志数据，id集合：【{}】", JSONObject.toJSONString(id));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getName(), "运费模板");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getName(), "删除运费模板单数据");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response,String billingMethod,String billingType) {

    }

    @Override
    public Boolean importFile(String billingMethod, String billingType, MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }


    @Override
    public Boolean exportExcel(ShippingTemplateDTO.ExportExcelParamDTO dto, HttpServletResponse response) {
        return null;
    }



    /**
     * @description: 处理分区城市
     * @author Will
     * @date: 2023/11/8 11:44
     * @param detailList
     */
    private void handleRegionCity (List<ShippingTemplateRuleDTO.ViewDTO> detailList) {
        for (ShippingTemplateRuleDTO.ViewDTO viewDTO :detailList) {
            List<ShippingRegionCityEntity> list = shippingRegionCityService.listByRuleId(viewDTO.getId());
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            List<String> cityList = list.stream().map(ShippingRegionCityEntity::getCity).collect(Collectors.toList());
            viewDTO.setCityList(cityList);
        }

    }

    /**
     * @description: 处理其他费用
     * @author Will
     * @date: 2023/11/8 12:08
     * @param otherCostList
     */
    private void handleOtherCost(List<ShippingTemplateOtherCostDTO.ViewDTO> otherCostList) {
        //计算方式
        List<String> otherCostIdList = otherCostList.stream().map(ShippingTemplateOtherCostDTO.ViewDTO::getId).collect(Collectors.toList());
        List<ShippingTemplateCostSettingEntity> list = shippingTemplateCostSettingService.listByOtherCostIds(otherCostIdList);

        for (ShippingTemplateOtherCostDTO.ViewDTO viewDTO : otherCostList) {

            List<ShippingTemplateCostSettingEntity> costSettingEntityList = list.stream().filter(obj -> obj.getOtherCostId().equals(viewDTO.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(costSettingEntityList)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = BeanMapperUtils.copyList(ShippingTemplateCostSettingDTO.ViewDTO.class, costSettingEntityList);
                //计算方式字典
                List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(viewDTO.getCalculationMethod());
                for (ShippingTemplateCostSettingDTO.ViewDTO costSettingDTO : costSettingList) {
                    String name = dictList.stream().filter(obj -> obj.getCode().equals(costSettingDTO.getCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    costSettingDTO.setName(name);
                }
                viewDTO.setCostSettingList(costSettingList);
            }
            //数值设置
            if (StringUtils.isNotBlank(viewDTO.getExtendJson())) {
                ExtendJsonDTO.CommonDTO jsonDTO = JSONUtil.toBean(viewDTO.getExtendJson(), ExtendJsonDTO.CommonDTO.class);
                viewDTO.setExtendJsonDto(jsonDTO);
            }
        }
    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2023/11/6 16:43
     * @param records
     */
    private void doOpHandleShippingTemplate (List<ShippingTemplateDTO.ListDTO> records) {

        //渠道
        List<String> idList = records.stream().map(ShippingTemplateDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(idList);

        for (ShippingTemplateDTO.ListDTO listDTO : records) {
            //模板类型名称
            listDTO.setTypeName(ShippingTemplateTypeEnum.getName(listDTO.getType()));
            //渠道名称
            List<String> channelNameList = refList.stream().filter(obj -> obj.getMainId().equals(listDTO.getId())).map(ShippingTemplateRefChannelDTO.ViewDTO::getLogisticsChannelName).collect(Collectors.toList());
            listDTO.setChannelNameList(channelNameList);
        }

    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateEntity shippingTemplateEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.mapper.CfgSettingMapper;
import com.erp.server.wms.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Case;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {

    @Autowired
    private DictBasicService dictBasicService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO addDTO) {
        // 数据处理
        List<CfgSettingEntity> cfgSettingList = handleData(addDTO);

        log.info("开始新增系统配置管理");
        boolean save = super.saveOrUpdateBatch(cfgSettingList);
        if(!save) {
            throw new ServiceException("系统配置管理保存失败");
        }
        return new BaseResultDTO.AddDTO("", "");
    }


    @Override
    public CfgSettingDTO.ViewDTO view() {
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getKey());
        if (CollectionUtils.isEmpty(dictList)) {
            return viewDTO;
        }
        //查询已有配置信息
        List<CfgSettingEntity> list = listCfgSetting();
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        for (CfgSettingEntity cfgSetting : list) {
            handleViewEnum(cfgSetting,viewDTO);
        }
        return viewDTO;
    }

    @Override
    public CfgSettingEntity getByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        CfgSettingEntity entity = baseMapper.getByKey(key);
        return entity;
    }

    /**
    * 新增修改处理数据
    */
    private List<CfgSettingEntity> handleData(CfgSettingDTO.AddDTO addDTO) {
        List<CfgSettingEntity> list = new ArrayList<>();
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getKey());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //查询已有配置信息
        List<CfgSettingEntity> cfgSettingList = listCfgSetting();

       for (DictBasicDTO.ListDTO listDTO : dictList) {
           //添加数据
           CfgSettingEntity entity = handleAddEnum(listDTO, addDTO,cfgSettingList);
           list.add(entity);
       }
       return  list;
    }


    /**
     * @description: 格式化枚举信息
     * @author Will
     * @date: 2024/1/11 15:15
     * @param listDTO
     * @param addDTO
     */
    private CfgSettingEntity handleAddEnum (DictBasicDTO.ListDTO listDTO,CfgSettingDTO.AddDTO addDTO,List<CfgSettingEntity> cfgSettingList) {
        CfgSettingEntity entity = new CfgSettingEntity();
        //系统配置json
        JSONObject jsonObject = new JSONObject();
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(listDTO.getValue());
        switch (cfgSettingEnum) {
            case SUBCONTRACT_ISSUE:
                 jsonObject = JSONUtil.parseObj(addDTO.getSubcontractIssueSettingDTO());
                break;
            case PO_RETURN:
                jsonObject = JSONUtil.parseObj(addDTO.getPoReturnSettingDTO());
                break;
            case PO_RECONCILIATION:
                handlePoReconciliationSetting(addDTO.getPoReconciliationSettingDTO());
                jsonObject = JSONUtil.parseObj(addDTO.getPoReconciliationSettingDTO());
                break;
            default:
                break;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> StrUtil.equals(obj.getKey(),listDTO.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(listDTO.getSort());
        entity.setKey(listDTO.getValue());
        entity.setDataJson(jsonObject);
        return entity;
    }


    /**
     * @description:
     * @author Will
     * @date: 2024/1/12 11:52
     * @param poReconciliationSettingDTO
     */
    private void handlePoReconciliationSetting (CfgSettingValueDTO.PoReconciliationSettingDTO poReconciliationSettingDTO) {
        //设置时间为空
        if (StrUtil.equals(ReconciliationTypeEnum.CREAT_BY_MONTH.getCode(),poReconciliationSettingDTO.getReconciliationType())) {
            poReconciliationSettingDTO.setEndDate(null);
        }
    }

    /**
     * @description: 格式化枚举信息
     * @author Will
     * @date: 2024/1/11 15:15
     * @param cfgSetting
     * @param viewDTO
     */
    private void handleViewEnum (CfgSettingEntity cfgSetting,CfgSettingDTO.ViewDTO viewDTO) {

        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(cfgSetting.getKey());
        switch (cfgSettingEnum) {
            case SUBCONTRACT_ISSUE:
                CfgSettingValueDTO.SubcontractIssueSettingDTO subcontractIssueSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.SubcontractIssueSettingDTO.class);
                viewDTO.setSubcontractIssueSettingDTO(subcontractIssueSettingDTO);
                break;
            case PO_RETURN:
                CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.PoReturnSettingDTO.class);
                viewDTO.setPoReturnSettingDTO(poReturnSettingDTO);
                break;
            case PO_RECONCILIATION:
                CfgSettingValueDTO.PoReconciliationSettingDTO poReconciliationSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.PoReconciliationSettingDTO.class);
                viewDTO.setPoReconciliationSettingDTO(poReconciliationSettingDTO);
                break;
            default:
                break;
        }
    }


    /**
     * @description: 查询未禁用配置
     * @author Will
     * @date: 2024/1/11 15:57
     * @return List<CfgSettingEntity>
     */
    private List<CfgSettingEntity> listCfgSetting () {
        List<CfgSettingEntity> list = baseMapper.listCfgSetting();
        return list;
    }
}

package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.server.tms.mapper.CfgSettingMapper;
import com.erp.server.tms.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgSettingDTO;
import java.util.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
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
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
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
    /**
     * @description: 格式化枚举信息
     * @author Will
     * @date: 2024/1/11 15:15
     * @param cfgSetting
     * @param viewDTO
     */
    private void handleViewEnum (CfgSettingEntity cfgSetting, CfgSettingDTO.ViewDTO viewDTO) {

        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(cfgSetting.getKey());
        switch (cfgSettingEnum) {
            case LOGISTICS_PRODUCT_DEST_DECLARE_PRICE:
                List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> prices = JSONUtil.toList(cfgSetting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
                viewDTO.setLogisticsProductDestDeclarePrices(prices);
                break;
            default:
                break;
        }
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
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //查询已有配置信息
        List<CfgSettingEntity> cfgSettingList = listCfgSetting();

        for (DictBasicDTO.ViewDTO listDTO : dictList) {
            //添加数据
            CfgSettingEntity entity = handleAddEnum(listDTO, addDTO,cfgSettingList);
            if (Objects.nonNull(entity)){
                list.add(entity);
            }
        }
        return  list;
    }
    /**
     * @description: 格式化枚举信息
     * @author zdy
     * @date: 2024/1/11 15:15
     * @param viewDTO
     * @param addDTO
     * @param cfgSettingList
     */
    private CfgSettingEntity handleAddEnum (DictBasicDTO.ViewDTO viewDTO, CfgSettingDTO.AddDTO addDTO, List<CfgSettingEntity> cfgSettingList) {
        CfgSettingEntity entity = new CfgSettingEntity();
        //系统配置json
        JSONObject jsonObject = new JSONObject();
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(viewDTO.getCode());
        if (Objects.isNull(cfgSettingEnum)){
            return null;
        }
        switch (cfgSettingEnum) {
            case LOGISTICS_PRODUCT_DEST_DECLARE_PRICE:
                JSONArray jsonArray = JSONUtil.parseArray(addDTO.getLogisticsProductDestDeclarePrices());
                jsonObject.putOpt("data", jsonArray);
                break;
            default:
                break;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> StrUtil.equals(obj.getKey(),viewDTO.getCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(viewDTO.getIndex());
        entity.setKey(viewDTO.getCode());
        entity.setDataJson(jsonObject);
        return entity;
    }

    /**
     * @description: 查询未禁用配置
     * @author zdy
     * @date: 2024/1/11 15:57
     * @return List<CfgSettingEntity>
     */
    private List<CfgSettingEntity> listCfgSetting () {
        List<CfgSettingEntity> list = baseMapper.listCfgSetting();
        return list;
    }
}

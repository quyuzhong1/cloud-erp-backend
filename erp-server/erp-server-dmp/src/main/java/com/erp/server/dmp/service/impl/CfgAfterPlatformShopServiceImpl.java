package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.common.business.dto.FindUserDTO;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.mapper.CfgAfterPlatformShopMapper;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.enums.ApiError;
import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
 */
@Slf4j
@Service
public class CfgAfterPlatformShopServiceImpl extends SuperServiceImpl<CfgAfterPlatformShopMapper, CfgAfterPlatformShopEntity> implements CfgAfterPlatformShopService {

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
    * 保存
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<CfgAfterPlatformShopDTO.SaveDTO> save(List<CfgAfterPlatformShopDTO.SaveDTO> saveDTOList) {

        // 检查平台是否重复
        checkPlatformDuplicateWithDB(saveDTOList);

        // 数据转换 & 唯一性校验
        List<CfgAfterPlatformShopEntity> cfgAfterPlatformShopEntities = handleData(saveDTOList);

        // 保存或更新数据
        boolean saveResult = super.saveOrUpdateBatch(cfgAfterPlatformShopEntities);
        if (!saveResult) {
            throw new ServiceException("保存失败");
        }

        return saveDTOList;
    }

    /**
     * 检查平台是否重复
     */
    private void checkPlatformDuplicateWithDB(List<CfgAfterPlatformShopDTO.SaveDTO> saveDTOList) {
        // 提取所有平台编码
        List<String> platforms = saveDTOList.stream()
                .filter(saveDTO -> saveDTO.getId() == null) // 仅校验新增数据
                .map(CfgAfterPlatformShopDTO.SaveDTO::getDictPlatform)
                .collect(Collectors.toList());

        if (!platforms.isEmpty()) {
            // 查询数据库中已存在的平台
            List<CfgAfterPlatformShopEntity> list = this.lambdaQuery()
                    .in(CfgAfterPlatformShopEntity::getDictPlatform, platforms)
                    .list();
            List<String> existingPlatforms = list.stream().map(item -> item.getDictPlatform()).collect(Collectors.toList());

            // 检查是否有重复
            Set<String> existingPlatformSet = new HashSet<>(existingPlatforms);
            for (CfgAfterPlatformShopDTO.SaveDTO saveDTO : saveDTOList) {
                if (saveDTO.getId() == null) {
                    String platform = saveDTO.getDictPlatform();
                    if (existingPlatformSet.contains(platform)) {
                        throw new ServiceException(ApiError.COMMON_PLATFORM_EXSIT, platform);
                    }
                }
            }
        }
    }

    /**
     * 处理数据转换
     */
    private List<CfgAfterPlatformShopEntity> handleData(List<CfgAfterPlatformShopDTO.SaveDTO> saveDTOList) {
        List<CfgAfterPlatformShopEntity> cfgAfterPlatformShopList = new ArrayList<>();
        for (CfgAfterPlatformShopDTO.SaveDTO saveDTO : saveDTOList) {
            CfgAfterPlatformShopEntity entity = new CfgAfterPlatformShopEntity();
            BeanUtils.copyProperties(saveDTO, entity);

            // 处理店铺
            if (saveDTO.getShopIdList() != null && !saveDTO.getShopIdList().isEmpty()) {
                List<String> shopIdList = saveDTO.getShopIdList();
                List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(shopIdList);
                // 构建 JSON 数据
                JSONArray shopArray = new JSONArray();
                for (String id : saveDTO.getShopIdList()) {
                    JSONObject shopInfoObj = new JSONObject();
                    shopInfoObj.set("id", id);

                    ShopInfoEntity shopInfoEntity = shopInfoList.stream()
                            .filter(item -> Objects.equals(id, item.getId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.nonNull(shopInfoEntity)) {
                        shopInfoObj.set("name", shopInfoEntity.getName());
                    }
                    shopArray.add(shopInfoObj);
                }

                JSONObject shopJson = new JSONObject();
                shopJson.putOpt("shops", shopArray);
                entity.setShopJson(shopJson);
            }

            if (saveDTO.getCsAgentIdList() != null && !saveDTO.getCsAgentIdList().isEmpty()) {
                List<String> csAgentIdList = saveDTO.getCsAgentIdList();
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(csAgentIdList);
                // 构建 JSON 数据
                JSONArray csAgentArray = new JSONArray();
                for (String id : saveDTO.getCsAgentIdList()) {
                    JSONObject csAgentObj = new JSONObject();
                    csAgentObj.set("id", id);

                    FindUserDTO userDTO = userList.stream()
                            .filter(item -> Objects.equals(id, item.getUserId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.nonNull(userDTO)) {
                        csAgentObj.set("name", userDTO.getUserName());
                    }
                    csAgentArray.add(csAgentObj);
                }

                JSONObject csAgentJson = new JSONObject();
                csAgentJson.putOpt("csAgents", csAgentArray);
                entity.setCsAgentJson(csAgentJson);
            }

            cfgAfterPlatformShopList.add(entity);
        }

        return cfgAfterPlatformShopList;
    }


    @Override
    public List<CfgAfterPlatformShopDTO.ListDTO> view() {
        List<CfgAfterPlatformShopDTO.ListDTO> resultList = new ArrayList<>();
        List<CfgAfterPlatformShopEntity> list = this.list();
        if (!list.isEmpty()) {
            for (CfgAfterPlatformShopEntity cfgAfterPlatformShopEntity : list) {
                CfgAfterPlatformShopDTO.ListDTO listDTO = new CfgAfterPlatformShopDTO.ListDTO();
                List<CfgAfterPlatformShopDTO.ShopInfoDTO> shopInfoDTOS = new ArrayList<>();
                List<CfgAfterPlatformShopDTO.CsAgentDTO> csAgentDTOS = new ArrayList<>();
                BeanUtils.copyProperties(cfgAfterPlatformShopEntity,listDTO);

                // 反序列化 JSON 到 POJO
                CfgAfterPlatformShopDTO.ShopJsonDTO shopJsonDTO = JSONUtil.toBean(cfgAfterPlatformShopEntity.getShopJson(), CfgAfterPlatformShopDTO.ShopJsonDTO.class);
                CfgAfterPlatformShopDTO.CsAgentJsonDTO csAgentJsonDTO = JSONUtil.toBean(cfgAfterPlatformShopEntity.getCsAgentJson(), CfgAfterPlatformShopDTO.CsAgentJsonDTO.class);

                if (Objects.nonNull(shopJsonDTO)) {
                    shopJsonDTO.getShops().forEach(shop -> {
                        CfgAfterPlatformShopDTO.ShopInfoDTO shopInfoDTO = new CfgAfterPlatformShopDTO.ShopInfoDTO();

                        shopInfoDTO.setId(shop.getId());
                        shopInfoDTO.setName(shop.getName());
                        shopInfoDTOS.add(shopInfoDTO);
                    });
                }

                if (Objects.nonNull(csAgentJsonDTO)) {
                    csAgentJsonDTO.getCsAgents().forEach(csAgent -> {
                        CfgAfterPlatformShopDTO.CsAgentDTO csAgentDTO = new CfgAfterPlatformShopDTO.CsAgentDTO();

                        csAgentDTO.setId(csAgent.getId());
                        csAgentDTO.setName(csAgent.getName());
                        csAgentDTOS.add(csAgentDTO);
                    });
                }

                listDTO.setShopInfoDTOList(shopInfoDTOS);
                listDTO.setCsAgentDTOList(csAgentDTOS);
                resultList.add(listDTO);
            }
        }

        return resultList;
    }

}

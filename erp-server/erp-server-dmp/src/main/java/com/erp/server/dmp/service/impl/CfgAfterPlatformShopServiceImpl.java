package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.mapper.CfgAfterPlatformShopMapper;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.DVALRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.enums.ApiError;
import javax.annotation.Resource;

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
    public boolean save(CfgAfterPlatformShopDTO.SaveDTO saveDTO) {

        List<String> deleteIdList = saveDTO.getDeleteIdList();
        if (deleteIdList != null && !deleteIdList.isEmpty()) {
            boolean deleteSuccess = this.removeByIds(deleteIdList);
            if (!deleteSuccess) {
                throw new ServiceException("删除操作失败");
            }
        }

        // 检查是否有重复的平台 + 店铺组合
        checkPlatformShopDuplicate(saveDTO);

        // 数据转换
        List<CfgAfterPlatformShopEntity> cfgAfterPlatformShopEntities = handleData(saveDTO);

        // 保存或更新数据
        boolean saveResult = super.saveOrUpdateBatch(cfgAfterPlatformShopEntities);
        if (!saveResult) {
            throw new ServiceException("保存失败");
        }

        return saveResult;
    }

    /**
     * 检查平台+店铺组合的唯一性
     */
    /**
     * 检查平台+店铺组合的唯一性（跳过 deleteIdList 中的数据）
     */
    private void checkPlatformShopDuplicate(CfgAfterPlatformShopDTO.SaveDTO saveDTO) {
        Map<String, Set<String>> inputCombinations = new HashMap<>();
        Map<String, Boolean> platformEmptyShopFlag = new HashMap<>();
        List<CfgAfterPlatformShopDTO.AfterPlatfromShopDTO> afterPlatfromShopDTOList = saveDTO.getAfterPlatfromShopDTOList();
        List<String> deleteIdList = saveDTO.getDeleteIdList();
        Set<String> deleteIdSet = deleteIdList != null ? new HashSet<>(deleteIdList) : Collections.emptySet();

        // 查询数据库中已存在的数据（排除 deleteIdList）
        QueryWrapper<CfgAfterPlatformShopEntity> query = new QueryWrapper<>();
        query.ne("shop_json","{}");
        if (deleteIdList != null && !deleteIdList.isEmpty()) {
            query.notIn("id", deleteIdList);
        }

        // 检查入参内部重复 + 数据库重复
        for (CfgAfterPlatformShopDTO.AfterPlatfromShopDTO afterPlatfromShopDTO : afterPlatfromShopDTOList) {

            String platform = afterPlatfromShopDTO.getDictPlatform();
            String currentId = afterPlatfromShopDTO.getId();

            // 如果当前记录在deleteIdList中，则跳过校验
            if (currentId != null && deleteIdSet.contains(currentId)) {
                continue;
            }

            boolean isShopIdListEmpty = afterPlatfromShopDTO.getShopIdList() == null || afterPlatfromShopDTO.getShopIdList().isEmpty();

            // 1. 检查入参内部是否有相同平台且 shopIdList 为空的重复记录
            if (isShopIdListEmpty) {
                if (platformEmptyShopFlag.getOrDefault(platform, false)) {
                    throw new ServiceException(
                            ApiError.COMMON_PLATFORM_SHOP_EXSIT,
                            platform,
                            "（空店铺列表）"
                    );
                }
                platformEmptyShopFlag.put(platform, true); // 标记该平台已有 shopIdList=null 的记录
                continue; // 跳过后续检查
            }

            inputCombinations.putIfAbsent(platform, new HashSet<>());

            for (String shopId : afterPlatfromShopDTO.getShopIdList()) {
                // 检查入参内部重复
                if (!inputCombinations.get(platform).add(shopId)) {
                    ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
                    throw new ServiceException(
                            ApiError.COMMON_PLATFORM_SHOP_EXSIT,
                            platform,
                            shopInfo.getName()
                    );
                }
            }
        }
    }


    /**
     * 处理数据转换
     */
    private List<CfgAfterPlatformShopEntity> handleData(CfgAfterPlatformShopDTO.SaveDTO dto) {
        List<CfgAfterPlatformShopEntity> cfgAfterPlatformShopList = new ArrayList<>();
        List<CfgAfterPlatformShopDTO.AfterPlatfromShopDTO> saveDTOList = dto.getAfterPlatfromShopDTOList();

        List<String> deleteIdList = dto.getDeleteIdList();

        for (CfgAfterPlatformShopDTO.AfterPlatfromShopDTO saveDTO : saveDTOList) {
            // 跳过deleteIdList中包含的ID
            if (deleteIdList.contains(saveDTO.getId())) {
                continue;
            }

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
            } else {
                JSONObject shopJson = new JSONObject();
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

                if (Objects.nonNull(shopJsonDTO) && Objects.nonNull(shopJsonDTO.getShops())) {
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


    public List<CfgAfterPlatformShopDTO.CsAgentDTO> matchCsAgent(String dictPlatform, String shopId) {
        List<CfgAfterPlatformShopDTO.CsAgentDTO> csAgentDTOList = new ArrayList<>();

        // 查询所有的记录
        List<CfgAfterPlatformShopEntity> list = this.lambdaQuery()
                //.eq(CfgAfterPlatformShopEntity::getDictPlatform, dictPlatform)
                .list();

        if (list.isEmpty()) {
            return csAgentDTOList;
        }

        // 优先只按店铺匹配
        if (Objects.nonNull(shopId)) {
            for (CfgAfterPlatformShopEntity entity : list) {
                JSONObject shopJson = entity.getShopJson();
                if (shopJson == null || shopJson.isEmpty()) {
                    continue;
                }

                CfgAfterPlatformShopDTO.ShopJsonDTO shopJsonDTO = JSONUtil.toBean(shopJson, CfgAfterPlatformShopDTO.ShopJsonDTO.class);
                if (shopJsonDTO.getShops() == null || shopJsonDTO.getShops().isEmpty()) {
                    continue;
                }

                // 遍历 shops，匹配 shopId
                for (CfgAfterPlatformShopDTO.Shop shop : shopJsonDTO.getShops()) {
                    if (Objects.equals(shop.getId(), shopId)) {
                        CfgAfterPlatformShopDTO.CsAgentJsonDTO csAgentJsonDTO = JSONUtil.toBean(
                                entity.getCsAgentJson(),
                                CfgAfterPlatformShopDTO.CsAgentJsonDTO.class
                        );
                        if (csAgentJsonDTO != null && csAgentJsonDTO.getCsAgents() != null) {
                            return BeanMapperUtils.copyList(CfgAfterPlatformShopDTO.CsAgentDTO.class, csAgentJsonDTO.getCsAgents());
                        }
                    }
                }
            }
        }

        // 如果店铺匹配失败，或 shopId 为空,则按 平台+空店铺 匹配
        for (CfgAfterPlatformShopEntity entity : list) {
            JSONObject shopJson = entity.getShopJson();
            if (shopJson.isEmpty()) {
                CfgAfterPlatformShopDTO.CsAgentJsonDTO csAgentJsonDTO = JSONUtil.toBean(
                        entity.getCsAgentJson(),
                        CfgAfterPlatformShopDTO.CsAgentJsonDTO.class
                );
                if (csAgentJsonDTO != null && csAgentJsonDTO.getCsAgents() != null) {
                    return BeanMapperUtils.copyList(CfgAfterPlatformShopDTO.CsAgentDTO.class, csAgentJsonDTO.getCsAgents());
                }
            }
        }

        return csAgentDTOList;
    }



}

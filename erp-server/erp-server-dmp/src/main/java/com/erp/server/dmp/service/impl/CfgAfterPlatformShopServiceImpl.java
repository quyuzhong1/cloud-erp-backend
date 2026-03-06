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

        // 检查是否有重复的平台+店铺组合
        checkPlatformShopDuplicate(saveDTO);

        // 数据转换 & 唯一性校验
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
        List<CfgAfterPlatformShopDTO.AfterPlatfromShopDTO> afterPlatfromShopDTOList = saveDTO.getAfterPlatfromShopDTOList();
        List<String> deleteIdList = saveDTO.getDeleteIdList();

        // 查询数据库中已存在的数据（排除 deleteIdList）
        QueryWrapper<CfgAfterPlatformShopEntity> query = new QueryWrapper<>();
        if (deleteIdList != null && !deleteIdList.isEmpty()) {
            query.notIn("id", deleteIdList);
        }
        List<CfgAfterPlatformShopEntity> existingEntities = this.list(query);

        // 检查入参内部重复 + 数据库重复
        for (CfgAfterPlatformShopDTO.AfterPlatfromShopDTO afterPlatfromShopDTO : afterPlatfromShopDTOList) {
            if (afterPlatfromShopDTO.getShopIdList() == null) continue;

            String platform = afterPlatfromShopDTO.getDictPlatform();
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

                // 检查数据库重复（跳过 deleteIdList 中的数据）
                boolean existsInDb = existingEntities.stream()
                        .anyMatch(entity ->
                                entity.getDictPlatform().equals(platform) &&
                                        entity.getShopJson() != null &&
                                        entity.getShopJson().getJSONArray("shops").stream()
                                                .anyMatch(shop -> shopId.equals(((JSONObject) shop).getStr("id")))
                        );

                if (existsInDb) {
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
        for (CfgAfterPlatformShopDTO.AfterPlatfromShopDTO saveDTO : saveDTOList) {
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


    public List<CfgAfterPlatformShopDTO.CsAgentDTO> matchCsAgent(String dictPlatform, String shopId) {
        List<CfgAfterPlatformShopDTO.CsAgentDTO> csAgentDTOList = new ArrayList<>();

        CfgAfterPlatformShopEntity cfgAfterPlatformShop = this.lambdaQuery()
                .eq(CfgAfterPlatformShopEntity::getDictPlatform, dictPlatform)
                .one();

        if (Objects.nonNull(cfgAfterPlatformShop)) {
            // 先处理shopId不为空的情况（平台+店铺匹配）
            if (Objects.nonNull(shopId)) {
                JSONObject shopJson = cfgAfterPlatformShop.getShopJson();
                CfgAfterPlatformShopDTO.ShopJsonDTO shopJsonDTO = JSONUtil.toBean(shopJson, CfgAfterPlatformShopDTO.ShopJsonDTO.class);

                if (Objects.nonNull(shopJsonDTO.getShops()) && !shopJsonDTO.getShops().isEmpty()) {
                    for (CfgAfterPlatformShopDTO.Shop shop : shopJsonDTO.getShops()) {
                        if (Objects.equals(shop.getId(), shopId)) {
                            // 找到匹配的店铺后，获取对应的售后
                            CfgAfterPlatformShopDTO.CsAgentJsonDTO csAgentJsonDTO = JSONUtil.toBean(cfgAfterPlatformShop.getCsAgentJson(), CfgAfterPlatformShopDTO.CsAgentJsonDTO.class);
                            if (csAgentJsonDTO != null && csAgentJsonDTO.getCsAgents() != null) {
                                csAgentDTOList = BeanMapperUtils.copyList(CfgAfterPlatformShopDTO.CsAgentDTO.class, csAgentJsonDTO.getCsAgents());
                            }
                            break;
                        }
                    }
                }
            } else {
                // 处理shopId为空的情况（仅按平台匹配）
                JSONObject csAgentJson = cfgAfterPlatformShop.getCsAgentJson();
                CfgAfterPlatformShopDTO.CsAgentJsonDTO csAgentJsonDTO = JSONUtil.toBean(csAgentJson, CfgAfterPlatformShopDTO.CsAgentJsonDTO.class);
                if (csAgentJsonDTO != null && csAgentJsonDTO.getCsAgents() != null) {
                    csAgentDTOList = BeanMapperUtils.copyList(CfgAfterPlatformShopDTO.CsAgentDTO.class, csAgentJsonDTO.getCsAgents());
                }
            }
        }
        return csAgentDTOList;
    }


}

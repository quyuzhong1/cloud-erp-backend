package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.model.plm.vo.BomVersionVO;
import com.erp.server.plm.mapper.ProductBomHistoryMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * bom 历史表(ProductBomHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@Service
public class ProductBomHistoryServiceImpl extends ServiceImpl<ProductBomHistoryMapper, ProductBomHistoryEntity> implements ProductBomHistoryService {


    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;

    @Resource
    private CommonService commonService;

    @Resource
    private BomSkuService bomSkuService;


    /**
     * 保存bom的历史信息
     *
     * @param bom
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-12 18:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(BomInfoEntity bom, List<BomSkuDTO> bomSkuList) {
        if (bom != null) {
            ProductBomHistoryEntity bomHistory = new ProductBomHistoryEntity();
            bomHistory.setBomId(bom.getId());
            bomHistory.setSerialNumber(bom.getSerialNumber());
            bomHistory.setType(bom.getType());
            bomHistory.setBomVersion(bom.getBomVersion());
            boolean saveFlag = this.save(bomHistory);
            //当保存成功的时候
            if (saveFlag) {
                productBomSkuHistoryService.saveBomSku(bomHistory.getId(), bomSkuList);
            }
        }


    }


    /**
     * 删除bom 信息
     *
     * @param bomId
     * @return void
     * @author yl
     * @date 2023-01-13 9:00
     */
    @Override
    public void deleteByBomId(String bomId) {
        List<ProductBomHistoryEntity> list = this.listByBomId(bomId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> historyBomIdList = list.stream().map(ProductBomHistoryEntity::getId).collect(Collectors.toList());
        //删除历史明细表数据
        productBomSkuHistoryService.removeByHistoryBomIdList(historyBomIdList);
        //删除历史主表数据
        this.removeByIds(historyBomIdList);
    }

    @Override
    public List<BomVersionVO> getVersionList(String bomId) {
        List<BomVersionVO> list = baseMapper.getVersionList(bomId);
        List<FindUserDTO> userList = commonService.getAllUser();
        List<String> bomHistoryIds = list.stream().map(BomVersionVO::getBomHistoryId).collect(Collectors.toList());
        List<ProductBomSkuHistoryEntity> skuList = productBomSkuHistoryService.getSkuByHistoryIds(bomHistoryIds);
        for (BomVersionVO item : list) {
            FindUserDTO findUserDTO = userList.stream().filter(user -> user.getUserId().equals(item.getCreateUserId())).findFirst().orElse(null);
            if (findUserDTO != null) {
                item.setCreateUserName(findUserDTO.getUserName());
            }else{
                item.setCreateUserName("system");
            }
            String bomHistoryId = item.getBomHistoryId();
            List<ProductBomSkuHistoryEntity> refSkuList = skuList.stream().filter(h -> bomHistoryId.equals(h.getBomHistoryId())).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            if (CollectionUtils.isNotEmpty(refSkuList)) {
                List<ProductBomSkuHistoryEntity> childrenSkuList = refSkuList.stream().collect(Collectors.toList());
                //父sku
                String parentSkuNo = refSkuList.get(0).getParentSkuNo();
                sb.append("父物料:").append(parentSkuNo).append(";");
                boolean addFlag = false;
                for (ProductBomSkuHistoryEntity childrenSku : childrenSkuList) {
                    if (addFlag) {
                        sb.append(",");
                    }
                    sb.append("子物料:");
                    sb.append(childrenSku.getSkuNo());
                    sb.append(" 数量:");
                    sb.append(childrenSku.getQuantity());
                    addFlag = true;
                }
            }
            item.setRefSku(sb.toString());
        }
        return list;
    }

    @Override
    public List<ProductBomHistoryEntity> listByBomId(String bomId) {
        List<ProductBomHistoryEntity> list = lambdaQuery().eq(ProductBomHistoryEntity::getBomId, bomId).list();
        return list;
    }

    @Override
    public List<ProductBomHistoryEntity> listByBomIds(List<String> bomIds) {
        if (CollectionUtils.isEmpty(bomIds)) {
            return Collections.emptyList();
        }
        List<ProductBomHistoryEntity> list = lambdaQuery().in(ProductBomHistoryEntity::getBomId, bomIds).list();
        return list;
    }

    @Override
    public List<ProductBomHistoryDTO.VersionDTO> listHistoryVersion(ProductBomHistoryDTO.ParamDTO dto) {
        List<BomChildrenSkuDTO> bomChildrenSkuList = bomSkuService.listBomChildBySkuIds(Arrays.asList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(bomChildrenSkuList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        List<ProductBomHistoryEntity> list = lambdaQuery().eq(ProductBomHistoryEntity::getBomId, bomChildrenSkuList.get(0).getBomId())
                .select(ProductBomHistoryEntity::getBomVersion)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductBomHistoryDTO.VersionDTO> resultList = list.stream().map(obj -> new ProductBomHistoryDTO.VersionDTO(obj.getBomVersion())).collect(Collectors.toList());
        return resultList;
    }

    /**
     * 保存bom 审核通过过的历史数据
     *
     * @param bom
     * @return void
     * @author yl
     * @date 2023-10-11 18:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBomApprovalHistory(BomInfoEntity bom) {
        if (Objects.isNull(bom)) {
            return;
        }
        String bomId = bom.getId();
        //历史版本
        List<ProductBomHistoryEntity> historyList = this.listByBomId(bomId);
        //表示第一次升级
        if(CollectionUtils.isEmpty(historyList)||historyList.size()==1){
            List<BomSkuDTO> bomSkuList = bomSkuService.getByBomId(bomId);
            //先删除历史 bom
            deleteByBomId(bom.getId());
            //历史版本
            ProductBomHistoryEntity bomHistory = new ProductBomHistoryEntity();
            bomHistory.setBomId(bom.getId());
            bomHistory.setSerialNumber(bom.getSerialNumber());
            bomHistory.setType(bom.getType());
            bomHistory.setBomVersion(bom.getBomVersion());
            boolean saveFlag = this.save(bomHistory);
            //当保存成功的时候
            if (saveFlag) {
                productBomSkuHistoryService.saveBomSku(bomHistory.getId(), bomSkuList);
            }
        }

    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(ProductBomHistoryEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), ProductBomHistoryEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), ProductBomHistoryEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId), ProductBomHistoryEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }
}

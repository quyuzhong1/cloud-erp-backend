package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.model.plm.vo.BomVersionVO;
import com.erp.server.plm.mapper.ProductBomHistoryMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
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
    public void insert(BomInfoEntity bom, List<BomSkuDTO> bomSkuList) {
        if (bom != null) {
            ProductBomHistoryEntity bomHistory = new ProductBomHistoryEntity();
            bomHistory.setBomId(bom.getId());
            bomHistory.setSerialNumber(bom.getSerialNumber());
            bomHistory.setType(bom.getType());
            bomHistory.setVersion(bom.getBomVersion());
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
            }
            String bomHistoryId = item.getBomHistoryId();
            List<ProductBomSkuHistoryEntity> refSkuList = skuList.stream().filter(h -> bomHistoryId.equals(h.getBomHistoryId())).collect(Collectors.toList());
            StringBuilder sb=new StringBuilder();
            if (CollectionUtils.isNotEmpty(refSkuList)) {
                List<String> childrenSkuList=refSkuList.stream().map(ProductBomSkuHistoryEntity::getSkuNo).collect(Collectors.toList());
                //父sku
                String parentSkuNo = refSkuList.get(0).getParentSkuNo();
                sb.append("父物料:").append(parentSkuNo).append(";");
                boolean addFlag=false;
                for(String childrenSku:childrenSkuList){
                    if(addFlag){
                        sb.append(",");
                    }
                    sb.append("子物料:");
                    sb.append(childrenSku);
                    addFlag=true;
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
    public List<ProductBomHistoryDTO.VersionDTO> listHistoryVersion(ProductBomHistoryDTO.ParamDTO dto) {
        List<BomSkuEntity> bomList = bomSkuService.getByParentSkuId(dto.getSkuId());
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        List<ProductBomHistoryEntity> list = lambdaQuery().eq(ProductBomHistoryEntity::getBomId, bomList.get(0).getBomId())
                .select(ProductBomHistoryEntity::getVersion)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductBomHistoryDTO.VersionDTO>  resultList= list.stream().map(obj -> new ProductBomHistoryDTO.VersionDTO(obj.getVersion())).collect(Collectors.toList());
        return resultList;
    }
}

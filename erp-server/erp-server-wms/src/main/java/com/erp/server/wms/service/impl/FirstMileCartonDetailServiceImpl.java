package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductPlanSaleInfoEntity;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonBillEntity;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.erp.server.wms.mapper.FirstMileCartonDetailMapper;
import com.erp.server.wms.service.FirstMileCartonBillService;
import com.erp.server.wms.service.FirstMileCartonDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱子信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonDetailServiceImpl extends SuperServiceImpl<FirstMileCartonDetailMapper, FirstMileCartonDetailEntity> implements FirstMileCartonDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private FirstMileCartonBillService firstMileCartonBillService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FirstMileCartonDTO.AddDTO addDTO, String cartonId, String mainId) {
        List<FirstMileCartonDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), FirstMileCartonDetailEntity.class);
        // 数据处理
        handleData(detailEntityList, cartonId, mainId);

        log.info("开始新增发货单箱子信息单");
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        //箱子信息明细
        this.firstMileCartonBillSave(addDTO.getBoxQty(), detailEntityList, cartonId, mainId);
    }

    @Override
    public List<FirstMileCartonDetailEntity> listByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileCartonDetailEntity::getCartonId, cartonIds).list();
    }

    @Override
    public List<FirstMileCartonDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileCartonDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(FirstMileCartonDetailEntity::getCartonId, cartonIds).remove();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(FirstMileCartonDetailEntity::getMainId, mainIds).remove();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FirstMileCartonDetailEntity> detailEntityList, String cartonId, String mainId) {
        for (FirstMileCartonDetailEntity firstMileCartonDetailEntity : detailEntityList) {
            firstMileCartonDetailEntity.setCartonId(cartonId);
            firstMileCartonDetailEntity.setMainId(mainId);
        }
    }

    /**
     * 保存箱子明细信息
     * @Author Luo_WG
     * @Date 2023/11/28 16:36
     * @param boxQty 箱数
     * @param detailEntityList 包装信息
     * @param cartonId 箱规id
     * @return void
     **/
    private void firstMileCartonBillSave(Integer boxQty, List<FirstMileCartonDetailEntity> detailEntityList, String cartonId, String mainId) {
        List<FirstMileCartonBillEntity> firstMileCartonBillEntities = firstMileCartonBillService.listByMainIds(Arrays.asList(mainId));
        Integer maxBoxNo = 0;
        if (CollectionUtils.isNotEmpty(firstMileCartonBillEntities)) {
            maxBoxNo = firstMileCartonBillEntities.stream().max(Comparator.comparingInt(req -> Integer.valueOf(req.getBoxNo()))).map(req -> Integer.valueOf(req.getBoxNo())).get();
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < detailEntityList.size(); i++) {
            sb.append(detailEntityList.get(i).getSkuNo());
            sb.append(StringPool.ASTERISK);
            sb.append(detailEntityList.get(i).getPackQty());
            sb.append(StringPool.PLUS);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 去掉最后的+号
        }
        for (Integer i = maxBoxNo+1; i <= maxBoxNo+boxQty; i++) {
            FirstMileCartonBillDTO.AddDTO billAdd = new FirstMileCartonBillDTO.AddDTO();
            billAdd.setBoxDesc(sb.toString());
            billAdd.setBoxNo(String.valueOf(i));
            billAdd.setCartonId(cartonId);
            billAdd.setMainId(mainId);
            firstMileCartonBillService.add(billAdd);
        }
    }
}

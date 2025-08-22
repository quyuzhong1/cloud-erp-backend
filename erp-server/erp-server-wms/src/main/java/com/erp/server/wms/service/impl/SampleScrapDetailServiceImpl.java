package com.erp.server.wms.service.impl;


import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.server.wms.mapper.SampleScrapDetailMapper;
import com.erp.server.wms.service.SampleScrapDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品报废单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleScrapDetailServiceImpl extends SuperServiceImpl<SampleScrapDetailMapper, SampleScrapDetailEntity> implements SampleScrapDetailService {


    @Override
    public SampleScrapDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<SampleScrapDetailEntity> listByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SampleScrapDetailEntity::getMainId, id).list();
    }

    @Override
    public Map<String,Integer> listBySku(String id,List<String> skuNos){
        return this.baseMapper.listBySku(id,skuNos);
    }


}

package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.TaskDocsCountDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;
import com.erp.server.plm.mapper.ProductArchiveMapper;
import com.erp.server.plm.service.ProductArchiveService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname ProductArchiveServiceImpl
 * @Description TODO
 * @Date 2022-10-09 11:40
 * @Created by yl
 */
@Service
public class ProductArchiveServiceImpl extends ServiceImpl<ProductArchiveMapper, ProductArchiveEntity>
        implements ProductArchiveService {

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;

    @Override
    public PagingVO paging(PagingDTO<ProductSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        List<ProductArchiveDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            //根据产品id 获取到对应的要交付的文档数
            List<TaskDocsCountDTO> productDocs = taskDeliveryService.getTaskDocsCountByProductId();
            //根据产品id 获取到对应完成的文档数
            List<TaskDocsCountDTO> productFinishDocs = finishService.getTaskDocsCountByProductId();

            for(ProductArchiveDTO item:list){
                String productId=item.getProductId();
                //总的文档数
                TaskDocsCountDTO totalDocsDTO=productDocs.stream().filter(p->productId.equals(p.getFlagId())).findFirst().orElse(null);
                if(totalDocsDTO!=null){
                    item.setTotalDocsCount(totalDocsDTO.getCount());
                }else{
                    item.setTotalDocsCount(0);
                }
                //完成的
                TaskDocsCountDTO finishDocsDTO=productFinishDocs.stream().filter(p->productId.equals(p.getFlagId())).findFirst().orElse(null);
                if(finishDocsDTO!=null){
                    item.setFinishDocsCount(finishDocsDTO.getCount());
                }else{
                    item.setFinishDocsCount(0);
                }

            }

        }
        return null;

    }
}

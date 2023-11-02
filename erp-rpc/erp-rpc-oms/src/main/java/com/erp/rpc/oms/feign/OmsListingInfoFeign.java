package com.erp.rpc.oms.feign;

import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "listingInfo")
public interface OmsListingInfoFeign {


    /**
     * 通过条件查询ListingInfoEntity列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @PostMapping("feign/listing/list")
    List<ListingInfoEntity> list(@RequestBody ListingInfoParamDTO dto);
}

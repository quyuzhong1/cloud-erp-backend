package com.erp.rpc.sys.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.vo.MsgChannelConfigDTO;
import com.erp.model.sys.vo.MsgConfigDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Classname 系统管理 服务

 */
@FeignClient(name = "erp-sys" , contextId = "sysPartition" ,configuration = {FeignErrorDecoder.class})
public interface SysPartitionFeign {

    /**
     * 通过国家获取分区
     */
    @PostMapping("feign/partition/getPartitionByCountry")
    String getPartitionByCountry(@RequestBody String country);

    /**
     * 高级查询军区信息
     */
    @PostMapping("feign/partition/listByAdvanceQuery")
    List<DictPartitionEntity> listByAdvanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);

}

package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.server.sys.query.KingdeeOperatorQueryHandler;
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 金蝶架构管理-业务员管理
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶业务员表")
@RequestMapping("/kingdeeOperator")
public class KingdeeOperatorRefPostController extends BaseController {

    @Resource
    private KingdeeOperatorRefPostService kingdeeOperatorRefPostService;



    /**
     * 初始化金蝶数据
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeeOperatorRefPostService.init();
        return result ? success() : failure();
    }


    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = KingdeeOperatorQueryHandler.class)
    public ApiResult<PagingVO<KingdeeOperatorRefPostDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<KingdeeOperatorRefPostDTO.PagingParamDTO> dto) {
        PagingVO<KingdeeOperatorRefPostDTO.PagingViewDTO> pagingVO = kingdeeOperatorRefPostService.paging(dto);
        return success(pagingVO);
    }




    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶业务员表新增")
    public ApiResult<List<BatchResultDTO>> add(@RequestBody @Validated KingdeeOperatorRefPostDTO.AddDTO dto) {
        List<String> userPostIdList = dto.getUserPostIdList();
        String typeCode = dto.getTypeCode();

        List<BatchResultDTO> resultDTOS = new ArrayList<>(userPostIdList.size());
        for (String userPostId : userPostIdList) {
            BatchResultDTO addResult;
            try {
                addResult = kingdeeOperatorRefPostService.add(typeCode,userPostId);
            }catch (Exception e){
                log.error("金蝶业务员 添加失败===>{}", e.getMessage());
                addResult = BatchResultDTO.fail(userPostId, typeCode, e.getMessage());
            }
            resultDTOS.add(addResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>>  delete(@RequestBody  @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kingdeeOperatorRefPostService.delete(id);
            }catch (Exception e){
                log.error("金蝶业务员 删除失败===>{}", e.getMessage());
                KingdeeOperatorRefPostEntity entity = kingdeeOperatorRefPostService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "金蝶业务员不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 业务员列表 用于B2B 销售订单下拉
     */
    @PostMapping("/list")
    public ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> list (@RequestBody KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto) {
        List<UserInfoDTO.BusinessOperationUserDTO> list = kingdeeOperatorRefPostService.listInfo(dto);
        return success(list);
    }

    /**
     * 批量启用/停用
     */
    @PostMapping("/updateState")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult updateState (@RequestBody @Validated KingdeeBusinessOperatorDTO.BatchUpdateDTO dto) {
        kingdeeOperatorRefPostService.updateState(dto);
        return success();
    }
}

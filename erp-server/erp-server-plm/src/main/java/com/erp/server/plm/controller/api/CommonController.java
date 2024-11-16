package com.erp.server.plm.controller.api;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.EnumCacheUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.dto.ProductOperateRecordDTO;
import com.erp.model.plm.dto.TaskConductDTO;
import com.erp.model.plm.entity.ProductOperateRecordEntity;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.ProductOperateRecordService;
import com.erp.server.plm.service.ProjectMembersService;
import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 公共接口
 *
 * @Classname plm

 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@LogSystemModule("PLM通用")
@RequestMapping("common")
public class CommonController extends BaseController {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ProductOperateRecordService productOperateRecordService;

    @Resource
    private ProjectMembersService projectMembersService;

    /**
     * 获取用户
     *
     * @param dto
     * @return
     */
    @PostMapping("/findUserList")
    public ApiResult<List<FindUserDTO>> findUserList(@RequestBody BaseSearchDTO dto) {
        return sysUserFeign.userList(dto);
    }
    /**
     * 获取用户
     *
     * @param dto
     * @return
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<UserSelectDto.PageSelectDTO>> pagingSelect(@RequestBody PagingDTO<UserSelectDto.SelectDTO> dto) {
        return sysUserFeign.pagingSelect(dto);
    }


    /**
     * 获取用户 任务情况
     *
     * @param
     * @return
     */
    @PostMapping("/getUserTask")
    public ApiResult<List<TaskConductDTO>> getUserTask(@RequestBody BaseSearchDTO dto) {
        ApiResult<List<FindUserDTO>> result = sysUserFeign.userList(dto);
        List<TaskConductDTO> list = new ArrayList<>();
        if (result.isSuccess()) {
            //任务负责人 的任务数 是查看 待发布，未开始，进行中
            List<Integer> stateList = new ArrayList<>();
            stateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            stateList.add(TaskStateEnum.NOT_START.getCode());
            stateList.add(TaskStateEnum.ING.getCode());
            list = projectMembersService.getUserTaskConduct((List<FindUserDTO>) result.getData(),stateList);
        }

        return success(list);
    }

    /**
     * 上传图片
     *
     * @param multipartFile 图片流
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     **/
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传图片:文件名={name}")
    @PostMapping("/upload")
    public ApiResult<List<String>> upload(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = FastDFSClientUtil.uploadFile(file);
            list.add(filePath);
        }
        return this.success(list);
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-查询
     *
     * @param productId 产品表id
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductOperateRecordEntity>>
     * @Author Luo_WG
     * @Date 2022/10/11 11:51
     **/
    @GetMapping("/listOperateRecord")
    public ApiResult<List<ProductOperateRecordEntity>> listOperateRecord(@RequestParam(value = "productId") String productId) {
        List<ProductOperateRecordEntity> list = productOperateRecordService.list(productId);
        return this.success(list);
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-新增
     *
     * @param dto 产品操作记录表（VO）
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     **/
    @PostMapping("/saveOrUpdateOperateRecord")
    public ApiResult<Object> saveOrUpdateOperateRecord(@RequestBody ProductOperateRecordDTO dto) {
        Boolean flag = productOperateRecordService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-批量新增
     *
     * @param dto 产品操作记录表
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     **/
    @PostMapping("/saveOrUpdateOperateRecordBatch")
    public ApiResult<Object> saveOrUpdateOperateRecordBatch(@RequestBody List<ProductOperateRecordDTO> dto) {
        Boolean flag = productOperateRecordService.saveOrUpdateBatch(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 获取字典表所有类型
     *
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     **/
    @PostMapping("/listBasicDictType")
    public ApiResult<List<String>> listBasicDictType() {
        return this.success(productOperateRecordService.listBasicDictType());
    }

    /**
     * 批量获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param types
     * @return
     */
    @GetMapping("enumDropDownBatch")
    public ApiResult<Map<String,List<Map<String,Object>>>> enumSelect(@RequestParam(value = "types")List<String> types) {
        Map<String,List<Map<String,Object>>> typeMaps = Maps.newHashMap();
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        if(CollUtil.isNotEmpty(types)) {
            types.stream().forEach(r-> typeMaps.put(r,enumMaps.get(r)));
        }
        return success(typeMaps);
    }

    /**
     * 获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param type
     * @return
     */
    @GetMapping("enumDropDown")
    public ApiResult<List<Map<String,Object>>> enumSelect(@RequestParam(value = "type")String type) {
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        return success(enumMaps.get(type));
    }


}

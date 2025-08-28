package com.erp.sdk.third.kingdee.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.google.gson.Gson;
import com.kingdee.bos.webapi.entity.*;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 金蝶API 处理类
 */
@Component
@Slf4j
public class KingdeeApiUtils {
    public K3CloudApi client;

    private String formId;
    
    private Date createTime;

    private static String APPID;

    private static String USERNAME;

    private static String SERVERURL;

    private static String APPSECRET;

	private static String DCID;

    private static Integer REQUEST_TIME_OUT;

    private static Integer STOCK_TIME_OUT;

    private static String SWITCH_TIME;

    @Value("${openApi.kingdee.switchTime}")
    private void setSwitchTime(String switchTime) {
        KingdeeApiUtils.SWITCH_TIME = switchTime;
    }

    @Value("${openApi.kingdee.appId}")
    public void setAppId(String appId) {
        KingdeeApiUtils.APPID = appId;
    }

    @Value("${openApi.kingdee.userName}")
    public void setUserName(String userName) {
        KingdeeApiUtils.USERNAME = userName;
    }

    @Value("${openApi.kingdee.serverUrl}")
    public void setServerUrl(String serverUrl) {
        KingdeeApiUtils.SERVERURL = serverUrl;
    }

    @Value("${openApi.kingdee.appSecret}")
    public void setAppSecret(String appSecret) {
        KingdeeApiUtils.APPSECRET = appSecret;
    }

    @Value("${openApi.kingdee.dCid}")
    public void setDCid(String dCid) {
        KingdeeApiUtils.DCID = dCid;
    }

    @Value("${openApi.kingdee.requestTimeout}")
    public void setRequestTimeout(Integer requestTimeout) {
        KingdeeApiUtils.REQUEST_TIME_OUT = requestTimeout;
    }

    @Value("${openApi.kingdee.stockTimeout}")
    public void setStockTimeout(Integer stockTimeout) {
        KingdeeApiUtils.STOCK_TIME_OUT = stockTimeout;
    }
    
    public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

    public String getFormId() {
		return formId;
	}

	public KingdeeApiUtils() {
    }

    public KingdeeApiUtils(String formId) {
        IdentifyInfo identifyInfo = new IdentifyInfo();
        identifyInfo.setdCID(DCID);
        identifyInfo.setAppId(APPID);
        identifyInfo.setUserName(USERNAME);
        identifyInfo.setServerUrl(SERVERURL);
        identifyInfo.setAppSecret(APPSECRET);
        this.client = new K3CloudApi(identifyInfo);
        this.formId = formId;
    }

    public KingdeeApiUtils(String url, String userName, String pwd) {
        IdentifyInfo identifyInfo = new IdentifyInfo();
        identifyInfo.setdCID(DCID);
        identifyInfo.setUserName(userName);
        identifyInfo.setServerUrl(url);
        identifyInfo.setPwd(pwd);
        this.client = new K3CloudApi(identifyInfo);
    }

    /**
     * 查询列表(分页查询)
     *
     * @param filterStr 过滤条件, 如 FModifyDate>"2022-01-01" and FCreatorId="1"
     * @param fieldKeys 要显示的字段, 如:FBillNo,FCreatorId,
     * @param pageSize  每页数据行数, 如:100，最大<10000
     * @param pageIndex 页码（第几页)
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> queryList(String filterStr, String fieldKeys, Integer pageSize, Integer pageIndex, Integer topRowCount) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (0 >= pageIndex) {
            pageIndex = 1;
        }
        if (0 >= pageSize) {
            pageSize = 1000;
        }
        Integer startRow = (pageIndex - 1) * pageSize;

        QueryParam param = new QueryParam();
        param.setFormId(formId);
        param.setFieldKeys(fieldKeys);
        param.setFilterString(filterStr);
        param.setLimit(pageSize);
        param.setStartRow(startRow);
        if (0 < topRowCount) {
            param.setTopRowCount(topRowCount);
        }
        String paramJson = JSONUtil.toJsonStr(param);
        try {
            List<List<Object>> apiResult = client.executeBillQuery(paramJson);
            if (CollectionUtils.isEmpty(apiResult)) {
                return dataList;
            }
            if (apiResult.size() == 1 && apiResult.get(0).get(0).toString().contains("IsSuccess=false")) {
                throw new RuntimeException(" ===== 金蝶云星空解析出信息数据失败 ===== " + apiResult);
            }
            for (List<Object> objects : apiResult) {
                Map<String, Object> rowData = KingdeeUtils.getApiDataForMap(fieldKeys, objects);
                dataList.add(rowData);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dataList;
    }

    /**
     * 查询列表(分页查询)
     *
     * @param filterStr 过滤条件, 如 FModifyDate>"2022-01-01" and FCreatorId="1"
     * @param fieldKeys 要显示的字段, 如:FBillNo,FCreatorId,
     * @param pageSize  每页数据行数, 如:100，最大<10000
     * @param pageIndex 页码（第几页)
     * @return 返回数据集(转实体类)
     */
    public List<?> queryList(String filterStr, String fieldKeys, Class entityClass, Integer pageSize, Integer pageIndex) {
        List<?> dataList = null;
        if (0 >= pageIndex) {
            pageIndex = 1;
        }
        if (0 <= pageSize) {
            pageSize = 1000;
        }
        Integer startRow = (pageIndex - 1) * pageSize + 1;

        QueryParam param = new QueryParam();
        param.setFormId(formId);
        param.setFieldKeys(fieldKeys);
        param.setFilterString(filterStr);
        param.setLimit(pageSize);
        param.setStartRow(startRow);

        try {
            dataList = client.executeBillQuery(param, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("金蝶查询列表数据失败[queryList]转Class:" + (Objects.isNull(e.getMessage()) ? e.toString() : e.getMessage()));
        }
        return dataList;
    }

    /**
     * 查看单据数据（按ID）
     *
     * @param id          单据Id
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return 返回操作结果
     */
    public OperatorResult viewById(String id, boolean... ignoreError) {
        OperatorResult result = null;
        OperateParam param = new OperateParam();
        param.setId(id);
        return view(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    /**
     * 查看单据数据（按单据编号）
     *
     * @param number      单据编号
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return 返回操作结果
     */
    public OperatorResult viewByNumber(String number, boolean... ignoreError) {
        OperatorResult result = null;
        OperateParam param = new OperateParam();
        param.setNumber(number);
        return view(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    public OperatorResult view(OperateParam param, boolean ignoreError) {
        OperatorResult result = null;
        try {
            result = client.view(this.formId, param);
            if (!result.isSuccessfully()) {
                if (!ignoreError) {
                    throw new RuntimeException("【查看单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println("【查看单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 查看单据数据（按单据编号）
     *
     * @param jsonData json字符串
     * @return 返回操作结果
     */
    public JSONObject getViewJson(String jsonData) {
        JSONObject json;
        try {
            String view = client.view(this.formId, jsonData);
            JSONObject parse = JSONUtil.parseObj(view);
            JSONObject result = JSONUtil.parseObj(parse.get("Result"));
            JSONObject responseStatus = (JSONObject) result.get("ResponseStatus");
            json = (JSONObject) result.get("Result");
            if (!(Boolean) responseStatus.get("IsSuccess")) {
                throw new RuntimeException("【查看单据】出错:" + responseStatus.get("Errors"));
            }
        } catch (Exception e) {
        	log.error("{}查看金蝶单据出错" , this.formId , e);
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * 查询客户分组
     *
     * @param id
     * @return 返回操作结果
     */
    public JSONObject queryGroupInfo(String id) {
        JSONObject json;
        try {
            LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
            viewMap.put("FormId", this.formId);
            viewMap.put("Ids", id);
            viewMap.put("GroupPkIds", id);
//            viewMap.put("GroupFieldKey","测试分组");
            String jsonData = JSONUtil.toJsonStr(viewMap);
            String view = client.queryGroupInfo(jsonData);
            JSONObject parse = JSONUtil.parseObj(view);
            JSONObject result = JSONUtil.parseObj(parse.get("Result"));

            JSONObject responseStatus = (JSONObject) result.get("ResponseStatus");
            JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(result.get("NeedReturnData")));
            json = (JSONObject) list.get(0);
            if (!(Boolean) responseStatus.get("IsSuccess")) {
                throw new RuntimeException("【查看单据】出错:" + responseStatus.get("Errors"));
            }
            if (json == null) {
                throw new RuntimeException("【客户分组】未查询到数据：" + view);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * 审核 单据(按ID）
     *
     * @param idList      ID列表
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return
     */
    public OperatorResult auditById(List<String> idList, boolean... ignoreError) {
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",", idList));
        return audit(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    /**
     * 审核单据(按单据编号)
     *
     * @param numberList  单据编号列表
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return
     */
    public OperatorResult auditByNumber(List<String> numberList, boolean... ignoreError) {
        OperatorResult result = null;
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        return audit(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    public OperatorResult audit(OperateParam param, boolean ignoreError) {
        OperatorResult result = null;
        try {
            result = client.audit(this.formId, param);
            if (!result.isSuccessfully()) {
                if (!ignoreError) {
                    throw new RuntimeException("【审核单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println("【审核单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
        	if(StringUtils.isNotBlank(param.getIds())) {
        		log.error("{}审核金蝶单据出错{}" , this.formId , param.getIds() , e);
        	}else {
        		log.error("{}审核金蝶单据出错" , this.formId , e);
        	}
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 反审核单据（按Id）
     *
     * @param idList      ID列表
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return
     */
    public OperatorResult unAuditById(List<String> idList, boolean... ignoreError) {
        OperatorResult result;

        OperateParam param = new OperateParam();
        param.setIds(String.join(",", idList));
        return unAudit(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    /**
     * 反审核单据(按单据编号)
     *
     * @param numberList  单据编号列表
     * @param ignoreError （可选) true:忽略错误,false:出现错误时抛出异常
     * @return
     */
    public OperatorResult unAuditByNumber(List<String> numberList, boolean... ignoreError) {
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        return unAudit(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    public OperatorResult unAudit(OperateParam param, boolean ignoreError) {
        OperatorResult result;
        try {
            result = client.unAudit(this.formId, param);
            if (!result.isSuccessfully()) {
                if (!ignoreError) {
                    throw new RuntimeException("【反审核单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println("【反审核单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
        	if(StringUtils.isNotBlank(param.getIds())) {
        		log.error("{}反审核金蝶单据出错{}" , this.formId , param.getIds() , e);
        	}else {
        		log.error("{}反审核金蝶单据出错" , this.formId , e);
        	}
            throw new RuntimeException(e);
        }
        return result;
    }

    public OperatorResult deleteById(List<String> idList, boolean... ignoreError) {
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",", idList));
        return delete(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    public OperatorResult deleteByNumber(List<String> numberList, boolean... ignoreError) {
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        return delete(param, (ObjectUtils.isEmpty(ignoreError) ? false : ignoreError[0]));
    }

    private OperatorResult delete(OperateParam param, boolean ignoreError) {
        OperatorResult result;
        try {
            result = client.delete(this.formId, param);
            if (!result.isSuccessfully()) {
                if (!ignoreError) {
                    throw new RuntimeException("【删除单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println(joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 提交单据
     *
     * @param idList ID列表
     * @return
     */
    public OperatorResult submit(List<String> idList) {
        return submit(idList, false);
    }

    /**
     * 提交单据
     *
     * @param idList ID列表
     * @return
     */
    public OperatorResult submit(List<String> idList, boolean ignoreError) {
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",", idList));
        try {
            result = client.submit(this.formId, param);
            if (!result.isSuccessfully()) {
                if (!ignoreError) {
                    throw new RuntimeException("【提交单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println("【提交单据】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
        	if(StringUtils.isNotBlank(param.getIds())) {
        		log.error("{}提交金蝶单据出错{}" , this.formId , param.getIds() , e);
        	}else {
        		log.error("{}提交金蝶单据出错" , this.formId , e);
        	}
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     *  撤销
     */
    public RepoRet cancelAssign(List<String> idList) {
        return cancelAssign(idList, false);
    }

    /**
     * @description: 撤销
     * @param idList
     * @param ignoreError
     * @return RepoRet
     */
    public RepoRet cancelAssign(List<String> idList, boolean ignoreError) {
        RepoRet  repoRet;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",", idList));
        try {
           String resultJson = client.cancelAssign(this.formId,JSONUtil.toJsonStr(param));
            //用于记录结果
            Gson gson = new Gson();
            //对返回结果进行解析和校验
             repoRet = gson.fromJson(resultJson, RepoRet.class);
            if (repoRet.getResult().getResponseStatus().isIsSuccess()) {
                System.out.printf("接口返回结果: %s%n", gson.toJson(repoRet.getResult()));
            } else {
                if (!ignoreError) {
                    throw new RuntimeException("【撤销单据】出错:" + joinErrors("\r\n", repoRet.getResult().getResponseStatus().getErrors()));
                } else {
                    System.err.println("【撤销单据】出错:" + joinErrors("\r\n", repoRet.getResult().getResponseStatus().getErrors()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return repoRet;
    }

    /**
     * 下推单据
     *
     * @param jsonDate jsonDate
     * @return
     */
    public RepoResult push(JSONObject jsonDate) {
        RepoResult result;
        try {
            String resultJson = client.push(this.formId, jsonDate.toString());
            //用于记录结果
            Gson gson = new Gson();
            //对返回结果进行解析和校验
            RepoRet repoRet = gson.fromJson(resultJson, RepoRet.class);
            if (repoRet.getResult().getResponseStatus().isIsSuccess()) {
                result = repoRet.getResult();
                ArrayList<SuccessEntity> successEntitys = result.getResponseStatus().getSuccessEntitys();
                if (CollectionUtils.isEmpty(successEntitys)) {
                    throw new RuntimeException("下推失败" + successEntitys);
                }
                return result;
            } else {
                throw new RuntimeException("【下推单据】出错:" + gson.toJson(repoRet.getResult().getResponseStatus()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存单据
     *
     * @param data 单据数据
     * @return
     */
    public SaveResult save(SaveParam<?> data) {
        SaveResult result;
        try {
            result = client.save(this.formId, data);
            if (!result.isSuccessfully()) {
                throw new RuntimeException("【保存】出错:" + joinErrors("\r\n", result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * @description: 新增金蝶数据
     * @author Will
     * @date: 2024/5/27 10:56
     * @param data
     * @return RepoResult
     */
    public RepoResult saveKingDee(KingdeeParamDTO.SaveParamDTO data) {
        RepoResult result;
        try {
        	String jsonStr = JSONUtil.toJsonStr(data);
        	log.warn("金蝶保存请求报文：{}" , jsonStr);
			String resultJson = client.save(this.formId, jsonStr);
			log.warn("金蝶保存响应报文：{}" , resultJson);
            //用于记录结果
            Gson gson = new Gson();
            //对返回结果进行解析和校验
            RepoRet repoRet = gson.fromJson(resultJson, RepoRet.class);
            if (repoRet.getResult().getResponseStatus().isIsSuccess()) {
                result = repoRet.getResult();
                ArrayList<SuccessEntity> successEntitys = result.getResponseStatus().getSuccessEntitys();
                if (CollectionUtils.isEmpty(successEntitys)) {
                    throw new RuntimeException("新增失败" + successEntitys);
                }
            } else {
                throw new RuntimeException("【新增数据】出错:" + gson.toJson(repoRet.getResult().getResponseStatus()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 分组保存单据
     *
     * @param data 单据数据
     * @return
     */
    public RepoRet customerGroupSave(KingdeeParamDTO.SaveParamDTO data) {
        RepoRet repoRet;
        try {
            String jsonData = JSONUtil.toJsonStr(data.getModel());
            String view = client.groupSave(this.formId, jsonData);
            //用于记录结果
            Gson gson = new Gson();
            //对返回结果进行解析和校验
            repoRet = gson.fromJson(view, RepoRet.class);
            if (repoRet.getResult().getResponseStatus().isIsSuccess()) {
                return repoRet;
            } else {
                throw new RuntimeException("【查看单据】出错:" + repoRet.getResult().getResponseStatus().getErrors());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return JSONObject
     * @description: 删除客户分组
     */
    public JSONObject customerGroupDelete(String id,String groupFieldKey) {
        JSONObject json;
        try {
            LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
            viewMap.put("FormId", this.formId);
            viewMap.put("GroupFieldKey", groupFieldKey);
            viewMap.put("GroupPkIds", id);
            viewMap.put("FID", id);
            //[{"FID":379804,"FNUMBER":"测试分组","FGROUPID":"66f39f43-f586-4ec7-9419-8e21cf5c7196","FPARENTID":0,"FFULLPARENTID":" ","FLEFT":0,"FRIGHT":0,"FNAME":"测试分组","FDESCRIPTION":" "}]}}
            String jsonData = JSONUtil.toJsonStr(viewMap);
            String view = client.groupDelete(jsonData);
            JSONObject parse = JSONUtil.parseObj(view);
            JSONObject result = JSONUtil.parseObj(parse.get("Result"));
            JSONObject responseStatus = (JSONObject) result.get("ResponseStatus");
            json = (JSONObject) result.get("Result");
            if (!(Boolean) responseStatus.get("IsSuccess")) {
                throw new RuntimeException("【查看单据】出错:" + responseStatus.get("Errors"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * @param operateNumber Forbid禁用、Enable反禁用、Cancel作废、Uncancel反作废
     * @param jsonData
     * @return JSONObject
     * @description: 禁用、反禁用、作废、反作废
     */
    public JSONObject excuteOperation(String operateNumber, String jsonData) {
        JSONObject json;
        try {
            String view = client.excuteOperation(this.formId, operateNumber, jsonData);
            JSONObject parse = JSONUtil.parseObj(view);
            JSONObject result = JSONUtil.parseObj(parse.get("Result"));
            JSONObject responseStatus = (JSONObject) result.get("ResponseStatus");
            json = (JSONObject) result.get("Result");
            if (!(Boolean) responseStatus.get("IsSuccess")) {
                throw new RuntimeException("【查看单据】出错:" + responseStatus.get("Errors"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * @param jsonData
     * @return JSONObject
     * @description: 删除
     */
    public JSONObject delete(String jsonData) {
        JSONObject json;
        try {
            String view = client.delete(this.formId, jsonData);
            JSONObject parse = JSONUtil.parseObj(view);
            JSONObject result = JSONUtil.parseObj(parse.get("Result"));
            JSONObject responseStatus = (JSONObject) result.get("ResponseStatus");
            json = (JSONObject) result.get("Result");
            if (!(Boolean) responseStatus.get("IsSuccess")) {
                throw new RuntimeException("【查看单据】出错:" + responseStatus.get("Errors"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }


    private String joinErrors(String joinStr, ArrayList<RepoError> errors) {
        StringBuffer result = new StringBuffer();
        if (errors.isEmpty()) {
            return "";
        }

        for (RepoError error : errors) {
            result.append(error.getDIndex() + ",");
            result.append(error.getFieldName() + ",");
            result.append(error.getMessage());
            result.append(joinStr);
        }
        return result.replace(1, 1, joinStr).toString();
    }

    /**
     * 拉取时间在切换时间前 则为false 拉取时间在切换时间之后 则为true
     * @param lastTime
     * @return
     */
    public boolean notNeedPushMQ(LocalDateTime lastTime) {
        if (Objects.nonNull(SWITCH_TIME)) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(SWITCH_TIME, df);
            if (Objects.nonNull(lastTime) && lastTime.isAfter(dateTime)) {
                //使用下次调用时间进行判断是否在切换时间之后，在之后就停止调用
                return true;
            }
        }
        return false;
    }
}

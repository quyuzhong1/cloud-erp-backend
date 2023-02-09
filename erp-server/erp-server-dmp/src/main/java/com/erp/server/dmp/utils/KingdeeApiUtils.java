package com.erp.server.dmp.utils;

import com.alibaba.fastjson.JSONObject;
import com.kingdee.bos.webapi.entity.*;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 金蝶API 处理类
 */
@Component
public class KingdeeApiUtils {
    private K3CloudApi client;
    private String formId;

    private static String APPID;

    private static String USERNAME;

    private static String SERVERURL;

    private static String APPSECRET;

    private static String DCID;

    @Value("${openApi.kingdee.appId}")
    public void setAppId(String appId){
        this.APPID = appId;
    }

    @Value("${openApi.kingdee.userName}")
    public void setUserName(String userName){
        this.USERNAME = userName;
    }

    @Value("${openApi.kingdee.serverUrl}")
    public void setServerUrl(String serverUrl){
        this.SERVERURL = serverUrl;
    }

    @Value("${openApi.kingdee.appSecret}")
    public void setAppSecret(String appSecret){
        this.APPSECRET = appSecret;
    }

    @Value("${openApi.kingdee.dCid}")
    public void setDCid(String dCid){
        this.DCID = dCid;
    }

    public KingdeeApiUtils(){
    }

    public KingdeeApiUtils(String formId){
        IdentifyInfo identifyInfo = new IdentifyInfo();
        identifyInfo.setdCID(DCID);
        identifyInfo.setAppId(APPID);
        identifyInfo.setUserName(USERNAME);
        identifyInfo.setServerUrl(SERVERURL);
        identifyInfo.setAppSecret(APPSECRET);
        this.client = new K3CloudApi(identifyInfo);
        this.formId = formId;
    }

    /**
     * 查询列表(分页查询)
     * @param filterStr 过滤条件, 如 FModifyDate>"2022-01-01" and FCreatorId="1"
     * @param fieldKeys 要显示的字段, 如:FBillNo,FCreatorId,
     * @param pageSize  每页数据行数, 如:100，最大<10000
     * @param pageIndex 页码（第几页)
     * @return List<Map<String,Object>>
     */
    public List<Map<String,Object>> queryList(String filterStr, String fieldKeys,Integer pageSize,Integer pageIndex, Integer topRowCount) {
        List<Map<String,Object>> dataList=new ArrayList<>();
        if(0 >= pageIndex){
            pageIndex=1;
        }
        if(0 >= pageSize){
            pageSize=1000;
        }
        Integer startRow=(pageIndex-1) * pageSize;

        QueryParam param = new QueryParam();
        param.setFormId(formId);
        param.setFieldKeys(fieldKeys);
        param.setFilterString(filterStr);
        param.setLimit(pageSize);
        param.setStartRow(startRow);
        if (0 < topRowCount){
            param.setTopRowCount(topRowCount);
        }

        String paramJson = JSONObject.toJSONString(param);
        try {
            List<List<Object>> apiResult = client.executeBillQuery(paramJson);
            if (apiResult.isEmpty()){
                return dataList;
            }
            if (apiResult.size() == 1 && apiResult.get(0).get(0).toString().contains("IsSuccess=false")) {
                throw new RuntimeException(" ===== 金蝶云星空解析出信息数据失败 ===== " + apiResult);
            }

            List<String> numberList=new ArrayList<>();
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
     * @param filterStr 过滤条件, 如 FModifyDate>"2022-01-01" and FCreatorId="1"
     * @param fieldKeys 要显示的字段, 如:FBillNo,FCreatorId,
     * @param pageSize  每页数据行数, 如:100，最大<10000
     * @param pageIndex 页码（第几页)
     * @return 返回数据集(转实体类)
     */
    public List<?> queryList(String filterStr, String fieldKeys,Class entityClass,Integer pageSize,Integer pageIndex) {
        List<?> dataList=null;
        if(0>=pageIndex){
            pageIndex=1;
        }
        if(0<=pageSize){
            pageSize=1000;
        }
        Integer startRow=(pageIndex-1) * pageSize +1;

        QueryParam param = new QueryParam();
        param.setFormId(formId);
        param.setFieldKeys(fieldKeys);
        param.setFilterString(filterStr);
        param.setLimit(pageSize);
        param.setStartRow(startRow);

        try {
            dataList=client.executeBillQuery(param,entityClass);
        } catch (Exception e) {
            throw new RuntimeException("金蝶查询列表数据失败[queryList]转Class:"+ null==e.getMessage()?e.toString():e.getMessage());
        }
        return dataList;
    }

    /**
     * 查看单据数据（按ID）
     * @param id    单据Id
     * @return  返回操作结果
     */
    public OperatorResult view(String id){
        OperatorResult result=null;
        OperateParam param = new OperateParam();
        param.setId(id);
        try {
            result = client.view(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【查看单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 查看单据数据（按单据编号）,现反序列化有误会结果集返回null值
     * @param number    单据编号
     * @return  返回操作结果
     */
    public OperatorResult viewByNumber(String number){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setNumber(number);
        try {
            result = client.view(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【查看单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 查看单据数据（按单据编号）
     * @param number    单据编号
     * @return  返回操作结果
     */
    public JSONObject getViewJson(String number){
        JSONObject json;
        OperateParam param = new OperateParam();
        param.setNumber(number);
        try {
            String view = client.view(this.formId, number);
            JSONObject parse = (JSONObject) JSONObject.parse(view);
            JSONObject result = (JSONObject)parse.get("Result");
            JSONObject responseStatus = (JSONObject)result.get("ResponseStatus");
            json = (JSONObject)result.get("Result");
            if(!(Boolean) responseStatus.get("IsSuccess")){
                throw new RuntimeException("【查看单据】出错:"+result.get("errors").toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * 反审核 单据(按ID）
     * @param idList    ID列表
     * @return
     */
    public OperatorResult auditById(List<String> idList){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",",idList));
        try {
            result = client.audit(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【审核单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 审核单据(按单据编号)
     * @param numberList 单据编号列表
     * @return
     */
    public OperatorResult auditByNumber(List<String> numberList){
        OperatorResult result=null;
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        try {
            result = client.audit(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【审核单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 反审核单据（按Id）
     * @param idList ID列表
     * @return
     */
    public OperatorResult unAuditById(List<String> idList){
        OperatorResult result;

        OperateParam param = new OperateParam();
        param.setIds(String.join(",",idList));
        try {
            result = client.unAudit(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【反审核单据】出错:"+ joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String joinErrors(String joinStr, ArrayList<RepoError> errors) {
        StringBuffer result=new StringBuffer();
        if(errors.isEmpty()){
            return "";
        }

        for(RepoError error:errors){
            result.append(error.getDIndex()+",");
            result.append(error.getFieldName()+",");
            result.append(error.getMessage());
            result.append(joinStr);
        }
        return result.replace(1,1,joinStr).toString();
    }

    /**
     * 反审核单据(按单据编号)
     * @param numberList    单据编号列表
     * @return
     */
    public OperatorResult unAuditByNumber(List<String> numberList){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        try {
            result = client.unAudit(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【反审核单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public OperatorResult deleteById(List<String> idList){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",",idList));
        try {
            result = client.delete(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【删除单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public OperatorResult deleteByNumber(List<String> numberList){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setNumbers(numberList);
        try {
            result = client.delete(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【删除单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 提交单据
     * @param idList    ID列表
     * @return
     */
    public OperatorResult submit(List<String> idList){
        OperatorResult result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",",idList));
        try {
            result = client.submit(this.formId,param);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【提交单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public OperatorResult CancelAssign(List<String> numberList){
        // TODO
        return null;
    }

    /**
     * 下推单据
     * @param idList    ID列表
     * @return
     */
    public String push(List<String> idList){
        // TODO 待测试
        String result;
        OperateParam param = new OperateParam();
        param.setIds(String.join(",",idList));
        String paramJson=JSONObject.toJSONString(param);
        try {
            result = client.push(this.formId,paramJson);
            System.out.println(result);
//            if(!result.isSuccessfully()){
//                throw new RuntimeException("【提交单据】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 保存单据
     * @param data  单据数据
     * @return
     */
    public SaveResult save(SaveParam<?> data){
        SaveResult result;
        try {
            result=client.save(this.formId,data);
            if(!result.isSuccessfully()){
                throw new RuntimeException("【保存】出错:"+joinErrors("\r\n",result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}

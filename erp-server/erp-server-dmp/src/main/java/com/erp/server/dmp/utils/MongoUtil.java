package com.erp.server.dmp.utils;

import com.common.core.anno.AnnoKey;
import com.common.core.anno.Panno;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MapUtil;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class MongoUtil {
	public static String isCheckNull(String str) {//判定是否为空值
        //      System.out.println("!!!");
        if (null == str || str.length() <= 0 || str.equals("null") || str.equals("NULL")) {
            return "";
        }
        str = str.trim();
        return str;
    }
	
	public static Criteria mongoFilter_duplicateKey(Object obj) {
		Criteria criteria = new Criteria();
		Map<String, List<ParamData>> filterParam = getFilterParam(obj);
		return createCriteriaByMap(criteria, filterParam);
	}
	
	private static Map<String, List<ParamData>> getFilterParam(Object obj){
		Class<? extends Object> classType=obj.getClass();
		List<Field> fields = new ArrayList<>() ;
		while(classType !=null && !classType.getSimpleName().toLowerCase().equals("object")) {
			fields.addAll(Arrays.asList(classType .getDeclaredFields()));
			classType = classType.getSuperclass();
		}
		Map<String, List<ParamData>> r = new HashMap<>();
		for(Field field:fields){
			String fieldName=field.getName();
			Object result = invokeMethod(obj, fieldName, null);
			AnnoKey ak = getAnnoKey(field);
			if(result != null && ak != null) {
				ParamData o = new ParamData(ak.getField(), fieldName, ak.getFindType(), result);
				List<ParamData> l = r.get(o.getColum_name());
				if(l == null) {
					l = new ArrayList<>();
				}
				l.add(o);
				r.remove(o.getColum_name());
				r.put(o.getColum_name(), l);
				o = null;
				l = null;
			}
		}
		return r;
	}

	private static Criteria createCriteriaByMap(Criteria criteria,Map<String, List<ParamData>> p) {
		if(p == null || p.size() <= 0) {
			return criteria;
		}
		for(Map.Entry<String, List<ParamData>> entry:p.entrySet()) {
			List<ParamData> list = entry.getValue();
			
			if(list.size()==1) {
				ParamData pd = list.get(0);
				switch (pd.getPe()) {
					case EQ:
						criteria.and(pd.getColum_name()).is(pd.getVal());
						break;
					case IN:
						criteria.and(pd.getColum_name()).in((List<?>)pd.getVal());
						break;
					case GT:
						criteria.and(pd.getColum_name()).gt(pd.getVal());
						break;
					case GTE:
						criteria.and(pd.getColum_name()).gte(pd.getVal());
						break;
					case LIKE:
						criteria.and(pd.getColum_name()).regex("*."+pd.getVal().toString()+".*");
						break;
					case LT:
						criteria.and(pd.getColum_name()).lt(pd.getVal());
						break;
					case LTE:
						criteria.and(pd.getColum_name()).lte(pd.getVal());
						break;
					case EXISTS:
						criteria.and(pd.getColum_name()).exists(Boolean.parseBoolean(pd.getVal().toString()));
						break;
					default:
						break;
				}
				pd = null;
			}else {
//				boolean isBetween = true;
				ParamData st = null;
				ParamData et = null;
				for(ParamData o:list) {
//					if(o.getPe() != PannoEnum.GT && o.getPe() != PannoEnum.GTE && o.getPe() != PannoEnum.LT && o.getPe() != PannoEnum.LTE) {
//						isBetween = false;
//					}
					if(o.getPe() == PannoEnum.GT || o.getPe() == PannoEnum.GTE) {
						st = o;
					}
					if(o.getPe() == PannoEnum.LT || o.getPe() == PannoEnum.LTE) {
						et = o;
					}
				}
				
				if(st != null && et == null) {
					if(st.getPe() == PannoEnum.GT) {
						criteria.and(st.getColum_name()).lt(st.getVal());
					}
					if(st.getPe() == PannoEnum.GTE) {
						criteria.and(st.getColum_name()).lte(st.getVal());
					}
				}else if(st == null && et != null) {
					if(et.getPe() == PannoEnum.LT) {
						criteria.and(et.getColum_name()).lt(et.getVal());
					}
					if(et.getPe() == PannoEnum.LTE) {
						criteria.and(et.getColum_name()).lte(et.getVal());
					}
				}else if(st != null && et != null) {
					criteria.andOperator(getBetweenCriteria(st),getBetweenCriteria(et));
				}
				
//				if(isBetween) {
					
//					criteria.andOperator(getBetweenCriteria(list.get(0)),getBetweenCriteria(list.get(1)));
//				}
			}
		}
		
	
		return criteria;
	}
	
	public static Criteria getBetweenCriteria(ParamData o) {
		switch (o.getPe()) {
			case GT:
				return Criteria.where(o.getColum_name()).gt(o.getVal());
			case GTE:
				return Criteria.where(o.getColum_name()).gte(o.getVal());
			case LT:
				return Criteria.where(o.getColum_name()).lt(o.getVal());
			case LTE:
				return Criteria.where(o.getColum_name()).lte(o.getVal());
			default:
				return null;
		}
	}
	
	public static Criteria mongoFilter(Object obj) {
		Criteria criteria = new Criteria();
		Class<? extends Object> classType=obj.getClass();
		List<Field> fields = new ArrayList<>() ;
		while(classType !=null && !classType.getSimpleName().toLowerCase().equals("object")) {
			fields.addAll(Arrays.asList(classType .getDeclaredFields()));
			classType = classType.getSuperclass();
		}
		for(Field field:fields){
			String fieldName=field.getName();
//			System.out.println(fieldName+"---"+field.getType().getName());
			Object result = invokeMethod(obj, fieldName, null);
			AnnoKey ak = getAnnoKey(field);
			if(result != null && ak != null) {
				criteria = addDataToCriteria(criteria, fieldName, result, ak);
			}
			
//			if(List.class.isAssignableFrom(field.getType())) {
//				field.setAccessible(true);
//				Type t = field.getGenericType();
//				if (t instanceof ParameterizedType) {
//					ParameterizedType pt = (ParameterizedType) t;
//					Class<? extends Object> clz = (Class<?>) pt.getActualTypeArguments()[0];//得到对象list中实例的类型
//					System.out.println(clz.getName());
//				    try {
//						Class<? extends Object> clazz = field.get(obj).getClass();//获取到属性的值的Class对象
//						Method m= clazz.getDeclaredMethod("size");
//					    int size = (Integer) m.invoke(field.get(obj));//调用list的size方法，得到list的长度
//					    for (int i = 0; i < size; i++) {//遍历list，调用get方法，获取list中的对象实例
//					    	Method getM= clazz.getDeclaredMethod("get", int.class);
//					        if(!getM.isAccessible()){
//					            getM.setAccessible(true);
//					        }
//					        System.out.println(getM.invoke(field.get(obj), i));
//					    }
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (NoSuchMethodException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				    
//				}
//			}
		}
		
		return criteria;
	}
	
	
	
	private static Criteria addDataToCriteria(Criteria criteria, String fieldName, Object result, AnnoKey ak) {
		if(ObjectUtils.isEmpty(fieldName) && ObjectUtils.isEmpty(ak.getField())) {
			return criteria;
		}
		if(ObjectUtils.isEmpty(fieldName)) {
			fieldName = ak.getField();
		}
		
		switch (ak.getFindType()) {
			case EQ:
				criteria.and(fieldName).is(result);
				break;
			case IN:
				criteria.and(fieldName).in(result);
				break;
			case GT:
				criteria.and(fieldName).gt(result);
				break;
			case GTE:
				criteria.and(fieldName).gte(result);
				break;
			case LIKE:
				criteria.and(fieldName).regex(result.toString());
				break;
			case LT:
				criteria.and(fieldName).lt(result);
				break;
			case LTE:
				criteria.and(fieldName).lte(result);
				break;
			case EXISTS:
				criteria.and(fieldName).exists(Boolean.parseBoolean(result.toString()));
				break;
			default:
				break;
		}
		return criteria;
	}
	
	public static MapUtil mysqlFilter(Object obj) {
		MapUtil params = new MapUtil();
		Class<? extends Object> classType=obj.getClass();
		List<Field> fields = new ArrayList<>() ;
		while(classType !=null && !classType.getSimpleName().toLowerCase().equals("object")) {
			fields.addAll(Arrays.asList(classType .getDeclaredFields()));
			classType = classType.getSuperclass();
		}
		for(Field field:fields){
			String fieldName=field.getName();
			Object result = invokeMethod(obj, fieldName, null);
			AnnoKey ak = getAnnoKey(field);
			if(result != null && ak != null) {
				params = addDataToMapUtil(params, fieldName, result, ak);
			}
		}
		return params;
	}
	
	private static MapUtil addDataToMapUtil(MapUtil params, String fieldName, Object result, AnnoKey ak) {
		if(ObjectUtils.isEmpty(fieldName) && ObjectUtils.isEmpty(ak.getField())) {
			return params;
		}
		if(ObjectUtils.isEmpty(fieldName)) {
			fieldName = ak.getField();
		}
		switch (ak.getFindType()) {
			case EQ:
				params.put(fieldName+"s", result);
				break;
			case IN:
				params.put(fieldName+"List", result);
				break;
			case GT:
				params.put(fieldName+"st", result);
				break;
			case GTE:
				params.put(fieldName+"ste", result);
				break;
			case LIKE:
				params.put(fieldName+"l", result);
				break;
			case LT:
				params.put(fieldName+"lt", result);
				break;
			case LTE:
				params.put(fieldName+"lte", result);
				break;
			case EXISTS:
				params.put(fieldName+"exists", Boolean.parseBoolean(result.toString()));
				break;
			default:
				break;
		}
		return params;
	}
	
	@SuppressWarnings("unused")
	private static Object getFieldVal(Object obj,Field field) {
		if(List.class.isAssignableFrom(field.getType())) {
			return null;
		}else if(String.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Integer.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Long.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Double.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Date.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Float.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Boolean.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Byte.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(char.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Short.class.isAssignableFrom(field.getType())) {
			return invokeMethod(obj, field.getName(), null);
		}else if(Map.class.isAssignableFrom(field.getType())) {
			return null;
		}else {
			return null;
		}
	}
	
	private static AnnoKey getAnnoKey(Field field) {
		boolean fieldHasAnno = field.isAnnotationPresent(Panno.class);
		if(!fieldHasAnno) {
			return null;
		}
		Panno panno = field.getAnnotation(Panno.class);
		return new AnnoKey(panno.findType()[0], panno.field());
	}
	
	private static Object invokeMethod(Object owner, String fieldname,Object[] args)  {
		
		Object object = null;
        
        try {
        	Class<? extends Object> ownerClass = owner.getClass();
    		Method method = null;
    		method = ownerClass.getMethod(toGetter(fieldname));
        	object = method.invoke(owner);
		} catch (SecurityException e) {
			;
		} catch (NoSuchMethodException e) {
			;
		} catch (IllegalArgumentException e) {
			;
		} catch (IllegalAccessException e) {
			;
		} catch (InvocationTargetException e) {
			;
		}

        return object;
	}
	
	private static String toGetter(String fieldname) {
         if (fieldname == null || fieldname.length() == 0) {
             return null;
         }
//         if (fieldname.length() > 2) {
//             String second = fieldname.substring(1, 2);
//             if (second.equals(second.toUpperCase())) {
//                 return new StringBuffer("get").append(fieldname).toString();
//             }
//         }

         fieldname = new StringBuffer("get").append(fieldname.substring(0, 1).toUpperCase())
                .append(fieldname.substring(1)).toString();

         return  fieldname;
    }
	
	public static String getStrings(Object... args) {
    	if(args == null) return null;
    	StringBuilder sb = new StringBuilder();
    	for (Object str : args) {
			sb.append(str);
		}
    	String str = sb.toString();
    	sb.setLength(0);
    	sb = null;
    	args = null;
    	return str;
    }
	
	@SuppressWarnings({ "unchecked", "unused" })
	private static <T> T getBean(Class<T> checkType,String className) {
		 try {
             Class<T> clz = (Class<T>)Class.forName(className);
             Object obj = clz.newInstance();
             //需要检查checkType是不是obj的字节码对象
             if (!checkType.isInstance(obj)) {
                 throw new Exception("对象跟字节码不兼容");
             }
             return (T)obj;
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
          
         return null;
	}
}

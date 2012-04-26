/*
 * Copyright 2010 Abdullah Shaikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.openforevent.gwt.gwtrpc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericValue;

public class GwtRpcUtil {

    public static final String module = GwtRpcUtil.class.getName();

    public static final HashMap<String, String> toStringMap(Map<String, Object> inMap) {

        HashMap<String, String> outMap = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                outMap.put(entry.getKey(), entry.getValue().toString());
            }
        return outMap;
        }
   
    //
    /*public static final HashMap<String, Object> toObjectMap(Map<String, Object> inMap) {

        HashMap<String, Object> outMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                outMap.put(entry.getKey(), entry.getValue());
            }
        return outMap;
        }*/
    //
   
    public static final HashMap<String, String> toStringMap(Map<String, Object> inMap, String fields) {
       
        HashMap<String, String> outMap = new HashMap<String, String>();
        if(UtilValidate.isEmpty(fields)) {
                outMap = toStringMap(inMap);
        } else {
                String[] fieldsArray = fields.split(",");
                for(int i=0; i<fieldsArray.length; i++) {
                        Object obj = inMap.get(fieldsArray[i]);
                        String value = (obj == null) ? null : obj.toString();  
                        outMap.put(fieldsArray[i], value);
                        }
        }
        return outMap;
        }

    public static final HashMap<String, String> toStringMap(GenericValue inMap) {

        HashMap<String, String> outMap = new HashMap<String, String>();
       
        Map<String, Object> gvMap = inMap.getAllFields();
                for (Map.Entry<String, Object> entry : gvMap.entrySet()) {
                outMap.put(entry.getKey(), entry.getValue().toString());
            }
        return outMap;
        }
   
    public static final HashMap<String, String> toStringMap(GenericValue inMap, String fields) {
       
        HashMap<String, String> outMap = new HashMap<String, String>();
        if(UtilValidate.isEmpty(fields)) {
                outMap = toStringMap(inMap);
        } else {
                String[] fieldsArray = fields.split(",");
                for(int i=0; i<fieldsArray.length; i++) {
                        outMap.put(fieldsArray[i], inMap.getString(fieldsArray[i]));
                        }
        }
        return outMap;
        }

    public static final List<HashMap<String, String>> toStringMapList(List<GenericValue> gvList, String fields) {
               
                List<HashMap<String, String>> outList = new ArrayList<HashMap<String, String>>();
               
                String[] fieldsArray = null;

        if(fields != null) {
                fieldsArray = fields.split(",");
        }

        for (GenericValue genericValue : gvList) {

                HashMap<String, String> gv = new HashMap<String, String>();
               
                if(fieldsArray != null) {

                        for(int i=0; i<fieldsArray.length; i++) {
                                gv.put(fieldsArray[i], genericValue.getString(fieldsArray[i]));
                        }
                       
                } else {
                       
                        Map<String, Object> gvMap = genericValue.getAllFields();
                       
                        Iterator<String> keys = gvMap.keySet().iterator();
                       
                        while(keys.hasNext()) {
                               
                                String key = keys.next();
                                gv.put(key, gvMap.get(key).toString());
                        }
                       
                }

                outList.add(gv);
        }

        return outList;
        }

}



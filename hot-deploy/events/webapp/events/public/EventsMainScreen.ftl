<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<html lang="lt" dir="ltr" xmlns="http://www.w3.org/1999/xhtml">
    <head>
    <title>Open For Event</title>
    
    <#if layoutSettings.javaScripts?has_content>
        <#assign javaScriptsSet = Static["org.ofbiz.base.util.UtilMisc"].toSet(layoutSettings.javaScripts)/>
        <#list layoutSettings.javaScripts as javaScript>
            <#if javaScriptsSet.contains(javaScript)>
                <#assign nothing = javaScriptsSet.remove(javaScript)/>
                <script src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>" type="text/javascript"></script>
            </#if>
        </#list>
    </#if>
</head>
<body>

<script type="text/javascript">
    // check for Geolocation support
    if (navigator.geolocation) {
        console.log('Geolocation is supported1!');
        navigator.geolocation.getCurrentPosition(successCallback, errorCallback);
        console.log('End1');
    }else {
        console.log('Geolocation is not supported for this Browser/OS version yet.');
    }
    
    function successCallback(pos) {
        console.log("Latitude: " + pos.coords.latitude + " , Longitude: " + pos.coords.longitude);
        
        jQuery.ajax({
            url: '<@ofbizUrl>userLocationProperties</@ofbizUrl>',
            type: 'POST',
            data: {"latitude" : pos.coords.latitude, "longitude" : pos.coords.longitude},
            error: function(msg) {
                alert("An error occured loading content! : " + msg);
            },
            success: function(msg) {
                alert("Success loading content! : " + msg);
            }
        });
    }
    
    function errorCallback (err) {
        console.log('errorCallback'+err.code);
    }
    
</script>

<div>
Result abbreviation - ${result.abbreviation}
</div>

</body>
</html>

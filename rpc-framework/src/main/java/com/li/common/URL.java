package com.li.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class URL {
    private String protocol;

    private  String host;

    private  int port;

    private String interfaceName;

    private String group;

    private String version;

    private int order;

    private Map<String, Object> attribute = new HashMap<>();

    public URL(String protocol, String host, int port, String interfaceName, String group, String version) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.version = version;
        this.group = group;
        this.interfaceName = interfaceName;
    }

    public URL(String protocol, String host, int port, String interfaceName, String group, String version, int order) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.interfaceName = interfaceName;
        this.group = group;
        this.version = version;
        this.order = order;
    }

    public static URL getUrl(String str){
        String[] split = str.split(":");
        String pro = split[0];
        if("registry".equals(pro)){
            new URL(pro, split[1], Integer.parseInt(split[2]), split[3], split[4], split[5]);
        }
        return new URL(pro, split[1], Integer.parseInt(split[2]), split[3], split[4], split[5], Integer.parseInt(split[6]));
    }


    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(protocol + ":");
        str.append(host);
        str.append(":" + port);
        str.append(":" + interfaceName);
        str.append(":" + group);
        str.append(":" + version);
        if(!"registry".equals(protocol))str.append(":" + order);
        return  str.toString();
    }

    public String getPath(){
        return "/rpcTest/" + interfaceName;
    }

    public String getKey(){
        return interfaceName + "." + version;
    }

    public String getAddress(){
        return host + port;
    }

}

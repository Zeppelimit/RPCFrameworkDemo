package com.li.common;

import com.li.spring.ReferenceBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReferenceManager {

    public static final String BEAN_NAME = "referenceManager";

    public static Map<String, String> referenceList = new ConcurrentHashMap<>();

    public static List<ReferenceBean> referenceBeanList = new CopyOnWriteArrayList<>();
}

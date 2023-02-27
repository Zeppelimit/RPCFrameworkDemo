package com.li.registry;

import com.li.common.URL;

import java.util.List;

public interface NotifyListener {
    void notify(List<URL> urls);
}

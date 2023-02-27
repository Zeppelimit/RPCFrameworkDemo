package com.li.rpc.remoting;

import com.li.transport.client.NettyClient;
import com.li.transport.server.NettyServer;

public interface Transporter {
    NettyServer bind(String address);

    NettyClient connect(String address);
}

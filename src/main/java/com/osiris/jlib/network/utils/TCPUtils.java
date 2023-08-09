package com.osiris.jlib.network.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Some useful methods for server side.
 */
public final class TCPUtils {

    private TCPUtils() {
    }

    public static SslContext buildSslContext() throws CertificateException, SSLException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder
                .forServer(ssc.certificate(), ssc.privateKey())
                .build();
    }

    private static final ConcurrentHashMap<Integer, Integer> mapNames = new ConcurrentHashMap<>();
    private static final AtomicInteger a = new AtomicInteger();

    public static <T> String simpleName(T t) {
        Integer instance = mapNames.get(t.hashCode());
        if (instance == null) {
            instance = a.incrementAndGet();
            mapNames.put(t.hashCode(), instance);
        }
        return t.getClass().getSimpleName() + " " + instance;
    }
}

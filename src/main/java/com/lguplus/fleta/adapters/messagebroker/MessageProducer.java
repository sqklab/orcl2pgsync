package com.lguplus.fleta.adapters.messagebroker;

@FunctionalInterface
public interface MessageProducer<K, V> {

	String send(K key, V value);
}

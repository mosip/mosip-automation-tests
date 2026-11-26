package io.mosip.testrig.dslrig.packetcreator.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class PacketCreatorActivityTracker {

	private static final AtomicInteger ACTIVE = new AtomicInteger();
	private static final AtomicLong LAST_BUSINESS_ACTIVITY_MS = new AtomicLong(System.currentTimeMillis());

	private PacketCreatorActivityTracker() {
	}

	public static void enter() {
		ACTIVE.incrementAndGet();
		touchBusinessActivity();
	}

	public static void leave() {
		ACTIVE.updateAndGet(current -> Math.max(0, current - 1));
		touchBusinessActivity();
	}

	public static void touchBusinessActivity() {
		LAST_BUSINESS_ACTIVITY_MS.set(System.currentTimeMillis());
	}

	public static int activeCount() {
		return ACTIVE.get();
	}

	public static long lastBusinessActivityEpochMs() {
		return LAST_BUSINESS_ACTIVITY_MS.get();
	}

	public static long idleMillis() {
		return Math.max(0L, System.currentTimeMillis() - LAST_BUSINESS_ACTIVITY_MS.get());
	}
}

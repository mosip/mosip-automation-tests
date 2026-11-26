package io.mosip.testrig.dslrig.packetcreator.service;

import java.util.concurrent.atomic.AtomicInteger;

public final class PacketCreatorActivityTracker {

	private static final AtomicInteger ACTIVE = new AtomicInteger();

	private PacketCreatorActivityTracker() {
	}

	public static void enter() {
		ACTIVE.incrementAndGet();
	}

	public static void leave() {
		ACTIVE.updateAndGet(current -> Math.max(0, current - 1));
	}

	public static int activeCount() {
		return ACTIVE.get();
	}
}

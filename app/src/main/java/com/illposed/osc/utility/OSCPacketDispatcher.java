/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;


// import .ListenerPatterns;
// import .Main;
import com.illposed.osc.AddressSelector;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Dispatches {@link OSCPacket}s to registered listeners (<i>Method</i>s).
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class OSCPacketDispatcher {

	private final Map<AddressSelector, OSCListener> selectorToListener;
	public String ERROR_DISPATCH;

	public OSCPacketDispatcher() {
		this.selectorToListener = new HashMap<AddressSelector, OSCListener>();
	}

	/**
	 * Adds a listener (<i>Method</i> in OSC speak) that will be notified
	 * of incoming messages that match the selector.
	 * @param addressSelector selects which messages will be forwarded to the listener,
	 *   depending on the message address
	 * @param listener receives messages accepted by the selector
	 */
	public void addListener(final AddressSelector addressSelector, final OSCListener listener) {
		selectorToListener.put(addressSelector, listener);
	}

	public void dispatchPacket(final OSCPacket packet) {
		dispatchPacket(packet, null);
	}

	public void dispatchPacket(final OSCPacket packet, final Date timestamp) {
		if (packet instanceof OSCBundle) {
			dispatchBundle((OSCBundle) packet);
		} else {
			dispatchMessage((OSCMessage) packet, timestamp);
		}
	}

	private void dispatchBundle(final OSCBundle bundle) {
		final Date timestamp = bundle.getTimestamp();
		final List<OSCPacket> packets = bundle.getPackets();
		for (final OSCPacket packet : packets) {
			dispatchPacket(packet, timestamp);
		}
	}

//	OLD FUNCTION
//	private void dispatchMessage(final OSCMessage message, final Date time) {
//		for (final Entry<AddressSelector, OSCListener> addrList : selectorToListener.entrySet()) {
//			if (addrList.getKey().matches(message.getAddress())) {
//				addrList.getValue().acceptMessage(time, message);
//			}
//		}
//	}

	/**
	 * FUNCTION MODIFICATED - ORIGINAL ABOVE THIS
	 * ONLY ACCEPTS THE MESSAGES THAT WAS SET IN "isReaperPattern" (CLASS CREATED ONLY FOR THIS APP)
	 * TRY / CATCH AVOIDS THE PROGRAM CRASHES
	 * @param message osc message received
	 * @param time current time
	 */
	private void dispatchMessage(final OSCMessage message, final Date time) {
		try {
			for (final Entry<AddressSelector, OSCListener> addrList : selectorToListener.entrySet()) {
				//if (message.getAddress() != null && ListenerPatterns.isReaperPattern(message.getAddress())) {
				//	addrList.getValue().acceptMessage(time, message);
				//}
			}
		}catch (ConcurrentModificationException e){
			ERROR_DISPATCH = e.toString();
		}catch (Exception e){
            ERROR_DISPATCH = e.toString();
        }
	}
}

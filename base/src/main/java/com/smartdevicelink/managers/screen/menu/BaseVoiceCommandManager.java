/*
 * Copyright (c) 2019 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.smartdevicelink.managers.screen.menu;

import android.support.annotation.NonNull;

import com.smartdevicelink.managers.BaseSubManager;
import com.smartdevicelink.managers.CompletionListener;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.DeleteCommand;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.PredefinedWindows;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.listeners.OnMultipleRequestListener;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;
import com.smartdevicelink.util.DebugTool;

import java.util.ArrayList;
import java.util.List;

abstract class BaseVoiceCommandManager extends BaseSubManager {

	List<VoiceCommand> voiceCommands, oldVoiceCommands;

	List<AddCommand> inProgressUpdate;

	int lastVoiceCommandId;
	private static final int voiceCommandIdMin = 1900000000;

	boolean waitingOnHMIUpdate;
	boolean hasQueuedUpdate;

	HMILevel currentHMILevel;
	OnRPCNotificationListener hmiListener;
	OnRPCNotificationListener commandListener;

	// CONSTRUCTORS

	BaseVoiceCommandManager(@NonNull ISdl internalInterface) {
		super(internalInterface);

		currentHMILevel = HMILevel.HMI_NONE;
		addListeners();
		lastVoiceCommandId = voiceCommandIdMin;
	}

	@Override
	public void start(CompletionListener listener) {
		transitionToState(READY);
		super.start(listener);
	}

	@Override
	public void dispose(){

		lastVoiceCommandId = voiceCommandIdMin;
		voiceCommands = null;
		oldVoiceCommands = null;

		waitingOnHMIUpdate = false;
		currentHMILevel = null;
		inProgressUpdate = null;
		hasQueuedUpdate = false;

		// remove listeners
		internalInterface.removeOnRPCNotificationListener(FunctionID.ON_HMI_STATUS, hmiListener);
		internalInterface.removeOnRPCNotificationListener(FunctionID.ON_COMMAND, commandListener);

		super.dispose();
	}

	// SETTERS

	public void setVoiceCommands(List<VoiceCommand> voiceCommands){

		// we actually need voice commands to set.
		if (voiceCommands == null || voiceCommands.size() == 0){
			DebugTool.logInfo("Trying to set empty list of voice commands, returning");
			return;
		}

		// make sure hmi is not none
		if (currentHMILevel == null || currentHMILevel == HMILevel.HMI_NONE){
			// Trying to send on HMI_NONE, waiting for full
			this.voiceCommands = new ArrayList<>(voiceCommands);
			waitingOnHMIUpdate = true;
			return;
		}

		waitingOnHMIUpdate = false;
		lastVoiceCommandId = voiceCommandIdMin;
		updateIdsOnVoiceCommands(voiceCommands);
		this.oldVoiceCommands = new ArrayList<>();
		if (this.voiceCommands != null && !this.voiceCommands.isEmpty()) {
			this.oldVoiceCommands.addAll(this.voiceCommands);
		}
		this.voiceCommands = new ArrayList<>(voiceCommands);

		update();
	}

	public List<VoiceCommand> getVoiceCommands(){
		return voiceCommands;
	}

	// UPDATING SYSTEM

	private void update(){

		if (currentHMILevel == null || currentHMILevel.equals(HMILevel.HMI_NONE)){
			waitingOnHMIUpdate = true;
			return;
		}

		if (inProgressUpdate != null){
			// There's an in-progress update, put this on hold
			hasQueuedUpdate = true;
			return;
		}

		sendDeleteCurrentVoiceCommands(new CompletionListener() {
			@Override
			public void onComplete(boolean success) {
				// we don't care about errors from deleting, send new add commands
				sendCurrentVoiceCommands(new CompletionListener() {
					@Override
					public void onComplete(boolean success2) {
						inProgressUpdate = null;

						if (hasQueuedUpdate){
							update();
							hasQueuedUpdate = false;
						}

						if (!success2){
							DebugTool.logError("Error sending voice commands");
						}
					}
				});
			}
		});

	}

	// DELETING OLD MENU ITEMS

	private void sendDeleteCurrentVoiceCommands(final CompletionListener listener){

		if (oldVoiceCommands == null || oldVoiceCommands.size() == 0){
			if (listener != null){
				listener.onComplete(true);
			}
			return;
		}

		List<DeleteCommand> deleteVoiceCommands = deleteCommandsForVoiceCommands(oldVoiceCommands);
		oldVoiceCommands.clear();
		internalInterface.sendRequests(deleteVoiceCommands, new OnMultipleRequestListener() {
			@Override
			public void onUpdate(int remainingRequests) {

			}

			@Override
			public void onFinished() {
				DebugTool.logInfo("Successfully deleted old voice commands");
				if (listener != null){
					listener.onComplete(true);
				}
			}

			@Override
			public void onError(int correlationId, Result resultCode, String info) {

			}

			@Override
			public void onResponse(int correlationId, RPCResponse response) {}
		});

	}

	// SEND NEW MENU ITEMS

	private void sendCurrentVoiceCommands(final CompletionListener listener){

		if (voiceCommands == null || voiceCommands.size() == 0){
			if (listener != null){
				listener.onComplete(true); // no voice commands to send doesnt mean that its an error
			}
			return;
		}

		inProgressUpdate = addCommandsForVoiceCommands(voiceCommands);

		internalInterface.sendRequests(inProgressUpdate, new OnMultipleRequestListener() {
			@Override
			public void onUpdate(int remainingRequests) {

			}

			@Override
			public void onFinished() {
				DebugTool.logInfo("Sending Voice Commands Complete");
				if (listener != null){
					listener.onComplete(true);
				}
				oldVoiceCommands = voiceCommands;
			}

			@Override
			public void onError(int correlationId, Result resultCode, String info) {

			}

			@Override
			public void onResponse(int correlationId, RPCResponse response) {
			}
		});
	}

	// DELETES

	List<DeleteCommand> deleteCommandsForVoiceCommands(List<VoiceCommand> voiceCommands){
		List<DeleteCommand> deleteCommandList = new ArrayList<>();
		for (VoiceCommand command : voiceCommands){
			DeleteCommand delete = new DeleteCommand(command.getCommandId());
			deleteCommandList.add(delete);
		}
		return deleteCommandList;
	}

	// COMMANDS

	List<AddCommand> addCommandsForVoiceCommands(List<VoiceCommand> voiceCommands){
		List<AddCommand> addCommandList = new ArrayList<>();
		for (VoiceCommand command : voiceCommands){
			addCommandList.add(commandForVoiceCommand(command));
		}
		return addCommandList;
	}

	private AddCommand commandForVoiceCommand(VoiceCommand voiceCommand){
		AddCommand command = new AddCommand(voiceCommand.getCommandId());
		command.setVrCommands(voiceCommand.getVoiceCommands());
		return command;
	}

	// HELPERS

	private void updateIdsOnVoiceCommands(List<VoiceCommand> voiceCommands){
		for (VoiceCommand command : voiceCommands){
			command.setCommandId(++lastVoiceCommandId);
		}
	}

	// LISTENERS

	private void addListeners(){

		// HMI UPDATES
		hmiListener = new OnRPCNotificationListener() {
			@Override
			public void onNotified(RPCNotification notification) {
				OnHMIStatus onHMIStatus = (OnHMIStatus)notification;
				if (onHMIStatus.getWindowID() != null && onHMIStatus.getWindowID() != PredefinedWindows.DEFAULT_WINDOW.getValue()) {
					return;
				}
				HMILevel oldHMILevel = currentHMILevel;
				currentHMILevel = onHMIStatus.getHmiLevel();
				// Auto-send an update if we were in NONE and now we are not
				if (oldHMILevel == HMILevel.HMI_NONE && currentHMILevel != HMILevel.HMI_NONE){
					if (waitingOnHMIUpdate){
						setVoiceCommands(voiceCommands);
					}
				}
			}
		};
		internalInterface.addOnRPCNotificationListener(FunctionID.ON_HMI_STATUS, hmiListener);

		// COMMANDS
		commandListener = new OnRPCNotificationListener() {
			@Override
			public void onNotified(RPCNotification notification) {
				OnCommand onCommand = (OnCommand) notification;
				if (voiceCommands != null && voiceCommands.size() > 0){
					for (VoiceCommand command : voiceCommands){
						if (onCommand.getCmdID() == command.getCommandId()){
							if (command.getVoiceCommandSelectionListener() != null) {
								command.getVoiceCommandSelectionListener().onVoiceCommandSelected();
								break;
							}
						}
					}
				}
			}
		};
		internalInterface.addOnRPCNotificationListener(FunctionID.ON_COMMAND, commandListener);
	}
}

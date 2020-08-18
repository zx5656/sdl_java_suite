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
package com.smartdevicelink.managers.screen;

import androidx.annotation.NonNull;

import com.livio.taskmaster.Queue;
import com.livio.taskmaster.Task;
import com.smartdevicelink.managers.BaseSubManager;
import com.smartdevicelink.managers.CompletionListener;
import com.smartdevicelink.managers.ManagerUtility;
import com.smartdevicelink.managers.file.FileManager;
import com.smartdevicelink.managers.file.MultipleFileCompletionListener;
import com.smartdevicelink.managers.file.filetypes.SdlArtwork;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.managers.lifecycle.SystemCapabilityManager;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.interfaces.OnSystemCapabilityListener;
import com.smartdevicelink.proxy.rpc.DisplayCapability;
import com.smartdevicelink.proxy.rpc.MetadataTags;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.WindowCapability;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.ImageFieldName;
import com.smartdevicelink.proxy.rpc.enums.MetadataType;
import com.smartdevicelink.proxy.rpc.enums.PredefinedWindows;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.enums.SystemCapabilityType;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;
import com.smartdevicelink.proxy.rpc.enums.TextFieldName;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;
import com.smartdevicelink.util.CompareUtils;
import com.smartdevicelink.util.DebugTool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.smartdevicelink.proxy.rpc.enums.TextAlignment.CENTERED;

/**
 * <strong>TextAndGraphicManager</strong> <br>
 *
 * Note: This class must be accessed through the SdlManager. Do not instantiate it by itself. <br>
 *
 */
abstract class BaseTextAndGraphicManager extends BaseSubManager {

	private static final String TAG = "TextAndGraphicManager";

	boolean isDirty;
	//volatile Show inProgressUpdate;
	Show currentScreenData;
	HMILevel currentHMILevel;
	WindowCapability defaultMainWindowCapability;
	private boolean pendingHMIFull, batchingUpdates;
	private final WeakReference<FileManager> fileManager;
	private final WeakReference<SoftButtonManager> softButtonManager;
	private CompletionListener pendingHMIListener;
	SdlArtwork blankArtwork;
	private OnRPCNotificationListener hmiListener;
	private OnSystemCapabilityListener onDisplaysCapabilityListener;
	private SdlArtwork primaryGraphic, secondaryGraphic;
	private TextAlignment textAlignment;
	private String textField1, textField2, textField3, textField4, mediaTrackTextField, title;
	private MetadataType textField1Type, textField2Type, textField3Type, textField4Type;
	private TextAndGraphicUpdateOperation updateOperation;


	private Queue transactionQueue;

	private TextsAndGraphicsState textsAndGraphicsState;

	//Constructors

	BaseTextAndGraphicManager(@NonNull ISdl internalInterface, @NonNull FileManager fileManager, @NonNull SoftButtonManager softButtonManager) {
		// set class vars
		super(internalInterface);
		this.fileManager = new WeakReference<>(fileManager);
		this.softButtonManager = new WeakReference<>(softButtonManager);
		batchingUpdates = false;
		isDirty = false;
		pendingHMIFull = false;
		textAlignment = CENTERED;
		currentHMILevel = HMILevel.HMI_NONE;
		currentScreenData = new Show();
		addListeners();
		getBlankArtwork();
		//TODO added
		this.transactionQueue = newTransactionQueue();
		//	this.batchQueue = new ArrayList<>();
		textsAndGraphicsState = new TextsAndGraphicsState();
	}

	@Override
	public void start(CompletionListener listener) {
		transitionToState(READY);
		super.start(listener);
	}

	@Override
	public void dispose() {

		textField1 = null;
		textField1Type = null;
		textField2 = null;
		textField2Type = null;
		textField3 = null;
		textField3Type = null;
		textField4 = null;
		textField4Type = null;
		mediaTrackTextField = null;
		title = null;
		textAlignment = null;
		primaryGraphic = null;
		secondaryGraphic = null;
		blankArtwork = null;
		defaultMainWindowCapability = null;
		currentScreenData = null;
		pendingHMIListener = null;
		isDirty = false;
		pendingHMIFull = false;

		// remove listeners
		internalInterface.removeOnRPCNotificationListener(FunctionID.ON_HMI_STATUS, hmiListener);
		internalInterface.removeOnSystemCapabilityListener(SystemCapabilityType.DISPLAYS, onDisplaysCapabilityListener);

		super.dispose();
	}

	//TODO temp location Also what do I set the ID to?
	private Queue newTransactionQueue() {
		Queue queue = internalInterface.getTaskmaster().createQueue("TextAndGraphicManager", 9, false);
		queue.pause();
		return queue;
	}

	//TODO fix for T&G Manager
	// Suspend the queue if the soft button capabilities are null (we assume that soft buttons are not supported)
	// OR if the HMI level is NONE since we want to delay sending RPCs until we're in non-NONE
	private void updateTransactionQueueSuspended() {
		if (defaultMainWindowCapability == null || HMILevel.HMI_NONE.equals(currentHMILevel)) {
			DebugTool.logInfo(TAG, String.format("Suspending the transaction queue. Current HMI level is NONE: %b, window capabilities are null: %b", HMILevel.HMI_NONE.equals(currentHMILevel), defaultMainWindowCapability == null));
			transactionQueue.pause();
		} else {
			DebugTool.logInfo(TAG, "Starting the transaction queue");
			transactionQueue.resume();
		}
	}

	//TODO not in IOS


	// Upload / Send

	protected void update(CompletionListener listener) {

		// make sure hmi is not none
		if (currentHMILevel == null || currentHMILevel == HMILevel.HMI_NONE) {
			//Trying to send show on HMI_NONE, waiting for full
			pendingHMIFull = true;
			if (listener != null) {
				pendingHMIListener = listener;
			}
			return;
		}

		// check if is batch update
		if (batchingUpdates) {
			return;
		}

		if (isDirty) {
			isDirty = false;
			sdlUpdate(listener);
		} else if (listener != null) {
			listener.onComplete(true);
		}
	}

	private synchronized void sdlUpdate(final CompletionListener listener) {

		if (transactionQueue.getTasksAsList().size() > 0) {
			//Transactions already exist, cancelling them
			transactionQueue.clear();
		}
		updateOperation = new TextAndGraphicUpdateOperation(internalInterface, fileManager.get(), defaultMainWindowCapability, currentScreenData, currentState(), new CompletionListener() {
			@Override
			public void onComplete(boolean success) {

				if (updateOperation.getSentShow() != null) {
					currentScreenData = updateOperation.getSentShow();
				}
				if (listener != null) { //IOS diff here
					listener.onComplete(success);
				}
			}
		});
		transactionQueue.add(updateOperation, false);
	}

	// Convert to State
	private TextsAndGraphicsState currentState() {
		return new TextsAndGraphicsState(textField1, textField2, textField3, textField4, mediaTrackTextField,
				title, primaryGraphic, secondaryGraphic, blankArtwork, textAlignment, textField1Type, textField2Type, textField3Type, textField4Type);
	}

	// Extraction

	//IOS has sdl_extractImageFromShow

	//IOS has sdl_createImageOnlyShowWithPrimaryArtwork

	private void updateCurrentScreenDataState(Show show) {

		if (show == null) {
			DebugTool.logError(TAG, "can not updateCurrentScreenDataFromShow from null show");
			return;
		}

		// If the items are null, they were not updated, so we can't just set it directly
		if (show.getMainField1() != null) {
			currentScreenData.setMainField1(show.getMainField1());
		}
		if (show.getMainField2() != null) {
			currentScreenData.setMainField2(show.getMainField2());
		}
		if (show.getMainField3() != null) {
			currentScreenData.setMainField3(show.getMainField3());
		}
		if (show.getMainField4() != null) {
			currentScreenData.setMainField4(show.getMainField4());
		}
		if (show.getTemplateTitle() != null) {
			currentScreenData.setTemplateTitle(show.getTemplateTitle());
		}
		if (show.getMediaTrack() != null) {
			currentScreenData.setMediaTrack(show.getMediaTrack());
		}
		if (show.getMetadataTags() != null) {
			currentScreenData.setMetadataTags(show.getMetadataTags());
		}
		if (show.getAlignment() != null) {
			currentScreenData.setAlignment(show.getAlignment());
		}
		if (show.getGraphic() != null) {
			currentScreenData.setGraphic(show.getGraphic());
		}
		if (show.getSecondaryGraphic() != null) {
			currentScreenData.setSecondaryGraphic(show.getSecondaryGraphic());
		}
	}

	// Helpers

	// IOS has sdl_hasData

	//Equality IOS has this section with:
	// sdl_showImages

	abstract SdlArtwork getBlankArtwork();

	// Getters / Setters

	void setTextAlignment(TextAlignment textAlignment) {
		this.textAlignment = textAlignment;
		// If we aren't batching, send the update immediately, if we are, set ourselves as dirty (so we know we should send an update after the batch ends)
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	TextAlignment getTextAlignment() {
		return textAlignment;
	}

	void setMediaTrackTextField(String mediaTrackTextField) {
		this.mediaTrackTextField = mediaTrackTextField;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getMediaTrackTextField() {
		return mediaTrackTextField;
	}

	void setTextField1(String textField1) {
		this.textField1 = textField1;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getTextField1() {
		return textField1;
	}

	void setTextField2(String textField2) {
		this.textField2 = textField2;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getTextField2() {
		return textField2;
	}

	void setTextField3(String textField3) {
		this.textField3 = textField3;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getTextField3() {
		return textField3;
	}

	void setTextField4(String textField4) {
		this.textField4 = textField4;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getTextField4() {
		return textField4;
	}

	void setTextField1Type(MetadataType textField1Type) {
		this.textField1Type = textField1Type;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	MetadataType getTextField1Type() {
		return textField1Type;
	}

	void setTextField2Type(MetadataType textField2Type) {
		this.textField2Type = textField2Type;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	MetadataType getTextField2Type() {
		return textField2Type;
	}

	void setTextField3Type(MetadataType textField3Type) {
		this.textField3Type = textField3Type;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	MetadataType getTextField3Type() {
		return textField3Type;
	}

	void setTextField4Type(MetadataType textField4Type) {
		this.textField4Type = textField4Type;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	MetadataType getTextField4Type() {
		return textField4Type;
	}

	void setTitle(String title) {
		this.title = title;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	String getTitle() {
		return title;
	}

	void setPrimaryGraphic(SdlArtwork primaryGraphic) {
		this.primaryGraphic = primaryGraphic;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	SdlArtwork getPrimaryGraphic() {
		return primaryGraphic;
	}

	void setSecondaryGraphic(SdlArtwork secondaryGraphic) {
		this.secondaryGraphic = secondaryGraphic;
		if (!batchingUpdates) {
			sdlUpdate(null);
		} else {
			isDirty = true;
		}
	}

	SdlArtwork getSecondaryGraphic() {
		return secondaryGraphic;
	}

	void setBatchUpdates(boolean batching) {
		this.batchingUpdates = batching;
	}

	private void addListeners() {
		// add listener
		hmiListener = new OnRPCNotificationListener() {
			@Override
			public void onNotified(RPCNotification notification) {
				OnHMIStatus onHMIStatus = (OnHMIStatus) notification;
				if (onHMIStatus.getWindowID() != null && onHMIStatus.getWindowID() != PredefinedWindows.DEFAULT_WINDOW.getValue()) {
					return;
				}

				currentHMILevel = onHMIStatus.getHmiLevel();
				updateTransactionQueueSuspended();
				if (currentHMILevel == HMILevel.HMI_FULL) {
					if (pendingHMIFull) {
						DebugTool.logInfo(TAG, "Acquired HMI_FULL with pending update. Sending now");
						pendingHMIFull = false;
						sdlUpdate(pendingHMIListener);
						pendingHMIListener = null;
					}
				}
			}
		};
		internalInterface.addOnRPCNotificationListener(FunctionID.ON_HMI_STATUS, hmiListener);


		onDisplaysCapabilityListener = new OnSystemCapabilityListener() {
			@Override
			public void onCapabilityRetrieved(Object capability) {
				// instead of using the parameter it's more safe to use the convenience method
				List<DisplayCapability> capabilities = SystemCapabilityManager.convertToList(capability, DisplayCapability.class);
				if (capabilities == null || capabilities.size() == 0) {
					DebugTool.logError(TAG, "TextAndGraphic Manager - Capabilities sent here are null or empty");
				} else {
					DisplayCapability display = capabilities.get(0);
					for (WindowCapability windowCapability : display.getWindowCapabilities()) {
						int currentWindowID = windowCapability.getWindowID() != null ? windowCapability.getWindowID() : PredefinedWindows.DEFAULT_WINDOW.getValue();
						if (currentWindowID == PredefinedWindows.DEFAULT_WINDOW.getValue()) {
							defaultMainWindowCapability = windowCapability;
						}
					}
				}
			}

			@Override
			public void onError(String info) {
				DebugTool.logError(TAG, "Display Capability cannot be retrieved");
				defaultMainWindowCapability = null;
			}
		};
		this.internalInterface.addOnSystemCapabilityListener(SystemCapabilityType.DISPLAYS, onDisplaysCapabilityListener);
	}
}

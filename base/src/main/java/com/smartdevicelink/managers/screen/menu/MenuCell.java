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
import android.support.annotation.Nullable;

import com.smartdevicelink.managers.file.filetypes.SdlArtwork;
import com.smartdevicelink.proxy.rpc.enums.MenuLayout;
import com.smartdevicelink.util.DebugTool;

import java.util.ArrayList;
import java.util.List;

public class MenuCell implements Cloneable{

	/**
	 * The cell's text to be displayed
	 */
	private String title;

	/**
	 * The cell's icon to be displayed
	 */
	private SdlArtwork icon;

	/**
	 * The strings the user can say to activate this voice command
	 */
	private List<String> voiceCommands;

	/**
	 * If this is not null, this cell will be a sub-menu button, displaying the sub-cells in a menu when pressed.
	 */
	private List<MenuCell> subCells;

	/**
	 * The listener that will be called when the command is activated
	 */
	private MenuSelectionListener menuSelectionListener;

	/**
	 * Used internally for cell ordering
	 */
	private int parentCellId;

	/**
	 * Used internally for cell ordering
	 */
	private int cellId;

	/**
	 * The submenu's layout that the subCells will be shown in. If `null`, the default submenu
	 * layout set via the screen manager's `MenuConfiguration` will be used.
	 */
	private MenuLayout subMenuLayout;

	/**
	 * MAX ID for cells - Cannot use Integer.MAX_INT as the value is too high.
	 */
	private static final int MAX_ID = 2000000000;

	// CONSTRUCTORS

	// SINGLE MENU ITEM CONSTRUCTORS

	/**
	 * Creates a new MenuCell Object with multiple parameters set
	 * @param title The cell's primary text
	 * @param icon The cell's image
	 * @param voiceCommands Voice commands that will activate the menu cell
	 * @param listener Calls the code that will be run when the menu cell is selected
	 */
	public MenuCell(@NonNull String title, @Nullable SdlArtwork icon, @Nullable List<String> voiceCommands, @Nullable MenuSelectionListener listener) {
		setTitle(title); // title is the only required param
		setIcon(icon);
		setVoiceCommands(voiceCommands);
		setMenuSelectionListener(listener);
		setCellId(MAX_ID);
		setParentCellId(MAX_ID);
	}

	// CONSTRUCTOR FOR CELL THAT WILL LINK TO SUB MENU

	/**
	 * Creates a new MenuCell Object with multiple parameters set
	 * <strong>NOTE: because this has sub-cells, there does not need to be a listener</strong>
	 * @param title The cell's primary text
	 * @param icon The cell's image
	 * @param subCells The sub-cells for the sub menu that will appear when the cell is selected
	 *
	 * @deprecated use {@link #MenuCell(String, MenuLayout, SdlArtwork, List)}
	 */
	@Deprecated
	public MenuCell(@NonNull String title, @Nullable SdlArtwork icon, @Nullable List<MenuCell> subCells) {
		setTitle(title); // title is the only required param
		setIcon(icon);
		setSubCells(subCells);
		setCellId(MAX_ID);
		setParentCellId(MAX_ID);
	}

	/**
	 * Creates a new MenuCell Object with multiple parameters set
	 * <strong>NOTE: because this has sub-cells, there does not need to be a listener</strong>
	 * @param title The cell's primary text
	 * @param subMenuLayout The submenu's layout that the subCells will be shown in. If `null`, the
	 *                      default submenu layout in the screen manager's `MenuConfiguration` will be used.
	 * @param icon The cell's image
	 * @param subCells The sub-cells for the sub menu that will appear when the cell is selected
	 */
	public MenuCell(@NonNull String title, @Nullable MenuLayout subMenuLayout, @Nullable SdlArtwork icon, @Nullable List<MenuCell> subCells) {
		setTitle(title); // title is the only required param
		setSubMenuLayout(subMenuLayout);
		setIcon(icon);
		setSubCells(subCells);
		setCellId(MAX_ID);
		setParentCellId(MAX_ID);
	}

	// SETTERS / GETTERS

	// PUBLIC METHODS

	/**
	 * Sets the title of the menu cell
	 * @param title - the title of the cell. Required
	 */
	public void setTitle(@NonNull String title){
		this.title = title;
	}

	/**
	 * Gets the title of the menu cell
	 * @return The title of the cell object
	 */
	public String getTitle(){
		return title;
	}

	/**
	 * Sets the icon of the menu cell
	 * @param icon - the icon being set, of type {@link SdlArtwork}
	 */
	public void setIcon(SdlArtwork icon){
		this.icon = icon;
	}

	/**
	 * Gets the icon for the cell
	 * @return the {@link SdlArtwork} icon for the cell
	 */
	public SdlArtwork getIcon() {
		return icon;
	}

	/**
	 * A list of Strings that will be used for voice commands
	 * @param voiceCommands - the string list used by the IVI system for voice commands
	 */
	public void setVoiceCommands(List<String> voiceCommands) {
		this.voiceCommands = voiceCommands;
	}

	/**
	 * the string list used by the IVI system for voice commands
	 * @return The String List used by the menu cell object for voice commands
	 */
	public List<String> getVoiceCommands() {
		return voiceCommands;
	}

	/**
	 * The list of MenuCells that can be set as subCells
	 * @param subCells - the list of subCells for this menu item
	 */
	public void setSubCells(List<MenuCell> subCells) {
		this.subCells = subCells;
	}

	/**
	 * The list of subCells for this menu item
	 * @return a list of MenuCells that are the subCells for this menu item
	 */
	public List<MenuCell> getSubCells() {
		return subCells;
	}

	/**
	 * The listener for when a menu item is selected
	 * @param menuSelectionListener the listener for this menuCell object
	 */
	public void setMenuSelectionListener(MenuSelectionListener menuSelectionListener) {
		this.menuSelectionListener = menuSelectionListener;
	}

	/**
	 * The listener that gets triggered when the menuCell object is selected
	 * @return the MenuSelectionListener for the cell
	 */
	public MenuSelectionListener getMenuSelectionListener() {
		return menuSelectionListener;
	}

	/**
	 * The submenu's layout that the subCells will be shown in. If `null`, the default submenu
	 * layout set via the screen manager's `MenuConfiguration` will be used.
	 * @param subMenuLayout - the layout used for the sub menu
	 */
	public void setSubMenuLayout(MenuLayout subMenuLayout) {
		this.subMenuLayout = subMenuLayout;
	}

	/**
	 * The submenu's layout that the subCells will be shown in. If `null`, the default submenu
	 * layout set via the screen manager's `MenuConfiguration` will be used.
	 * @return - the layout used for the sub menu
	 */
	public MenuLayout getSubMenuLayout() {
		return this.subMenuLayout;
	}

	// INTERNALLY USED METHODS

	/**
	 * Set the cell Id.
	 * @param cellId - the cell Id
	 */
	void setCellId(int cellId) {
		this.cellId = cellId;
	}

	/**
	 * Get the cellId
	 * @return the cellId for this menuCell
	 */
	int getCellId() {
		return cellId;
	}

	/**
	 * Sets the ParentCellId
	 * @param parentCellId the parent cell's Id
	 */
	void setParentCellId(int parentCellId) {
		this.parentCellId = parentCellId;
	}

	/**
	 * Get the parent cell's Id
	 * @return the parent cell's Id
	 */
	int getParentCellId() {
		return parentCellId;
	}

	// HELPER

	/**
	 * Note: You should compare using the {@link #equals(Object)} method. <br>
	 * Hash the parameters of the object and return the result for comparison
	 * For each param, increase the rotation distance by one.
	 * It is necessary to rotate each of our properties because a simple bitwise OR will produce equivalent results if, for example:
	 * Object 1: getText() = "Hi", getSecondaryText() = "Hello"
	 * Object 2: getText() = "Hello", getSecondaryText() = "Hi"
	 * @return the hash code as an int
	 */
	@Override
	public int hashCode() {
		int result = 1;
		result += ((getTitle() == null) ? 0 : Integer.rotateLeft(getTitle().hashCode(), 1));
		result += ((getIcon() == null) ? 0 : Integer.rotateLeft(getIcon().hashCode(), 2));
		result += ((getVoiceCommands() == null) ? 0 : Integer.rotateLeft(getVoiceCommands().hashCode(), 3));
		result += ((getSubCells() == null) ? 0 : Integer.rotateLeft(1, 4));
		return result;
	}

	/**
	 * Uses our custom hashCode for MenuCell objects, but does <strong>NOT</strong> compare the listener objects
	 * @param o - The object to compare
	 * @return boolean of whether the objects are the same or not
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) { return false; }
		// if this is the same memory address, its the same
		if (this == o) return true;
		// if this is not an instance of this class, not the same
		if (!(o instanceof MenuCell)) return false;
		// if we get to this point, create the hashes and compare them
		return hashCode() == o.hashCode();
	}

	/**
	 * Creates a deep copy of the object
	 * @return deep copy of the object, null if an exception occurred
	 */
	@Override
	public MenuCell clone() {
		try {
			MenuCell clone = (MenuCell) super.clone();
			if(this.icon != null){
				clone.icon = this.icon.clone();
			}
			if(this.subCells != null){
				ArrayList<MenuCell> cloneSubCells = new ArrayList<>();
				for(MenuCell subCell : subCells){
					cloneSubCells.add(subCell.clone());
				}
				clone.subCells = cloneSubCells;
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			if(DebugTool.isDebugEnabled()){
				throw new RuntimeException("Clone not supported by super class");
			}
		}

		return null;
	}
}

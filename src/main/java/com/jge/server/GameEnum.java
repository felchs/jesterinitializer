/*
 * Jester Game Engine is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Jester Game Engine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author: orochimaster
 * @email: orochimaster@yahoo.com.br
 */
package com.jge.server;

import java.util.HashMap;

import com.jge.server.utils.ByteUtils;

public enum GameEnum implements Protocol<Byte> {
	TIC_TAC_TOE((byte)0, (byte)2);
	
	private static HashMap<Byte, GameEnum> gameEnumByIdMap;
	
	public static GameEnum getGameEnumWithId(byte id) {
		if (gameEnumByIdMap == null) {
			for (GameEnum gameEnum : GameEnum.values()) {
				gameEnumByIdMap.put(gameEnum.getId(), gameEnum);
			}
		}
		return gameEnumByIdMap.get(id);
	}

	///////////////////////////////////////////////////////////////////////////

	private byte id;

	private byte numPlaces;
	
	private GameEnum(byte id, byte numPlaces) {
		this.id = id;
		this.numPlaces = numPlaces;
	}
	
	public byte getNumPlaces() {
		return numPlaces;
	}
	
	public void setNumPlaces(byte numPlaces) {
		this.numPlaces = numPlaces;
	}

	@Override
	public Byte getId() {
		return id;
	}
	
	@Override
	public byte[] getIdAsBytes() {
		return ByteUtils.getBytes(id);
	}

	@Override
	public String getName() {
		return this.toString();
	}
}
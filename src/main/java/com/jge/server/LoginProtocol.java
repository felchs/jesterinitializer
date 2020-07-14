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

import com.jge.server.utils.ByteUtils;

public enum LoginProtocol implements Protocol<Byte> {
	ENTER_LOBBY((byte)0),
	ENTER_MATCHMAKER((byte)1);

	private byte id;
	
	private LoginProtocol(byte id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return this.toString();
	}
	
	@Override
	public byte[] getIdAsBytes() {
		return ByteUtils.getBytes(id);
	}

	@Override
	public Byte getId() {
		return id;
	}
}
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

import java.util.Hashtable;


public class ClientsLoginData {
	private Hashtable<String, String> clientPass = new Hashtable<String, String>();
	
	public void storePassWithName(String clientName, String pass) {
		clientPass.put(clientName, pass);
	}
	
	public boolean isCorrectPassWithName(String clientName, String pass) {
		String storedPass = clientPass.get(clientName);
		return storedPass != null && storedPass.equals(pass);
	}
}
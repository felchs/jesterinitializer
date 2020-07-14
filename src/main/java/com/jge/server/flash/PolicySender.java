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
package com.jge.server.flash;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class PolicySender extends Thread {
	// Used for all logging from this class
	private static final Logger logger = Logger.getLogger(PolicySender.class.getName());
	
	private Socket socket;

	private BufferedReader in;
	private PrintWriter out;

	private String trigger;
	private String response;

	public PolicySender(Socket socket, String trigger, String response) {
		this.socket = socket;
		this.trigger = trigger;
		this.response = response;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			socket.setSoTimeout(10000);
			String read = read();
			if (trigger.equals(read)) {
				out.print(response + "\0");
				out.flush();
				try {
					// To fix Flash's timing issues
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return;
		}
	}

	/**
	 * Read: Grabs the policy request string.
	 * 
	 * @return String
	 * @throws IOException
	 * @throws EOFException
	 * @throws InterruptedIOException
	 */
	private String read() throws IOException, EOFException, InterruptedIOException {
		StringBuffer buffer = new StringBuffer();
		int character;

		while (buffer.length() < 100) {
			character = in.read();
			if (character == 0) {
				return buffer.toString();
			} else {
				buffer.appendCodePoint(character);
			}
		}

		return buffer.toString();
	}
}
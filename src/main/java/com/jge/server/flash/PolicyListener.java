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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class PolicyListener extends Thread {
	// Used for all logging from this class
	private static final Logger logger = Logger.getLogger(PolicyListener.class.getName());
	
	private boolean running;
	private int port;
	private String response;
	private String trigger;

	private ServerSocket portServer;

	public PolicyListener(int port, String trigger, String response) throws NullPointerException {
		if (response == null) {
			throw new NullPointerException("Missing Required response File.");
		}
		this.port = port;
		this.response = response;
		this.trigger = trigger;
		this.running = true;
		System.out.print("Creating Listener\n");
	}

	public void run() {
		try {
			portServer = new ServerSocket(port);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return;
		}
		while (running) {
			try {
				System.out.println("Some message...");
				Socket socket = portServer.accept();
				
				PolicySender sender = new PolicySender(socket, trigger, response);
				//sender.start();
				sender.run();
				//Sender sender = new Sender(socket, trigger, response);
				//sender.start();
			} catch (IOException e) {
				logger.warning(e.getMessage());
				try {
					sleep(500);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				} // if whole-sale communication fails wait a bit before trying again
			}
		}
	}

	public void halt() {
		this.running = false;
		this.interrupt();
	}
}
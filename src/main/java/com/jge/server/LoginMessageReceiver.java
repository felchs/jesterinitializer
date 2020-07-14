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

import java.nio.ByteBuffer;
import java.util.logging.Level;

import com.jge.server.client.Client;
import com.jge.server.client.ClientListener;
import com.jge.server.client.MessageSender;
import com.jge.server.net.Channel;
import com.jge.server.net.ClientSessionListener;
import com.jge.server.net.session.ClientSession;
import com.jge.server.space.MessageReceiver;
import com.jge.server.space.Space;
import com.jge.server.space.SpaceIdMapping;
import com.jge.server.space.SpaceMessageReceiver;
import com.jge.server.utils.ByteUtils;
import com.jge.server.utils.DGSLogger;
import com.jge.server.utils.IdList;
import com.jge.server.utils.MappingUtil;

public class LoginMessageReceiver implements MessageReceiver {
	private ClientListener clientListener;

	public LoginMessageReceiver() {
	}

	public ClientSessionListener createSessionListener(ClientSession session, MessageSender sender) {
		Client client = (Client)sender;
		ClientListener clientListener = new ClientListener(client);
		this.clientListener = clientListener;
		client.setClientListener(clientListener);
		clientListener.addMessageReceiver(this);

		return clientListener;
	}
	
	@Override
	public String getId() {
		return SpaceMessageReceiver.MSG_RECEIVER_PREFIX + "-1";
	}
	
	@Override
	public boolean isActive() {
		return true;
	}

	public String getAnonymousClientName() {
		IdList anonymoustList = (IdList) MappingUtil.getObject("AnonymousLogged");
		if (anonymoustList == null) {
			anonymoustList = new IdList("ClientAnonymous_");
			MappingUtil.addObject("AnonymousLogged", anonymoustList);
		}
		return anonymoustList.getNextNameId();
	}
	
	@Override
	public void receivedChannelMessage(Channel channel, MessageSender sender, ByteBuffer msg) {		
	}
	
	@Override
	public void receivedMessage(MessageSender sender, ByteBuffer msg) {
		if (!sender.isHuman()) {
			return;
		}
		
		LoginProtocol protocol = LoginProtocol.values()[msg.get()];
		DGSLogger.log("LoginMessageReceiver_: " + protocol.getId());
		
		switch (protocol) {
			case ENTER_MATCHMAKER:
			{
				DGSLogger.log(Level.INFO, "login received: client name and username selected");

				// get username and password
				byte payload = msg.get();
				DGSLogger.log("payload: " + payload);
				String username = ByteUtils.getString(msg, payload);
				//payload = msg.get();
				//String password = ByteUtils.getString(msg, payload);
				boolean anonymous = msg.get() == 1;
				
				// now put the client in some space
				String clientObjName = Client.CLIENT_PREFIX + username;
				DGSLogger.log("LOGIN: username: " + username + ", ClientObjName: " + clientObjName);
				Client client = (Client)MappingUtil.getObject(clientObjName);
				client.setAnonymous(anonymous && username.contains("@anonymous")); // avoid hacking
				//putInSomeSpace(client, msg);
				
				// we're logged, so we do not more need to process messages to login handling
				removeAsReceiver();
			} break;
		default:
			throw new RuntimeException();
		}
	}
	
	private void removeAsReceiver() {
		clientListener.removeMessageReceiver(this);
	}
	
	private void putInSomeSpace(Client client, ByteBuffer msg) {
		// event enter in some space
		byte enter_lobby = msg.get();
		DGSLogger.log("LoginMessageReceiver.putInSomeSpace(), enter_lobby: " + enter_lobby);
		if (enter_lobby == LoginProtocol.ENTER_LOBBY.getId()) {
			int gameId = msg.getInt();
			DGSLogger.log("Space Id: " + gameId);
			Space space = SpaceIdMapping.getSpaceWithId(gameId);
			if (space != null) {
				DGSLogger.log("Space name: " + space.getName());
				space.putClient(client);
			}
		}
	}
}
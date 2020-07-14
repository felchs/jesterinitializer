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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import com.gametac.bdb.DBHandler;
import com.jge.server.client.Client;
import com.jge.server.net.AppContext;
import com.jge.server.net.AppListener;
import com.jge.server.net.ClientSessionListener;
import com.jge.server.net.LoginResponse;
import com.jge.server.net.LogoutReason;
import com.jge.server.net.session.ClientSession;
import com.jge.server.space.game.GameFactory;
import com.jge.server.space.game.GameSpace;
import com.jge.server.turn.tictactoe.TicTacToeGame;
import com.jge.server.utils.DGSLogger;
import com.jge.server.utils.IdList;
import com.jge.server.utils.MappingUtil;

public class Initializer implements AppListener, Serializable {
	private static final long serialVersionUID = 1L;
		
	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates the channels. Channels persist across server restarts, so they
	 * only need to be created here in {@code initialize}.
	 */
	public void initialize(Properties props) {
		//
		// here is where the games must be created
		//
		GameFactory.setInstance(new GameFactory() {
			private ArrayList<Byte> gameTypes;
			
			@Override
			public GameSpace createGame(byte gameEnum, int numPlayers, boolean fillWithRobots) {
				switch (GameEnum.getGameEnumWithId(gameEnum)) {
				case TIC_TAC_TOE:
					boolean isMiniTicTackToe= true;
					int gameId = UUID.randomUUID().clockSequence(); // a random UUID by now
					return new TicTacToeGame(gameId, gameEnum, isMiniTicTackToe);

				default:
					break;
				}
				return null;
			}

			@Override
			public int getNumPlayers(byte gameType) {
				if (gameType == GameEnum.TIC_TAC_TOE.getId()) {
					return 2;
				}
				return 0;
			}

			@Override
			public ArrayList<Byte> getGameTypes() {
				if (gameTypes == null) {
					gameTypes = new ArrayList<>();
					for (GameEnum gameEnum : GameEnum.values()) {
						gameTypes.add(gameEnum.getId());
					}
				}
				return gameTypes;
			}
			
		});
		

		try {
			LocalConnection.initRobotsConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getNextClientId() {
		IdList clientList = (IdList) MappingUtil.getObject("ClientsLogged");
		if (clientList == null) {
			clientList = new IdList();//Client.CLIENT_PREFIX);
			MappingUtil.addObject("ClientsLogged", clientList);
		}
		return clientList.getNextNameId();
	}

	public ClientSessionListener loggedIn(ClientSession session) {
		DGSLogger.log("User ------{ " + session.getName() + " }------ has logged in");
		
		if (!session.getName().contains("ROBOT")) { // FIXME: change the way the robot is created
			LoginMessageReceiver messageReceiver = new LoginMessageReceiver();
			String sessionNameEmail = session.getName();
			String clientSessionName = Client.CLIENT_PREFIX + sessionNameEmail;
			Client client = (Client)MappingUtil.getObject(clientSessionName);
	
			if (client == null) {
				client = new Client(clientSessionName);
				short clientId = Short.valueOf(getNextClientId());
				client.setId(clientId);
				DGSLogger.log("Adding this client session: " + session.getName() + " Client Name: " + clientSessionName + " Client Obj: " + client.toString() + "");
				MappingUtil.addObject(clientSessionName, client);	
			}

			client.setClientSession(session);
			return messageReceiver.createSessionListener(session, client);
		}

		return new RobotListener();
	}
	
	@Override
	public void loggedOut(ClientSession session, LogoutReason reason) {	
	}
	
	@Override
	public void logginFailure(ClientSession session) {		
	}
	
	@Override
	public LoginResponse authenticateOnLogin(ClientSession session, ByteBuffer message) {
		short len = message.getShort();
		byte[] emailUserNameAsBytes = new byte[len];
		message.get(emailUserNameAsBytes);
		String emailUsername = null;
		try {
			emailUsername = new String(emailUserNameAsBytes, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		len = message.getShort();
		byte[] encodedPasswordAsBytes = new byte[len];
		message.get(encodedPasswordAsBytes);
		String encodedPassword = null;
		
		try {
			encodedPassword = new String(encodedPasswordAsBytes, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
		session.setName(emailUsername);
        DGSLogger.log("email: " + emailUsername + " plainPass: " + encodedPassword);
        
        LoginResponse loginResponse = LoginResponse.LOGIN_FAILED;

        try {
        	DGSLogger.log("nameoremail: " + emailUsername + ", encodedPass: " + encodedPassword);
       		
       		if (emailUsername.equals("ROBOT-PLAYER@&")) {
       			loginResponse = LoginResponse.LOGIN_SUCCESS;
       		} else if (checkIsValidEmail(emailUsername)) {
				if (!DBHandler.getUserDataAccessor().isUserRegistered(emailUsername)) {
					DBHandler.getUserDataAccessor().createUser(emailUsername, encodedPassword, 0);
				}
				loginResponse = LoginResponse.LOGIN_SUCCESS;
       		} else {
       			loginResponse = LoginResponse.LOGIN_NOT_OK_WRONG_EMAIL;
       		}
       		
        } catch (Exception e) {
        	e.printStackTrace();
        	
        	String errMsg = e.getMessage();
        	
        	DGSLogger.log("Err on login: " + errMsg);
        	loginResponse = LoginResponse.LOGIN_FAILED;
		}
        
        DGSLogger.log("Can Login: " + loginResponse);

		DGSLogger.log("NamePasswordAuthenticator.authenticateIdentity(), could login: " + loginResponse);
		
		return loginResponse;
	}
	
	private boolean checkIsValidEmail(String emailUsername) {
		if (!emailUsername.contains("@")) {
			return false;
		}
		
		String emailServer = emailUsername.split("@")[1];
		if (!emailServer.contains(".")) {
			return false;
		}
		
		if (!(emailServer.contains(".com")
			|| emailServer.contains(".co")
			|| emailServer.contains(".org")
			|| emailServer.contains(".net")
			|| emailServer.contains(".info")
			|| emailServer.contains(".biz")
			|| emailServer.contains(".pt")
			|| emailServer.contains(".eu")
			|| emailServer.contains(".mobi")
			|| emailServer.contains(".tv")))
		{
			return false;
		}
		
		return true;
	}

	///////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		Initializer initializer = new Initializer();
		AppContext.get().init(initializer);
	}
}
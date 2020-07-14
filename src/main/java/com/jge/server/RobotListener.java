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

import com.jge.server.client.RobotSender;
import com.jge.server.net.ClientSessionListener;
import com.jge.server.space.SpaceIdMapping;
import com.jge.server.space.SpaceMessageReceiver;
import com.jge.server.utils.DGSLogger;

public class RobotListener implements ClientSessionListener {
	
	@Override
	public void receivedMessage(ByteBuffer message) {
		DGSLogger.log("recevied a message, capacity(): " + message.capacity());
		
		int spaceID = message.getInt();
		DGSLogger.log("recevied a message spaceID: " + spaceID);
//		String spaceSimpleName = SpaceBuilder.decodeSpaceName(spaceID);

//		short robotIdx = message.getShort();
//		String robotName = spaceSimpleName + ":PLAYER:" + GameSpace.ROBOT_PREFIX + robotIdx;
		
		SpaceMessageReceiver receiver = SpaceIdMapping.getSpaceWithId(spaceID).createMessageReceiver();
		
		DGSLogger.log("is active: " + receiver.isActive());
		if (receiver.isActive()) {
			RobotSender robotSender = new RobotSender();
			receiver.receivedMessage(robotSender, message);
		}
	}

	@Override
	public void disconnected(boolean graceful) {
		DGSLogger.log("RobotClientListener.disconnected()");
		
		// todo this?
//		SpaceMessageReceiver receiverForUpdate = receiverRef.getForUpdate();
//		if (receiverForUpdate != null) {
//			AppContext.getDataManager().removeObject(receiverForUpdate);
//		}
	}
	
	@Override
	public void reconnect() {		
	}
}
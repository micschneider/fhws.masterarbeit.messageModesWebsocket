package fhws.masterarbeit.messageModesWebsocket.client;

import javax.websocket.CloseReason;

public interface MessageModesClientListener 
{
	public void setConnected(boolean isConnected, CloseReason cr);

	public void reportMessage(String message);
	
	public void reportConnectionHealth(long millis); 
}

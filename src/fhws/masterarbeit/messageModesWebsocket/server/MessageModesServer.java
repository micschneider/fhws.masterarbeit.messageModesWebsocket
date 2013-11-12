package fhws.masterarbeit.messageModesWebsocket.server;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/modes")
public class MessageModesServer 
{
	public static final int MESSAGE_MAX = 15 * 1000 * 1024;
	
	@OnOpen
	public void open(Session session)
	{
		session.setMaxBinaryMessageBufferSize(MESSAGE_MAX);
		this.reportMessage(session, "Connected!");
	}
	
	@OnMessage
	public void binaryMessage(byte[] bytes, Session session)
	{
		this.reportMessage(session, "Processed binary message of length " + bytes.length / 1024 + "kb");
	}
	
	@OnMessage (maxMessageSize = MESSAGE_MAX)
	public void textMessage(String partialMessage, boolean isLast, Session session)
	{
		String report = "Processed partial text message of size " + partialMessage.length() / 1024 + "kb...";
		if (isLast)
			report += "message complete.";
		this.reportMessage(session, report);
	}
	
	@OnError
	public void error(Throwable t)
	{
		System.out.println("MessageModeServer error: " + t.getMessage());
	}
	
	@OnClose
	public void close(Session s, CloseReason cr)
	{
		System.out.println("MessageModeServer closing because: " + cr.getReasonPhrase());
	}
	
	private void reportMessage(Session s, String message) 
	{
		try
		{
			String timestamp = DateFormat.getTimeInstance().format(new Date());
			s.getBasicRemote().sendText(timestamp + " " + message);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
}

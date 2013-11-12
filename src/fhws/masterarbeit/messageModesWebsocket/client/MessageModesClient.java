package fhws.masterarbeit.messageModesWebsocket.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.Session;

@ClientEndpoint
public class MessageModesClient 
{
	private Session session;
	private MessageModesClientListener listener;
	static int PIECES_COUNT = 100;
	private int sendTimeout = 10;
	
	public MessageModesClient(MessageModesClientListener listener)
	{
		this.listener = listener;
	}
	
	public void setTimeout(int millis)
	{
		this.sendTimeout = millis;
	}
	
	@OnOpen
	public void open(Session session)
	{
		this.session = session;
		this.listener.setConnected(true, null);
	}
	
	@OnMessage
	public void handleMessage(String message)
	{
		this.listener.reportMessage(message);
	}
	
	@OnMessage
	public void handlePong(PongMessage pm)
	{
		String sendAtString = new String(pm.getApplicationData().array());
		long sendAtMillis = Long.parseLong(sendAtString);
		long roundtripMillis = System.currentTimeMillis() - sendAtMillis;
		this.listener.reportConnectionHealth(roundtripMillis);
	}
	
	@OnClose
	public void close(Session session, CloseReason cr)
	{
		this.listener.setConnected(false, cr);
	}
	
	public void disconnect() throws IOException
	{
		this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User closed applicaton"));
	}
	
	public void sendPing() throws IOException
	{
		long now = System.currentTimeMillis();
		byte[] data = ("" + now).getBytes();
		session.getBasicRemote().sendPing(ByteBuffer.wrap(data));
	}
	
	public Future<Void> sendAsyncByFuture(byte[] data) throws IOException
	{
		RemoteEndpoint.Async rea = session.getAsyncRemote();
		rea.setSendTimeout(this.sendTimeout);
		ByteBuffer bb = ByteBuffer.wrap(data);
		return rea.sendBinary(bb);
	}
	
	public Future<Void> sendAsyncByFuture(String textData) throws IOException
	{
		RemoteEndpoint.Async rea = session.getAsyncRemote();
		rea.setSendTimeout(this.sendTimeout);
		return rea.sendText(textData);
	}
	
	public void sendAsyncWithHandler(byte[] data, SendHandler sh)
	{
		RemoteEndpoint.Async rea = session.getAsyncRemote();
		ByteBuffer bb = ByteBuffer.wrap(data);
		rea.setSendTimeout(this.sendTimeout);
		rea.sendBinary(bb, sh);
	}
	
	public void sendAsyncWithHandler(String textData, SendHandler sh)
	{
		RemoteEndpoint.Async rea = session.getAsyncRemote();
		rea.setSendTimeout(this.sendTimeout);
		rea.sendText(textData, sh);
	}
	
	public void sendSynchronously(byte[]data) throws IOException
	{
		RemoteEndpoint.Basic reb = session.getBasicRemote();
		ByteBuffer bb = ByteBuffer.wrap(data);
		reb.sendBinary(bb);
	}
	
	public void sendSynchronously(String textData) throws IOException
	{
		RemoteEndpoint.Basic reb = session.getBasicRemote();
		reb.sendText(textData);
	}
	
	public void sendInPieces(byte[] data, PartialMessageSendListener pc) throws IOException
	{
		RemoteEndpoint.Basic reb = session.getBasicRemote();
		int chunkSize = (int)(data.length / PIECES_COUNT);
		BinaryDataIterator di = new BinaryDataIterator(data, chunkSize);
		int counter = 0;
		while(di.hasNext())
		{
			ByteBuffer nextPiece = di.next();
			boolean isLast = !di.hasNext();
			reb.sendBinary(nextPiece, isLast);
			counter++;
			pc.reportProgress(counter);
		}
	}
	
	public void sendInPieces(String textData, PartialMessageSendListener pc) throws IOException
	{
		RemoteEndpoint.Basic reb = session.getBasicRemote();
		byte[] bytes = textData.getBytes();
		int chunkSize = (int)(bytes.length / PIECES_COUNT);
		BinaryDataIterator di = new BinaryDataIterator(bytes, chunkSize);
		int counter = 0;
		while(di.hasNext())
		{
			ByteBuffer nextPiece = di.next();
			boolean isLast = !di.hasNext();
			reb.sendText(new String(nextPiece.array()), isLast);
			counter++;
			pc.reportProgress(counter);
		}
	}
}

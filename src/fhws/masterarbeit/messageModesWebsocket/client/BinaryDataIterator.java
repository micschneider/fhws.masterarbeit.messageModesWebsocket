package fhws.masterarbeit.messageModesWebsocket.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class BinaryDataIterator implements Iterator<ByteBuffer>
{
	private ByteArrayInputStream bais;
    private boolean done = false;
    private int chunkSize = 1024;
    
    BinaryDataIterator(byte[] data, int chunkSize) 
    {
        this.chunkSize = chunkSize;
        this.bais = new ByteArrayInputStream(data);
    }
    
    @Override
    public boolean hasNext() 
    {
        return !this.done;
    }
    
    @Override
    public ByteBuffer next() 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        int count = 0;
        try {
            while ((i = bais.read()) != -1 && count < chunkSize) 
            {
                baos.write(i);
                count++;
            }
            if (i == -1) 
            {
                this.done = true;
                bais.close();
            }
        } 
        catch (IOException ioe) 
        {
            throw new RuntimeException("failed to read");
        }
        return ByteBuffer.wrap(baos.toByteArray());
    }
    
    @Override
    public void remove() 
    {
        throw new UnsupportedOperationException();
    }
}

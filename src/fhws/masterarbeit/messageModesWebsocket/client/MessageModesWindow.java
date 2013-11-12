package fhws.masterarbeit.messageModesWebsocket.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;

import fhws.masterarbeit.messageModesWebsocket.server.MessageModesServer;


public class MessageModesWindow extends JFrame implements MessageModesClientListener 
{
    private MessageModesClient sendModesClient;
    private String endpointPath = "ws://localhost:8080/fhws.masterarbeit.messageModesWebsocket/modes";
    private int dataSize;
    private byte[] currentBinaryData;
    boolean isBinaryMode;
    private JTextArea serverOutputField = new JTextArea();
    private ProgressDialog progressDialog;
    private JSlider messageSizeSlider = new JSlider();
    private JSlider timeoutSlider = new JSlider();
    private JButton sendPingButton = new JButton("Send ping to test connection");
    private JButton sendSynchronousButton = new JButton("Send synchronously as whole message");
    private JButton sendAsynchronousFutureButton = new JButton("Send asynchronously with a future");
    private JButton sendInPiecesButton = new JButton("Send message in pieces");
    private JButton sendAsynchronousHandlerButton = new JButton("Send asynchronously with a handler");
    private JRadioButton binaryButton = new JRadioButton("Binary");
    private JRadioButton textButton = new JRadioButton("Text");
    private JLabel connectionHealthLabel = new JLabel("");
    private TitledBorder messageSizeBorder = BorderFactory.createTitledBorder("Message Size:                        ");
    private TitledBorder timeoutBorder = BorderFactory.createTitledBorder("Timeout:   ms");

    
    public static void main(String args[]) throws Exception {
        MessageModesWindow isw = new MessageModesWindow();  
    }
    
    public MessageModesWindow() {
        this.sendModesClient = new MessageModesClient(this);
        this.setBounds(30,30,600,800);
        this.progressDialog = new ProgressDialog(this, MessageModesClient.PIECES_COUNT); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.messageSizeSlider.setLabelTable(this.messageSizeSlider.createStandardLabels(5));
        
        // Layouts
        JPanel layoutPanel = new JPanel();
        layoutPanel.setBorder(BorderFactory.createLineBorder(layoutPanel.getBackground(), 10));
        BoxLayout bl = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(bl);

        // panel for pings
        JPanel pingPanel = new JPanel();
        pingPanel.setBorder(BorderFactory.createTitledBorder("Connection health"));
        pingPanel.setLayout(new BoxLayout(pingPanel, BoxLayout.Y_AXIS));
        pingPanel.add(sendPingButton);
        pingPanel.add(connectionHealthLabel);
        
        // Panel for message parameters
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message parameters"));
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        ButtonGroup bg = new ButtonGroup();
        bg.add(this.textButton);
        bg.add(this.binaryButton);
        this.textButton.setSelected(true);
        messagePanel.add(this.textButton);
        messagePanel.add(this.binaryButton);
        this.messageSizeSlider.setBorder(messageSizeBorder);
        messagePanel.add(this.messageSizeSlider);
        
        // Panel for send buttons
        JPanel syncButtonPanel = new JPanel();
        syncButtonPanel.setLayout(new BoxLayout(syncButtonPanel, BoxLayout.Y_AXIS));
        syncButtonPanel.setBorder(BorderFactory.createTitledBorder("Synchronous Send options"));
        syncButtonPanel.add(this.sendSynchronousButton);
        syncButtonPanel.add(this.sendInPiecesButton);
        
        JPanel asyncPanel = new JPanel();
        asyncPanel.setLayout(new BoxLayout(asyncPanel, BoxLayout.Y_AXIS));
        asyncPanel.setBorder(BorderFactory.createTitledBorder("Asynchronous Send options"));
        
        asyncPanel.add(this.sendAsynchronousFutureButton);
        asyncPanel.add(this.sendAsynchronousHandlerButton);
        this.timeoutSlider.setBorder(timeoutBorder);
        asyncPanel.add(this.timeoutSlider);

        // Panel for Server Output
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        this.serverOutputField.setEditable(false);
        outputPanel.add(new JScrollPane(serverOutputField));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Server messages"));
        
        layoutPanel.add(messagePanel);
        layoutPanel.add(pingPanel);
        layoutPanel.add(syncButtonPanel);
        layoutPanel.add(asyncPanel);
        layoutPanel.add(outputPanel);
        
        
        this.getContentPane().add(layoutPanel);
        
        // wire up buttons
        this.binaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                changeMessageMode();
            }
        });
        
        this.textButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                changeMessageMode();
            }
        });
        
        this.sendPingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendPing();
            }
        });

        this.sendSynchronousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendSynchronously();
            }
        });
        
        this.sendAsynchronousFutureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendAsynchronouslyFuture();
            }
        });
        
        this.sendAsynchronousHandlerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendAsynchronouslyHandler();
            }
        });
        
        this.sendInPiecesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendPartial();
            }
        });
        
        this.messageSizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                messageSizeSliderChanged();
            }
        });
        
        this.timeoutSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                timeoutSliderChanged();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                disconnect();
            }
        });
        
        this.changeMessageMode();
        this.messageSizeSliderChanged();
        this.timeoutSliderChanged();
        this.setConnected(false, null);
        this.updateHealth("??");
        this.doLayout();
        this.setVisible(true);
        this.connect();       
    }
    
    public void disconnect() {
        try {
            this.sendModesClient.disconnect();
        } catch (Throwable thw) {
            System.out.println("Error closing client: " + thw.getMessage());
        }
    }
    
    public void connect() {
        try {
            URI uri = new URI(endpointPath);
            ContainerProvider.getWebSocketContainer().connectToServer(sendModesClient, uri);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to \n" + this.endpointPath, "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void setWidgetsEnabled(boolean enabled) {
        this.sendPingButton.setEnabled(enabled);
        this.sendSynchronousButton.setEnabled(enabled);
        this.sendAsynchronousFutureButton.setEnabled(enabled);
        this.sendInPiecesButton.setEnabled(enabled);
        this.sendAsynchronousHandlerButton.setEnabled(enabled);
        this.serverOutputField.setEnabled(enabled);
        this.messageSizeSlider.setEnabled(enabled);
        this.binaryButton.setEnabled(enabled);
        this.textButton.setEnabled(enabled);
        this.messageSizeSlider.setEnabled(enabled);
    }
    
    public void sendPing() {
        try {
            this.updateHealth("??");
            this.sendModesClient.sendPing();
        } catch (IOException ioe) {
            JOptionPane.showConfirmDialog(this, "Ping failed to send...check the connection.");
        }
    }
    
    public void sendSynchronously() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        this.setWidgetsEnabled(false);
        try {
            if (isBinaryMode) {
                sendModesClient.sendSynchronously(getBinaryData()) ;
             } else {
                sendModesClient.sendSynchronously(getTextData());
             }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getContentPane(), "Error sending message:\n" + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
        setWidgetsEnabled(true);
        setCursor(Cursor.getDefaultCursor());

    }
    
    public void changeMessageMode() {
        this.isBinaryMode = this.binaryButton.isSelected();
    }
    
    public void sendAsynchronouslyFuture() {
        this.setWidgetsEnabled(false);
        final CancelDialog cd = new CancelDialog(this);
        cd.setVisible(true);
        
        Thread sendThread = new Thread() {
          public void run() {
              Future<Void> f;
              
              try {
                
                if (isBinaryMode) {
                    f = sendModesClient.sendAsyncByFuture(getBinaryData());
                } else {
                    f = sendModesClient.sendAsyncByFuture(getTextData());
                }
                cd.setFuture(f);
                f.get();
                cd.doClose();
              } catch (ExecutionException | IOException | InterruptedException e) {
                  cd.doClose();
                  String message = "Error sending message:\n" + e.getMessage();
                  if (e instanceof ExecutionException) {
                      Throwable causecause = ((ExecutionException) ((ExecutionException) e)).getCause().getCause();
                      if (causecause instanceof java.util.concurrent.TimeoutException) {
                          message = "Send timed out !\nTry increasing the timeout...";
                      }
                  }
                  JOptionPane.showMessageDialog(getContentPane(), message, "Warning", JOptionPane.ERROR_MESSAGE);

              }
              setWidgetsEnabled(true);
              
          }    
        };
         
        sendThread.start();
    }
    
    public void sendAsynchronouslyHandler() {
        this.setWidgetsEnabled(false);
        class TimedSendHandler implements SendHandler {
            long timeStarted = System.currentTimeMillis();
            
            public void onResult(SendResult sr) {
                sendCompleted(sr, timeStarted);
            } 
        }

        try {
            if (this.isBinaryMode) {
                this.sendModesClient.sendAsyncWithHandler(this.getBinaryData(), new TimedSendHandler());
            } else {
                this.sendModesClient.sendAsyncWithHandler(this.getTextData(), new TimedSendHandler());
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error sending message:\n" + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);

        }
    }
    
    public void sendCompleted(SendResult sendResult, long now) {
        this.setWidgetsEnabled(true);
        if (sendResult.isOK()) {
            long millis = System.currentTimeMillis() - now;
            JOptionPane.showMessageDialog(this, "Message transmitted in " + millis + "ms" , "Message Send Update", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String message = "Error sending message:\n" + sendResult.getException().getMessage();
            Throwable ex = sendResult.getException();
            if (ex instanceof ExecutionException) {
                Throwable eex = ((ExecutionException) ex).getCause();
                if (eex instanceof java.util.concurrent.TimeoutException) {
                    message = "Send timed out !\nTry increasing the timeout...";
                }
            }
            JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void messageSizeSliderChanged() {
        this.dataSize = this.getDataSize();
        int sizeKb = (int) this.dataSize / 1024;
        float percentOfLimit = ( this.dataSize * 100/MessageModesServer.MESSAGE_MAX);
        messageSizeBorder.setTitle("Size: " + (int) percentOfLimit + "% of the server limit");
        if (percentOfLimit > 100) {
            messageSizeBorder.setTitleColor(Color.red);
        } else {
            messageSizeBorder.setTitleColor(Color.black);
        }
        this.currentBinaryData = null;
    }
    
    public void timeoutSliderChanged() {
        int timeout = this.timeoutSlider.getValue() * 20;
        this.timeoutBorder.setTitle("Timeout: " + timeout + "ms");
        this.sendModesClient.setTimeout(timeout);
    }
    
    int getDataSize() {
        int size =  (int)  ((MessageModesServer.MESSAGE_MAX / 50) + (this.messageSizeSlider.getValue() * MessageModesServer.MESSAGE_MAX / 100));
        return size;
    }
    
    public void sendPartial() {
        this.progressDialog.setVisible(true);
        Thread sendThread = new Thread() {
            public void run() {
                try {
                    if (isBinaryMode) {
                        sendModesClient.sendInPieces(getBinaryData(), progressDialog);
                    } else {
                        sendModesClient.sendInPieces(getTextData(), progressDialog);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getContentPane(), "Error sending message:\n" + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
                }
                progressDialog.doClose();
            }
        };
        sendThread.start();
    }
    
    private String getTextData() {
        return new String(this.getBinaryData());
    }
    
    private byte[] getBinaryData() {
        if (this.currentBinaryData == null) {
            this.currentBinaryData = new byte[dataSize];
            for (int i = 0; i < dataSize; i++) {
                currentBinaryData[i] = (byte) i;
            }
        }
        byte[] bytes = new byte[this.currentBinaryData.length];
        System.arraycopy(this.currentBinaryData, 0, bytes, 0, this.currentBinaryData.length);
        return bytes;
    }
    
    @Override
    public void reportMessage(String message) {
        this.serverOutputField.insert(message + "\n", 0);  
    }
    
    void updateHealth(String millisString) {
        this.connectionHealthLabel.setText("Last roundtrip: " + millisString + "ms");
    }
    
    @Override
    public void reportConnectionHealth(long millis) {
        this.updateHealth("" + millis);
        this.connectionHealthLabel.setForeground(new Color(0,150,0));
    }
    
    @Override
    public void setConnected(boolean isConnected, CloseReason cr) {
        if (isConnected) {
            this.setTitle("Message Modes Client: connected");
        } else {
            this.setTitle("Message Modes Client: disconnected");
            this.connectionHealthLabel.setForeground(Color.black);
            this.updateHealth("??");
        }
        this.setWidgetsEnabled(isConnected);
        if (!isConnected && cr != null) {
            String message;
            if (cr.getCloseCode() == CloseReason.CloseCodes.TOO_BIG) {
                message = "The message you sent was too big for the server\nso it closed the connection !\n";
                int result = JOptionPane.showConfirmDialog(this, message + "Start a new connection ?", "Server Disconnected", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    this.connect();
                }
            } else {
                System.out.println("Connection closed");
            }
            
        }
                
    }
    
    
}

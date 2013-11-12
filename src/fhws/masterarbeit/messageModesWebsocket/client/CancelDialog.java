package fhws.masterarbeit.messageModesWebsocket.client;

import java.awt.event.*;
import java.util.concurrent.*;
import javax.swing.*;

public class CancelDialog extends JDialog {
    private int myWidth = 200;
    private int myHeight = 75;
    private Future future;
    private JButton cancelButton = new JButton("Cancel");
    
    CancelDialog(JFrame frame) {
        super(frame);
        this.setTitle("Sending...");
        this.setBounds(frame.getX() + (int) ((frame.getWidth() -myWidth) / 2), 
                        frame.getY() + (int) ((frame.getHeight() -myHeight) / 2),
                        myWidth,
                        myHeight);
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                doCancel();
            }
        });
        JPanel mainPanel = new JPanel();
        mainPanel.add(cancelButton);
        this.cancelButton.setEnabled(false);
        this.getContentPane().add(mainPanel);
        this.doLayout();
    }
    
    public void doClose() {
        this.setVisible(false);
    }
    
    public void setFuture(Future future) {
        this.cancelButton.setEnabled(true);
        this.future = future;
    }
    
    public void doCancel() {
        this.future.cancel(true);
    }
  
}

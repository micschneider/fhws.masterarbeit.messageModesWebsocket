package fhws.masterarbeit.messageModesWebsocket.client;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

public class ProgressDialog extends JDialog implements PartialMessageSendListener {
    private JProgressBar jpb;
    private TitledBorder progressBorder = BorderFactory.createTitledBorder("0% complete");

    private int myWidth = 250;
    private int myHeight = 100;
    
    ProgressDialog(JFrame frame, int piecesCount) {
        super(frame);
        this.jpb = new JProgressBar(1, piecesCount+1);
        this.jpb.setBorder(progressBorder);
        this.setBounds(frame.getX() + (int) ((frame.getWidth() -myWidth) / 2), 
                        frame.getY() + (int) ((frame.getHeight() -myHeight) / 2),
                        myWidth,
                        myHeight);
        this.getContentPane().add(jpb);
        this.setTitle("Sending message...");
    }
    
    @Override
    public void reportProgress(int i) {
        progressBorder.setTitle(i + " % complete");
        this.jpb.setValue(i);
    }
    
    public void doClose() {
        this.setVisible(false);
    }
}

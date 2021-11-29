package zendesk.ticketviewer;

import org.junit.Test;
import org.junit.Assert.*;

import java.io.File;

public class TicketViewerTest {
    @Test
    public void basicTest(){
        Runnable task = () -> {
            File file = new File("input.txt");
            String[] array = new String[1];
            array[0] = file.getAbsolutePath();
            TicketViewer.main(array);
        };
        new Thread(task).start();
        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

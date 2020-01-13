import java.util.timer.*;

class TimerTaskExample extends TimerTask {
        public void run() {
        	println new Date()
        }
}

int delay = 5000   // delay for 5 sec.
int period = 1000  // repeat every sec.
Timer timer = new Timer()
timer.scheduleAtFixedRate(new TimerTaskExample(), delay, period)
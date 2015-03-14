package MarcusD.AudioRepeaterDroid;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity
{
    AudioPipeStream recorder;
    int multiplier = 1;
    CheckBox chk;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.res_main);
        chk = (CheckBox)findViewById(R.id.chk);
        
        chk.setText("Repeat");
        chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean chk)
            {
                if(chk)
                {
                    MainActivity.this.recorder = MainActivity.this.new AudioPipeStream();
                }
                else
                {
                    MainActivity.this.chk.setEnabled(false);
                    MainActivity.this.recorder.stahp();
                    MainActivity.this.recorder.interrupt();
                    try
                    {
                        MainActivity.this.recorder.join();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        MainActivity.this.chk.setEnabled(true);
                    }
                }
            }
        });
        
        SeekBar sb = (SeekBar)findViewById(R.id.multi);
        sb.setMax(4);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser)
            {
                if(fromUser)
                {
                    MainActivity.this.multiplier = progress + 1;
                }
            }
        });
    }
    
    private class AudioPipeStream extends Thread
    { 
        private boolean running = false;
        
        public final int[] rates = new int[] {44100, 22050, 16000, 11025, 800};
        
        private AudioPipeStream()
        { 
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            start();
        }

        @Override
        public void run()
        {
            running = true;
            
            int hz = 0;
            int N = 0;
            short[] buf;
            
            for(int rate : rates)
            {
                N = AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
                if(N > 0)
                {
                    hz = rate;
                    break;
                }
            }
            
            buf = new short[N];
            
            N *= MainActivity.this.multiplier;
            
            if(hz == 0)
            {
                throw new UnsupportedOperationException("No supported sample rate found");
            }
            else
            {
                Log.i("SNDREC", "Sample rate is "+ hz);
            }
            
            AudioRecord rec = null;
            AudioTrack trk = null;
            //short[][] bufz  = new short[256][160];
            //int ix = 0;

            try
            {
                rec = new AudioRecord(AudioSource.MIC, hz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N);
                trk = new AudioTrack(AudioManager.STREAM_MUSIC, hz, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N, AudioTrack.MODE_STREAM);
                rec.startRecording();
                trk.play();
                
                Log.i("SNDREC", "Start'd");

                while(running)
                {
                    Log.v("SNDREC", "Iter");
                    //short[] buf = bufz[ix];
                    N = rec.read(buf,0,buf.length);
                    trk.write(buf, 0, buf.length);
                    //ix = (ix+1) % bufz.length;
                }
                Log.i("SNDREC", "Stahp'd");
            }
            catch(Throwable t)
            { 
                t.printStackTrace();
                Log.e("SNDREC", "RETVAL #" + N);
            }
            finally
            { 
                rec.stop();
                rec.release();
                trk.stop();
                trk.release();
                Log.i("SNDREC", "Disposed");
            }
        }
        
        public void stahp()
        { 
             running = false;
        }

    }
}

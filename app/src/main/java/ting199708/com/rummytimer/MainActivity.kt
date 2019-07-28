package ting199708.com.rummytimer

import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var time = 120
    var min = 0
    var sec = 0
    var count = 0
    val mHandler = Handler()
    var soundPool: SoundPool
    var beep = 0 //sound id
    var beep2 = 0 //sound id
    var beep2_stream = 0
    var running = false
    var player = 0
    var max_player = 4
    private var setting: SharedPreferences? = null

    init {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        beep = soundPool.load(this, R.raw.beep, 1)
        beep2 = soundPool.load(this, R.raw.beep2, 1)
        setting = getSharedPreferences("SETTINGS", 0)
        time = setting!!.getInt("TIME",120)
        max_player = setting!!.getInt("PLAYER",4)
        min=time/60
        sec=time-60*min
        if(sec < 10)
            time_text.text = min.toString()+":0"+sec
        else
            time_text.text = min.toString()+":"+sec
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        when(time) {
            30 -> menu.findItem(R.id.time1).setChecked(true)
            60 -> menu.findItem(R.id.time2).setChecked(true)
            120 -> menu.findItem(R.id.time3).setChecked(true)
        }
        when(max_player) {
            2 -> menu.findItem(R.id.player2).setChecked(true)
            3 -> menu.findItem(R.id.player3).setChecked(true)
            4 -> menu.findItem(R.id.player4).setChecked(true)
            0 -> menu.findItem(R.id.no_player).setChecked(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.player2 -> {
                item.setChecked(true)
                max_player = 2
                change_player()
            }
            R.id.player3 -> {
                item.setChecked(true)
                max_player = 3
                change_player()
            }
            R.id.player4 -> {
                item.setChecked(true)
                max_player = 4
                change_player()
            }
            R.id.no_player -> {
                item.setChecked(true)
                max_player = 0
                change_player()
                player_text.text = ""
            }
            R.id.time1 ->
                if(!item.isChecked) {
                    item.setChecked(true)
                    time = 30
                    change_time()
                }
            R.id.time2 ->
                if(!item.isChecked) {
                    item.setChecked(true)
                    time = 60
                    change_time()
                }
            R.id.time3 ->
                if(!item.isChecked) {
                    item.setChecked(true)
                    time = 120
                    change_time()
                }
        }
        return true
    }

    private val timer = object : Runnable {
        override fun run() {
            count--
            min=count/60
            sec=count-60*min
            update(count)
            if(sec < 10)
                time_text.text = min.toString()+":0"+sec
            else
                time_text.text = min.toString()+":"+sec
            if(count > 0) {
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    fun change_time() {
        //Save
        setting!!.edit()
                .putInt("TIME", time)
                .apply()
        running = false
        mHandler.removeCallbacks(timer)
        min=time/60
        sec=time-60*min
        if(sec < 10)
            time_text.text = min.toString()+":0"+sec
        else
            time_text.text = min.toString()+":"+sec
        pause_button.visibility = View.GONE
        stop_button.visibility = View.GONE
        relativeLayout.setBackgroundColor(Color.WHITE)
        player = 0
    }

    fun change_player() {
        setting!!.edit()
                .putInt("PLAYER", max_player)
                .apply()
        player = 0
    }

    fun update(time : Int) {
        //10秒內
        if(time == 10) {
            soundPool.play(beep, 1f, 1f, 0, 0, 1f)
        } else if(time == 5) {
            beep()
            relativeLayout.setBackgroundColor(Color.RED)
        } else if(time > 0 && time <= 3) {
            beepbeep()
        }
        if(time == 0) beep2()
    }

    fun beep() {
        soundPool.play(beep, 1f, 1f, 0, 0, 1f)
    }
    fun beepbeep() {
        soundPool.play(beep2, 1f, 1f, 0, 0, 1f)
    }
    fun beep2() {
        stop_button.visibility = View.VISIBLE
        pause_button.visibility = View.GONE
        beep2_stream = soundPool.play(beep, 1f, 1f, 0, -1, 1f)
    }

    fun start(v : View) {
        soundPool.stop(beep2_stream)
        relativeLayout.setBackgroundColor(Color.WHITE)
        pause_button.visibility = View.VISIBLE
        pause_button.text = getString(R.string.pause)
        count = time
        min=count/60
        sec=count-60*min
        if(sec < 10)
            time_text.text = min.toString()+":0"+sec
        else
            time_text.text = min.toString()+":"+sec
        if(running) {
            mHandler.removeCallbacks(timer)
        }
        mHandler.postDelayed(timer,1000)
        running = true
        if(max_player != 0) {
            if(player < max_player) player++
            else player = 1
            player_text.text = "Player "+player+"'s Turn"
        }

    }

    fun stop(v: View) {
        stop_button.visibility = View.GONE
        soundPool.stop(beep2_stream)
        relativeLayout.setBackgroundColor(Color.WHITE)
        min=time/60
        sec=time-60*min
        if(sec < 10)
            time_text.text = min.toString()+":0"+sec
        else
            time_text.text = min.toString()+":"+sec
    }
    fun pause(v: View) {
        if(running) {
            running = false
            mHandler.removeCallbacks(timer)
            pause_button.text = getString(R.string.resume)
        } else {
            running = true
            mHandler.postDelayed(timer,1000)
            pause_button.text = getString(R.string.pause)
        }

    }
}
